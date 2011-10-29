package org.immopoly.appengine;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class IndexServlet extends HttpServlet {

	static Logger LOG = Logger.getLogger(IndexServlet.class.getName());

	public final String LOGIN = "login";

	static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
	// static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("",
	// Locale.GERMANY);
	private static String colorArray[] = new String[] { "", "black", "green", "green", "red" };

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			// filldummydb(pm);
			String html = getBase();
			// top5
			List<User> top5 = DBManager.getTopUser(pm, 20);
			StringBuffer t5 = new StringBuffer("");
			int i = 1;
			for (User u : top5) {
				try {
					if (i > 1)
						t5.append(", ");
					t5.append(i).append(". <a href='/user/profile/").append(u.getUserName()).append("'>").append(u.getUserName()).append("</a> ")
							.append(History.MONEYFORMAT.format(u.getBalance()));
					i++;
				} catch (Exception e) {
					LOG.log(Level.WARNING, "lalala ", e);
				}
			}
			html = html.replace("_TOP5_", t5.toString());

			// history
			List<History> histories = DBManager.getAllHistory(pm, 10);
			StringBuffer history = new StringBuffer("");
			for (History h : histories) {
				try {
					User u = DBManager.getUser(pm, h.getUserId());
					// history.append("<p class='c'><span>");
					history.append("<li><a href='/user/profile/").append(u.getUserName()).append("'>").append(u.getUserName()).append("</a> ").append(DATE_FORMAT.format(h.getTime() + 2 * 60 * 60 * 1000))
							.append("<br/><p style='font-size:smaller;color:" + colorArray[h.getType()] + "'>").append(h.getText()).append(
									"</p></li>");
					// history.append("</span></p>");
				} catch (Exception e) {
					LOG.log(Level.WARNING, h.getText(), e);
				}
			}
			html = html.replace("_HISTORY_", history.toString());
			resp.getWriter().write(html);
		} finally {
			pm.close();
		}

	}

	private void filldummydb(PersistenceManager pm) {
		User u = new User("name", "password", "email@email.de", "twitter");
		u.setBalance(154987);
		pm.makePersistent(u);

		History h = new History(History.TYPE_EXPOSE_ADDED, u.getId(), System
				.currentTimeMillis(), "text", 42);
		pm.makePersistent(h);

	}

	private String getBase() {
		return readFileAsString("Immopoly.html");
	}

	static String readFileAsString(String filePath) {
		try {
			byte[] buffer = new byte[(int) new File(filePath).length()];
			BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
			return new String(buffer);
		} catch (Exception e) {
			LOG.severe("Konnte template " + filePath + " nich lesen " + e.getMessage());
			return "";
		}
	}
}
