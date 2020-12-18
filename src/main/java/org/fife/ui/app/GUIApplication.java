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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.Serial;
import java.util.ResourceBundle;
import java.util.SortedSet;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JMenuBar;

import org.fife.help.HelpDialog;
import org.fife.ui.CustomizableToolBar;
import org.fife.ui.OS;
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
	void addAction(String key, Action action);


	/**
	 * This method should be overridden to convey to the user that an
	 * Exception occurred in some way, for example, in a dialog box.
	 *
	 * @param t The exception/throwable that occurred.
	 */
	void displayException(Throwable t);


	/**
	 * This method should be overridden to convey to the user that an
	 * Exception occurred in some way, for example, in a dialog box.
	 * This version is useful if a child dialog of the GUI application
	 * threw the Exception.
	 *
	 * @param dialog The child dialog that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 */
	void displayException(Dialog dialog, Throwable t);


	/**
	 * This method should be overridden to convey to the user that an
	 * Exception occurred in some way, for example, in a dialog box.
	 * This version is useful if a child frame of the GUI application
	 * threw the Exception.
	 *
	 * @param frame The child frame that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 */
	void displayException(Frame frame, Throwable t);


	/**
	 * Called when the user attempts to close the application, whether from
	 * an "Exit" menu item, closing the main application window, or any other
	 * means.  Applications should override this method to do any cleanup
	 * before the application exits.  You can also prevent the application
	 * from closing based on the application's state in this method.
	 */
	void doExit();


	/**
	 * Returns the About dialog for this application.
	 *
	 * @return The About dialog.
	 * @see org.fife.ui.AboutDialog
	 */
	JDialog getAboutDialog();


	/**
	 * Returns one of this application's actions.
	 *
	 * @param key The key of the action to return.
	 * @return The action, or <code>null</code> if no action exists for the
	 *         specified key.
	 * @see #addAction(String, Action)
	 * @see #getActions()
	 */
	Action getAction(String key);



	/**
	 * Returns the keys of all actions known to this application.
	 *
	 * @return The action keys.
	 * @see #getAction(String)
	 */
	SortedSet<String> getActionKeys();


	/**
	 * Returns the actions of this GUI application as an array.
	 *
	 * @return The actions.
	 * @see #getAction(String)
	 */
	Action[] getActions();


	/**
	 * Returns the Help dialog for this application, or <code>null</code>
	 * if this application does not have a Help dialog.
	 *
	 * @return The Help dialog.
	 */
	HelpDialog getHelpDialog();


	/**
	 * Return the image used for the icon of this GUI application.
	 *
	 * @return The image.
	 */
	Image getIconImage();


	/**
	 * Returns the directory in which this GUI application is installed
	 * (running).
	 *
	 * @return The directory.
	 */
	String getInstallLocation();


	/**
	 * Returns the menu bar this application is using.
	 *
	 * @return The menu bar.
	 * @see #setJMenuBar
	 */
	JMenuBar getJMenuBar();


	/**
	 * Returns the language used by this GUI application, in a
	 * <code>Locale</code>-friendly language string; e.g., <code>en</code>
	 * or <code>es</code>.
	 *
	 * @return The language being used by this application.
	 */
	String getLanguage();


	/**
	 * Returns the location of this application's main window on the screen.
	 *
	 * @return The location of the application's main window.
	 * @see #getSize()
	 */
	Point getLocation();


	/**
	 * Returns the current OS.  This can be handy for special-case situations
	 * such as Mac OS X (special application registration) or Windows (allow
	 * mixed case, etc.).
	 *
	 * @return The OS we're running on.
	 */
	OS getOS();


	/**
	 * Returns the resource bundle associated with this application.
	 *
	 * @return The resource bundle.
	 */
	ResourceBundle getResourceBundle();


	/**
	 * Returns the size of the application's main window.
	 *
	 * @return The size of the application's main window.
	 * @see #getLocation()
	 */
	Dimension getSize();


	/**
	 * Returns the status bar this application is using.
	 *
	 * @return The status bar.
	 * @see #setStatusBar
	 */
	StatusBar getStatusBar();


	/**
	 * Returns whether the status bar is visible.
	 *
	 * @return Whether the status bar is visible.
	 * @see #setStatusBarVisible
	 */
	boolean getStatusBarVisible();


	/**
	 * Returns the toolbar this application is using.
	 *
	 * @return The toolbar.
	 * @see #setToolBar
	 */
	CustomizableToolBar getToolBar();


	/**
	 * Returns whether the toolbar is visible in this application.
	 *
	 * @return Whether the toolbar is visible.
	 * @see #setToolBarVisible
	 */
	boolean getToolBarVisible();


	/**
	 * Returns the version string for this application.
	 *
	 * @return The version string.
	 */
	String getVersionString();


	/**
	 * Returns true if this application's main window is maximized.
	 *
	 * @return <code>true</code> if this application's window is maximized, or
	 *         <code>false</code> if it isn't.
	 */
	boolean isMaximized();


	/**
	 * Repacks the GUI application's main window.  This should be called
	 * after things are added or removed from it.
	 */
	void pack();


	/**
	 * Sets the menu bar to use in this application.
	 *
	 * @param menuBar The menu bar.
	 * @see #getJMenuBar
	 */
	void setJMenuBar(JMenuBar menuBar);


	/**
	 * Sets the language for this GUI application and all of its dialogs,
	 * UI widgets, etc.<p>
	 *
	 * @param language The language to use.  If <code>null</code>,
	 *        English will be used.
	 */
	void setLanguage(String language);


	/**
	 * Sets the status bar to use in this application.
	 *
	 * @param statusBar The status bar to use.
	 * @see #getStatusBar
	 */
	void setStatusBar(StatusBar statusBar);


	/**
	 * Sets whether the status bar is visible.
	 *
	 * @param visible Whether the status bar is to be visible.
	 * @see #getStatusBarVisible
	 */
	void setStatusBarVisible(boolean visible);


	/**
	 * Sets the toolbar used by this GUI application.
	 *
	 * @param toolBar The toolbar to use.
	 * @see #getToolBar
	 */
	void setToolBar(CustomizableToolBar toolBar);


	/**
	 * Sets whether the toolbar used by this GUI application is visible.
	 *
	 * @param visible Whether the toolbar should be visible.
	 * @see #getToolBarVisible
	 */
	void setToolBarVisible(boolean visible);


	/**
	 * The action that displays the application's About dialog.  This action
	 * should be sufficient for most applications; it simply displays the
	 * modal About dialog obtained from <code>getAboutDialog</code>.
	 *
	 * @param <T> The parent application type.
	 */
	class AboutAction<T extends GUIApplication> extends AppAction<T> {

		@Serial
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>AboutAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public AboutAction(T app, String nameKey) {
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
		public AboutAction(T app, ResourceBundle msg, String nameKey) {
			super(app, msg, nameKey);
		}

		/**
		 * Displays the About dialog.
		 *
		 * @param e The action event.
		 */
		@Override
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
	 *
	 * @param <T> The parent application type.
	 */
	class ExitAction<T extends GUIApplication> extends AppAction<T> {

		@Serial
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>ExitAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public ExitAction(T app, String nameKey) {
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
		public ExitAction(T app, String nameKey, String icon) {
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
		public ExitAction(T app, ResourceBundle msg, String nameKey) {
			super(app, msg, nameKey);
		}

		/**
		 * Calls the application's <code>doExit</code> method.
		 *
		 * @param e The action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			getApplication().doExit();
		}

	}


	/**
	 * The action that displays the application's Help dialog.  This action
	 * should be sufficient for most applications; it simply displays the
	 * modal Help dialog obtained from <code>getHelpDialog</code>.
	 *
	 * @param <T> The parent application type.
	 */
	class HelpAction<T extends GUIApplication> extends AppAction<T> {

		@Serial
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>HelpAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public HelpAction(T app, String nameKey) {
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
		public HelpAction(T app, String nameKey, String icon) {
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
		public HelpAction(T app, ResourceBundle msg, String nameKey) {
			super(app, msg, nameKey);
		}

		/**
		 * Displays the Help dialog, if it is non-<code>null</code>.
		 *
		 * @param e The action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			GUIApplication app = getApplication();
			if (app instanceof Component) {
				((Component)app).setCursor(Cursor.
							getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
			HelpDialog hd;
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
	 *
	 * @param <T> The parent application type.
	 */
	class ToggleStatusBarAction<T extends GUIApplication> extends AppAction<T> {

		@Serial
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>ToggleStatusBarAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public ToggleStatusBarAction(T app, String nameKey) {
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
		public ToggleStatusBarAction(T app, String nameKey, String icon) {
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
		public ToggleStatusBarAction(T app, ResourceBundle msg, String nameKey){
			super(app, msg, nameKey);
		}

		/**
		 * Toggles the status bar.
		 *
		 * @param e The action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			GUIApplication app = getApplication();
			app.setStatusBarVisible(!app.getStatusBarVisible());
		}

	}


	/**
	 * Action to toggle a <code>GUIApplication</code>'s toolbar.
	 *
	 * @param <T> The parent application type.
	 */
	class ToggleToolBarAction<T extends GUIApplication> extends AppAction<T> {

		@Serial
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new <code>ToggleToolBarAction</code>.
		 *
		 * @param app The GUI application that owns this action.
		 * @param nameKey The key for the name of the action (and the possible
		 *        root of keys for description, mnemonic, etc.).
		 */
		public ToggleToolBarAction(T app, String nameKey) {
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
		public ToggleToolBarAction(T app, String nameKey, String icon) {
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
		public ToggleToolBarAction(T app, ResourceBundle msg, String nameKey) {
			super(app, msg, nameKey);
		}

		/**
		 * Toggles the toolbar.
		 *
		 * @param e The action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			GUIApplication app = getApplication();
			app.setToolBarVisible(!app.getToolBarVisible());
		}

	}


}
