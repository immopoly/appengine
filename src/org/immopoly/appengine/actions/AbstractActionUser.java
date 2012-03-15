package org.immopoly.appengine.actions;

import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.immopoly.appengine.DBManager;
import org.immopoly.appengine.PMF;
import org.immopoly.appengine.User;
import org.immopoly.common.ImmopolyException;
import org.json.JSONArray;

public abstract class AbstractActionUser extends AbstractAction {

	public AbstractActionUser(Map<String, Action> actions) {
		super(actions);
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
				throw new ImmopolyException(ImmopolyException.MISSING_PARAMETER_START_END,"start end not Integers" + startS + "," + endS);
			}
			
			if(end-start > 50)
				throw new ImmopolyException(ImmopolyException.PARAMETER_START_END_TO_WIDE, "end - start > 50 " + startS + "," + endS);
	
			String token = req.getParameter(TOKEN);
			User user = null;
			if (null != token && token.length() > 0) {
				user = DBManager.getUserByToken(pm, token);
				if (null == user)
					throw new ImmopolyException(ImmopolyException.TOKEN_NOT_FOUND, "user by token not found " + token);
				LOG.info("History " + user.getUserName());
			}
	
			JSONArray array = getArray(pm, user, start, end);
			resp.getOutputStream().write(array.toString().getBytes("UTF-8"));
		} catch (ImmopolyException e) {
			throw e;
		} catch (Exception e) {
			throw new ImmopolyException(ImmopolyException.HISTORY_FAILED,"could not show history", e);
		} finally {
			pm.close();
		}
	}

	abstract JSONArray getArray(PersistenceManager pm, User user, int start, int end) throws ImmopolyException;

}