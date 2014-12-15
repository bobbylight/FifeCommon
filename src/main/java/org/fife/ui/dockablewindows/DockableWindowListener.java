/*
 * 02/09/2006
 *
 * DockableWindowListener.java - Listens for events from dockable windows.
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.dockablewindows;

import java.util.EventListener;


/**
 * Interface for classes interested in listening for dockable window events.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public interface DockableWindowListener extends EventListener {


	/**
	 * Called whenever a dockable window changes its position.
	 *
	 * @param e The dockable window event.
	 */
	public void dockableWindowPositionChanged(DockableWindowEvent e);


	/**
	 * Called whenever a dockable window will change its position.
	 *
	 * @param e The dockable window.  The specified "new position" is the
	 *        position that the dockable window will be in.
	 */
	public void dockableWindowPositionWillChange(DockableWindowEvent e);


}