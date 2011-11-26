package org.immopoly.appengine.actions;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.ImmopolyC2DMMessaging;
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
public class ActionC2DMSend extends AbstractAction {

	public ActionC2DMSend(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/C2DMsend";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String message = req.getParameter("message");
			String username = req.getParameter(USERNAME);

			User user = DBManager.getUserByUsername(pm, username);
			if (null == user) {
				throw new ImmopolyException(ImmopolyException.USERNAME_NOT_FOUND,"user not found " + username);
			}
			if (null == user.getC2dmRegistrationId()) {
				throw new ImmopolyException(ImmopolyException.C2DM_ID_NOT_FOUND,"no c2dm registration found for " + username);
			}
			ImmopolyC2DMMessaging c2dm = new ImmopolyC2DMMessaging();
			Map<String, String[]> params = new HashMap<String, String[]>();
			params.put("data.message", new String[] { message });
			c2dm.sendNoRetry(user.getC2dmRegistrationId(), "mycollapse",
					params, true);
			LOG.info("Send c2dm message to" + user.getUserName() + " "
					+ message);

			resp.getOutputStream().write("OK".getBytes());
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException(ImmopolyException.C2DM_FAILED,"could not send c2dm", e);
		} finally {
			pm.close();
		}
	}
}
