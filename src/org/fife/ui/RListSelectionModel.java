/*
 * 11/14/2003
 *
 * RListSelectionModel.java - Selection model for a JList allowing only single
 * selections, and you cannot have nothing selected.
 * Copyright (C) 2003 Robert Futrell
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
package org.fife.ui;

import javax.swing.ListSelectionModel;
import javax.swing.DefaultListSelectionModel;


/**
 * A wrapper class; this selection model allows only a single selection,
 * and does not allow the user to deselect.  This model is useful when
 * the user should be required to have exactly one selection.
 *
 * @author Robert Futrell
 *
 * @version 1.0
 */
public class RListSelectionModel extends DefaultListSelectionModel {

	private static final long serialVersionUID = 1L;


	/**
	 * Constructor.
	 */
	public RListSelectionModel() {
		// Allow only a single element to be selected.
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}


	/**
	 * Remove the indices in the interval index0,index1 (inclusive) from the
	 * selection model. This will be called when the user Ctrl+clicks, for
	 * example. We don't want them to be able to have nothing selected in our
	 * list, so this method simply does nothing.
	 */
	public void removeSelectionInterval(int index0, int index1) {
	}


}