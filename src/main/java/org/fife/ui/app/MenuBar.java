/*
 * 01/07/2005
 *
 * MenuBar.java - The menu bar used by GUIApplications.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
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
	 * Registry of "named" menus, so you can grab a specific menu without
	 * knowing the order of menus in this menu bar.
	 */
	private Map<String, JMenu> namedMenus;


	/**
	 * Adds an "extra" menu to this menu bar.
	 *
	 * @param menu The menu to add.
	 */
	public void addExtraMenu(JMenu menu) {
		add(menu, getExtraMenuInsertionIndex());
	}

	private static final void configureMenuItem(JMenuItem item, String desc) {
		// Since these menu items are often configured with Actions, we must
		// explicitly set the tool tip text to null
		item.setToolTipText(null);
		item.getAccessibleContext().setAccessibleDescription(desc);
	}


	/**
	 * Returns a check box menu item configured from an action.
	 *
	 * @param action The action associated with the menu item.
	 * @return The menu item.
	 */
	protected JCheckBoxMenuItem createCheckBoxMenuItem(Action action) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
		item.setToolTipText(null);
		return item;
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
		JMenu menu = new JMenu(msg.getString(textKey));
		// TODO: When we remove Java 1.4/1.5 support, use msg.containsKey()
		String mnemonicKey = textKey + MNEMONIC_SUFFIX;
		try {
			menu.setMnemonic((int)msg.getString(mnemonicKey).charAt(0));
		} catch (MissingResourceException mre) {
			// Swallow
		}
		return menu;
	}


	/**
	 * Returns a menu item with the specified text.  Further, if
	 * there is an entry in the specified resource bundle with name
	 * <code>textKey + ".Mnemonic"</code>, it is taken to contain the
	 * mnemonic for the menu.
	 *
	 * @param msg The resource bundle in which to get the text.
	 * @param textKey The key into the bundle containing the string text value.
	 * @return The menu item.
	 */
	protected JMenuItem createMenuItem(ResourceBundle msg, String textKey) {
		JMenuItem item = new JMenuItem(msg.getString(textKey));
		// TODO: When we remove Java 1.4/1.5 support, use msg.containsKey()
		String mnemonicKey = textKey + MNEMONIC_SUFFIX;
		try {
			item.setMnemonic((int)msg.getString(mnemonicKey).charAt(0));
		} catch (MissingResourceException mre) {
			// Swallow
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
	 * @see #addExtraMenu(JMenu)
	 */
	private int getExtraMenuInsertionIndex() {
		// Substance adds one of those search LafWidget thingies as the last
		// "menu item," which is returned as null from getMenu(menuCount-1).
		// So for Substance, we need add extra menus one item further "left."
		int count = getMenuCount();
		if (count==0) {
			return 0;
		}
		JMenu menu = getMenu(count-1);
		return menu!=null ? (count-1) : (count-2);
	}


	/**
	 * Returns the menu registered with the specified name.
	 *
	 * @param name The name.
	 * @return The menu, or <code>null</code> if no menu was registered with
	 *         that name.
	 * @see #registerMenuByName(String, JMenu)
	 */
	public JMenu getMenuByName(String name) {
		return namedMenus!=null ? (JMenu)namedMenus.get(name) : null;
	}


	/**
	 * Associates a name with a menu.  This allows applications to easily
	 * grab specific menus in this menu bar, without knowing the order of
	 * the menus.
	 *
	 * @param name The name.  This cannot be <code>null</code>.
	 * @param menu The menu.
	 * @see #getMenuByName(String)
	 */
	public void registerMenuByName(String name, JMenu menu) {
		if (name==null) { // HashMap permits null.
			throw new NullPointerException("menu name cannot be null");
		}
		if (namedMenus==null) {
			namedMenus = new HashMap<String, JMenu>();
		}
		namedMenus.put(name, menu);
	}


}