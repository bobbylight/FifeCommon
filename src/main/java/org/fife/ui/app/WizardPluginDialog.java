/*
 * 02/13/2005
 *
 * WizardPluginDialog.java - The wizard dialog used by wizard plugins.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.text.StyleContext;

import org.fife.ui.BevelDividerBorder;
import org.fife.ui.UIUtil;


/**
 * The "wizard dialog" used by wizard plugins.  This class provides the layout
 * commonly used by wizards in Microsoft Windows applications.
 *
 * @author Robert Futrell
 * @version 0.2
 */
public class WizardPluginDialog extends JDialog {

	private HeaderPanel infoContainer;
	private CardLayout infoPanelLayout;
	private JPanel infoPanel;

	private JButton cancelButton;
	private JButton backButton;
	private JButton nextButton;
	private JButton finishedButton;

	private WizardPlugin plugin;
	private WizardDialogInfoPanel[] panels;
	private int currentPanel;
	private Map<String, Object> wizardProperties;
	private Listener listener;

	private int retVal;

	private static final ResourceBundle msg = ResourceBundle.getBundle(
									"org.fife.ui.app.WizardDialog");


	public WizardPluginDialog(WizardPlugin plugin) {
		initialize(plugin);
	}


	public WizardPluginDialog(Frame owner, WizardPlugin plugin) {
		super(owner);
		initialize(plugin);
	}


	/**
	 * Returns the plugin that is running this wizard.
	 *
	 * @return The plugin.
	 */
	public WizardPlugin getPlugin() {
		return plugin;
	}


	/**
	 * Retrieves a property stored by one of the wizard panels.
	 *
	 * @param key The property key.
	 * @return The property value.
	 * @see #setWizardProperty
	 */
	public Object getWizardProperty(String key) {
		return wizardProperties.get(key);
	}


	protected void initialize(WizardPlugin plugin) {

		this.plugin = plugin;
		listener = new Listener();
		wizardProperties = new HashMap<String, Object>();

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(UIUtil.getEmpty5Border());

		// Add the button panel at the bottom.
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
							new BevelDividerBorder(SwingConstants.TOP),
							UIUtil.getEmpty5Border()));
		JPanel temp = new JPanel(new GridLayout(1,4, 5,5));
		cancelButton = new JButton(msg.getString("Button.Cancel"));
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(listener);
		temp.add(cancelButton);
		backButton = new JButton(msg.getString("Button.Back"));
		backButton.setActionCommand("Back");
		backButton.addActionListener(listener);
		backButton.setEnabled(false);
		temp.add(backButton);
		nextButton = new JButton(msg.getString("Button.Next"));
		nextButton.setActionCommand("Next");
		nextButton.addActionListener(listener);
		temp.add(nextButton);
		finishedButton = new JButton(msg.getString("Button.Finished"));
		finishedButton.setActionCommand("Finished");
		finishedButton.addActionListener(listener);
		finishedButton.setEnabled(false);
		temp.add(finishedButton);
		buttonPanel.add(temp, BorderLayout.LINE_END);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		// Add the top panel containing the side icon on its left
		// and a panel to contain the info panel in its center.
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(UIUtil.getEmpty5Border());
		JPanel iconPanel = new JPanel();
		iconPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		JLabel iconLabel = new JLabel(plugin.getSideIcon());
		iconLabel.setBorder(BorderFactory.createBevelBorder(
									BevelBorder.LOWERED));
		iconPanel.add(iconLabel);
		topPanel.add(iconPanel, BorderLayout.WEST);
		infoContainer = new HeaderPanel();
		infoPanelLayout = new CardLayout();
		infoPanel = new JPanel(infoPanelLayout);
		infoContainer.add(infoPanel);
		topPanel.add(infoContainer);
		contentPane.add(topPanel);
		setPanels(plugin);

		setContentPane(contentPane);
		getRootPane().setDefaultButton(nextButton);
		setTitle(plugin.getWizardDialogTitle());
		setModal(true);
		setResizable(false);
		pack();

	}


	/**
	 * Runs the wizard represented by this wizard plugin dialog.
	 *
	 * @return Either <code>CANCEL</code> or <code>SUCCESSFUL</code>.
	 */
	protected int runWizard() {
		currentPanel = 0;
		infoContainer.setHeader(panels[0].getHeader());
		setVisible(true);
		return retVal;
	}


	/**
	 * Enables or disables the "Next" button in the wizard dialog.  This
	 * method can be used by dialog panels that wish to prevent the user
	 * from advancing to the next step in the wizard without first
	 * completing the current step.
	 *
	 * @param enabled Whether or not the "Next" button should be enabled.
	 */
	public void setNextButtonEnabled(boolean enabled) {
		nextButton.setEnabled(enabled);
	}


	/**
	 * Sets the panels displayed as steps (including introduction and
	 * "successful" message) for this dialog.
	 *
	 * @param plugin The plugin.
	 */
	private void setPanels(WizardPlugin plugin) {

		int stepCount = plugin.getStepCount();
		panels = new WizardDialogInfoPanel[stepCount+2];

		WizardDialogInfoPanel panel;
		panel = plugin.getIntroductionPanel(this);
		infoPanel.add("0", panel);
		panels[0] = panel;

		// getInfoPanel() is 1-based, so we're okay here.
		for (int i=1; i<=stepCount; i++) {
			panel = plugin.getInfoPanel(i, this);
			infoPanel.add(""+i, panel);
			panels[i] = panel;
		}

		panel = plugin.getWizardSuccessfulPanel(this);
		infoPanel.add(""+(panels.length-1), panel);
		panels[panels.length-1] = panel;

	}


	/**
	 * Sets a property value.  This is used by the wizard panels to store
	 * the values of user input.
	 *
	 * @param key The property name.
	 * @param value The property value.
	 * @see #getWizardProperty
	 */
	public void setWizardProperty(String key, Object value) {
		wizardProperties.put(key, value);
	}


	/**
	 * Updates the visible panel to reflect the value of
	 * <code>currentPanel</code>.
	 */
	private void updateVisiblePanel() {

		WizardDialogInfoPanel panel = panels[currentPanel];
		infoPanelLayout.show(infoPanel, ""+currentPanel);
		infoContainer.setHeader(panel.getHeader());
		String title = plugin.getWizardDialogTitle();
		int stepCount = plugin.getStepCount();

		// NOTE:  We cannot use a switch statement here as javac requires
		// constant values for cases (which "stepCount" and "stepCount+1" are
		// not).
		if (currentPanel==0) {
			backButton.setEnabled(false);
		}
		else if (currentPanel==1) {
			// Re-enable back button in case coming from 0.
			backButton.setEnabled(true);
		}

		boolean lastPanel = currentPanel==stepCount+1;
		nextButton.setEnabled(!lastPanel); // isDisplayed() can override.
		finishedButton.setEnabled(lastPanel);

		// Don't do an "else" in case stepCount==1.
		/*else*/ if (currentPanel==stepCount) {
			getRootPane().setDefaultButton(nextButton);
		}
		else if (currentPanel==stepCount+1) {
			getRootPane().setDefaultButton(finishedButton);
			finishedButton.requestFocusInWindow();
		}

		if (currentPanel>0 && currentPanel<=stepCount) {
			String temp = msg.getString("Dialog.InStepTitle");
			title = MessageFormat.format(temp,
					new Object[] { title, ""+currentPanel, ""+stepCount });
		}
		setTitle(title);
		panel.isDisplayed();

	}


	/**
	 * A panel that has a "header" and a bevel border on its bottom.
	 */
	private static class HeaderPanel extends JPanel {

		private JLabel header;

		public HeaderPanel() {
			setLayout(new BorderLayout());
			JPanel headerPanel = new JPanel(new BorderLayout());
			headerPanel.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(0,0,5,0),
						new BevelDividerBorder(SwingConstants.BOTTOM)));
			header = new JLabel("Header");
			// WORKAROUND for Sun JRE bug 6282887 (for JRE's pre-1.5.0-b45).
			//Font font = header.getFont().deriveFont(Font.BOLD, 18);
			StyleContext sc = StyleContext.getDefaultStyleContext();
			Font font = header.getFont();
			font = sc.getFont(font.getFamily(), Font.BOLD, 18);
			header.setFont(font);
			headerPanel.add(header, BorderLayout.WEST);
			add(headerPanel, BorderLayout.NORTH);
		}

		public void setHeader(String headerText) {
			header.setText(headerText);
		}

	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			String actionCommand = e.getActionCommand();

			if (actionCommand.equals("Cancel")) {
				if (plugin.promptBeforeCancel()) {
					int rc = JOptionPane.showConfirmDialog(
							WizardPluginDialog.this,
							msg.getString("MessageBox.ExitPrompt.Text"),
							msg.getString("MessageBox.ExitPrompt.Title"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (rc!=JOptionPane.YES_OPTION)
						return;
				}
				retVal = WizardPlugin.CANCEL;
				setVisible(false);
			}

			else if (actionCommand.equals("Back")) {
				currentPanel--;
				updateVisiblePanel();
			}

			else if (actionCommand.equals("Next")) {
				String errorMessage = panels[currentPanel].validateInput();
				if (errorMessage!=null) {
					JOptionPane.showMessageDialog(WizardPluginDialog.this,
							errorMessage, "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				panels[currentPanel].saveUserInput(WizardPluginDialog.this);
				panels[currentPanel].isAccepted();
				currentPanel++;
				updateVisiblePanel();
			}

			else if (actionCommand.equals("Finished")) {
				panels[currentPanel].isAccepted();
				setVisible(false);
				retVal = WizardPlugin.SUCCESSFUL;
			}

		}

	}


}