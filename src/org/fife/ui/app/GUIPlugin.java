/*
 * 12/28/2004
 *
 * GUIPlugin.java - A plugin that includes 1 or more dockable windows.
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