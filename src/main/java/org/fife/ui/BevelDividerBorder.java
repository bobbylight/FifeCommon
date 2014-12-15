/*
 * 09/08/2004
 *
 * BevelDividerBorder.java - A border that is really just a bevel "divider" on
 * one of the component's sides.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
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
 * A "border" that is actually just a bevel "divider" like those found in many
 * Microsoft Windows applications.  The border can be placed on any of the four
 * sides of a component.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class BevelDividerBorder implements Border, SwingConstants {
	
	private int location;
	private int slack;
	private Color light;
	private Color dark;
	private Insets insets;


	/**
	 * Creates a bevel divider border on the specified side of the panel.
	 *
	 * @param location One of <code>SwingConstants.TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code>, or <code>RIGHT</code>.
	 */
	public BevelDividerBorder(int location) {
		this(location, 0);
	}


	/**
	 * Creates a bevel divider border on the specified side of the panel with
	 * the specified "slack" on either side.
	 *
	 * @param location One of <code>SwingConstants.TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code>, or <code>RIGHT</code>.
	 * @param slack How much empty space to leave on either side of the
	 *        divider.
	 */
	public BevelDividerBorder(int location, int slack) {
		this(location, slack, null, null);
	}


	/**
	 * Creates a bevel divider border on the specified side of the panel with
	 * the specified "slack" on either side.
	 *
	 * @param location One of <code>SwingConstants.TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code>, or <code>RIGHT</code>.
	 * @param slack How much empty space to leave on either side of the
	 *        divider.
	 * @param light The lighter color to use in the bevel.
	 * @param dark The darker color to use in the bevel.
	 */
	public BevelDividerBorder(int location, int slack, Color light,
							Color dark) {

		if (location<TOP || location>RIGHT)
			location = BOTTOM;

		this.location = location;
		this.slack = slack;
		this.light = light;
		this.dark = dark;

		insets = new Insets(0,0,0,0);
		switch (location) {
			case TOP:
				insets.top = 2;
				break;
			case LEFT:
				insets.left = 2;
				break;
			case BOTTOM:
				insets.bottom = 2;
				break;
			case RIGHT:
				insets.right = 2;
		}

	}


	/**
	 * Returns the insets of the border.
	 *
	 * @param c Not used.
	 */
	public Insets getBorderInsets(Component c) {
		return insets;
	}


	/**
	 * Returns whether or not the border is opaque.
	 *
	 * @return This method always returns <code>true</code>.
	 */
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
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
						int height) {

		// If the divider would be "inverted" or not show up, just don't
		// bother painting.
		if (width <= 2*slack)
			return;

		int x1, y1, x2, y2;
		int xinc, yinc;

		switch (location) {

			case TOP:
				x1 = x + slack;
				y1 = y;
				x2 = (x+width-slack) - 2;
				y2 = y;
				xinc = 0;
				yinc = 1;
				break;

			case LEFT:
				x1 = x;
				y1 = y + slack;
				x2 = x;
				y2 = (y+height-slack) - 2;
				xinc = 1;
				yinc = 0;
				break;

			case BOTTOM:
				x1 = x + slack;
				y1 = y + height - 2;
				x2 = (x+width-slack) - 2;
				y2 = y1;
				xinc = 0;
				yinc = 1;
				break;

			default: // case RIGHT:
				x1 = x + width - 2;
				y1 = y + slack;
				x2 = x1;
				y2 = (y+height-slack) - 2;
				xinc = 1;
				yinc = 0;
				break;

		}

		g.setColor(dark==null ? c.getBackground().darker() : dark);
		g.drawLine(x1,y1, x2,y2);
		g.setColor(light==null ? c.getBackground().brighter() : light);
		g.drawLine(x1+xinc,y1+yinc, x2+xinc,y2+yinc);
		
	}


}