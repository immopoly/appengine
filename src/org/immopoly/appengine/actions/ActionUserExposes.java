package org.immopoly.appengine.actions;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.jdo.PersistenceManager;

import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.Expose;
import org.immopoly.appengine.User;
import org.immopoly.common.ImmopolyException;
import org.json.JSONArray;
import org.json.JSONException;

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
public class ActionUserExposes extends AbstractActionUser {

	public ActionUserExposes(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "user/exposes";
	}

	JSONArray getArray(PersistenceManager pm, User user, int start, int end) throws ImmopolyException {
		if (null == user)
			throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_TOKEN, "missing token");
		JSONArray array = new JSONArray();
		List<Expose> exposes = DBManager.getExposes(pm, user.getId(), start, end);
		if (exposes.size() == 0)
			throw new ImmopolyException(ImmopolyException.NO_MORE_DATA, "Keine Badges mehr von " + start + " bis " + end);

		for (Expose e : exposes) {
			try {
				array.put(e.toJSON());
			} catch (JSONException e1) {
				LOG.log(Level.WARNING, "", e1);
			}
		}
		return array;
	}
}
