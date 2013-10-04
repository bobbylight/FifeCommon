/*
 * 07/26/2004
 *
 * GUIApplicationPreferences.java - An interface for Preferences objects for
 * GUI applications.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.KeyStroke;


/**
 * Preferences for a <code>GUIApplication</code>.  This class currently
 * explicitly remembers the accelerators for all actions of the GUI
 * application in a hash map.  The keys in the map are the action keys in
 * the application.
 *
 * @author Robert Futrell
 * @version 0.1
 * @see GUIApplication
 * @see AbstractGUIApplication
 */
public abstract class GUIApplicationPreferences
								implements GUIApplicationConstants {

	/**
	 * Accelerators for all actions in the <code>GUIApplication</code>'s
	 * action map.
	 *
	 * @see #getAccelerator(String)
	 */
	public HashMap<String, KeyStroke> accelerators;

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
	 * Creates a properties object with all fields initialized to the values
	 * that the specified application instance is currently running with.<p>
	 *
	 * The default implementation returns <code>null</code>.  This method
	 * should be overridden to return properties for the specified object.
	 *
	 * @param obj The object for which to generate preferences.
	 * @return A <code>GUIApplicaitonPrefences</code> object initialized to
	 *         contain the properties the specified object is running with.
	 */
	public static GUIApplicationPreferences generatePreferences(Object obj) {
		return null;
	}


	/**
	 * Returns the accelerator for the specified action name.
	 * This is shorthand for (KeyStroke)accelerators.get(NAME);
	 *
	 * @return The accelerator.
	 */
	public KeyStroke getAccelerator(String actionName) {
		return accelerators.get(actionName);
	}


	/**
	 * Loads the preferences specified in this class into the specified
	 * <code>GUIApplicationPreferences</code> object.  This method
	 * can be called by any subclass's <code>loadPreferences</code>
	 * so they don't have to worry about doing it all themselves.
	 *
	 * @param prefs The preferences into which to load.  If any of the
	 *        preferences aren't found in the backing store, the value
	 *        of those preferences saved in this instance are not
	 *        are not changed (i.e., the defaults are the current values).
	 * @param p The preferences backing store from which to
	 *        retrieve preferences.
	 */
	protected static void loadCommonPreferences(
					GUIApplicationPreferences prefs,
					Preferences p) {
		prefs.location.x		= p.getInt("location.x", prefs.location.x);
		prefs.location.y		= p.getInt("location.y", prefs.location.y);
		prefs.size.width		= p.getInt("size.width", prefs.size.width);
		prefs.size.height		= p.getInt("size.height", prefs.size.height);
		prefs.toolbarVisible	= p.getBoolean("toolbarVisible",
											prefs.toolbarVisible);
		prefs.statusBarVisible	= p.getBoolean("statusBarVisible",
											prefs.statusBarVisible);
		prefs.lookAndFeel		= p.get("lookAndFeel", prefs.lookAndFeel);
		prefs.language			= p.get("language", prefs.language);
	}


	/**
	 * Initializes this preferences instance with data saved previously via
	 * the Java Preferences API.  If the load fails, the result is
	 * implementation-specific; usually this preferences instance will be
	 * populated with default values.<p>
	 *
	 * The default implementation returns <code>null</code>.  This method
	 * should be overridden to return properties for the specified object.
	 *
	 * @return If the load went okay, the preferences for the given application
	 *         are returned.  If something went wrong, or the user has never
	 *         used this application before (and thus there are no saved
	 *         preferences), default values are returned.
	 */
	public static GUIApplicationPreferences loadPreferences() {
		return null;
	}


	/**
	 * Saves the common preferences specified in this class.  This method
	 * can be called by any subclass's <code>savePreferences</code>
	 * so they don't have to worry about doing it all themselves.
	 *
	 * @param prefs The preferences backing store into which to
	 *        save preferences.
	 */
	protected void saveCommonPreferences(Preferences prefs) {
		prefs.putInt("location.x", location.x);
		prefs.putInt("location.y", location.y);
		prefs.putInt("size.width", size.width);
		prefs.putInt("size.height", size.height);
		prefs.putBoolean("toolbarVisible", toolbarVisible);
		prefs.putBoolean("statusBarVisible", statusBarVisible);
		prefs.put("lookAndFeel", lookAndFeel);
		prefs.put("language", language);
	}


	/**
	 * Saves this preferences instance via the Java Preferences API.
	 *
	 * @param object Can be anything needed by an implementation of
	 *        <code>GUIApplicationPreferences</code>.  For example, it can be
	 *        the application instance for which you are saving preferences.
	 *        This parameter may not be needed by your implementation.
	 */
	public abstract void savePreferences(Object object);


	/**
	 * Sets this preferences instance to contain all default values.
	 */
	protected abstract void setDefaults();


}