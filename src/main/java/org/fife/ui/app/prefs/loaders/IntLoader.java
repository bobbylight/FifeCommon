/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.util.Properties;

/**
 * Loader implementation for ints.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class IntLoader implements TypeLoader<Integer> {

	static String intToString(Object value) {
		return value != null ? value.toString() : null;
	}

	@Override
	public Integer load(String name, String value, Properties props) {
		return stringToInt(value);
	}

	@Override
	public String save(String name, Object value, Properties props) {
		return intToString(value);
	}

	static Integer stringToInt(String value) {

		Integer i = null;

		try {
			i = Integer.valueOf(value);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}

		return i;
	}
}
