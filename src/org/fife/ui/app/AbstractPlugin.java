/*
 * 09/24/2011
 *
 * AbstractPlugin.java - Base class for Plugins that don't need to extend a
 * specific class.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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