/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.icons;

import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.app.AbstractGUIApplication;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * An icon group consisting of SVG icons.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see RasterImageIconGroup
 */
public class SvgIconGroup extends AbstractIconGroup {


	public SvgIconGroup(AbstractGUIApplication<?> owner, String name, String path,
						String nativePath) {
		super(name, path, nativePath, "svg");
	}


	@Override
	protected ImageIcon getIconImpl(String iconFullPath, int w, int h) {

		try {

			// First see if it's on our classpath.
			// If not, see if it's a plain file on disk
			InputStream in = getClass().getClassLoader().
				getResourceAsStream(iconFullPath);
			if (in == null) {
				File file = new File(iconFullPath);
				if (file.isFile()) {
					in = new FileInputStream(file);
				}
			}

			if (in != null) {
				BufferedImage image = ImageTranscodingUtil.rasterize(
					iconFullPath, in, w, h);
				return new ImageIcon(image);
			}
			return null;

		} catch (IOException ioe) {
			return null;
		}
	}


}
