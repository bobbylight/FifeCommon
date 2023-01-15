/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.util.Properties;

/**
 * Loader implementation for characters.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class CharacterLoader implements TypeLoader<Character> {

	@Override
	public Character load(String name, String value, Properties props) {
		return value.isEmpty() ? null : value.charAt(0);
	}

	@Override
	public String save(String name, Object value, Properties props) {
		return value != null ? value.toString() : null;
	}
}
