/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.icons;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;


/**
 * A base class for icon groups.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class AbstractIconGroup implements IconGroup {

	protected String path;
	protected boolean separateLargeIcons;
	protected String largeIconSubDir;
	protected String extension;
	protected String name;
	protected String jarFile;

	private Map<String, ImageIcon> cache;

	private static final String DEFAULT_EXTENSION	= "gif";


	/**
	 * Creates an icon set without "large versions" of the icons.
	 *
	 * @param name The name of the icon group.
	 * @param path The directory containing the icon group.
	 */
	AbstractIconGroup(String name, String path) {
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
	AbstractIconGroup(String name, String path, String largeIconSubDir) {
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
	AbstractIconGroup(String name, String path, String largeIconSubDir,
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
	AbstractIconGroup(String name, String path, String largeIconSubDir,
                             String extension, String jar) {
		this.name = name;
		this.path = path;
		if (path!=null && path.length()>0 && !path.endsWith("/")) {
			this.path += "/";
		}
		this.separateLargeIcons = largeIconSubDir != null;
		this.largeIconSubDir = largeIconSubDir;
		this.extension = extension!=null ? extension : DEFAULT_EXTENSION;
		this.jarFile = jar;
		cache = new HashMap<>();
	}


	/**
	 * Returns whether two icon groups are equal.
	 *
	 * @param o2 The object to check against.
	 * @return Whether <code>o2</code> represents the same icons as this icon
	 *         group.
	 */
	@Override
	public boolean equals(Object o2) {
		if (o2 instanceof AbstractIconGroup) {
			AbstractIconGroup ig2 = (AbstractIconGroup)o2;
			if (ig2.getName().equals(getName()) &&
					separateLargeIcons==ig2.hasSeparateLargeIcons()) {
				if (separateLargeIcons) {
					if (!largeIconSubDir.equals(ig2.largeIconSubDir)) {
						return false;
					}
				}
				return path.equals(ig2.path);
			}
			// If we got here, separateLargeIcons values weren't equal.
		}
		return false;
	}


	@Override
	public Icon getIcon(String name) {

		Icon icon = getIconAndCache(path + name + "." + extension);

		// JDK 6.0 b74 returns icons with width/height==-1 in certain error
		// cases (new ImageIcon(url) where url is not resolved?).  We'll
		// just return null in this case as Swing AbstractButtons throw
		// exceptions when expected to paint an icon with width or height
		// is less than 1.
		if (icon!=null && (icon.getIconWidth()<1 || icon.getIconHeight()<1)) {
			icon = null;
		}
		return icon;
	}


	/**
	 * Fetches an icon from the cache, loading it if necessary.
	 *
	 * @param iconFullPath The icon to fetch, loading it into the cache
	 *        if necessary.
	 * @return The icon, or {@code null} if it does not exist.
	 */
	private ImageIcon getIconAndCache(String iconFullPath) {
		return cache.computeIfAbsent(iconFullPath, this::getIconImpl);
	}


	@Override
	public Image getImage(String name) {

		ImageIcon icon = getIconAndCache(path + name + "." + extension);

		// JDK 6.0 b74 returns icons with width/height==-1 in certain error
		// cases (new ImageIcon(url) where url is not resolved?).  We'll
		// just return null in this case as Swing AbstractButtons throw
		// exceptions when expected to paint an icon with width or height
		// is less than 1.
		if (icon == null || icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
			return null;
		}
		return icon.getImage();
	}


	/**
	 * Does the dirty work of loading an image.<p>
	 *
	 * This method is protected so applications can provide other
	 * implementations, for example, adding the ability to load SVG
	 * icons.
	 *
	 * @param iconFullPath The full path to the icon, either on the local
	 *        file system or in the Jar file, if this icon group represents
	 *        icons in a Jar file.
	 * @return The icon.  This method should return {@code null} if an error
	 *         occurs.
	 */
	protected abstract ImageIcon getIconImpl(String iconFullPath);


	@Override
	public Icon getLargeIcon(String name) {

		Icon icon = getIconAndCache(path + largeIconSubDir + "/" +
			name + "." + extension);

		// JDK 6.0 b74 returns icons with width/height==-1 in certain error
		// cases (new ImageIcon(url) where url is not resolved?).  We'll
		// just return null in this case as Swing AbstractButtons throw
		// exceptions when expected to paint an icon with width or height
		// is less than 1.
		if (icon!=null && (icon.getIconWidth()<1 || icon.getIconHeight()<1)) {
			icon = null;
		}

		return icon;
	}


	@Override
	public Image getLargeImage(String name) {

		ImageIcon icon = getIconAndCache(path + largeIconSubDir + "/" +
			name + "." + extension);

		// JDK 6.0 b74 returns icons with width/height==-1 in certain error
		// cases (new ImageIcon(url) where url is not resolved?).  We'll
		// just return null in this case as Swing AbstractButtons throw
		// exceptions when expected to paint an icon with width or height
		// is less than 1.
		if (icon == null || icon.getIconWidth() < 1 || icon.getIconHeight() < 1) {
			return null;
		}
		return icon.getImage();
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public boolean hasSeparateLargeIcons() {
		return separateLargeIcons;
	}


	@Override
	public int hashCode() {
		return getName().hashCode();
	}


}
