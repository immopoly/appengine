package org.immopoly.appengine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.Transient;

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
public class Badge extends org.immopoly.common.Badge implements JSONable, Serializable, Comparable<Badge> {

	private static final long serialVersionUID = 2L;
	public static final int ONE_OF_THE_FIRST = 23;
	public static final int EARLY_ADOPTER = 123;
	public static final int RENTED = 100000;
	public static final int OVERTAKETRIES = 300000;

	public static Map<Integer, String> IMAGE;
	static {
		IMAGE = new HashMap<Integer, String>();
		IMAGE.put(ONE_OF_THE_FIRST, "http://immopoly.org/img/badges/badge-oftf.png");
		IMAGE.put(EARLY_ADOPTER, "http://immopoly.org/img/badges/badge-ea.png");

		IMAGE.put(RENTED + 10, "http://immopoly.org/img/badges/10.png");
		IMAGE.put(RENTED + 30, "http://immopoly.org/img/badges/30.png");
		IMAGE.put(RENTED + 60, "http://immopoly.org/img/badges/60.png");
		IMAGE.put(RENTED + 80, "http://immopoly.org/img/badges/80.png");
		IMAGE.put(RENTED + 100, "http://immopoly.org/img/badges/100.png");

		IMAGE.put(OVERTAKETRIES + 3, "http://immopoly.org/img/badges/overtaketries3.png");
		IMAGE.put(OVERTAKETRIES + 5, "http://immopoly.org/img/badges/overtaketries5.png");
		IMAGE.put(OVERTAKETRIES + 10, "http://immopoly.org/img/badges/overtaketries10.png");
	}
	static Logger LOG = Logger.getLogger(Badge.class.getName());
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private long userId;

	@Persistent
	private long time;

	@Persistent
	private String text;

	@Persistent
	private int type;

	@Persistent
	private String url;

	@Persistent
	private Double amount;

	@Persistent
	private Long exposeId;

	@Transient
	private String username=null;
	
	public Badge(int type, long userId, long time, String text, String url, Double amount, Long exposeId) {
		super();
		this.userId = userId;
		this.time = time;
		this.text = text;
		this.url = url;
		this.type = type;
		this.exposeId = exposeId;
		this.amount = amount;
	}
	
	public void loadUsername(PersistenceManager pm){
		User u = DBManager.getUser(pm, userId);
		if(u!=null)
			this.username=u.getUserName();
	}

	public JSONObject toJSON() {
		JSONObject resp = new JSONObject();
		JSONObject o = new JSONObject();
		try {
			o.put("time", time);
			o.put("text", text);
			o.put("type", getType());
			o.put("url", url);

			if (username != null)
				o.put("username", username);
			if (null != amount)
				o.put("amount", amount);
			if (null != exposeId)
				o.put("exposeId", exposeId);

			resp.put("Badge", o);
		} catch (JSONException e) {
			LOG.log(Level.SEVERE, "toJson failed", e);
		}
		return resp;
	}

	public long getUserId() {
		return userId;
	}

	public long getTime() {
		return time;
	}

	public String getText() {
		return text;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public void setExposeId(long exposeId) {
		this.exposeId = exposeId;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int compareTo(Badge o) {
		return (int) ((o.time / 1000) - (time / 1000));
	}
}
