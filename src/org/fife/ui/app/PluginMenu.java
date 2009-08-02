/*
 * 04/28/2005
 *
 * PluginMenu.java - A menu containing all options for an application's plugins.
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

import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.fife.ui.OptionsDialog;

/**
 * A tailor-made menu for displaying all necessary information about an
 * <code>AbstractPluggableGUIApplication</code>'s plugins.  This menu
 * contains popup submenus for all plugins that would like one, as well as an
 * options dialog for modifying all options pertaining to the plugins.<p>
 *
 * Your applicaiton can add a <code>PluginMenu</code> to its menu bar as
 * follows:
 * <pre>
 *   menuBar.add(new PluginMenu(app));
 * </pre>
 * The plugin menu will take care of its menu items itself, and will be
 * localized (except for the submenus, which are the individual plugins'
 * responsibilities).
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class PluginMenu extends JMenu {

	private AbstractPluggableGUIApplication app;
	private boolean showIcons;

	private static final String MSG = "org.fife.ui.app.PluginMenu";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Creates a plugin menu.
	 *
	 * @param app The GUI application.
	 */
	public PluginMenu(AbstractPluggableGUIApplication app, boolean showIcons) {

		this.app = app;
		setShowIcons(showIcons);

		setText(msg.getString("Name.text"));
		setMnemonic(msg.getString("Name.mnemonic").charAt(0));

		addSeparator();
		String text = msg.getString("PluginOptions.text");
		char mnemonic = msg.getString("PluginOptions.mnemonic").charAt(0);
		String desc = msg.getString("PluginOptions.description");
		JMenuItem item = new JMenuItem(new PluginOptionsAction(text, null,
							desc, new Integer(mnemonic), null));
		add(item);

	}


	/**
	 * Removes and re-adds menu items for all installed/activated
	 * plugins.
	 */
	private void addPluginMenuItems() {

		// Installed/activated plugins may have changed from the last
		// time this menu was displayed.

		// Remove old plugin items.
		int count = getMenuComponentCount() - 2; // Skip bottom 2.
		for (int i=0; i<count; i++) {
			remove(0);
		}

		// Add new plugin items.
		JMenuItem item = null;
		Plugin[] plugins = app.getPlugins();
		count = plugins.length;
		if (count==0) {
			item = new JMenuItem(msg.getString("NoPluginsInstalled.text"));
			item.setEnabled(false);
			add(item, 0);
		}
		else {
			ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());
			for (int i=0; i<count; i++) {
				Plugin plugin = plugins[i];
				if (plugin.getAddToPluginMenu()) {
					JMenu menu = plugin.getPluginMenu();
					if (menu!=null) {
						add(menu, 0);
					}
					else {
						menu = new JMenu(plugin.getPluginName());
						item = new JMenuItem(msg.getString("NoOptions.text"));
						item.setEnabled(false);
						menu.add(item);
						add(menu, 0);
					}
					menu.applyComponentOrientation(o);
					if (getShowIcons()) {
						menu.setIcon(plugin.getPluginIcon());
					}
				}
			}
		}

	}


	/**
	 * Overridden to always repopulate the menu when it is displayed.
	 */
	protected void fireMenuSelected() {
		addPluginMenuItems();
		super.fireMenuSelected();
	}


	/**
	 * Returns whether or not to show the icons for plugins.
	 *
	 * @return Whether or not to display plugins' icons.
	 */
	public boolean getShowIcons() {
		return showIcons;
	}


	/**
	 * Sets whether or not to show the icons for plugins.
	 *
	 * @param show Whether or not to display plugins' icons.
	 * @see #getShowIcons
	 */
	private void setShowIcons(boolean show) {
		showIcons = show;
	}


	/**
	 * An action that displays the application's plugin options dialog.
	 */
	private class PluginOptionsAction extends AbstractAction {

		public PluginOptionsAction(String text, Icon icon, String desc,
							Integer mnemonic, KeyStroke accelerator) {
			super(text, icon);
			putValue(ACCELERATOR_KEY, accelerator);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public void actionPerformed(ActionEvent e) {
			OptionsDialog optionsDialog = app.getPluginOptionsDialog();
			optionsDialog.initialize();		// Inivialize values.
			// This will take care of updating values itself.
			optionsDialog.setVisible(true);
		}

	}


}