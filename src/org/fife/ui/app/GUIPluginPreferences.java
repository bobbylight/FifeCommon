/*
 * 05/03/2005
 *
 * GUIPluginPreferences.java - An interface for Preferences objects for
 * GUIPlugins.
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

import java.util.prefs.Preferences;


/**
 * Preferences for a <code>GUIPlugin</code>.  This class loads and saves all
 * properties common to <code>GUIPlugin</code>s, and should be extended for
 * any particular GUI plugin.
 *
 * @author Robert Futrell
 * @version 0.5
 * @see GUIPlugin
 */
public abstract class GUIPluginPreferences extends PluginPreferences {

	/**
	 * Whether the GUI plugin is active (visible).
	 */
	public boolean active;

	/**
	 * The GUI plugin's position.
	 */
	public int position;


	/**
	 * Loads the preferences specified in this class into the specified
	 * <code>GUIPluginPreferences</code> object.  This method can
	 * be called by any subclass's <code>load</code> method so they
	 * don't have to worry about doing it all themselves.
	 *
	 * @param prefs The preferences into which to load.  If any of the
	 *        the preferences aren't found in the backing store, the value
	 *        the value of those preferences saved in this instance are not
	 *        are not changed (i.e., the defaults are the current values).
	 * @param p The preferences backing store from which to retrieve
	 *        preferences.
	 */
	protected static void loadCommonPreferences(GUIPluginPreferences prefs,
										Preferences p) {
		prefs.active	= p.getBoolean("active", prefs.active);
		prefs.position	= p.getInt("position", prefs.position);
	}


	/**
	 * Saves the common preferences specified in this class.  This method
	 * can be called by any subclass's <code>save</code> method so they
	 * don't have to worry about doing it all themselves.
	 *
	 * @param prefs The preferences backing store into which to save
	 *        preferences.
	 */
	protected void saveCommonPreferences(Preferences prefs) {
		prefs.putBoolean("active", active);
		prefs.putInt("position", position);
	}


	/**
	 * Sets this preferences instance to contain all default values.
	 * Subclasses should remember to call the superclass implementation of
	 * this method when they override it so the common properties get
	 * set to default values.
	 */
	protected void setDefaults() {
		active = true;
		position = GUIPlugin.LEFT;
	}


}