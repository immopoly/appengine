package org.immopoly.appengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
public class UserCronjobServlet extends HttpServlet {

	static Logger LOG = Logger.getLogger(UserCronjobServlet.class.getName());
	// 23h
	private static final long TIME_CALC_DIFF = 24 * 60 * 60 * 1000;
	private static final double DAILY_RENT_FRACTION = 30.0;
	private static final double PROVISON_MULTIPLIER = 2.0;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// logHeaders(req);
		PersistenceManager pm = PMF.get().getPersistenceManager();
//		int count = 1;
		try {
			// alle benutzer die seit TIME_CALC_DIFF millisekunden nicht
			// berechnet worden sind
			long lastCalculation =System.currentTimeMillis() - TIME_CALC_DIFF;
			List<User> users;
			boolean debug=false;
			
			if(null!=req.getParameter("username")){
				debug=true;
				users= new ArrayList<User>();
				users.add(DBManager.getUser(pm, req.getParameter("username")));
			}
			else
				users	= DBManager.getUsersToCheck(pm, lastCalculation, 10);
			//			LOG.log(Level.INFO, System.currentTimeMillis()+" users to check since "+lastCalculation);

			// List<User> users =new ArrayList<User>();
			// users.add(DBManager.getUser(pm, "mrschtief"));
			for (User user : users) {
				if (null == user) {
					LOG.log(Level.SEVERE, "user is null");
					continue;
				}
				if (!debug && user.getLastcalculation() != null && user.getLastcalculation() > System.currentTimeMillis() - TIME_CALC_DIFF) {
					LOG.log(Level.SEVERE, "user is already calculated in last timeframe");
					continue;
				}

				// falls die Berechnung länger als 1 Zyklus (siehe cron.xml)
				// dauert nicht wieder den gleichen Benutzer nehmen
				// TODO schtief noch nötig???
				user.setLastcalculation(System.currentTimeMillis());
				pm.makePersistent(user);

				//count all sold
				//only once countAllSold(pm,user);
				
				//give numexposesoldbadge
				giveNumberExposesSoldBadge(pm, user);
				
				double rent = 0;
				double provision = 0;
				// int numRemoved = 0;
				int numRent = 0;
				// get all Exposes des users
				List<Expose> exposes = DBManager.getExposesForUserToCheck(pm, user.getId()/*, System.currentTimeMillis() - (TIME_CALC_DIFF)*/);
				// TODO schtief count exposes each night brauchen wir noch?
				// user.setNumExposes(exposes.size());
				LOG.info("User " + user.getUserName() + " Exposes #" + exposes.size());
				resp.getWriter().write(" User " + user.getUserName() + " Exposes #" + exposes.size()+"<br>");

				for (Expose expose : exposes) {
					// schauen ob es noch da ist
					// es ist noch da, wenn deleted == null ist
					if (expose.getDeleted() == null) {
						// falls noch da, dann die Miete berechnen
						rent += expose.getRent() / DAILY_RENT_FRACTION;
						numRent++;
						resp.getWriter().write(expose.getExposeId()+" OK <br>");
						LOG.info(expose.getExposeId()+" OK");
					} else {
						// falls nein provision drauf
						provision += PROVISON_MULTIPLIER * expose.getRent();
						// numRemoved++;
						// wird jetzt im ExposeCronjob gemacht
						// Historieneintrag erstellen
						// History history = new
						// History(History.TYPE_EXPOSE_SOLD, user.getId(),
						// System.currentTimeMillis(), "Wohnung '"
						// + expose.getName() +
						// "' vermietet. Provision überwiesen: "
						// + MONEYFORMAT.format(PROVISON_MULTIPLIER *
						// expose.getRent()), PROVISON_MULTIPLIER *
						// expose.getRent(),
						// expose.getExposeId());
						//
						// resp.getWriter().write(history.getText() + "<br>");
						// LOG.info(history.getText());
						//
						// pm.makePersistent(history);

						// wird jetzt im ExposeCronjob gemacht
						// nicht mehr loeschen sondern nur noch markieren
						// expose.setDeleted(System.currentTimeMillis());
						// pm.makePersistent(expose);
						resp.getWriter().write(expose.getExposeId()+" Sold! <br>");
						LOG.info(expose.getExposeId()+" Sold!");
						// jetzt lastcalculation auf null setzen damit es nicht
						// wieder geholt wird
						expose.setLastcalculation(null);
						pm.makePersistent(expose);
					}
				}
				// check miete mit num wohnungen
				if (user.getNumExposes() != numRent) {
					LOG.log(Level.SEVERE, "NumExposes " + user.getNumExposes() + "!= numRent " + numRent);
					user.setNumExposes(numRent);
				}

				LOG.info("update User");
				// Provision wird jetzt im ExposeCronjob gemacht
				user.setBalance(user.getBalance() - rent);
				// user.addExpose(-numRemoved);

				user.setLastProvision(provision);
				user.setLastRent(rent);

				//monthly balance
				//delete on first of month
				if(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)==1)
					user.setBalanceMonth(0);
				user.setBalanceMonth(user.getBalanceMonth() - rent+provision);
				
				if(numRent!=0)
				{
					History historyRent = new History(History.TYPE_DAILY_RENT, user.getId(), System.currentTimeMillis(), "Miete: "
							+ History.MONEYFORMAT.format(rent) + " Tagesabrechnung Miete für " + user.getNumExposes() + " Wohnungen", rent,
							null,
							null);
					// History historyProvision = new
					// History(History.TYPE_DAILY_PROVISION, user.getId(),
					// System.currentTimeMillis(),
					// "Tagesabrechnung Provision: " +
					// MONEYFORMAT.format(provision), provision, null);
	
					resp.getWriter().write(historyRent.getText() + "<br>");
					LOG.info(historyRent.getText());
					// LOG.info(historyProvision.getText());

					pm.makePersistent(historyRent);
				}
				// resp.getWriter().write(historyProvision.getText() + "<br>");
				resp.getWriter().flush();


				// pm.makePersistent(historyProvision);
				user.setLastcalculation(System.currentTimeMillis());
				LOG.info("save User "+System.currentTimeMillis());
				pm.makePersistent(user);
			}
			resp.getWriter().write("OK ");
		} catch (Exception e) {
			LOG.log(Level.SEVERE, " Abbruch ", e);
			e.printStackTrace(resp.getWriter());
		} finally {
			pm.close();
			// LOG.info("finally");
		}
	}

	private void giveNumberExposesSoldBadge(PersistenceManager pm,User user) {
		if(user.getNumExposesSold()>=10)
			giveNumberExposesSoldBadge(pm, user,10);
		if(user.getNumExposesSold()>=30)
			giveNumberExposesSoldBadge(pm, user,30);
		if(user.getNumExposesSold()>=60)
			giveNumberExposesSoldBadge(pm, user,60);
		if(user.getNumExposesSold()>=80)
			giveNumberExposesSoldBadge(pm, user,80);
		if(user.getNumExposesSold()>=100)
			giveNumberExposesSoldBadge(pm, user,100);
	}

	private void giveNumberExposesSoldBadge(PersistenceManager pm, User user, int num) {
		//check if Badge already given
		List<Badge> badges = DBManager.getBadges(pm, user.getId(),100000+num, 0, 1);
		if(null!=badges && badges.size()>0){
			LOG.info("Badge already given "+100000+num);
			return;
		}
		Badge b = new Badge(100000+num, user.getId(), System.currentTimeMillis(), "Du hast "+num+" Wohnungen vermietet!",
				"http://immopoly.org/img/badges/"+num+".png", 0.0,	null);
		pm.makePersistent(b);
		try{
			if (null != user.getC2dmRegistrationId() && user.getC2dmRegistrationId().length() > 0) {
				ImmopolyC2DMMessaging c2dm = new ImmopolyC2DMMessaging();
				Map<String, String[]> params = new HashMap<String, String[]>();
				// type message title
				params.put("data.type", new String[] { "1" });
				params.put("data.message", new String[] { b.getText() });
				params.put("data.title", new String[] { "Immopoly" });
				c2dm.sendNoRetry(user.getC2dmRegistrationId(), "mycollapse", params, true);
				LOG.info("Send c2dm message to" + user.getUserName() + " " + b.getText());
			}
		}catch(Exception e){
			LOG.log(Level.SEVERE,"Send c2dm message to" + user.getUserName() + " FAILED ",e);									
		}
		}

	private void countAllSold(PersistenceManager pm, User user) {
		// get all expose deleted > 0
		List<Expose> sold = DBManager.getSoldExposes(pm, user.getId());
		user.setNumExposesSold(sold.size());
		LOG.info("User "+user.getUserName()+" sold Expose : "+user.getNumExposesSold());
	}

	private void logHeaders(HttpServletRequest req) {
		Enumeration en = req.getHeaderNames();
		while (en.hasMoreElements()) {
			String header = en.nextElement().toString();
			LOG.info("header" + header + " : " + req.getHeader(header).toString());
		}
	}
}
