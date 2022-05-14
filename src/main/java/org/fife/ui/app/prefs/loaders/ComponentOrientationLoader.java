/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs.loaders;

import org.fife.ui.app.prefs.TypeLoader;

import java.awt.*;
import java.util.Properties;

/**
 * Loader implementation for {@code ComponentOrientation}s.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ComponentOrientationLoader implements TypeLoader<ComponentOrientation> {

	private static final String LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
	private static final String RIGHT_TO_LEFT = "RIGHT_TO_LEFT";


	@Override
	public ComponentOrientation load(String name, String value, Properties props) {
		return RIGHT_TO_LEFT.equals(value) ? ComponentOrientation.RIGHT_TO_LEFT :
			ComponentOrientation.LEFT_TO_RIGHT;
	}


	@Override
	public String save(String name, Object value, Properties props) {
		return RIGHT_TO_LEFT.equals(value) ? RIGHT_TO_LEFT : LEFT_TO_RIGHT;
	}
}
