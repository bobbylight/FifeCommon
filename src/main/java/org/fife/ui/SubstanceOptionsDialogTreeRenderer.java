/*
 * 01/13/2013
 *
 * SubstanceOptionsDialogTreeRenderer - Renderer for options dialog tree nodes
 * when Substance is installed.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;


/**
 * Renderer used in the Options dialog's tree when Substance is installed.  I
 * love how Substance makes custom renderers a pain in the ass!
 * 
 * @author Robert Futrell
 * @version 1.0
 */
class SubstanceOptionsDialogTreeRenderer extends SubstanceDefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;


	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf,
			int row, boolean focused)  {

		super.getTreeCellRendererComponent(tree, value, selected, expanded,
				leaf, row, focused);

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object obj = node.getUserObject();
		if (obj instanceof OptionsDialogPanel) {
			OptionsDialogPanel panel = (OptionsDialogPanel) obj;
			setIcon(panel.getIcon());
		} else {
			setIcon(null);
		}

		return this;

	}


}