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
public class ActionUserC2DMRegister extends AbstractAction {

	public ActionUserC2DMRegister(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/C2DMregister";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String token = req.getParameter(TOKEN);
			String c2dmRegistrationId = req.getParameter(C2DMREGISTRATIONID);

			if (null == token || token.length() == 0)
				throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_TOKEN,"missing token");
			if (null == c2dmRegistrationId || c2dmRegistrationId.length() == 0)
				throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_C2DM_ID,"missing " + C2DMREGISTRATIONID);

			User user = DBManager.getUserByToken(pm, token);
			if (null == user) {
				throw new ImmopolyException(ImmopolyException.TOKEN_NOT_FOUND,"token not found " + token);
			}

			LOG.info("Register C2DMREGISTRANIONID for" + user.getUserName()
					+ " " + c2dmRegistrationId);
			user.setC2DMRegistrationId(c2dmRegistrationId);
			pm.makePersistent(user);
			resp.getOutputStream().write(
					user.toJSON().toString().getBytes("UTF-8"));
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException(ImmopolyException.C2DM_REGISTER_FAILED,"could not change password", e);
		} finally {
			pm.close();
		}
	}
}
