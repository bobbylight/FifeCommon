/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license keystroke for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import javax.swing.KeyStroke;
import java.util.Properties;

/**
 * Loader implementation for keystrokes.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class KeyStrokeLoader implements TypeLoader<KeyStroke> {

	@Override
	public KeyStroke load(String name, String value, Properties props) {
		// Empty value => still use default.  Will be null if formatted
		// incorrectly
		return value.isEmpty() ? null : KeyStroke.getKeyStroke(value);
	}

	@Override
	public String save(String name, Object value, Properties props) {
		return value != null ? value.toString() : null;
	}
}
