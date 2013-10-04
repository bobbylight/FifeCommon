/*
 * 10/21/2004
 *
 * RTextFileChooserOptionPanel.java - Option panel for configuring an
 * RTextFileChooser.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
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
import org.fife.ui.SpecialValueComboBox;
import org.fife.ui.SubstanceUtils;
import org.fife.ui.UIUtil;
import org.fife.ui.modifiabletable.*;


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

	private static final long serialVersionUID = 1L;

	public static final String AUTO_COMPLETE_PROPERTY		= "AutoComplete";
	public static final String COLOR_CHANGED_PROPERTY		= "ColorChanged";
	public static final String HIDDEN_FILES_PROPERTY		= "HiddenFiles";
	public static final String OFFER_SUGGESTIONS_PROPERTY	= "OfferSuggestions";
	public static final String OPEN_FILES_STYLE_PROPERTY	= "OpenFilesStyle";
	public static final String STYLE_OPEN_FILES_PROPERTY	= "StyleOpenFiles";

	private JCheckBox hiddenFilesCheckBox;
	private JCheckBox offerSuggestionsCB;
	private JCheckBox autoCompleteCheckBox;
	private RColorSwatchesButton hiddenColorButton;
	private ColorTableModel colorTableModel;
	private static String defaultColorString;
	private SpecialValueComboBox openFilesStyleCombo;
	private JCheckBox styleOpenFilesCheckBox;

	private static final String SUBSTANCE_RENDERER_CLASS =
			"org.fife.ui.rtextfilechooser.SubstanceTextColorCellRenderer";

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

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(msg.getString("General")));
		hiddenFilesCheckBox = createCheckBox(msg, "HiddenFiles");
		Container temp2 = createHorizontalBox();
		hiddenColorButton = new RColorSwatchesButton();
		temp2.add(hiddenFilesCheckBox);
		temp2.add(hiddenColorButton);
		temp2.add(Box.createHorizontalGlue());
		temp.add(temp2);
		temp2 = createStyleOpenFilesPanel(msg);
		temp.add(temp2);
		offerSuggestionsCB = createCheckBox(msg, "OfferSuggestions");
		addLeftAligned(temp, offerSuggestionsCB);
		autoCompleteCheckBox = createCheckBox(msg, "AutoComplete");
		addLeftAligned(temp, autoCompleteCheckBox, 0, 20);
		add(temp, BorderLayout.NORTH);

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
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if ("HiddenFiles".equals(actionCommand)) {
			boolean checked = hiddenFilesCheckBox.isSelected();
			hiddenColorButton.setEnabled(checked);
			firePropertyChange(HIDDEN_FILES_PROPERTY, !checked, checked);
		}

		else if ("OfferSuggestions".equals(actionCommand)) {
			boolean checked = offerSuggestionsCB.isSelected();
			autoCompleteCheckBox.setEnabled(checked);
			firePropertyChange(OFFER_SUGGESTIONS_PROPERTY, !checked,checked);
		}

		else if ("AutoComplete".equals(actionCommand)) {
			boolean checked = autoCompleteCheckBox.isSelected();
			firePropertyChange(AUTO_COMPLETE_PROPERTY, !checked, checked);
		}

		else if ("StyleOpenFiles".equals(actionCommand)) {
			boolean checked = styleOpenFilesCheckBox.isSelected();
			openFilesStyleCombo.setEnabled(checked);
			firePropertyChange(STYLE_OPEN_FILES_PROPERTY, !checked, checked);
		}

		else if ("OpenFilesStyle".equals(actionCommand)) {
			String style = openFilesStyleCombo.getSelectedSpecialItem();
			firePropertyChange(OPEN_FILES_STYLE_PROPERTY, null, style);
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
		String style = openFilesStyleCombo.getSelectedSpecialItem();
		chooser.setOpenFilesStyle(Integer.parseInt(style));

		// Do all of the other colors.
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


	private Box createStyleOpenFilesPanel(ResourceBundle msg) {

		Box panel = createHorizontalBox();

		openFilesStyleCombo = new SpecialValueComboBox();
		UIUtil.fixComboOrientation(openFilesStyleCombo);

		/*
		openFilesStyleCombo.addSpecialItem(msg.getString("Bold"),
								""+RTextFileChooser.STYLE_BOLD);
		openFilesStyleCombo.addSpecialItem(msg.getString("Italic"),
								""+RTextFileChooser.STYLE_ITALIC);
		*/
		openFilesStyleCombo.addSpecialItem(msg.getString("Underline"),
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
		panel.add(styleOpenFilesCheckBox);
		panel.add(openFilesStyleCombo);
		if (end.length()>0) {
			panel.add(new JLabel(end));
		}
		panel.add(Box.createHorizontalGlue());

		return panel;

	}


	/**
	 * Creates and returns a renderer to use for the "example" file name
	 * rendering column.
	 *
	 * @return The cell renderer.
	 */
	private static final TableCellRenderer createTextColorCellRenderer() {
		TableCellRenderer renderer = null;
		if (SubstanceUtils.isSubstanceInstalled()) {
			// Use reflection to avoid hard dependency on Substance.
			try {
				Class<?> clazz = Class.forName(SUBSTANCE_RENDERER_CLASS);
				renderer = (TableCellRenderer)clazz.newInstance();
			} catch (Exception e) { // Never happens
				e.printStackTrace();
			}
		}
		if (renderer==null) {
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
		if (!(owner instanceof FileChooserOwner)) {
			throw new IllegalArgumentException(
								"owner must be a FileChooserOwner");
		}
		FileChooserOwner fco = (FileChooserOwner)owner;
		configureFileChooser(fco.getFileChooser());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// They can't input invalid stuff on this options panel.
		return null;
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 *
	 * @return The top <code>JComponent</code>.
	 */
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
		openFilesStyleCombo.setSelectedSpecialItem(""+chooser.getOpenFilesStyle());
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
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(COLOR_CHANGED_PROPERTY, null,
						new Integer(e.getRow()));
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
		if (!(owner instanceof FileChooserOwner)) {
			throw new IllegalArgumentException(
									"owner must be a FileChooserOwner");
		}
		FileChooserOwner fco = (FileChooserOwner)owner;
		initialize(fco.getFileChooser());
	}


	/**
	 * The table model used by the custom colors table.
	 */
	private static class ColorTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public ColorTableModel(String extensionHeader, String colorHeader) {
			super(new Object[] { extensionHeader, colorHeader }, 0);
		}

		public void initCustomColorTable(RTextFileChooser chooser) {
			setRowCount(0);
			Vector<Object> v = new Vector<Object>(2);
			v.add(defaultColorString);
			v.add(chooser.getDefaultFileColor());
			addRow(v);
			Map<String, Color> map = chooser.getCustomColorsMap();
			Set<String> keySet = map.keySet();
			if (keySet!=null) {
				for (String extension : keySet) {
					// DefaultTableModel uses Vectors internally.
					v = new Vector<Object>(2);
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
		private JButton cancelButton;
		private int rc;

		public ExtensionColorMappingDialog(JDialog owner) {

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
			cancelButton = UIUtil.newButton(msg,
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

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source==okButton)
				rc = OK;
			setVisible(false);
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public Color getColor() {
			return colorChooser.getColor();
		}

		public String getExtension() {
			return extensionField.getText();
		}

		public void insertUpdate(DocumentEvent e) {
			okButton.setEnabled(true);
		}

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
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					extensionField.requestFocusInWindow();
					extensionField.selectAll();
				}
			});
			setLocationRelativeTo(getOwner());
			okButton.setEnabled(false);
			setVisible(true);
			return rc;
		}

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