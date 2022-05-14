/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.substance;

import org.pushingpixels.substance.api.SubstanceCortex;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.SubstanceSlices;
import org.pushingpixels.substance.api.colorscheme.SubstanceColorScheme;
import org.pushingpixels.substance.internal.AnimationConfigurationManager;

import java.awt.*;

/**
 * The implementation of {@code SubstanceProps} used when Substance is found on the classpath.
 */
public class SubstancePropsImpl implements SubstanceProps {

	@Override
	public long getAnimationSpeed() {
		return AnimationConfigurationManager.getInstance().getTimelineDuration();
	}

	private SubstanceColorScheme getColorScheme() {
		SubstanceSkin skin = SubstanceCortex.GlobalScope.getCurrentSkin();
		return skin.getActiveColorScheme(
			SubstanceSlices.DecorationAreaType.NONE);
	}

	@Override
	public Color getDarkColor() {
		return getColorScheme().getDarkColor();
	}

	@Override
	public Color getExtraLightColor() {
		return getColorScheme().getExtraLightColor();
	}

	@Override
	public Color getLightColor() {
		return getColorScheme().getLightColor();
	}

	@Override
	public Color getMidColor() {
		return getColorScheme().getMidColor();
	}

	@Override
	public Color getUltraDarkColor() {
		return getColorScheme().getUltraDarkColor();
	}

	@Override
	public Color getUltraLightColor() {
		return getColorScheme().getUltraLightColor();
	}

	@Override
	public void setAnimationSpeed(long millis) {
		AnimationConfigurationManager.getInstance().setTimelineDuration(millis);
	}
}
