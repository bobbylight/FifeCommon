/*
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs;

import java.awt.*;


/**
 * Preferences for an application.  Has some built-in properties that all
 * applications would want to keep track of.  Concrete {@code GUIApplication}
 * implementations are expected to have sister implementations of this
 * class that add public fields representing app-specific preferences
 * to load and save.
 *
 * @author Robert Futrell
 * @version 1.0
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
public abstract class AppPrefs extends Prefs {

	/**
	 * The location on-screen of this GUI application.
	 */
	public Point location;

	/**
	 * The size of this GUI application's main window.
	 */
	public Dimension size;

	/**
	 * Whether this application's toolbar is visible.
	 */
	public boolean toolbarVisible;

	/**
	 * Whether this application's status bar is visible.
	 */
	public boolean statusBarVisible;

	/**
	 * The current application theme name.
	 */
	public String appTheme;

	/**
	 * The language for this application, in a Locale-friendly string.
	 */
	public String language;
}
