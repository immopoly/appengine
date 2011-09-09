package org.immopoly.appengine;

import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class ActionUserInfo extends AbstractAction {

	public ActionUserInfo(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/info";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String token = req.getParameter(TOKEN);
			if (null == token || token.length() == 0)
				throw new ImmopolyException("missing token", 61);

			User user = DBManager.getUserByToken(pm, token);
			LOG.info("Info " + user.getUserName() + " " + user.toJSON().toString());
			if (null == user) {
				throw new ImmopolyException("token not found " + token, 62);
			} else {
				resp.getOutputStream().write(user.toJSON().toString().getBytes("UTF-8"));
			}
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException("could not login user", 101, e);
		} finally {
			pm.close();
		}
	}
}
