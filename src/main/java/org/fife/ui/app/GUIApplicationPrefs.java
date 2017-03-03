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
import java.util.prefs.Preferences;
import javax.swing.UIManager;


/**
 * Preferences for a <code>GUIApplication</code>.  This class remembers basic
 * information relevant to most GUI applications.  It is expected that
 * subclasses will override and load/store more information specific to each
 * application.
 *
 * @param <T> The parent application type.
 * @author Robert Futrell
 * @version 0.2
 * @see GUIApplication
 * @see AbstractGUIApplication
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
public abstract class GUIApplicationPrefs<T extends GUIApplication>
								implements GUIApplicationConstants {

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

	}


	/**
	 * Loads the preferences instances for this application.
	 *
	 * @return This preferences instance.
	 * @see #populate(GUIApplication)
	 * @see #save()
	 */
	public abstract GUIApplicationPrefs<T> load();


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