package org.fife.ui.dockablewindows;

import java.awt.Color;
import javax.swing.UIManager;


/**
 * Utility methods for the dockable windows package.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DockableWindowUtil {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private DockableWindowUtil() {
	}


	public static Color getDockableWindowBorderColor() {
		// TODO: Check for null and return sensible default
		Color c = UIManager.getColor("TabbedPane.shadow");
		return c;
	}


}