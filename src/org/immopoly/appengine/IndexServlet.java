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

import com.google.appengine.api.memcache.MemcacheServiceFactory;

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

	static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm", Locale.GERMANY);
	// static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("",
	// Locale.GERMANY);
	private static String colorArray[] = new String[] { "", "black", "green",
			"green", "red" };

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
//			List<User> users = DBManager.getUsers(pm, Long.MAX_VALUE);
//			for (User user : users) {
//				int numExposes = DBManager.getExposes(pm, user.getId()).size();
////				if(0==numExposes && user.getBalance()==5000.00)
////				{
//////					pm.deletePersistent(user);
////				}
////				else
////				{
//					user.setNumExposes(numExposes);
//					pm.makePersistent(user);
////				}
//			}
			MemcacheServiceFactory.getMemcacheService().clearAll();
			// giveAllUSerEarlyAdopterBadge(pm);
//				createDummyBadge(pm);
			// createDummyBadge2(pm);
			// updateDB(pm, 0,0);
			// updateDB(pm, Long.parseLong(req.getParameter("start")),
			// Long.parseLong(req.getParameter("end")));
//			String html = getBase();
//			// top5
//			html = generatetop5(pm, html);
//			// history
//			html = generateHistory(pm, html);
//			resp.getOutputStream().write(html.getBytes("utf-8"));
//			// memcache stats
//			Stats stats = MemcacheServiceFactory.getMemcacheService().getStatistics();
//			LOG.log(Level.INFO,stats.toString());
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "", e);
		} finally {
			pm.close();
		}

	}

	// private String generateHistory(PersistenceManager pm,String html ) {
	// List<History> histories = DBManager.getAllHistory(pm, 10);
	// StringBuffer history = new StringBuffer("");
	// for (History h : histories) {
	// try {
	// User u = DBManager.getUser(pm, h.getUserId());
	// if (u == null) {
	// // TODO schtief dunno why its a local problem
	// LOG.log(Level.WARNING, "user null " + h.getUserId());
	// continue;
	// }
	// // history.append("<p class='c'><span>");
	// history.append("<tr><td><a href='/user/profile/").append(
	// u.getUserName()).append("'>").append(
	// u.getUserName()).append("</a></td><td> ").append(
	// DATE_FORMAT
	// .format(h.getTime() + 2 * 60 * 60 * 1000))
	// .append(
	// "</td><td>")
	// .append(h.getText()).append("</td></tr>");
	// // history.append("</span></p>");
	// } catch (Exception e) {
	// LOG.log(Level.WARNING, h.getText(), e);
	// }
	// }
	// html = html.replace("_HISTORY_", history.toString());
	// return html;
	// }
	//
	// private String generatetop5(PersistenceManager pm, String html) {
	// List<User> top5 = DBManager.getTopUser(pm, 0, 20);
	// StringBuffer t5 = new StringBuffer("");
	// int i = 1;
	// for (User u : top5) {
	// try {
	// t5.append("<trfoo><td>").append(i).append(".</td><td>").append("<a href='/user/profile/");
	// t5.append(u.getUserName()).append("'>").append(u.getUserName()).append("</a></td>");
	// t5.append("<td>").append(History.MONEYFORMAT.format(u.getBalance())).append("</td>");
	// t5.append("</tr>");
	// i++;
	// } catch (Exception e) {
	// LOG.log(Level.WARNING, "lalala ", e);
	// System.out.println(e.getMessage());
	// }
	// }
	//
	// html = html.replace("_TOP5_", t5.toString());
	// return html;
	// }

//	private void createDummyBadge(PersistenceManager pm) {
//		List<User> users = DBManager.getUsers(pm, Long.MAX_VALUE);
//		for (User u : users) {
//			Badge b = new Badge(1, u.getId(), System.currentTimeMillis(), "Du wars schon dabei als Immopoly noch nicht cool war!",
//					"http://immopoly.appspot.com/img/immopoly.png", 0.0,
//					null);
//			pm.makePersistent(b);
//		}
//	}

	private void giveAllUSerEarlyAdopterBadge(PersistenceManager pm) {
		List<User> users = DBManager.getUsers(pm, Long.MAX_VALUE);
		// for (User user : users) {
		// user.giveBadge(pm, Badge.EARLY_ADOPTER,
		// "Du warst schon dabei, als Immopoly nur ein bisschen cool war ;)");
		// }
		LOG.info("Counted all user and gave badges " + users.size());
		Counter counter = DBManager.getLatestCounter(pm);
		counter.setUser(users.size());
		LOG.info("Counter" + counter.toJSON().toString());
		pm.makePersistent(counter);
	}

	private void createDummyBadge2(PersistenceManager pm) {
		{
			User u = DBManager.getUser(pm, "pointkilla");
			Badge b = new Badge(444, u.getId(), System.currentTimeMillis(), "Du hast die Februar 2012 Wertung gewonnen!",
					"http://immopoly.org/img/badges/feb121.png", 0.0, null);
			pm.makePersistent(b);
		}
		{
			User u = DBManager.getUser(pm, "tobi_h");
			Badge b = new Badge(4444, u.getId(), System.currentTimeMillis(), "Du bist 2ter in der Februar 2012 Wertung geworden!",
					"http://immopoly.org/img/badges/feb122.png", 0.0, null);
			pm.makePersistent(b);
		}
		{
			User u = DBManager.getUser(pm, "Tom");
			Badge b = new Badge(44444, u.getId(), System.currentTimeMillis(), "Du bist 3ter in der Februar 2012 Wertung geworden!",
					"http://immopoly.org/img/badges/feb123.png", 0.0, null);
			pm.makePersistent(b);
		}

	}

	private void updateDB(PersistenceManager pm, long start, long end) {
		List<Expose> exposes = DBManager.getExposes(pm);
		LOG.info("updateDB " + start + " - " + end + " number of entries: " + exposes.size());
		for (Expose expose : exposes) {
			if (expose.getDeleted() == Long.MAX_VALUE) {
				if (expose.getLastcalculation() == null) {
					LOG.info("updateDB lastcalculation is null " + expose.getExposeId());
					continue;
				}
				expose.setDeleted(null);
				pm.makePersistent(expose);
			}
		}
		// User u = new User("wwaoname", "2password", "email@email.de",
		// "twitter");
		// u.setBalance(2149127);
		// pm.makePersistent(u);
		//
		// History h = new History(History.TYPE_EXPOSE_ADDED, u.getId(),
		// System.currentTimeMillis(),
		// "jemand hat versucht mit einer Kettensaege in die wohnung einzubrechen und sich dabei den fuss verstaucht",
		// 42, (long) -42);
		// pm.makePersistent(h);

	}

	private String getBase() {
		return readFileAsString("Immopoly.html");
	}

	static String readFileAsString(String filePath) {
		try {
			byte[] buffer = new byte[(int) new File(filePath).length()];
			BufferedInputStream f = new BufferedInputStream(
					new FileInputStream(filePath));
			f.read(buffer);
			return new String(buffer);
		} catch (Exception e) {
			LOG.severe("Konnte template " + filePath + " nich lesen "
					+ e.getMessage());
			return "";
		}
	}
}
