/*
 * 04/28/2005
 *
 * PluginOptionsDialogPanel.java - An options panel for a plugin.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;

import javax.swing.*;


/**
 * Options panel specifically for a plugin.  This subclass is aware of the
 * plugin it represents, and can update it with its values.
 *
 * @param <T> The type of plugin this option panel is configuring.
 * @author Robert Futrell
 * @version 0.6
 */
public abstract class PluginOptionsDialogPanel<T extends Plugin> extends OptionsDialogPanel {

	private T plugin;


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin whose options we're displaying.
	 */
	public PluginOptionsDialogPanel(T plugin) {
		this(null, plugin);
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of the panel.
	 * @param plugin The plugin whose options we're displaying.
	 */
	public PluginOptionsDialogPanel(String name, T plugin) {

		super(name);
		this.plugin = plugin;

		boolean isDarkLookAndFeel = UIUtil.isLightForeground(new JLabel().getForeground());
		setIcon(plugin.getPluginIcon(isDarkLookAndFeel));
	}


	/**
	 * Returns the plugin we're representing.
	 *
	 * @return The plugin.
	 */
	public T getPlugin() {
		return plugin;
	}


}
