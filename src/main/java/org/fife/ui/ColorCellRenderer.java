/*
 * 02/24/2004
 *
 * ColorCellRenderer.java - Renderer for a JTable that displays
 * a color.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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
	@Override
	public Component getTableCellRendererComponent(JTable table, Object color,
								boolean isSelected, boolean hasFocus,
								int row, int column) {
		Color newColor = (Color)color;
		setBackground(newColor);
		return this;
	}


	/**
	 * Returns the tool tip text for this renderer, which is the RGB value
	 * of the renderer pointed to.
	 *
	 * @return The tool tip text.
	 */
	@Override
	public String getToolTipText() {
		return getToolTipText(getDisplayedColor());
	}


	/**
	 * Returns the tool tip text for a renderer displaying this color.
	 *
	 * @param color The color.
	 * @return The tool tip text.
	 */
	public static String getToolTipText(Color color) {
		return "RGB: " + color.getRed() + ", " +
				color.getGreen() + ", " +
				color.getBlue();
	}


}