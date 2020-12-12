/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.prefs;

import org.fife.ui.app.GUIApplication;

import javax.swing.*;
import java.awt.*;


/**
 * Preferences for an application.  Has some built-in properties that all
 * applications would want to keep track of.
 *
 * @param <T> The type of application whose preferences are stored.
 * @author Robert Futrell
 * @version 1.0
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
public abstract class AppPrefs<T extends GUIApplication> extends Prefs {

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
	 * The user's preferred look and feel for the application.
	 */
	public String lookAndFeel;

	/**
	 * The language for this application, in a Locale-friendly string.
	 */
	public String language;


	/**
	 * Initializes the preferences in this class (those shared across
	 * all applications) to the values of the specified application.
	 * Subclasses should override but call the super implementation so
	 * that the fields defined in this class are set.
	 *
	 * @param app The application from which to load the preferences.
	 */
	public void loadFromApplication(T app) {
		location = app.getLocation();
		size = app.getSize();
		toolbarVisible = app.getToolBarVisible();
		statusBarVisible = app.getStatusBarVisible();
		lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		language = app.getLanguage();
	}
}
