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
	private Long exposeId;

	public History(int type, long userId, long time, String text, double amount, Long exposeId) {
		this.userId = userId;
		this.time = time;
		this.text = text;
		this.type = type;
		this.exposeId = exposeId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public JSONObject toJSON() {
		JSONObject resp = new JSONObject();
		JSONObject o = new JSONObject();
		try {
			o.put("time", time);
			o.put("userId", userId);
			o.put("text", text);
			o.put("type", getType());
			o.put("type2", getType2());
			if (null != amount)
				o.put("amount", amount);
			if (null != exposeId)
				o.put("exposeId", exposeId);
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
		if (type > 4)
			return type - 2;
		return type;
	}

	public void setType(int type) {
	}

	public int getType2() {
		return type;
	}

	public void setType2(int type2) {
		this.type = type2;
	}

	@Override
	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public void setExposeId(Long exposeId) {
		this.exposeId = exposeId;
	}
}
