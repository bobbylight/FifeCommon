/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.util;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;


/**
 * Utility methods to make FifeCommon mesh better with FlatLaf.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class FlatUtil {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private FlatUtil() {
		// Do nothing
	}


	/**
	 * Returns whether the currently installed LookAndFeel is a FlatLaf variant.
	 *
	 * @return Whether the currently installed LookAndFeel is a FlatLaf variant.
	 * @see #isFlatLookAndFeel(String)
	 */
	public static boolean isFlatLafInstalled() {
		return isFlatLookAndFeel(UIManager.getLookAndFeel());
	}


	/**
	 * Returns whether a LookAndFeel is Flat.
	 *
	 * @param laf The LookAndFeel.  If this is {@code null}, {@code false} is
	 *        returned.
	 * @return Whether the LaF is a FlatLaf variant.
	 * @see #isFlatLafInstalled()
	 */
	public static boolean isFlatLookAndFeel(LookAndFeel laf) {
		return laf != null && isFlatLookAndFeel(laf.getClass().getSimpleName());
	}


	/**
	 * Returns whether a LookAndFeel is Flat.
	 *
	 * @param laf The class name of the LookAndFeel.  If this is {@code null},
	 *        {@code null} is returned.
	 * @return Whether the LaF is a FlatLaf variant.
	 * @see #isFlatLafInstalled()
	 */
	public static boolean isFlatLookAndFeel(String laf) {
		return laf != null && laf.contains("Flat");
	}

	/**
	 * Returns whether the specified UI is a FlatLaf UI.
	 *
	 * @param ui The UI to check.
	 * @return Whether it's a FlatLaf UI.
	 */
	public static boolean isFlatUI(ComponentUI ui) {
		return ui.getClass().getSimpleName().contains("Flat");
	}


}
