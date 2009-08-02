/*
 * 10/27/2004
 *
 * FileChooserPreferences.java - The preferences object for RTextFileChooser.
 * Copyright (C) 2004 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * Preferences for the file chooser.  These are stored via the Java Preferences
 * API so they are remembered between application invocations.
 *
 * @author Robert Futrell
 * @version 0.7
 */
class FileChooserPreferences {

	public boolean showHiddenFiles;
	public boolean fileSystemAware;
	public boolean autoCompleteFileNames;
	public Color hiddenFileColor;
	public Color defaultFileColor;
	public HashMap customColors;
	public boolean styleOpenFiles;
	public int openFilesStyle;

	private static final String FTI_PREFIX				= "fti_";
	private static final String AUTO_COMPLETE_KEY		= "autoComplete";
	private static final String DEFAULT_COLOR_KEY		= "defaultcolor";
	private static final String FILE_SYSTEM_AWARE_KEY		= "fileSystemAware";
	private static final String HIDDEN_FILE_COLOR_KEY		= "hiddenfilecolor";
	private static final String OPEN_FILES_STYLE_KEY		= "openFilesStyle";
	private static final String SHOW_HIDDEN_FILES_KEY		= "showhiddenfiles";
	private static final String STYLE_OPEN_FILES_KEY		= "styleOpenFiles";

	private static final Color DEFAULT_HIDDEN_FILE_COLOR	= new Color(192,192,192);
	private static final Color DEFAULT_COLOR			= Color.BLACK;

	private static final int DEFAULT_HIDDEN_FILE_COLOR_INT	= DEFAULT_HIDDEN_FILE_COLOR.getRGB();
	private static final int DEFAULT_COLOR_INT			= DEFAULT_COLOR.getRGB();


	/**
	 * Constructor.
	 */
	private FileChooserPreferences() {
		setDefaults();
	}


	/**
	 * Creates a properties object with all fields initialized to the values
	 * that the specified <code>RTextFileChooser</code> instance is currently
	 * running with.
	 *
	 * @param chooser The <code>RTextFileChooser</code> for which to get
	 *        preferences.
	 * @return The preferences for that file chooser.
	 */
	private static FileChooserPreferences generate(RTextFileChooser chooser) {
		FileChooserPreferences prefs = new FileChooserPreferences();
		prefs.customColors = chooser.getCustomColorsMap();
		prefs.defaultFileColor = chooser.getDefaultFileColor();
		prefs.hiddenFileColor = chooser.getHiddenFileColor();
		prefs.showHiddenFiles = chooser.getShowHiddenFiles();
		prefs.fileSystemAware = chooser.getFileSystemAware();
		prefs.autoCompleteFileNames = chooser.getAutoCompleteFileNames();
		prefs.styleOpenFiles = chooser.getStyleOpenFiles();
		prefs.openFilesStyle = chooser.getOpenFilesStyle();
		return prefs;
	}


	/**
	 * Initializes this preferences instance with data saved previously via
	 * the Java Preferences API.  If the load fails, default values will be
	 * used.
	 *
	 * @return The preferences loaded.
	 */
	public static FileChooserPreferences load() {

		FileChooserPreferences fprefs = new FileChooserPreferences();

		try {

			Preferences prefs = Preferences.userNodeForPackage(
										RTextFileChooser.class);

			fprefs.customColors = FileChooserPreferences.
											loadCustomColorsMap();

			// Get the default color.
			fprefs.defaultFileColor = new Color(
					prefs.getInt(DEFAULT_COLOR_KEY, DEFAULT_COLOR_INT));

			// Get the hidden file color.
			fprefs.hiddenFileColor = new Color(
					prefs.getInt(HIDDEN_FILE_COLOR_KEY,
									DEFAULT_HIDDEN_FILE_COLOR_INT));

			// After we get all of the colors, go for the other stuff.
			fprefs.showHiddenFiles	= prefs.getBoolean(
						SHOW_HIDDEN_FILES_KEY, fprefs.showHiddenFiles);
			fprefs.fileSystemAware	= prefs.getBoolean(
						FILE_SYSTEM_AWARE_KEY, fprefs.fileSystemAware);
			fprefs.autoCompleteFileNames = prefs.getBoolean(
						AUTO_COMPLETE_KEY, fprefs.autoCompleteFileNames);
			fprefs.styleOpenFiles	= prefs.getBoolean(
						STYLE_OPEN_FILES_KEY, fprefs.styleOpenFiles);
			fprefs.openFilesStyle	= prefs.getInt(
						OPEN_FILES_STYLE_KEY, fprefs.openFilesStyle);

		} catch (RuntimeException re) {
			throw re; // Keep FindBugs happy.
		} catch (Exception e) {
			fprefs.setDefaults();
		}

		return fprefs;

	}


	/**
	 * Returns the mapping of extensions to custom colors last saved.
	 *
	 * @return The mapping.
	 */
	private static HashMap loadCustomColorsMap() {

		HashMap map = new HashMap();

		try {

			Preferences prefs = Preferences.userNodeForPackage(
										RTextFileChooser.class);
			String[] keys = prefs.keys();

			// All of the preferences that are extension => color mappings
			// share a common prefix that we check for.

			int count = keys.length;
			int prefixLength = FTI_PREFIX.length();
			for (int i=0; i<count; i++) {
				if (keys[i].startsWith(FTI_PREFIX)) {
					String extension = keys[i].substring(prefixLength);
					int packed = prefs.getInt(keys[i], DEFAULT_COLOR_INT);
					Color c = new Color(packed);
					map.put(extension, c);
				}
			}

		} catch (RuntimeException re) {
			throw re; // Keep FindBugs happy.
		} catch (Exception e) {
			// Do nothing.
		}

		return map;

	}


	/**
	 * Saves the preferences for a file chooser instance via the Java
	 * Preferences API.
	 *
	 * @param chooser The file chooser for which you want to save preferences.
	 */
	public static void save(RTextFileChooser chooser) {

		// Create an object containing all proeprties of the file chooser.
		FileChooserPreferences fcp = FileChooserPreferences.generate(chooser);

		// prefs is the Java interface to native preferences saving.
		Preferences prefs = Preferences.userNodeForPackage(
											RTextFileChooser.class);

		// Clear all old preferences.  We do this since the extension-to-
		// color mapping may have changed (e.g., some mappings may have
		// been removed).  This physically removes them.
		try {
			prefs.clear();
		} catch (BackingStoreException bse) {
			bse.printStackTrace();
		}

		// First, save all color/file extension mappings.
		Set keys = fcp.customColors.keySet();
		for (Iterator i=keys.iterator(); i.hasNext(); ) {
			String key = (String)i.next();
			Color c = (Color)fcp.customColors.get(key);
			prefs.putInt(FTI_PREFIX+key, c.getRGB());
		}

		// Do default color.
		prefs.putInt(DEFAULT_COLOR_KEY, fcp.defaultFileColor.getRGB());

		// Do hidden file color.
		prefs.putInt(HIDDEN_FILE_COLOR_KEY, fcp.hiddenFileColor.getRGB());

		// Do other stuff.
		prefs.putBoolean(SHOW_HIDDEN_FILES_KEY, fcp.showHiddenFiles);
		prefs.putBoolean(FILE_SYSTEM_AWARE_KEY, fcp.fileSystemAware);
		prefs.putBoolean(AUTO_COMPLETE_KEY, fcp.autoCompleteFileNames);
		prefs.putBoolean(STYLE_OPEN_FILES_KEY,  fcp.styleOpenFiles);
		prefs.putInt(OPEN_FILES_STYLE_KEY, fcp.openFilesStyle);

	}


	/**
	 * Sets this preferences instance to contain all default values.
	 */
	protected void setDefaults() {
		showHiddenFiles = false;
		hiddenFileColor = DEFAULT_HIDDEN_FILE_COLOR;
		fileSystemAware = true;
		autoCompleteFileNames = true;
		defaultFileColor = DEFAULT_COLOR;
		customColors = null;
		styleOpenFiles = true;
		openFilesStyle = RTextFileChooser.STYLE_UNDERLINE;
	}


}