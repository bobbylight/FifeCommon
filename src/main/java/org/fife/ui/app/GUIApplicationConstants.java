/*
 * 12/28/2004
 *
 * GUIApplicationConstants.java - Constants used by GUI applications.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import org.fife.ui.dockablewindows.DockableWindowConstants;


/**
 * The constants needed by the various pieces of a GUI application.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public interface GUIApplicationConstants extends DockableWindowConstants {

	/**
	 * Integer constant representing a Windows-variant OS.
	 */
	public static final int OS_WINDOWS			= 1;

	/**
	 * Integer constant representing Mac OS X.
	 */
	public static final int OS_MAC_OSX			= 2;

	/**
	 * Integer constant representing Linux.
	 */
	public static final int OS_LINUX			= 4;

	/**
	 * Integer constant representing an "unknown" OS.  99.99% of the
	 * time, this means some UNIX variant (AIX, SunOS, etc.).
	 */
	public static final int OS_OTHER			= 8;


}