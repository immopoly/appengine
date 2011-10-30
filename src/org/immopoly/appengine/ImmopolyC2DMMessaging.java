package org.immopoly.appengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

public class ImmopolyC2DMMessaging {
	static Logger LOG = Logger.getLogger(ImmopolyC2DMMessaging.class.getName());
	public static final String PARAM_REGISTRATION_ID = "registration_id";
	public static final String PARAM_DELAY_WHILE_IDLE = "delay_while_idle";
	public static final String PARAM_COLLAPSE_KEY = "collapse_key";
	public static final String DATAMESSAGING_SEND_ENDPOINT = "https://android.apis.google.com/c2dm/send";
	private static final String UTF8 = "UTF-8";
	private static final String UPDATE_CLIENT_AUTH = "Update-Client-Auth";

	public boolean sendNoRetry(String registrationId, String collapse,
			Map<String, String[]> params, boolean delayWhileIdle)
			throws IOException {

		// Send a sync message to this Android device.
		StringBuilder postDataBuilder = new StringBuilder();
		postDataBuilder.append(PARAM_REGISTRATION_ID).append("=").append(
				registrationId);

		if (delayWhileIdle) {
			postDataBuilder.append("&").append(PARAM_DELAY_WHILE_IDLE).append(
					"=1");
		}
		postDataBuilder.append("&").append(PARAM_COLLAPSE_KEY).append("=")
				.append(collapse);

		for (Object keyObj : params.keySet()) {
			String key = (String) keyObj;
			if (key.startsWith("data.")) {
				String[] values = params.get(key);
				postDataBuilder.append("&").append(key).append("=").append(
						URLEncoder.encode(values[0], UTF8));
			}
		}

		byte[] postData = postDataBuilder.toString().getBytes(UTF8);

		// Hit the dm URL.
		URL url = new URL(DATAMESSAGING_SEND_ENDPOINT);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded;charset=UTF-8");
		conn.setRequestProperty("Content-Length", Integer
				.toString(postData.length));
		String authToken = Secrets.AUTH_TOKEN;
		conn.setRequestProperty("Authorization", "GoogleLogin auth="
				+ authToken);

		OutputStream out = conn.getOutputStream();
		out.write(postData);
		out.close();

		int responseCode = conn.getResponseCode();

		if (responseCode == HttpServletResponse.SC_UNAUTHORIZED
				|| responseCode == HttpServletResponse.SC_FORBIDDEN) {
			// The token is too old - return false to retry later, will fetch
			// the token
			// from DB. This happens if the password is changed or token
			// expires. Either admin
			// is updating the token, or Update-Client-Auth was received by
			// another server,
			// and next retry will get the good one from database.
			LOG.warning("Unauthorized - need token");
			// serverConfig.invalidateCachedToken();
			return false;
		}

		// Check for updated token header
		String updatedAuthToken = conn.getHeaderField(UPDATE_CLIENT_AUTH);
		if (updatedAuthToken != null && !authToken.equals(updatedAuthToken)) {
			LOG.info("Got updated auth token from datamessaging servers: "
					+ updatedAuthToken);
			// serverConfig.updateToken(updatedAuthToken);
		}

		String responseLine = new BufferedReader(new InputStreamReader(conn
				.getInputStream())).readLine();

		// NOTE: You *MUST* use exponential backoff if you receive a 503
		// response code.
		// Since App Engine's task queue mechanism automatically does this for
		// tasks that
		// return non-success error codes, this is not explicitly implemented
		// here.
		// If we weren't using App Engine, we'd need to manually implement this.
		if (responseLine == null || responseLine.equals("")) {
			LOG.info("Got " + responseCode
					+ " response from Google AC2DM endpoint.");
			throw new IOException(
					"Got empty response from Google AC2DM endpoint.");
		}

		String[] responseParts = responseLine.split("=", 2);
		if (responseParts.length != 2) {
			LOG.warning("Invalid message from google: " + responseCode + " "
					+ responseLine);
			throw new IOException("Invalid response from Google "
					+ responseCode + " " + responseLine);
		}

		if (responseParts[0].equals("id")) {
			LOG.info("Successfully sent data message to device: "
					+ responseLine);
			return true;
		}

		if (responseParts[0].equals("Error")) {
			String err = responseParts[1];
			LOG
					.warning("Got error response from Google datamessaging endpoint: "
							+ err);
			// No retry.
			// TODO(costin): show a nicer error to the user.
			throw new IOException(err);
		} else {
			// 500 or unparseable response - server error, needs to retry
			LOG.warning("Invalid response from google " + responseLine + " "
					+ responseCode);
			return false;
		}
	}

}
