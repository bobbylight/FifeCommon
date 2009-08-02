/*
 * 07/17/2004
 *
 * IconDesktopPane.java - Desktop pane that allows the user to select multiple
 *                        internal frames simultaneously.
 * Copyright (C) 2004 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,1
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.ui.rtextfilechooser;

import java.util.ArrayList;
import javax.swing.*;


/**
 * Subclass of <code>JDesktopPane</code> that allows the user to select multiple
 * internal frames simultaneously.  Also, gets rid of the frame around internal
 * frames to give them an "icon" appearance.
 *
 * @author Robert Futrell
 * @version 0.1
 */
class IconDesktopPane extends JDesktopPane {

	private static final long serialVersionUID = 1L;

	private ArrayList selectedFrames;


	/**
	 * Constructor.
	 */
	public IconDesktopPane() {
		setDesktopManager(new IconDesktopManager());
		selectedFrames = new ArrayList(5);
	}


	/**
	 * Makes a frame selected.
	 *
	 * @param frame The frame to select.
	 */
	public void addSelectedFrame(JInternalFrame frame) {
		if (!selectedFrames.contains(frame))
			selectedFrames.add(frame);
	}


	/**
	 * Gets the most recently-selected internal frame.
	 *
	 * @return The frame, or <code>null</code> if no frames are selected.
	 */
	public JInternalFrame getSelectedFrame() {
		if (selectedFrames.size()==0)
			return null;
		return (JInternalFrame)selectedFrames.get(0);
	}


	/**
	 * Gets the selected internal frames.
	 *
	 * @return The frames, or <code>null</code> if no frames are selected.
	 */
	public JInternalFrame[] getSelectedFrames() {
		int size = selectedFrames.size();
		if (size==0)
			return null;
		JInternalFrame[] frames = new JInternalFrame[size];
		for (int i=0; i<size; i++)
			frames[i] = (JInternalFrame)selectedFrames.get(i);
		return frames;
	}


	/**
	 * De-selects a frame.
	 *
	 * @param frame The frame to deselect.
	 */
	public void removeSelectedFrame(JInternalFrame frame) {
		selectedFrames.remove(frame);
	}


}