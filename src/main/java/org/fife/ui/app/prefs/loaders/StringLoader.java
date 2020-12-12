/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.util.Properties;

/**
 * Loader implementation for strings.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class StringLoader implements TypeLoader<String> {

	@Override
	public String load(String name, String value, Properties props) {
		return value;
	}

	@Override
	public String save(String name, Object value, Properties props) {
		String str = (String)value;
		return str == null || str.isEmpty() ? null : str;
	}
}
