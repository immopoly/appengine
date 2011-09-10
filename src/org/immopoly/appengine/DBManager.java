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
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(User.class.getName());
		jdoql.append(" WHERE username == '").append(name).append("' && password == '").append(password).append("' RANGE 0,1");

		List<User> result = (List<User>) pm.newQuery(jdoql.toString()).execute();
		if (null == result || result.size() == 0)
			return null;
		else
			return result.get(0);
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

	public static User getUser(PersistenceManager pm, long userId) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(User.class.getName());
		jdoql.append(" WHERE id == ").append(userId).append(" RANGE 0,1");

		List<User> result = (List<User>) pm.newQuery(jdoql.toString()).execute();
		if (null == result || result.size() == 0)
			return null;
		else
			return result.get(0);
	}

	public static List<Expose> getExposes(PersistenceManager pm, long userId) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName());
		jdoql.append(" WHERE userId == " + userId + "");
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}

	public static List<Expose> getLastExposes(PersistenceManager pm, long userId, long minTime) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName());
		jdoql.append(" WHERE userId == " + userId + " && time > "+minTime);
		LOG.info("before getLastExposes");
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}
	
	public static List<Expose> getExposes(PersistenceManager pm) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(Expose.class.getName());
		return (List<Expose>) pm.newQuery(jdoql.toString()).execute();
	}

	public static List<User> getUsers(PersistenceManager pm, long lastcalculation) {
		StringBuffer jdoql = new StringBuffer("SELECT FROM ");
		jdoql.append(User.class.getName());
		jdoql.append(" WHERE lastcalculation < " + lastcalculation);
		return (List<User>) pm.newQuery(jdoql.toString()).execute();
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

	public static List<History> getHistory(PersistenceManager pm, long userId, int i) {
		try {
			StringBuffer jdoql = new StringBuffer("SELECT FROM ");
			jdoql.append(History.class.getName());
			jdoql.append(" WHERE userId == " + userId + " ORDER BY time DESC RANGE 0,"+i);
			return (List<History>) pm.newQuery(jdoql.toString()).execute();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static List<History> getAllHistory(PersistenceManager pm, int count) {
		try {
			StringBuffer jdoql = new StringBuffer("SELECT FROM ");
			jdoql.append(History.class.getName());
			jdoql.append(" ORDER BY time DESC RANGE 0," + count);
			return (List<History>) pm.newQuery(jdoql.toString()).execute();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static List<User> getTopUser(PersistenceManager pm, int i) {
		try {
			StringBuffer jdoql = new StringBuffer("SELECT FROM ");
			jdoql.append(User.class.getName());
			jdoql.append(" ORDER by balance DESC RANGE 0," + i);

			return (List<User>) pm.newQuery(jdoql.toString()).execute();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
