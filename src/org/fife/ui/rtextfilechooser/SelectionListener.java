/*
 * 09/26/2009
 *
 * SelectionListener.java - Listens for selections in file chooser views.
 * Copyright (C) 2009 Robert Futrell
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
package org.fife.ui.rtextfilechooser;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Listens for the user selecting files in both the list mode and details mode.
 * This class can be used by any subclass of <code>RTextFileChooserView</code>
 * as its list selection listener, if it implements a list.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SelectionListener implements ListSelectionListener {

	private RTextFileChooser chooser;


	/**
	 * Constructor.
	 *
	 * @param chooser The file chooser.
	 */
	public SelectionListener(RTextFileChooser chooser) {
		this.chooser = chooser;
	}


	/**
	 * Called when the list's selection changes.  This method updates the
	 * file name and encoding text fields to be in-synch with the view's
	 * selection.
	 *
	 * @param e The selection event.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			chooser.synchronizeTextFieldWithView();
			chooser.updateSelectedEncoding();
		}
	}


}