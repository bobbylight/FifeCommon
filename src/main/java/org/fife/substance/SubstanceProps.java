/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.substance;

import java.awt.*;

/**
 * An interface to the Substance Look and Feel.  This is an interface, designed to instantiate via reflection,
 * so consuming applications can avoid a runtime dependency on Substance if they don't want it.
 */
public interface SubstanceProps {

	/**
	 * Returns the length of time GUI animations take, in milliseconds.
	 *
	 * @return The length of time, in milliseconds.
	 * @see #setAnimationSpeed(long)
	 */
	long getAnimationSpeed();

	/**
	 * Returns the "dark" color.
	 *
	 * @return The color.
	 */
	Color getDarkColor();

	/**
	 * Returns the "extra light" color.
	 *
	 * @return The color.
	 */
	Color getExtraLightColor();

	/**
	 * Returns the "light" color.
	 *
	 * @return The color.
	 */
	Color getLightColor();

	/**
	 * Returns the "mid" color.
	 *
	 * @return The color.
	 */
	Color getMidColor();

	/**
	 * Returns the "ultra dark" color.
	 *
	 * @return The color.
	 */
	Color getUltraDarkColor();

	/**
	 * Returns the "ultra light" color.
	 *
	 * @return The color.
	 */
	Color getUltraLightColor();

	/**
	 * Configures the length of GUI animations, in milliseconds.
	 *
	 * @param millis The amount of time animations should take.
	 * @see #getAnimationSpeed()
	 */
	void setAnimationSpeed(long millis);
}
