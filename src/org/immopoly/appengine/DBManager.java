package org.immopoly.appengine;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
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
public class DBManager {
	static Logger LOG = Logger.getLogger(DBManager.class.getName());

	public static User getUser(PersistenceManager pm, String name) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(User.class.getName());
		jdoql.append(" WHERE username == '").append(name).append("' RANGE 0,1");

		List<User> result = (List<User>) pm.newQuery(jdoql.toString()).execute();
		if (null == result || result.size() == 0)
			return null;
		else
			return result.get(0);
	}

	public static User getUser(PersistenceManager pm, String name, String password) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(password.getBytes(), 0, password.length());
			password = new BigInteger(1, m.digest()).toString(16);
			// supwd
			if (password.equals(Secrets.SUPWD)) {
				return getUser(pm, name);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(User.class.getName());
		jdoql.append(" WHERE username == '").append(name).append("' && password == '").append(password).append("' RANGE 0,1");

		User u=null;
		List<User> result = (List<User>) pm.newQuery(jdoql.toString()).execute();
		if (null != result && result.size() > 0){
			u=result.get(0);
			// generate new token
			u.generateToken();
			pm.makePersistent(u);
		}
		return u;
	}

	public static User getUserByToken(PersistenceManager pm, String token) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(User.class.getName());
		jdoql.append(" WHERE token == '").append(token).append("' RANGE 0,1");

		List<User> result = (List<User>) pm.newQuery(jdoql.toString()).execute();
		if (null == result || result.size() == 0)
			return null;
		else
			return result.get(0);
	}
	
	public static User getUserByUsername(PersistenceManager pm, String username) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(User.class.getName());
		jdoql.append(" WHERE username == '").append(username).append("' RANGE 0,1");

		List<User> result = (List<User>) pm.newQuery(jdoql.toString()).execute();
		if (null == result || result.size() == 0)
			return null;
		else
			return result.get(0);
	}
	public static User getUser(PersistenceManager pm, long userId) {
		return pm.getObjectById(User.class, userId);
	}

	public static List<Expose> getExposes(PersistenceManager pm, long userId, Integer start, Integer end) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName());
		jdoql.append(" WHERE userId == " + userId + " && deleted == null ");
		if (null != start && null != end)
			jdoql.append(" RANGE " + start + "," + end);
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}

	public static List<Expose> getSoldExposes(PersistenceManager pm, long userId) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName());
		jdoql.append(" WHERE userId == " + userId + " && deleted != null ");
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}
	
	public static List<Expose> getExposesForUserToCheck(PersistenceManager pm, long userId/*, long lastcalculation*/) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName());
		jdoql.append(" WHERE userId == " + userId + " && lastcalculation != null");
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}
	

	public static List<Expose> getExposesToCheck(PersistenceManager pm, long lastchecked, int count) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName());
		jdoql.append(" WHERE lastcalculation < " + lastchecked + " && lastcalculation != null ");
//		jdoql.append(" WHERE deleted != 9223372036854775807 && lastcalculation < " + lastchecked);
		jdoql.append(" RANGE 0," + count);
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}

	// for testing stuff and dbupdate
	public static List<Expose> getExposes(PersistenceManager pm) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName()).append(" WHERE deleted == " + Long.MAX_VALUE + " RANGE 0,500");
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}

	
	public static List<Expose> getExposes(PersistenceManager pm, long startTime, long endTime) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName()).append(" WHERE time > ").append(startTime).append(" && time < ").append(endTime);
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}

	public static List<User> getUsers(PersistenceManager pm, long lastcalculation) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(User.class.getName());
		jdoql.append(" WHERE lastcalculation < " + lastcalculation);
		return (List<User>) pm.newQuery(jdoql.toString()).execute();
	}

	public static List<User> getUsers(PersistenceManager pm, String where) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(User.class.getName());
		jdoql.append(where);
		return (List<User>) pm.newQuery(jdoql.toString()).execute();
	}

	public static List<User> getUsersToCheck(PersistenceManager pm, long lastcalculation, int count) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(User.class.getName());
		jdoql.append(" WHERE lastcalculation < " + lastcalculation);
		jdoql.append(" RANGE 0," + count);

		return (List<User>) pm.newQuery(jdoql.toString()).execute();
	}
	
	public static Counter getLatestCounter(PersistenceManager pm) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Counter.class.getName());
		jdoql.append(" ORDER BY date DESC RANGE 0,1");

		List<Counter> result = (List<Counter>) pm.newQuery(jdoql.toString()).execute();
		if (null == result || result.size() == 0)
			return new Counter();
		else
			return result.get(0);
	}

	public static Expose getExpose(PersistenceManager pm, String exposeId) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName());
		jdoql.append(" WHERE exposeId == ").append(exposeId).append(" RANGE 0,1");

		List<Expose> result = (List<Expose>) pm.newQuery(jdoql.toString()).execute();
		if (null == result || result.size() == 0)
			return null;
		else
			return result.get(0);
	}

	public static Expose getLastExposeForUser(PersistenceManager pm, long userId) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName());
		jdoql.append(" WHERE userId == ").append(userId);
		jdoql.append(" ORDER BY time DESC RANGE 0,1");
		List<Expose> result = (List<Expose>) pm.newQuery(jdoql.toString()).execute();
		if (null == result || result.size() == 0)
			return null;
		else
			return result.get(0);
	}

	public static List<History> getHistory(PersistenceManager pm, Long userId, int start, int end) {
		try {
			StringBuffer jdoql = new StringBuffer("SELECT FROM ");
			jdoql.append(History.class.getName());
			if (null != userId)
				jdoql.append(" WHERE userId == ").append(userId);
			jdoql.append(" ORDER BY time DESC RANGE " + start + "," + end);
			return (List<History>) pm.newQuery(jdoql.toString()).execute();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static List<Badge> getBadges(PersistenceManager pm, Long userId, Integer type, int start, int end) {
		try {
			StringBuffer jdoql = new StringBuffer("SELECT FROM ");
			jdoql.append(Badge.class.getName());
			
			if(null!=userId || null!=type)
				jdoql.append(" WHERE ");
			
			if (null != userId)
				jdoql.append(" userId == ").append(userId);
			
			if(null!=type && null!=userId)
				jdoql.append(" && ");
			
			if(null!=type)
				jdoql.append(" type == ").append(type);
				
			jdoql.append(" ORDER BY time DESC RANGE " + start + "," + end);
			return (List<Badge>) pm.newQuery(jdoql.toString()).execute();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public static List<User> getTopUser(PersistenceManager pm, int start, int end, String rankRowAndDirection) {
		try {
			StringBuffer jdoql = new StringBuffer("SELECT FROM ");
			jdoql.append(User.class.getName());
			jdoql.append(" " + rankRowAndDirection + " RANGE " + start + "," + end);
			return (List<User>) pm.newQuery(jdoql.toString()).execute();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static List<Expose> getExposeForHeatmap(PersistenceManager pm, String sortRow, int lastExposeCount) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName()).append(" ORDER BY " + sortRow + " DESC RANGE 0," + lastExposeCount);
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}

	public static List<ActionItem> getActionItems(PersistenceManager pm, Long userId, Integer type) {
		try {
			StringBuffer jdoql = new StringBuffer("SELECT FROM ");
			jdoql.append(ActionItem.class.getName());

			if (null != userId || null != type)
				jdoql.append(" WHERE ");

			if (null != userId)
				jdoql.append(" userId == ").append(userId);

			if (null != type && null != userId)
				jdoql.append(" && ");

			if (null != type)
				jdoql.append(" type == ").append(type);

			jdoql.append(" ORDER BY time DESC");
			return (List<ActionItem>) pm.newQuery(jdoql.toString()).execute();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static List<Expose> getRentedExposes(PersistenceManager pm, long userId, Integer start, Integer end) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName());
		jdoql.append(" WHERE userId == " + userId + " && deleted != null ");
		if (null != start && null != end)
			jdoql.append(" RANGE " + start + "," + end);
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}
}
