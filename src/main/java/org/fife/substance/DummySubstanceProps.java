/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.substance;

import java.awt.*;

/**
 * The implementation of {@code SubstanceProps} used when Substance is not found on the classpath.
 */
public class DummySubstanceProps implements SubstanceProps {

	@Override
	public long getAnimationSpeed() {
		return -1;
	}

	@Override
	public Color getDarkColor() {
		return null;
	}

	@Override
	public Color getExtraLightColor() {
		return null;
	}

	@Override
	public Color getLightColor() {
		return null;
	}

	@Override
	public Color getMidColor() {
		return null;
	}

	@Override
	public Color getUltraDarkColor() {
		return null;
	}

	@Override
	public Color getUltraLightColor() {
		return null;
	}

	@Override
	public void setAnimationSpeed(long millis) {
	}
}
