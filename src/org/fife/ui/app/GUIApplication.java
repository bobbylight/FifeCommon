/*
 * 11/20/2004
 *
 * GUIApplication.java - An interface for a basic GUI application.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.SortedSet;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JMenuBar;

import org.fife.help.HelpDialog;
import org.fife.ui.CustomizableToolBar;
import org.fife.ui.StatusBar;


/**
 * An interface for a generic GUI application.  Your Swing application can
 * override {@link org.fife.ui.app.AbstractGUIApplication} to have some of its
 * basic code already implemented.<p>
 *
 * This class provides the framework for a GUI application having the following
 * features:
 * <ul>
 *   <li>A menu bar.</li>
 *   <li>A customizable toolbar.</li>
 *   <li>A status bar.</li>
 *   <li>A "Help" dialog.</li>
 *   <li>An "About" dialog.</li>
 *   <li>Easy localization.</li>
 *   <li>A way to capture <code>Exceptions</code> and report them to
 *       the user.</li>
 *   <li>An easy to organize/maintain the actions associated with the
 *       application's menu bar, toolbar, etc.</li>
 * </ul>
 *
 * @author Robert Futrell
 * @version 0.5
 * @see AbstractGUIApplication
 * @see AbstractPluggableGUIApplication
 */
public interface GUIApplication extends GUIApplicationConstants {


	/**
	 * Adds an action to this application's action map.
	 *
	 * @param key The key with which to fetch the action via
	 *        <code>getAction</code>.
	 * @param action The action to add.
	 * @see #getAction(String)
	 */
	public void addAction(String key, Action action);


	/**
	 * This method should be overridden to convey to the user that an
	 * Exception occurred in some way, for example, in a dialog box.
	 *
	 * @param t The exception/throwable that occurred.
	 */
	public void displayException(Throwable t);


	/**
	 * This method should be overridden to convey to the user that an
	 * Exception occurred in some way, for example, in a dialog box.
	 * This version is useful if a child dialog of the GUI application
	 * threw the Exception.
	 *
	 * @param dialog The child dialog that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 */
	public void displayException(Dialog dialog, Throwable t);


	/**
	 * This method should be overridden to convey to the user that an
	 * Exception occurred in some way, for example, in a dialog box.
	 * This version is useful if a child frame of the GUI application
	 * threw the Exception.
	 *
	 * @param frame The child frame that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 */
	public void displayException(Frame frame, Throwable t);


	/**
	 * Called when the user attempts to close the application, whether from
	 * an "Exit" menu item, closing the main application window, or any other
	 * means.  Applications should override this method to do any cleanup
	 * before the application exits.  You can also prevent the application
	 * from closing based on the application's state in this method.
	 */
	public void doExit();


	/**
	 * Returns the About dialog for this application.
	 *
	 * @return The About dialog.
	 * @see org.fife.ui.AboutDialog
	 */
	public JDialog getAboutDialog();


	/**
	 * Returns one of this application's actions.
	 *
	 * @return The action, or <code>null</code> if no action exists for the
	 *         specified key.
	 * @see #addAction(String, Action)
	 * @see #getActions()
	 */
	public Action getAction(String key);



	/**
	 * Returns the keys of all actions known to this application.
	 *
	 * @return The action keys.
	 * @see #getAction(String)
	 */
	public SortedSet<String> getActionKeys();


	/**
	 * Returns the actions of this GUI application as an array.
	 *
	 * @return The actions.
	 * @see #getAction(String)
	 */
	public Action[] getActions();


	/**
	 * Returns the Help dialog for this application, or <code>null</code>
	 * if this application does not have a Help dialog.
	 *
	 * @return The Help dialog.
	 */
	public HelpDialog getHelpDialog();


	/**
	 * Return the image used for the icon of this GUI application.
	 *
	 * @return The image.
	 */
	public Image getIconImage();


	/**
	 * Returns the directory in which this GUI application is installed
	 * (running).
	 *
	 * @return The directory.
	 */
	public String getInstallLocation();


	/**
	 * Returns the menu bar this application is using.
	 *
	 * @return The menu bar.
	 * @see #setJMenuBar
	 */
	public JMenuBar getJMenuBar();


	/**
	 * Returns the language used by this GUI application, in a
	 * <code>Locale</code>-friendly language string; e.g., <code>en</code>
	 * or <code>es</code>.
	 *
	 * @return The language being used by this application.
	 */
	public abstract String getLanguage();


	/**
	 * Returns an integer constant representing the OS.  This can be handy for
	 * special case situations such as Mac OS-X (special application
	 * registration) or Windows (allow mixed case, etc.).
	 *
	 * @return An integer constant representing the OS.
	 */
	public int getOS();


	/**
	 * Returns the resource bundle associated with this application.
	 *
	 * @return The resource bundle.
	 */
	public ResourceBundle getResourceBundle();


	/**
	 * Returns the status bar this application is using.
	 *
	 * @return The status bar.
	 * @see #setStatusBar
	 */
	public StatusBar getStatusBar();


	/**
	 * Returns whether the status bar is visible.
	 *
	 * @return Whether the status bar is visible.
	 * @see #setStatusBarVisible
	 */
	public boolean getStatusBarVisible();


	/**
	 * Returns the toolbar this application is using.
	 *
	 * @return The toolbar.
	 * @see #setToolBar
	 */
	public CustomizableToolBar getToolBar();


	/**
	 * Returns whether the toolbar is visible in this application.
	 *
	 * @return Whether the toolbar is visible.
	 * @see #setToolBarVisible
	 */
	public boolean getToolBarVisible();


	/**
	 * Returns the version string for this application.
	 *
	 * @return The version string.
	 */
	public String getVersionString();


	/**
	 * Returns true if this application's main window is maximized.
	 *
	 * @return <code>true</code> if this applicaiton's window is maximized, or
	 *         <code>false</code> if it isn't.
	 */
	public boolean isMaximized();


	/**
	 * Loads the preferences for this GUI application.  If this application
	 * does not use preferences or something, <code>null</code> is
	 * goes wrong, <code>null</code> is returned.
	 *
	 * @return This application's preferences.
	 */
	public GUIApplicationPreferences loadPreferences();


	/**
	 * Repacks the GUI application's main window.  This should be called
	 * after things are added or removed from it.
	 */
	public void pack();


	/**
	 * Sets the menu bar to use in this application.
	 *
	 * @param menuBar The menu bar.
	 * @see #getJMenuBar
	 */
	public void setJMenuBar(JMenuBar menuBar);


	/**
	 * Sets the language for this GUI application and all of its dialogs,
	 * UI widgets, etc.<p>
	 *
	 * @param language The language to use.  If <code>null</code>,
	 *        English will be used.
	 */
	public void setLanguage(final String language);


	/**
	 * Sets the status bar to use in this application.
	 *
	 * @param statusBar The status bar to use.
	 * @see #getStatusBar
	 */
	public void setStatusBar(StatusBar statusBar);


	/**
	 * Sets whether the status bar is visible.
	 *
	 * @param visible Whether the status bar is to be visible.
	 * @see #getStatusBarVisible
	 */
	public void setStatusBarVisible(boolean visible);


	/**
	 * Sets the toolbar used by this GUI application.
	 *
	 * @param toolBar The toolbar to use.
	 * @see #getToolBar
	 */
	public void setToolBar(CustomizableToolBar toolBar);


	/**
	 * Sets whether the toolbar used by this GUI application is visible.
	 *
	 * @param visible Whether the toolbar should be visible.
	 * @see #getToolBarVisible
	 */
	public void setToolBarVisible(boolean visible);


	/**
	 * The action that displays the application's About dialog.  This action
	 * should be sufficient for most applications; it simply displays the
	 * modal About dialog obtained from <code>getAboutDialog</code>.
	 */
	public static class AboutAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>AboutAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public AboutAction(GUIApplication app, String nameKey) {
			super(app, nameKey);
		}

		/**
		 * Creates a new <code>AboutAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param msg The resource bundle to localize from.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public AboutAction(GUIApplication app, ResourceBundle msg,
							String nameKey) {
			super(app, msg, nameKey);
		}

		/**
		 * Displays the About dialog.
		 *
		 * @param e The action event.
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				getApplication().getAboutDialog().setVisible(true);
			} catch (Exception ex) {
				getApplication().displayException(ex);
			}
		}

	}


	/**
	 * Action that attempts to close the application.  This action calls
	 * the application's <code>doExit</code> method.
	 */
	public static class ExitAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>ExitAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public ExitAction(GUIApplication app, String nameKey) {
			super(app, nameKey);
		}

		/**
		 * Creates a new <code>ExitAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 * @param icon The name of the icon resource for this action.
		 */
		public ExitAction(GUIApplication app, String nameKey, String icon) {
			super(app, nameKey, icon);
		}

		/**
		 * Creates a new <code>ExitAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param msg The resource bundle to localize from.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public ExitAction(GUIApplication app, ResourceBundle msg,
						String nameKey) {
			super(app, msg, nameKey);
		}

		/**
		 * Calls the application's <code>doExit</code> method.
		 *
		 * @param e The action event.
		 */
		public void actionPerformed(ActionEvent e) {
			getApplication().doExit();
		}

	}


	/**
	 * The action that displays the application's Help dialog.  This action
	 * should be sufficient for most applications; it simply displays the
	 * modal Help dialog obtained from <code>getHelpDialog</code>.
	 */
	public static class HelpAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>HelpAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public HelpAction(GUIApplication app, String nameKey) {
			super(app, nameKey);
		}

		/**
		 * Creates a new <code>HelpAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 * @param icon The name of the icon resource for this action.
		 */
		public HelpAction(GUIApplication app, String nameKey, String icon) {
			super(app, nameKey, icon);
		}

		/**
		 * Creates a new <code>HelpAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param msg The resource bundle to localize from.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public HelpAction(GUIApplication app, ResourceBundle msg,
									String nameKey) {
			super(app, msg, nameKey);
		}

		/**
		 * Displays the Help dialog, if it is non-<code>null</code>.
		 *
		 * @param e The action event.
		 */
		public void actionPerformed(ActionEvent e) {
			GUIApplication app = getApplication();
			if (app instanceof Component) {
				((Component)app).setCursor(Cursor.
							getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
			HelpDialog hd = null;
			try {
				hd = app.getHelpDialog();
			} finally {
				if (app instanceof Component) {
					((Component)app).setCursor(Cursor.getPredefinedCursor(
											Cursor.DEFAULT_CURSOR));
				}
			}
			if (hd!=null) {
				try {
					hd.setVisible(true);
				} catch (Exception ex) {
					app.displayException(ex);
				}
			}
		}

	}


	/**
	 * Action to toggle a <code>GUIApplication</code>'s status bar.
	 */
	public static class ToggleStatusBarAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>ToggleStatusBarAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public ToggleStatusBarAction(GUIApplication app, String nameKey) {
			super(app, nameKey);
		}

		/**
		 * Creates a new <code>ToggleStatusBarAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 * @param icon The name of the icon resource for this action.
		 */
		public ToggleStatusBarAction(GUIApplication app, String nameKey,
				String icon) {
			super(app, nameKey, icon);
		}

		/**
		 * Creates a new <code>ToggleStatusBarAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param msg The resource bundle to localize from.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public ToggleStatusBarAction(GUIApplication app, ResourceBundle msg,
								String nameKey) {
			super(app, msg, nameKey);
		}

		/**
		 * Toggles the status bar.
		 *
		 * @param e The action event.
		 */
		public void actionPerformed(ActionEvent e) {
			GUIApplication app = getApplication();
			app.setStatusBarVisible(!app.getStatusBarVisible());
		}

	}


	/**
	 * Action to toggle a <code>GUIApplication</code>'s toolbar.
	 */
	public static class ToggleToolBarAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>ToggleToolBarAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public ToggleToolBarAction(GUIApplication app, String nameKey) {
			super(app, nameKey);
		}

		/**
		 * Creates a new <code>ToggleToolBarAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 * @param icon The name of the icon resource for this action.
		 */
		public ToggleToolBarAction(GUIApplication app, String nameKey,
				String icon) {
			super(app, nameKey, icon);
		}

		/**
		 * Creates a new <code>ToggleToolBarAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param msg The resource bundle to localize from.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public ToggleToolBarAction(GUIApplication app, ResourceBundle msg,
								String nameKey) {
			super(app, msg, nameKey);
		}

		/**
		 * Toggles the toolbar.
		 *
		 * @param e The action event.
		 */
		public void actionPerformed(ActionEvent e) {
			GUIApplication app = getApplication();
			app.setToolBarVisible(!app.getToolBarVisible());
		}

	}


}