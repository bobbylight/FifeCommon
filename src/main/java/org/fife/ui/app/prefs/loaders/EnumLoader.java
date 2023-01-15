/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.util.Properties;


/**
 * A type loader for enums. Provide a non-{@code null} default value to use when
 * saving and loading and this loader will store values by enum name.
 *
 * @param <T> The type of enum.
 */
public class EnumLoader<T extends Enum<T>> implements TypeLoader<T> {

	private final T defaultValue;

	public EnumLoader(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T load(String name, String value, Properties props) {
		T loadedValue = defaultValue;
		if (value != null) {
			try {
				loadedValue = T.valueOf((Class<T>)defaultValue.getClass(), value);
			} catch (IllegalArgumentException ignore) {
				// Do nothing, use the default
			}
		}
		return loadedValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String save(String name, Object value, Properties props) {
		return value != null ? ((T)value).name() : defaultValue.name();
	}
}
