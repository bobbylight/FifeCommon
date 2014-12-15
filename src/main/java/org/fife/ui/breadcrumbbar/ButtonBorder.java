/*
 * 09/28/2009
 *
 * ButtonBorder.java - The border for breadcrumb bar buttons.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.breadcrumbbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.plaf.basic.BasicBorders;


/**
 * Border for the buttons in a breadcrumb bar.  It honors the margins of the
 * button.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ButtonBorder extends BasicBorders.MarginBorder {


	private static final boolean isArrowActivated(AbstractButton b) {
		return Boolean.TRUE==b.getClientProperty(BreadcrumbBar.ARROW_ACTIVATED);
	}


	private static final boolean isArrowSelected(AbstractButton b) {
		return Boolean.TRUE==b.getClientProperty(BreadcrumbBar.ARROW_SELECTED);
	}


	/**
	 * Paints this border.
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y,
							int w, int h) {
		AbstractButton b = (AbstractButton)c;
		Color color = (Color)b.getClientProperty("breadcrumbBorderColor");
		if (isArrowActivated(b) || isArrowSelected(b)) {
			g.setColor(color);
			g.drawLine(x,y, x,y+h-1);
		}
		else if (b.getModel().isRollover() || b.getModel().isArmed() ||
				b.getModel().isPressed() || b.isSelected()) {
			g.setColor(color);
			g.drawLine(x,y, x,y+h-1);
			x += w-1;
			g.drawLine(x,y, x,y+h-1);
		}
	}


}