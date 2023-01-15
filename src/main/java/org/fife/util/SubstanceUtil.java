/*
 * 11/11/2010
 *
 * Copyright (C) 2003 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.util;

import org.fife.substance.DummySubstanceProps;
import org.fife.substance.SubstanceProps;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;


/**
 * Utility methods for interfacing with Substance.  Access is done via reflection to avoid forcing a
 * hard runtime dependency on applications consuming this library.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class SubstanceUtil {

	private static SubstanceProps props;

	private static final String SUBSTANCE_PROPS_IMPL_CLASS = "org.fife.substance.SubstancePropsImpl";

	/**
	 * Private constructor to prevent instantiation.
	 */
	private SubstanceUtil() {
		// Do nothing
	}

	/**
	 * Returns the animation speed.
	 *
	 * @return The animation speed, in milliseconds.
	 * @see #setAnimationSpeed(long)
	 */
	public static long getAnimationSpeed() {
		loadSubstanceProps();
		return props.getAnimationSpeed();
	}

	/**
	 * Returns the "dark" color.
	 *
	 * @return The color.
	 */
	public static Color getDarkColor() {
		loadSubstanceProps();
		return props.getDarkColor();
	}

	/**
	 * Returns the "extra light" color.
	 *
	 * @return The color.
	 */
	public static Color getExtraLightColor() {
		loadSubstanceProps();
		return props.getExtraLightColor();
	}

	/**
	 * Returns the "light" color.
	 *
	 * @return The color.
	 */
	public static Color getLightColor() {
		loadSubstanceProps();
		return props.getLightColor();
	}

	/**
	 * Returns the "mid" color.
	 *
	 * @return The color.
	 */
	public static Color getMidColor() {
		loadSubstanceProps();
		return props.getMidColor();
	}

	/**
	 * Returns the "ultra dark" color.
	 *
	 * @return The color.
	 */
	public static Color getUltraDarkColor() {
		loadSubstanceProps();
		return props.getUltraDarkColor();
	}

	/**
	 * Returns the "ultra light" color.
	 *
	 * @return The color.
	 */
	public static Color getUltraLightColor() {
		loadSubstanceProps();
		return props.getUltraLightColor();
	}

	/**
	 * Returns whether a given LookAndFeel is a Substance LookAndFeel.
	 *
	 * @param laf The LookAndFeel.  If this is {@code null}, {@code false} is
	 *        returned.
	 * @return Whether it is a Substance LookAndFeel.
	 * @see #isASubstanceLookAndFeel(LookAndFeel)
	 * @see #isSubstanceInstalled()
	 */
	public static boolean isASubstanceLookAndFeel(LookAndFeel laf) {
		return laf != null && isASubstanceLookAndFeel(laf.getClass().getName());
	}

	/**
	 * Returns whether a given LookAndFeel is a Substance LookAndFeel.
	 *
	 * @param laf The class name of the LookAndFeel.  If this is {@code null},
	 *        {@code null} is returned.
	 * @return Whether it is a Substance LookAndFeel.
	 * @see #isASubstanceLookAndFeel(String)
	 * @see #isSubstanceInstalled()
	 */
	public static boolean isASubstanceLookAndFeel(String laf) {
		return laf != null && laf.contains(".Substance");
	}

	/**
	 * Returns whether the currently installed LookAndFeel is Substance.
	 *
	 * @return Whether the currently installed LookAndFeel is Substance.
	 */
	public static boolean isSubstanceInstalled() {
		return isASubstanceLookAndFeel(UIManager.getLookAndFeel());
	}

	private static void loadSubstanceProps() {

		if (props == null) {

			if (isSubstanceInstalled()) {

				// Use reflection to instantiation the class to avoid a hard runtime dependency
				try {
					props = (SubstanceProps)Class.forName(SUBSTANCE_PROPS_IMPL_CLASS).getDeclaredConstructor().
						newInstance();
				} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
						IllegalAccessException | InvocationTargetException ignored) {
					// Do nothing (comment for Sonar)
				}
			}

			if (props == null) {
				props = new DummySubstanceProps();
			}
		}
	}

	/**
	 * Sets the animation speed to use.
	 *
	 * @param millis The animation speed, in milliseconds.
	 * @see #getAnimationSpeed()
	 */
	public static void setAnimationSpeed(long millis) {
		loadSubstanceProps();
		props.setAnimationSpeed(millis);
	}
}
