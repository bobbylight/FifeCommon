/*
 * 02/09/2006
 *
 * DockableWindowListener.java - Listens for events from dockable windows.
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