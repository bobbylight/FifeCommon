/*
 * 11/14/2003
 *
 * RTreeSelectionModel.java - Selection Model for a JTree allowing only single
 * selections, and you cannot have nothing selected.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeSelectionModel;


/**
 * A wrapper class; this selection model allows only a single selection,
 * and does not allow the user to deselect.  This model is useful when
 * the user should be required to have exactly one selection.
 *
 * @author Robert Futrell
 *
 * @version 1.0
 */
public class RTreeSelectionModel extends DefaultTreeSelectionModel {

	private static final long serialVersionUID = 1L;


	// Constructor.
	public RTreeSelectionModel() {
		// Allow only a single element to be selected.
		setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}


	/**
	 * Overrides <code>DefaultTreeSelectionModel</code>'s
	 * <code>removeSelectionPath</code> to do nothing, so the user can't
	 * "deselect."
	 */
	@Override
	public void removeSelectionPath(TreePath path) {
	}


}