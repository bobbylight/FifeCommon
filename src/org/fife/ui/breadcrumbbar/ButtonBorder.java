/*
 * 09/28/2009
 *
 * ButtonBorder.java - The border for breadcrumb bar buttons.
 * Copyright (C) 2009 Robert Futrell
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