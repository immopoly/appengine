package org.immopoly.appengine.actions;

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
public interface Action {
	public final String USERNAME = "username";
	public final String PASSWORD = "password";
	public final String TOKEN = "token";
	public final String EXPOSE = "expose";
	public final String EMAIL = "email";
	public final String TWITTER = "twitter";
	public final String C2DMREGISTRATIONID = "c2dmregistrationid";
	public final String TOPXSTART = "start";
	public final String TOPXEND = "end";
	public final String RANKTYPE = "ranktype";
	public final String ACTIONTYPE = "actiontype";
	public final String EXPOSES = "exposes";

	String getURI();

	void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException;
}
