/*
 * 04/15/2009
 *
 * MouseListner.java - Listens for mouse events in the file chooser.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JComponent;


/**
 * Listens for mouse events.  This class can be used by any subclass of
 * {@link RTextFileChooserView} as its mouse listener.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class MouseListener extends MouseAdapter {

	private RTextFileChooser chooser;


	/**
	 * Constructor.
	 *
	 * @param chooser The file chooser.
	 */
	public MouseListener(RTextFileChooser chooser) {
		this.chooser = chooser;
	}


	/**
	 * Called when the mouse is clicked in the parent view.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {

		// Don't listen to clicks from other objects.  This is a hack to
		// avoid some problems with views that contain actual components
		// and not renderers, like the "icon view."
		Object source = e.getSource();
		if (!(source instanceof RTextFileChooserView))
			return;

		RTextFileChooserView view = (RTextFileChooserView)source;

		if (view.getDisplayedFileCount()>0) {

			boolean wasButton1 = (e.getModifiers()&InputEvent.BUTTON1_MASK)==
									InputEvent.BUTTON1_MASK;
			int clickCount = (e.getClickCount()%2)==0 ? 2 : 1;

			// If they double-click, do something...
			if (wasButton1 && clickCount==2) {

				// If we're only interested in selecting files, this is
				// equivalent to clicking the Approve button.
				if (chooser.getFileSelectionMode()==RTextFileChooser.FILES_ONLY) {
					chooser.actionPerformed(new ActionEvent(
						view,
						ActionEvent.ACTION_PERFORMED, "AcceptButtonPressed"));
				}

				// Otherwise, the user wants to be able to select directories
				// also.  In this case, a double-click on a directory
				// means go into that directory, whereas pressing the
				// Approve button (or pressing Enter) means return the
				// directory as the selection.
				else {
					File file = view.getFileAtPoint(e.getPoint());
					if (file==null) { // Clicked on background of view, not a file.
						view.clearSelection();
						return;
					}
					else if (file.isDirectory()) {
						chooser.setCurrentDirectory(file);
						return;
					}
					else {
						chooser.actionPerformed(new ActionEvent(view,
							ActionEvent.ACTION_PERFORMED, "AcceptButtonPressed"));
					}
				}

			} // End of if (wasButton1 && clickCount==2).

			// If they click in the view outside of any displayed file,
			// clear the selected files.
			else if (wasButton1 && clickCount==1) {
				File file = view.getFileAtPoint(e.getPoint());
				if (file==null) { // Clicked on background of view, not a file.
					view.clearSelection();
					chooser.setEncoding(RTextFileChooser.getDefaultEncoding());
					return;
				}
				// Otherwise, update the "encoding" combo box to display the
				// proper encoding for the file.
				chooser.updateSelectedEncoding();
			}

			else if (e.isPopupTrigger()) {
				Point p = e.getPoint();
				chooser.displayPopupMenu((JComponent)view, p.x,p.y);
			}

		}

	}


	@Override
	public void mousePressed(MouseEvent e) {

		// Don't listen to clicks from other objects.  This is a hack to
		// avoid some problems with views that contain actual components
		// and not renderers, like the "icon view."
		Object source = e.getSource();
		if (!(source instanceof RTextFileChooserView)) {
			return;
		}

		if (e.isPopupTrigger()) {
			Point p = e.getPoint();
			chooser.displayPopupMenu((JComponent)source, p.x,p.y);
		}
	}


	@Override
	public void mouseReleased(MouseEvent e) {

		// Don't listen to clicks from other objects.  This is a hack to
		// avoid some problems with views that contain actual components
		// and not renderers, like the "icon view."
		Object source = e.getSource();
		if (!(source instanceof RTextFileChooserView)) {
			return;
		}

		if (e.isPopupTrigger()) {
			Point p = e.getPoint();
			chooser.displayPopupMenu((JComponent)source, p.x,p.y);
		}

	}


}