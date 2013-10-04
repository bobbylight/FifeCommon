/*
 * 02/13/2005
 *
 * WizardDialogInfoPanel.java - The type of panel you add to a
 * WizardPluginDialog.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.fife.ui.UIUtil;


/**
 * The type of panel you add via a <code>WizardPlugin</code> to a
 * <code>WizardPluginDialog</code>.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class WizardDialogInfoPanel extends JPanel {

	private String header;


	/**
	 * Constructor.
	 *
	 * @param header The header for this panel.
	 */
	public WizardDialogInfoPanel(String header) {
		setHeader(header);
		setBorder(UIUtil.getEmpty5Border());
	}


	/**
	 * Returns the "header text" for this dialog panel.  The header text
	 * is the text displayed at the top of the panel, and is usually a
	 * short description of the step represented by this panel.
	 *
	 * @return The panel's header.
	 * @see #setHeader(String)
	 */
	public String getHeader() {
		return header;
	}


	/** 
	 * Returns the preferred size of this panel.  This is overridden just
	 * so wizard dialogs don't get too large.
	 *
	 * @return This panel's preferred size.
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (d.width>540) d.width = 540;
		return d;
	}


	/**
	 * Returns the wizard dialog that contains this panel.  Note that this
	 * method can only be called after this panel has been added to a wizard
	 * dialog.
	 *
	 * @return The wizard dialog.
	 */
	protected WizardPluginDialog getWizard() {
		return (WizardPluginDialog)SwingUtilities.windowForComponent(this);
	}


	/**
	 * Called when this panel is "accepted" in the wizard (e.g., the user
	 * clicks "Next" and <code>validateInput</code> returns <code>null</code>).
	 * This gives the panel to do anything "extra" it wants to do.<p>
	 *
	 * The default implementation does nothing.
	 * 
	 * @see #isDisplayed
	 */
	public void isAccepted() {
	}


	/**
	 * Called when this panel is displayed in the wizard.  This gives the
	 * panel to do anything "extra" it wants to do, such as disable the
	 * "Next" button.<p>
	 *
	 * The default implementation does nothing.
	 * @see #isAccepted
	 */
	public void isDisplayed() {
	}


	/**
	 * Returns the "header text" for this dialog panel.  The header text
	 * is the text displayed at the top of the panel, and is usually a
	 * short description of the step represented by this panel.
	 *
	 * @param header The panel's header.
	 * @see #getHeader()
	 */
	public void setHeader(String header) {
		this.header = header;
	}


	/**
	 * Enables or disables the "Next" button in the wizard dialog.  This
	 * method can be used by dialog panels that wish to prevent the user
	 * from advancing to the next step in the wizard without first
	 * completing the current step.
	 *
	 * @param enabled Whether or not the "Next" button should be enabled.
	 */
	protected void setNextButtonEnabled(boolean enabled) {
		getWizard().setNextButtonEnabled(enabled);
	}


	/**
	 * Saves input from the user.  This method is called when the user clicks
	 * the "Next" button after giving any input needed for this panel.  The
	 * panel should use the <code>setWizardProperty</code> method of the
	 * wizard dialog to save entered data.<p>
	 *
	 * The default implementation of this method does nothing.
	 *
	 * @param dialog The wizard dialog.
	 */
	protected void saveUserInput(WizardPluginDialog dialog) {
	}


	/**
	 * Ensures all of the data entered by the user is valid.  If it is,
	 * <code>null</code> should be returned.  If it isn't, then a message
	 * is returned that is displayed, instructing the user on how to
	 * correct the error.
	 *
	 * @return <code>null</code> if all input in this panel is correct,
	 *         or a message explaining an error if errors exist.  The
	 *         default method returns <code>null</code>.
	 */
	public String validateInput() {
		return null;
	}


}