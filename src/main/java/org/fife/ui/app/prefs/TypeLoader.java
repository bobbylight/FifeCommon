/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs;


import java.util.Properties;

/**
 * An intermediary that can read and write string representations of
 * objects.  An instance of this class is registered with a {@code Prefs}
 * instance for each type of object that can be saved.  This provides
 * applications with a mechanism to save custom, complex POJO's.
 *
 * @param <T> The type of object read and written.
 */
public interface TypeLoader<T> {

	/**
	 * Parses a value out of a saved string representation.
	 *
	 * @param name The name of the field whose value is being loaded.
	 * @param value The saved string representation of the value.
	 * @param props The properties from which {@code value} was
	 *         retrieved.  Most implementations can ignore this parameter;
	 *         it's here for the rare scenario where a complex type is
	 *         stored across more than one property in the properties file.
	 * @return The value.
	 */
	T load(String name, String value, Properties props);


	/**
	 * Creates a string representation of a value.
	 *
	 * @param name The name of the field whose value is being saved.
	 * @param value The value to create a string representation of.
	 * @param props The properties into which {@code value} will be
	 *         stored.  Most implementations can ignore this parameter;
	 *         it's here for the rare scenario where a complex type is
	 *         stored across more than one property in the properties file.
	 * @return The value.
	 */
	String save(String name, Object value, Properties props);
}
