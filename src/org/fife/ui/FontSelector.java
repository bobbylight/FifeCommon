/*
 * 05/28/2006
 *
 * FontSelector.java - A panel allowing the user to select a font
 * (for use in things such as options dialogs).
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;


/**
 * A panel allowing the user to select a font, for use in things
 * such as options dialogs.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FontSelector extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	public static final int LABELED     = 0;
	public static final int NOT_LABELED = 1;
	public static final int CHECK_BOX   = 2;

	private JComponent labelComp;
	private FontTextField field;
	private boolean underline;
	private JButton browseButton;
	private boolean underlineSelectable;
	private boolean colorSelectable;
	private Color fontColor;

	public static final String ENABLED_PROPERTY		= "enabled";
	public static final String FONT_PROPERTY		= "font";
	public static final String FONT_COLOR_PROPERTY	= "fontColor";

	private static final String MSG = "org.fife.ui.FontSelector";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Constructor.
	 */
	public FontSelector() {
		this(LABELED);
	}


	/**
	 * Constructor.
	 *
	 * @param type One of {@link #LABELED}, {@link #NOT_LABELED}, or
	 *        {@link #CHECK_BOX}.
	 */
	public FontSelector(int type) {

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setAlignmentX(Component.LEFT_ALIGNMENT);
		field = new FontTextField();
		switch (type) {
			default:
			case LABELED:
				JLabel label = new JLabel(msg.getString("FontTitle"));
				label.setLabelFor(field);
				labelComp = label;
				break;
			case NOT_LABELED:
				break;
			case CHECK_BOX:
				JCheckBox cb = new JCheckBox(msg.getString("FontTitle"), true);
				cb.addActionListener(this);
				labelComp = cb;
				break;
		}

		browseButton = new JButton(msg.getString("Browse"));
		browseButton.addActionListener(this);

		if (labelComp!=null) {
			add(labelComp);
			add(Box.createHorizontalStrut(5));
		}
		add(field);
		add(Box.createHorizontalStrut(5));
		add(browseButton);
		add(Box.createHorizontalGlue());

	}


	/**
	 * Listens for actions in this panel.
	 */
	public void actionPerformed(ActionEvent e) {

		// If the user clicked the "Browse" button for fonts...
		if (e.getSource()==browseButton) {

			Window parent = SwingUtilities.getWindowAncestor(this);
			FontDialog fd = null;
			if (parent instanceof Frame) {
				fd = new FontDialog((Frame)parent, msg.getString("Font"),
									field.getDisplayedFont(),
									fontColor,
									underlineSelectable,
									colorSelectable);
			}
			else {
				fd = new FontDialog((Dialog)parent, msg.getString("Font"),
						field.getDisplayedFont(),
						fontColor,
						underlineSelectable,
						colorSelectable);
			}
			fd.setUnderlineSelected(underline);
			fd.setLocationRelativeTo(parent);
			fd.setVisible(true);
			Font f = fd.getSelectedFont();
			if (f!=null) {
				//Font old = fontField.getDisplayedFont();
				underline = fd.getUnderlineSelected();
				field.setDisplayedFont(f, underline);
				// HACK: Use null instead of old so we force the property
				// to be fired.  This is because if old==f (e.g. they only
				// changed the "underline" property), then
				// firePropertyChange quietly does not fire the property.
				firePropertyChange(FONT_PROPERTY, null/*old*/, f);
			}
			fontColor = fd.getSelectedColor(); // Will be null if canceled.
			if (fontColor!=null) { // Happens when f!=null also.
				firePropertyChange(FONT_COLOR_PROPERTY, null, fontColor);
			}

		}

		else if (e.getSource()==labelComp) {
			JCheckBox cb = (JCheckBox)labelComp;
			boolean selected = cb.isSelected();
			field.setEnabled(selected);
			browseButton.setEnabled(selected);
			firePropertyChange(ENABLED_PROPERTY, !selected, selected);
		}

	}


	/**
	 * Returns the font being displayed.
	 *
	 * @return The displayed font.
	 * @see #setDisplayedFont(Font, boolean)
	 */
	public Font getDisplayedFont() {
		return field.getDisplayedFont();
	}


	/**
	 * Returns the font color selected by the user.  This value
	 * will be <code>null</code> if <code>isColorSelectable()</code>
	 * returns <code>false</code>.
	 *
	 * @return The font color selected by the user.
	 * @see #setFontColor(Color)
	 */
	public Color getFontColor() {
		return isColorSelectable() ? fontColor : null;
	}


	/**
	 * Returns whether the current font is underlined.  Note that
	 * if <code>isUnderlineSelectable()</code> returns <code>false</code>,
	 * this method will always return <code>false</code> also.
	 *
	 * @return Whether the current font is underlined.
	 * @see #getDisplayedFont()
	 */
	public boolean getUnderline() {
		return underline;
	}


	/**
	 * Sets whether the font color is selectable in the
	 * font dialog used by this font selector.
	 *
	 * @return Whether the color option is available.
	 * @see #setColorSelectable(boolean)
	 */
	public boolean isColorSelectable() {
		return colorSelectable;
	}


	/**
	 * Returns whether this font selector is "toggled on."  This
	 * method is unfortunately named because <code>isEnabled()</code>
	 * is already a method defined by <code>Component</code>.<p>
	 *
	 * If this font selector is not togglable, this method always
	 * returns <code>true</code>.
	 *
	 * @return Whether this font selector is toggled on.
	 * @see #setToggledOn(boolean)
	 */
	public boolean isToggledOn() {
		return labelComp==null || (labelComp instanceof JLabel) ||
			((JCheckBox)labelComp).isSelected();
	}


	/**
	 * Sets whether "underline" is selectable in the font
	 * dialog used by this font selector.
	 *
	 * @return Whether "underline" is available.
	 * @see #setUnderlineSelectable(boolean)
	 */
	public boolean isUnderlineSelectable() {
		return underlineSelectable;
	}


	/**
	 * Toggles whether font color will be selectable in the font
	 * dialog used by this font selector.
	 *
	 * @param selectable Whether color will be available.
	 * @see #isColorSelectable()
	 */
	public void setColorSelectable(boolean selectable) {
		colorSelectable = selectable;
	}


	/**
	 * Sets the font displayed.
	 *
	 * @param font The font to display.
	 * @param underline Whether the font should be underlined.
	 *        Note that if <code>isUnderlineSelectable()</code>
	 *        returns <code>false</code>, this parameter will do
	 *        nothing.
	 * this method will always return <code>false</code> also.
	 * @see #getDisplayedFont()
	 * @see #getUnderline()
	 */
	public void setDisplayedFont(Font font, boolean underline) {
		field.setDisplayedFont(font, underline);
		this.underline = underline;
	}


	/**
	 * Sets the font color displayed.  This method will do
	 * nothing if <code>isColorSelectable()</code> returns
	 * <code>false</code>.
	 *
	 * @param color The font color to display.
	 * @see #getFontColor()
	 */
	public void setFontColor(Color color) {
		if (isColorSelectable()) {
			fontColor = color;
		}
	}


	/**
	 * Sets whether this font selector is "toggled on."  This method is
	 * unfortunately named since <code>setEnabled</code> is already a
	 * method defined by <code>Component</code>.<p>
	 *
	 * If this font selector is not togglable, this method does nothing.
	 *
	 * @param toggled Whether this font selector should be enabled.
	 * @see #isToggledOn()
	 */
	public void setToggledOn(boolean toggled) {
		if (labelComp instanceof JCheckBox) {
			JCheckBox cb = (JCheckBox)labelComp;
			cb.setSelected(toggled);
			field.setEnabled(toggled);
			browseButton.setEnabled(toggled);
		}
	}


	/**
	 * Toggles whether "underline" will be selectable in the font
	 * dialog used by this font selector.
	 *
	 * @param selectable Whether "underline" will be available.
	 * @see #isUnderlineSelectable()
	 */
	public void setUnderlineSelectable(boolean selectable) {
		underlineSelectable = selectable;
	}


	/**
	 * A text field that displays a font name and size, but always in a
	 * fixed, reasonable size.  You can get the font whose description
	 * is shown by calling <code>getDisplayedFont</code>.
	 */
	private static class FontTextField extends JTextField {

		private static final long serialVersionUID = 1L;

		private Font displayedFont;

		public FontTextField() {
			setEditable(false);
		}

		public Font getDisplayedFont() {
			return displayedFont;
		}

		public void setDisplayedFont(Font font, boolean underline) {
			this.displayedFont = font;
			// The font we display needs to be smaller; the font will be
			// shown in its actual size in the preview panel.
			Font ourFont = font.deriveFont(getFont().getSize2D());
			setFont(ourFont);
			setText(ourFont.getFamily() + " " + displayedFont.getSize() +
								(font.isBold() ? " Bold" : "") +
								(font.isItalic() ? " Italic" : "") +
								(underline ? " Underline" : ""));
		}

	}


}