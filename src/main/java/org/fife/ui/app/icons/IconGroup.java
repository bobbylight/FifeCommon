/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.icons;

import javax.swing.*;
import java.awt.*;


/**
 * This interface encapsulates the location, properties, etc. of an icon set used
 * throughout an application.  If the location of the icon
 * group is invalid in any way, any attempt to retrieve icons from an icon
 * group will return {@code null}.<p>
 *
 * An icon group can logically refer to any set of icons.  You can fetch them
 * as either icons or as raw images.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see RasterImageIconGroup
 * @see SvgIconGroup
 */
public interface IconGroup {


	/**
	 * Returns the icon from this icon group with the specified name.
	 * The icon returned will be a "standard" size for the current desktop
	 * environment (e.g. 16x16 for most desktops, or larger for 4k and Retina
	 * displays).
	 *
	 * @param name The name of the icon.  For example, if you want the icon
	 * specified in <code>new.gif</code>, this value should be
	 * <code>new</code>.
	 * @return The icon, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getLargeIcon(String)
	 * @see #getIcon(String, int, int)
	 */
	Icon getIcon(String name);


	/**
	 * Returns the icon from this icon group with the specified name.
	 *
	 * @param name The name of the icon.  For example, if you want the icon
	 * specified in <code>new.gif</code>, this value should be
	 * <code>new</code>.
	 * @param w The icon width.
	 * @param h The icon height.
	 * @return The icon, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getIcon(String)
	 * @see #getLargeIcon(String)
	 */
	Icon getIcon(String name, int w, int h);


	/**
	 * Returns the image for an icon from this icon group with the specified name.
	 * The image returned will be a "standard" size for the current desktop
	 * environment (e.g. 16x16 for most desktops, or larger for 4k and Retina
	 * displays).
	 *
	 * @param name The name of the icon.  For example, if you want the image
	 * specified in <code>new.gif</code>, this value should be
	 * <code>new</code>.
	 * @return The image, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getImage(String, int, int)
	 * @see #getIcon(String)
	 * @see #getLargeIcon(String)
	 */
	Image getImage(String name);


	/**
	 * Returns the image for an icon from this icon group with the specified name.
	 *
	 * @param name The name of the icon.  For example, if you want the image
	 * specified in <code>new.gif</code>, this value should be
	 * <code>new</code>.
	 * @return The image, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @param w The icon width.
	 * @param h The icon height.
	 * @see #getImage(String)
	 * @see #getIcon(String)
	 * @see #getLargeIcon(String)
	 */
	Image getImage(String name, int w, int h);


	/**
	 * Returns the large icon from this icon group with the specified name.
	 * If this icon group does not have large icons, <code>null</code> is
	 * returned.
	 *
	 * @param name The name of the icon.  For example, if you want the icon
	 *        specified in <code>new.gif</code>, this value should be
	 *        <code>new</code>.
	 * @return The icon, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getIcon(String)
	 */
	Icon getLargeIcon(String name);


	/**
	 * Returns the large image from this icon group with the specified name.
	 * If this icon group does not have large images, <code>null</code> is
	 * returned.
	 *
	 * @param name The name of the icon.  For example, if you want the image
	 *        specified in <code>new.gif</code>, this value should be
	 *        <code>new</code>.
	 * @return The image, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getImage(String)
	 * @see #getLargeIcon(String)
	 */
	Image getLargeImage(String name);


	/**
	 * Returns the name of this icon group.
	 *
	 * @return This icon group's name.
	 */
	String getName();


	/**
	 * Returns whether a separate directory for the large icons exists.
	 *
	 * @return Whether a directory containing "large versions" of the icons
	 *         exists.
	 * @see #getLargeIcon(String)
	 */
	boolean hasSeparateLargeIcons();


}
