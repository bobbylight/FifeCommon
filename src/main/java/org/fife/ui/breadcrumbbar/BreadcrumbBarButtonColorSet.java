/*
 * 01/10/2013
 *
 * BreadcrumbBarButtonColorSet.java - Colors used while rendering breadcrumb
 * bar buttons.
 * Copyright (C) 2013 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.breadcrumbbar;

import org.fife.util.DarculaUtil;

import java.awt.Color;
import javax.swing.AbstractButton;
import javax.swing.JTextField;
import javax.swing.UIManager;


/**
 * The set of colors used when rendering breadcrumb bar buttons in various
 * states.
 *
 * @author Robert Futrell
 * @version 1.0
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
class BreadcrumbBarButtonColorSet {

	public Color defaultFG;
	public Color rolloverC1;
	public Color rolloverC2;
	public Color rolloverFG;
	public Color pressedC1;
	public Color pressedC2;
	public Color pressedFG;
	public Color nonArrowArrowArmedC1;
	public Color nonArrowArrowArmedC2;
	public Color borderColor;

	private static final float BRIGHTNESS_FACTOR = 0.15f;


	private static Color brighter(Color c) {

		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();

		/*
		 * From 2D group: 1. black.brighter() should return grey 2. applying
		 * brighter to blue will always return blue, brighter 3. non-pure color
		 * (nonzero rgb) will eventually return white
		 */
		int i = (int) (1.0 / BRIGHTNESS_FACTOR);
		if (r == 0 && g == 0 && b == 0) {
			return new Color(i, i, i);
		}
		if (r > 0 && r < i)
			r = i;
		if (g > 0 && g < i)
			g = i;
		if (b > 0 && b < i)
			b = i;

		return new Color(
				Math.min((int)(r/(1f-BRIGHTNESS_FACTOR)), 255),
				Math.min((int)(g/(1f-BRIGHTNESS_FACTOR)), 255),
				Math.min((int)(b/(1f-BRIGHTNESS_FACTOR)), 255));
	}


	public static Color darker(Color c) {
		final float factor = 1f - BRIGHTNESS_FACTOR;
		float r = c.getRed() * factor;
		float g = c.getGreen() * factor;
		float b = c.getBlue() * factor;
		return new Color((int)r, (int)g, (int)b);
	}


	public void initialize(AbstractButton b) {

		JTextField textField = new JTextField();
		Color highlight = textField.getSelectionColor();
		defaultFG = textField.getForeground();

		if (DarculaUtil.isDarculaInstalled()) {
			rolloverC1 = rolloverC2 = highlight;
			pressedC1 = pressedC2 = darker(highlight);
			rolloverFG = pressedFG = textField.getSelectedTextColor();
			nonArrowArrowArmedC1 = rolloverC1;
			nonArrowArrowArmedC2 = rolloverC2;
			borderColor = darker(pressedC2);
		}

		else {

			rolloverC1 = brighter(highlight);
			rolloverC2 = highlight;
			pressedC1 = highlight;
			pressedC2 = darker(pressedC1);
			rolloverFG = pressedFG = textField.getSelectedTextColor();
			nonArrowArrowArmedC1 = rolloverC1;
			nonArrowArrowArmedC2 = rolloverC2;
			borderColor = darker(pressedC2);

			if (needsExtraBrightening(textField)) {
				rolloverC1 = brighter(rolloverC1);
				rolloverC2 = brighter(rolloverC2);
				pressedC1 = brighter(pressedC1);
				pressedC2 = brighter(pressedC2);
				nonArrowArrowArmedC1 = brighter(nonArrowArrowArmedC1);
				nonArrowArrowArmedC2 = brighter(nonArrowArrowArmedC2);
			}

		}

	}


	/**
	 * Returns whether the colors derived for breadcrumb bar buttons need to
	 * be brightened a little more for the current Look and Feel.  LaFs with
	 * very dark selection colors often meet this requirement.
	 *
	 * @param textField A text field, to be used for checking colors of the
	 *        Look and Feel.
	 * @return Whether the button colors will need extra brightening.
	 */
	private static boolean needsExtraBrightening(JTextField textField) {
		String laf = UIManager.getLookAndFeel().getClass().getName();
		return laf.endsWith(".WindowsLookAndFeel") &&
					Color.white.equals(textField.getBackground()) ||
				(laf.endsWith(".NimbusLookAndFeel"));
	}


}
