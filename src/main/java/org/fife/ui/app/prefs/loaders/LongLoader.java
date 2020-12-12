/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.util.Properties;

/**
 * Loader implementation for longs.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class LongLoader implements TypeLoader<Long> {

	@Override
	public Long load(String name, String value, Properties props) {

		Long i = null;

		try {
			i = Long.valueOf(value);
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
