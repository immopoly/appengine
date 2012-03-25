package org.immopoly.appengine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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
public class ActionItem extends org.immopoly.common.ActionItem implements JSONable, Serializable, Comparable<ActionItem> {

	private static final long serialVersionUID = 423432423444L;

	public static Map<Integer, String> IMAGE;
	static {
		IMAGE = new HashMap<Integer, String>();
		IMAGE.put(TYPE_ACTION_FREEEXPOSES, "http://immopoly.org/img/actions/freeexposes.png");
	}
	static Logger LOG = Logger.getLogger(ActionItem.class.getName());

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private long userId;

	@Persistent
	private long time;

	@Persistent
	private int type;

	@Persistent
	private int amount;

	@Persistent
	private String text;

	@Persistent
	private String url;
	
	public ActionItem(long userId, long time, int type, int amount, String text, String url) {
		super();
		this.userId = userId;
		this.time = time;
		this.type = type;
		this.amount = amount;
		this.text = text;
		this.url = url;
	}

	public JSONObject toJSON() {
		JSONObject resp = new JSONObject();
		JSONObject o = new JSONObject();
		try {
			o.put("userId", userId);
			o.put("time", time);
			o.put("type", type);
			o.put("amount", amount);
			o.put("text", text);
			o.put("url", url);

			resp.put(getJSONObjectKey(), o);
		} catch (JSONException e) {
			LOG.log(Level.SEVERE, "toJson failed", e);
		}
		return resp;
	}

	@Override
	public int compareTo(ActionItem o) {
		return (int) ((o.time / 1000) - (time / 1000));
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
