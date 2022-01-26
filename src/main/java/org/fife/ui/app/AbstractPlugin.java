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

import javax.swing.*;


/**
 * Base class for <code>Plugin</code> implementations that don't need to
 * extend a specific class.
 *
 * @author Robert Futrell
 * @version 1.0
 * @param <T> The type of parent application.
 * @see StatusBarPlugin
 * @see GUIPlugin
 */
public abstract class AbstractPlugin<T extends GUIApplication> implements Plugin<T> {

	private T application;
	private String parentOptionPanelID;


	protected AbstractPlugin(T app) {
		this.application = app;
		UIManager.addPropertyChangeListener(e -> {
			String property = e.getPropertyName();
			if ("lookAndFeel".equals(property)) {
				lookAndFeelChanged((LookAndFeel)e.getNewValue());
			}
		});
	}


	@Override
	public T getApplication() {
		return application;
	}


	@Override
	public String getOptionsDialogPanelParentPanelID() {
		return parentOptionPanelID;
	}


	/**
	 * Called when the LookAndFeel changes.  This is a hook for plugins to
	 * manually update their popup dialogs.  Subclasses should override if
	 * necessary, but always call the super implementation.
	 *
	 * @param newLaf The new <code>LookAndFeel</code>.
	 */
	protected void lookAndFeelChanged(LookAndFeel newLaf) {
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
