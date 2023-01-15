/*
 * 12/28/2004
 *
 * Plugin.java - A generic plugin for a GUIApplication.
 * Copyright (C) 2004 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import org.fife.ui.app.icons.IconGroup;

import javax.swing.*;


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
 * </ul>
 *
 * @author Robert Futrell
 * @version 0.1
 * @param <T> The type of parent application.
 * @see org.fife.ui.app.GUIApplication
 * @see org.fife.ui.app.GUIPlugin
 * @see org.fife.ui.app.StatusBarPlugin
 */
public interface Plugin<T extends GUIApplication> {


	/**
	 * Returns the parent application this plugin is installed in.
	 *
	 * @return The parent application.
	 */
	T getApplication();


	/**
	 * Returns an options panel for use in an Options dialog.  This panel
	 * should contain all options pertaining to this plugin.
	 *
	 * @return The options panel.
	 */
	PluginOptionsDialogPanel<? extends Plugin<T>> getOptionsDialogPanel();


	/**
	 * Returns the panel that this plug-in's option panels should be added
	 * as children of.
	 *
	 * @return The ID of the parent panel, or <code>null</code> if they
	 *         should be added at the "root" level of option panels.
	 *
	 * @see #getOptionsDialogPanel()
	 */
	String getOptionsDialogPanelParentPanelID();


	/**
	 * Returns the author of the plugin.
	 *
	 * @return The author.
	 */
	String getPluginAuthor();


	/**
	 * Returns the icon to display beside the name of this plugin in the
	 * application's interface.
	 *
	 * @return The icon for this plugin.  This value may be <code>null</code>
	 *         to represent no icon.
	 */
	Icon getPluginIcon();


	/**
	 * Returns the name of the plugin.
	 *
	 * @return The plugin name.
	 */
	String getPluginName();


	/**
	 * Returns the version of the plugin.
	 *
	 * @return The version number of this plugin.
	 */
	String getPluginVersion();


	/**
	 * Updates any icons used by this plugin for a new icon group.  This is
	 * called whenever an application's icon group changes.
	 *
	 * @param iconGroup The new icon group.
	 * @see #themeChanged(AppTheme)
	 * @see #lookAndFeelChanged(LookAndFeel)
	 */
	void iconGroupChanged(IconGroup iconGroup);


	/**
	 * Called just after a plugin is added to a GUI application.  If this is
	 * a <code>GUIPlugin</code>, it has already been added visually.  Plugins
	 * should use this method to register any listeners to the GUI application
	 * and do any other necessary setup.
	 *
	 * @see #uninstall()
	 */
	void install();


	/**
	 * Called when the LookAndFeel changes.  This is a hook for plugins to
	 * manually update their popup dialogs.  Subclasses should override if
	 * necessary, but always call the super implementation.
	 *
	 * @param newLaf The new <code>LookAndFeel</code>.
	 * @see #themeChanged(AppTheme)
	 * @see #iconGroupChanged(IconGroup)
	 */
	void lookAndFeelChanged(LookAndFeel newLaf);


	/**
	 * Called when the GUI application is shutting down.  When this method is
	 * called, the <code>Plugin</code> should save any preferences.
	 */
	void savePreferences();


	/**
	 * Called whenever the application theme changes.
	 *
	 * @param newTheme The new application theme.
	 * @see #iconGroupChanged(IconGroup)
	 * @see #lookAndFeelChanged(LookAndFeel)
	 */
	void themeChanged(AppTheme newTheme);


	/**
	 * Called just before this <code>Plugin</code> is removed from a
	 * <code>GUIApplication</code>.  This gives the plugin a chance to clean
	 * up any loose ends (kill any threads, close any files, remove listeners,
	 * etc.).
	 *
	 * @return Whether the uninstall went cleanly.
	 * @see #install()
	 */
	boolean uninstall();
}
