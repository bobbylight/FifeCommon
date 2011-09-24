/*
 * 09/24/2011
 *
 * AbstractPlugin.java - Base class for Plugins that don't need to extend a
 * specific class.
 * Copyright (C) 2011 Robert Futrell
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
package org.fife.ui.app;


/**
 * Base class for <code>Plugin</code> implementations that don't need to 
 * extend a specific class.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see StatusBarPlugin
 * @see GUIPlugin
 */
public abstract class AbstractPlugin implements Plugin {


	private String parentOptionPanelID;


	/**
	 * {@inheritDoc}
	 */
	public String getOptionsDialogPanelParentPanelID() {
		return parentOptionPanelID;
	}


	/**
	 * Sets the option panel to add this plug-in's option panels to.
	 *
	 * @param id The ID of the parent panel, or <code>null</code> if there
	 *        should be no parent.
	 * @see #getOptionsDialogPanelParentPanelID()
	 */
	public void setOptionsDialogPanelParentPanelID(String id) {
		parentOptionPanelID = id;
	}


}