/*
 * 11/11/2010
 *
 * SubstanceUtils.java - Utility methods for Java 1.4-compatible applications
 * looking to support Substance 6.1 if the current JRE is 1.6+.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;


/**
 * Utility methods for interfacing with Substance 7.2.1 (Insubstantial) in
 * applications that only require Java 1.4 or later (Substance 6 requires
 * Java 6).
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SubstanceUtils {

	public static final String FOREGROUND_COLOR			= "ForegroundColor";

	public static final String ULTRA_LIGHT_COLOR		= "UltraLightColor";

	public static final String EXTRA_LIGHT_COLOR		= "ExtraLightColor";

	public static final String LIGHT_COLOR				= "LightColor";

	public static final String MID_COLOR				= "MidColor";

	public static final String DARK_COLOR				= "DarkColor";

	public static final String ULTRA_DARK_COLOR			= "UltraDarkColor";

	public static final String LINE_COLOR				= "LineColor";

	public static final String SELECTION_BG_COLOR		= "SelectionBackgroundColor";

	public static final String SELECTION_FG_COLOR		= "SelectionForegroundColor";

	public static final String BACKGROUND_FILL_COLOR	= "BackgroundFillColor";

	public static final String TEXT_BG_FILL_COLOR		= "TextBackgroundFillColor";

	public static final String FOCUS_RING_COLOR			= "FocusRingColor";

	/**
	 * Package for the Substance public API.
	 */
	private static final String PKG = "org.pushingpixels.substance.api.";

	private static final String LAFWIDGET_PKG = "org.pushingpixels.lafwidget.";


	/**
	 * Returns the length of time GUI animations take, in milliseconds.
	 *
	 * @return The length of time, in milliseconds.
	 * @throws Exception If an error occurs.
	 * @see #setAnimationSpeed(long)
	 */
	public static long getAnimationSpeed() throws Exception {

		long speed = -1;

		ClassLoader cl = (ClassLoader)UIManager.get("ClassLoader");
		if (cl!=null) {
			String managerClassName =
				LAFWIDGET_PKG + "animation.AnimationConfigurationManager";
			Class<?> managerClazz = Class.forName(managerClassName, true, cl);
			Method m = managerClazz.getMethod("getInstance");
			Object manager = m.invoke(null);
			m = managerClazz.getMethod("getTimelineDuration");
			Long millis = (Long)m.invoke(manager);
			speed = millis.longValue();
		}

		return speed;

	}


	/**
	 * Returns a color from the currently active Substance skin.
	 *
	 * @param name The name of a Color, for example {@link #LIGHT_COLOR}.
	 * @return The color, or <code>null</code> if no color by that name is
	 *         defined.
	 * @throws Exception If an error occurs.
	 */
	public static Color getSubstanceColor(String name) throws Exception {

		/*
		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf instanceof SubstanceLookAndFeel) {
			SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
			SubstanceColorScheme scheme = skin.getActiveColorScheme(
												DecorationAreaType.NONE);
			scheme.getXXX();
		}
		*/

		Color color = null;
		name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

		LookAndFeel laf = UIManager.getLookAndFeel();
		ClassLoader cl = (ClassLoader)UIManager.get("ClassLoader");
		if (cl!=null) {
			Class<?> clazz = Class.forName(PKG + "SubstanceLookAndFeel", true, cl);
			if (clazz.isInstance(laf)) {
				Class<?> skinClazz = Class.forName(PKG + "SubstanceSkin", true, cl);
				Method m = clazz.getDeclaredMethod("getCurrentSkin");
				Object skin = m.invoke(null);
				Class<?> decAreaTypeClazz = Class.forName(PKG + "DecorationAreaType", true, cl);
				Field decAreaTypeField = decAreaTypeClazz.getDeclaredField("GENERAL");
				Object decAreaType = decAreaTypeField.get(null);
				m = skinClazz.getDeclaredMethod("getActiveColorScheme", new Class[] { decAreaTypeClazz });
				Object colorScheme = m.invoke(skin, new Object[] { decAreaType });
				Class<?> colorSchemeClazz = Class.forName(PKG + "SubstanceColorScheme", true, cl);
				m = colorSchemeClazz.getMethod("get" + name);
				color = (Color)m.invoke(colorScheme);
			}
		}

		return color;

	}


	/**
	 * Returns whether the currently installed LookAndFeel is Substance.
	 *
	 * @return Whether the currently installed LookAndFeel is Substance.
	 */
	public static boolean isSubstanceInstalled() {
		return isASubstanceLookAndFeel(UIManager.getLookAndFeel());
	}


	/**
	 * Returns whether a given LookAndFeel is a Substance LookAndFeel.
	 *
	 * @param laf The LookAndFeel.
	 * @return Whether it is a Substance LookAndFeel.
	 * @see #isASubstanceLookAndFeel(String)
	 * @see #isSubstanceInstalled()
	 */
	public static boolean isASubstanceLookAndFeel(LookAndFeel laf) {
		return isASubstanceLookAndFeel(laf.getClass().getName());
	}



	/**
	 * Returns whether a given LookAndFeel is a Substance LookAndFeel.
	 *
	 * @param lafName The LookAndFeel's class name.
	 * @return Whether it is a Substance LookAndFeel.
	 * @see #isASubstanceLookAndFeel(LookAndFeel)
	 * @see #isSubstanceInstalled()
	 */
	public static boolean isASubstanceLookAndFeel(String lafName) {
		return lafName.indexOf(".Substance")>-1;
	}



	/**
	 * Configures the length of GUI animations, in milliseconds.
	 *
	 * @param millis The amount of time animations should take.
	 * @throws Exception If an error occurs.
	 * @see #getAnimationSpeed()
	 */
	public static void setAnimationSpeed(long millis) throws Exception {

		ClassLoader cl = (ClassLoader)UIManager.get("ClassLoader");
		if (cl!=null) {
			String managerClassName =
				LAFWIDGET_PKG + "animation.AnimationConfigurationManager";
			Class<?> managerClazz = Class.forName(managerClassName, true, cl);
			Method m = managerClazz.getMethod("getInstance");
			Object manager = m.invoke(null);
			m = managerClazz.getMethod("setTimelineDuration",
					new Class[] { long.class });
			m.invoke(manager, new Object[] { new Long(millis) });
		}

	}


}