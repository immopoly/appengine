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
public class ActionPublicUserInfo extends AbstractAction {

	public ActionPublicUserInfo(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/profile";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			LOG.info("Profile "+req.getRequestURI());
			String username = req.getRequestURI().substring(14);
			//TODO schtief enhance framework for responsetyp enums
			//check for .json
			RESPONSETYPE mode = RESPONSETYPE.HTML;
			if(username.endsWith(".json")){
				username = username.replace(".json","");
				mode= RESPONSETYPE.JSON;
			}
			LOG.info("username "+username);
			if (null == username || username.length() == 0)
				throw new ImmopolyException("missing username", 61);

			User user = DBManager.getUserByUsername(pm, username);
			if (null == user) {
				throw new ImmopolyException("username not found " + username, 62);
			} else {
				LOG.info("Profile " + user.getUserName());
				if(mode==RESPONSETYPE.JSON){
					resp.getOutputStream().write(user.toPublicJSON().toString().getBytes("UTF-8"));
				}else if(mode==RESPONSETYPE.HTML){
					String template = getTemplate("Profile.html");
					template = template.replace("_USERNAME_", username);
					resp.setContentType("text/html");
					resp.getWriter().write(template);
				}
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
