/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.icons;

import org.fife.ui.ImageTranscodingUtil;
import org.fife.ui.app.AbstractGUIApplication;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * An icon group consisting of SVG icons.
 */
public class SvgIconGroup extends AbstractIconGroup {

	private String jarFile;


	public SvgIconGroup(AbstractGUIApplication<?> owner, String name, String jarFile) {
		super(name, "", null, "svg", jarFile);
		this.jarFile = owner.getInstallLocation() + '/' + jarFile;
	}


	@Override
	protected Icon getIconImpl(String iconFullPath) {
		try (InputStream svg = new URL("jar:file:///" +
			jarFile + "!/" + iconFullPath).openStream()) {
			BufferedImage image = ImageTranscodingUtil.rasterize(
				iconFullPath, svg, 16, 16);
			return new ImageIcon(image);
		} catch (IOException ioe) {
			// If an icon doesn't exist in this group, just return no icon.
			return null;
		}
	}


}
