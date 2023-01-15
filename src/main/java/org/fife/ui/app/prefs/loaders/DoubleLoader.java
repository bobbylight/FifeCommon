/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.util.Properties;

/**
 * Loader implementation for doubles.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class DoubleLoader implements TypeLoader<Double> {

	@Override
	public Double load(String name, String value, Properties props) {

		Double i = null;

		try {
			i = Double.valueOf(value);
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
