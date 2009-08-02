/*
 * 11/27/2004
 *
 * MaxWidthComboBox.java - A combo box with a maximum width, to avoid pesky
 * layout problems when the combo contains lengthy strings.
 * Copyright (C) 2004 Robert Futrell
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

import java.awt.Dimension;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;


/**
 * A combo box whose width cannot go over a specified value.  This class is
 * useful when you have a layout manager that adheres to the combo box's
 * preferred/maximum sizes (such as <code>SpringLayout</code>), and your
 * combo box contains a value longer than you'd like - the combo box is drawn
 * too large and the GUI looks ugly.  With this class you can set a maximum
 * width for the combo box, and its height will never be affected.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class MaxWidthComboBox extends JComboBox {

	private static final long serialVersionUID = 1L;

	/**
	 * The width of this combo box will never be greater than this value.
	 */
	private int maxWidth;


	/**
	 * Constructor.
	 *
	 * @param maxWidth The maximum width for this combo box.
	 */
	public MaxWidthComboBox(int maxWidth) {
		this.maxWidth = maxWidth;
	}


	/**
	 * Constructor.
	 *
	 * @param model The model for this combo box.
	 * @param maxWidth The maximum width for this combo box.
	 */
	public MaxWidthComboBox(ComboBoxModel model, int maxWidth) {
		super(model);
		this.maxWidth = maxWidth;
	}


	/**
	 * Overridden to ensure that the returned size has width no greater than
	 * the specified maximum.
	 *
	 * @return The maximum size of this combo box.
	 */
	public Dimension getMaximumSize() {
		Dimension size = super.getMaximumSize();
		size.width = Math.min(size.width, maxWidth);
		return size;
	}


	/**
	 * Overridden to ensure that the returned size has width no greater than
	 * the specified maximum.
	 *
	 * @return The minimum size of this combo box.
	 */
	public Dimension getMinimumSize() {
		Dimension size = super.getMinimumSize();
		size.width = Math.min(size.width, maxWidth);
		return size;
	}


	/**
	 * Overridden to ensure that the returned size has width no greater than
	 * the specified maximum.
	 *
	 * @return The preferred size of this combo box.
	 */
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		size.width = Math.min(size.width, maxWidth);
		return size;
	}


}