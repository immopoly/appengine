package org.immopoly.appengine;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
@SuppressWarnings("serial")
public class ExposeCronjobServlet extends HttpServlet {

	static Logger LOG = Logger.getLogger(ExposeCronjobServlet.class.getName());
	// 23h
	private static final long TIME_CALC_DIFF = 24 * 60 * 60 * 1000;
	private static final double PROVISON_MULTIPLIER = 2.0;

	// format fuer Waehrung in der Historie
	private static DecimalFormat MONEYFORMAT = new DecimalFormat("0.00 Eur");

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		// logHeaders(req);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// int count = 1;
		try {

			// pro Minute X Exposes holen deren lastChecked Datum mindestens 24h
			// zurückliegt.
			List<Expose> exposes = DBManager.getExposesToCheck(pm, System.currentTimeMillis() - (TIME_CALC_DIFF), 50);
			if(exposes.size()>0)
				LOG.info("checke exposes #" + exposes.size());
			// TODO schtief wenn lastcalculation null ist dann anders holen
			for (Expose expose : exposes) {
				// TODO schtief check deleted before
				if (expose.getDeleted() != Long.MAX_VALUE) {
					expose.setLastcalculation(null);
				} else {
					// Verfügbarkeit bei is24 checken.
					URL url = new URL(OAuthData.SERVER + OAuthData.SEARCH_PREFIX + "expose/" + expose.getExposeId() + ".json");
					JSONObject obj = WebHelper.getHttpData(url);
					if (null == obj) {
						LOG.log(Level.SEVERE, "Expose Response is null! " + expose.getExposeId());
						continue;
					}
					expose.setLastcalculation(System.currentTimeMillis());
					// Wenn verfügbar, lastChecked Datum setzen
					if (obj.has("expose.expose")) {
						// LOG.info("ExposeId " + expose.getExposeId() + " OK");
						resp.getWriter().write("ExposeId " + expose.getExposeId() + " OK" + " <br>");
					} else {
						LOG.log(Level.FINE, "Expose " + obj.toString());
						// Wenn vermietet, lastChecked Datum setzen und als
						// vermietet Kennzeichnen
						User user = DBManager.getUser(pm, expose.getUserId());
						if (null == user) {
							LOG.log(Level.SEVERE, "User is null! " + expose.getUserId());
						} else {
							LOG.info("ExposeId " + expose.getExposeId() + " SOLD!");
							resp.getWriter().write("ExposeId " + expose.getExposeId() + " SOLD!" + " <br>");
							// Historieneintrag erstellen
							History history = new History(History.TYPE_EXPOSE_SOLD, user.getId(), System.currentTimeMillis(), "Wohnung '"
									+ expose.getName() + "' vermietet. Provision überwiesen: "
									+ MONEYFORMAT.format(PROVISON_MULTIPLIER * expose.getRent()), PROVISON_MULTIPLIER * expose.getRent(),
									expose.getExposeId(),null);

							resp.getWriter().write(history.getText() + "<br>");
							// LOG.info(history.getText());
							pm.makePersistent(history);

							// add provision to user
							user.setBalance(user.getBalance() + PROVISON_MULTIPLIER * expose.getRent());
							user.addExpose(-1);
							pm.makePersistent(user);

							// notification
							// https://github.com/immopoly/appengine/issues/9
							// c2dm
							if (null != user.getC2dmRegistrationId() && user.getC2dmRegistrationId().length() > 0) {
								try{
									ImmopolyC2DMMessaging c2dm = new ImmopolyC2DMMessaging();
									Map<String, String[]> params = new HashMap<String, String[]>();
									// type message title
									params.put("data.type", new String[] { "1" });
									params.put("data.message", new String[] { history.getText() });
									params.put("data.title", new String[] { "Immopoly" });
									c2dm.sendNoRetry(user.getC2dmRegistrationId(), "mycollapse", params, true);
									LOG.info("Send c2dm message to" + user.getUserName() + " " + history.getText());
									resp.getWriter().write("Send c2dm message to" + user.getUserName() + " " + history.getText());
								}catch(Exception e){
									LOG.log(Level.SEVERE,"Send c2dm message to" + user.getUserName() + " FAILED ",e);									
								}
							}
							// nicht mehr loeschen sondern nur noch markieren
							expose.setDeleted(expose.getLastcalculation());
						}
					}
				}
				pm.makePersistent(expose);
			}

			resp.getWriter().flush();
			resp.getWriter().write("OK");
		} catch (Exception e) {
			LOG.log(Level.SEVERE, " Abbruch ", e);
			e.printStackTrace(resp.getWriter());
		} finally {
			pm.close();
			// LOG.info("finally");
		}
	}

	private void logHeaders(HttpServletRequest req) {
		Enumeration en = req.getHeaderNames();
		while (en.hasMoreElements()) {
			String header = en.nextElement().toString();
			LOG.info("header" + header + " : " + req.getHeader(header).toString());
		}
	}
}
