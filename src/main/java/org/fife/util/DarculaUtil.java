/*
 * 05/21/2016
 *
 * Copyright (C) 20016 Robert Futrell
 * http://fifesoft.com/rtext
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
public class DarculaUtil {


	public static boolean isDarculaUI(ComponentUI ui) {
		return ui.getClass().getSimpleName().contains("Darcula");
	}


	/**
	 * Returns whether the currently installed LookAndFeel is Substance.
	 *
	 * @return Whether the currently installed LookAndFeel is Substance.
	 */
	public static boolean isSubstanceInstalled() {
		LookAndFeel laf = UIManager.getLookAndFeel();
		return laf != null && "DarculaLaf".equals(laf.getClass().getSimpleName());
	}


}