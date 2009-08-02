/*
 * 12/28/2004
 *
 * GUIPluginEvent.java - The event fired by a GUIPlugin.
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

import java.util.EventObject;


/**
 * Event fired by <code>GUIPlugin</code>s.  These events are fired when:
 *
 * <ul>
 *   <li>The plugin changes its preferred position.</li>
 * </ul>
 *
 * @author Robert Futrell
 * @version 0.1
 * @see GUIPlugin
 */
public class GUIPluginEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private int oldPosition;
	private int newPosition;


	public GUIPluginEvent(Object source, int oldPos, int newPos) {
		super(source);
		this.oldPosition = oldPos;
		this.newPosition = newPos;
	}


	public int getNewPosition() {
		return newPosition;
	}


	public int getOldPosition() {
		return oldPosition;
	}


}