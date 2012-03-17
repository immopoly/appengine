package org.immopoly.appengine;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.Transient;

import org.immopoly.common.JSONable;
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
public class History extends org.immopoly.common.History implements JSONable, Serializable {

	private static final long serialVersionUID = 1L;
	// format fuer Waehrung in der Historie
	public static DecimalFormat MONEYFORMAT = new DecimalFormat("0.00 Eur");
	// public static DecimalFormat MONEYFORMAT_2 = new DecimalFormat("0.00");

	static Logger LOG = Logger.getLogger(History.class.getName());
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private long userId;

	@Persistent
	private long time;

	@Persistent
	private String text;

	@Persistent
	private int type;

	@Persistent
	private Double amount;

	@Persistent
	private Long exposeId = null;

	@Transient
	private String username = null;

	@Persistent
	private String otherUsername = null;

	public History(int type, long userId, long time, String text, Double amount, Long exposeId, String username, String otherUsername) {
		this.userId = userId;
		this.time = time;
		this.text = text;
		this.type = type;
		this.exposeId = exposeId;
		this.amount = amount;
		this.username = username;
		this.otherUsername=otherUsername;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	// public void loadUsername(PersistenceManager pm){
	// User u = DBManager.getUser(pm, userId);
	// if(u!=null)
	// this.username=u.getUserName();
	// }

	public JSONObject toJSON() {
		JSONObject resp = new JSONObject();
		JSONObject o = new JSONObject();
		try {
			o.put("time", time);
			o.put("text", text);
			o.put("type", type);

			if (null != amount)
				o.put("amount", amount);
			if (null != exposeId)
				o.put("exposeId", exposeId);
			if (username != null)
				o.put("username", username);
			if (otherUsername != null)
				o.put("otherUsername", otherUsername);
			
			resp.put("org.immopoly.common.History", o);
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
}
