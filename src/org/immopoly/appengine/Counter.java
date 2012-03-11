package org.immopoly.appengine;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.immopoly.common.JSONable;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.datastore.Key;
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
public class Counter implements JSONable, Serializable {

	private static final long serialVersionUID = 2455L;


	static Logger LOG = Logger.getLogger(Counter.class.getName());

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private long date = 0;

	@Persistent
	private int user = 0;

	@Persistent
	private int exposesRented = 0;

	@Persistent
	private int badgeOneOfTheFirst = 0;

	
	public Counter() {
		super();
	}
	
	public Counter(Counter counter) {
		super();
		this.date = System.currentTimeMillis();
		this.user = counter.user;
		this.exposesRented = counter.exposesRented;
		this.badgeOneOfTheFirst = counter.badgeOneOfTheFirst;
	}

	public JSONObject toJSON() {
		JSONObject resp = new JSONObject();
		JSONObject o = new JSONObject();
		try {
			o.put("date", date);
			o.put("user", user);
			o.put("exposesRented", exposesRented);
			o.put("badgeOneOfTheFirst", badgeOneOfTheFirst);
			resp.put("Counter", o);
		} catch (JSONException e) {
			LOG.log(Level.SEVERE, "toJson failed", e);
		}
		return resp;
	}


	@Override
	public void fromJSON(JSONObject o) throws JSONException {
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int getUser() {
		return user;
	}

	public void setUser(int user) {
		this.user = user;
	}

	public int getExposesRented() {
		return exposesRented;
	}

	public void setExposesRented(int exposesRented) {
		this.exposesRented = exposesRented;
	}

	public int getBadgeOneOfTheFirst() {
		return badgeOneOfTheFirst;
	}

	public void setBadgeOneOfTheFirst(int badgeOneOfTheFirst) {
		this.badgeOneOfTheFirst = badgeOneOfTheFirst;
	}

	public void addBadgeOneOfTheFirst(int i) {
		this.badgeOneOfTheFirst += i;
	}

	public void addUser(int i) {
		user += i;
	}

	public void addExposesRented(int i) {
		exposesRented += i;
	}

}
