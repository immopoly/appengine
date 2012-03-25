package org.immopoly.appengine.actions;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immopoly.appengine.ActionItem;
import org.immopoly.appengine.Const;
import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.Expose;
import org.immopoly.appengine.History;
import org.immopoly.appengine.PMF;
import org.immopoly.appengine.User;
import org.immopoly.common.ImmopolyException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/*
 This is the server side Google App Engine component of Immopoly
 http://immopoly.appspot.com
 Copyright (C) 2011 Mister Schtief

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ActionUserAction extends AbstractAction implements Action {

	public ActionUserAction(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/action";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			JSONObject jsonRequest = new JSONObject(new JSONTokener(req.getInputStream(), "UTF-8"));
			String token = jsonRequest.getString(TOKEN);
			int actiontype = jsonRequest.getInt(ACTIONTYPE);
			JSONArray exposes = jsonRequest.getJSONArray(EXPOSES);

			if (null == token || token.length() == 0)
				throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_TOKEN, "missing token");

			User user = DBManager.getUserByToken(pm, token);
			if (null == user)
				throw new ImmopolyException(ImmopolyException.TOKEN_NOT_FOUND, "token not found " + token);

			// check for action available
			List<ActionItem> actionItems = DBManager.getActionItems(pm, user.getId(), actiontype);
			if (null == actionItems || actionItems.size() == 0 || actionItems.get(0).getAmount() == 0)
				throw new ImmopolyException(ImmopolyException.ACTIONITEM_NOTFOUND, "ActionItem not found or empty " + actiontype);

			// decrement amount
			ActionItem actionItem = actionItems.get(0);
			actionItem.setAmount(actionItem.getAmount() - 1);
			pm.makePersistent(actionItem);

			// History Eintrag
			History history = new History(History.TYPE_ACTIONITEM, user.getId(), System.currentTimeMillis(), "Aktion: "
					+ user.getUserName() + " spielt " + actionItem.getText(), null, null, user.getUserName(), null);
			pm.makePersistent(history);
			// filter
			JSONArray freeExposes = new JSONArray();
			for (int i = 0; i < exposes.length(); i++) {
				int exposeId = exposes.getInt(i);
				LOG.info("check " + exposeId);
				if (null == DBManager.getExpose(pm, Integer.toString(exposeId))) {
					freeExposes.put(exposeId);
					LOG.info("added " + exposeId);
				}
			}

			resp.getOutputStream().write(freeExposes.toString().getBytes("UTF-8"));
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException(ImmopolyException.EXPOSE_ADD_FAILED, "could not add expose ", e);
		} finally {
			pm.close();
		}
	}
	
	
	private void checkDistance(PersistenceManager pm, User user, Expose expose) {
		Expose lastExpose = DBManager.getLastExposeForUser(pm, expose.getUserId());
		if (null != lastExpose) {
			double distance = calcDistance(expose.getLatitude(), expose.getLongitude(), lastExpose.getLatitude(), lastExpose.getLongitude());
			double distancePerSecond = distance / ((expose.getTime() - lastExpose.getTime()) / 1000);
			LOG.info("distance " + distance + " distancePerSecond " + distancePerSecond + " max " + Const.MAX_SPOOFING_DISTANCE_PER_SECOND);
			if (distance > Const.MAX_SPOOFING_DISTANCE && distancePerSecond > Const.MAX_SPOOFING_DISTANCE_PER_SECOND) {
				LOG.severe("LOCATION SPOOFING ALERT!! " + user.getUserName() + " !! distance " + distance + " distancePerSecond "
						+ distancePerSecond + " max " + Const.MAX_SPOOFING_DISTANCE_PER_SECOND);
			}
		}
	}

	

	public static double calcDistance(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		int meterConversion = 1609;

		return new Double(dist * meterConversion).doubleValue();
	}
}
