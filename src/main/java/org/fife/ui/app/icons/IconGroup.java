/*
 * https://fifesoft.com/rtext
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
	 *        specified in <code>new.gif</code>, this value should be
	 *        <code>new</code>.
	 * @return The icon, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getIcon(String, int, int)
	 * @see #getNativeIcon(String)
	 */
	Icon getIcon(String name);


	/**
	 * Returns the icon from this icon group with the specified name.
	 *
	 * @param name The name of the icon.  For example, if you want the icon
	 *        specified in <code>new.gif</code>, this value should be
	 *        <code>new</code>.
	 * @param w The icon width.
	 * @param h The icon height.
	 * @return The icon, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getIcon(String)
	 * @see #getNativeIcon(String, int, int)
	 */
	Icon getIcon(String name, int w, int h);


	/**
	 * Returns the image for an icon from this icon group with the specified name.
	 * The image returned will be a "standard" size for the current desktop
	 * environment (e.g. 16x16 for most desktops, or larger for 4k and Retina
	 * displays).
	 *
	 * @param name The name of the icon.  For example, if you want the image
	 *        specified in <code>new.gif</code>, this value should be
	 *        <code>new</code>.
	 * @return The image, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getImage(String, int, int)
	 * @see #getIcon(String)
	 * @see #getNativeImage(String)
	 */
	Image getImage(String name);


	/**
	 * Returns the image for an icon from this icon group with the specified name.
	 *
	 * @param name The name of the icon.  For example, if you want the image
	 *        specified in <code>new.gif</code>, this value should be
	 *        <code>new</code>.
	 * @param w The icon width.
	 * @param h The icon height.
	 * @return The image, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getImage(String)
	 * @see #getIcon(String)
	 * @see #getNativeImage(String, int, int)
	 */
	Image getImage(String name, int w, int h);


	/**
	 * Returns the name of this icon group.
	 *
	 * @return This icon group's name.
	 */
	String getName();


	/**
	 * Returns a version of an icon to use in "native" UI components.
	 * This is primarily used to determine icons in the macOS native
	 * menu bar, since it may not match the application's theme.<p>
	 *
	 * Note this may be the same icon as that returned by
	 * {@code getIcon(String)} if this icon group doesn't have icons
	 * specifically for native components.
	 *
	 * @param name The icon to load.
	 * @return The icon, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getIcon(String)
	 */
	Icon getNativeIcon(String name);


	/**
	 * Returns a version of an icon to use in "native" UI components.
	 * This is primarily used to determine icons in the macOS native
	 * menu bar, since it may not match the application's theme.<p>
	 *
	 * Note this may be the same icon as that returned by
	 * {@code getIcon(String, int, int)} if this icon group doesn't
	 * have icons specifically for native components.
	 *
	 * @param name The icon to load.
	 * @param w The width of the icon to load.
	 * @param h The height of the icon to load.
	 * @return The icon, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getIcon(String, int, int)
	 */
	Icon getNativeIcon(String name, int w, int h);




	/**
	 * Returns a version of an image to use in "native" UI components.
	 * This is primarily used to determine icons in the macOS native
	 * menu bar, since it may not match the application's theme.<p>
	 *
	 * Note this may be the same image as that returned by
	 * {@code getImage(String)} if this icon group doesn't have icons
	 * specifically for native components.
	 *
	 * @param name The image to load.
	 * @return The image, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getImage(String)
	 */
	Image getNativeImage(String name);


	/**
	 * Returns a version of an image to use in "native" UI components.
	 * This is primarily used to determine icons in the macOS native
	 * menu bar, since it may not match the application's theme.<p>
	 *
	 * Note this may be the same image as that returned by
	 * {@code getImage(String)} if this icon group doesn't have icons
	 * specifically for native components.
	 *
	 * @param name The image to load.
	 * @param w The width of the image to load.
	 * @param h The height of the image to load.
	 * @return The image, or <code>null</code> if it could not be found or
	 *         loaded.
	 * @see #getImage(String, int, int)
	 */
	Image getNativeImage(String name, int w, int h);


}
