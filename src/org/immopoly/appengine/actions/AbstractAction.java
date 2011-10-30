package org.immopoly.appengine.actions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.logging.Logger;

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
public abstract class AbstractAction implements Action {
	
	public static enum RESPONSETYPE{
		HTML,JSON
	}
	
	static Logger LOG = Logger.getLogger(AbstractAction.class.getName());
	RESPONSETYPE responseType = RESPONSETYPE.JSON;
	
	protected AbstractAction(Map<String, Action> actions) {
		actions.put(getURI(), this);
	}
	
	protected String getTemplate(String template) {
		return readFileAsString(template);
	}

	static String readFileAsString(String filePath) {
		try {
			byte[] buffer = new byte[(int) new File(filePath).length()];
			BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
			return new String(buffer);
		} catch (Exception e) {
			LOG.severe("Konnte template " + filePath + " nich lesen " + e.getMessage());
			return "Konnte template " + filePath + " nich lesen " + e.getMessage();
		}
	}
}
