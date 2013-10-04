/*
 * 02/27/2004
 *
 * OptionsDialog.java - Generic dialog for displaying options you can change in
 * an application, similar to those found in many Microsoft Windows apps.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.List;
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

	private JButton okButton;
	private JButton applyButton;
	private JButton cancelButton;

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
			@Override
			public void updateUI() {
				super.updateUI();
				// Must set new cell renderer each time lnf changes as
				// DefaultTreeCellRenderer is "buggy" in that it caches
				// colors, fonts, icons, etc.
				setCellRenderer(createTreeCellRenderer());
			}
		};
		optionTree.setSelectionModel(new RTreeSelectionModel());
		optionTree.setCellRenderer(createTreeCellRenderer());
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
		JPanel temp = new JPanel(new GridLayout(1,3, 5,0));
		okButton = new JButton(msg.getString("OK"));
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton(msg.getString("Cancel"));
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		applyButton = new JButton(msg.getString("Apply"));
		applyButton.setActionCommand("Apply");
		applyButton.addActionListener(this);
		applyButton.setEnabled(false);	// Until they make a change.
		UIUtil.ensureDefaultButtonWidth(okButton);
		UIUtil.ensureDefaultButtonWidth(cancelButton);
		UIUtil.ensureDefaultButtonWidth(applyButton);
		temp.add(okButton);
		temp.add(cancelButton);
		temp.add(applyButton);
		JComponent buttonPanel = (JComponent)UIUtil.createButtonFooter(
				temp, -1, 10);
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
				new BevelDividerBorder(SwingConstants.TOP, 15),
				buttonPanel.getBorder()));

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
	 * Adds an options panel, and any child panels, to this options dialog.
	 *
	 * @param panel The options panel to add.
	 */
	private void addOptionPanel(OptionsDialogPanel panel) {
		panel.addPropertyChangeListener(this);
		currentOptionPanel.add(panel, createKeyForPanel(panel));
		for (int i=0; i<panel.getChildPanelCount(); i++) {
			addOptionPanel(panel.getChildPanel(i));
		}
	}


	/**
	 * Returns the key to use to store/retrieve a panel in the CardLayout
	 * display panel.
	 *
	 * @param panel The options panel.
	 * @return The key.
	 */
	private static final String createKeyForPanel(OptionsDialogPanel panel) {
		String key = panel.getName();
		while ((panel=panel.getParentPanel())!=null) {
			key = panel.getName() + "-" + key;
		}
		return key;
	}


	/**
	 * Creates and returns a renderer to use for the nodes in this tree.
	 *
	 * @return The renderer to use.
	 */
	private static final TreeCellRenderer createTreeCellRenderer() {
		if (SubstanceUtils.isSubstanceInstalled()) {
			//  Use reflection to avoid compile-time dependencies form this
			// class to Substance.
			String clazzName = "org.fife.ui.SubstanceOptionsDialogTreeRenderer";
			try {
				Class<?> clazz = Class.forName(clazzName);
				return (TreeCellRenderer)clazz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				// Fall through
			}
		}
		return new OptionTreeCellRenderer();
	}


	/**
	 * Called when the Escape key is pressed in this dialog.  Subclasses
	 * can override to handle any custom "Cancel" logic.
	 */
	@Override
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
				if (result.component instanceof JTextComponent) {
					((JTextComponent)result.component).selectAll();
				}
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
	 * Inserts a tree node into the navigation tree for an option panel and
	 * its children (if any).
	 *
	 * @param parentNode The parent node to insert into.
	 * @param panel The option panel to add.
	 */
	private void insertOptionPanel(MutableTreeNode parentNode,
			OptionsDialogPanel panel) {
		MutableTreeNode node = new DefaultMutableTreeNode(panel);
		treeModel.insertNodeInto(node, parentNode, parentNode.getChildCount());
		int childCount = panel.getChildPanelCount();
		for (int i = 0; i < childCount; i++) {
			insertOptionPanel(node, panel.getChildPanel(i));
		}
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
	 * @see #setOptionsPanels(OptionsDialogPanel[])
	 */
	public void setOptionsPanels(List<OptionsDialogPanel> optionsPanels) {
		int count = optionsPanels.size();
		OptionsDialogPanel[] panels = new OptionsDialogPanel[count];
		setOptionsPanels(optionsPanels.toArray(panels));
	}


	/**
	 * Sets the options panels available on this options dialog.
	 *
	 * @param optionsPanels The options panels to be available.
	 * @see #setOptionsPanels(List)
	 */
	public void setOptionsPanels(OptionsDialogPanel[] optionsPanels) {

		this.optionsPanels = optionsPanels;
		int numOptionPanels = optionsPanels.length;

		// Populate the navigation tree and the CardLayout main view.
		root.removeAllChildren();
		currentOptionPanel.removeAll();
		for (int i=0; i<numOptionPanels; i++) {
			insertOptionPanel(root, optionsPanels[i]);
			addOptionPanel(optionsPanels[i]);
		}
		UIUtil.expandAllNodes(optionTree);

		// Must be done after currentOptionPanelLayout is created.
		// Must check option panel count because of 1-param constructor.
		if (numOptionPanels>0)
			optionTree.setSelectionRow(0);

		// Must do this so child nodes of (hidden) root are visible.
		optionTree.expandPath(new TreePath(root));

		pack();

	}


	/**
	 * Selects the options panel with the specified name.
	 *
	 * @param name The name of the panel.
	 * @return Whether the panel was selected.  This will only be
	 *         <code>false</code> if <code>name</code> is not the name of an
	 *         options panel added to this dialog.
	 */
	public boolean setSelectedOptionsPanel(String name) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)optionTree.
														getModel().getRoot();
		return setSelectedOptionsPanelImpl(name, root);
	}


	/**
	 * Makes the specified panel visible.
	 *
	 * @param panel The panel to make visible.
	 * @param selectInTree Whether the tree node should be selected as well.
	 *        This is here so this method can be called in response to a tree
	 *        node being clicked, as well as other times (when the tree needs
	 *        to be updated as well as the panel).
	 */
	private void setSelectedOptionsPanel(OptionsDialogPanel panel,
											boolean selectInTree) {

		if (selectInTree) {
			// Select the proper node in the tree. It must be visible for this
			// to work, so we first expand all nodes.
			UIUtil.expandAllNodes(optionTree);
			for (int i=0; i<optionTree.getRowCount(); i++) {
				TreePath path = optionTree.getPathForRow(i);
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
											path.getLastPathComponent();
				if (node!=null) {
					Object obj = node.getUserObject();
					if (obj==panel) {
						optionTree.setSelectionPath(path);
						break;
					}
				}
			}
		}

		currentOptionPanelLayout.show(currentOptionPanel,
										createKeyForPanel(panel));

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


	private boolean setSelectedOptionsPanelImpl(String name,
									DefaultMutableTreeNode node) {

		// Be careful and check for nulls as we support branch nodes not
		// actually containing an option panel.
		Object obj = node.getUserObject();
		if (obj instanceof OptionsDialogPanel) {
			OptionsDialogPanel panel = (OptionsDialogPanel)obj;
			if (name.equals(panel.getName())) {
				setSelectedOptionsPanel(panel, true);
				return true;
			}
		}

		for (Enumeration<?> e=node.children(); e.hasMoreElements(); ) {
			node = (DefaultMutableTreeNode)e.nextElement();
			if (setSelectedOptionsPanelImpl(name, node)) {
				return true;
			}
		}

		return false;

	}


	/**
	 * Sets the visible option panel to the one specified.
	 *
	 * @param panel The panel to display.
	 */
	private void setVisibleOptionPanel(OptionsDialogPanel panel) {

		Enumeration<?> e = root.depthFirstEnumeration();
		while (e.hasMoreElements()) {
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
	@Override
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

		setSelectedOptionsPanel(panel, false);

	}


	/**
	 * Renderer for the tree view.
	 */
	private static class OptionTreeCellRenderer extends DefaultTreeCellRenderer{

		@Override
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