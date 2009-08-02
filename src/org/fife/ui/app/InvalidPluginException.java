/*
 * 02/10/2005
 *
 * InvalidPluginException.java - Exception that is thrown when a plugin or
 * its jar is invalid in some way.
 * Copyright (C) 2005 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.ui.app;


/**
 * The exception that is thrown if a plugin JAR contains some kind of
 * error.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class InvalidPluginException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 182575532328519085L;


	/**
	 * Constructor.
	 *
	 * @param msg A message describing the exception.
	 */
	public InvalidPluginException(String msg) {
		super(msg);
	}


}