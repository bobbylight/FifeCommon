/*
 * 12/28/2004
 *
 * GUIApplicationConstants.java - Constants used by GUI applications.
 * Copyright (C) 2004 Robert Futrell
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