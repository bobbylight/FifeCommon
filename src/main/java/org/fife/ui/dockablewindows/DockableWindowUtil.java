/*
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.dockablewindows;

import java.awt.Color;
import javax.swing.UIManager;


/**
 * Utility methods for the dockable windows package.
 *
 * @author Robert Futrell
 * @version 1.0
 */
final class DockableWindowUtil {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private DockableWindowUtil() {
	}


	public static Color getDockableWindowBorderColor() {
		// TODO: Check for null and return sensible default
		return UIManager.getColor("TabbedPane.shadow");
	}


}
