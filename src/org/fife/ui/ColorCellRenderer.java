/*
 * 02/24/2004
 *
 * ColorCellRenderer.java - Renderer for a JTable that displays
 * a color.
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

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * A renderer for a <code>JTable</code> that displays a color.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ColorCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;


	/**
	 * Constructor.
	 */
	public ColorCellRenderer() {
		setOpaque(true);
	}


	/**
	 * Returns the color displayed.  This method is here so that subclasses
	 * can be "displaying" a different color than the background color (as this
	 * class does).
	 *
	 * @return The "displayed" color.
	 */
	public Color getDisplayedColor() {
		return getBackground();
	}


	/**
	 * Returns the component (this object itself) to use to display the cell.
	 */
	public Component getTableCellRendererComponent(JTable table, Object color,
								boolean isSelected, boolean hasFocus,
								int row, int column) {
		Color newColor = (Color)color;
		setBackground(newColor);
		return this;
	}


	/**
	 * Returns the tooltip text for this renderer, which is the RGB value
	 * of the renderer pointed to.
	 *
	 * @return The tooltip text.
	 */
	public String getToolTipText() {
		Color color = getDisplayedColor();
		return "RGB: " + color.getRed() + ", " +
					color.getGreen() + ", " +
					color.getBlue();
	}


}