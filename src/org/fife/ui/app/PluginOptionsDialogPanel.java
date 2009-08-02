/*
 * 04/28/2005
 *
 * PluginOptionsDialogPanel.java - An options panel for a plugin.
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

import javax.swing.Icon;

import org.fife.ui.OptionsDialogPanel;


/**
 * Options panel specifically for a plugin.  This subclass is aware of the
 * plugin it represents, and can update it with its values.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public abstract class PluginOptionsDialogPanel extends OptionsDialogPanel {

	private Plugin plugin;


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin whose options we're displaying.
	 */
	public PluginOptionsDialogPanel(Plugin plugin) {
		this(null, plugin);
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of the panel.
	 * @param plugin The plugin whose options we're displaying.
	 */
	public PluginOptionsDialogPanel(String name, Plugin plugin) {
		super(name);
		this.plugin = plugin;
	}


	/**
	 * Returns the icon to display for this options panel, if any.
	 *
	 * @return The icon for this options dialog panel, if any.
	 */
	public Icon getIcon() {
		return getPlugin().getPluginIcon();
	}


	/**
	 * Returns the plugin we're representing.
	 *
	 * @return The plugin.
	 */
	public Plugin getPlugin() {
		return plugin;
	}


}