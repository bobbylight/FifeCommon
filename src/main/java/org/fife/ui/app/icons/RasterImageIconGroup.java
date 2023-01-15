/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.icons;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;


/**
 * An icon group for PNG, GIF or JPG files.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see SvgIconGroup
 */
public class RasterImageIconGroup extends AbstractIconGroup {

	private static final String DEFAULT_EXTENSION	= "gif";


	/**
	 * Creates an icon set without "large versions" of the icons.
	 *
	 * @param name The name of the icon group.
	 * @param path The directory containing the icon group.
	 * @param nativePath The directory containing the native icons, or
	 *        {@code null} if none.
	 */
	public RasterImageIconGroup(String name, String path, String nativePath) {
		this(name, path, nativePath, DEFAULT_EXTENSION);
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of the icon group.
	 * @param path The directory containing the icon group.
	 * @param nativePath The directory containing the native icons, or
	 *        {@code null} if none.
	 * @param extension The extension of the icons (one of <code>gif</code>,
	 *        <code>jpg</code>, or <code>png</code>).
	 */
	public RasterImageIconGroup(String name, String path, String nativePath,
								String extension) {
		super(name, path, nativePath, extension);
	}


	@Override
	protected ImageIcon getIconImpl(String iconFullPath, int w, int h) {

		try {

			// First see if it's on our classpath
			URL url = getClass().getClassLoader().
									getResource(iconFullPath);
			if (url!=null) {
				return new ImageIcon(url);
			}

			// If not found, always try with PNG (hack)
			if (!iconFullPath.toLowerCase(Locale.ROOT).endsWith(".png")) {
				String pngFullPath = iconFullPath.
					replaceAll("\\." + extension + "$", ".png");
				url = getClass().getClassLoader().getResource(pngFullPath);
				if (url!=null) {
					return new ImageIcon(url);
				}
			}

			// If not, see if it's a plain file on disk
			File file = new File(iconFullPath);
			if (file.isFile()) {
				BufferedImage image = ImageIO.read(file);
				return image != null ? new ImageIcon(image) : null;
			}

			return null;

		} catch (IOException ioe) {
			return null;
		}
	}


}
