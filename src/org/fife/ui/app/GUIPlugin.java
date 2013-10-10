/*
 * 12/28/2004
 *
 * GUIPlugin.java - A plugin that includes 1 or more dockable windows.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;

import org.fife.ui.dockablewindows.DockableWindow;


/**
 * A plugin thats includes one or more dockable windows.
 *
 * @author Robert Futrell
 * @version 0.8
 * @see org.fife.ui.app.Plugin
 * @see org.fife.ui.app.AbstractPluggableGUIApplication
 * @see org.fife.ui.dockablewindows.DockableWindow
 */
public abstract class GUIPlugin extends AbstractPlugin
		implements GUIApplicationConstants {

	/**
	 * Map of window ID's to dockable windows.
	 */
	private Map<String, DockableWindow> windowMap;


	/**
	 * Constructor.
	 */
	public GUIPlugin() {
		windowMap = new HashMap<String, DockableWindow>();
	}


	/**
	 * Returns an iterator over all dockable windows in this plugin.
	 *
	 * @return An iterator.
	 * @see #getDockableWindow(String)
	 */
	public Iterator<DockableWindow> dockableWindowIterator() {
		return windowMap.values().iterator();
	}


	/**
	 * Returns a dockable window.
	 *
	 * @param id The id for the dockable window.
	 * @return The dockable window, or <code>null</code> if no dockable
	 *         window is associated with that identifier.
	 * @see #putDockableWindow(String, DockableWindow)
	 * @see #dockableWindowIterator()
	 */
	public DockableWindow getDockableWindow(String id) {
		return windowMap.get(id);
	}


	/**
	 * Overridden to update the <code>LookAndFeel</code> of any non-active
	 * (i.e. not visible) dockable windows owned by this plugin.  Active
	 * dockable windows are automatically handled by the main window's updating
	 * of its UI.<p>
	 * 
	 * Subclasses can override if they have modal dialogs (for example) that
	 * are cached and should be updated, but should call into the super
	 * implementation.
	 *
	 * @param newLaf The new <code>LookAndFeel</code>.
	 */
	@Override
	protected void lookAndFeelChanged(LookAndFeel newLaf) {
		for (DockableWindow dw : windowMap.values()) {
			if (!dw.isActive()) {
				SwingUtilities.updateComponentTreeUI(dw);
			}
		}
	}


	/**
	 * Associates a dockable window with an identifier.
	 *
	 * @param id The identifier.
	 * @param window The dockable window.
	 * @see #getDockableWindow(String)
	 */
	protected void putDockableWindow(String id, DockableWindow window) {
		windowMap.put(id, window);
	}


}