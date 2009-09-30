/*
 * 09/26/2009
 *
 * BreadcrumbBarButtonUI.java - UI for a breadcrumb bar's buttons.
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
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;


/**
 * The UI for the buttons in a breadcrumb bar.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class BreadcrumbBarButtonUI extends BasicButtonUI {


	protected void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		b.setMargin(new Insets(5, 4, 5, 4));
		b.setBorder(new ButtonBorder());
		b.setRolloverEnabled(true); // Not true by default
		b.setFocusable(false); // Prevent JRootPane default button issues
		b.setOpaque(false);
	}
   

	public void paint(Graphics g, JComponent c) {

		AbstractButton b = (AbstractButton)c;
		ButtonModel model = b.getModel();
		if (Boolean.TRUE==b.getClientProperty(BreadcrumbBar.ARROW_SELECTED)) {
			Color c1 = new Color(194, 228, 246);
			Color c2 = new Color(146, 204, 235);
			g.setColor(c1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(c2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
		}
		else if (Boolean.TRUE==b.getClientProperty(BreadcrumbBar.ARROW_ACTIVATED)) {
			Color c1 = new Color(240,240,240);
			Color c2 = new Color(212,212,212);
			g.setColor(c1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(c2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
		}
		else if (model.isPressed()) {
			Color c1 = new Color(194, 228, 246);
			Color c2 = new Color(146, 204, 235);
			g.setColor(c1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(c2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
		}
		else if (model.isRollover() || model.isArmed()) {
			Color c1 = new Color(223, 242, 252);
			Color c2 = new Color(177, 223, 249);
			g.setColor(c1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(c2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
		}

		super.paint(g, c);

//		g.setColor(java.awt.Color.BLACK);
//		g.drawRect(0, 0, c.getWidth()-1, c.getHeight()-1);

	}


}
