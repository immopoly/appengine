package org.immopoly.appengine.actions;

import java.util.Map;
import java.util.Properties;

import javax.jdo.PersistenceManager;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.PMF;
import org.immopoly.appengine.User;
import org.immopoly.common.ImmopolyException;

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
public class ActionUserPasswordMail extends AbstractAction {

	public ActionUserPasswordMail(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/sendpasswordmail";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String username = req.getParameter(USERNAME);
			String email = req.getParameter(EMAIL);

			if (null == username || username.length() == 0)
				throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_USERNAME,"missing username");
			if (null == email || email.length() == 0)
				throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_EMAIL,"missing email");

			User user = DBManager.getUserByUsername(pm, username);
			if (null == user) {
				throw new ImmopolyException(ImmopolyException.USERNAME_NOT_FOUND,"User not found " + username);
			}
			if (user.getEmail() == null || user.getEmail().length() == 0) {
				throw new ImmopolyException(ImmopolyException.USER_SEND_PASSWORDMAIL_NOEMAIL, "Email for username " + username
						+ " is not set!");
			}

			if (!email.equals(user.getEmail())) {
				throw new ImmopolyException(ImmopolyException.USER_SEND_PASSWORDMAIL_EMAIL_NOMATCH, "Email for username " + username
						+ " does not match!");
			}
			// send email
			sendMail(user);
			LOG.info("Send Token per mail " + user.getUserName());

			resp.getOutputStream().write("{\"OK\":\"OK\"}".getBytes("UTF-8"));
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException(ImmopolyException.USER_SEND_PASSWORDMAIL_FAILED, "could not send passwordmail"+e.getMessage(), e);
		} finally {
			pm.close();
		}
	}

	private void sendMail(User user) throws Exception {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		String msgBody = "Klicke auf den Link um dein Passwort zu ändern http://immopoly.appspot.com/resetpasswd.html?token="
				+ user.getToken() + " \n your Immopoly Team";

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("immopolyteam@googlemail.com",
					"Immopoly Team"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user
					.getEmail(), user.getUserName()));
		msg.setSubject("Immopoly Password setzen");
			msg.setText(msgBody);
			Transport.send(msg);
	}
}
