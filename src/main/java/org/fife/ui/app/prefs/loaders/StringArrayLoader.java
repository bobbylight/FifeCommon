/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.util.Properties;

/**
 * Loader implementation for an array of strings.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class StringArrayLoader implements TypeLoader<String[]> {

	@Override
	public String[] load(String name, String value, Properties props) {

		String[] temp = null;

		int length = -1;
		try {
			length = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}

		if (length > -1) { // -1 => null array or NFE
			temp = new String[length];
			for (int j = 0; j < length; j++) {
				// Property will not be defined if the String
				// should be null, so everything works out
				temp[j] = props.getProperty(name + "." + j);
			}
		}

		return temp;
	}

	@Override
	public String save(String name, Object value, Properties props) {

		// Store length of array as "main" property, then each
		// item as an extra property of form "name.index".
		if (value == null) {
			return "-1";
		}

		String[] array = (String[])value;
		String strVal = Integer.toString(array.length);
		for (int j = 0; j < array.length; j++) {
			// No "name.N" property => String at that index
			// was null (can't put null values into Properties)
			if (array[j] != null) {
				props.setProperty(name + "." + j, array[j]);
			}
		}

		return strVal;
	}
}
