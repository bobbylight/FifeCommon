/*
 * 09/28/2009
 *
 * BreadcrumbBarToggleButton.java - A toggle button for a breadcrumb bar.
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

import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.plaf.ButtonUI;


/**
 * Button that always uses a special UI.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class BreadcrumbBarToggleButton extends JToggleButton {


	public BreadcrumbBarToggleButton(Icon icon) {
		super(icon);
	}


	public void setUI(ButtonUI ui) {
		super.setUI(new BreadcrumbBarToggleButtonUI());
	}


}