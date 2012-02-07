package org.immopoly.appengine.actions;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.immopoly.appengine.Badge;
import org.immopoly.appengine.DBManager;
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
public class ActionUserBadges extends AbstractActionUser {

	public ActionUserBadges(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/badges";
	}

	JSONArray getArray(PersistenceManager pm, User user, int start, int end) throws ImmopolyException {
		JSONArray array = new JSONArray();
		List<Badge> badges = DBManager.getBadges(pm, null == user ? null : user.getId(),null, start, end);
		if (badges.size() == 0)
			throw new ImmopolyException(ImmopolyException.NO_MORE_DATA, "Keine Badges mehr von " + start + " bis " + end);

		for (Badge b : badges) {
			if (null == user)
				b.loadUsername(pm);
			array.put(b.toJSON());
		}
		return array;
	}
}
