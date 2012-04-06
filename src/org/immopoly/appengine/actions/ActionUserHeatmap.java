package org.immopoly.appengine.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.Expose;
import org.immopoly.appengine.PMF;
import org.immopoly.appengine.User;
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
public class ActionUserHeatmap extends AbstractAction {

	// public static String HEATMAP_TYPE = "type";
	// public static String HEATMAP_TYPE_SOLD = "sold";
	// public static String HEATMAP_TYPE_TAKEOVER = "takeover";

	class Cluster {
		private List<Expose> exposes;
		double latSum = 0;
		double lonSum = 0;

		public Cluster(Expose expose) {
			super();
			exposes = new ArrayList<Expose>();
			add(expose);
		}

		public void add(Expose expose) {
			exposes.add(expose);
			latSum += expose.getLatitude();
			lonSum += expose.getLongitude();
		}

		public List<Expose> getExpose() {
			return exposes;
		}

		public double distance(Expose expose) {
			return expose.calcDistance(getLatitude(), getLongitude());
		}

		public double getLatitude() {
			return latSum / (exposes.size()*1.00);
		}

		public double getLongitude() {
			return lonSum / (exposes.size()*1.00);
		}
	}

	public ActionUserHeatmap(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/heatmap";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp)
			throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String username = req.getParameter(USERNAME);

			if (null == username || username.length() == 0)
				throw new ImmopolyException(
						ImmopolyException.MISSING_PARAMETER_USERNAME,
						"missing username");

			User user = DBManager.getUserByUsername(pm, username);
			if (null == user)
				throw new ImmopolyException(
						ImmopolyException.USERNAME_NOT_FOUND,
						"user by name not found " + username);

			List<Expose> exposes = DBManager.getRentedExposes(pm, user.getId(), null,
					null);
			LOG.info("found num exposes "+exposes.size());
			// clustering
			List<Cluster> cluster = getClusteredLocations(exposes,1000);
			// { max: 46, data: [{lat: 33.5363, lng:-117.044, count: 1},...] }
			JSONObject response = new JSONObject();
			JSONArray data = new JSONArray();
			int max=1;
			for (Cluster c : cluster) {
				JSONObject e = new JSONObject();
				e.put("lat", c.getLatitude());
				e.put("lng", c.getLongitude());
				e.put("count", c.exposes.size());
				if(c.exposes.size()>max)
					max=c.exposes.size();
				data.put(e);
			}
			response.put("max", max);
			response.put("data", data);
			resp.getOutputStream().write(response.toString().getBytes("UTF-8"));

		} catch (Exception e) {
			throw new ImmopolyException(
					ImmopolyException.EXPOSE_HEATMAP_FAILED,
					"could not heatmap " + e.getMessage(), e);
		} finally {
			pm.close();
		}
	}

	public List<Cluster> getClusteredLocations(List<Expose> exposes,
			double thresholdRadius) {
		List<Cluster> result = new ArrayList<Cluster>();

		if (null == exposes || exposes.size() == 0)
			return result;

		for (Expose expose : exposes) {
			// über alle cluster schleifen, das näheste finden
			Cluster minCluster = null;
			double minDistance = Double.MAX_VALUE;
			for (Cluster cluster : result) {
				double distance = cluster.distance(expose);
				if (distance < minDistance) {
					minCluster = cluster;
					minDistance = distance;
				}
			}
			// wenn Cluster noch nicht da erstellen mit Location
			if (null == minCluster) {
				LOG.info("new cluster ");
				result.add(new Cluster(expose));
				continue;
			}
			LOG.info("minDistance = "+minDistance);
			// wenn abstand zum Cluster < thresholdRadius
			if (minDistance < thresholdRadius) {
				LOG.info("added to existing cluster");
				minCluster.add(expose);
			} else // wenn abstand zum Cluster > thresholdRadius
			{
				LOG.info("to distant new cluster ");
				result.add(new Cluster(expose));
			}
		}
		return result;
	}
}
