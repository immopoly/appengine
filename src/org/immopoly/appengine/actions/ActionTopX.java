package org.immopoly.appengine.actions;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immopoly.appengine.DBManager;
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
public class ActionTopX extends AbstractAction {

	public static String RANKTYPE_BALANCE_ALL = "balance";
	public static String RANKTYPE_BALANCE_MONTH = "balanceMonth";
	
	public ActionTopX(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/top";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
//			LOG.info("TopX " + req.getRequestURI());
			String startS = req.getParameter(TOPXSTART);
			String endS = req.getParameter(TOPXEND);
			int start, end = 0;
			try {
				start = Integer.parseInt(startS);
				end = Integer.parseInt(endS);
			} catch (NumberFormatException nfe) {
				throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_START_END,"start end not Integers" + startS + "," + endS);
			}
			//https://github.com/immopoly/appengine/issues/14
			String rankRowAndDirection;
			String rankType = req.getParameter(RANKTYPE);
			if(null!=rankType && RANKTYPE_BALANCE_ALL.equals(rankType))
			{
				rankRowAndDirection="balance DESC";
			}else{
				rankRowAndDirection="balanceMonth DESC";
			}
			
			List<User> users = DBManager.getTopUser(pm, start, end, rankRowAndDirection);
			if (0 == users.size()) {
				throw new ImmopolyException(ImmopolyException.NO_MORE_DATA,"no more users ");
			} else {
				JSONArray topx = new JSONArray();
				for (User user : users) {
					topx.put(user.toPublicJSON());
				}
				resp.getOutputStream().write(topx.toString().getBytes("UTF-8"));
			}
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException(ImmopolyException.TOPX_FAILED, "could not return topX "+e.getMessage(), e);
		} finally {
			pm.close();
		}
	}
}
