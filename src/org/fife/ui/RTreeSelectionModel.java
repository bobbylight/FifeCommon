/*
 * 11/14/2003
 *
 * RTreeSelectionModel.java - Selection Model for a JTree allowing only single
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
	public void removeSelectionPath(TreePath path) {
	}


}