/*
 * 11/14/2003
 *
 * RListSelectionModel.java - Selection model for a JList allowing only single
 * selections, and you cannot have nothing selected.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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
	@Override
	public void removeSelectionInterval(int index0, int index1) {
	}


}