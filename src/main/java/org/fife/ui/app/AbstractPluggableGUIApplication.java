/*
 * 04/25/2005
 *
 * AbstractPluggableGUIApplication.java - A GUI application able to be
 * extended by plug-ins.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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

import org.fife.ui.SplashScreen;
import org.fife.ui.app.prefs.AppPrefs;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowPanel;


/**
 * An extension of <code>AbstractGUIApplication</code> adding the ability to
 * add/remove plug-ins.
 *
 * @param <T> The preferences class for this application.
 * @author Robert Futrell
 * @version 0.5
 */
public abstract class AbstractPluggableGUIApplication<T extends AppPrefs<?>>
									extends AbstractGUIApplication<T> {

	/**
	 * List of installed plug-ins.
	 */
	private List<Plugin> pluginList;

	/**
	 * The class loader used for plugin stuff.
	 */
	private PluginLoader pluginLoader;


	/**
	 * Constructor.
	 *
	 * @param title The title for this frame.
	 */
	public AbstractPluggableGUIApplication(String title) {
		super(title);
	}


	/**
	 * Constructor.  This constructor is useful when you are making a clone of
	 * the current application (e.g., "Open in New Window...") and you want
	 * the two instances to have the same properties.
	 *
	 * @param title The title for this frame.
	 * @param prefs The preferences with which to initialize this application.
	 */
	public AbstractPluggableGUIApplication(String title, T prefs) {
		super(title, prefs);
	}


	/**
	 * Adds a plugin to this GUI application.  Note this should only be called
	 * on the EDT.
	 *
	 * @param plugin The plugin to add.
	 * @see #handleInstallPlugin
	 * @see #removePlugin
	 * @see #isPluginLoadingComplete()
	 */
	public final void addPlugin(Plugin plugin) {

		if (pluginList==null) {
			pluginList = new ArrayList<>(1);
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
	 * any GUI plug-ins.
	 *
	 * @param actualContentPane The panel that will contain the program's
	 *        content.  This panel should be added to the returned panel.
	 * @return The panel.
	 */
	@Override
	JPanel createMainContentPanel(Container actualContentPane) {
		MainContentPanel mcp = new MainContentPanel();
		mcp.setContentPanel(actualContentPane);
		return mcp;
	}


	/**
	 * Returns all installed plug-ins.  Note that this returns the actual
	 * plug-ins and not deep copies, so any changes made to the plug-ins in
	 * the array will affect the application itself.
	 *
	 * @return All installed plug-ins.  If no plug-ins are installed, a
	 *         zero-length array is returned.
	 * @see #addPlugin(Plugin)
	 * @see #removePlugin(Plugin)
	 * @see #isPluginLoadingComplete()
	 */
	public Plugin[] getPlugins() {
		int count = pluginList==null ? 0 : pluginList.size();
		Plugin[] plugins = new Plugin[count];
		if (count>0)
			plugins = pluginList.toArray(plugins);
		return plugins;
	}


	/**
	 * Returns the location of the divider of the specified split pane.
	 *
	 * @param splitPane One of <code>GUIApplicationConstants.TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code>, or <code>RIGHT</code>.
	 * @return The divider location.
	 * @throws IllegalArgumentException If <code>splitPane</code> is
	 *         invalid.
	 * @see #setSplitPaneDividerLocation
	 */
	public int getSplitPaneDividerLocation(int splitPane) {
		return switch (splitPane) {
			case TOP, BOTTOM, LEFT, RIGHT -> ((MainContentPanel)mainContentPanel).
				getDividerLocation(splitPane);
			default -> throw new IllegalArgumentException("Bad splitPane value");
		};
	}


	/**
	 * Does the dirty work of actually installing a plugin.  This method
	 * should be overridden by subclasses to do stuff as appropriate for a
	 * plugin.  A subclass of {@link org.fife.ui.app.GUIPlugin} will already
	 * have its dockable windows added to the GUI; anything else that is
	 * application-specific should be done here.<p>
	 *
	 * This default version of the method does nothing.
	 *
	 * @param plugin The plugin to install.
	 */
	protected void handleInstallPlugin(Plugin plugin) {
	}


	/**
	 * Returns whether a dockable window group is expanded or collapsed.
	 *
	 * @param group The group.  Should be a value from
	 *        {@link org.fife.ui.dockablewindows.DockableWindowConstants}.
	 * @return Whether it is expanded.
	 * @see #setDockableWindowGroupExpanded(int, boolean)
	 * @see #setSplitPaneDividerLocation(int, int, boolean)
	 */
	public boolean isDockableWindowGroupExpanded(int group) {
		return ((MainContentPanel)mainContentPanel).isDockableWindowGroupExpanded(group);
	}


	/**
	 * Returns whether all plug-ins have been loaded for this application.
	 * This does not count plug-ins added programmatically via
	 * {@link #addPlugin(Plugin)}, although that isn't usually done (plug-ins
	 * are usually all loaded automatically by the
	 * <code>PluginLoader</code>.
	 *
	 * @return Whether the loading of plug-ins is complete.
	 */
	public boolean isPluginLoadingComplete() {
		return pluginLoader!=null && pluginLoader.isPluginLoadingComplete();
	}


	/**
	 * Loads plug-ins.  This is done in a separate thread to offset plugin
	 * loading from the EDT.  GUI plug-ins will be added to the GUI on the
	 * EDT.
	 */
	private void loadPlugins() {
		new Thread(() -> {
			try {
				pluginLoader = new PluginLoader(
							AbstractPluggableGUIApplication.this);
				pluginLoader.loadPlugins();
			} catch (final IOException ioe) {
				SwingUtilities.invokeLater(() -> displayException(ioe));
			}
		}).start();
	}


	/**
	 * This is called in the GUI application's constructor.  It is a chance
	 * for subclasses to do initialization of stuff that will be needed by
	 * the application right before it is displayed.<p>
	 *
	 * This method loads all plug-ins.  If you override this method, you should
	 * be sure to call <code>super.preDisplayInit()</code>.  This should be
	 * called after the GUI has been finalized so that plug-ins can access all
	 * parts of the GUI.
	 *
	 * @param prefs The preferences of the application.
	 * @param splashScreen The "splash screen" for this application.  This
	 *        value may be <code>null</code>.
	 */
	@Override
	protected void preDisplayInit(T prefs, SplashScreen splashScreen) {
		loadPlugins();
	}


	/**
	 * Tries to uninstall and remove the specified plugin.  This should only
	 * be called on the EDT.
	 *
	 * @param plugin The plugin to remove.
	 * @return Whether the uninstall was successful.
	 * @see #addPlugin(Plugin)
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
	@Override
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
	 * that if no plug-ins are docked at the specified location (and thus no
	 * split pane is there), this method does nothing.
	 *
	 * @param splitPane The split pane for which to set the divider
	 *        location; one of <code>GUIApplicationConstants.TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code> or
	 *        <code>RIGHT</code>.
	 * @param pos The new position for the divider.
	 * @param expanded Whether the split pane should be initially expanded.
	 * @see #getSplitPaneDividerLocation
	 * @see #isDockableWindowGroupExpanded(int)
	 */
	public void setSplitPaneDividerLocation(int splitPane, int pos, boolean expanded) {
		MainContentPanel mcp = (MainContentPanel)mainContentPanel;
		mcp.setDividerLocation(splitPane, pos);
		mcp.setDockableWindowGroupExpanded(splitPane, expanded);
	}


	/**
	 * Toggles whether a dockable window group is expanded or collapsed.  Does nothing if
	 * there are no dockable windows at that position.
	 *
	 * @param group The group.  Should be a value from {@link org.fife.ui.dockablewindows.DockableWindowConstants}.
	 * @param expanded Whether it should be expanded or collapsed.
	 * @see #isDockableWindowGroupExpanded(int)
	 */
	public void setDockableWindowGroupExpanded(int group, boolean expanded) {
		((MainContentPanel)mainContentPanel).setDockableWindowGroupExpanded(group, expanded);
	}


	/**
	 * A panel capable of using split panes to add "GUI plug-ins" to the
	 * top, left, bottom, and right of some main content.
	 */
	private static final class MainContentPanel extends DockableWindowPanel {

		public boolean addPlugin(GUIPlugin plugin) {
			boolean success = true;
			Iterator<DockableWindow> i = plugin.dockableWindowIterator();
			while (i.hasNext()) {
				DockableWindow wind = i.next();
				if (!addDockableWindow(wind)) {
					success = false; // Any 1 failure => failure
				}
			}
			return success;
		}

		public boolean removePlugin(GUIPlugin plugin) {
			boolean success = true;
			Iterator<DockableWindow> i = plugin.dockableWindowIterator();
			while (i.hasNext()) {
				DockableWindow wind = i.next();
				if (!removeDockableWindow(wind)) {
					success = false; // Any 1 failure => failure
				}
			}
			return success;
		}

	}


}
