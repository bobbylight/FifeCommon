/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.util.Properties;

/**
 * Loader implementation for booleans.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class BooleanLoader implements TypeLoader<Boolean> {

	static String booleanToString(Object value) {
		return value != null ? value.toString() : null;
	}

	@Override
	public Boolean load(String name, String value, Properties props) {
		return stringToBoolean(value);
	}

	@Override
	public String save(String name, Object value, Properties props) {
		return booleanToString(value);
	}

	static Boolean stringToBoolean(String value) {
		return Boolean.valueOf(value);
	}
}
