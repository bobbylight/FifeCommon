/*
 * 10/21/2004
 *
 * RTextFileChooserOptionPanel.java - Option panel for configuring an
 * RTextFileChooser.
 * Copyright (C) 2004 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.LabelValueComboBox;
import org.fife.ui.UIUtil;
import org.fife.ui.modifiabletable.*;
import org.fife.util.SubstanceUtil;


/**
 * Option panel for configuring an <code>RTextFileChooser</code>.  Your
 * subclass of {@link org.fife.ui.OptionsDialog} should call this class's
 * {@link #initialize} method in the dialog's <code>initialize</code> method,
 * and this class's {@link #configureFileChooser} method in the dialog's
 * <code>doApplyImpl</code> method.
 *
 * @author Robert Futrell
 * @version 0.3
 */
public class RTextFileChooserOptionPanel extends OptionsDialogPanel
					implements ActionListener, ModifiableTableListener {

	@Serial
	private static final long serialVersionUID = 1L;

	private JCheckBox hiddenFilesCheckBox;
	private JCheckBox offerSuggestionsCB;
	private JCheckBox autoCompleteCheckBox;
	private RColorSwatchesButton hiddenColorButton;
	private ColorTableModel colorTableModel;
	private static String defaultColorString;
	private LabelValueComboBox<String, String> openFilesStyleCombo;
	private JCheckBox styleOpenFilesCheckBox;

	private static final String MAPPING_DIALOG_BUNDLE	=
				"org.fife.ui.rtextfilechooser.ExtensionColorMappingDialog";


	/**
	 * Constructor.  All strings in the file chooser are initialized via the
	 * current locale.
	 */
	public RTextFileChooserOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle(
							"org.fife.ui.rtextfilechooser.FileChooser");

		defaultColorString = msg.getString("DefaultColorLabel");

		setName(msg.getString("FileChooser"));
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		add(createGeneralPanel(msg), BorderLayout.NORTH);

		JPanel customColorsPanel = new JPanel(new BorderLayout());
		customColorsPanel.setBorder(new OptionPanelBorder(
									msg.getString("Colors")));
		colorTableModel = new ColorTableModel(msg.getString("Extension"),
										msg.getString("Color"));
		ModifiableTable modifiableTable = new ModifiableTable(colorTableModel);
		modifiableTable.setRowHandler(new FileChooserRowHandler());
		modifiableTable.addModifiableTableListener(this);
		JTable colorTable = modifiableTable.getTable();
		colorTable.setPreferredScrollableViewportSize(new Dimension(300,200));
		colorTable.getColumnModel().getColumn(1).setCellRenderer(
				createTextColorCellRenderer());
		customColorsPanel.add(modifiableTable);
		add(customColorsPanel);

		applyComponentOrientation(orientation);

	}


	/**
	 * Listens for actions on this panel.
	 *
	 * @param e The action that occurred.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if ("HiddenFiles".equals(actionCommand)) {
			boolean checked = hiddenFilesCheckBox.isSelected();
			hiddenColorButton.setEnabled(checked);
			setDirty(true);
		}

		else if ("OfferSuggestions".equals(actionCommand)) {
			boolean checked = offerSuggestionsCB.isSelected();
			autoCompleteCheckBox.setEnabled(checked);
			setDirty(true);
		}

		else if ("AutoComplete".equals(actionCommand)) {
			setDirty(true);
		}

		else if ("StyleOpenFiles".equals(actionCommand)) {
			boolean checked = styleOpenFilesCheckBox.isSelected();
			openFilesStyleCombo.setEnabled(checked);
			setDirty(true);
		}

		else if ("OpenFilesStyle".equals(actionCommand)) {
			setDirty(true);
		}

	}


	/**
	 * Configures the passed-in file chooser to have its parameters set like
	 * those specified in this option panel.
	 *
	 * @param chooser The file chooser to configure.
	 * @see #initialize
	 */
	public void configureFileChooser(RTextFileChooser chooser) {

		chooser.setShowHiddenFiles(hiddenFilesCheckBox.isSelected());
		chooser.setHiddenFileColor(hiddenColorButton.getColor());
		chooser.setFileSystemAware(offerSuggestionsCB.isSelected());
		chooser.setAutoCompleteFileNames(autoCompleteCheckBox.isSelected());
		chooser.clearExtensionColorMap(); // In case they removed any.

		chooser.setStyleOpenFiles(styleOpenFilesCheckBox.isSelected());
		String style = openFilesStyleCombo.getSelectedValue();
		chooser.setOpenFilesStyle(Integer.parseInt(style));

		// Do all the other colors.
		int rowCount = colorTableModel.getRowCount();
		for (int i=1; i<rowCount; i++) {
			String ext = (String)colorTableModel.getValueAt(i,0);
			Color color = (Color)colorTableModel.getValueAt(i,1);
			chooser.setColorForExtension(ext, color);
		}

	}


	private JCheckBox createCheckBox(ResourceBundle msg, String root) {
		JCheckBox cb = new JCheckBox(msg.getString(root + "Label"));
		cb.setMnemonic((int)msg.getString(root + "Mnemonic").charAt(0));
		cb.setActionCommand(root);
		cb.addActionListener(this);
		return cb;
	}


	private Container createGeneralPanel(ResourceBundle msg) {

		Box generalPanel = Box.createVerticalBox();
		generalPanel.setBorder(new OptionPanelBorder(msg.getString("General")));

		hiddenFilesCheckBox = createCheckBox(msg, "HiddenFiles");
		Container temp = createHorizontalBox();
		hiddenColorButton = new RColorSwatchesButton();
		temp.add(hiddenFilesCheckBox);
		temp.add(hiddenColorButton);
		temp.add(Box.createHorizontalGlue());
		generalPanel.add(temp);
		generalPanel.add(Box.createVerticalStrut(COMPONENT_VERTICAL_SPACING));

		addLeftAligned(generalPanel, createStyleOpenFilesPanel(msg), COMPONENT_VERTICAL_SPACING);

		offerSuggestionsCB = createCheckBox(msg, "OfferSuggestions");
		addLeftAligned(generalPanel, offerSuggestionsCB, COMPONENT_VERTICAL_SPACING);
		autoCompleteCheckBox = createCheckBox(msg, "AutoComplete");
		addLeftAligned(generalPanel, autoCompleteCheckBox, SECTION_VERTICAL_SPACING, 20);

		return generalPanel;
	}


	private Box createStyleOpenFilesPanel(ResourceBundle msg) {

		Box openFilesPanel = createHorizontalBox();

		openFilesStyleCombo = new LabelValueComboBox<>();
		UIUtil.fixComboOrientation(openFilesStyleCombo);

		/*
		openFilesStyleCombo.addSpecialItem(MSG.getString("Bold"),
								""+RTextFileChooser.STYLE_BOLD);
		openFilesStyleCombo.addSpecialItem(MSG.getString("Italic"),
								""+RTextFileChooser.STYLE_ITALIC);
		*/
		openFilesStyleCombo.addLabelValuePair(msg.getString("Underline"),
								""+RTextFileChooser.STYLE_UNDERLINE);
		openFilesStyleCombo.setActionCommand("OpenFilesStyle");
		openFilesStyleCombo.addActionListener(this);

		String text = msg.getString("StyleOpenFilesLabel");
		int pos = text.indexOf("{0}");
		if (pos==-1) { // Never happens.
			pos = text.length();
		}
		String beg = text.substring(0, pos);
		String end = text.substring(pos+3);
		styleOpenFilesCheckBox = new JCheckBox(beg);
		styleOpenFilesCheckBox.setMnemonic((int)msg.
						getString("StyleOpenFilesMnemonic").charAt(0));
		styleOpenFilesCheckBox.setActionCommand("StyleOpenFiles");
		styleOpenFilesCheckBox.addActionListener(this);
		openFilesPanel.add(styleOpenFilesCheckBox);
		openFilesPanel.add(openFilesStyleCombo);
		if (!end.isEmpty()) {
			openFilesPanel.add(new JLabel(end));
		}
		openFilesPanel.add(Box.createHorizontalGlue());

		return openFilesPanel;

	}


	/**
	 * Creates and returns a renderer to use for the "example" file name
	 * rendering column.
	 *
	 * @return The cell renderer.
	 */
	private static TableCellRenderer createTextColorCellRenderer() {
		TableCellRenderer renderer;
		if (SubstanceUtil.isSubstanceInstalled()) {
			renderer = new SubstanceTextColorCellRenderer();
		}
		else {
			renderer = new TextColorCellRenderer();
		}
		return renderer;
	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.  This application should implement
	 *        {@link FileChooserOwner}.
	 * @throws IllegalArgumentException If <code>owner</code> is not a
	 *         {@link FileChooserOwner}.
	 */
	@Override
	protected void doApplyImpl(Frame owner) {
		if (!(owner instanceof FileChooserOwner fco)) {
			throw new IllegalArgumentException(
								"owner must be a FileChooserOwner");
		}
		configureFileChooser(fco.getFileChooser());
	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// They can't input invalid stuff on this options panel.
		return null;
	}


	@Override
	public JComponent getTopJComponent() {
		return hiddenFilesCheckBox;
	}


	/**
	 * Initializes the values displayed in this option panel.
	 *
	 * @param chooser The file chooser whose properties you want to use to
	 *        initialize this options panel.
	 * @see #configureFileChooser
	 */
	public void initialize(RTextFileChooser chooser) {
		boolean showHiddenFiles = chooser.getShowHiddenFiles();
		hiddenFilesCheckBox.setSelected(showHiddenFiles);
		hiddenColorButton.setColor(chooser.getHiddenFileColor());
		hiddenColorButton.setEnabled(showHiddenFiles);
		styleOpenFilesCheckBox.setSelected(chooser.getStyleOpenFiles());
		openFilesStyleCombo.setSelectedValue(""+chooser.getOpenFilesStyle());
		openFilesStyleCombo.setEnabled(chooser.getStyleOpenFiles());
		setOfferSuggestionsSelected(chooser.getFileSystemAware());
		autoCompleteCheckBox.setSelected(chooser.getAutoCompleteFileNames());
		colorTableModel.initCustomColorTable(chooser);
	}


	/**
	 * Called whenever the extension/color mapping table is changed.
	 *
	 * @param e An event describing the change.
	 */
	@Override
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		setDirty(true);
	}


	/**
	 * Toggles whether the "Offer filename suggestions" checkbox is selected.
	 *
	 * @param selected Whether it should be selected.
	 */
	private void setOfferSuggestionsSelected(boolean selected) {
		offerSuggestionsCB.setSelected(selected);
		autoCompleteCheckBox.setEnabled(selected);
	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.  This should implement
	 *        {@link FileChooserOwner}.
	 * @throws IllegalArgumentException If <code>owner</code> is not a
	 *         {@link FileChooserOwner}.
	 * @see #setValues(Frame)
	 */
	@Override
	protected void setValuesImpl(Frame owner) {
		if (!(owner instanceof FileChooserOwner fco)) {
			throw new IllegalArgumentException(
									"owner must be a FileChooserOwner");
		}
		initialize(fco.getFileChooser());
	}


	/**
	 * The table model used by the custom colors table.
	 */
	private static class ColorTableModel extends DefaultTableModel {

		@Serial
		private static final long serialVersionUID = 1L;

		ColorTableModel(String extensionHeader, String colorHeader) {
			super(new Object[] { extensionHeader, colorHeader }, 0);
		}

		void initCustomColorTable(RTextFileChooser chooser) {
			setRowCount(0);
			Vector<Object> v = new Vector<>(2);
			v.add(defaultColorString);
			v.add(chooser.getDefaultFileColor());
			addRow(v);
			Map<String, Color> map = chooser.getCustomColorsMap();
			Set<String> keySet = map.keySet();
			if (keySet!=null) {
				for (String extension : keySet) {
					// DefaultTableModel uses Vectors internally.
					v = new Vector<>(2);
					v.add(extension);
					v.add(map.get(extension));
					addRow(v);
				}
			}
		}

	}


	/**
	 * The dialog that allows the user to add or modify an extension/color
	 * mapping.
	 */
	static class ExtensionColorMappingDialog extends JDialog
			implements ActionListener, ChangeListener, DocumentListener {

		static final int OK		= 0;
		static final int CANCEL	= 1;

		private JTextField extensionField;
		private JColorChooser colorChooser;
		private JButton okButton;
		private int rc;

		ExtensionColorMappingDialog(JDialog owner) {

			super(owner);
			ComponentOrientation orientation = ComponentOrientation.
										getOrientation(getLocale());
			ResourceBundle msg = ResourceBundle.
									getBundle(MAPPING_DIALOG_BUNDLE);
			JPanel contentPane = new ResizableFrameContentPane(
											new BorderLayout());
			contentPane.setBorder(UIUtil.getEmpty5Border());

			// Panel containing main stuff.
			Box topPanel = Box.createVerticalBox();
			JPanel temp = new JPanel(new BorderLayout());
			temp.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
			JLabel label = UIUtil.newLabel(msg, "Extension");
			JPanel temp2 = new JPanel(new BorderLayout());
			temp2.add(label);
			if (orientation.isLeftToRight()) { // Space between label and text field.
				temp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
			}
			else {
				temp2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
			}
			temp.add(temp2, BorderLayout.LINE_START);
			extensionField = new JTextField();
			extensionField.getDocument().addDocumentListener(this);
			label.setLabelFor(extensionField);
			temp.add(extensionField);
			topPanel.add(temp);
			colorChooser = new JColorChooser();
			colorChooser.getSelectionModel().addChangeListener(this);
			topPanel.add(colorChooser);
			contentPane.add(topPanel, BorderLayout.NORTH);

			// Panel containing buttons for the bottom.
			okButton = UIUtil.newButton(msg, "OK.Text", "OK.Mnemonic");
			okButton.addActionListener(this);
			JButton cancelButton = UIUtil.newButton(msg,
				"Cancel.Text", "Cancel.Mnemonic");
			cancelButton.addActionListener(this);
			Container buttons=UIUtil.createButtonFooter(okButton, cancelButton);
			contentPane.add(buttons, BorderLayout.SOUTH);

			// Get ready to go.
			setTitle(msg.getString("Title"));
			setContentPane(contentPane);
			getRootPane().setDefaultButton(okButton);
			setModal(true);
			applyComponentOrientation(orientation);
			pack();

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source==okButton)
				rc = OK;
			setVisible(false);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}

		public Color getColor() {
			return colorChooser.getColor();
		}

		public String getExtension() {
			return extensionField.getText();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			okButton.setEnabled(true);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			okButton.setEnabled(extensionField.getDocument().getLength()>0);
		}

		public void setData(String extension, Color color) {
			extensionField.setText(extension);
			colorChooser.setColor(color);
		}

		@Override
		public void setVisible(boolean visible) {
			if (visible) {
				String extension = extensionField.getText();
				extensionField.setEnabled(
							!extension.equals(defaultColorString));
			}
			super.setVisible(visible);
		}

		public int showMappingDialog() {
			rc = CANCEL; // Set here in case they "X" the dialog out.
			SwingUtilities.invokeLater(() -> {
				extensionField.requestFocusInWindow();
				extensionField.selectAll();
			});
			setLocationRelativeTo(getOwner());
			okButton.setEnabled(false);
			setVisible(true);
			return rc;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			// Ensure the document has text in it.
			okButton.setEnabled(extensionField.getDocument().getLength()>0);
		}

	}


	/**
	 * Handles the addition, removal, and modifying of rows in the color
	 * table.
	 */
	class FileChooserRowHandler extends AbstractRowHandler {

		private ExtensionColorMappingDialog dialog;

		@Override
		public boolean canModifyRow(int row) {
			// Row 0 is always "<Default>," which the user can't modify/remove.
			return row>0;
		}

		@Override
		public Object[] getNewRowInfo(Object[] oldData) {
			if (dialog==null)
				dialog = new ExtensionColorMappingDialog(getOptionsDialog());
			if (oldData==null)
				dialog.setData(null, null);
			else
				dialog.setData((String)oldData[0], (Color)oldData[1]);
			int rc = dialog.showMappingDialog();
			if (rc==ExtensionColorMappingDialog.OK) {
				return new Object[] { dialog.getExtension(),dialog.getColor() };
			}
			return null;
		}

		/**
		 * Not an override.  Implements <code>RowHandler#updateUI()</code>.
		 */
		@Override
		public void updateUI() {
			if (dialog!=null) {
				SwingUtilities.updateComponentTreeUI(dialog);
			}
		}

	}


}
