/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import javax.swing.*;


/**
 * A menu item that knows how to update more properties when its underlying
 * action changes, if it is an instance of {@link StandardAction}.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see StandardAction
 */
public class StandardMenuItem extends JMenuItem {


	/**
	 * Constructor.
	 *
	 * @param a The action to back the menu item.
	 */
	public StandardMenuItem(Action a) {
		super(a);
	}


	/**
	 * Constructor. Note you'd typically want to use an Action-based
	 * constructor rather than this one.
	 *
	 * @param text The label for the menu item.
	 */
	public StandardMenuItem(String text) {
		super(text);
	}


	@Override
	protected void actionPropertyChanged(Action action, String propertyName) {

		if (StandardAction.ROLLOVER_SMALL_ICON.equals(propertyName)) {
			setRolloverIconFromAction(action);
		}
		else {
			super.actionPropertyChanged(action, propertyName);
		}
	}


	@Override
	protected void configurePropertiesFromAction(Action action) {
		super.configurePropertiesFromAction(action);
		setRolloverIconFromAction(action);
	}


	private void setRolloverIconFromAction(Action action) {

		Icon icon = null;

		if (action != null) {
			Object value = action.getValue(StandardAction.ROLLOVER_SMALL_ICON);
			if (value instanceof Icon) {
				icon = (Icon)value;
			}
		}

		// Confusingly, it's the "selected" state that is equivalent to "rollover" for
		// JMenuItems.
		// See https://bugs.openjdk.org/browse/JDK-4776403 and
		// https://stackoverflow.com/questions/21693961/how-to-change-icon-for-jmenuitem-on-rollover
		setSelectedIcon(icon);
	}
}
