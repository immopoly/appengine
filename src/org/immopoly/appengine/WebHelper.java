package org.immopoly.appengine;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
public class WebHelper {
	static Logger LOG = Logger.getLogger(WebHelper.class.getName());

	public static JSONObject getHttpData(URL url) throws JSONException {
		HttpURLConnection request;
		try {
//			LOG.log(Level.FINE, "open Connection " + url.toString());
			request = (HttpURLConnection) url.openConnection();
//			LOG.log(Level.FINE, "sign Request ");
			OAuthData.consumer.sign(request);
//			LOG.log(Level.FINE, "connect");
			request.connect();
			// String message = request.getResponseMessage();
			// InputStream in = new
			// BufferedInputStream(request.getInputStream());
			// String s = readInputStream(in);
//			LOG.log(Level.FINE, "parse");
			JSONTokener tokener = new JSONTokener(request.getInputStream(), "UTF-8");// s);
			JSONObject obj = new JSONObject(tokener);
			return obj;
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "request '" + url + "' failed", e);
		}
		return null;
	}

	public static String readInputStream(InputStream in) throws IOException {
		StringBuffer stream = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			stream.append(new String(b, 0, n));
		}
		return stream.toString();
	}
}
