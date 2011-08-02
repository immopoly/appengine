package org.immopoly.appengine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
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
@SuppressWarnings("serial")
public class ImmopolyServlet extends HttpServlet {
	static Logger LOG = Logger.getLogger(ImmopolyServlet.class.getName());

	public static Map<String, Action> actions = new HashMap<String, Action>();

	static {
		new ActionUserInfo(actions);
		new ActionUserLogin(actions);
		new ActionUserRegister(actions);
		new ActionExposeAdd(actions);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			req.setCharacterEncoding("UTF-8");
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("text/plain");
			String requestUri = req.getRequestURI();
			for (Map.Entry<String, Action> a : actions.entrySet()) {
				if (requestUri.contains(a.getKey())) {
					a.getValue().execute(req, resp);
					break;
				}
			}
		} catch (ImmopolyException e) {
			resp.getOutputStream().write(e.toJSON().toString().getBytes("UTF-8"));
		}
	}

}
