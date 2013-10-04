/*
 * 11/14/2003
 *
 * FontDialog.java - A modal dialog box letting the user choose a font and
 * optionally a font color.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;



/**
 * A dialog box that allows the user to choose from all fonts available to
 * the application on the system, as well as choose a font color.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FontDialog extends JDialog implements ActionListener,
								ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private JList fontList;
	private JList fontSizeList;
	private JLabel sampleTextLabel;
	private Box fontFormatPanel;
	private JPanel samplePanel;
	private JCheckBox boldCheckBox;
	private JCheckBox italicCheckBox;
	private JCheckBox underlineCheckBox;

	private int properties = 0;		// Bit flag for bold/italics.
	private int size = 12;			// Point size of font.

	private Font selectedFont;		// The font they end up selecting (null if canceled).
	private Color selectedColor;		// The color they wish to use for the font.

	private JLabel fontChooserLabel;
	private String styleLabel;
	private JLabel sizeLabel;
	private String sampleLabel;


	/**
	 * Creates a new font dialog from which the user can choose the font
	 * property "underline" and the font color.
	 *
	 * @param owner The window for which you are picking a font.
	 * @param title The title of this dialog (e.g., "Font").
	 * @param initialSelection The font that this dialog initially has
	 *        selected.  A <code>null</code> value defaults to
	 *        <code>owner</code>'s font.
	 * @param initialColor The color to use for the font initially.  A
	 *        <code>null</code> value defaults to black.
	 */
	public FontDialog(Dialog owner, String title, Font initialSelection,
					Color initialColor) {
		this(owner, title, initialSelection, initialColor, true, true);
	}


	/**
	 * Creates a new font dialog.
	 *
	 * @param owner The window for which you are picking a font.
	 * @param title The title of this dialog (e.g., "Font").
	 * @param initialSelection The font that this dialog initially has
	 *        selected.  A <code>null</code> value defaults to
	 *        <code>owner</code>'s font.
	 * @param initialColor The color to use for the font initially.  A
	 *        <code>null</code> value defaults to black.
	 * @param underlineSelectable Whether or not the user can select
	 *        "underline" as a property for the font being chosen.
	 * @param colorSelectable Whether or not the user can change the font's
	 *        color from here.
	 */
	public FontDialog(Dialog owner, String title, Font initialSelection,
					Color initialColor, boolean underlineSelectable,
					boolean colorSelectable) {
		super(owner);
		init(owner, title, initialSelection, initialColor, underlineSelectable,
				colorSelectable);
	}


	/**
	 * Creates a new font dialog from which the user can choose the font
	 * property "underline" and the font color.
	 *
	 * @param owner The window for which you are picking a font.
	 * @param title The title of this dialog (e.g., "Font").
	 * @param initialSelection The font that this dialog initially has
	 *        selected.  A <code>null</code> value defaults to
	 *        <code>owner</code>'s font.
	 * @param initialColor The color to use for the font initially.  A
	 *        <code>null</code> value defaults to black.
	 */
	public FontDialog(Frame owner, String title, Font initialSelection,
					Color initialColor) {
		this(owner, title, initialSelection, initialColor, true, true);
	}


	/**
	 * Creates a new font dialog.
	 *
	 * @param owner The window for which you are picking a font.
	 * @param title The title of this dialog (e.g., "Font").
	 * @param initialSelection The font that this dialog initially has
	 *        selected.  A <code>null</code> value defaults to
	 *        <code>owner</code>'s font.
	 * @param initialColor The color to use for the font initially.  A
	 *        <code>null</code> value defaults to black.
	 * @param underlineSelectable Whether or not the user can select
	 *        "underline" as a property for the font being chosen.
	 * @param colorSelectable Whether or not the user can change the font's
	 *        color from here.
	 */
	public FontDialog(Frame owner, String title, Font initialSelection,
					Color initialColor, boolean underlineSelectable,
					boolean colorSelectable) {
		super(owner);
		init(owner, title, initialSelection, initialColor, underlineSelectable,
				colorSelectable);
	}


	private void init(Window owner, String title, Font initialSelection,
			Color initialColor, boolean underlineSelectable,
			boolean colorSelectable) {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		float alignment = orientation.isLeftToRight() ?
					Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT;

		ResourceBundle msg = ResourceBundle.getBundle("org.fife.ui.UI");

		if (title==null)
			title = msg.getString("FontDialog.Title");
		setTitle(title);

		// Create a panel for choosing the font.
		Box fontChooserPanel = Box.createVerticalBox();
		fontList = new JList(createFontListModel());
		fontList.setSelectionModel(new RListSelectionModel());
		fontList.getSelectionModel().addListSelectionListener(this);
		JScrollPane fontListScrollPane = new RScrollPane(fontList);
		fontChooserLabel = UIUtil.newLabel(msg, "FontLabel");
		fontChooserPanel.add(fontChooserLabel);
		fontChooserPanel.add(fontListScrollPane);
		fontChooserLabel.setAlignmentX(alignment);
		fontListScrollPane.setAlignmentX(alignment);

		// Create a panel for choosing the font's point size.
		Box fontSizePanel = Box.createVerticalBox();
		DefaultListModel listModel = new DefaultListModel();
		for (int i=2; i<=40; i++) {
			// TODO: Use Integer.valueOf(i) in 1.5.
			listModel.addElement(new Integer(i));//Integer.valueOf(i));
		}
		fontSizeList = new JList(listModel);
		fontSizeList.setSelectionModel(new RListSelectionModel());
		fontSizeList.getSelectionModel().addListSelectionListener(this);
		JScrollPane fontSizeListScrollPane = new RScrollPane(fontSizeList);
		sizeLabel = UIUtil.newLabel(msg, "SizeLabel");
		fontSizePanel.add(sizeLabel);
		fontSizePanel.add(fontSizeListScrollPane);
		sizeLabel.setAlignmentX(alignment);
		fontSizeListScrollPane.setAlignmentX(alignment);

		// Create a panel for bold/italic/underline.
		boldCheckBox = createCheckBox(msg, "Bold");
		italicCheckBox = createCheckBox(msg, "Italic");
		underlineCheckBox = createCheckBox(msg, "Underline");
		if (underlineSelectable==false)
			underlineCheckBox.setEnabled(false);
		fontFormatPanel = Box.createVerticalBox();
		fontFormatPanel.add(boldCheckBox);
		fontFormatPanel.add(italicCheckBox);
		fontFormatPanel.add(underlineCheckBox);
		styleLabel = msg.getString("FontStyle");
		setFontStyleLabel(styleLabel);

		// Create a panel for the OK and Cancel buttons.
		JButton okButton = createButton(msg, "OK");
		JButton cancelButton = createButton(msg, "Cancel");
		JButton colorButton = createButton(msg, "Color");
		if (colorSelectable==false)
			colorButton.setVisible(false);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(4,1, 0,5));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(5,5)));
		buttonPanel.add(colorButton);

		// Create a panel containing the previous panels.
		JPanel topPanel = new JPanel();
		topPanel.add(fontChooserPanel);
		topPanel.add(fontFormatPanel);
		topPanel.add(fontSizePanel);
		topPanel.add(buttonPanel);

		topPanel.setBorder(UIUtil.getEmpty5Border());

		// Create a panel that shows a "sample" of the selected font.
		samplePanel = new JPanel();
		sampleLabel = msg.getString("Sample");
		setFontSampleLabel(sampleLabel);
		sampleTextLabel = new JLabel(msg.getString("SampleText"));
		sampleTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sampleTextLabel.setPreferredSize(new Dimension(250,80));
		samplePanel.add(sampleTextLabel);

		// Put it all together.
		if (initialSelection==null)
			initialSelection = owner!=null ? owner.getFont() :
								new Font("Monospaced", Font.PLAIN, 13);
		if (initialSelection.isBold())
			properties |= Font.BOLD;
		if (initialSelection.isItalic())
			properties |= Font.ITALIC;
		//if (initialSelection.isUnderline())
		//	properties |= Font.UNDERLINE;
		setSelectedFont(initialSelection, initialColor);
		getRootPane().setDefaultButton(okButton);
		getContentPane().setLayout(new BoxLayout(getContentPane(),
												BoxLayout.Y_AXIS));
		getContentPane().add(topPanel);
		getContentPane().add(samplePanel);
		setResizable(false);
		setModal(true);
		applyComponentOrientation(orientation);
		pack();



	}


	// What to do when some property of the prospective new font changes.
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("Bold")) {
			properties ^= Font.BOLD;
			Font newFont = sampleTextLabel.getFont().deriveFont(properties, size);
			sampleTextLabel.setFont(newFont);
		}

		else if (command.equals("Italic")) {
			properties ^= Font.ITALIC;
			Font newFont = sampleTextLabel.getFont().deriveFont(properties, size);
			sampleTextLabel.setFont(newFont);
		}

		else if (command.equals("Underline")) {
			//properties ^= Font.CENTER_BASELINE;
			//Font newFont = sampleLabel.getFont().deriveFont(properties, size);
			//sampleTextLabel.setFont(newFont);
			if (getUnderlineSelected()) {
				sampleTextLabel.setText("<html><u>" +
								sampleTextLabel.getText() + "</u>");
			}
			else {
				String old = sampleTextLabel.getText();
				sampleTextLabel.setText(old.substring(9, old.length()-4));
			}
		}

		else if (command.equals("Color")) {
			Color tempColor = JColorChooser.showDialog(this, "Font Color",
									sampleTextLabel.getForeground());
			if (tempColor != null) {
				sampleTextLabel.setForeground(tempColor);
				this.repaint();
			}
		}

		else if (command.equals("OK")) {
			selectedFont = sampleTextLabel.getFont();
			selectedColor = sampleTextLabel.getForeground();
			this.setVisible(false);
		}

		else if (command.equals("Cancel")) {
			selectedFont = null;
			selectedColor = null;
			this.setVisible(false);
		}

	}


	/**
	 * Creates a button to use in this dialog.
	 *
	 * @param msg The resource bundle for localizations.
	 * @param keyRoot The root of the keys in the resource bundle to use
	 *        when localizing.
	 * @return The button.
	 */
	private JButton createButton(ResourceBundle msg, String keyRoot) {
		JButton button = UIUtil.newButton(msg, keyRoot,
												keyRoot+"ButtonMnemonic");
		button.setActionCommand(keyRoot);
		button.addActionListener(this);
		return button;
	}


	/**
	 * Creates a checkbox for use in this dialog.
	 *
	 * @param msg The resouce bundle for localizations.
	 * @param keyRoot The root of the keys in the resource bundle to use
	 *        when localizing.
	 * @return The checkbox.
	 */
	private JCheckBox createCheckBox(ResourceBundle msg, String keyRoot) {
		JCheckBox cb = new JCheckBox(msg.getString(keyRoot));
		cb.setMnemonic((int)msg.getString(keyRoot+"Mnemonic").charAt(0));
		cb.setActionCommand(keyRoot);
		cb.addActionListener(this);
		return cb;
	}


	/**
	 * Creates and returns a list model containing information about
	 * installed system fonts.
	 *
	 * @return The list model.
	 */
	private static final DefaultListModel createFontListModel() {

		// Get available fonts from the system.
		String[] families = GraphicsEnvironment.
				getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

		HashSet<String> monospacedFontsSet = createMonospacedFontsSet();

		// Add them to a list model.
		DefaultListModel listModel = new DefaultListModel();
		for (int i=0; i<families.length; i++) {
			boolean monospaced = monospacedFontsSet.contains(families[i]);
			FontInfo fi = new FontInfo(families[i], monospaced);
			listModel.addElement(fi);
		}

		return listModel;

	}


	/**
	 * Creates a hash set containing monospaced font names.  See
	 * <a href="http://www.codestyle.org/css/font-family/sampler-Monospace.shtml">
	 * http://www.codestyle.org/css/font-family/sampler-Monospace.shtml</a>
	 * for more information.
	 *
	 * @return The set of common monospaced font names on Windows, OS X, and
	 *         UNIX.
	 */
	private static final HashSet<String> createMonospacedFontsSet() {
		HashSet<String> set = new HashSet<String>();
		set.add("Andale Mono");
		set.add("Andale Mono IPA");
		set.add("Bitstream Vera Sans Mono");
		set.add("Consolas");
		set.add("Courier");
		set.add("Courier New");
		set.add("DejaVu Sans Mono");
		set.add("Fixed");
		set.add("FreeMono");
		set.add("Liberation Mono");
		set.add("Lucida Console");
		set.add("Lucida Sans Typewriter");
		set.add("Lucida Typewriter");
		set.add("Luxi Mono");
		set.add("Monaco");
		set.add("Monospaced"); // Logicak font in Java
		set.add("Monotype.com");
		set.add("Nimbus Mono L");
		set.add("OCR A Extended");
		set.add("OCRB");
		set.add("Terminal");
		set.add("Vera Sans Mono");
		return set;
	}


	/**
	 * Returns the label being used for the font list.
	 *
	 * @return The label for the font list.
	 * @see #setFontLabel
	 */
	public final String getFontLabel() {
		return fontChooserLabel.getText();
	}


	/**
	 * Returns the label being used for the font sample area.
	 *
	 * @return The label for the font sample area.
	 * @see #setFontSampleLabel
	 */
	public final String getFontSampleLabel() {
		return sampleLabel;
	}


	/**
	 * Returns the text being used for the font sample.
	 *
	 * @return The text being used for the font sample.
	 * @see #setFontSampleText
	 */
	public final String getFontSampleText() {
		return sampleTextLabel.getText();
	}


	/**
	 * Returns the label being used for the font size list.
	 *
	 * @return The label for the font size list.
	 * @see #setFontSizeLabel
	 */
	public final String getFontSizeLabel() {
		return sizeLabel.getText();
	}


	/**
	 * Returns the label being used for the font style checkbox area.
	 *
	 * @return The label for the font style checkbox area.
	 * @see #setFontStyleLabel
	 */
	public final String getFontStyleLabel() {
		return styleLabel;
	}


	/**
	 * Gets the color last chosen to use for fonts.
	 *
	 * @return The color to use for the current font, or
	 *         <code>null</code> if the dialog was canceled.
	 */
	public Color getSelectedColor() {
		return selectedColor;
	}


	/**
	 * Gets the font last selected to use.
	 *
	 * @return The font to use for the current font, or <code>null</code>
	 *         if the dialog was canceled.
	 * @see #getUnderlineSelected
	 */
	public Font getSelectedFont() {
		return selectedFont;
	}


	/**
	 * Returns whether or not the user checked the "Underline" check box.
	 * Note that if this dialog was created such that "Underline" is not
	 * selectable, this method will always return <code>false</code>.
	 *
	 * @return Whether or not "underline" was selected.
	 * @see #setUnderlineSelected
	 */
	public boolean getUnderlineSelected() {
		return underlineCheckBox.isSelected();
	}


	/**
	 * Sets the label being used for the font list.
	 *
	 * @param text The label for the font list.
	 * @see #getFontLabel
	 */
	public void setFontLabel(String text) {
		fontChooserLabel.setText(text);
	}


	/**
	 * Sets the label being used for the font sample area.
	 *
	 * @param text The label for the font sample area.
	 * @see #getFontSampleLabel
	 */
	public void setFontSampleLabel(String text) {
		sampleLabel = text;
		samplePanel.setBorder(BorderFactory.createCompoundBorder(
			UIUtil.getEmpty5Border(),
			BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(sampleLabel),
				BorderFactory.createCompoundBorder(
					UIUtil.getEmpty5Border(),
					BorderFactory.createBevelBorder(BevelBorder.LOWERED)))));
	}


	/**
	 * Sets the text being used for the font sample.
	 *
	 * @param text The text to use for the font sample.
	 * @see #getFontSampleText
	 */
	public void setFontSampleText(String text) {
		sampleTextLabel.setText(text);
	}


	/**
	 * Sets the label to use for the font size list.
	 *
	 * @param text The label to usefor the font size list.
	 * @see #getFontSizeLabel
	 */
	public void setFontSizeLabel(String text) {
		sizeLabel.setText(text);
	}


	/**
	 * Sets the label to use for the font style checkbox area.
	 *
	 * @param text The label for the font style checkbox area.
	 * @see #getFontStyleLabel
	 */
	public void setFontStyleLabel(String text) {
		styleLabel = text;
		fontFormatPanel.setBorder(
			BorderFactory.createCompoundBorder(
							BorderFactory.createTitledBorder(styleLabel),
							UIUtil.getEmpty5Border()));
	}


	/**
	 * Sets the currently selected font.
	 *
	 * @param toSelect The new currently-selected font.
	 * @param color The color to use as the font color.
	 */
	public void setSelectedFont(Font toSelect, Color color) {

		// Null is not a valid font.
		if (toSelect==null)
			return;

		sampleTextLabel.setForeground(color!=null ? color : Color.BLACK);
		//fontList.setSelectedValue(toSelect.getFamily(), true);
		setSelectedFontInFontList(toSelect.getFamily());
		fontList.ensureIndexIsVisible(fontList.getSelectedIndex());
		boldCheckBox.setSelected(toSelect.isBold());
		italicCheckBox.setSelected(toSelect.isItalic());
		// Underlining is handled separately.
		// TODO: Use Integer.valueOf() in 1.5.
		fontSizeList.setSelectedValue(new Integer(toSelect.getSize()), true);

	}


	/**
	 * Selects the specified font in the font list.  This is here because
	 * {@link javax.swing.JList#setSelectedValue(Object, boolean)} uses
	 * <code>fontFamily.equals()</code> to search for the item, but we need
	 * <code>item.equals(fontFamily)</code>, since we cheat and have the
	 * "items" (FontInfos) compare for equality correctly against Strings.
	 * 
	 * @param fontFamily The font family to select.
	 */
	private void setSelectedFontInFontList(String fontFamily) {
		if (fontFamily==null) {
			fontList.setSelectedIndex(-1);
		}
		else {
			// TODO: Use a binary search since items are ordered.
			ListModel model = fontList.getModel();
			for (int i=0; i<model.getSize(); i++) {
				if (model.getElementAt(i).equals(fontFamily)) {
					fontList.setSelectedIndex(i);
					fontList.ensureIndexIsVisible(i);
					break;
				}
			}
		}
		// setSelectedIndex doesn't always repaint when necessary
        fontList.repaint();
	}


	/**
	 * Sets or clears the "Underline" check box.
	 * Note that if this dialog was created such that "Underline" is not
	 * selectable, this method does nothing.
	 *
	 * @param underline Whether or not the Underline check box should be
	 *        selected.
	 * @see #getUnderlineSelected
	 */
	public void setUnderlineSelected(boolean underline) {
		if (underlineCheckBox.isEnabled())
			underlineCheckBox.setSelected(underline);
	}


	// Listen for when they change font or font size.
	public void valueChanged(ListSelectionEvent e) {

		ListSelectionModel lsm = (ListSelectionModel)e.getSource();

		// If they've chosen a new font, update the sample.
		if (lsm == fontList.getSelectionModel()) {
			FontInfo fi = (FontInfo)fontList.getSelectedValue();
			Font newFont = new Font(fi.fontFamily, properties, size);
			sampleTextLabel.setFont(newFont);
		}

		// If they've chosen a new font size, also update the sample.
		else if (lsm == fontSizeList.getSelectionModel()) {
			size = ((Integer)fontSizeList.getSelectedValue()).intValue();
			Font newFont = sampleTextLabel.getFont().deriveFont(properties, size);
			sampleTextLabel.setFont(newFont);
		}

	}


	/**
	 * Basic information about a font.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private static class FontInfo implements Comparable<Object> {

		public String fontFamily;
		public boolean monospaced;

		public FontInfo(String fontFamily, boolean monospaced) {
			this.fontFamily = fontFamily;
			this.monospaced = monospaced;
		}

		public int compareTo(Object obj) {
			int value = -1;
			// We cheat here as we know we'll be comparing Strings (font
			// family names) FontInfos.
			if (obj instanceof String) {
				value = fontFamily.compareTo((String)obj);
			}
			else if (obj instanceof FontInfo) {
				value = fontFamily.compareTo(((FontInfo)obj).fontFamily);
			}
			return value;
		}

		@Override
		public boolean equals(Object obj) {
			return compareTo(obj)==0;
		}

		@Override
		public int hashCode() {
			return fontFamily.hashCode() + (monospaced ? 1 : 0);
		}

		@Override
		public String toString() {
			String value = fontFamily;
			if (monospaced) {
				value = "<html><b>" + value + "</b>";
			}
			return value;
		}

	}


}