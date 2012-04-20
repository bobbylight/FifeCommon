/*
 * 12/28/2004
 *
 * Plugin.java - A generic plugin for a GUIApplication.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import javax.swing.Icon;


/**
 * A "plugin" in a GUI application is a class that extends that application
 * in some way.  There are several base classes of plugins that you can
 * subclass to make a plugin for your application, including:
 *
 * <ul>
 *   <li><code>GUIPlugin</code> is an actual GUI panel containing widgets,
 *       etc., and is either docked on one of the four sides of the main
 *       window or in its own floating window.
 *   <li><code>StatusBarPlugin</code> is a widget added to the application's
 *       status bar.
 *   <li><code>WizardPlugin</code> is a series of dialogs that guides the
 *       user through some process.
 * </ul>
 *
 * @author Robert Futrell
 * @version 0.1
 * @see org.fife.ui.app.GUIApplication
 * @see org.fife.ui.app.GUIPlugin
 * @see org.fife.ui.app.StatusBarPlugin
 * @see org.fife.ui.app.WizardPlugin
 */
public interface Plugin {


	/**
	 * Returns an options panel for use in an Options dialog.  This panel
	 * should contain all options pertaining to this plugin.
	 *
	 * @return The options panel.
	 */
	public PluginOptionsDialogPanel getOptionsDialogPanel();


	/**
	 * Returns the panel that this plug-in's option panels should be added
	 * as children of.
	 *
	 * @return The ID of the parent panel, or <code>null</code> if they
	 *         should be added at the "root" level of option panels.
	 * 
	 * @see #getOptionsDialogPanel()
	 */
	public String getOptionsDialogPanelParentPanelID();


	/**
	 * Returns the author of the plugin.
	 *
	 * @return The author.
	 */
	public String getPluginAuthor();


	/**
	 * Returns the icon to display beside the name of this plugin in the
	 * application's interface.
	 *
	 * @return The icon for this plugin.  This value may be <code>null</code>
	 *         to represent no icon.
	 */
	public Icon getPluginIcon();


	/**
	 * Returns the name of the plugin.
	 *
	 * @return The plugin name.
	 */
	public String getPluginName();


	/**
	 * Returns the version of the plugin.
	 *
	 * @return The version number of this plugin.
	 */
	public String getPluginVersion();


	/**
	 * Called just after a plugin is added to a GUI application.  If this is
	 * a <code>GUIPlugin</code>, it has already been added visually.  Plugins
	 * should use this method to register any listeners to the GUI application
	 * and do any other necessary setup.
	 *
	 * @param app The application to which this plugin was just added.
	 * @see #uninstall
	 */
	public void install(AbstractPluggableGUIApplication app);


	/**
	 * Called when the GUI application is shutting down.  When this method is
	 * called, the <code>Plugin</code> should save any preferences.
	 */
	public void savePreferences();


	/**
	 * Called just before this <code>Plugin</code> is removed from an
	 * <code>GUIApplication</code>.  This gives the plugin a chance to clean
	 * up any loose ends (kill any threads, close any files, remove listeners,
	 * etc.).
	 *
	 * @return Whether the uninstall went cleanly.
	 * @see #install
	 */
	public boolean uninstall();


}