/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.awt.*;
import java.util.Properties;

/**
 * Loader implementation for colors.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ColorLoader implements TypeLoader<Color> {


	static String colorToString(Color color) {

		if (color == null) {
			return "$ff000000";
		}

		// Shove argb value into a long, using it directly (or even just
		// casting it to long) make it interpreted as -1.
		long argb = color.getRGB() & 0xffffffffL;
		return "$" + Long.toHexString(argb+0x100000000L).substring(1);
	}


	@Override
	public Color load(String name, String value, Properties props) {
		return stringToColor(value);
	}


	static Color stringToColor(String value) {

		Color color = null;

		if (value.length() == 9 && value.charAt(0) == '$') {
			long temp = Long.parseLong(value.substring(1), 16);
			int rgba = (int)temp;
			//System.out.println("... " + temp + " > " + rgba);
			color = new Color(rgba, true);
		}

		return color;
	}


	@Override
	public String save(String name, Object value, Properties props) {
		return colorToString((Color)value);
	}
}
