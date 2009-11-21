/*
 * 02/09/2006
 *
 * DockableWindowScrollPane.java - A scrollpane that never draws borders.
 * Copyright (C) 2006 Robert Futrell
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
package org.fife.ui.dockablewindows;

import java.awt.Component;
import javax.swing.border.Border;

import org.fife.ui.RScrollPane;


/**
 * A scroll pane to go into a dockable window.  This scroll pane never draws
 * any borders, to avoid the ugly compound/bevel border effect of nesting
 * components.<p>
 *
 * Components going into a {@link DockableWindow} should put themselves inside
 * instances of this class.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class DockableWindowScrollPane extends RScrollPane {


	/**
	 * Constructor.
	 */
	public DockableWindowScrollPane() {
	}


	/**
	 * Constructor.
	 *
	 * @param view The view for this scroll pane.
	 */
	public DockableWindowScrollPane(Component view) {
		super(view);
	}


	/**
	 * Overridden so the border is never set.
	 *
	 * @param b This parameter is ignored.
	 */
	public void setBorder(Border b) {
		super.setBorder(null);
	}


}