package org.immopoly.appengine;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class User extends org.immopoly.common.User implements JSONable, Serializable {
	static Logger LOG = Logger.getLogger(User.class.getName());
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private String username;

	@Persistent
	private String password;

	@Persistent
	private String email;

	@Persistent
	private String twitter;

	@Persistent
	private double balance;

	@Persistent
	private Double balanceMonth;

	@Persistent
	private String token;

	@Persistent
	private Long lastcalculation;

	@Persistent
	private Double lastRent;

	@Persistent
	private Double lastProvision;

	@Persistent
	private String c2dmRegistrationId;

	@Persistent
	private Integer numExposes;

	@Persistent
	private Integer numExposesSold;

	@Persistent
	private Boolean releaseBadge = Boolean.FALSE;


	public User(String name, String password, String email, String twitter) {
		this.username = name;
		this.email = email;
		this.twitter = twitter;
		this.password = digestPassword(password);
		this.balance = 5000;
		this.lastcalculation = null;
		this.numExposes = 0;
		this.numExposesSold = 0;
		generateToken();
	}

	public static String digestPassword(String p) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(p.getBytes(), 0, p.length());
			return new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getUserName() {
		return username;
	}

	public JSONObject toJSON() {
		JSONObject resp = new JSONObject();
		JSONObject o = new JSONObject();
		try {
			o.put("username", username);
			o.put("email", email);
			o.put("twitter", twitter);
			o.put("token", token);
			o.put("info", getInfo(false, true, true, true));
			resp.put("org.immopoly.common.User", o);
		} catch (JSONException e) {
			LOG.log(Level.SEVERE, " User.toJSON ", e);
			e.printStackTrace();
		}
		return resp;
	}

	public JSONObject toPublicJSON(boolean whistory, boolean wbadges, boolean wactions) {
		JSONObject resp = new JSONObject();
		JSONObject o = new JSONObject();
		try {
			o.put("username", username);
			o.put("twitter", twitter);
			o.put("info", getInfo(true, whistory, wbadges, wactions));
			resp.put("org.immopoly.common.User", o);
		} catch (JSONException e) {
			LOG.log(Level.SEVERE, " User.toJSON ", e);
			e.printStackTrace();
		}
		return resp;
	}

	private JSONObject getInfo(boolean pub, boolean whistory, boolean wbadges, boolean wactions) throws JSONException {
		JSONObject info = new JSONObject();
		info.put(KEY_BALANCE, balance);
		if (null != numExposes)
			info.put(KEY_NUM_EXPOSES, numExposes);
		else
			info.put(KEY_NUM_EXPOSES, -1);

		info.put(KEY_MAX_EXPOSES, 50);

		if (null != lastRent)
			info.put(KEY_LAST_RENT, lastRent);
		else
			info.put(KEY_LAST_RENT, 0);
		if (null != lastProvision)
			info.put(KEY_LAST_PROVISION, lastProvision);
		else
			info.put(KEY_LAST_PROVISION, 0);

		if (null != balanceMonth)
			info.put(KEY_MONTH_BALANCE, balanceMonth);
		else
			info.put(KEY_MONTH_BALANCE, 0);

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			// last 20 history
			if (whistory) {
				JSONArray historyList = new JSONArray();
				List<History> history = DBManager.getHistory(pm, id, 0, 20);
				for (History h : history) {
					historyList.put(h.toJSON());
				}
				info.put(KEY_HISTORY_LIST, historyList);
			}

			// last 20 badges
			if (wbadges) {
				JSONArray badgeList = new JSONArray();
				List<Badge> badges = DBManager.getBadges(pm, id, null, 0, 20);
				for (Badge b : badges) {
					badgeList.put(b.toJSON());
				}
				info.put(KEY_BADGES_LIST, badgeList);
			}
			if (pub)
				return info;

			if (wactions) {
				JSONArray actionItemList = new JSONArray();
				List<ActionItem> actions = DBManager.getActionItems(pm, id, null);
				for (ActionItem a : actions) {
					if (a.getAmount() > 0)
						actionItemList.put(a.toJSON());
				}
				info.put(KEY_ACTIONITEM_LIST, actionItemList);
			}

			// expose
			JSONObject resultlist = new JSONObject();
			JSONArray resultlistEntries = new JSONArray();
			JSONArray resultlistEntry = new JSONArray();

			// alle exposes des users holen
			List<Expose> exposes = DBManager.getExposes(pm, id, null, null);
			Collections.sort(exposes);
			for (Expose expose : exposes) {
				resultlistEntry.put(expose.toJSON());
			}
			resultlistEntries.put(resultlistEntry);
			resultlist.put("resultlistEntries", resultlistEntries);
			info.put("resultlist.resultlist", resultlist);

		} catch (Exception e) {
			LOG.log(Level.SEVERE, " getInfo ", e);
			e.printStackTrace();
		} finally {
			pm.close();
		}
		return info;
	}

	public void generateToken() {
		this.token = digestPassword(Long.toString(System.currentTimeMillis()) + password + username);
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

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}

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
	public org.immopoly.common.History instantiateHistory(JSONObject o) {
		return null;
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

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getTwitter() {
		return twitter;
	}

	public void setPassword(String password2) {
		this.password = digestPassword(password2);
	}

	public void setC2DMRegistrationId(String c2dmRegistrationId) {
		this.c2dmRegistrationId = c2dmRegistrationId;
	}

	public String getC2dmRegistrationId() {
		return c2dmRegistrationId;
	}

	public Integer getNumExposes() {
		if (null == numExposes)
			numExposes = 0;
		return numExposes;
	}

	public void setNumExposes(Integer numExposes) {
		this.numExposes = numExposes;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void addExpose(int i) {
		if (null == this.numExposes)
			this.numExposes = 0;
		this.numExposes += i;
	}

	@Override
	public org.immopoly.common.Badge instantiateBadge(JSONObject jsonObject) {
		return null;
	}

	@Override
	public void setBadges(List<org.immopoly.common.Badge> badges) {
	}

	@Override
	public void setMaxExposes(int maxExposes) {
	}

	@Override
	public void setNumExposes(int numExposes) {
		this.numExposes = numExposes;
	}

	public double getBalanceMonth() {
		if (null == balanceMonth)
			return 0;
		return balanceMonth;
	}

	public void setBalanceMonth(double balanceMonth) {
		this.balanceMonth = balanceMonth;
	}

	public Integer getNumExposesSold() {
		if (null == numExposesSold)
			return 0;
		return numExposesSold;
	}

	public void setNumExposesSold(Integer numExposesSold) {
		this.numExposesSold = numExposesSold;
	}

	public boolean hasBadge(PersistenceManager pm, int type) {
		List<Badge> badges = DBManager.getBadges(pm, id, type, 0, 1);
		return badges != null && badges.size() > 0;
	}

	public boolean hasActionItem(PersistenceManager pm, int type) {
		List<ActionItem> actionItems = DBManager.getActionItems(pm, this.getId(), type);
		return actionItems != null && actionItems.size() > 0;
	}

	public boolean giveBadge(PersistenceManager pm, int type, String msg) {
		// check if Badge already given
		if (hasBadge(pm, type)) {
			LOG.info("Badge already given type : " + msg);
			return false;
		}
		Badge b = new Badge(type, id, System.currentTimeMillis(), msg, Badge.IMAGE.get(type), 0.0, null);
		pm.makePersistent(b);
		try {
			if (null != getC2dmRegistrationId() && getC2dmRegistrationId().length() > 0) {
				ImmopolyC2DMMessaging c2dm = new ImmopolyC2DMMessaging();
				Map<String, String[]> params = new HashMap<String, String[]>();
				// type message title
				params.put("data.type", new String[] { "1" });
				params.put("data.message", new String[] { b.getText() });
				params.put("data.title", new String[] { "Immopoly" });
				c2dm.sendNoRetry(getC2dmRegistrationId(), "mycollapse", params, true);
				LOG.info("Send c2dm message to" + getUserName() + " " + b.getText());
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Send c2dm message to" + getUserName() + " FAILED ", e);
		}
		return true;
	}

	public Boolean getReleaseBadge() {
		if(null==releaseBadge)
			return false;
		return releaseBadge;
	}

	public void setReleaseBadge(Boolean releaseBadge) {
		this.releaseBadge = releaseBadge;
	}

	@Override
	public org.immopoly.common.ActionItem instantiateActionItem(JSONObject jsonObject) {
		return null;
	}

	@Override
	public void setActionItems(List<org.immopoly.common.ActionItem> actionItems) {
	}
}
