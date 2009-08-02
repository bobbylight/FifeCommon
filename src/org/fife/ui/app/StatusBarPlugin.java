/*
 * 09/16/2005
 *
 * StatusBarPlugin.java - A plugin for a GUI application's status bar.
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

import org.fife.ui.StatusBarPanel;


/**
 * A plugin representing a component in a status bar.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class StatusBarPlugin extends StatusBarPanel implements Plugin {


	/**
	 * Returns whether this plugin wants its menu added to an application's
	 * "Plugins" menu.  The default implementation returns <code>true</code>.
	 * Override this method if you don't want this plugin's menu under the
	 * Plugins menu.
	 *
	 * @return Whether or not this plugin should have its menu put under
	 *         the "Plugins" menu.
	 * @see org.fife.ui.app.Plugin#getPluginMenu()
	 */
	public boolean getAddToPluginMenu() {
		return true;
	}


	/**
	 * Returns an options panel for use in an Options dialog.  This panel
	 * should contain all options pertaining to this plugin.
	 *
	 * @return The options panel.
	 */
	public abstract PluginOptionsDialogPanel getOptionsDialogPanel();


	/**
	 * Called just after a plugin is added to a GUI application.  If this is
	 * a <code>GUIPlugin</code>, it has already been added visually.  Plugins
	 * should use this method to register any listeners to the GUI application
	 * and do any other necessary setup.
	 *
	 * @param app The application to which this plugin was just added.
	 * @see #uninstall
	 */
	public abstract void install(AbstractPluggableGUIApplication app);


	/**
	 * Called when the GUI application is shutting down.  When this method is
	 * called, the <code>Plugin</code> should save any properties via the
	 * Java Preferences API.
	 *
	 * @see PluginPreferences
	 */
	public abstract void savePreferences();


	/**
	 * Called just before this <code>Plugin</code> is removed from an
	 * <code>GUIApplication</code>.  This gives the plugin a chance to clean
	 * up any loose ends (kill any threads, close any files, remove listeners,
	 * etc.).
	 *
	 * @return Whether the uninstall went cleanly.
	 * @see #install
	 */
	public abstract boolean uninstall();


}