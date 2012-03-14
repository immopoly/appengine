package org.immopoly.appengine;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.Transient;

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
public class Expose implements JSONable, Serializable, Comparable<Expose> {

	//TODO schtief fuck we need make use of a Key and exposeId
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private long exposeId;

	@Persistent
	private long userId;

	@Persistent
	private String name;

	@Persistent
	private double latitude = 0.0;

	@Persistent
	private double longitude = 0.0;

	@Persistent
	private double rent = 0.0;

	@Persistent
	private Long time;

	@Persistent
	private Long deleted = null;

	@Persistent
	private Integer numberOfRooms=null;
	
	@Persistent
	private Integer livingSpace=null;

	@Persistent
	private String titlePicture=null;

	@Persistent
	private Long lastcalculation = null;
	
	@Transient
	private Boolean courtage=false;
	
	public boolean isCourtage() {
		return null!=courtage && courtage;
	}

	public Long getDeleted() {
		return deleted;
	}

	public void setDeleted(Long deleted) {
		this.deleted = deleted;
	}

	@Persistent
	private int overtakestries = 0;

	// public Expose(long exposeId, String name, double latitude, double
	// longitude, long userId, double rent) {
	// this.exposeId = exposeId;
	// this.name = name;
	// this.latitude = latitude;
	// this.longitude = longitude;
	// this.userId = userId;
	// this.rent = rent;
	// }

	public int getOvertakestries() {
		return overtakestries;
	}

	public void addOvertake() {
		this.overtakestries++;
	}

	public Expose(long userId, JSONObject obj) throws JSONException {
		this.userId = userId;
		this.time = System.currentTimeMillis();
		this.lastcalculation = this.time;
		fromJSON(obj);
	}

//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}

	public JSONObject toJSON() throws JSONException {
		JSONObject resp = new JSONObject();
		JSONObject o = new JSONObject();

		JSONObject wgs84Coordinate = new JSONObject();
		wgs84Coordinate.put("longitude", longitude);
		wgs84Coordinate.put("latitude", latitude);

		JSONObject address = new JSONObject();
		address.put("wgs84Coordinate", wgs84Coordinate);

		JSONObject realEstate = new JSONObject();
		realEstate.put("address", address);
		realEstate.put("title", name);
		realEstate.put("@id", exposeId);
		realEstate.put("baseRent", rent);
		realEstate.put("overtakeTries", overtakestries);
		realEstate.put("overtakeDate",time);
		
		if(null!=numberOfRooms)
			realEstate.put("numberOfRooms",numberOfRooms);
		if(null!=livingSpace)
			realEstate.put("livingSpace",livingSpace);
		if(null!=titlePicture){
			// "urls": [ { "url": {

			JSONObject t = new JSONObject();
			t.put("@href", titlePicture);

			JSONObject url = new JSONObject();
			url.put("url", t);

			JSONArray urls = new JSONArray();
			urls.put(url);

			JSONObject jtitlePicture = new JSONObject();
			jtitlePicture.put("urls", urls);

			realEstate.put("titlePicture", jtitlePicture);
		}
		
		o.put("realEstate", realEstate);
		resp.put("expose.expose", o);

		return resp;
	}

	@Override
	public void fromJSON(JSONObject o) throws JSONException {
		// OWN OBJECT
		JSONObject expose = o.getJSONObject("expose.expose");
		JSONObject objRealEstate = expose.getJSONObject("realEstate");
		name = objRealEstate.optString("title");
		exposeId = objRealEstate.getLong("@id");
		// description = objRealEstate.optString("descriptionNote");
		// locationNote = objRealEstate.optString("locationNote");
		// OWN OBJECT

		/**
		 * Address Fields
		 */
		JSONObject objAddress = objRealEstate.getJSONObject("address");
		// street = objAddress.optString("street");
		// houseNumber = objAddress.optInt("houseNumber", 0);
		// postcode = objAddress.optString("postcode");
		// city = objAddress.optString("city");
		// quarter = objAddress.optString("quater");
		JSONObject coordinate = objAddress.getJSONObject("wgs84Coordinate");
		latitude = coordinate.optDouble("latitude");
		longitude = coordinate.optDouble("longitude");
		// if (objRealEstate.has("titlePicture")) {
		// JSONObject objPicture =
		// objRealEstate.getJSONObject("titlePicture");
		// titlePictureTitle = objPicture.optString("title");
		// titlePictureDescriptionNote = objPicture
		// .optString("descriptionNote");
		// titlePictureFurnitureNote =
		// objPicture.optString("furnishingNote");
		// titlePictureLocationNote = objPicture.optString("locationNote");
		// titlePictureSmall = objPicture.optString("@xlink.href");
		// if (objPicture.has("urls")
		// && objPicture.getJSONArray("urls").length() > 0) {
		// //JSONArray urls = objPicture.getJSONArray("urls").getJSONObject(
		// //0).getJSONArray("url");
		// JSONObject urls = objPicture.getJSONArray("urls").getJSONObject(
		// 0).getJSONObject("url");
		// titlePictureSmall = urls.optString("@href");
		// /*
		// if (urls.length() > 0) {
		// titlePictureSmall = urls.getJSONObject(0).getString("href");
		// } else if (urls.length() > 1) {
		// titlePictureMedium = urls.getJSONObject(1).getString("href");
		// } else if (urls.length() > 2) {
		// titlePictureLarge = urls.getJSONObject(2).getString("href");
		// }
		// */
		// }
		// }
		if (objRealEstate.has("price")) {
			// marketingType =
			// objRealEstate.getJSONObject("price").optString("marketingType");
			// priceValue =
			// objRealEstate.getJSONObject("price").optString("value");
			// priveIntervaleType =
			// objRealEstate.getJSONObject("price").optString("priceIntervalType");
			// currency =
			// objRealEstate.getJSONObject("price").optString("currency");
		}
		if (objRealEstate.has("baseRent"))
			rent = objRealEstate.getDouble("baseRent");

		if (objRealEstate.has("courtage"))
		{
			String hasCourtage = objRealEstate.getJSONObject("courtage").optString("hasCourtage");
			if(null!=hasCourtage && hasCourtage.equalsIgnoreCase("YES"))
				courtage=true;
		}
		
		if (objRealEstate.has("numberOfRooms"))
		{
			numberOfRooms=objRealEstate.getInt("numberOfRooms");
		}

		if (objRealEstate.has("livingSpace"))
		{
			livingSpace=objRealEstate.getInt("livingSpace");
		}else if (objRealEstate.has("usableFloorSpace"))
		{
			livingSpace=objRealEstate.getInt("usableFloorSpace");
		}
			
		if (objRealEstate.has("titlePicture") && objRealEstate.getJSONObject("titlePicture").has("urls"))
		{
			JSONArray url	=	 objRealEstate.getJSONObject("titlePicture").getJSONArray("urls").getJSONObject(0).getJSONArray("url");
			titlePicture=url.getJSONObject(0).optString("@href");
		}
		
	}

	public long getUserId() {
		return userId;
	}

	public long getExposeId() {
		return exposeId;
	}

	public String getName() {
		return name;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getRent() {
		return rent;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Integer getNumberOfRooms() {
		return numberOfRooms;
	}


	public String getTitlePicture() {
		return titlePicture;
	}

	public void setTitlePicture(String titlePicture) {
		this.titlePicture = titlePicture;
	}

	@Override
	public int compareTo(Expose o) {
		if (o == null)
			return Integer.MIN_VALUE;
		if (o.time == null)
			return Integer.MIN_VALUE;
		if (time == null)
			return Integer.MAX_VALUE;
		return (int) ((o.time / 1000) - (time / 1000));
	}

	public void setLastcalculation(Long timestamp) {
		this.lastcalculation = timestamp;
	}

	public Long getLastcalculation() {
		return lastcalculation;
	}

}
