/*
 * 02/09/2006
 *
 * DockableWindowEvent.java - A dockable window event.
 * Copyright (C) 2006 Robert Futrell
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
	 */
	public int getNewPosition() {
		return newPosition;
	}


	/**
	 * Returns the old location of the dockable window.
	 *
	 * @return The old location.
	 */
	public int getOldPosition() {
		return oldPosition;
	}


}