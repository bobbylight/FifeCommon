/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.util.Properties;

/**
 * Loader implementation for bytes.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ByteLoader implements TypeLoader<Byte> {

	@Override
	public Byte load(String name, String value, Properties props) {

		Byte i = null;

		try {
			i = Byte.valueOf(value);
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
