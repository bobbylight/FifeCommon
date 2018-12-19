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

	Color getDarkColor();

	Color getExtraLightColor();

	Color getLightColor();

	Color getMidColor();

	Color getUltraDarkColor();

	Color getUltraLightColor();

	/**
	 * Configures the length of GUI animations, in milliseconds.
	 *
	 * @param millis The amount of time animations should take.
	 * @see #getAnimationSpeed()
	 */
	void setAnimationSpeed(long millis);
}
