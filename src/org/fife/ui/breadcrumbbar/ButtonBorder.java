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


	/**
	 * Paints this border.
	 */
	public void paintBorder(Component c, Graphics g, int x, int y,
							int w, int h) {
		AbstractButton b = (AbstractButton)c;
		if (b.getModel().isRollover() || b.getModel().isArmed() ||
				b.isSelected()) {
			g.setColor(Color.BLACK);
			g.drawLine(x,y, x,y+h-1);
			x += w-1;
			g.drawLine(x,y, x,y+h-1);
		}
		else if (Boolean.TRUE==b.getClientProperty(BreadcrumbBar.ARROW_ACTIVATED)) {
			g.setColor(Color.BLACK);
			g.drawLine(x,y, x,y+h-1);
		}
		else if (Boolean.TRUE==b.getClientProperty(BreadcrumbBar.ARROW_SELECTED)) {
			g.setColor(Color.BLACK);
			g.drawLine(x,y, x,y+h-1);
		}
	}


}