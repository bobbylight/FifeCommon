/*
 * 01/13/2013
 *
 * SubstanceFileSystemTreeRenderer - Renderer for file system tree nodes when
 * Substance is installed.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Component;
import java.io.File;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;


/**
 * Renderer used in file system trees when Substance is installed.  I love how
 * Substance makes custom renderers a pain in the ass!
 * 
 * @author Robert Futrell
 * @version 1.0
 */
class SubstanceFileSystemTreeRenderer extends SubstanceDefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;

	private FileSystemTree tree;


	public SubstanceFileSystemTreeRenderer(FileSystemTree tree) {
		this.tree = tree;
	}


	@Override
	public Component getTreeCellRendererComponent(JTree tree,
								Object value, boolean sel,
								boolean expanded, boolean leaf,
								int row, boolean hasFocus)  {

		super.getTreeCellRendererComponent(tree, value, sel, expanded,
									leaf, row, hasFocus);

		// Make the node have the proper icon and only display the
		// file name.

		// We must check "instanceof File" because it appears that Metal
		// and Motif LnF's call this method during a JTree's setRoot()
		// call (although Windows LnF doesn't... ???), which throws a
		// ClassCastException, as this is evidently called before the
		// root is replaced (and so the root node contains default sample
		// data such as "Colors" or some junk).  So if we check this, we
		// don't cast to File before the stuff has changed to File.
		Object userObj = ((DefaultMutableTreeNode)value).getUserObject();
		if (userObj instanceof File) {
			File file = (File)userObj;
			setText(this.tree.getName(file));
			setIcon(this.tree.iconManager.getIcon(file));
		}
		return this;

	}


}