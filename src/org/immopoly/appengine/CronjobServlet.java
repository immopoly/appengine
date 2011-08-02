package org.immopoly.appengine;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;
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
public class CronjobServlet extends HttpServlet {

	static Logger LOG = Logger.getLogger(CronjobServlet.class.getName());
	// 23h
	private static final long TIME_CALC_DIFF = 23 * 60 * 60 * 1000;
	private static final double DAILY_RENT_FRACTION = 30.0;
	private static final double PROVISON_MULTIPLIER = 2.0;

	// format fuer Waehrung in der Historie
	private static DecimalFormat MONEYFORMAT = new DecimalFormat("0.00 Eur");

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		LOG.info("starte Mietberechnung");
		// logHeaders(req);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		int count = 1;
		try {
			// alle benutzer die seit TIME_CALC_DIFF millisekunden nicht
			// berechnet worden sind
			List<User> users = DBManager.getUsers(pm, System.currentTimeMillis() - (TIME_CALC_DIFF));
			// List<User> users =new ArrayList<User>();
			// users.add(DBManager.getUser(pm, "mrschtief"));
			for (User user : users) {
				if (null == user) {
					LOG.log(Level.WARNING, "user is null");
					continue;
				}
				LOG.info(" User " + user.getUserName());
				resp.getWriter().write(" User " + user.getUserName());

				// falls die berechnung länger als 1 Zyklus (siehe cron.xml)
				// dauert nicht wieder den gleichen Benutzer nehmen
				user.setLastcalculation(System.currentTimeMillis());
				pm.makePersistent(user);

				double rent = 0;
				double provision = 0;
				// get all Exposes des users
				List<Expose> exposes = DBManager.getExposes(pm, user.getId());
				LOG.info(" User " + user.getUserName() + " Exposes #" + exposes.size());

				for (Expose expose : exposes) {
					resp.getWriter().write(expose.getExposeId() + " <br>");
					LOG.info(count + " " + expose.getExposeId());

					// schauen ob es noch da ist
					URL url = new URL(OAuthData.SERVER + OAuthData.SEARCH_PREFIX + "expose/" + expose.getExposeId() + ".json");
					JSONObject obj = WebHelper.getHttpData(url);
					if (null == obj) {
						LOG.log(Level.WARNING, "Expose Response is null! " + expose.getExposeId());
						count++;
						continue;
					}
					if (obj.has("expose.expose")) {
						// falls noch da, dann die Miete berechnen
						rent += expose.getRent() / DAILY_RENT_FRACTION;
					} else {
						// falls nein provision drauf
						provision += PROVISON_MULTIPLIER * expose.getRent();

						// Historieneintrag erstellen
						History history = new History(History.TYPE_EXPOSE_SOLD, user.getId(), System.currentTimeMillis(), "Wohnung '"
								+ expose.getName() + "' vermietet. Provision überwiesen: "
								+ MONEYFORMAT.format(PROVISON_MULTIPLIER * expose.getRent()), PROVISON_MULTIPLIER * expose.getRent());

						resp.getWriter().write(history.getText() + "<br>");
						LOG.info(history.getText());

						pm.makePersistent(history);

						// loeschen
						pm.deletePersistent(expose);
					}
					count++;
				}
				LOG.info("update User");
				user.setBalance(user.getBalance() + (provision - rent));
				user.setLastProvision(provision);
				user.setLastRent(rent);

				History historyRent = new History(History.TYPE_DAILY_RENT, user.getId(), System.currentTimeMillis(),
						"Tagesabrechnung Miete: " + MONEYFORMAT.format(rent), rent);
				History historyProvision = new History(History.TYPE_DAILY_PROVISION, user.getId(), System.currentTimeMillis(),
						"Tagesabrechnung Provision: " + MONEYFORMAT.format(provision), provision);

				resp.getWriter().write(historyRent.getText() + "<br>");
				resp.getWriter().write(historyProvision.getText() + "<br>");
				resp.getWriter().flush();

				LOG.info(historyRent.getText());
				LOG.info(historyProvision.getText());

				pm.makePersistent(historyRent);
				pm.makePersistent(historyProvision);
				user.setLastcalculation(System.currentTimeMillis());
				LOG.info("save User");
				pm.makePersistent(user);
				break;
			}
			resp.getWriter().write("OK ");
		} catch (Exception e) {
			LOG.log(Level.SEVERE, " Abbruch ", e);
			e.printStackTrace(resp.getWriter());
		} finally {
			pm.close();
			LOG.info("finally");
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
