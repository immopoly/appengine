package org.immopoly.appengine.actions;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immopoly.appengine.Const;
import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.Expose;
import org.immopoly.appengine.History;
import org.immopoly.appengine.ImmopolyC2DMMessaging;
import org.immopoly.appengine.OAuthData;
import org.immopoly.appengine.PMF;
import org.immopoly.appengine.User;
import org.immopoly.appengine.WebHelper;
import org.immopoly.common.ImmopolyException;
import org.json.JSONObject;

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
public class ActionExposeAdd extends AbstractAction implements Action {

	public ActionExposeAdd(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "portfolio/add";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String token = req.getParameter(TOKEN);
			String exposeId = req.getParameter(EXPOSE);

			if (null == token || token.length() == 0)
				throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_TOKEN, "missing token");

			User user = DBManager.getUserByToken(pm, token);
			if (null == user)
				throw new ImmopolyException(ImmopolyException.TOKEN_NOT_FOUND, "token not found " + token);

			History history = null;
			// first check if already owned
			Expose expose = DBManager.getExpose(pm, exposeId);
			if (null != expose) {
				if (expose.getUserId() == user.getId()) {
					throw new ImmopolyException(ImmopolyException.EXPOSE_ALREADY_IN_PORTFOLIO, "gehört dir schon du penner");
				} else {
					// history eintrag
					// other user
					User otherUser = DBManager.getUser(pm, expose.getUserId());
					if (null == otherUser) {
						throw new ImmopolyException(ImmopolyException.USERNAME_NOT_FOUND,
								"user of other expose is null!!! gebe verrückten Fehler zurück");
					}
					// minus 30tel
					double fine = 2 * expose.getRent() / 30.0;
					user.setBalance(user.getBalance() - fine);

					otherUser.setBalance(otherUser.getBalance() + fine);

					history = new History(History.TYPE_EXPOSE_MONOPOLY_NEGATIVE, user.getId(), System.currentTimeMillis(), "Strafe: "
							+ History.MONEYFORMAT.format(fine) + " Die Wohnung '" + expose.getName() + "' gehört schon '"
							+ otherUser.getUserName() + "'", -fine, expose.getExposeId(), user.getUserName(), otherUser.getUserName());
					History otherHistory = new History(History.TYPE_EXPOSE_MONOPOLY_POSITIVE, otherUser.getId(),
							System.currentTimeMillis(), "Belohnung: " + History.MONEYFORMAT.format(fine) + " " + user.getUserName()
									+ " wollte deine Wohnung '" + expose.getName() + "' übernehmen", fine, expose.getExposeId(),
							otherUser.getUserName(), user.getUserName());
					pm.makePersistent(otherHistory);
					// c2dm
					try{
						if (null != otherUser.getC2dmRegistrationId() && otherUser.getC2dmRegistrationId().length() > 0) {
							ImmopolyC2DMMessaging c2dm = new ImmopolyC2DMMessaging();
							Map<String, String[]> params = new HashMap<String, String[]>();
							// type message title
							params.put("data.type", new String[] { "1" });
							params.put("data.message", new String[] { otherHistory.getText() });
							params.put("data.title", new String[] { "Immopoly" });
							c2dm.sendNoRetry(otherUser.getC2dmRegistrationId(), "mycollapse", params, true);
							LOG.info("Send c2dm message to" + otherUser.getUserName() + " " + history.getText());
						}
					}catch(Exception e){
						LOG.log(Level.WARNING, "Send c2dm message to" + otherUser.getUserName() + " FAILED ", e);
					}
					
					pm.makePersistent(history);
					pm.makePersistent(user);
					if (null != otherUser)
						pm.makePersistent(otherUser);
					// #47
					expose.addOvertake();
					pm.makePersistent(expose);
				}
			} else {
				URL url = new URL(OAuthData.SERVER + OAuthData.SEARCH_PREFIX + "expose/" + exposeId + ".json");
				JSONObject obj = WebHelper.getHttpData(url);
				if (null == obj) {
					throw new ImmopolyException(ImmopolyException.EXPOSE_NOT_FOUND, "Expose Response is null! " + exposeId);
				} else if (obj.has("expose.expose")) {
					LOG.info(obj.toString());
					expose = new Expose(user.getId(), obj);
					// nur wohnungen mit rent
					if (expose.getRent() == 0.0)
						throw new ImmopolyException(ImmopolyException.EXPOSE_NO_RENT,
								"Expose hat keinen Wert für Kaltmiete, sie kann nicht übernommen werden");

					// und nur Provisionspflichtige ;)......
					// if (!expose.isCourtage())
					// throw new
					// ImmopolyException(ImmopolyException.EXPOSE_NO_COURTAGE,
					// "Wohnung ist nicht provisionspflichtig, du willst doch was verdienen oder?");

					// check for maxExposes 30
					if (user.getNumExposes() != null && user.getNumExposes() >= 50)
						throw new ImmopolyException(ImmopolyException.EXPOSE_MAX_NUM,
								"Du hast schon 50 Wohnungen in deinem Portfolio, du solltest es lieber optimieren!");

					// check distance to last exposes
					// https://github.com/immopoly/immopoly/issues/26
					checkDistance(pm, user, expose);
					// throw new ImmopolyException("SPOOFING ALERT", 441);
					pm.makePersistent(expose);
					double fine = 2 * expose.getRent() / 30.0;
					history = new History(History.TYPE_EXPOSE_ADDED, user.getId(), System.currentTimeMillis(), "Übernahmekosten: "
							+ History.MONEYFORMAT.format(fine) + " Du hast die Wohnung '" + expose.getName() + "' gemietet für "
							+ History.MONEYFORMAT.format(expose.getRent()) + " im Monat", -fine, expose.getExposeId(), user.getUserName(),
							null);
					user.setBalance(user.getBalance() - fine);
					user.addExpose(1);
					pm.makePersistent(user);
					pm.makePersistent(history);
				} else if (obj.toString().contains("RESOURCE_NOT_FOUND"))
					throw new ImmopolyException(ImmopolyException.EXPOSE_NOT_FOUND, "expose jibs nich");
			}
			// history eintrag
			resp.getOutputStream().write(history.toJSON().toString().getBytes("UTF-8"));
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
