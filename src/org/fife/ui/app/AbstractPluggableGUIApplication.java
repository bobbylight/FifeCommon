/*
 * 04/25/2005
 *
 * AbstractPluggableGUIApplication.java - A GUI application able to be
 * extended by plugins.
 * Copyright (C) 2005 Robert Futrell
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
package org.fife.ui.app;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.fife.ui.OptionsDialog;
import org.fife.ui.SplashScreen;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowPanel;


/**
 * An extension of <code>AbstractGUIApplication</code> adding the ability to
 * add/remove plugins.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public abstract class AbstractPluggableGUIApplication
									extends AbstractGUIApplication {

	/**
	 * List of installed plugins.
	 */
	private List pluginList;

	/**
	 * The options dialog for plugins.
	 */
	private PluginOptionsDialog pluginOptionsDialog;

//	/**
//	 * The class loader used for plugin stuff.
//	 */
//	private PluginClassLoader pluginClassLoader;


	/**
	 * Constructor.
	 *
	 * @param jarFile The name (not full path) of the JAR file containing the
	 *        main class of this application (e.g. "Foobar.jar").
	 */
	public AbstractPluggableGUIApplication(String jarFile) {
		super(jarFile);
	}


	/**
	 * Constructor.
	 *
	 * @param title The title for this frame.
	 * @param jarFile The name (not full path) of the JAR file containing
	 *        the main class of this application (e.g. "Foobar.jar").
	 */
	public AbstractPluggableGUIApplication(String title, String jarFile) {
		super(title, jarFile);
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
	public AbstractPluggableGUIApplication(String title, String jarFile,
									GUIApplicationPreferences prefs) {
		super(title, jarFile, prefs);
	}


	/**
	 * Adds a plugin to this GUI application.
	 *
	 * @param plugin The plugin to add.
	 * @see #handleInstallPlugin
	 * @see #removePlugin
	 */
	public final void addPlugin(Plugin plugin) {

		if (pluginList==null) {
			pluginList = new ArrayList(1);
		}
		pluginList.add(plugin);

		// If it's a GUI plugin, we'll physically add it to
		// the GUI for you...
		if (plugin instanceof GUIPlugin) {
			GUIPlugin gp = (GUIPlugin)plugin;
			((MainContentPanel)mainContentPanel).addPlugin(gp);
		}
		else if (plugin instanceof StatusBarPlugin) {
			// FIXME: Get the constraints from the plugin itself.
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.0;
			getStatusBar().addStatusBarComponent(
									(StatusBarPlugin)plugin, 0, c);
		}

		// And we let the plugin register any listeners, etc...
		plugin.install(this);

		// But your subclass must do everything else.
		handleInstallPlugin(plugin);

	}


	/**
	 * Creates the panel that contains the main content (via the
	 * <code>actualContentPane</code>.  The returned panel also contains
	 * any GUI plugins.
	 *
	 * @param actualContentPane The panel that will contain the program's
	 *        content.  This panel should be added to the returned panel.
	 * @return The panel.
	 */
	JPanel createMainContentPanel(JPanel actualContentPane) {
		MainContentPanel mcp = new MainContentPanel();
		mcp.setContentPanel(actualContentPane);
		return mcp;
	}


	/**
	 * Returns an options dialog containing options for all installed
	 * plugins.  This dialog is lazily created the first time this method
	 * is called.
	 *
	 * @return The options dialog.
	 */
	public synchronized OptionsDialog getPluginOptionsDialog() {
		return getPluginOptionsDialog(true);
	}


	/**
	 * Workaround for an apparent javac bug - I could not simply make
	 * <code>pluginOptionsDialog</code> protected and call
	 * <code>pluginOptionsDialog.pack()</code> in a subclass without getting
	 * a compiler warning about "Method pack is not public and cannot be
	 * accessed outside of package"... But, I'd like a way for subclasses to
	 * get to <code>pluginOptionsDialog</code> without it being created if it
	 * hasn't already been.
	 *
	 * @param create Whether or not to create the dialog if it hasn't already
	 *        been.
	 * @return The plugin options dialog.
	 */
	protected synchronized OptionsDialog getPluginOptionsDialog(
												boolean create) {
		if (pluginOptionsDialog==null && create)
			pluginOptionsDialog = new PluginOptionsDialog(this);
		if (pluginOptionsDialog!=null)
			pluginOptionsDialog.initialize();
		return pluginOptionsDialog;
	}


	/**
	 * Returns all installed plugins.  Note that this returns the actual
	 * plugins and not deep copies, so any changes made to the plugin
	 * array will affect the application itself.
	 *
	 * @return All installed plugins.  If no plugins are installed, a
	 *         zero-length array is returned.
	 * @see #addPlugin
	 * @see #removePlugin
	 */
	public Plugin[] getPlugins() {
		int count = pluginList==null ? 0 : pluginList.size();
		Plugin[] plugins = new Plugin[count];
		if (count>0)
			plugins = (Plugin[])pluginList.toArray(plugins);
		return plugins;
	}


	/**
	 * Returns the location of the divider of the specified split pane.
	 *
	 * @param splitPane One of <code>GUIApplicationConstants.TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code>, or <code>RIGHT</code>.
	 * @throws IllegalArgumentException If <code>splitPane</code> is
	 *         invalid.
	 * @see #setSplitPaneDividerLocation
	 */
	public int getSplitPaneDividerLocation(int splitPane) {
		int dividerLocation = 0;
		switch (splitPane) {
			case TOP:
			case BOTTOM:
			case LEFT:
			case RIGHT:
				dividerLocation = ((MainContentPanel)mainContentPanel).
										getDividerLocation(splitPane);
				break;
			default:
				throw new IllegalArgumentException("Bad splitPane value");
		}
		return dividerLocation;
	}


	/**
	 * Does the dirty work of actually installing a plugin.  This method
	 * should be overridden by subclasses to do stuff as appropriate for
	 * a plugin.  A subclass of {@link org.fife.ui.app.GUIPlugin} will
	 * already have been added to the GUI; everything else (such as adding
	 * the plugin's popup menu to your menu bar) you must do yourself.
	 * This default version of the method does nothing.
	 *
	 * @param plugin The plugin to install.
	 */
	protected void handleInstallPlugin(Plugin plugin) {
	}


	/**
	 * Loads plugins.  This is done in a separate thread to offset plugin
	 * loading from the EDT.  GUI plugins will be added to the GUI on the
	 * EDT.
	 */
	private void loadPlugins() {
		new Thread() {
			public void run() {
				try {
					/*pluginClassLoader = */new PluginLoader(
								AbstractPluggableGUIApplication.this);
				} catch (final IOException ioe) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							displayException(ioe);
						}
					});
				}
			}
		}.start();
	}


	/**
	 * This is called in the GUI application's constructor.  It is a chance
	 * for subclasses to do initialization of stuff that will be needed by
	 * the application right before it is displayed.<p>
	 *
	 * This method loads all plugins.  If you override this method, you should
	 * be sure to call <code>super.preDisplayInit()</code>.  This should be
	 * called after the GUI has been finalized so that plugins can access all
	 * parts of the GUI.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be <code>null</code>.
	 */
	protected void preDisplayInit(GUIApplicationPreferences prefs,
								SplashScreen splashScreen) {
		loadPlugins();
	}


	/**
	 * Tries to uninstall and remove the specified plugin.
	 *
	 * @param plugin The plugin to remove.
	 * @return Whether the uninstall was successful.
	 */
	public boolean removePlugin(Plugin plugin) {

		pluginList.remove(plugin);

		// If it's a GUI plugin...
		if (plugin instanceof GUIPlugin) {
			GUIPlugin gp = (GUIPlugin)plugin;
			return ((MainContentPanel)mainContentPanel).removePlugin(gp);
		}

		throw new IllegalArgumentException(
						"Only GUIPlugins are currently supported");

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
			MainContentPanel mcp = (MainContentPanel)mainContentPanel;
			if (actualContentPane!=null)
				mcp.removeContentPanel(actualContentPane);
			mcp.setContentPanel(contentPane);
		}
	}


	/**
	 * Sets the position of the divider for the specified split pane.  Note
	 * that if no plugins are docked at the specified location (and thus no
	 * split pane is there), this method does nothing.
	 *
	 * @param splitPane The split pane for which to set the divider
	 *        location; one of <code>GUIApplicationConstants.TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code> or
	 *        <code>RIGHT</code>.
	 * @param pos The new position for the divider.
	 * @see #getSplitPaneDividerLocation
	 */
	public void setSplitPaneDividerLocation(int splitPane, int pos) {
		MainContentPanel mcp = (MainContentPanel)mainContentPanel;
		mcp.setDividerLocation(splitPane, pos);
	}


	/**
	 * A panel capable of using split panes to add "GUI plugins" to the
	 * top, left, bottom, and right of some main content.
	 */
	private static final class MainContentPanel extends DockableWindowPanel {
	
		public boolean addPlugin(GUIPlugin plugin) {
			boolean success = true;
			for (Iterator i=plugin.dockableWindowIterator(); i.hasNext(); ) {
				DockableWindow wind = (DockableWindow)i.next();
				if (!addDockableWindow(wind)) {
					success = false; // Any 1 failure => failure
				}
			}
			return success;
		}

		public boolean removePlugin(GUIPlugin plugin) {
			boolean success = true;
			for (Iterator i=plugin.dockableWindowIterator(); i.hasNext(); ) {
				DockableWindow wind = (DockableWindow)i.next();
				if (!removeDockableWindow(wind)) {
					success = false; // Any 1 failure => failure
				}
			}
			return success;
		}

	}


}