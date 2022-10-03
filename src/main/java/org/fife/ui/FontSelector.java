/*
 * 05/28/2006
 *
 * FontSelector.java - A panel allowing the user to select a font
 * (for use in things such as options dialogs).
 * Copyright (C) 2006 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
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

	@Serial
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
	private boolean toggledOn;

	public static final String TOGGLED_ON_PROPERTY = "toggledOn";
	public static final String FONT_PROPERTY		= "font";
	public static final String FONT_COLOR_PROPERTY	= "fontColor";

	private static final ResourceBundle MSG = ResourceBundle.getBundle(
			"org.fife.ui.FontSelector");


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
				JLabel label = new JLabel(MSG.getString("FontTitle"));
				label.setLabelFor(field);
				labelComp = label;
				break;
			case NOT_LABELED:
				break;
			case CHECK_BOX:
				JCheckBox cb = new JCheckBox(MSG.getString("FontTitle"), true);
				cb.addActionListener(this);
				labelComp = cb;
				break;
		}

		browseButton = new JButton(MSG.getString("Browse"));
		browseButton.addActionListener(this);

		if (labelComp!=null) {
			add(labelComp);
			add(Box.createHorizontalStrut(5));
		}
		add(field);
		add(Box.createHorizontalStrut(5));
		add(browseButton);
		add(Box.createHorizontalGlue());
		toggledOn = true;

	}


	/**
	 * Listens for actions in this panel.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		// If the user clicked the "Browse" button for fonts...
		if (e.getSource()==browseButton) {

			Window parent = SwingUtilities.getWindowAncestor(this);
			FontDialog fd;
			if (parent instanceof Frame) {
				fd = new FontDialog((Frame)parent, MSG.getString("Font"),
									field.getDisplayedFont(),
									fontColor,
									underlineSelectable,
									colorSelectable);
			}
			else {
				fd = new FontDialog((Dialog)parent, MSG.getString("Font"),
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
			setToggledOn(selected);
			firePropertyChange(TOGGLED_ON_PROPERTY, !selected, selected);
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
	 * Returns whether this font selector is "toggled on;" that is,
	 * assuming the component is enabled, whether the user can select
	 * a font. This method will return {@code true} if it is a
	 * checkbox that is selected, or if it is just a label or unlabelled.
	 * It will return {@code false} if it is a checkbox and the
	 * checkbox is not selected.<p>
	 *
	 * This property is completely independent of the component's
	 * enabled state. Thus, it can be toggled on (checked) but not
	 * enabled.
	 *
	 * @return Whether this font selector is toggled on.
	 * @see #setToggledOn(boolean)
	 */
	public boolean isToggledOn() {
		return toggledOn;
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
	 * @see #getDisplayedFont()
	 * @see #getUnderline()
	 */
	public void setDisplayedFont(Font font, boolean underline) {
		field.setDisplayedFont(font, underline);
		this.underline = underline;
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (labelComp != null) {
			labelComp.setEnabled(enabled);
		}
		field.setEnabled(enabled && toggledOn);
		browseButton.setEnabled(enabled && toggledOn);
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
	 * Sets whether this font selector is "toggled on."  See the
	 * description of {@link #isToggledOn()} for a description of
	 * this property.
	 * <p>
	 * If this font selector is not toggleable (i.e. is not a
	 * checkbox), this method does nothing.
	 *
	 * @param toggled Whether this font selector should be toggled on.
	 * @see #isToggledOn()
	 */
	public void setToggledOn(boolean toggled) {
		if (labelComp instanceof JCheckBox) {
			JCheckBox cb = (JCheckBox)labelComp;
			cb.setSelected(toggled);
			field.setEnabled(isEnabled() && toggled);
			browseButton.setEnabled(isEnabled() && toggled);
			this.toggledOn = toggled;
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

		@Serial
		private static final long serialVersionUID = 1L;

		private Font displayedFont;

		FontTextField() {
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
