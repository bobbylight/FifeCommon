/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.util.Properties;

/**
 * Loader implementation for floats.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FloatLoader implements TypeLoader<Float> {

	@Override
	public Float load(String name, String value, Properties props) {

		Float i = null;

		try {
			i = Float.valueOf(value);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}

		return i;
	}

	@Override
	public String save(String name, Object value, Properties props) {
		return value != null ? value.toString() : null;
	}
}
