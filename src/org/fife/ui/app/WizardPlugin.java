/*
 * 02/13/2005
 *
 * WizardPlugin.java - A base class for creating dialog-based wizard plugins.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * A base plugin for creating dialog-based wizards.
 *
 * @author Robert Futrell
 * @version 0.1
 * @see org.fife.ui.app.Plugin
 * @see org.fife.ui.app.GUIApplication
 */
public abstract class WizardPlugin implements Plugin, GUIApplicationConstants {

	public static final int CANCEL	= 0;
	public static final int SUCCESSFUL	= 1;

	private GUIApplication app;
	private WizardPluginDialog dialog;
	private List<WizardDialogInfoPanel> infoPanels;


	/**
	 * Constructor.
	 *
	 * @param app The GUI application.
	 */
	public WizardPlugin(GUIApplication app) {
		this.app = app;
		infoPanels = new ArrayList<WizardDialogInfoPanel>(3);
	}


	/**
	 * Adds a "step" to this wizard.  The first panel passed to this method
	 * will become the panel displayed as "step 1", the second as "step 2",
	 * etc.  Thus, you must pass the panels for your "steps" to this method
	 * in order.
	 *
	 * @param panel The panel to add.
	 * @return The "step number" that this panel will be.  Note that this
	 *         value is 1-based.
	 * @see #getInfoPanel
	 */
	protected int addInfoPanel(WizardDialogInfoPanel panel) {
		infoPanels.add(panel);
		return infoPanels.size();
	}


	/**
	 * This method should be overridden to initialize all panels in this
	 * wizard via <code>addInfoPanel</code>.  Note that you only need to add
	 * the "step" panels in this way, not the "introduction" or "successful"
	 * panels.
	 */
	protected abstract void createPanels();


	/**
	 * Returns the "introduction panel;" that is, the initial information
	 * displayed to the user explain what this wizard will do.  For example,
	 * <p>
	 *
	 *   <pre>This wizard will walk you through creating a simple Java
	 * class.</pre>
	 *
	 * @param dialog The dialog in which this information will be displayed.
	 * @return The panel.
	 * @see #getInfoPanel
	 * @see #getWizardSuccessfulPanel
	 */
	protected abstract WizardDialogInfoPanel getIntroductionPanel(
										WizardPluginDialog dialog);


	/**
	 * Returns the specified info panel.  Each panel corresponds to a single
	 * "step" in the wizard.
	 *
	 * @param index The "step number."  This value will be 1-based; that is,
	 *        it will be in the range <code>1-getStepCount()</code>.
	 * @param dialog The dialog in which this information will be displayed.
	 * @return The specified info panel.
	 * @see #getIntroductionPanel
	 * @see #getWizardSuccessfulPanel
	 * @see #addInfoPanel
	 */
	protected WizardDialogInfoPanel getInfoPanel(int index,
										WizardPluginDialog dialog) {
		if (index<=0 || index>getStepCount())
			throw new IllegalArgumentException("Invalid index: " + index);
		return infoPanels.get(index-1);
	}


	/**
	 * Returns an options panel for use in an Options dialog.  This panel
	 * should contain all options pertaining to this plugin.
	 *
	 * @return The options panel.
	 */
	public abstract PluginOptionsDialogPanel getOptionsDialogPanel();


	/**
	 * Returns the image to use on the side of the dialog.  This image should
	 * be taller than it is wide (perhaps around three time taller) to match
	 * the appearance of Microsoft Windows-style wizards.  If this method is
	 * not overridden, a bad-looking default icon will be used.
	 *
	 * @return The icon to use.
	 */
	public Icon getSideIcon() {
		return new ImageIcon() {
			@Override
			public int getIconHeight() {
				return 300;
			}
			@Override
			public int getIconWidth() {
				return 120;
			}
			// FIXME:  Make me a gradient.
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				g.setColor(Color.BLUE);
				g.fillRect(x,y, getIconWidth(),getIconHeight());
			}
		};
	}


	/**
	 * Returns the number of steps in this wizard.  This number should be
	 * the number of times the user will have to click "Next".
	 *
	 * @return The number of steps in this wizard.
	 */
	public final int getStepCount() {
		return infoPanels.size();
	}


	/**
	 * Returns the title of the Wizard dialog.
	 *
	 * @return The title of the wizard dialog.
	 */
	public abstract String getWizardDialogTitle();


	/**
	 * Returns the dialog information to display when the wizard has
	 * completed successfully.
	 *
	 * @param dialog The dialog in which this information will be displayed.
	 * @return The panel.
	 * @see #getIntroductionPanel
	 * @see #getInfoPanel
	 */
	protected abstract WizardDialogInfoPanel getWizardSuccessfulPanel(
										WizardPluginDialog dialog);


	/**
	 * This method is called if the user runs the wizard and clicks the
	 * "Finish" button; e.g., the wizard has run to completion.  Subclasses
	 * should override this method to perform whatever action the wizard was
	 * gathering information about.
	 *
	 * @param panels The "step" panels in which the user entered values.
	 */
	protected abstract void handleWizardSuccessful(
									WizardDialogInfoPanel[] panels);


	/**
	 * Returns whether or not the user should be prompted to confirm a
	 * "cancel" request by the user.  Override this method if you don't
	 * want the user to be prompted if they click "Cancel."
	 *
	 * @return Whether the user should be prompted whether or not they
	 *         really want to cancel.
	 */
	public boolean promptBeforeCancel() {
		return true;
	}


	/**
	 * Starts the wizard.
	 *
	 * @return Either <code>CANCEL</code> or <code>SUCCESSFUL</code>,
	 *         depending on whether the user cancelled the wizard.
	 */
	public final int runWizard() {

		// We must create a new set of panels each time to clear out old
		// values, and to ensure that if the LnF changed, our panels reflect
		// the current LnF (since they don't have a parent, they cannot be
		// updated).
		infoPanels.clear();
		createPanels();

		if (app instanceof AbstractGUIApplication)
			dialog = new WizardPluginDialog(
							(AbstractGUIApplication)app, this);
		else
			dialog = new WizardPluginDialog(this);
		String title = getWizardDialogTitle();
		dialog.setTitle(title);
		dialog.setLocationRelativeTo(null);
		int rc = dialog.runWizard();

		if (rc==SUCCESSFUL) {
			WizardDialogInfoPanel[] wdip =
					new WizardDialogInfoPanel[infoPanels.size()];
			wdip = infoPanels.toArray(wdip);
			handleWizardSuccessful(wdip);
		}

		return rc;

	}


}