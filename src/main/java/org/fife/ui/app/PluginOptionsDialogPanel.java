/*
 * 04/28/2005
 *
 * PluginOptionsDialogPanel.java - An options panel for a plugin.
 * Copyright (C) 2005 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import org.fife.ui.OptionsDialogPanel;


/**
 * Options panel specifically for a plugin.  This subclass is aware of the
 * plugin it represents, and can update it with its values.
 *
 * @author Robert Futrell
 * @version 0.6
 * @param <P> The type of plugin this option panel is configuring.
 */
public abstract class PluginOptionsDialogPanel<P extends Plugin<?>> extends OptionsDialogPanel {

	private P plugin;


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin whose options we're displaying.
	 */
	public PluginOptionsDialogPanel(P plugin) {
		this(null, plugin);
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of the panel.
	 * @param plugin The plugin whose options we're displaying.
	 */
	public PluginOptionsDialogPanel(String name, P plugin) {
		super(name);
		this.plugin = plugin;
		setIcon(plugin.getPluginIcon());
	}


	/**
	 * Returns the plugin we're representing.
	 *
	 * @return The plugin.
	 */
	public P getPlugin() {
		return plugin;
	}


}
