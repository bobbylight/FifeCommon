/*
 * 12/28/2004
 *
 * GUIPlugin.java - A GUI plugin for an AbstractPluggableGUIApplication.
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

import javax.swing.Icon;

import org.fife.ui.dockablewindows.DockableWindow;


/**
 * A plugin that is a panel either docked to one of the four sides of
 * a GUI application's window, or in a floating window.
 *
 * @author Robert Futrell
 * @version 0.7
 * @see org.fife.ui.app.Plugin
 * @see org.fife.ui.app.AbstractPluggableGUIApplication
 * @see org.fife.ui.dockablewindows.DockableWindow
 */
public abstract class GUIPlugin extends DockableWindow implements Plugin,
										GUIApplicationConstants {


	/**
	 * Creates a preferences instance for this GUI plugin based on its
	 * current properties.  Your GUI plugin should create a subclass of
	 * <code>GUIPluginPreferences</code> that loads and saves properties
	 * specific to your plugin, and return it from this method.
	 *
	 * @return A preferences instance.
	 * @see GUIPluginPreferences
	 */
	protected abstract GUIPluginPreferences createPreferences();


	/**
	 * Returns the name of this dockable window.  The name is
	 * displayed on tabs, etc.
	 *
	 * @return The name of this dockable window.
	 * @see #setDockableWindowName(String)
	 */
	public String getDockableWindowName() {
		return getPluginName();
	}


	/**
	 * Returns the icon to display beside the name of this dockable window
	 * in the application's interface.
	 *
	 * @return The icon for this dockable window.  This value may be
	 *         <code>null</code> to represent no icon.
	 */
	public Icon getIcon() {
		return getPluginIcon();
	}


	/**
	 * Called when the GUI application is shutting down.  When this method is
	 * called, the <code>Plugin</code> should save any properties via the
	 * Java Preferences API.
	 *
	 * @see PluginPreferences
	 * @see GUIPluginPreferences
	 */
	public void savePreferences() {
		createPreferences().save();
	}


}