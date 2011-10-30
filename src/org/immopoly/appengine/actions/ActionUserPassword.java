package org.immopoly.appengine.actions;

import java.util.Map;

import javax.jdo.PersistenceManager;
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
public class ActionUserPassword extends AbstractAction {

	public ActionUserPassword(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/password";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String username = req.getParameter(USERNAME);
			String token = req.getParameter(TOKEN);
			String password = req.getParameter(PASSWORD);
			String email = req.getParameter(EMAIL);

			if (null == username || username.length() == 0)
				throw new ImmopolyException("missing username", 43);
			if (null == password || password.length() == 0)
				throw new ImmopolyException("missing password", 44);
			if (null == token || token.length() == 0)
				throw new ImmopolyException("missing token", 61);

			User user = DBManager.getUserByToken(pm, token);
			if (null == user) {
				throw new ImmopolyException("token not found " + token, 62);
			}
			if (!username.equals(user.getUserName())) {
				throw new ImmopolyException("username does not match token "
						+ token, 52);
			}
			LOG.info("Change Password " + user.getUserName() + " "
					+ user.toJSON().toString());
			user.setPassword(password);
			if (null != email && email.length() > 0)
				user.setEmail(email);
			pm.makePersistent(user);
			resp.getOutputStream().write(
					user.toJSON().toString().getBytes("UTF-8"));
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException("could not change password", 108, e);
		} finally {
			pm.close();
		}
	}
}
