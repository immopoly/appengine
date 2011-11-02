package org.immopoly.appengine.actions;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.History;
import org.immopoly.appengine.PMF;
import org.immopoly.appengine.User;
import org.immopoly.common.ImmopolyException;
import org.json.JSONArray;

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
public class ActionUserHistory extends AbstractAction {

	public ActionUserHistory(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/history";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String startS = req.getParameter(TOPXSTART);
			String endS = req.getParameter(TOPXEND);
			int start, end = 0;
			try {
				start = Integer.parseInt(startS);
				end = Integer.parseInt(endS);
			} catch (NumberFormatException nfe) {
				throw new ImmopolyException("start end not Integers" + startS + "," + endS, 93);
			}

			String token = req.getParameter(TOKEN);
			List<History> history;
			User user = null;
			if (null != token && token.length() > 0) {
				user = DBManager.getUserByToken(pm, token);
				LOG.info("History " + user.getUserName());
				if (null == user)
					throw new ImmopolyException("user by token not found " + token, 62);
			}

			JSONArray historyList = new JSONArray();
			history = DBManager.getHistory(pm, null == user ? null : user.getId(), start, end);
			for (History h : history) {
				historyList.put(h.toJSON());
			}
			resp.getOutputStream().write(historyList.toString().getBytes("UTF-8"));
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException("could not login user", 101, e);
		} finally {
			pm.close();
		}
	}
}
