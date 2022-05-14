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
 * Loader implementation for points.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class PointLoader implements TypeLoader<Point> {

	@Override
	public Point load(String name, String value, Properties props) {

		String[] tokens = value.split(",");

		if (tokens.length == 2) {
			try {
				int x = Integer.parseInt(tokens[0]);
				int y = Integer.parseInt(tokens[1]);
				return new Point(x, y);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public String save(String name, Object value, Properties props) {
		Point point = (Point)value;
		return point != null ? (point.x + "," + point.y) : null;
	}
}
