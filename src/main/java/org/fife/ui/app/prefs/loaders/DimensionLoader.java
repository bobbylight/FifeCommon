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
 * Loader implementation for dimensions.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class DimensionLoader implements TypeLoader<Dimension> {

	@Override
	public Dimension load(String name, String value, Properties props) {

		String[] tokens = value.split(",");

		if (tokens.length == 2) {
			try {
				int w = Integer.parseInt(tokens[0]);
				int h = Integer.parseInt(tokens[1]);
				return new Dimension(w, h);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public String save(String name, Object value, Properties props) {
		Dimension d = (Dimension)value;
		return d != null ?  (d.width + "," + d.height) : null;
	}
}
