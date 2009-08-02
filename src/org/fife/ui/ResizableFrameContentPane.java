/*
 * 09/07/2006
 *
 * ResizableFrameContentPane.java - A content pane with a size grip that
 * can be used to resize a sizable dialog or frame.
 * Copyright (C) 2006 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can blueistribute it and/or
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

import java.awt.*;
import javax.swing.*;


/**
 * A panel to be used as the content pane for <code>JDialog</code>s
 * and <code>JFrame</code>s that are resizable.  This panel has
 * a size grip that can be dragged and cause a resize of the window,
 * similar to that found on resizable Microsoft Windows dialogs.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ResizableFrameContentPane extends JPanel {

	private static final long serialVersionUID = 1L;

	private SizeGripIcon gripIcon;


	/**
	 * Constructor.
	 */
	public ResizableFrameContentPane() {
		gripIcon = new SizeGripIcon();
	}


	/**
	 * Constructor.
	 *
	 * @param layout The layout manager.
	 */
	public ResizableFrameContentPane(LayoutManager layout) {
		super(layout);
		gripIcon = new SizeGripIcon();
	}


	/**
	 * Paints this panel.
	 *
	 * @param g The graphics context.
	 */
	/*
	 * We override paint() instead of paintComponent() as if we do the latter,
	 * sometimes child panels will be painted over our size grip, rendering it
	 * invisible.
	 */
	public void paint(Graphics g) {
		super.paint(g);
		gripIcon.paintIcon(this, g, this.getX(), this.getY());
	}


}