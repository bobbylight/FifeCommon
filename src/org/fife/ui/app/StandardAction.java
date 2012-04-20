/*
 * 11/27/2004
 *
 * StandardAction.java - An action used by a GUIApplication implementation.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;


/**
 * The action type used by all instances of <code>GUIApplication</code>.  This
 * is merely an action with many ease-of-use methods.
 *
 * @author Robert Futrell
 * @version 0.6
 * @see org.fife.ui.app.GUIApplication
 */
public abstract class StandardAction extends AbstractAction {

	/**
	 * The parent GUI application.
	 */
	private GUIApplication app;


	/**
	 * Creates an action, initializing its properties from a resource bundle.
	 * The name of the action is found using the specified key.  If keys exist
	 * with the names <code>key + ".Mnemonic"</code>,
	 * <code>key + ".Accelerator"</code> or <code>key + ".ShortDesc"</code>,
	 * then those properties are set as well.
	 *
	 * @param app The parent application.
	 * @param msg The bundle to localize from.
	 * @param key The key in the bundle for the name of this action.
	 */
	public StandardAction(GUIApplication app, ResourceBundle msg, String key) {
		super(msg.getString(key));
		this.app = app;
		// TODO: Use msg.containsKey() when we drop 1.4/1.5 support
		try {
			String mnemonicKey = key + ".Mnemonic";
			setMnemonic((int)msg.getString(mnemonicKey).charAt(0));
		} catch (MissingResourceException mre) {
			// Swallow
		}
		try {
			String accelKey = key + ".Accelerator";
			String temp = msg.getString(accelKey);
			if (temp!=null) {
				// Use meta on OS X instead of ctrl
				if (app.getOS()==GUIApplication.OS_MAC_OSX &&
						((temp.startsWith("control ") ||
							temp.startsWith("ctrl ")) ||
							temp.startsWith("default "))) {
					int space = temp.indexOf(' ');
					temp = "meta" + temp.substring(space);
				}
				else if (temp.startsWith("default ")) {
					int space = temp.indexOf(' ');
					temp = "control" + temp.substring(space);
				}
				KeyStroke ks = KeyStroke.getKeyStroke(temp);
				setAccelerator(ks);
			}
		} catch (MissingResourceException mre) {
			// Swallow
		}
		try {
			String shortDescKey = key + ".ShortDesc";
			setShortDescription(msg.getString(shortDescKey));
		} catch (MissingResourceException mre) {
			// Swallow
		}
	}


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 * @param name The name of the action.
	 */
	public StandardAction(GUIApplication app, String name) {
		super(name);
		this.app = app;
	}


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 * @param name The name of the action.
	 * @param icon The icon associated with the action.
	 */
	public StandardAction(GUIApplication app, String name, Icon icon) {
		super(name);
		this.app = app;
		setIcon(icon);
	}


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 * @param name The name of the action.
	 * @param icon The icon associated with the action.
	 * @param desc The description of the action.
	 * @param mnemonic The mnemonic for the action.
	 * @param accelerator The accelerator key for the action.
	 */
	public StandardAction(GUIApplication app, String name, Icon icon,
					String desc, int mnemonic, KeyStroke accelerator) {
		super(name);
		this.app = app;
		setIcon(icon);
		setShortDescription(desc);
		setAccelerator(accelerator);
		setMnemonic(mnemonic);
	}


	/**
	 * Returns the accelerator for this action.
	 *
	 * @return The accelerator.
	 * @see #setAccelerator(KeyStroke)
	 */
	public KeyStroke getAccelerator() {
		return (KeyStroke)getValue(ACCELERATOR_KEY);
	}


	/**
	 * Returns the application.
	 *
	 * @return The application.
	 */
	public GUIApplication getApplication() {
		return app;
	}


	/**
	 * Returns the icon for this action.
	 *
	 * @return The icon.
	 * @see #setIcon(Icon)
	 */
	public Icon getIcon() {
		return (Icon)getValue(SMALL_ICON);
	}


	/**
	 * Returns the mnemonic for this action.
	 *
	 * @return The mnemonic, or <code>-1</code> if not defined.
	 * @see #setMnemonic(int)
	 */
	public int getMnemonic() {
		Integer i = (Integer)getValue(MNEMONIC_KEY);
		return i!=null ? i.intValue() : -1;
	}


	/**
	 * Returns the name of this action.
	 *
	 * @return The name of this action.
	 * @see #setName(String)
	 */
	public String getName() {
		return (String)getValue(NAME);
	}


	/**
	 * Returns the short description for this action.
	 *
	 * @return The description.
	 * @see #setShortDescription(String)
	 */
	public String getShortDescription() {
		return (String)getValue(SHORT_DESCRIPTION);
	}


	/**
	 * Sets the accelerator for this action.
	 *
	 * @param accelerator The new accelerator, or <code>null</code> for none.
	 * @see #getAccelerator()
	 */
	public void setAccelerator(KeyStroke accelerator) {
		putValue(ACCELERATOR_KEY, accelerator);
	}


	/**
	 * Sets the icon of this action.
	 *
	 * @param icon The icon.
	 * @see #getIcon()
	 */
	public void setIcon(Icon icon) {
		putValue(SMALL_ICON, icon);
	}


	/**
	 * Sets the mnemonic for this action.
	 *
	 * @param mnemonic The new mnemonic.  A value of <code>-1</code> means
	 *        "no mnemonic."
	 * @see #getMnemonic()
	 */
	public void setMnemonic(int mnemonic) {
		// TODO: When we drop 1.4 support, use Integer.valueOf(mnemonic).
		putValue(MNEMONIC_KEY, mnemonic>0 ? new Integer(mnemonic) : null);
	}


	/**
	 * Sets the name of this action.
	 *
	 * @param name The name of this action.
	 * @see #getName()
	 */
	public void setName(String name) {
		putValue(NAME, name);
	}


	/**
	 * Sets the short description for this action.
	 *
	 * @param desc The description.
	 * @see #getShortDescription()
	 */
	public void setShortDescription(String desc) {
		putValue(SHORT_DESCRIPTION, desc);
	}


}