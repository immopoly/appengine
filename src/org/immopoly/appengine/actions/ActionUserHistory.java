package org.immopoly.appengine.actions;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.History;
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
public class ActionUserHistory extends AbstractActionUser {

	public ActionUserHistory(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/history";
	}

	JSONArray getArray(PersistenceManager pm, User user, int start, int end) throws ImmopolyException {
		JSONArray array = new JSONArray();
		List<History> history = DBManager.getHistory(pm, null == user ? null : user.getId(), start, end);
		if (history.size() == 0)
			throw new ImmopolyException(ImmopolyException.NO_MORE_DATA, "Keine Daten mehr von " + start + " bis " + end);

		for (History h : history) {
			if (null == user)
				h.loadUsername(pm);
			array.put(h.toJSON());
		}
		return array;
	}
}
