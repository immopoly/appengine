package org.immopoly.appengine;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.immopoly.common.JSONable;
import org.json.JSONArray;
import org.json.JSONException;
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
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class User extends org.immopoly.common.User implements JSONable {
	static Logger LOG = Logger.getLogger(User.class.getName());
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private String username;

	@Persistent
	private String password;

	@Persistent
	private double balance;

	@Persistent
	private String token;

	@Persistent
	private Long lastcalculation;

	@Persistent
	private Double lastRent;

	@Persistent
	private Double lastProvision;

	public User(String name, String password) {
		this.username = name;

		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(password.getBytes(), 0, password.length());
			this.password = new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		this.balance = 5000;
		this.lastcalculation = null;
		generateToken();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return username;
	}

	public JSONObject toJSON() {
		JSONObject resp = new JSONObject();
		JSONObject o = new JSONObject();
		try {
			o.put("username", username);
			o.put("token", token);
			o.put("info", getInfo(false));
			resp.put("org.immopoly.common.User", o);
		} catch (JSONException e) {
			LOG.log(Level.SEVERE, " User.toJSON ", e);
			e.printStackTrace();
		}
		return resp;
	}

	public JSONObject toPublicJSON() {
		JSONObject resp = new JSONObject();
		JSONObject o = new JSONObject();
		try {
			o.put("username", username);
			o.put("info", getInfo(true));
			resp.put("org.immopoly.common.User", o);
		} catch (JSONException e) {
			LOG.log(Level.SEVERE, " User.toJSON ", e);
			e.printStackTrace();
		}
		return resp;
	}
	
	private JSONObject getInfo(boolean pub) throws JSONException {
		JSONObject info = new JSONObject();
		info.put("balance", balance);
		if (null != lastRent)
			info.put("lastRent", lastRent);
		else
			info.put("lastRent", 0);
		if (null != lastProvision)
			info.put("lastProvision", lastProvision);
		else
			info.put("lastProvision", 0);
		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
			// last 10 history
			JSONArray historyList = new JSONArray();
			List<History> history = DBManager.getHistory(pm, id, 10);
			for (History h : history) {
				historyList.put(h.toJSON());
			}
			info.put("historyList", historyList);

			if(!pub){
				// expose
				JSONObject resultlist = new JSONObject();
				JSONArray resultlistEntries = new JSONArray();
				JSONArray resultlistEntry = new JSONArray();
				// alle exposes des users holen
	
				List<Expose> exposes = DBManager.getExposes(pm, id);
				for (Expose expose : exposes) {
					resultlistEntry.put(expose.toJSON());
				}
				resultlistEntries.put(resultlistEntry);
				resultlist.put("resultlistEntries", resultlistEntries);
				info.put("resultlist.resultlist", resultlist);
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, " getInfo ", e);
			e.printStackTrace();
		} finally {
			pm.close();
		}
		return info;
	}

	public void generateToken() {
		this.token = Long.toString(System.currentTimeMillis());
	}

	@Override
	public double getBalance() {
		return balance;
	}

	@Override
	public String getToken() {
		return token;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	// @Override
	// public void setPassword(String password) {
	// this.password = password;
	// }

	@Override
	public void setBalance(double balance) {
		this.balance = balance;
	}

	@Override
	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public void setPortfolio(JSONObject portfolio) {
	}

	@Override
	public void setHistory(List<org.immopoly.common.History> history) {
	}

	@Override
	public org.immopoly.common.History instantiateHistory() {
		return null;
		// return new History();
	}

	public Long getLastcalculation() {
		return lastcalculation;
	}

	public void setLastcalculation(Long lastcalculation) {
		this.lastcalculation = lastcalculation;
	}

	public Double getLastRent() {
		return lastRent;
	}

	public void setLastRent(double lastRent) {
		this.lastRent = lastRent;
	}

	public Double getLastProvision() {
		return lastProvision;
	}

	public void setLastProvision(double lastProvision) {
		this.lastProvision = lastProvision;
	}

}