/*
 * 09/08/2004
 *
 * LineDividerBorder.java - A border that is really just a line "divider" on
 * one of the component's sides.
 * Copyright (C) 2004 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.SwingConstants;
import javax.swing.border.Border;


/**
 * A "border" that is actually just a single-line "divider" like those found in many
 * flat applications these days.  The border can be placed on any of the four
 * sides of a component.
 *
 * @author Robert Futrell
 * @version 0.9
 */
public class LineDividerBorder implements Border, SwingConstants {

	private int location;
	private int slack;
	private Color color;
	private Insets insets;


	/**
	 * Creates a bevel divider border on the specified side of the panel.
	 *
	 * @param location One of {@code SwingConstants.TOP},
	 *        {@code LEFT}, {@code BOTTOM}, or {@code RIGHT}.
	 */
	public LineDividerBorder(int location) {
		this(location, 0);
	}


	/**
	 * Creates a bevel divider border on the specified side of the panel with
	 * the specified "slack" on either side.
	 *
	 * @param location One of {@code SwingConstants.TOP},
	 *        {@code LEFT}, {@code BOTTOM}, or {@code RIGHT}.
	 * @param slack How much empty space to leave on either side of the
	 *        divider.
	 */
	public LineDividerBorder(int location, int slack) {
		this(location, slack, null);
	}


	/**
	 * Creates a bevel divider border on the specified side of the panel with
	 * the specified "slack" on either side.
	 *
	 * @param location One of {@code SwingConstants.TOP},
	 *        {@code LEFT}, {@code BOTTOM}, or {@code RIGHT}.
	 * @param slack How much empty space to leave on either side of the
	 *        divider.
	 * @param color The color to use in the divider.  If this is {@code null},
	 *        a color complimentary to the LaF will be used.
	 */
	public LineDividerBorder(int location, int slack, Color color) {

		if (location<TOP || location>RIGHT)
			location = BOTTOM;

		this.location = location;
		this.slack = slack;
		this.color = color;

		insets = new Insets(0,0,0,0);
		switch (location) {
			case TOP -> insets.top = 2;
			case LEFT -> insets.left = 2;
			case BOTTOM -> insets.bottom = 2;
			case RIGHT -> insets.right = 2;
		}

	}


	/**
	 * Returns the insets of the border.
	 *
	 * @param c Not used.
	 */
	@Override
	public Insets getBorderInsets(Component c) {
		return insets;
	}


	/**
	 * Returns whether or not the border is opaque.
	 *
	 * @return This method always returns {@code true}.
	 */
	@Override
	public boolean isBorderOpaque() {
		return true;
	}


	/**
	 * Paints the border for the specified component with the specified
	 * position and size.
	 *
	 * @param c The component that has this border.
	 * @param g The graphics context with which to paint.
	 * @param x The x-coordinate of the border.
	 * @param y The y-coordinate of the border.
	 * @param width The width of the component.
	 * @param height The height of the component.
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
						int height) {

		// If the divider would be "inverted" or not show up, just don't
		// bother painting.
		if (width <= 2*slack)
			return;

		int x1;
		int y1;
		int x2;
		int y2;

		switch (location) {

			case TOP:
				x1 = x + slack;
				y1 = y;
				x2 = (x+width-slack) - 2;
				y2 = y;
				break;

			case LEFT:
				x1 = x;
				y1 = y + slack;
				x2 = x;
				y2 = (y+height-slack) - 2;
				break;

			case BOTTOM:
				x1 = x + slack;
				y1 = y + height - 2;
				x2 = (x+width-slack) - 2;
				y2 = y1;
				break;

			default: // case RIGHT:
				x1 = x + width - 2;
				y1 = y + slack;
				x2 = x1;
				y2 = (y+height-slack) - 2;
				break;

		}

		Color dividerColor = color;
		if (dividerColor == null) {
			boolean isDarkLaf = UIUtil.isLightForeground(c.getForeground());
			dividerColor = isDarkLaf ? c.getBackground().brighter() :
				c.getBackground().darker();
		}

		g.setColor(dividerColor);
		g.drawLine(x1,y1, x2,y2);
	}


}
