/*
 * 05/03/2005
 *
 * PluginPreferences.java - An interface for Preferences objects for Plugins.
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
 * Preferences for a <code>Plugin</code>.  This class loads and saves all
 * properties common to <code>Plugin</code>s, and should be extended for
 * any particular plugin.
 *
 * @author Robert Futrell
 * @version 0.5
 * @see Plugin
 */
public abstract class PluginPreferences {


	/**
	 * Initializes this preferences instance with data saved previously via
	 * the Java Preferences API.  If the load fails, the result is
	 * implementation-specific; usually this preferences instance will be
	 * populated with default values.<p>
	 *
	 * The default implementation returns <code>null</code>.  This method
	 * should be overridden to return properties for the plugin.
	 *
	 * @return If the load went okay, the preferences for the given plugin
	 *         are returned.  If something went wrong, or the user has never
	 *         used this plugin before (and thus there are no saved
	 *         preferences), default values are returned.
	 */
	public static PluginPreferences load() {
		return null;
	}


	/**
	 * Saves this preferences instance to permanent, OS-dependant backing
	 * store.
	 */
	public abstract void save();


	/**
	 * Sets this preferences instance to contain all default values.
	 */
	protected abstract void setDefaults();


}