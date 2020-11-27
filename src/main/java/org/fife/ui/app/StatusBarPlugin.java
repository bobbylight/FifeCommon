/*
 * 09/16/2005
 *
 * StatusBarPlugin.java - A plugin for a GUI application's status bar.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import org.fife.ui.StatusBarPanel;
import org.fife.ui.UIUtil;

import javax.swing.*;


/**
 * A plugin representing a component in a status bar.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class StatusBarPlugin extends StatusBarPanel implements Plugin {

	private String parentOptionPanelID;


	@Override
	public String getOptionsDialogPanelParentPanelID() {
		return parentOptionPanelID;
	}


	@Override
	public Icon getPluginIcon() {
		return getPluginIcon(UIUtil.isDarkLookAndFeel());
	}


	/**
	 * Sets the option panel to add this plug-in's option panels to.
	 *
	 * @param id The ID of the parent panel, or <code>null</code> if there
	 *        should be no parent.
	 * @see #getOptionsDialogPanelParentPanelID()
	 */
	public void setOptionsDialogPanelParentPanelName(String id) {
		parentOptionPanelID = id;
	}


}
