/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.util.Map;

/**
 * A Swing {@code LookAndFeel} along with additional, application-specific
 * properties, that need to be changed when the user changes the "theme"
 * of the application.  Applications can implement this interface to
 * include themes with additional colors and styles for custom components.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface AppTheme {

	/**
	 * Retrns extra values to put into the {@code UIManager}.
	 *
	 * @return Extra properties.
	 */
	Map<String, Object> getExtraUiDefaults();

	/**
	 * Returns a unique ID for this theme.  This is used for internal purposes.
	 *
	 * @return The unique ID for this theme.
	 * @see #getName()
	 */
	String getId();

	/**
	 * Returns the class name for the {@code LookAndFeel} to install.
	 *
	 * @return The look and feel.
	 */
	String getLookAndFeel();

	/**
	 * Returns a display name for this theme.
	 *
	 * @return A display name for this theme.
	 * @see #getId()
	 */
	String getName();
}
