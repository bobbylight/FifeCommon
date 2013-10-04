/*
 * 06/30/2005
 *
 * ToolTipTree.java - A JTree that displays tooltips for its nodes directly
 * over them, like the file system tree in Windows Explorer.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


/**
 * A tree that displays tooltips directly over the text of its nodes, like the
 * file system tree found in Windows Explorer.  This class registers itself
 * with the <code>javax.swing.ToolTipManager</code> and knows how to position
 * its tooltips correctly.<p>
 *
 * This class assumes that the <code>TreeCellRenderer</code> is a subclass of
 * <code>javax.swing.JLabel</code>, which is the default (which is
 * <code>javax.swing.tree.DefaultTreeCellRenderer</code>).  If you've
 * implemented your own <code>TreeCellRenderer</code> class that is not a
 * subclass of <code>JLabel</code> (or of <code>DefaultTreeCellRenderer</code>),
 * then tooltips will be placed in the location Swing thinks is best
 * suitable.<p>
 *
 * By default, the displayed tooltip is the text of the armed node.  To change
 * the tooltip text, override the {@link #getToolTipText} method.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class ToolTipTree extends JTree {

	private static final long serialVersionUID = 1L;

	private boolean toolTipsEnabled;


	public ToolTipTree() {
		initialize();
	}


	public ToolTipTree(Hashtable<?, ?> value) {
		super(value);
		initialize();
	}


	public ToolTipTree(Object[] value) {
		super(value);
		initialize();
	}


	public ToolTipTree(TreeModel newModel) {
		super(newModel);
		initialize();
	}


	public ToolTipTree(TreeNode root) {
		super(root);
		initialize();
	}


	public ToolTipTree(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
		initialize();
	}


	public ToolTipTree(Vector<?> value) {
		super(value);
		initialize();
	}


	/**
	 * Returns the location at which to place the tooltip generated from the
	 * given mouse event.
	 *
	 * @param e The mouse event.
	 * @return The location for the tooltip.
	 */
	@Override
	public Point getToolTipLocation(MouseEvent e) {
		Point p = null;
		int row = getRowForLocation(e.getX(), e.getY());
		if (row!=-1) {
			TreePath path = getPathForRow(row);
			if (path!=null) {
				Object comp = path.getLastPathComponent();
				Component c = cellRenderer.getTreeCellRendererComponent(
									this, comp,
									isRowSelected(row),
									!isCollapsed(row),
									getModel().isLeaf(comp),
									row,
									false); // hasFocus ???
				// NOTE:  Since DefaultTreeCellRenderer extends JLabel, and
				// JLabel provides all of the functionality we need to
				// compute the tooltip location, and the user may implement
				// their own TreeCellRenderer and extend JLabel, we just
				// check for JLabel instances here.
				if (c instanceof JLabel) {
					JLabel renderer = (JLabel)c;
					Icon icon = renderer.getIcon();
					int xOffset = icon==null ? 0 :
						(icon.getIconWidth()+renderer.getIconTextGap());
					Rectangle bounds = getRowBounds(row);
					p = new Point(bounds.x+xOffset, bounds.y);
				}
			}
		}
		return p; // If p==null, then Swing picks "suitable" location.
	}


	/**
	 * Returns whether tooltips are enabled.
	 *
	 * @return Whether tooltips are enabled for this tree.
	 * @see #setToolTipsEnabled
	 */
	public boolean getToolTipsEnabled() {
		return toolTipsEnabled;
	}


	/**
	 * Returns the string to use for the tooltip.  By default, this method
	 * returns the text value of the armed node.
	 *
	 * @return The tooltip text.
	 */
	@Override
	public String getToolTipText(MouseEvent e) {
		String tip = null;
		int x = e.getX();
		int y = e.getY();
		TreePath path = getPathForLocation(x, y);
		if (path!=null) {
			// If last component is a DefaultMutableTreeNode, toString()
			// returns the toString() of the contained userObject.
			tip = path.getLastPathComponent().toString();
		}
		return tip;
	}


	/**
	 * Does any extra initialization.
	 */
	protected void initialize() {
		setToolTipsEnabled(true);
	}


	/**
	 * Sets whether tooltips are enabled.
	 *
	 * @param enabled Whether tooltips should be enabled for this tree.
	 * @see #getToolTipsEnabled
	 */
	public void setToolTipsEnabled(boolean enabled) {
		toolTipsEnabled = enabled;
		if (toolTipsEnabled)
			ToolTipManager.sharedInstance().registerComponent(this);
		else
			ToolTipManager.sharedInstance().unregisterComponent(this);
	}


}