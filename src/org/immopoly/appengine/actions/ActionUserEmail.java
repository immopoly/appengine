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
public class ActionUserEmail extends AbstractAction {

	public ActionUserEmail(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "setemail";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String username = req.getParameter(USERNAME);
			// String password = req.getParameter(PASSWORD);
			String email = req.getParameter(EMAIL);

			if (null == username || username.length() == 0)
				throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_USERNAME, "missing username");
			// if (null == password || password.length() == 0)
			// throw new
			// ImmopolyException(ImmopolyException.MISSING_PARAMETER_PASSWORD,"missing password");
			// if (null == token || token.length() == 0)
			// throw new
			// ImmopolyException(ImmopolyException.MISSING_PARAMETER_TOKEN,"missing token");

			User user = DBManager.getUserByUsername(pm, username);
			if (null == user) {
				throw new ImmopolyException(ImmopolyException.USERNAME_NOT_FOUND, "user not found " + username);
			}
			// if (!username.equals(user.getUserName())) {
			// throw new
			// ImmopolyException(ImmopolyException.USER_DOES_NOT_MATCH_TOKEN,"username does not match token "
			// + token);
			// }
			LOG.info("Change Email " + user.getUserName());
			user.setEmail(email);
			pm.makePersistent(user);
			resp.getOutputStream().write("OK".getBytes("UTF-8"));
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException(ImmopolyException.USER_PASSWORD_CHANGE_FAILED,"could not change password", e);
		} finally {
			pm.close();
		}
	}
}
