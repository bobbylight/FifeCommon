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
	private Map windowMap;


	/**
	 * Constructor.
	 */
	public GUIPlugin() {
		windowMap = new HashMap();
	}


	/**
	 * Returns an iterator over all dockable windows in this plugin.
	 *
	 * @return An iterator.
	 * @see #getDockableWindow(String)
	 */
	public Iterator dockableWindowIterator() {
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
		return (DockableWindow)windowMap.get(id);
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