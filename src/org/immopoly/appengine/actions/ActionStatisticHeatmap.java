package org.immopoly.appengine.actions;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.Expose;
import org.immopoly.appengine.PMF;
import org.immopoly.common.ImmopolyException;
import org.json.JSONArray;
import org.json.JSONObject;

/*
 This is the server side Google App Engine component of Immopoly
 http://immopoly.appspot.com
 Copyright (C) 2012 Mister Schtief

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
public class ActionStatisticHeatmap extends AbstractAction {

	public static String HEATMAP_TYPE = "type";
	public static String HEATMAP_TYPE_SOLD = "sold";
	public static String HEATMAP_TYPE_TAKEOVER = "takeover";
	
	public ActionStatisticHeatmap(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "statistic/heatmap";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String sortRow;
			String sortType = req.getParameter(HEATMAP_TYPE);
			if (null != sortType && HEATMAP_TYPE_SOLD.equals(sortType))
			{
				sortRow = "deleted";
			}else{
				sortRow = "time";
			}
			
			List<Expose> exposes = DBManager.getExposeForHeatmap(pm, sortRow, 250);
			// { max: 46, data: [{lat: 33.5363, lng:-117.044, count: 1},...] }
			JSONObject response = new JSONObject();
			response.put("max", 1);

			JSONArray data = new JSONArray();
			for (Expose expose : exposes) {
				JSONObject e = new JSONObject();
				e.put("lat", expose.getLatitude());
				e.put("lng", expose.getLongitude());
				e.put("count", 1);
				data.put(e);
			}
			response.put("data", data);
			resp.getOutputStream().write(response.toString().getBytes("UTF-8"));

		} catch (Exception e) {
			throw new ImmopolyException(ImmopolyException.EXPOSE_HEATMAP_FAILED, "could not heatmap " + e.getMessage(), e);
		} finally {
			pm.close();
		}
	}
}
