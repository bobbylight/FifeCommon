/*
 * 02/05/2010
 *
 * FloatingWindow.java - A floating window containing a dockable window.
 * Copyright (C) 2010 Robert Futrell
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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;


/**
 * A floating window containing a single dockable window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FloatingWindow extends JFrame {


	/**
	 * Constructor.
	 *
	 * @param window The dockable window to display.
	 */
	public FloatingWindow(DockableWindow window) {
		setContentPane(window);
		setTitle(window.getDockableWindowTitle());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new Listener());
	}


	/**
	 * Returns the dockable window displayed in this floating window.
	 *
	 * @return The dockable window.
	 */
	public DockableWindow getDockableWindow() {
		return (DockableWindow)getContentPane();
	}


	/**
	 * Checks whether the dockable window's title has changed and refreshes
	 * this window's title if it has.
	 *
	 * @return Whether the title changed.
	 */
	public boolean refreshTitle() {
		String newTitle = getDockableWindow().getDockableWindowTitle();
		if (newTitle!=getTitle()) {
			setTitle(newTitle);
			return true;
		}
		return false;
	}


	/**
	 * Listens for window events.
	 */
	private class Listener extends WindowAdapter {

		public void windowClosing(WindowEvent e) {
			// Causes this window to close, but also updates all dockable
			// window-related stuff.
			getDockableWindow().setActive(false);
		}

	}


}