/*
 * 02/10/2005
 *
 * InvalidPluginException.java - Exception that is thrown when a plugin or
 * its jar is invalid in some way.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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