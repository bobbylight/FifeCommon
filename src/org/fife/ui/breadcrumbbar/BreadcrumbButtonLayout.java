/*
 * 09/26/2009
 *
 * BreadcrumbButtonLayout.java - Layout manager for a breadcrumb bar.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.breadcrumbbar;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;


/**
 * The layout manager for buttons in a breadcrumb bar.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class BreadcrumbButtonLayout implements LayoutManager {

	private int vgap;


	public BreadcrumbButtonLayout() {
		vgap = 0;
	}


	/**
	 * {@inheritDoc}
	 */
	public void addLayoutComponent(String name, Component comp) {
	}


	public void layoutContainer(Container parent) {

		synchronized (parent.getTreeLock()) {

			int childCount = parent.getComponentCount();
			int w = parent.getWidth();
			Insets insets = parent.getInsets();
			w -= (insets.left + insets.right);
			w -= parent.getComponent(1).getPreferredSize().width;
			int h = parent.getHeight() - insets.top - insets.bottom;

			// Determine what components we can fit.
			int curW = 0;
			int first = childCount - 1;
			while (first>=3) {
				Component arrow = parent.getComponent(first);
				Component c = parent.getComponent(first-1);
				int tempW = arrow.getPreferredSize().width +
							c.getPreferredSize().width;
				if (curW+tempW<=w) {
					curW += tempW;
					first -= 2;
				}
				else {
					break;
				}
			}
			first++;

			boolean ltr = parent.getComponentOrientation().isLeftToRight();
			int x = ltr ? 0 : w;

			// The first component is always either a root drive arrow or
			// a "back" arrow.
			if (first==2) {
				Component c = parent.getComponent(0); // "root" button.
				c.setVisible(true);
				parent.getComponent(1).setVisible(false);
				w = c.getPreferredSize().width;
				if (!ltr) {
					x -= w;
				}
				c.setBounds(x, 0, w, h);
				if (ltr) {
					x += w;
				}
			}
			else { // "back" button
				Component c = parent.getComponent(1); // "root" button.
				c.setVisible(true);
				parent.getComponent(0).setVisible(false);
				w = c.getPreferredSize().width;
				if (!ltr) {
					x -= w;
				}
				c.setBounds(x, 0, w, h);
				if (ltr) {
					x += w;
				}
			}
			for (int i=2; i<first; i++) {
				Component c = parent.getComponent(i);
				c.setVisible(false);
			}
			for (int i=first; i<childCount; i++) {
				Component c = parent.getComponent(i);
				c.setVisible(true);
				w = c.getPreferredSize().width;
				if (!ltr) {
					x -= w;
				}
				c.setBounds(x, 0, w, h);
				if (ltr) {
					x += w;
				}
			}

		}

	}


	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}


	/**
	 * {@inheritDoc}
	 */
	public Dimension preferredLayoutSize(Container parent) {

		synchronized (parent.getTreeLock()) {

			Dimension dim = new Dimension(0, parent.getHeight());
			int nmembers = parent.getComponentCount();

			for (int i = 0 ; i < nmembers ; i++) {
				Component m = parent.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getPreferredSize();
					dim.width += d.width;
					dim.height = Math.max(dim.height, d.height);
				}
			}

			Insets insets = parent.getInsets();
			// Don't use width of all directories for preferred width, as
			// this can cause layout headaches.  Try adding a BreadcrumbBar
			// showing a long directory path in BorderLayout.NORTH, the panel
			// will want to be long enough to display the entire directory
			// path.
			//dim.width += insets.left + insets.right;
			dim.width = 250 + insets.left + insets.right;
			dim.height += insets.top + insets.bottom + vgap*2;
			return dim;

		}

	}


	/**
	 * {@inheritDoc}
	 */
	public void removeLayoutComponent(Component comp) {
	}


}