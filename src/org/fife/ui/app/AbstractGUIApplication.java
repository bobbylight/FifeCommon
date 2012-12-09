/*
 * 11/20/2004
 *
 * AbstractGUIApplication.java - A basic GUI application.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fife.ui.AboutDialog;
import org.fife.ui.CustomizableToolBar;
import org.fife.ui.OptionsDialog;
import org.fife.ui.SplashScreen;
import org.fife.ui.StatusBar;
import org.fife.help.HelpDialog;

import com.apple.osxadapter.NativeMacApp;


/**
 * A basic, generic GUI application.  Your Swing application can
 * override this class to have some of its basic code already implemented.<p>
 *
 * This class currently helps you implement:
 * <ul>
 *   <li>A menu bar.
 *   <li>A customizable toolbar.
 *   <li>A status bar.
 *   <li>A "Help" dialog.
 *   <li>An "About" dialog.
 *   <li>Easy localization.
 *   <li>A way to capture <code>Exceptions</code> and report them to
 *       the user.
 *   <li>An easy to organize/maintain the actions associated with the
 *       application's menu bar, toolbar, etc.
 *   <li>A "splash screen" interface.
 *   <li>Necessary Mac OS X hooks.
 * </ul>
 *
 * @author Robert Futrell
 * @version 0.5
 * @see org.fife.ui.app.AbstractPluggableGUIApplication
 */
public abstract class AbstractGUIApplication extends JFrame
							implements GUIApplication, NativeMacApp {

	/**
	 * This property is fired whenever the status bar changes.
	 */
	public static final String STATUS_BAR_PROPERTY		= "statusBar";

	/**
	 * This property is fired whenever the status bar is made visible or
	 * invisible.
	 */
	public static final String STATUS_BAR_VISIBLE_PROPERTY	="statusBarVisible";

	/**
	 * This property is fired whenever the toolbar changes.
	 */
	public static final String TOOL_BAR_PROPERTY			= "toolBar";

	/**
	 * This property is fired whenever the toolbar is made visible or
	 * invisible.
	 */
	public static final String TOOL_BAR_VISIBLE_PROPERTY	= "toolBarVisible";


	/**
	 * The key for getting the About action from <code>getAction</code>.
	 */
	public static final String ABOUT_ACTION_KEY			= "aboutAction";

	/**
	 * The key for getting the Exit action from <code>getAction</code>.
	 */
	public static final String EXIT_ACTION_KEY			= "exitAction";

	/**
	 * The key for getting the Help action from <code>getAction</code>.
	 */
	public static final String HELP_ACTION_KEY			= "helpAction";


	/**
	 * The About dialog.
	 */
	private AboutDialog aboutDialog;

	/**
	 * The toolbar.
	 */
	private CustomizableToolBar toolBar;

	/**
	 * The status bar.
	 */
	private StatusBar statusBar;

	/**
	 * The map of actions for this application.
	 */
	private HashMap actionMap;

	/**
	 * This application's resource bundle.
	 */
	private ResourceBundle resourceBundle;

	/**
	 * The directory in which this application was installed.
	 */
	private String installLocation;

	/**
	 * Locale language string (e.g., "en" or "es").
	 */
	private String language;


	/**
	 * The content pane.
	 */
	private JPanel contentPane;

	/**
	 * Panels containing toolbars.  At least one of these always exists;
	 * it contains the main toolbar.  The last-indexed panel will contain
	 * a toolbar and the <code>mainContentPanel</code>.  Each
	 * increasing toolbar panel is contained in the previous one (i.e.,
	 * if there are 3 of these, 0 contains 1 and 1 contains 2, which also
	 * contains <code>mainContentPanel</code>).
	 */
	private JPanel[] toolBarPanels;

	/**
	 * This panel contains <code>actualContentPane</code>.  This panel is here
	 * so that the subclass <code>AbstractPluggableGUIApplication</code> can
	 * also place GUI plugins inside of it.
	 */
	protected JPanel mainContentPanel;

	/**
	 * This panel contains the actual content of the application (i.e.,
	 * when the use calls <code>getContentPane</code> to add
	 * stuff, this is the panel they get).
	 */
	protected JPanel actualContentPane;

	/**
	 * Used to dynamically load 3rd-party LookAndFeels.
	 */
	private ThirdPartyLookAndFeelManager lafManager;


	private static final String STATUS_BAR_LOCATION	= BorderLayout.SOUTH;
	private static final String TOOL_BAR_LOCATION	= BorderLayout.NORTH;

	/**
	 * An integer constant representing the OS, such as
	 * <code>OS_WINDOWS</code> or <code>OS_LINUX</code>.
	 */
	private int os;


	/**
	 * Constructor.
	 *
	 * @param jarFile The name (not full path) of the JAR file containing the
	 *        main class of this application (e.g. "Foobar.jar").
	 */
	public AbstractGUIApplication(String jarFile) {
		this(null, jarFile);
	}


	/**
	 * Constructor.
	 *
	 * @param title The title for this frame.
	 * @param jarFile The name (not full path) of the JAR file containing the
	 *        main class of this application (e.g. "Foobar.jar").
	 */
	public AbstractGUIApplication(String title, String jarFile) {
		initialize(title, jarFile, loadPreferences());
	}


	/**
	 * Constructor.  This constructor is useful when you are making a clone of
	 * the current application (e.g., "Open in New Window...") and you want
	 * the two instances to have the same properties.
	 *
	 * @param title The title for this frame.
	 * @param jarFile The name (not full path) of the JAR file containing the
	 *        main class of this application (e.g. "Foobar.jar").
	 * @param prefs The preferences with which to initialize this application.
	 */
	public AbstractGUIApplication(String title, String jarFile,
							GUIApplicationPreferences prefs) {
		initialize(title, jarFile, prefs);
	}


	/**
	 * Initializes this GUI application.  This is separate from the
	 * constructors because we need the ability to call
	 * <code>loadPreferences</code> to get the preferences with which to
	 * initialize this class.
	 *
	 * @param title The title for this frame.
	 * @param jarFile The name (not full path) of the JAR file containing the
	 *        main class of this application (e.g. "Foobar.jar").
	 * @param prefs The preferences with which to initialize this application.
	 */
	private void initialize(String title, String jarFile,
							GUIApplicationPreferences prefs) {

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		// Set up the localization stuff.
		// NOTE: We default to English instead of using the JVM's default
		// locale as we want to be sure all of RText's UI uses the same l10n.
		// For this reason, our locale must be one RText has been translated
		// into, and we have no guarantee that the user's JVM defaults to one
		// of these locales.  RText will remember the locale last selected by
		// the user and use it the next time it starts.
		setLanguage(prefs==null ? "en" : prefs.language);
		Locale locale = null;
		String language = getLanguage();
		int underscore = language.indexOf('_');
		if (underscore>-1) {
			// Use Locale(lang, country) constructor.
			locale = new Locale(language.substring(0,underscore),
						language.substring(underscore+1));
		}
		else {
			// Use Locale(lang) constructor.
			locale = new Locale(language);
		}
		Locale.setDefault(locale);
		JComponent.setDefaultLocale(locale);
		setLocale(locale);

		// Create the splash screen, if this application has one.
		final SplashScreen splashScreen = createSplashScreen();
		if (splashScreen!=null) {
			splashScreen.setVisible(true);
		}

		// Do the rest of this stuff "later," so that the EDT has time to
		// actually display the splash screen and update it.
		SwingUtilities.invokeLater(new StartupRunnable(title, jarFile,
													splashScreen, prefs));

	}


	/**
	 * Gets called from the OSXAdapter; this method is needed by the Mac OS X
	 * JVM.  This is a hook for the standard Apple application menu.  This
	 * method displays the application's About dialog.
	 *
	 * @see #getAboutDialog()
	 */
	public void about() {
		try {
			getAboutDialog().setVisible(true);
		} catch (Exception e) {
			displayException(e);
		}
	}


	/**
	 * Adds an action to this application's action map.
	 *
	 * @param key The key with which to fetch the action via
	 *        <code>getAction</code>.
	 * @param action The action to add.
	 * @see #createActions
	 * @see #getAction
	 */
	public void addAction(String key, Action action) {
		if (action==null)
			throw new NullPointerException("action cannot be null");
		else if (key==null)
			throw new NullPointerException("key cannot be null");
		if (actionMap==null)
			actionMap = new HashMap();
		actionMap.put(key, action);
	}


	/**
	 * Adds a toolbar to this GUI application.  Note that this should only
	 * be used for toolbars other than the main toolbar.
	 *
	 * @param toolBar The toolbar to add.
	 * @param pos The position at which to add it (one of
	 *        <code>BorderLayout.NORTH</code>, etc.).
	 */
	public void addToolBar(JToolBar toolBar, String pos) {
		int count = toolBarPanels.length;
		JPanel[] newPanels = new JPanel[count+1];
		System.arraycopy(toolBarPanels,0, newPanels,0, count);
		newPanels[count] = new JPanel(new BorderLayout());
		toolBarPanels = newPanels;
		toolBarPanels[count-1].remove(mainContentPanel);
		toolBarPanels[count-1].add(toolBarPanels[count]);
		toolBarPanels[count].add(toolBar, pos);
		toolBarPanels[count].add(mainContentPanel);
	}


	/**
	 * Creates the About dialog this application uses.
	 *
	 * @return The About dialog.
	 * @see #getAboutDialog()
	 */
	protected AboutDialog createAboutDialog() {
		return new AboutDialog(this);
	}


	/**
	 * Creates the actions used by this application.  Implementations should
	 * override this method and add actions used by their application via
	 * <code>addAction</code>.
	 *
	 * @param prefs The preferences for this GUI application.  This may
	 *        contain information such as accelerators, etc.
	 * @see #addAction
	 * @see #getAction
	 */
	protected void createActions(GUIApplicationPreferences prefs) {
	}


	/**
	 * Creates the panel that contains the main content (via the
	 * <code>actualContentPane</code>.  This factory is here so subclasses
	 * such as <code>AbstractPluggableGUIApplication</code> can add
	 * functionality (such as the ability to add GUI plugins to this panel).
	 *
	 * @param actualContentPane The panel that will contain the program's
	 *        content.  This panel should be added to the returned panel.
	 * @return The panel.
	 */
	JPanel createMainContentPanel(JPanel actualContentPane) {
		JPanel mcp = new JPanel(new GridLayout(1,1));
		mcp.add(actualContentPane);
		return mcp;
	}


	/**
	 * Creates and returns the menu bar used in this application.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The menu bar.
	 */
	protected abstract JMenuBar createMenuBar(
								GUIApplicationPreferences prefs);


	/**
	 * Creates and returns the splash screen to display while this GUI
	 * application is loading.
	 *
	 * @return The splash screen.  If <code>null</code> is returned, no splash
	 * screen is displayed.
	 */
	protected abstract SplashScreen createSplashScreen();


	/**
	 * Creates and returns the status bar to be used by this application.  This
	 * method is called in this <code>GUIApplication</code>'s constructor.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The status bar.
	 */
	protected abstract StatusBar createStatusBar(
								GUIApplicationPreferences prefs);


	/**
	 * Creates and returns the toolbar to be used by this application.  This
	 * method is called in this <code>GUIApplication</code>'s constructor.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The toolbar.
	 */
	protected abstract CustomizableToolBar createToolBar(
						GUIApplicationPreferences prefs);


	/**
	 * Displays a dialog box telling the user that an <code>Exception</code>
	 * was thrown.  This method can be overridden to customize how
	 * the user is informed of an <code>Exception</code>.
	 *
	 * @param t The exception/throwable that occurred.
	 */
	public final void displayException(Throwable t) {
		displayException(this, t);
	}


	/**
	 * Displays a dialog box telling the user that an <code>Exception</code>
	 * was thrown.  This method can be overridden to customize how
	 * the user is informed of an <code>Exception</code>.
	 *
	 * @param t The exception/throwable that occurred.
	 * @param desc A short description of the error.  This can be
	 *        <code>null</code>.
	 */
	public final void displayException(Throwable t, String desc) {
		displayException(this, t, desc);
	}


	/**
	 * Displays a dialog box telling the user that an <code>Exception</code>
	 * was thrown.  This method can be overridden to customize how
	 * the user is informed of an <code>Exception</code>.
	 * This version of the method allows a window spawned from the
	 * main GUI application window to be the owner of the displayed
	 * exception.
	 *
	 * @param owner The dialog that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 */
	public final void displayException(Dialog owner, Throwable t) {
		displayException(owner, t, null);
	}


	/**
	 * Displays a dialog box telling the user that an <code>Exception</code>
	 * was thrown.  This method can be overridden to customize how
	 * the user is informed of an <code>Exception</code>.
	 * This version of the method allows a window spawned from the
	 * main GUI application window to be the owner of the displayed
	 * exception.
	 *
	 * @param owner The dialog that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 * @param desc A short description of the error.  This can be
	 *        <code>null</code>.
	 */
	public void displayException(Dialog owner, Throwable t, String desc) {
		ExceptionDialog ed = new ExceptionDialog(owner, t);
		if (desc!=null) {
			ed.setDescription(desc);
		}
		ed.setLocationRelativeTo(owner);
		ed.setVisible(true);
	}


	/**
	 * Displays a dialog box telling the user that an <code>Exception</code>
	 * was thrown.  This method can be overridden to customize how
	 * the user is informed of an <code>Exception</code>.
	 * This version of the method allows a window spawned from the
	 * main GUI application window to be the owner of the displayed
	 * exception.
	 *
	 * @param owner The child frame that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 */
	public final void displayException(Frame owner, Throwable t) {
		displayException(owner, t, null);
	}


	/**
	 * Displays a dialog box telling the user that an <code>Exception</code>
	 * was thrown.  This method can be overridden to customize how
	 * the user is informed of an <code>Exception</code>.
	 * This version of the method allows a window spawned from the
	 * main GUI application window to be the owner of the displayed
	 * exception.
	 *
	 * @param owner The child frame that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 * @param desc A short description of the error.  This can be
	 *        <code>null</code>.
	 */
	public void displayException(Frame owner, Throwable t, String desc) {
		ExceptionDialog ed = new ExceptionDialog(owner, t);
		if (desc!=null) {
			ed.setDescription(desc);
		}
		ed.setLocationRelativeTo(owner);
		ed.setVisible(true);
	}


	/**
	 * Called when the user attempts to close the application, whether from
	 * an "Exit" menu item, closing the main application window, or any other
	 * means.  Applications should override this method to do any cleanup
	 * before the application exits.  You can also prevent the application
	 * from closing based on the application's state in this method.<p>
	 *
	 * The default implementation simply calls <code>System.exit(0)</code>.
	 */
	public void doExit() {
		System.exit(0);
	}


	/**
	 * Returns the About dialog for this application.
	 *
	 * @return The About dialog.
	 * @see org.fife.ui.AboutDialog
	 * @see #createAboutDialog()
	 */
	public AboutDialog getAboutDialog() {
		if (aboutDialog==null) {
			aboutDialog = createAboutDialog();
			aboutDialog.setLocationRelativeTo(this);
		}
		return aboutDialog;
	}


	/**
	 * Returns one of this application's actions.
	 *
	 * @return The action, or <code>null</code> if no action exists for the
	 *         specified key.
	 * @see #addAction
	 * @see #createActions
	 */
	public Action getAction(String key) {
		return (Action)actionMap.get(key);
	}


	/**
	 * Returns the actions of this GUI application as an array.  This array
	 * is unique, so you can sort it, etc.
	 *
	 * @return The actions.  <code>null</code> is returned if the action
	 *         map has not yet been initialized.
	 * @see #getAction
	 */
	public Action[] getActions() {
		if (actionMap==null)
			return null;
		Set keySet = actionMap.keySet();
		int size = keySet.size();
		Action[] array = new Action[size];
		int j = 0;
		for (Iterator i=keySet.iterator(); i.hasNext(); ) {
			array[j++] = (Action)actionMap.get(i.next());
		}
		// Sanity check.
		if (j!=size)
			throw new InternalError("Error in getActions!");
		return array;
	}


	/**
	 * This method is overridden to throw an exception; you should be adding
	 * components via this class's <code>add</code> methods.
	 *
	 * @return Nothing, an exception is thrown.
	 * @throws UnsupportedOperationException always.
	 * @see #setContentPane
	 */
	public Container getContentPane() {
		//throw new UnsupportedOperationException("Use the add() methods!");
		return actualContentPane;
	}


	/**
	 * Returns the Help dialog for this application, or <code>null</code>
	 * if this application does not have a Help dialog.
	 *
	 * @return The Help dialog.
	 */
	public abstract HelpDialog getHelpDialog();


	/**
	 * Returns the directory in which this GUI application is installed (i.e.,
	 * the location if the JAR file containing the main method).
	 *
	 * @return The directory.
	 */
	public String getInstallLocation() {
		return installLocation;
	}


	/**
	 * Returns the language used by this GUI application, in a
	 * <code>Locale</code>-friendly language string; e.g., <code>en</code>
	 * or <code>es</code>.
	 *
	 * @return The language being used by this application.
	 */
	public String getLanguage() {
		return language;
	}


	/**
	 * Returns the manager in charge of any 3rd-party LookAndFeels this
	 * application is aware of.
	 *
	 * @return The manager, or <code>null</code> if there is none.
	 * @see #setLookAndFeelManager(ThirdPartyLookAndFeelManager)
	 */
	public ThirdPartyLookAndFeelManager getLookAndFeelManager() {
		return lafManager;
	}


	/**
	 * Returns the location of the specified jar file in the currently-running
	 * application's classpath.  This can be useful if you wish to know the
	 * location of the installation of the currently-running application.<p>
	 * For example, a Java program running from the executable jar
	 * <code>Foo.jar</code> can call this method with <code>Foo.jar</code> as
	 * the parameter, and the location of the jar file would be returned.  With
	 * this knowledge, along with knowledge of the directory layout of the
	 * application, the programmer can access other files in the installation.
	 *
	 * @param jarFileName The name of the jar file for which to search.
	 * @return The directory in which the jar file resides.
	 */
	public static String getLocationOfJar(String jarFileName) {

		String classPath = System.getProperty("java.class.path");
		int index = classPath.indexOf(jarFileName);

		// A jar file on a classpath MUST be explicitly given; a jar file
		// in a directory, for example, will not be picked up by specifying
		// "-classpath /my/directory/".  So, we can simply search for the
		// jar name in the classpath string, and if it isn't there, it must
		// be in the current directory.
		if (index>-1) {
			int pathBeginning = classPath.lastIndexOf(File.pathSeparator,
												index-1) + 1;
			String loc = classPath.substring(pathBeginning, index);
			File file = new File(loc);
			return file.getAbsolutePath();
		}

		// Otherwise, it must be in the current directory.
		return System.getProperty("user.dir");

	}


	/**
	 * Returns an array of info. for JAR files containing 3rd party Look and
	 * Feels.  These JAR files will be added to the <code>UIManager</code>'s
	 * classpath so that these LnFs can be used in this GUI application.<p>
	 *
	 * For this method to return anything, you must install a
	 * {@link ThirdPartyLookAndFeelManager}.
	 *
	 * @return An array of information on JAR files containing Look and Feels.
	 * @see #setLookAndFeelManager(ThirdPartyLookAndFeelManager)
	 */
	public ExtendedLookAndFeelInfo[] get3rdPartyLookAndFeelInfo() {
		return lafManager!=null ? lafManager.get3rdPartyLookAndFeelInfo() : null;
	}


	/**
	 * Returns this application's Options dialog.
	 *
	 * @return The options dialog, or <code>null</code> if this application
	 *         doesn't have one.  The default implementation returns
	 *         <code>null</code>.
	 */
	public OptionsDialog getOptionsDialog() {
		return null;
	}


	/**
	 * Returns an integer constant representing the OS.  This can be handy for
	 * special case situations such as Mac OS-X (special application
	 * registration) or Windows (allow mixed case, etc.).
	 *
	 * @return An integer constant representing the OS.
	 */
	public int getOS() {
		if (os==0) {
			os = OS_OTHER;
			String osName = System.getProperty("os.name");
			if (osName!=null) { // Should always be true.
				osName = osName.toLowerCase();
				if (osName.indexOf("windows") > -1)
					os = OS_WINDOWS;
				else if (osName.indexOf("mac os x") > -1)
					os = OS_MAC_OSX;
				else if (osName.indexOf("linux") > -1)
					os = OS_LINUX;
				else
					os = OS_OTHER;
			}
		}
		return os;
	}


	/**
	 * Returns the name of the preferences class for this application.  This
	 * class must be a subclass of <code>GUIApplicationPreferences</code>.
	 *
	 * @return The class name, or <code>null</code> if this GUI application
	 *         does not save preferences.
	 */
	protected abstract String getPreferencesClassName();


	/**
	 * Returns the resource bundle associated with this application.
	 *
	 * @return The resource bundle.
	 * @see #getString(String)
	 * @see #getString(String, Object)
	 * @see #getString(String, Object[])
	 */
	public ResourceBundle getResourceBundle() {
		if (resourceBundle==null) {
			resourceBundle = ResourceBundle.getBundle(
									getResourceBundleClassName());
		}
		return resourceBundle;
	}


	/**
	 * Returns the fully-qualified class name of the resource bundle for this
	 * application.  This is used by <code>getResourceBundle</code> to locate
	 * the class.
	 *
	 * @return The fully-qualified class name of the resource bundle.
	 * @see #getResourceBundle
	 */
	public abstract String getResourceBundleClassName();


	/**
	 * Returns the status bar this application is using.
	 *
	 * @return The status bar.
	 * @see #setStatusBar
	 */
	public StatusBar getStatusBar() {
		return statusBar;
	}


	/**
	 * Returns whether the status bar is visible.
	 *
	 * @return Whether the status bar is visible.  If this application does not
	 *         have a status bar, <code>null</code> is returned.
	 * @see #setStatusBarVisible
	 */
	public boolean getStatusBarVisible() {
		return statusBar!=null ? statusBar.isVisible() : false;
	}


	/**
	 * Returns the localized text for the given key.  This method is
	 * equivalent to <code>getResourceBundle().getString(key)</code>.
	 *
	 * @param key The key into the resource bundle.
	 * @return The localized text.
	 * @see #getString(String, Object)
	 * @see #getString(String, Object[])
	 * @see #getResourceBundle()
	 */
	public String getString(String key) {
		return getResourceBundle().getString(key);
	}


	/**
	 * Returns the localized text for the given key.
	 *
	 * @param key The key into the resource bundle.
	 * @param param A parameter in the localized text.
	 * @return The localized text.
	 * @see #getString(String)
	 * @see #getString(String, Object[])
	 * @see #getResourceBundle()
	 */
	public String getString(String key, Object param) {
		return getString(key, new Object[] { param });
	}


	/**
	 * Returns the localized text for the given key.
	 *
	 * @param key The key into the resource bundle.
	 * @param param1 A parameter in the localized text.
	 * @param param2 Another parameter in the localized text.
	 * @return The localized text.
	 * @see #getString(String)
	 * @see #getString(String, Object)
	 * @see #getString(String, Object[])
	 * @see #getResourceBundle()
	 */
	public String getString(String key, Object param1, Object param2) {
		return getString(key, new Object[] { param1, param2 });
	}


	/**
	 * Returns the localized text for the given key.
	 *
	 * @param key The key into the resource bundle.
	 * @param param1 A parameter in the localized text.
	 * @param param2 Another parameter in the localized text.
	 * @param param3 Another parameter in the localized text.
	 * @return The localized text.
	 * @see #getString(String)
	 * @see #getString(String, Object)
	 * @see #getString(String, Object[])
	 * @see #getResourceBundle()
	 */
	public String getString(String key, Object param1, Object param2,
						Object param3) {
		return getString(key, new Object[] { param1, param2, param3 });
	}


	/**
	 * Returns localized text for the given key.
	 *
	 * @param key The key into the resource bundle.
	 * @param params Parameters for the localized text.
	 * @return The localized text.
	 * @see #getString(String)
	 * @see #getString(String, Object)
	 * @see #getResourceBundle()
	 */
	public String getString(String key, Object[] params) {
		String text = getResourceBundle().getString(key);
		return MessageFormat.format(text, params);
	}


	/**
	 * Returns the toolbar this application is using.
	 *
	 * @return The toolbar.
	 * @see #setToolBar
	 */
	public CustomizableToolBar getToolBar() {
		return toolBar;
	}


	/**
	 * Returns whether the toolbar is visible in this application.
	 *
	 * @return Whether the toolbar is visible.  If this application has no
	 *         toolbar, <code>false</code> is returned.
	 * @see #setToolBarVisible
	 */
	public boolean getToolBarVisible() {
		return toolBar!=null ? toolBar.isVisible() : false;
	}


	/**
	 * Returns the version string for this application.
	 *
	 * @return The version string.
	 */
	public abstract String getVersionString();


	/**
	 * Returns true if this application's main window is maximized.
	 *
	 * @return <code>true</code> if this applicaiton's window is maximized,
	 *         or <code>false</code> if it isn't.
	 */
	public boolean isMaximized() {
		return getExtendedState()==MAXIMIZED_BOTH;
	}


	/**
	 * Loads the preferences for this GUI application.  If this application
	 * does not use preferences or something, <code>null</code> is
	 * goes wrong, <code>null</code> is returned.
	 *
	 * @return This application's preferences.
	 */
	public GUIApplicationPreferences loadPreferences() {
		GUIApplicationPreferences prefs = null;
		String prefsClassName = getPreferencesClassName();
		if (prefsClassName!=null) {
			try {
				Class prefsClass = Class.forName(prefsClassName);
				Class[] nullClass = null; // Stops JDK 1.5 varargs warnings.
				Method method = prefsClass.getMethod("loadPreferences",
												nullClass);
				prefs = (GUIApplicationPreferences)method.invoke(
											prefsClass, nullClass);
			} catch (Exception e) {
				displayException(e);
			}
		}
		return prefs;
	}


	/**
	 * Gets called from the OSXAdapter; this method is needed by the Mac OS X
	 * JVM.  This is a hook for the standard Apple application menu.  This
	 * method gets called when we receive an open event from the finder on
	 * Mac OS X, and should be overridden to do whatever makes sense in your
	 * application to "open a file."
	 */
	public abstract void openFile(final String fileName);


	/**
	 * 1.5.2004/pwy: Generic registration with the Mac OS X application menu.  
	 * Checks the platform, then attempts to register with the Apple EAWT.
	 * This method calls OSXAdapter.registerMacOSXApplication() and
	 * OSXAdapter.enablePrefs().
	 * See OSXAdapter.java for the signatures of these methods.
	 */
	private void possibleMacOSXRegistration() {

		if (getOS()==OS_MAC_OSX) {

			try {
				Class osxAdapter = Class.forName(
									"com.apple.osxadapter.OSXAdapter");
				Class[] defArgs = { NativeMacApp.class };
				Method registerMethod = osxAdapter.getDeclaredMethod(
								"registerMacOSXApplication", defArgs);
				if (registerMethod != null) {
					Object[] args = { this };
					registerMethod.invoke(osxAdapter, args);
				}
				// This is slightly gross.  to reflectively access methods
				// with boolean args, use "boolean.class", then pass a
				// Boolean object in as the arg, which apparently gets
				// converted for you by the reflection system.
				defArgs[0] = boolean.class;
				Method prefsEnableMethod =  osxAdapter.getDeclaredMethod(
											"enablePrefs", defArgs);
				if (prefsEnableMethod != null) {
					Object args[] = {Boolean.TRUE};
					prefsEnableMethod.invoke(osxAdapter, args);
				}
			} catch (NoClassDefFoundError e) {
				// This will be thrown first if the OSXAdapter is loaded on
				// a system without the EAWT because OSXAdapter extends
				// ApplicationAdapter in its def
				displayException(e);
			} catch (ClassNotFoundException e) {
				// This shouldn't be reached; if there's a problem with the
				// OSXAdapter we should get the above NoClassDefFoundError
				// first.
				displayException(e);
			} catch (Exception e) {
				displayException(e);
			}

		} // End of if (getOS()==OS_MAC_OSX).

	}


	/**
	 * Gets called from the OSXAdapter; this method is needed by the Mac OS X
	 * JVM.  This is a hook for the standard Apple application menu.  This
	 * method should be overridden to show the Options dialog.
	 */
	public abstract void preferences();


	/**
	 * This is called in the GUI application's constructor.  It is a chance
	 * for subclasses to do initialization of stuff that will be needed
	 * by the class before the appliction is displayed on-screen.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be <code>null</code>.
	 */
	protected abstract void preDisplayInit(GUIApplicationPreferences prefs,
								SplashScreen splashScreen);


	/**
	 * This is called in the GUI application's constructor.  It is a chance
	 * for subclasses to do initialization of stuff that will be needed by
	 * their menu bar before it gets created.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be <code>null</code>.
	 */
	protected abstract void preMenuBarInit(GUIApplicationPreferences prefs,
								SplashScreen splashScreen);


	/**
	 * This is called in the GUI application's constructor.  It is a chance
	 * for subclasses to do initialization of stuff that will be needed by
	 * their status bar before it gets created.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be <code>null</code>.
	 */
	protected abstract void preStatusBarInit(GUIApplicationPreferences prefs,
									SplashScreen splashScreen);


	/**
	 * This is called in the GUI application's constructor.  It is a chance
	 * for subclasses to do initialization of stuff that will be needed by
	 * their toolbar before it gets created.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be <code>null</code>.
	 */
	protected abstract void preToolBarInit(GUIApplicationPreferences prefs,
								SplashScreen splashScreen);


	/**
	 * Called when a window event occurs for this application.
	 *
	 * @param e The window event.
	 */
	protected void processWindowEvent(WindowEvent e) {

		switch (e.getID()) {

			case WindowEvent.WINDOW_CLOSING:
				doExit(); // Closes the application cleanly.
				break;

			case WindowEvent.WINDOW_DEACTIVATED:
				// Make popup menus not stay up when the frame loses focus.
				// This "bug" is fixed in 1.5 JRE's.
				MenuSelectionManager.defaultManager().clearSelectedPath();
				break;

		}

		super.processWindowEvent(e);

	}


	/**
	 * Gets called from the OSXAdapter; this method is needed by the Mac OS X
	 * JVM.  This is a hook for the standard Apple application menu.  This
	 * method calls <code>doExit</code>.
	 *
	 * @see #doExit()
	 */
	public void quit() {
		doExit();
	}


	/**
	 * Registers the status bar to receive notification of menu item armed
	 * events.  This allows it to display a description of the menu item, if
	 * it has one available.
	 */
	private void registerMenuSelectionManagerListener() {

		if (statusBar==null) {
			return;
		}

		MenuSelectionManager.defaultManager().addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (statusBar==null) {
						return;
					}
					String msg = statusBar.getDefaultStatusMessage();
					MenuElement[] path = MenuSelectionManager.
									defaultManager().getSelectedPath();
					if (path.length>0) {
						Component c = path[path.length-1].getComponent();
						if (c instanceof JMenuItem) {
							JMenuItem item = (JMenuItem)c;
							Action a = item.getAction();
							if (a!=null) {
								msg = (String)a.getValue(
												Action.SHORT_DESCRIPTION);
							}
							else {
								String text = item.getAccessibleContext().
											getAccessibleDescription();
								if (text!=null) {
									msg = text;
								}
							}
						}
					}
					statusBar.setStatusMessage(msg);
				}
			}
		);

	}


	/**
	 * This method sets the content pane.  It is overridden so it does not
	 * meddle with the status bar, toolbar, etc.
	 *
	 * @param contentPane The new content pane.
	 * @see #getContentPane
	 */
	public void setContentPane(Container contentPane) {
		if (contentPane!=null && !contentPane.equals(actualContentPane)) {
			if (actualContentPane!=null)
				mainContentPanel.remove(actualContentPane);
			mainContentPanel.add(contentPane);
		}
	}


	/**
	 * Sets the "install location" of this application.
	 *
	 * @param location The directory in which this application is installed.
	 * @see #getInstallLocation
	 */
	private void setInstallLocation(String location) {
		File temp = new File(location);
		if (temp.isDirectory())
			installLocation = temp.getAbsolutePath();
		else
			installLocation = System.getProperty("user.dir");
	}


	/**
	 * Sets the language for this GUI application and all of its dialogs,
	 * UI widgets, etc.<p>
	 *
	 * @param language The language to use.  If <code>null</code>,
	 *        English will be used.
	 */
	public void setLanguage(final String language) {
		this.language = language==null ? "en" : language;
	}


	/**
	 * Sets the utility used to dynamically load 3rd-party LookAndFeels.
	 *
	 * @param manager The utility, or <code>null</code> for none.
	 * @see #getLookAndFeelManager()
	 */
	public void setLookAndFeelManager(ThirdPartyLookAndFeelManager manager) {
		lafManager = manager;
	}


	/**
	 * Sets the status bar to use in this application.  This method fires a
	 * property change of type <code>STATUS_BAR_PROPERTY</code>.
	 *
	 * @param statusBar The status bar to use.
	 * @see #getStatusBar
	 */
	public void setStatusBar(StatusBar statusBar) {
		if (statusBar!=null && !statusBar.equals(this.statusBar)) {
			StatusBar old = this.statusBar;
			if (old!=null)
				contentPane.remove(old);
			this.statusBar = statusBar;
			contentPane.add(statusBar, STATUS_BAR_LOCATION);
			firePropertyChange(STATUS_BAR_PROPERTY, old, statusBar);
		}
	}


	/**
	 * Sets whether the status bar is visible.  This method fires a property
	 * change of type <code>STATUS_BAR_VISIBLE_PROPERTY</code>.
	 *
	 * @param visible Whether the status bar is to be visible.
	 * @see #getStatusBarVisible
	 */
	public void setStatusBarVisible(boolean visible) {
		if (statusBar!=null && statusBar.isVisible()!=visible) {
			statusBar.setVisible(visible);
			firePropertyChange(STATUS_BAR_VISIBLE_PROPERTY,
											!visible, visible);
		}
	}


	/**
	 * Sets the toolbar used by this GUI application.  This method fires a
	 * property change of type <code>TOOL_BAR_PROPERTY</code>.
	 *
	 * @param toolBar The toolbar to use.
	 * @see #getToolBar
	 */
	public void setToolBar(CustomizableToolBar toolBar) {
		if (toolBar!=null && !toolBar.equals(this.toolBar)) {
			CustomizableToolBar old = this.toolBar;
			if (old!=null)
				toolBarPanels[0].remove(old);
			this.toolBar = toolBar;
			toolBarPanels[0].add(toolBar, TOOL_BAR_LOCATION);
			firePropertyChange(TOOL_BAR_PROPERTY, old, toolBar);
		}
	}


	/**
	 * Sets whether the toolbar used by this GUI application is visible.
	 * This method fires a property change of type
	 * <code>TOOL_BAR_VISIBLE_PROPERTY</code>.
	 *
	 * @param visible Whether the toolbar should be visible.
	 * @see #getToolBarVisible
	 */
	public void setToolBarVisible(boolean visible) {
		if (toolBar!=null && toolBar.isVisible()!=visible) {
			toolBar.setVisible(visible);
			firePropertyChange(TOOL_BAR_VISIBLE_PROPERTY, !visible,
												visible);
		}
	}


	/**
	 * Actually creates the GUI.  This is called after the splash screen is
	 * displayed via <code>SwingUtilities#invokeLater()</code>.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private class StartupRunnable implements Runnable {

		private String title;
		private String jarFile;
		private SplashScreen splashScreen;
		private GUIApplicationPreferences prefs;

		public StartupRunnable(String title, String jarFile,
								SplashScreen splashScreen,
								GUIApplicationPreferences prefs) {
			this.splashScreen = splashScreen;
			this.prefs = prefs;
			this.title = title;
			this.jarFile = jarFile;
		}

		public void run() {

			setTitle(title);
			setInstallLocation(getLocationOfJar(jarFile));

			// contentPane contains the status bar to the south and toolBarPane
			// in the center (which contains everything else).
			contentPane = new JPanel(new BorderLayout());
			AbstractGUIApplication.super.setContentPane(contentPane);

			// Toolbar panels each contain 1 toolbar, with the last one
			// also containing the mainContentPanel in its center.  Each
			// increasing index of a toolbar panel is contained in the
			// lower-indexed one.
			toolBarPanels = new JPanel[1];
			toolBarPanels[0] = new JPanel(new BorderLayout());
			contentPane.add(toolBarPanels[0]);

			// actualContentPane contains the actual user content added via
			// add().
			actualContentPane = new JPanel(new BorderLayout());

			// mainContentPanel contains both actualContentPane and possible
			// GUIPlugins on any of the 4 sides if we're an instance of
			// AbstractPluggableGUIApplication.
			mainContentPanel = createMainContentPanel(actualContentPane);
			toolBarPanels[0].add(mainContentPanel);

			if (lafManager!=null) {
				// We MUST add our class loader capable of loading 3rd party
				// LaF jars as this property as it is used internally by
				// Swing when loading classes.  Any 3rd party jars aren't
				// on our classpath, so Swing won't pick them up without it.
				// It must be set AFTER any UIManager.setLookAndFeel() call as
				// that call resets this property to null (for the new LnF).
				// We don't actually change the LAF in accordance with user
				// preferences here as we assume the application did that
				// before instantiating us (required by some LAFs, such as
				// Substance).
				ClassLoader cl = lafManager.getLAFClassLoader();
				UIManager.getLookAndFeelDefaults().put("ClassLoader", cl);
			}

			createActions(prefs);

			// Create the status bar.
			preStatusBarInit(prefs, splashScreen);
			//long start = System.currentTimeMillis();
			StatusBar statusBar = createStatusBar(prefs);
			//System.err.println("createStatusBar: " + (System.currentTimeMillis()-start));
			setStatusBar(statusBar);

			// Create the toolbar.
			preToolBarInit(prefs, splashScreen);
			//start = System.currentTimeMillis();
			CustomizableToolBar toolBar = createToolBar(prefs);
			//System.err.println("createToolBar: " + (System.currentTimeMillis()-start));
			setToolBar(toolBar);

			// Create the menu bar.
			preMenuBarInit(prefs, splashScreen);
			//long start = System.currentTimeMillis();
			JMenuBar menuBar = createMenuBar(prefs);
			// Must set orientation of menuBar separately as it's done later.
			ComponentOrientation o = ComponentOrientation.
										getOrientation(getLocale());
			menuBar.applyComponentOrientation(o);
			//System.err.println("createMenuBar: " + (System.currentTimeMillis()-start));
			setJMenuBar(menuBar);

			// Do the rest of the subclass's custom initialization.
			preDisplayInit(prefs, splashScreen);

			// Register status bar to receive notice on menu item changes,
			// in case we have descriptions of them to display.
			registerMenuSelectionManagerListener();

			// 1.5.2004/pwy: If running on a Mac OS X system, enable the
			// application menu and other Apple specific functions.
			possibleMacOSXRegistration();

			// Set location/appearance properties.
			Toolkit.getDefaultToolkit().setDynamicLayout(true);
			pack();
			if (prefs!=null) {
				if (prefs.location!=null) {
					setLocation(prefs.location);
				}
				else {
					setLocationRelativeTo(null);
				}
				if (prefs.size==null || prefs.size.equals(new Dimension(-1,-1)))
					setExtendedState(MAXIMIZED_BOTH);
				else
					setSize(prefs.size);
				setToolBarVisible(prefs.toolbarVisible);
				setStatusBarVisible(prefs.statusBarVisible);
			}
			else {
				setToolBarVisible(true);
				setStatusBarVisible(true);
				setLocationRelativeTo(null);
			}

			ComponentOrientation orientation = ComponentOrientation.
										getOrientation(getLocale());
			applyComponentOrientation(orientation);

			// Clean up the splash screen if necessary.
			if (splashScreen!=null) {
				splashScreen.setVisible(false);
				splashScreen.dispose();
			}

			setVisible(true);

		}

	}


}