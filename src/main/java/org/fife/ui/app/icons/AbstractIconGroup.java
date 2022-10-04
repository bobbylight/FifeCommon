/*
 * https://fifesoft.com/rtext
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
	protected String nativePath;
	protected String extension;
	protected String name;

	private Map<String, ImageIcon> cache;

	private static final String DEFAULT_EXTENSION	= "gif";

	// TODO: Determine default sizes based on screen resolution
	private static final int DEFAULT_IMAGE_SIZE = 16;


	/**
	 * Creates an icon set without "large versions" of the icons.
	 *
	 * @param name The name of the icon group.
	 * @param path The root of the icon resources, or the directory
	 *        containing the icon files if they're on the local file system
	 *        instead of in the application classpath.
	 * @param nativePath The root of the native icon resources, or the directory
	 *        containing the icon files if they're on the local file system
	 *        instead of in the application classpath.
	 */
	AbstractIconGroup(String name, String path, String nativePath) {
		this(name, path, nativePath, null);
	}


	AbstractIconGroup(String name, String path, String nativePath, String extension) {
		this.name = name;
		this.path = path;
		this.nativePath = nativePath;
		if (path!=null && path.length()>0 && !path.endsWith("/")) {
			this.path += "/";
		}
		if (nativePath!=null && nativePath.length()>0 && !nativePath.endsWith("/")) {
			this.nativePath += "/";
		}
		this.extension = extension!=null ? extension : DEFAULT_EXTENSION;
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
		if (o2 instanceof AbstractIconGroup ig2) {
			if (ig2.getName().equals(getName())) {
				return path.equals(ig2.path);
			}
		}
		return false;
	}


	@Override
	public Icon getIcon(String name) {
		return getIcon(name, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE);
	}


	@Override
	public Icon getIcon(String name, int w, int h) {
		return getIconAndCache(path + name + "." + extension, w, h);
	}


	/**
	 * Fetches an icon from the cache, loading it if necessary.
	 *
	 * @param iconFullPath The icon to fetch, loading it into the cache
	 *        if necessary.
	 * @param w The icon width.
	 * @param h The icon height.
	 * @return The icon, or {@code null} if it does not exist.
	 */
	private ImageIcon getIconAndCache(String iconFullPath, int w, int h) {
		String key = iconFullPath + "-" + w + "-" + h;
		ImageIcon icon =  cache.computeIfAbsent(key, k -> getIconImpl(iconFullPath, w, h));

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
	 * Does the dirty work of loading an image.<p>
	 *
	 * This method is protected so applications can provide other
	 * implementations, for example, adding the ability to load SVG
	 * icons.
	 *
	 * @param iconFullPath The full path of the icon resource.
	 * @param w The icon width.
	 * @param h The icon height.
	 * @return The icon.  This method should return {@code null} if an error
	 *         occurs.
	 */
	protected abstract ImageIcon getIconImpl(String iconFullPath, int w, int h);


	@Override
	public Image getImage(String name) {
		return getImage(name, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE);
	}


	@Override
	public Image getImage(String name, int w, int h) {
		ImageIcon icon = getIconAndCache(path + name + "." + extension, w, h);
		return icon != null ? icon.getImage() : null;
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public Icon getNativeIcon(String name) {
		return getNativeIcon(name, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE);
	}


	@Override
	public Icon getNativeIcon(String name, int w, int h) {
		// Fall back onto the regular icons if native isn't defined
		String path = this.nativePath != null ? this.nativePath : this.path;
		return getIconAndCache(path + name + "." + extension, w, h);
	}


	@Override
	public Image getNativeImage(String name) {
		return getNativeImage(name, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE);
	}


	@Override
	public Image getNativeImage(String name, int w, int h) {
		// Fall back onto the regular icons if native isn't defined
		String path = this.nativePath != null ? this.nativePath : this.path;
		ImageIcon icon = getIconAndCache(path + name + "." + extension, w, h);
		return icon != null ? icon.getImage() : null;
	}


	@Override
	public int hashCode() {
		return getName().hashCode();
	}


}
