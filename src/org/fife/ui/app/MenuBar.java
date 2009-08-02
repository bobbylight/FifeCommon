/*
 * 01/07/2005
 *
 * MenuBar.java - The menu bar used by GUIApplications.
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

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;


/**
 * The menu bar used by instances of <code>GUIApplication</code>.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class MenuBar extends JMenuBar {

	private static final String MNEMONIC_SUFFIX	= ".Mnemonic";


	/**
	 * Adds an "extra" menu to this menu bar.
	 *
	 * @param menu The menu to add.
	 * @see #getExtraMenuInsertionIndex
	 */
	public void addExtraMenu(JMenu menu) {
		add(menu, getExtraMenuInsertionIndex());
	}


	private void configureMenuItem(JMenuItem item, String desc) {
		// Since these menu items are often configured with Actions, we must
		// explicitly set the tool tip text to null
		item.setToolTipText(null);
		item.getAccessibleContext().setAccessibleDescription(desc);
	}


	/**
	 * Returns an <code>JMenu</code> with the specified text.  Further, if
	 * there is an entry in the specified resource bundle with name
	 * <code>textKey + ".Mnemonic"</code>, it is taken to contain the
	 * mnemonic for the menu.
	 *
	 * @param msg The resource bundle in which to get the text.
	 * @param textKey The key into the bundle containing the string text value.
	 * @return The <code>JMenu</code>.
	 */
	protected JMenu createMenu(ResourceBundle msg, String textKey) {
		return createMenu(msg, textKey, textKey + MNEMONIC_SUFFIX);
	}


	/**
	 * Returns an <code>JMenu</code> with the specified text and mnemonic.
	 *
	 * @param msg The resource bundle in which to get the text.
	 * @param textKey The key into the bundle containing the string text value.
	 * @param mnemonicKey The key into the bundle containing a single-char
	 *        <code>String</code> value for the mnemonic.
	 * @return The <code>JMenu</code>.
	 */
	protected JMenu createMenu(ResourceBundle msg, String textKey,
							String mnemonicKey) {
		JMenu menu = new JMenu(msg.getString(textKey));
		// TODO: When we remove Java 1.4/1.5 support, use msg.containsKey()
		if (mnemonicKey!=null) {
			try {
				menu.setMnemonic((int)msg.getString(mnemonicKey).charAt(0));
			} catch (MissingResourceException mre) {
				// Swallow
			}
		}
		return menu;
	}


	/**
	 * Returns an <code>JMenu</code> with the specified text.  Further, if
	 * there is an entry in the specified resource bundle with name
	 * <code>textKey + ".Mnemonic"</code>, it is taken to contain the
	 * mnemonic for the menu.
	 *
	 * @param msg The resource bundle in which to get the text.
	 * @param textKey The key into the bundle containing the string text value.
	 * @return The <code>JMenu</code>.
	 */
	protected JMenuItem createMenuItem(ResourceBundle msg, String textKey) {
		return createMenuItem(msg, textKey, textKey + MNEMONIC_SUFFIX);
	}


	/**
	 * Returns an <code>JMenuItem</code> with the specified text and
	 * mnemonic.
	 *
	 * @param bundle The resource bundle in which to get the text.
	 * @param textKey The key into the bundle containing the string text value.
	 * @param mnemonicKey The key into the bundle containing a single-char
	 *        <code>String</code> value for the mnemonic.
	 * @return The <code>JMenuItem</code>.
	 */
	protected JMenuItem createMenuItem(ResourceBundle bundle,
								String textKey, String mnemonicKey) {
		JMenuItem item = new JMenuItem(bundle.getString(textKey));
		// TODO: When we remove Java 1.4/1.5 support, use msg.containsKey()
		if (mnemonicKey!=null) {
			try {
				item.setMnemonic((int)bundle.getString(mnemonicKey).charAt(0));
			} catch (MissingResourceException mre) {
				// Swallow
			}
		}
		return item;
	}


	/**
	 * Returns an <code>JMenuItem</code> with the specified text and
	 * mnemonic that performs the specified action when clicked.
	 *
	 * @param a The action that occurs when this item is clicked.
	 * @param bundle The resource bundle in which to get the text.
	 * @param textKey The key into the bundle containing the string text value.
	 * @param mnemonicKey The key into the bundle containing a single-char
	 *        <code>String</code> value for the mnemonic.
	 * @return The <code>JMenuItem</code>.
	 */
	protected JMenuItem createMenuItem(Action a, ResourceBundle bundle,
								String textKey, String mnemonicKey) {
		return createMenuItem(a, bundle, textKey, mnemonicKey, null);
	}


	/**
	 * Returns an <code>JMenuItem</code> with the specified text and
	 * mnemonic that performs the specified action when clicked.
	 *
	 * @param a The action that occurs when this item is clicked.
	 * @param bundle The resource bundle in which to get the text.
	 * @param textKey The key into the bundle containing the string text value.
	 * @param mnemonicKey The key into the bundle containing a single-char
	 *        <code>String</code> value for the mnemonic.
	 * @param accelerator The accelerator for the menu item.
	 * @return The <code>JMenuItem</code>.
	 */
	protected JMenuItem createMenuItem(Action a, ResourceBundle bundle,
					String textKey, String mnemonicKey, KeyStroke accelerator) {
		JMenuItem item = new JMenuItem(a);
		item.setText(bundle.getString(textKey));
		item.setMnemonic((int)bundle.getString(mnemonicKey).charAt(0));
		item.setAccelerator(accelerator);
		return item;
	}


	/**
	 * Returns a menu item with the passed-in properties.
	 *
	 * @param a The action that occurs on clicking this menu item.
	 * @param desc The accessible description.
	 * @return The menu item.
	 */
	protected JMenuItem createMenuItem(Action a, String desc) {
		JMenuItem item = new JMenuItem(a);
		configureMenuItem(item, desc);
		return item;
	}


	/**
	 * Returns a menu item with the passed-in properties.
	 *
	 * @param a The action that occurs on clicking this menu item.
	 * @return The menu item.
	 */
	protected JMenuItem createMenuItem(Action a) {
		JMenuItem item = new JMenuItem(a);
		String desc = (String)a.getValue(Action.SHORT_DESCRIPTION);
		configureMenuItem(item, desc);
		return item;
	}


	/**
	 * Returns a radio button menu item with the passed-in properties.
	 *
	 * @param a The action that occurs on clicking this menu item.
	 * @param description The accessible description.
	 * @return The radio button menu item.
	 */
	protected JRadioButtonMenuItem createRadioButtonMenuItem(Action a,
												String description) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(a);
		configureMenuItem(item, description);
		return item;
	}


	/**
	 * Returns the index at which to insert an "extra" menu.
	 *
	 * @return The index.
	 * @see #addExtraMenu
	 */
	protected int getExtraMenuInsertionIndex() {
		return getMenuCount()-1;
	}


}