/*
 * 07/17/2004
 *
 * IconDesktopPane.java - Desktop pane that allows the user to select multiple
 *                        internal frames simultaneously.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.util.ArrayList;
import java.util.List;

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

	private List<JInternalFrame> selectedFrames;


	/**
	 * Constructor.
	 */
	public IconDesktopPane() {
		setDesktopManager(new IconDesktopManager());
		selectedFrames = new ArrayList<JInternalFrame>(5);
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
	@Override
	public JInternalFrame getSelectedFrame() {
		if (selectedFrames.size()==0)
			return null;
		return selectedFrames.get(0);
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
			frames[i] = selectedFrames.get(i);
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