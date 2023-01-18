/*
 * 01/07/2005
 *
 * MenuBar.java - The menu bar used by GUIApplications.
 * Copyright (C) 2005 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import org.fife.ui.StandardAction;
import org.fife.ui.StandardMenuItem;
import org.fife.ui.app.icons.IconGroup;
import org.fife.util.MacOSUtil;

import java.util.HashMap;
import java.util.Map;
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
 * @param <T> The type of application.
 */
public abstract class MenuBar<T extends AbstractGUIApplication<?>>
	extends JMenuBar {

	private T app;

	/**
	 * Registry of "named" menus, so you can grab a specific menu without
	 * knowing the order of menus in this menu bar.
	 */
	private Map<String, JMenu> namedMenus;


	protected MenuBar(T app) {

		this.app = app;

		initializeUI();

		// On OS's that use a "native" toolbar, ensure this menu
		// bar uses icons with appropriate contrast there, since
		// that may not match the theme of this application.
		if (MacOSUtil.isMacOs()) {
			updateIconsForNativeMenuBar(app.getIconGroup());
			app.addPropertyChangeListener(AbstractGUIApplication.ICON_STYLE_PROPERTY,
				e -> updateIconsForNativeMenuBar((IconGroup)e.getNewValue()));
		}
	}


	/**
	 * Adds an "extra" menu to this menu bar.
	 *
	 * @param menu The menu to add.
	 */
	public void addExtraMenu(JMenu menu) {
		add(menu, getExtraMenuInsertionIndex());
	}


	private static void configureMenuItem(JMenuItem item, String desc) {
		// Since these menu items are often configured with Actions, we must
		// explicitly set the tool tip text to null
		item.setToolTipText(null);
		item.getAccessibleContext().setAccessibleDescription(desc);
	}


	/**
	 * Returns a checkbox menu item configured from an action.
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
	 * Returns an <code>JMenu</code> with the specified text.  If there
	 * is an entry in the specified resource bundle with name
	 * {@code textKey + ".Mnemonic"}, it is taken to contain the mnemonic for
	 * the menu.  Further, if there is an entry in the specified resource
	 * bundle with name {@code textKey + ".ShortDesc"}, it is taken to contain
	 * a short description for the menu.
	 *
	 * @param msg The resource bundle in which to get the text.
	 * @param textKey The key into the bundle containing the string text value.
	 * @return The <code>JMenu</code>.
	 */
	protected JMenu createMenu(ResourceBundle msg, String textKey) {

		JMenu menu = new JMenu(msg.getString(textKey));

		String mnemonicKey = textKey + StandardAction.MNEMONIC_SUFFIX;
		if (msg.containsKey(mnemonicKey)) {
			menu.setMnemonic((int)msg.getString(mnemonicKey).charAt(0));
		}

		String shortDescKey = textKey + StandardAction.SHORT_DESC_SUFFIX;
		if (msg.containsKey(shortDescKey)) {
			configureMenuItem(menu, msg.getString(shortDescKey));
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
		String mnemonicKey = textKey + StandardAction.MNEMONIC_SUFFIX;
		if (msg.containsKey(mnemonicKey)) {
			item.setMnemonic((int)msg.getString(mnemonicKey).charAt(0));
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
		JMenuItem item = new StandardMenuItem(a);
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
		JMenuItem item = new StandardMenuItem(a);
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
		JMenuItem item = new StandardMenuItem(a);
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


	protected T getApplication() {
		return app;
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
		return namedMenus!=null ? namedMenus.get(name) : null;
	}


	/**
	 * Subclasses should override this method and add all
	 * menu items here.  Doing so ensures all other listeners are
	 * set up properly.
	 */
	protected abstract void initializeUI();


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
			namedMenus = new HashMap<>();
		}
		namedMenus.put(name, menu);
	}


	/**
	 * A hook that gets called when the application changes icon
	 * groups.  This is here for applications to use the "native"
	 * versions of action icons in the menu bar when in OS X.
	 * This method is only called if using the native versions of
	 * icons is appropriate.
	 *
	 * @param iconGroup The new icon group.
	 */
	protected void updateIconsForNativeMenuBar(IconGroup iconGroup) {
		// Do nothing (comment for Sonar)
	}


}
