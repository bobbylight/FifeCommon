/*
 * http://fifesoft.com/rtext
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
import java.security.AccessControlException;


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
	 */
	public RasterImageIconGroup(String name, String path) {
		this(name, path, null);
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of the icon group.
	 * @param path The directory containing the icon group.
	 * @param largeIconSubDir The subdirectory containing "large versions" of
	 *        the icons.  If no subdirectory exists, pass in <code>null</code>.
	 */
	public RasterImageIconGroup(String name, String path, String largeIconSubDir) {
		this(name, path, largeIconSubDir, DEFAULT_EXTENSION);
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of the icon group.
	 * @param path The directory containing the icon group.
	 * @param largeIconSubDir The subdirectory containing "large versions" of
	 *        the icons.  If no subdirectory exists, pass in <code>null</code>.
	 * @param extension The extension of the icons (one of <code>gif</code>,
	 *        <code>jpg</code>, or <code>png</code>).
	 */
	public RasterImageIconGroup(String name, String path, String largeIconSubDir,
								String extension) {
		this(name, path, largeIconSubDir, extension, null);
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of the icon group.
	 * @param path The directory containing the icon group.
	 * @param largeIconSubDir The subdirectory containing "large versions" of
	 *        the icons.  If no subdirectory exists, pass in <code>null</code>.
	 * @param extension The extension of the icons (one of <code>gif</code>,
	 *        <code>jpg</code>, or <code>png</code>).
	 * @param jar The Jar file containing the icons, or <code>null</code> if
	 *        the icons are on the local file system.  If a Jar is specified,
	 *        the value of <code>path</code> must be a path in the Jar file.
	 *        If this is not a valid Jar file, then no Jar file will be used,
	 *        meaning all icons returned from this icon group will be
	 *        <code>null</code>.
	 */
	public RasterImageIconGroup(String name, String path, String largeIconSubDir,
								String extension, String jar) {
		super(name, path, largeIconSubDir, extension, jar);
	}


	@Override
	protected ImageIcon getIconImpl(String iconFullPath, int w, int h) {
		try {
			if (jarFile==null) {
				// First see if it's on our classpath (e.g. an icon in
				// RText.jar, so we'd need to use the class loader).
				URL url = getClass().getClassLoader().
										getResource(iconFullPath);
				if (url!=null) {
					return new ImageIcon(url);
				}
				// If not, see if it's a plain file on disk.
				BufferedImage image = ImageIO.read(new File(iconFullPath));
				return image!=null ? new ImageIcon(image) : null;
			}
			else { // If it's in a Jar, create a URL and grab it.
				URL url = new URL("jar:file:///" +
									jarFile + "!/" + iconFullPath);
				ImageIcon icon = new ImageIcon(url);
				// URLs that are valid but simply don't exist can create -1x-1 ImageIcons
				return icon.getIconWidth() == -1 ? null : icon;
			}
		} catch (AccessControlException | IOException ace) {
			return null;
		}
	}


}
