/*
 * 02/27/2004
 *
 * OptionsDialog.java - Generic dialog for displaying options you can change in
 * an application, similar to those found in many Microsoft Windows apps.
 * Copyright (C) 2004 Robert Futrell
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
package org.fife.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import javax.swing.tree.*;



/**
 * An options dialog similar to those found in many Microsoft Windows
 * applications.  An <code>OptionsDialog</code> contains a list on its left-hand
 * side containing options categories; clicking on a category displays all
 * options related to that category.<p>
 *
 * Using this class along with {@link OptionsDialogPanel} will provide you
 * with a framework for creating an Options dialog with error checking and
 * instant-Apply functionality.
 *
 * @author Robert Futrell
 * @version 0.8
 * @see org.fife.ui.OptionsDialogPanel
 */
public class OptionsDialog extends EscapableDialog implements ActionListener,
						TreeSelectionListener, PropertyChangeListener {

	/**
	 * The panels the use can display.
	 */
	private OptionsDialogPanel[] optionsPanels;

	private JTree optionTree;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode root;
	private JScrollPane optionTreeScrollPane;

	private RButton okButton;
	private RButton applyButton;
	private RButton cancelButton;

	private JPanel currentOptionPanel;
	private CardLayout currentOptionPanelLayout;
	private TitledPanel titledPanel;


	/**
	 * Creates a new options dialog with no options panels.
	 *
	 * @param owner The parent of this dialog.
	 */
	public OptionsDialog(Frame owner) {
		this(owner, new OptionsDialogPanel[0]);
	}


	/**
	 * Creates a new options dialog with the specified array of option
	 * panels.
	 *
	 * @param owner The parent of this dialog.
	 * @param optionsPanels The option panels to add to this options dialog.
	 *        Note that this array MUST have at least one panel in it;
	 *        otherwise, this constructor will throw a
	 *        <code>NullPointerException</code>.
	 * @see OptionsDialogPanel
	 */
	public OptionsDialog(Frame owner, OptionsDialogPanel[] optionsPanels) {

		super(owner);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle("org.fife.ui.UI");

		// Create the options tree and its scroll pane.
		root = new DefaultMutableTreeNode(msg.getString("Options"));
		treeModel = new DefaultTreeModel(root);
		optionTree = new JTree(treeModel) {
			public void updateUI() {
				super.updateUI();
				// Must set new cell renderer each time lnf changes as
				// DefaultTreeCellRenderer is "buggy" in that it caches
				// colors, fonts, icons, etc.
				DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
				r.setLeafIcon(null);
				r.setOpenIcon(null);
				r.setClosedIcon(null);
				setCellRenderer(r);
			}
		};
		optionTree.setSelectionModel(new RTreeSelectionModel());
		optionTree.setCellRenderer(new OptionTreeCellRenderer());
		optionTree.setShowsRootHandles(true);
		optionTree.setRootVisible(false);
		//optionTree.setSelectionRow(0); // Must call this later.
		optionTree.addTreeSelectionListener(this);
		optionTreeScrollPane = new RScrollPane(optionTree);
		optionTreeScrollPane.setVerticalScrollBarPolicy(
								JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel indexPanel = new JPanel(new GridLayout(1,1));
		indexPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		indexPanel.add(optionTreeScrollPane);

		// Create a panel to hold the current options panel.
		currentOptionPanelLayout = new CardLayout();
		currentOptionPanel = new JPanel(currentOptionPanelLayout);

		setOptionsPanels(optionsPanels);

		// Create a panel with buttons.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new BevelDividerBorder(SwingConstants.TOP, 15));
		JPanel temp = new JPanel(new GridLayout(1,3, 5,0));
		okButton = new RButton(msg.getString("OK"));
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		cancelButton = new RButton(msg.getString("Cancel"));
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		applyButton = new RButton(msg.getString("Apply"));
		applyButton.setActionCommand("Apply");
		applyButton.addActionListener(this);
		applyButton.setEnabled(false);	// Until they make a change.
		temp.add(okButton);
		temp.add(cancelButton);
		temp.add(applyButton);
		buttonPanel.add(temp);

		// Create a panel containing the two above panels.
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		titledPanel = new TitledPanel("Temporary", currentOptionPanel,
								TitledPanel.LINE_BORDER);
		titledPanel.setBorder(UIUtil.getEmpty5Border());
		rightPanel.add(titledPanel);
		rightPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Put everything into a neat little package.
		JPanel contentPane =new ResizableFrameContentPane(new BorderLayout());
		contentPane.setBorder(UIUtil.getEmpty5Border());
		contentPane.add(indexPanel, BorderLayout.LINE_START);
		contentPane.add(rightPanel);
		setContentPane(contentPane);
		getRootPane().setDefaultButton(okButton);
		setTitle(msg.getString("Options"));
		setModal(true);
		applyComponentOrientation(orientation);
		pack();

	}


	/**
	 * Listens for action events in this dialog.
	 */
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if (actionCommand.equals("Cancel")) {
			this.setVisible(false);
		}

		else if (actionCommand.equals("OK")) {
			boolean result = doApply((Frame)getParent());
			if (result==true)
				this.setVisible(false);
		}

		else if (actionCommand.equals("Apply")) {
			doApply((Frame)getParent());
		}

	}


	/**
	 * Adds an options panel to this options dialog.
	 *
	 * @param panel The options panel to add.
	 */
	private void addOptionPanel(OptionsDialogPanel panel) {
		panel.addPropertyChangeListener(this);
		currentOptionPanel.add(panel, panel.getName());
	}


	/**
	 * Called when the Escape key is pressed in this dialog.  Subclasses
	 * can override to handle any custom "Cancel" logic.
	 */
	protected void escapePressed() {
		cancelButton.doClick();
	}


	/**
	 * Automatically called when the user clicks the "Apply" button and when
	 * the user first clicks the "OK" button before the dialog goes away.
	 * This method first ensures all input is valid by calling
	 * <code>ensureValidInputs</code> on all option panels; then it actually
	 * applies the changes to the application.  If all inputs were valid, then
	 * the Apply button is disabled and the "unsaved changes" flag for all
	 * Options panels is cleared.  If there was a problem in any of the
	 * inputs, an error dialog is displayed.
	 *
	 * @param owner The parent frame that was specified in the constructor.
	 * @return <code>true</code> if all options checked out okay and the apply
	 *         was successful; <code>false</code> if at least one option
	 *         wasn't valid (and thus the Options dialog needs to stay up).
	 */
	public final boolean doApply(Frame owner) {

		// Ensure that all of the changes specified are valid.  If one isn't,
		// scold the user, shift focus to it and bail out.
		int numPanels = optionsPanels.length;
		for (int i=0; i<numPanels; i++) {
			OptionsDialogPanel.OptionsPanelCheckResult result =
								optionsPanels[i].ensureValidInputs();
			if (result!=null) {
				setVisibleOptionPanel(result.panel);
				// As an added bonus, if it's a text field, (which it
				// usually (always?) will be), select the text so they
				// can easily delete it.
				if (result.component instanceof JTextComponent)
					((JTextComponent)result.component).selectAll();
				result.component.requestFocusInWindow();
				JOptionPane.showMessageDialog(this, result.errorMessage,
								"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		// Actually apply the changes, disable the "Apply" button, clear
		// the "Unsaved changes" flags of all options panels, and
		// we're done!
		doApplyImpl(owner);
		setApplyButtonEnabled(false);
		for (int i=0; i<numPanels; i++) {
			optionsPanels[i].setUnsavedChanges(false);
		}
		return true;

	}


	/**
	 * Applies all changes specified in the Options dialog to the application.
	 *
	 * @param owner The application.
	 */
	private void doApplyImpl(Frame owner) {
		int panelCount = optionsPanels.length;
		for (int i=0; i<panelCount; i++) {
			OptionsDialogPanel currentPanel = optionsPanels[i];
			currentPanel.doApply(owner);
		}
		owner.repaint();
	}


	/**
	 * Returns the text on the "Apply" button.
	 *
	 * @return The text on the Apply button.
	 * @see #setApplyButtonText
	 */
	public final String getApplyButtonText() {
		return applyButton.getText();
	}


	/**
	 * Returns the text on the "Cancel" button.
	 *
	 * @return The text on the Cancel button.
	 * @see #setCancelButtonText
	 */
	public final String getCancelButtonText() {
		return cancelButton.getText();
	}


	/**
	 * Returns the text on the "OK" button.
	 *
	 * @return The text on the OK button.
	 * @see #setOKButtonText
	 */
	public final String getOKButtonText() {
		return okButton.getText();
	}


	/**
	 * Returns the option panels.
	 *
	 * @return The option panels.
	 */
	protected OptionsDialogPanel[] getOptionsDialogPanels() {
		return optionsPanels;
	}


	/**
	 * Initializes all fields/radio buttons/etc. in this options dialog
	 * with their proper states as obtained from the owner of this options
	 * dialog (as passed into the constructor).  The "Apply" button will be
	 * disabled as a visual cue that nothing has been modified yet.
	 */
	public void initialize() {
		Frame owner = (Frame)getParent();
		int panelCount = optionsPanels.length;
		for (int i=0; i<panelCount; i++) {
			OptionsDialogPanel currentPanel = optionsPanels[i];
			currentPanel.setValues(owner);
		}
		setApplyButtonEnabled(false);
	}


	/**
	 * Listens for a property change in one of the option panels.  This
	 * basically just listens for the user to change a value, so it can
	 * activate the "Apply" button.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		if (!applyButton.isEnabled())
			applyButton.setEnabled(true);
	}


	/**
	 * Enables or disables the "Apply" button.
	 *
	 * @param enabled Whether or not the Apply button should be enabled.
	 */
	public void setApplyButtonEnabled(boolean enabled) {
		applyButton.setEnabled(enabled);
	}


	/**
	 * Sets the text on the "Apply" button.
	 *
	 * @param text The text to use on the Apply button.
	 * @see #getApplyButtonText
	 */
	public void setApplyButtonText(String text) {
		applyButton.setText(text);
	}


	/**
	 * Sets the text on the "Cancel" button.
	 *
	 * @param text The text to use on the Cancel button.
	 * @see #getCancelButtonText
	 */
	public void setCancelButtonText(String text) {
		cancelButton.setText(text);
	}


	/**
	 * Sets the text on the "OK" button.
	 *
	 * @param text The text to use on the OK button.
	 * @see #getOKButtonText
	 */
	public void setOKButtonText(String text) {
		okButton.setText(text);
	}


	/**
	 * Sets the options panels available on this options dialog.
	 *
	 * @param optionsPanels The options panels to be available.
	 */
	public void setOptionsPanels(OptionsDialogPanel[] optionsPanels) {

		this.optionsPanels = optionsPanels;
		int numOptionPanels = optionsPanels.length;

		// Create the tree listing the options panels.
		root.removeAllChildren();
		for (int i=0; i<numOptionPanels; i++) {
			OptionsDialogPanel panel = optionsPanels[i];
			MutableTreeNode node = new DefaultMutableTreeNode(panel);
			treeModel.insertNodeInto(node, root, root.getChildCount());
			int childCount = panel.getChildPanelCount();
			for (int j=0; j<childCount; j++) {
				treeModel.insertNodeInto(new DefaultMutableTreeNode(
											panel.getChildPanel(j)),
									node, j);
			}
		}
		UIUtil.expandAllNodes(optionTree);

		// Remove old option panels and add new ones.
		currentOptionPanel.removeAll();
		for (int i=0; i<numOptionPanels; i++) {
			addOptionPanel(optionsPanels[i]);
			int childCount = optionsPanels[i].getChildPanelCount();
			for (int j=0; j<childCount; j++) {
				addOptionPanel(optionsPanels[i].getChildPanel(j));
			}
		}

		// Must be done after currentOptionPanelLayout is created.
		// Must check option panel count because of 1-param constructor.
		if (numOptionPanels>0)
			optionTree.setSelectionRow(0);

		// Must do this so child nodes of (hidden) root are visible.
		optionTree.expandPath(new TreePath(root));

		pack();

	}


	/**
	 * Sets the visible option panel to the one specified.
	 *
	 * @param panel The panel to display.
	 */
	private void setVisibleOptionPanel(OptionsDialogPanel panel) {

		for (Enumeration e=root.depthFirstEnumeration(); e.hasMoreElements(); ) {
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)e.nextElement();
				OptionsDialogPanel panel2 = (OptionsDialogPanel)dmtn.getUserObject();
				if (panel2==panel) {
					TreePath path = new TreePath(dmtn.getPath());
					optionTree.setSelectionPath(path);
					break;
				}
		}

/*
 * TODO: This is buggy - may be on a "child" panel in the tree.  We need
 * to get the current panel by name and check it that way.
		// Check to see if a text field was edited in the previous
		// panel (as if they switch Options panels before the text field
		// loses focus, the Apply button won't get enabled).
		int currentPanel = optionTree.getRowForPath(
									optionTree.getSelectionPath());
		if (currentPanel<0) {
			currentPanel = 0;
		}
		if (optionsPanels[currentPanel].hasUnsavedChanges())
			setApplyButtonEnabled(true);

		// Ensure a valid panel number.
		if (panel<0)
			panel = 0;
		else if (panel>optionsPanels.length)
			panel = optionsPanels.length-1;

		// Show the next panel.
		if (currentPanel!=panel) {
			optionTree.setSelectionRow(panel);
		}
*/
	}

		
	/**
	 * This method is overridden to ensure that all nodes in the tree are
	 * expanded (as if they're not, the size of the window and its widgets
	 * may be incorrect; not enough room for the tree).
	 *
	 * @param visible Whether or not this dialog should be visible.
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			UIUtil.expandAllNodes(optionTree);
			// Set the preferred size of the scroll pane so that it stays the
			// same size no matter what nodes they click on/hide.  If we
			// don't do this, the JScrollPane resizes when you click on nodes
			// to show the entire displayed node - annoying!
			Dimension size = optionTree.getPreferredScrollableViewportSize();
			size.width += 45; // Just in case a scrollbar is there.
			optionTreeScrollPane.setPreferredSize(size);
			pack();
		}
		// Initially select the first node.
		int[] selectedRows = optionTree.getSelectionRows();
		if (selectedRows==null || selectedRows.length==0) {
			optionTree.setSelectionRow(0); 
		}
		super.setVisible(visible);
	}


	/**
	 * Listens for the user to select values in the tree.
	 */
	public void valueChanged(TreeSelectionEvent e) {

		// Be careful and check for nulls as we support branch nodes not
		// actually containing an option panel.
		TreePath path = optionTree.getSelectionPath();
		if (path==null)
			return;
		Object obj = path.getLastPathComponent();
		if (obj==null)
			return;
		obj = ((DefaultMutableTreeNode)obj).getUserObject();
		if (!(obj instanceof OptionsDialogPanel))
			return;
		OptionsDialogPanel panel = (OptionsDialogPanel)obj;
		currentOptionPanelLayout.show(currentOptionPanel, panel.getName());

		titledPanel.setTitle(panel.getName());
		titledPanel.setIcon(panel.getIcon());

		// Give the "top" component on the panel focus, just to make
		// things a little nicer.
		JComponent topComponent = panel.getTopJComponent();
		if (topComponent!=null) { // Should always be true
			topComponent.requestFocusInWindow();
			if (topComponent instanceof JTextComponent) {
				((JTextComponent)topComponent).selectAll();
			}
		}

	}


	/**
	 * Renderer for the tree view.
	 */
	private static class OptionTreeCellRenderer extends DefaultTreeCellRenderer{

		public Component getTreeCellRendererComponent(JTree tree, Object value,
						boolean sel, boolean expanded, boolean leaf, int row,
						boolean focused) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
												row, focused);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			Object obj = node.getUserObject();
			if (obj instanceof OptionsDialogPanel) {
				OptionsDialogPanel panel = (OptionsDialogPanel)obj;
				setIcon(panel.getIcon());
			}
			else {
				setIcon(null);
			}
			return this;
		}

	}


}