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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.swing.*;

import org.fife.io.IOUtil;
import org.fife.ui.*;
import org.fife.help.HelpDialog;
import org.fife.ui.SplashScreen;
import org.fife.ui.app.icons.IconGroup;
import org.fife.ui.app.prefs.AppPrefs;
import org.fife.ui.app.themes.NativeTheme;


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
 *   <li>Necessary Mac OS X hooks (used on any OS with OS-specific Help menu
 *       items, Options/Preferences menu items, etc.).
 * </ul>
 *
 * @author Robert Futrell
 * @version 0.6
 * @param <P> The type of preferences for this application.
 * @see AbstractPluggableGUIApplication
 */
public abstract class AbstractGUIApplication<P extends AppPrefs> extends JFrame
							implements GUIApplication {

	/**
	 * This property is fired whenever the icon style changes.
	 */
	public static final String ICON_STYLE_PROPERTY		= "iconStyle";

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
	 * If a resource with this name is found on the classpath, it is
	 * assumed to contain the build date of this application, as an
	 * ISO-8601 string.
	 */
	public static final String BUILD_DATE_RESOURCE = "/build-date.txt";

	private AppContext<? extends AbstractGUIApplication<P>, P> context;

	/**
	 * The About dialog.
	 */
	private JDialog aboutDialog;

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
	private ActionRegistry actions;

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
	 * The current application theme.
	 */
	private AppTheme theme;

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
	protected Container actualContentPane;

	/**
	 * The current icon group the application is using.
	 */
	private IconGroup iconGroup;

	private static final String STATUS_BAR_LOCATION	= BorderLayout.SOUTH;
	private static final String TOOL_BAR_LOCATION	= BorderLayout.NORTH;


	/**
	 * Constructor.
	 *
	 * @param context The application context.
	 * @param title The title for this frame.
	 * @param prefs The preferences with which to initialize this application.
	 */
	public AbstractGUIApplication(AppContext<? extends AbstractGUIApplication<P>, P> context, String title, P prefs) {
		initialize(context, title, prefs);
	}


	/**
	 * Initializes this GUI application.  This is separate from the
	 * constructors because we need the ability to call
	 * <code>loadPreferences</code> to get the preferences with which to
	 * initialize this class.
	 *
	 * @param title The title for this frame.
	 * @param prefs The preferences with which to initialize this application.
	 */
	private void initialize(AppContext<? extends AbstractGUIApplication<P>, P> context, String title, P prefs) {

		this.context = context;
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		setTheme(prefs.appTheme != null ? prefs.appTheme : NativeTheme.NAME);

		// Set up the localization stuff.
		// NOTE: We default to English instead of using the JVM's default
		// locale as we want to be sure all of RText's UI uses the same l10n.
		// For this reason, our locale must be one RText has been translated
		// into, and we have no guarantee that the user's JVM defaults to one
		// of these locales.  RText will remember the locale last selected by
		// the user and use it the next time it starts.
		setLanguage(prefs==null ? "en" : prefs.language);
		Locale locale;
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

		actions = new ActionRegistry();

		//long start = System.currentTimeMillis();
		if (title != null) {
			setTitle(title);
		}
		setInstallLocation(getLocationOfJar());
		//System.err.println(("setInstallLocation: " + (System.currentTimeMillis() - start)));

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
		//System.err.println("toolbarPanels: " + (System.currentTimeMillis() - start));

		preCreateActions(prefs, splashScreen);
		createActions(prefs);

		// Create the status bar.
		preStatusBarInit(prefs, splashScreen);
		StatusBar statusBar = createStatusBar(prefs);
		//System.err.println("createStatusBar: " + (System.currentTimeMillis()-start));
		setStatusBar(statusBar);

		// Create the toolbar.
		preToolBarInit(prefs, splashScreen);
		CustomizableToolBar toolBar = createToolBar(prefs);
		//System.err.println("createToolBar: " + (System.currentTimeMillis()-start));
		setToolBar(toolBar);

		// Create the menu bar.
		preMenuBarInit(prefs, splashScreen);
		JMenuBar menuBar = createMenuBar(prefs);
		// Must set orientation of menuBar separately as it's done later.
		ComponentOrientation o = ComponentOrientation.
			getOrientation(getLocale());
		menuBar.applyComponentOrientation(o);
		//System.err.println("createMenuBar: " + (System.currentTimeMillis()-start));
		setJMenuBar(menuBar);

		// Do the rest of the subclass's custom initialization.
		preDisplayInit(prefs, splashScreen);
		//System.err.println("preDisplayInit: " + (System.currentTimeMillis()-start));

		// Register status bar to receive notice on menu item changes,
		// in case we have descriptions of them to display.
		registerMenuSelectionManagerListener();

		addSystemHooks();

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

		applyComponentOrientation(o);
		//System.err.println("Everything else: " + (System.currentTimeMillis()-start));

		// Clean up the splash screen if necessary.
		if (splashScreen!=null) {
			splashScreen.setVisible(false);
			splashScreen.dispose();
		}

	}


	@Override
	public void addAction(String key, Action action) {
		actions.addAction(key, action);
	}


	private void addSystemHooks() {

		try {

			Desktop desktop = Desktop.getDesktop();

			if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
				desktop.setAboutHandler(e -> {
					try {
						getAboutDialog().setVisible(true);
					} catch (Exception ex) {
						displayException(ex);
					}
				});
			}

			if (desktop.isSupported(Desktop.Action.APP_OPEN_FILE)) {
				desktop.setOpenFileHandler(e -> {
					for (File file : e.getFiles()) {
						openFile(file);
					}
				});
			}

			if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
				desktop.setPreferencesHandler(e -> preferences());
			}

			if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
				desktop.setQuitHandler((e, response) -> {
					response.cancelQuit(); // Let our application handle quitting
					doExit();
				});
			}
		} catch (Exception e) {
			displayException(e);
		}
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
	protected JDialog createAboutDialog() {
		return new AboutDialog(this);
	}


	/**
	 * Creates the actions used by this application.  Implementations should
	 * override this method and add actions used by their application via
	 * <code>addAction</code>.
	 *
	 * @param prefs The preferences for this GUI application.  This may
	 *        contain information such as accelerators, etc.
	 * @see #addAction(String, Action)
	 * @see #getAction(String)
	 */
	protected void createActions(P prefs) {
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
	JPanel createMainContentPanel(Container actualContentPane) {
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
	protected abstract JMenuBar createMenuBar(P prefs);


	/**
	 * Creates and returns the splash screen to display while this GUI
	 * application is loading.
	 *
	 * @return The splash screen.  If <code>null</code> is returned, no splash
	 *         screen is displayed.
	 */
	protected abstract SplashScreen createSplashScreen();


	/**
	 * Creates and returns the status bar to be used by this application.  This
	 * method is called in this <code>GUIApplication</code>'s constructor.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The status bar.
	 */
	protected abstract StatusBar createStatusBar(P prefs);


	/**
	 * Creates and returns the toolbar to be used by this application.  This
	 * method is called in this <code>GUIApplication</code>'s constructor.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The toolbar.
	 */
	protected abstract CustomizableToolBar createToolBar(P prefs);


	/**
	 * Displays a dialog box telling the user that an <code>Exception</code>
	 * was thrown.  This method can be overridden to customize how
	 * the user is informed of an <code>Exception</code>.
	 *
	 * @param t The exception/throwable that occurred.
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public JDialog getAboutDialog() {
		if (aboutDialog==null) {
			aboutDialog = createAboutDialog();
			aboutDialog.setLocationRelativeTo(this);
		}
		return aboutDialog;
	}


	@Override
	public Action getAction(String key) {
		return actions.getAction(key);
	}


	@Override
	public SortedSet<String> getActionKeys() {
		return actions.getActionKeys();
	}


	@Override
	public Action[] getActions() {
		return actions.getActions();
	}


	/**
	 * Returns the active, unsaved state of preferences of this application.
	 *
	 * @return This application's unsaved preferences state, or {@code null}
	 *          if this application does not store preferences.
	 * @see #savePreferences()
	 */
	@SuppressWarnings("unchecked")
	public P getActivePreferencesState() {

		P prefs = null;

		try {
			prefs = ((AppContext<AbstractGUIApplication<P>, P>)context).
				getActivePreferencesState(this);
		} catch (IOException ioe) { // Never happens
			displayException(ioe);
		}

		return prefs;
	}


	/**
	 * Returns the context for this application.  Typically doesn't have to be called.
	 *
	 * @return The application context.
	 */
	protected AppContext<? extends AbstractGUIApplication<P>, P> getAppContext() {
		return context;
	}


	/**
	 * Returns the available application themes.
	 *
	 * @return The available application themes.
	 */
	public List<AppTheme> getAppThemes() {
		return context.getAvailableAppThemes();
	}


	/**
	 * Returns the main content pane of the application.  Applications should
	 * add components to this panel.
	 *
	 * @return The application's main content pane.
	 * @see #setContentPane(Container)
	 */
	@Override
	public Container getContentPane() {
		return actualContentPane;
	}


	/**
	 * Returns the Help dialog for this application, or <code>null</code>
	 * if this application does not have a Help dialog.
	 *
	 * @return The Help dialog.
	 */
	@Override
	public abstract HelpDialog getHelpDialog();


	/**
	 * Returns the icon group being used by the application.
	 *
	 * @return The icon group.
	 * @see #setIconGroup(IconGroup)
	 */
	@Override
	public IconGroup getIconGroup() {
		return iconGroup;
	}


	/**
	 * Returns the directory in which this GUI application is installed (i.e.,
	 * the location if the JAR file containing the main method).
	 *
	 * @return The directory.
	 */
	@Override
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
	@Override
	public String getLanguage() {
		return language;
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
	 * @return The directory in which the jar file resides.
	 */
	public static String getLocationOfJar() {

		String path = AbstractGUIApplication.class.getProtectionDomain().
				getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

		return new File(decodedPath).getParent();

	}


	/**
	 * Returns the build date of this application.  First checks for a resource
	 * on the classpath named {@link #BUILD_DATE_RESOURCE}.  If that isn't
	 * found, an attempt is made to read a {@code Build-Date} attribute from
	 * the application's {@code Manifest.mf} file.  Note that the latter
	 * approach will fail when called in a jlink-generated executable.
	 *
	 * @return The build date, or {@code null} if it cannot be determined.
	 */
	public Date getBuildDate() {

		Date buildDate = null;

		// First check for a 'build-date.txt' file, since the backup code below
		// doesn't work when an app is wrapped in a a jlink-generated executable
		InputStream in = getClass().getResourceAsStream(BUILD_DATE_RESOURCE);
		if (in != null) {
			try {
				buildDate = Date.from(Instant.parse(IOUtil.readFully(in).trim()));
			} catch (IOException ioe) { // Should never happen
				displayException(ioe);
			}
			return buildDate;
		}

		// Assumption here is the main class is in the "main" jar, with a Build-Date manifest entry
		String path = getClass().getProtectionDomain().
			getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

		File file = new File(decodedPath);
		if (file.isFile()) {

			try (JarInputStream jin = new JarInputStream(new BufferedInputStream(
				new FileInputStream(file)))) {

				Manifest mf = jin.getManifest();
				String temp = mf.getMainAttributes().getValue("Build-Date");
				if (temp != null) {
					buildDate = Date.from(Instant.parse(temp));
				}
			} catch (IOException | DateTimeParseException e) {
				// Do nothing (comment for Sonar) - we just aren't in a built jar
			}
		}

		return buildDate;
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


	@Override
	public OS getOS() {
		return OS.get();
	}


	/**
	 * Returns the resource bundle associated with this application.
	 *
	 * @return The resource bundle.
	 * @see #getString(String, Object...)
	 */
	@Override
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
	@Override
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
	@Override
	public boolean getStatusBarVisible() {
		return statusBar != null && statusBar.isVisible();
	}


	/**
	 * Returns localized text for the given key.
	 *
	 * @param key The key into the resource bundle.
	 * @param params Any (optional) parameters for the localized text.
	 * @return The localized text.
	 * @see #getResourceBundle()
	 */
	public String getString(String key, Object... params) {
		String text = getResourceBundle().getString(key);
		return MessageFormat.format(text, params);
	}


	@Override
	public AppTheme getTheme() {
		return theme;
	}


	/**
	 * Returns the toolbar this application is using.
	 *
	 * @return The toolbar.
	 * @see #setToolBar
	 */
	@Override
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
	@Override
	public boolean getToolBarVisible() {
		return toolBar != null && toolBar.isVisible();
	}


	/**
	 * Returns the version string for this application.
	 *
	 * @return The version string.
	 */
	@Override
	public abstract String getVersionString();


	/**
	 * Returns true if this application's main window is maximized.
	 *
	 * @return <code>true</code> if this application's window is maximized,
	 *         or <code>false</code> if it isn't.
	 */
	@Override
	public boolean isMaximized() {
		return getExtendedState()==MAXIMIZED_BOTH;
	}


	/**
	 * Loads saved (customized) shortcuts for this application's actions from
	 * a file.  Implementations are expected to call this method after creating
	 * their actions via {@link #createActions(AppPrefs)} to
	 * restore any user customizations to shortcuts (assuming the application
	 * allows them).<p>
	 *
	 * If an IO error occurs, an error is displayed to the user.
	 *
	 * @param file The file to load from.
	 * @see #saveActionShortcuts(File)
	 */
	protected void loadActionShortcuts(File file) {
		try {
			actions.loadShortcuts(file);
		} catch (IOException ioe) {
			displayException(ioe);
		}
	}


	/**
	 * Opens a file.  Called on OS X when system hooks are enabled.
	 *
	 * @param file The file to open.
	 */
	public abstract void openFile(File file);


	/**
	 * Called on OS X when the user opens the Options dialog.
	 */
	public abstract void preferences();


	/**
	 * This is called early in the application's lifecycle, before
	 * {@link #createActions(AppPrefs)} is called.  This gives the
	 * application a chance to do any initialization necessary for
	 * the actions to be properly instantiated.<p>
	 *
	 * The default implementation does nothing.  Subclasses can override.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be {@code null}.
	 * @see #preDisplayInit(AppPrefs, SplashScreen)
	 * @see #preMenuBarInit(AppPrefs, SplashScreen)
	 * @see #preStatusBarInit(AppPrefs, SplashScreen)
	 * @see #preToolBarInit(AppPrefs, SplashScreen)
	 */
	protected void preCreateActions(P prefs, SplashScreen splashScreen) {
		 // Do nothing (comment for Sonar)
	}

	/**
	 * A lifecycle hook for subclasses to do any initialization of stuff that
	 * is needed before the application is displayed on-screen.  This lifecycle
	 * hook is run after the menu bar, status bar, and toolbar are all created.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be {@code null}.
	 * @see #preCreateActions(AppPrefs, SplashScreen)
	 * @see #preMenuBarInit(AppPrefs, SplashScreen)
	 * @see #preStatusBarInit(AppPrefs, SplashScreen)
	 * @see #preToolBarInit(AppPrefs, SplashScreen)
	 */
	protected abstract void preDisplayInit(P prefs, SplashScreen splashScreen);


	/**
	 * A lifecycle hook for subclasses to do any initialization of stuff that
	 * will be needed by their menu bar before it gets created.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be {@code null}.
	 * @see #preCreateActions(AppPrefs, SplashScreen)
	 * @see #preDisplayInit(AppPrefs, SplashScreen)
	 * @see #preStatusBarInit(AppPrefs, SplashScreen)
	 * @see #preToolBarInit(AppPrefs, SplashScreen)
	 */
	protected abstract void preMenuBarInit(P prefs, SplashScreen splashScreen);


	/**
	 * A lifecycle hook for subclasses to do any initialization of stuff that
	 * is needed by their status bar before it gets created.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be {@code null}.
	 * @see #preCreateActions(AppPrefs, SplashScreen)
	 * @see #preDisplayInit(AppPrefs, SplashScreen)
	 * @see #preMenuBarInit(AppPrefs, SplashScreen)
	 * @see #preToolBarInit(AppPrefs, SplashScreen)
	 */
	protected abstract void preStatusBarInit(P prefs,SplashScreen splashScreen);


	/**
	 * A lifecycle hook for subclasses to do any initialization of stuff that
	 * is needed by their toolbar before it gets created.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be {@code null}.
	 * @see #preCreateActions(AppPrefs, SplashScreen)
	 * @see #preDisplayInit(AppPrefs, SplashScreen)
	 * @see #preMenuBarInit(AppPrefs, SplashScreen)
	 * @see #preStatusBarInit(AppPrefs, SplashScreen)
	 */
	protected abstract void preToolBarInit(P prefs, SplashScreen splashScreen);


	/**
	 * Called when a window event occurs for this application.
	 *
	 * @param e The window event.
	 */
	@Override
	protected void processWindowEvent(WindowEvent e) {

		// Closes the application cleanly.
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			doExit();
		}

		super.processWindowEvent(e);

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
			e -> {
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
		);

	}


	/**
	 * Makes all actions use default accelerators, if possible.  This is useful
	 * for applications that let users set shortcuts for actions (menu items,
	 * etc.).
	 *
	 * @see StandardAction
	 */
	public void restoreDefaultAccelerators() {
		for (Action a : getActions()) {
			if (a instanceof StandardAction) {
				((StandardAction)a).restoreDefaultAccelerator();
			}
		}
	}


	/**
	 * <p>
	 * Saves the (customized) shortcuts for this application's actions from a
	 * file.  Implementations are expected to call this method when shutting
	 * down, to save any user customizations to shortcuts (assuming the
	 * application allows them).</p>
	 *
	 * <p>
	 * If an IO error occurs, an error is displayed to the user.
	 * </p>
	 *
	 * @param file The file to save to.
	 * @see #loadActionShortcuts(File)
	 */
	protected void saveActionShortcuts(File file) {
		try {
			actions.saveShortcuts(file);
		} catch (IOException ioe) {
			displayException(ioe);
		}
	}


	/**
	 * Saves this application's preferences.
	 *
	 * @see #getActivePreferencesState()
	 */
	@SuppressWarnings("unchecked")
	public void savePreferences() {

		try {
			((AppContext<AbstractGUIApplication<P>, P>)context).savePreferences(this);
		} catch (IOException ioe) {
			displayException(ioe);
		}
	}


	/**
	 * This method sets the content pane.  It is overridden so it does not
	 * meddle with the status bar, toolbar, etc.
	 *
	 * @param contentPane The new content pane.
	 * @see #getContentPane()
	 */
	@Override
	public void setContentPane(Container contentPane) {
		if (contentPane!=null && !contentPane.equals(actualContentPane)) {
			if (actualContentPane!=null) {
				mainContentPanel.remove(actualContentPane);
			}
			mainContentPanel.add(contentPane);
			actualContentPane = contentPane;
		}
	}


	/**
	 * Sets the icon group for the application.<p>
	 *
	 * This method fires a property change event of type
	 * {@code ICON_STYLE_PROPERTY}.
	 *
	 * @param iconGroup The icon group.
	 * @see #getIconGroup()
	 */
	public void setIconGroup(IconGroup iconGroup) {
		if (iconGroup != this.iconGroup) {
			IconGroup old = this.iconGroup;
			this.iconGroup = iconGroup;
			updateIconsForNewIconGroup(iconGroup);
			firePropertyChange(ICON_STYLE_PROPERTY, old, iconGroup);
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
	@Override
	public void setLanguage(String language) {
		this.language = language==null ? "en" : language;
	}


	/**
	 * Makes the menu bar and tool bar (if defined) drag the entire window around then the user clicks and
	 * drags them.
	 *
	 * @see ComponentMover
	 */
	public void setWindowDraggableByMenuBarAndToolBar() {

		List<JComponent> components = new ArrayList<>();

		if (getJMenuBar() != null) {
			components.add(getJMenuBar());
		}
		if (getToolBar() != null) {
			components.add(getToolBar());
		}

		if (!components.isEmpty()) {
			ComponentMover mover = new ComponentMover(this, components.toArray(new JComponent[0]));
			mover.setChangeCursor(false);
		}
	}

	/**
	 * Sets the status bar to use in this application.  This method fires a
	 * property change of type <code>STATUS_BAR_PROPERTY</code>.
	 *
	 * @param statusBar The status bar to use.
	 * @see #getStatusBar
	 */
	@Override
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
	@Override
	public void setStatusBarVisible(boolean visible) {
		if (statusBar!=null && statusBar.isVisible()!=visible) {
			statusBar.setVisible(visible);
			firePropertyChange(STATUS_BAR_VISIBLE_PROPERTY,
											!visible, visible);
		}
	}


	/**
	 * Installs a theme by name.  Does nothing if no such theme is known.
	 *
	 * @param newTheme The theme to install.
	 * @see #setTheme(AppTheme)
	 */
	public void setTheme(String newTheme) {
		setTheme(getAppThemes().stream()
			.filter(t -> newTheme.equals(t.getName()))
			.findFirst()
			.orElse(getAppThemes().get(0))); // Fall back on something known to exist
	}


	/**
	 * Sets the theme used by this application.
	 *
	 * @param newTheme The theme to install.  This cannot be {@code null}.
	 * @see #setThemeAdditionalProperties(AppTheme)
	 * @see #getTheme()
	 * @see #setTheme(String)
	 */
	@Override
	public void setTheme(AppTheme newTheme) {

		if (theme == null || !newTheme.getName().equals(theme.getName())) {

			theme = newTheme;
			String currentLookAndFeel = UIManager.getLookAndFeel().getClass().getName();

			// LookAndFeel always refreshes the app, so we defensively check here.
			if (!newTheme.getLookAndFeel().equals(currentLookAndFeel)) {
				try {

					UIManager.setLookAndFeel(newTheme.getLookAndFeel());
				} catch (ClassNotFoundException | InstantiationException |
					IllegalAccessException | UnsupportedLookAndFeelException e) {
					displayException(e);
					return;
				}
			}

			newTheme.installIntoUiDefaults(); // Call before updateComponentTreeUI()
			SwingUtilities.updateComponentTreeUI(this); // Sometimes needed, e.g. FlatLaF => Windows
			updateLookAndFeel(UIManager.getLookAndFeel());
			setThemeAdditionalProperties(newTheme);
		}
	}


	/**
	 * Installs any application-specific properties.  The {@code LookAndFeel}
	 * has already been updated when this method is called.<p>
	 *
	 * The default implementation does nothing.  Subclasses can override.
	 *
	 * @param theme The theme being installed.
	 * @see #setTheme(AppTheme)
	 */
	protected void setThemeAdditionalProperties(AppTheme theme) {
		// Do nothing - subclasses can override
	}


	/**
	 * Sets the toolbar used by this GUI application.  This method fires a
	 * property change of type <code>TOOL_BAR_PROPERTY</code>.
	 *
	 * @param toolBar The toolbar to use.
	 * @see #getToolBar
	 */
	@Override
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
	@Override
	public void setToolBarVisible(boolean visible) {
		if (toolBar!=null && toolBar.isVisible()!=visible) {
			toolBar.setVisible(visible);
			firePropertyChange(TOOL_BAR_VISIBLE_PROPERTY, !visible,
												visible);
		}
	}


	/**
	 * Updates the application's icons due to an icon group change.
	 * The default implementation does nothing.  Subclasses can
	 * override.
	 *
	 * @param iconGroup The new icon group.
	 * @see #setIconGroup(IconGroup)
	 */
	protected void updateIconsForNewIconGroup(IconGroup iconGroup) {
		// Do nothing - subclasses can override
	}


	/**
	 * Updates the look and feel for all components and windows in
	 * this application instance.  This method is called by
	 * {@code setTheme(AppTheme)}, after
	 * {@code UIManager.setLookAndFeel(lnf)} has been called.<p>
	 *
	 * Subclasses should override this method to update any child
	 * dialogs or windows, and be sure to call the super implementation
	 * as well.
	 *
	 * @param lnf The new look and feel.  You can usually ignore this
	 *        parameter, as the LookAndFeel has already been installed.
	 * @see #setTheme(AppTheme)
	 */
	public void updateLookAndFeel(LookAndFeel lnf) {
		if (aboutDialog!=null) {
			SwingUtilities.updateComponentTreeUI(aboutDialog);
		}
	}


	/**
	 * Action to show an application's Options dialog.
	 *
	 * @param <T> The parent application class type.
	 */
	public static class OptionsAction<T extends AbstractGUIApplication<?>>
			extends AppAction<T> {

		/**
		 * Creates an instance of this action.
		 *
		 * @param app The application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public OptionsAction(T app, String nameKey) {
            super(app, nameKey);
        }

		/**
		 * Creates an instance of this action.
		 *
		 * @param app The application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 * @param icon The name of the icon resource for this action.
		 */
		public OptionsAction(T app, String nameKey, String icon) {
            super(app, nameKey, icon);
        }

		/**
		 * Creates an instance of this action.
		 *
		 * @param app The application that owns this action.
		 * @param msg The resource bundle to localize from.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public OptionsAction(T app, ResourceBundle msg, String nameKey) {
			super(app, msg, nameKey);
		}

        @Override
		public void actionPerformed(ActionEvent e) {

            AbstractGUIApplication<?> app = getApplication();
            app.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            OptionsDialog dialog;

            try {
                dialog = app.getOptionsDialog();
                dialog.initialize();
                dialog.pack();
                dialog.setLocationRelativeTo(app);
            } finally { // Just in case something goes horribly awry
                app.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            dialog.setVisible(true);
        }
    }


}
