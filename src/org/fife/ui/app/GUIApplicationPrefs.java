/*
 * 07/26/2004
 *
 * GUIApplicationPrefs.java - An interface for Preferences objects for
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
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.UIManager;


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
public abstract class GUIApplicationPrefs<T extends GUIApplication>
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

	private static final String NOTHING_STRING = "-";


	/**
	 * Populates this preferences instance with the "common" preferences
	 * found in this base class.
	 *
	 * @param app The application to populate from.
	 * @see #populateCommonPreferences(GUIApplication, String)
	 */
	protected void populateCommonPreferences(GUIApplication app) {
		populateCommonPreferences(app, null);
	}


	/**
	 * Populates this preferences instance with the "common" preferences
	 * found in this base class.
	 *
	 * @param app The application to populate from.
	 * @param lnf An override for the LookAndFeel to populate.  If this
	 *        parameter is <code>null</code>, the current LookAndFeel is
	 *        used.
	 * @see #populateCommonPreferences(GUIApplication)
	 */
	protected void populateCommonPreferences(GUIApplication app, String lnf) {
		
		if (lnf==null) {
			lnf = UIManager.getLookAndFeel().getClass().getName();
		}

		location				= app.getLocation();
		location.translate(15,15);
		size					= app.isMaximized() ? new Dimension(-1,-1) :
									app.getSize();
		lookAndFeel				= lnf;
		toolbarVisible			= app.getToolBarVisible();
		statusBarVisible		= app.getStatusBarVisible();
		language					= app.getLanguage();
		
		accelerators = new HashMap<String, KeyStroke>();
		for (Action a : app.getActions()) {
			if (a instanceof StandardAction) {
				StandardAction sa = (StandardAction)a;
				accelerators.put(sa.getName(), sa.getAccelerator());
				//System.out.println(sa.getName() + " - " + sa.getAccelerator());
			}
		}
		
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
	 * Returns a string suitable for saving this keystroke via the Java
	 * Preferences API of the form "&lt;keycode> &lt;modifiers>".
	 *
	 * @param stroke The keystroke for which to get the string.
	 * @return A <code>String</code> representing the keystroke.
	 * @see #getKeyStrokeFromString
	 */
	private static final String getKeyStrokeString(KeyStroke stroke) {
		if (stroke!=null) {
			return stroke.getKeyCode() + " " + stroke.getModifiers();
		}
		return NOTHING_STRING;
	}


	/**
	 * Returns the keystroke from the passed-in string of the form
	 * "&lt;keycode&gt; &lt;modifiers&gt;".
	 *
	 * @param string The string from which to get the keystroke.  This string
	 *        was saved by a previous <code>RTextPreferences</code>.
	 * @return The keystroke.
	 * @see #getKeyStrokeString
	 */
	private static final KeyStroke getKeyStrokeFromString(String string) {
		int space = string.indexOf(' ');
		if (space>-1) {
			return KeyStroke.getKeyStroke(
						Integer.parseInt(string.substring(0,space)),
						Integer.parseInt(string.substring(space+1)));
		}
		return null;
	}


	/**
	 * Loads the preferences instances for this application.
	 *
	 * @return This preferences instance.
	 * @see #populate(GUIApplication)
	 * @see #save()
	 */
	public abstract GUIApplicationPrefs<T> load();


	protected void loadActionAccelerators(String[] actionNames, Preferences prefs) {
		for (int i=0; i<actionNames.length; i++) {
			String actionName = actionNames[i];
			String temp = prefs.get(actionName, null);
			if (temp!=null)
				accelerators.put(actionName,
							getKeyStrokeFromString(temp));
		}
	}


	/**
	 * Loads the preferences specified in this class into the specified
	 * <code>GUIApplicationPrefs</code> object.  This method
	 * can be called by any subclass's <code>loadPreferences</code>
	 * so they don't have to worry about doing it all themselves.
	 *
	 * @param p The preferences backing store from which to
	 *        retrieve preferences.
	 */
	protected void loadCommonPreferences(Preferences p) {
		location.x		= p.getInt("location.x", location.x);
		location.y		= p.getInt("location.y", location.y);
		size.width		= p.getInt("size.width", size.width);
		size.height		= p.getInt("size.height", size.height);
		toolbarVisible	= p.getBoolean("toolbarVisible",
											toolbarVisible);
		statusBarVisible	= p.getBoolean("statusBarVisible",
											statusBarVisible);
		lookAndFeel		= p.get("lookAndFeel", lookAndFeel);
		language			= p.get("language", language);
	}


	/**
	 * Populates this preferences instance from an application.
	 *
	 * @param app The application.
	 * @return This preferences instance.
	 * @see #load()
	 * @see #save()
	 */
	public abstract GUIApplicationPrefs<T> populate(T app);


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
		for (Map.Entry<String, KeyStroke> entry : accelerators.entrySet()) {
			//System.out.println(entry.getKey() + "... ... " + getKeyStrokeString(entry.getValue()));
			prefs.put(entry.getKey(), getKeyStrokeString(entry.getValue()));
		}
	}


	/**
	 * Saves this preferences instance via the Java Preferences API.
	 *
	 * @see #load()
	 * @see #populate(GUIApplication)
	 */
	public abstract void save();


	/**
	 * Sets this preferences instance to contain all default values.
	 */
	protected abstract void setDefaults();


}