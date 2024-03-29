/*
 * 05/21/2016
 *
 * Copyright (C) 20016 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.util;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;


/**
 * Utility methods to make FifeCommon mesh better with the Darcula Look and
 * Feel.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class DarculaUtil {

	/**
	 * The class name used by Darcula.
	 */
	public static final String CLASS_NAME = "com.bulenkov.darcula.DarculaLaf";


	/**
	 * Private constructor to prevent instantiation.
	 */
	private DarculaUtil() {
		// Do nothing
	}


	/**
	 * Returns whether the currently installed LookAndFeel is Darcula.
	 *
	 * @return Whether the currently installed LookAndFeel is Darcula.
	 * @see #isDarculaLookAndFeel(String)
	 */
	public static boolean isDarculaInstalled() {
		return isDarculaLookAndFeel(UIManager.getLookAndFeel());
	}


	/**
	 * Returns whether a LookAndFeel is Darcula.
	 *
	 * @param laf The LookAndFeel.  If this is {@code null}, {@code false} is
	 *        returned.
	 * @return Whether the LaF is Darcula.
	 * @see #isDarculaInstalled()
	 */
	public static boolean isDarculaLookAndFeel(LookAndFeel laf) {
		return laf != null && isDarculaLookAndFeel(laf.getClass().getSimpleName());
	}


	/**
	 * Returns whether a LookAndFeel is Darcula.
	 *
	 * @param laf The class name of the LookAndFeel.  If this is {@code null},
	 *        {@code null} is returned.
	 * @return Whether the LaF is Darcula.
	 * @see #isDarculaInstalled()
	 */
	public static boolean isDarculaLookAndFeel(String laf) {
		return laf != null && laf.endsWith("DarculaLaf");
	}


	/**
	 * Returns whether the given UI is a Darcula UI.
	 *
	 * @param ui The UI to check.  This cannot be {@code null}.
	 * @return Whether the UI is a Darcula UI.
	 */
	public static boolean isDarculaUI(ComponentUI ui) {
		return ui.getClass().getSimpleName().contains("Darcula");
	}


}
