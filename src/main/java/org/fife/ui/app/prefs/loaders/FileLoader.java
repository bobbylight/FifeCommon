/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.io.File;
import java.util.Properties;

/**
 * Loader implementation for files.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileLoader implements TypeLoader<File> {

	@Override
	public File load(String name, String value, Properties props) {
		// Empty value => still use default
		return value.isEmpty() ? null : new File(value);
	}

	@Override
	public String save(String name, Object value, Properties props) {
		return value != null ? ((File)value).getAbsolutePath() : null;
	}
}
