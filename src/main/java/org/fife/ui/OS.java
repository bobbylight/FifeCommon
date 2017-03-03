/*
 * 12/16/2014
 *
 * OS.java - An enumeration of operating systems.
 * Copyright (C) 2014 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;


/**
 * An enumeration of operating systems.  Also provides a means of getting the
 * current operating system.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public enum OS {

	/**
	 * Any Windows variant.
	 */
	WINDOWS(false),

	/**
	 * Any version of OS X.
	 */
	MAC_OS_X(false),

	/**
	 * Any Linux variant.
	 */
	LINUX(true),

	/**
	 * Solaris.
	 */
	SOLARIS(true),

	/**
	 * Any Operating System that does not match any of the other values in
	 * this enum, such as z/OS.
	 */
	OTHER(true);


	private boolean caseSensitive;

	private static OS os;


	OS(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}


	/**
	 * Returns the current OS.  This can be handy for special-case situations
	 * such as Mac OS X (special application registration) or Windows (allow
	 * mixed case, etc.).
	 *
	 * @return The OS we're running on.
	 */
	public static final OS get() {
		return os;
	}


	/**
	 * Returns whether the OS is case sensitive.
	 *
	 * @return Whether the OS is case sensitive.
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}


	static {
		os = OTHER;
		String osName = System.getProperty("os.name");
		if (osName!=null) { // Should always be true.
			osName = osName.toLowerCase();
			if (osName.indexOf("windows") > -1)
				os = WINDOWS;
			else if (osName.indexOf("mac os x") > -1)
				os = MAC_OS_X;
			else if (osName.indexOf("linux") > -1)
				os = LINUX;
			else
				os = OTHER;
		}
	}


}