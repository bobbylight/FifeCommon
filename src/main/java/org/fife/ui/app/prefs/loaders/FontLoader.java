/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.awt.*;
import java.util.Properties;

/**
 * Loader implementation for fonts.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FontLoader implements TypeLoader<Font> {

	@Override
	public Font load(String name, String value, Properties props) {

		String[] tokens = value.split(",");

		if (tokens.length == 3) {
			String fontName = tokens[0];
			try {
				int style = Integer.parseInt(tokens[1]);
				int size = Integer.parseInt(tokens[2]);
				return new Font(fontName, style, size);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public String save(String name, Object value, Properties props) {
		if (value != null) {
			Font font = (Font)value;
			return font.getName() + "," + font.getStyle() + "," + font.getSize();
		}
		return null;
	}
}
