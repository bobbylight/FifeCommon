/*
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import org.fife.jgoodies.looks.common.ShadowPopupFactory;
import org.fife.ui.UIUtil;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.app.prefs.AppPrefs;
import org.fife.util.DarculaUtil;
import org.fife.util.SubstanceUtil;

import javax.swing.*;
import java.io.File;
import java.io.IOException;


/**
 * The context in which an application runs.  Applications should extend this class
 * with one that knows how to load the preferences for the app as well as how to
 * instantiate the main {@code GUIApplication} instance.<p>
 *
 * Implementations must override a handful of methods to specify important
 * information:
 * <ul>
 *     <li>{@code getPreferencesClassName()} - the Java class containing the
 *         preferences for the application
 *     <li>{@code getPreferencesDir()} - The directory in which to store
 *         preference-related files
 *     <li>{@code getPreferencesFileName()} - The name of the file to
 *         create in the preferences directory that will contain the
 *         application preferences
 * </ul>
 *
 * If the directory specified by {@code getPreferencesDir()} doesn't exist,
 * it will get created.  By default, loading and saving application preferences
 * will only load and save the single file named by
 * {@code getPreferencesfileName()}; however, applications can store other
 * preferences files in this directory, such as a file containing the
 * application's configurable shortcuts.
 *
 * @param <T> The application type.
 * @param <P> The preferences type.
 */
public abstract class AppContext<T extends GUIApplication, P extends AppPrefs> {


	/**
	 * Returns the name of the preferences class for this application.  This
	 * class must be a subclass of {@link AppPrefs}.
	 *
	 * @return The class name, or {@code null} if this GUI application
	 *         does not save preferences.
	 * @see #getPreferencesFile()
	 */
	protected abstract String getPreferencesClassName();


	/**
	 * Returns the directory in which to store application preferences.
	 *
	 * @return The directory.  If this is {@code null}, no preferences
	 *          will be loaded or saved.
	 * @see #getPreferencesClassName()
	 * @see #getPreferencesFile()
	 */
	public abstract File getPreferencesDir();


	/**
	 * Returns the file in which to store application preferences.
	 * This is the concatenation of {@link #getPreferencesDir()}
	 * and {@link #getPreferencesFileName()}.  If either of those
	 * values are {@code null}, this method will also return
	 * {@code null}.
	 *
	 * @return The file.  If this is {@code null}, no preferences
	 *          will be loaded or saved.
	 * @see #getPreferencesDir()
	 * @see #getPreferencesFileName()
	 */
	public final File getPreferencesFile() {
		File dir = getPreferencesDir();
		String fileName = getPreferencesFileName();
		if (dir != null && fileName != null) {
			return new File(dir, fileName);
		}
		return null;
	}


	/**
	 * Returns the name of the file, excluding the path,
	 * in which to store application preferences.
	 *
	 * @return The file.  If this is {@code null}, no preferences
	 *          will be loaded or saved.
	 * @see #getPreferencesFile()
	 * @see #getPreferencesClassName()
	 */
	public abstract String getPreferencesFileName();


	/**
	 * Callback called when an exception occurs and no specific application
	 * instance is available to handle it.<p>
	 *
	 * The default implementation prints the stack trace.  Subclasses can
	 * override.
	 *
	 * @param e The error.
	 */
	protected void handleException(Exception e) {
		e.printStackTrace();
	}


	protected abstract T createApplicationImpl(String[] filesToOpen, P preferences);


	/**
	 * Returns the active, unsaved state of preferences of an application.
	 *
	 * @param app The application.
	 * @return The application's unsaved preferences state, or {@code null}
	 *          if this application does not store preferences.
	 * @throws IOException If an IO error occurs.
	 * @see #savePreferences(GUIApplication)
	 * @see #loadPreferences()
	 */
	@SuppressWarnings("unchecked")
	public P getActivePreferencesState(T app) throws IOException {

		File preferencesFile = getPreferencesFile();
		if (preferencesFile == null) {
			return null;
		}

		String prefsClassName = getPreferencesClassName();
		if (prefsClassName!=null) {
			try {
				Class<?> prefsClass = Class.forName(prefsClassName);
				P prefs = (P)prefsClass.getDeclaredConstructor().newInstance();
				populatePrefsFromApplication(app, prefs);
				return prefs;
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}
		}

		return null;
	}


	/**
	 * Does any initialization and configuration of the Look and Feel that must
	 * be done before any windows are instantiated, for example, configuring
	 * look-and-feel-decorated.
	 *
	 * @param lafName The name of the look and feel to instantiate.
	 * @return A Look and Feel manager for the application to use when
	 *          loading 3rd-party Look and Feels.
	 */
	private ThirdPartyLookAndFeelManager initializeAndConfigureLookAndFeel(String lafName) {

		// Allow Substance to paint window titles, etc.  We don't allow
		// Metal (for example) to do this, because setting these
		// properties to "true", then toggling to a LAF that doesn't
		// support this property, such as Windows, causes the
		// OS-supplied frame to not appear (as of 6u20).
		if (SubstanceUtil.isASubstanceLookAndFeel(lafName) ||
			DarculaUtil.isDarculaLookAndFeel(lafName)) {
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
		}

		String rootDir = AbstractGUIApplication.getLocationOfJar();
		ThirdPartyLookAndFeelManager lafManager =
			new ThirdPartyLookAndFeelManager(rootDir);

		try {
			initializeLookAndFeelImpl(lafManager, lafName);
		} catch (RuntimeException re) { // FindBugs
			throw re;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// The default speed of Substance animations is too slow
		// (200ms), looks bad moving through JMenuItems quickly.
		if (SubstanceUtil.isSubstanceInstalled()) {
			try {
				SubstanceUtil.setAnimationSpeed(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (lafName.contains(".Darcula")) {
			UIManager.getLookAndFeelDefaults().put("Tree.rendererFillBackground", Boolean.FALSE);
		}
		else {
			UIManager.getLookAndFeelDefaults().put("Tree.rendererFillBackground", null);
		}

		return lafManager;
	}


	/**
	 * Sets the look and feel the first time, before the application starts.
	 *
	 * @param lafManager The 3rd-party LAF manager that will be installed.
	 * @param lafName The Look and Feel to install.
	 * @throws ReflectiveOperationException If an unexpected error occurs.
	 * @throws UnsupportedLookAndFeelException If an unexpected error occurs.
	 */
	private static void initializeLookAndFeelImpl(
		ThirdPartyLookAndFeelManager lafManager,
		String lafName) throws ReflectiveOperationException,
		UnsupportedLookAndFeelException {

		ClassLoader cl = lafManager.getLAFClassLoader();

		// Set these properties before instantiating WebLookAndFeel
		if (WebLookAndFeelUtils.isWebLookAndFeel(lafName)) {
			WebLookAndFeelUtils.installWebLookAndFeelProperties(cl);
		}
		else {
			ShadowPopupFactory.install();
		}

		// Must set UIManager's ClassLoader before instantiating
		// the LAF.  Substance is so high-maintenance!
		UIManager.getLookAndFeelDefaults().put("ClassLoader", cl);

		// Java 11+ won't let us reflectively access system LookAndFeel
		// classes, so we need a little extra logic here
		if (UIManager.getSystemLookAndFeelClassName().equals(lafName)) {
			UIManager.setLookAndFeel(lafName);
		}
		else {

			try {

				Class<?> clazz;
				clazz = cl.loadClass(lafName);

				LookAndFeel laf = (LookAndFeel)clazz.getDeclaredConstructor().
					newInstance();
				UIManager.setLookAndFeel(laf);
			} catch (UnsupportedClassVersionError | ClassNotFoundException e) {
				// A LookAndFeel requiring Java X or later, but we're
				// now restarting with a Java version earlier than X.
				// ClassNotFoundException can only occur with a preferences
				// file manually edited to a bad value.  In either case
				// we want to start with the OS "native" LAF and not Metal.
				// Note we must set it this way and not load via reflection
				// due to Java 9+ forbidding reflection accessing com.sun.*
				// classes
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		}

		UIManager.getLookAndFeelDefaults().put("ClassLoader", cl);
		UIUtil.installOsSpecificLafTweaks();
	}


	/**
	 * Loads the preferences for this application type.
	 *
	 * @return The preferences.
	 * @see #savePreferences(GUIApplication)
	 */
	@SuppressWarnings("unchecked")
	public P loadPreferences() {

		File preferencesFile = getPreferencesFile();
		if (preferencesFile == null) {
			return null;
		}

		P prefs = null;
		String prefsClassName = getPreferencesClassName();
		if (prefsClassName!=null) {
			try {
				Class<?> prefsClass = Class.forName(prefsClassName);
				prefs = (P)prefsClass.getDeclaredConstructor().newInstance();
				if (preferencesFile.isFile()) { // Doesn't exist first time through
					prefs.load(preferencesFile);
				}
			} catch (Exception e) {
				handleException(e);
			}
		}

		return prefs;
	}


	/**
	 * Initializes the preference values in {@code prefs} that are shared
	 * across all applications to the values of the specified application.
	 * Subclasses should override but call the super implementation so
	 * that the fields defined in this class are set, and set all app-specific
	 * properties of {@code prefs}.
	 *
	 * @param app The application from which to load the preferences.
	 * @param prefs The preferences to populate.
	 */
	protected void populatePrefsFromApplication(T app, P prefs) {
		prefs.location = app.getLocation();
		prefs.size = app.getSize();
		prefs.toolbarVisible = app.getToolBarVisible();
		prefs.statusBarVisible = app.getStatusBarVisible();
		prefs.lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		prefs.language = app.getLanguage();
	}


	/**
	 * Saves an application's preferences.
	 *
	 * @param app The application whose preferences should be saved.
	 * @throws IOException If an IO error occurs.
	 * @see #loadPreferences()
	 * @see #getActivePreferencesState(GUIApplication)
	 */
	@SuppressWarnings("unchecked")
	public void savePreferences(T app) throws IOException {

		File preferencesFile = getPreferencesFile();
		if (preferencesFile == null) {
			return;
		}

		String prefsClassName = getPreferencesClassName();
		if (prefsClassName!=null) {
			try {
				Class<?> prefsClass = Class.forName(prefsClassName);
				P prefs = (P)prefsClass.getDeclaredConstructor().newInstance();
				populatePrefsFromApplication(app, prefs);
				prefs.save(getPreferencesFile());
			} catch (IOException ioe) {
				throw ioe;
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}
		}
	}


	/**
	 * Starts the application.  This should be called on the EDT.
	 *
	 * @param args The command line arguments (typically files to open).
	 * @return The application.
	 */
	public T createApplication(String[] args) {

		// 1.5.2004/pwy: Setting this property makes the menu appear on top
		// of the screen on Apple Mac OS X systems. It is ignored by all other
		// other Java implementations.
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// 1.5.2004/pwy: Setting this property defines the standard
		// Application menu name on Apple Mac OS X systems. It is ignored by
		// all other Java implementations.
		// NOTE: Although you can set the useScreenMenuBar property above at
		// runtime, it appears that for this one, you must set it before
		// (such as in your *.app definition).
		//System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RText");

		P prefs = loadPreferences();

		// Make Darcula and Metal not use bold fonts
		UIManager.put("swing.boldMetal", Boolean.FALSE);

		ThirdPartyLookAndFeelManager lafManager =
			initializeAndConfigureLookAndFeel(prefs.lookAndFeel);

		T app = createApplicationImpl(args, prefs);

		if (app instanceof AbstractGUIApplication) {
			((AbstractGUIApplication<?>)app).setLookAndFeelManager(lafManager);
		}

		return app;
	}
}
