package org.immopoly.appengine.actions;

import java.net.URL;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immopoly.appengine.Const;
import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.Expose;
import org.immopoly.appengine.History;
import org.immopoly.appengine.OAuthData;
import org.immopoly.appengine.PMF;
import org.immopoly.appengine.User;
import org.immopoly.appengine.WebHelper;
import org.immopoly.common.ImmopolyException;
import org.json.JSONObject;

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
public class ActionExposeRemove extends AbstractAction implements Action {

	public ActionExposeRemove(Map<String, Action> actions) {
		super(actions);
	}

	@Override
	public String getURI() {
		return "portfolio/remove";
	}

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ImmopolyException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			String token = req.getParameter(TOKEN);
			String exposeId = req.getParameter(EXPOSE);

			if (null == token || token.length() == 0)
				throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_USERNAME, "missing token");

			User user = DBManager.getUserByToken(pm, token);
			if (null == user)
				throw new ImmopolyException(ImmopolyException.TOKEN_NOT_FOUND, "token not found " + token);

			History history = null;
			// first check if already owned
			Expose expose = DBManager.getExpose(pm, exposeId);
			if (null != expose) {
				if (expose.getUserId() == user.getId()) {
					URL url = new URL(OAuthData.SERVER + OAuthData.SEARCH_PREFIX + "expose/" + exposeId + ".json");
					JSONObject obj = WebHelper.getHttpData(url);
					if (null == obj) {
						throw new ImmopolyException(ImmopolyException.EXPOSE_NOT_FOUND, "Expose Response is null! " + exposeId);
					} else if (obj.has("expose.expose")) {
						double fine = Const.FINE_REMOVED * expose.getRent();
						history = new History(History.TYPE_EXPOSE_REMOVED, user.getId(), System.currentTimeMillis(), "Strafe: "
								+ History.MONEYFORMAT.format(fine) + " Du hast die Wohnung '" + expose.getName() + "' für "
								+ History.MONEYFORMAT.format(expose.getRent()) + " im Monat zurückgegeben.", -fine,
								expose.getExposeId(), null);
						pm.deletePersistent(expose);
						user.setBalance(user.getBalance() - fine);
						user.addExpose(-1);
						pm.makePersistent(user);
						pm.makePersistent(history);
						// } else if
						// (obj.toString().contains("RESOURCE_NOT_FOUND")) {
						// throw new
						// ImmopolyException(ImmopolyException.EXPOSE_NOT_FOUND,
						// "expose jibs nich mehr, eventuell heute schon vermietet");
						// } else {
						// LOG.log(Level.SEVERE,
						// "merkwürdig, merkwürdig, wo isset hin? " +
						// obj.toString());
						// throw new
						// ImmopolyException(ImmopolyException.EXPOSE_NOT_FOUND,
						// "merkwürdig, merkwürdig, wo isset hin? ");
					} else {
						// #https://github.com/immopoly/appengine/issues/20
						history = new History(History.TYPE_EXPOSE_REMOVED, user.getId(), System.currentTimeMillis(),
								"Du hast die Wohnung '" + expose.getName() + "' für " + History.MONEYFORMAT.format(expose.getRent())
										+ " im Monat zurückgegeben. Sie wurde aber nicht gefunden, keine Strafe.", 0.0,
								expose.getExposeId(),
								null);
						pm.deletePersistent(expose);
						user.addExpose(-1);
						pm.makePersistent(user);
						pm.makePersistent(history);
					}

				} else {
					throw new ImmopolyException(ImmopolyException.EXPOSE_NOT_OWNED, "gehört nem anderen penner");
				}
			} else {
				throw new ImmopolyException(ImmopolyException.EXPOSE_NOT_FOUND, "expose jehört dir nich");
			}
			// history eintrag
			resp.getOutputStream().write(history.toJSON().toString().getBytes("UTF-8"));
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException(ImmopolyException.EXPOSE_REMOVE_FAILED, "could not remove expose " + e.getMessage(), e);
		} finally {
			pm.close();
		}
	}
}
