/*
 * 09/07/2006
 *
 * SizeGripIcon.java - An icon that paints a size grip.
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import javax.swing.*;


/**
 * An icon that looks like a Windows 98/XP-style size grip.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SizeGripIcon implements Icon {

	private static final int SIZE = 20;

	private int style;


	/**
	 * Constructor.
	 */
	public SizeGripIcon() {
		this(-1); // Important for StatusBar
	}


	/**
	 * Constructor.
	 *
	 * @param style The style of the size grip.
	 */
	public SizeGripIcon(int style) {
		setStyle(style);
	}


	/**
	 * Returns the height of this icon.
	 *
	 * @return This icon's height.
	 */
	public int getIconHeight() {
		return SIZE;
	}


	/**
	 * Returns the width of this icon.
	 *
	 * @return This icon's width.
	 */
	public int getIconWidth() {
		return SIZE;
	}


	/**
	 * Returns the current style of this icon.
	 *
	 * @return The icon's style.
	 * @see #setStyle(int)
	 */
	public int getStyle() {
		return style;
	}


	/**
	 * Paints this icon.
	 *
	 * @param c The component to paint on.
	 * @param g The graphics context.
	 * @param x The x-coordinate at which to paint.
	 * @param y The y-coordinate at which to paint.
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {

		Dimension dim = c.getSize();
		Color c1 = UIManager.getColor("Label.disabledShadow");
		Color c2 = UIManager.getColor("Label.disabledForeground");

		ComponentOrientation orientation = c.getComponentOrientation();

		switch (style) {

			case StatusBar.WINDOWS_98_STYLE:
				// TODO: This is broken and only works with StatusBars
				// because they give it their own JPanel.  Widths should
				// be relative to the "end" of the panel!
				int width = dim.width;
				int height = dim.height;
				g.setColor(c1.brighter());
				g.drawLine(7,height, width,7);
				g.drawLine(11,height, width,11);
				g.drawLine(15,height, width,15);
				g.setColor(c2.darker());
				g.drawLine(8,height, width,8);
				g.drawLine(12,height, width,12);
				g.drawLine(16,height, width,16);
				break;

			default:// StatusBar.WINDOWS_XP_STYLE:
				if (orientation.isLeftToRight()) {
					width = dim.width  -= 3;
					height = dim.height -= 3;
					g.setColor(c1);
					g.fillRect(width-9,height-1, 3,3);
					g.fillRect(width-5,height-1, 3,3);
					g.fillRect(width-1,height-1, 3,3);
					g.fillRect(width-5,height-5, 3,3);
					g.fillRect(width-1,height-5, 3,3);
					g.fillRect(width-1,height-9, 3,3);
					g.setColor(c2);
					g.fillRect(width-9,height-1, 2,2);
					g.fillRect(width-5,height-1, 2,2);
					g.fillRect(width-1,height-1, 2,2);
					g.fillRect(width-5,height-5, 2,2);
					g.fillRect(width-1,height-5, 2,2);
					g.fillRect(width-1,height-9, 2,2);
				}
				else {
					height = dim.height -= 3;
					g.setColor(c1);
					g.fillRect(10,height-1, 3,3);
					g.fillRect(6,height-1, 3,3);
					g.fillRect(2,height-1, 3,3);
					g.fillRect(6,height-5, 3,3);
					g.fillRect(2,height-5, 3,3);
					g.fillRect(2,height-9, 3,3);
					g.setColor(c2);
					g.fillRect(10,height-1, 2,2);
					g.fillRect(6,height-1, 2,2);
					g.fillRect(2,height-1, 2,2);
					g.fillRect(6,height-5, 2,2);
					g.fillRect(2,height-5, 2,2);
					g.fillRect(2,height-9, 2,2);
				}
				break;

		}

	}


	/**
	 * Sets the style of this icon.
	 *
	 * @param style This icon's style.
	 * @see #getStyle()
	 */
	public void setStyle(int style) {
		this.style = style;
	}


}