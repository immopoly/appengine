package org.immopoly.appengine;

import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	protected ActionExposeAdd(Map<String, Action> actions) {
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
				throw new ImmopolyException("missing token", 61);

			User user = DBManager.getUserByToken(pm, token);
			if (null == user)
				throw new ImmopolyException("token not found " + token, 62);

			History history = null;
			// first check if already owned
			Expose expose = DBManager.getExpose(pm, exposeId);
			if (null != expose) {
				if (expose.getUserId() == user.getId()) {
					throw new ImmopolyException("geh�rt dir schon du penner", 201);
				} else {
					// history eintrag
					// other user
					User otherUser = DBManager.getUser(pm, expose.getUserId());
					// minus 30tel
					double fine = 2 * expose.getRent() / 30.0;
					user.setBalance(user.getBalance() - fine);
					if (null != otherUser)
						otherUser.setBalance(otherUser.getBalance() + fine);

					history = new History(History.TYPE_EXPOSE_MONOPOLY_NEGATIVE, user.getId(), System.currentTimeMillis(), "Die Wohnung '"
							+ expose.getName() + "' geh�rt schon '" + otherUser.getUserName() + "' Strafe "
							+ History.MONEYFORMAT.format(fine), fine);
					if (null != otherUser) {
						History otherHistory = new History(History.TYPE_EXPOSE_MONOPOLY_POSITIVE, otherUser.getId(), System
								.currentTimeMillis(), "Jemand wollte deine Wohnung '" + expose.getName() + "' �bernehmen: Belohung "
								+ History.MONEYFORMAT.format(fine), fine);
						pm.makePersistent(otherHistory);
					}
					pm.makePersistent(history);
					pm.makePersistent(user);
					if (null != otherUser)
						pm.makePersistent(otherUser);
				}
			} else {
				URL url = new URL(OAuthData.SERVER + OAuthData.SEARCH_PREFIX + "expose/" + exposeId + ".json");
				JSONObject obj = WebHelper.getHttpData(url);
				if (obj.has("expose.expose")) {
					expose = new Expose(user.getId(), obj);
					// nur wohnungen mit rent
					if (expose.getRent() == 0.0)
						throw new ImmopolyException("Expose hat keinen Wert f�r Kaltmiete, sie kann nicht �bernommen werden", 302);
					
					//check distance to last exposes https://github.com/immopoly/immopoly/issues/26
					if(!checkDistance(pm,expose))
						throw new ImmopolyException("SPOOFING ALERT", 441);
					
					pm.makePersistent(expose);
					double fine = 2 * expose.getRent() / 30.0;
					history = new History(History.TYPE_EXPOSE_ADDED, user.getId(), System.currentTimeMillis(), "Du hast die Wohnung '"
							+ expose.getName() + "' gemietet f�r " + History.MONEYFORMAT.format(expose.getRent())
							+ " im Monat. �bernahmekosten: " + History.MONEYFORMAT.format(fine), fine);
					user.setBalance(user.getBalance() - fine);
					pm.makePersistent(user);
					pm.makePersistent(history);
				} else if (obj.toString().contains("ERROR_RESOURCE_NOT_FOUND"))
					throw new ImmopolyException("expose jibs nich", 301);
			}
			// history eintrag
			resp.getOutputStream().write(history.toJSON().toString().getBytes("UTF-8"));
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException("could not add expose ", 101, e);
		} finally {
			pm.close();
		}
	}

	private boolean checkDistance(PersistenceManager pm, Expose expose) {
		//get last x entries
		List<Expose> lastExposes	= DBManager.getLastExposes(pm, expose.getUserId(),System.currentTimeMillis()-(60*60*1000));
		LOG.info("lastExposes "+lastExposes.size() + " userId: " +expose.getUserId()+" "+(System.currentTimeMillis()-(60*60*1000)));
		for (Expose e : lastExposes) {
			//wenn e weiter weg ist als MAX_SPOOFING_METER_PER_SECOND per return false
			double distance = calcDistance(expose.getLatitude(),expose.getLongitude(),e.getLatitude(),e.getLongitude());
			double distancePerSecond=distance/((System.currentTimeMillis()-e.getTime())/1000);
			LOG.info("distance "+distance+" distancePerSecond "+distancePerSecond+" max "+Const.MAX_SPOOFING_METER_PER_SECOND);
			if(distancePerSecond>Const.MAX_SPOOFING_METER_PER_SECOND){
				LOG.severe("distance "+distance+" distancePerSecond "+distancePerSecond+" max "+Const.MAX_SPOOFING_METER_PER_SECOND);
				return false;
			}
		}
		return true;
	}
	 public static double calcDistance(double lat1, double lng1, double lat2, double lng2) {
		    double earthRadius = 3958.75;
		    double dLat = Math.toRadians(lat2-lat1);
		    double dLng = Math.toRadians(lng2-lng1);
		    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		               Math.sin(dLng/2) * Math.sin(dLng/2);
		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    double dist = earthRadius * c;

		    int meterConversion = 1609;

		    return new Double(dist * meterConversion).doubleValue();
 }
}
