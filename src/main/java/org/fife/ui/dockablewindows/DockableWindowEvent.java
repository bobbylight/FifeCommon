/*
 * 02/09/2006
 *
 * DockableWindowEvent.java - A dockable window event.
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.dockablewindows;

import java.util.EventObject;


/**
 * Event fired by <code>DockableWindow</code>s.  These events are fired when:
 *
 * <ul>
 *   <li>The dockable window is about to change its preferred position.
 *   <li>The plugin changes its preferred position.
 * </ul>
 *
 * @author Robert Futrell
 * @version 0.1
 * @see DockableWindow
 */
public class DockableWindowEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private int oldPosition;
	private int newPosition;


	/**
	 * Constructor.
	 *
	 * @param source The dockable window.
	 * @param oldPos The old location of the dockable window.
	 * @param newPos The new location of the dockable window.
	 */
	public DockableWindowEvent(Object source, int oldPos, int newPos) {
		super(source);
		this.oldPosition = oldPos;
		this.newPosition = newPos;
	}


	/**
	 * Returns the new location of the dockable window.
	 *
	 * @return The new location.
	 * @see DockableWindowConstants
	 */
	public int getNewPosition() {
		return newPosition;
	}


	/**
	 * Returns the old location of the dockable window.
	 *
	 * @return The old location.
	 * @see DockableWindowConstants
	 */
	public int getOldPosition() {
		return oldPosition;
	}


}