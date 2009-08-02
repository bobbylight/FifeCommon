/*
 * 01/24/2006
 *
 * MenuPlugin.java - A plugin that adds a menu to an application.
 * Copyright (C) 2006 Robert Futrell
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

import javax.swing.JMenu;


/**
 * A plugin that adds a menu to an application.
 *
 * @author Robert Futrell
 * @version 0.1
 * @see org.fife.ui.app.GUIApplication
 */
/*
 * TODO: THIS PLUGIN TYPE CURRENTLY DOES NOT WORK.  This is because of a race
 * condition - this class's install() method wants the GUIApplication's
 * JMenuBar to have been created already, but PluggableGUIApplications load
 * and try to install all plugins before the menu bar is created (so PluginMenu
 * works).
 */
public abstract class MenuPlugin implements Plugin {

	private JMenu menu;


	/**
	 * Creates the menu for this plugin.
	 *
	 * @return The menu for this plugin.
	 * @see org.fife.ui.app.Plugin#getPluginMenu()
	 */
	protected abstract JMenu createMenu();


	/**
	 * Returns whether this plugin wants its menu added to an application's
	 * "Plugins" menu.  This method returns <code>false</code> as
	 * <code>MenuPlugin</code>s want their menu in the application's menu
	 * bar.
	 *
	 * @return Whether or not this plugin should have its menu put under
	 *         the "Plugins" menu.
	 * @see org.fife.ui.app.Plugin#getPluginMenu()
	 */
	public final boolean getAddToPluginMenu() {
		return false;
	}


	/**
	 * Returns the menu for this plugin.  This menu is added to the
	 * application's menu bar.
	 *
	 * @return The menu for this plugin.
	 */
	public synchronized final JMenu getPluginMenu() {
		if (menu==null)
			menu = createMenu();
		return menu;
	}


	/**
	 * Returns an options panel for use in an Options dialog.  This panel
	 * should contain all options pertaining to this plugin.
	 *
	 * @return The options panel.
	 */
	public abstract PluginOptionsDialogPanel getOptionsDialogPanel();


	/**
	 * Called just after a plugin is added to a GUI application.  This method
	 * adds this plugin's menu to the application's menu bar.
	 *
	 * @param app The application to which this plugin was just added.
	 * @see #uninstall
	 */
	public void install(final AbstractPluggableGUIApplication app) {
		((MenuBar)app.getJMenuBar()).addExtraMenu(getPluginMenu());
	}


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
	 * <code>GUIApplication</code>.  This method removes this plugin's
	 * menu from the application.
	 *
	 * @return Whether the uninstall went cleanly.
	 * @see #install
	 */
	public boolean uninstall() {
//		((MenuBar)app.getJMenuBar()).remove(getMenu());
		return true;
	}


}