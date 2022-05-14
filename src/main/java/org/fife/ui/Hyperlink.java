/*
 * 08/02/2009
 *
 * Hyperlink.java - A clickable hyperlink.
 * Copyright (C) 2009 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serial;
import javax.swing.*;
import javax.swing.plaf.LabelUI;
import javax.swing.plaf.basic.BasicLabelUI;


/**
 * A clickable hyperlink.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Hyperlink extends JLabel {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * If this property is set in the UI Defaults to a color, it is used for the
	 * foreground instead of the default value.
	 */
	public static final String UI_PROPERTY_FOREGROUND = "org.fife.ui.Hyperlink.foreground";

	private String text;
	private String address;


	/**
	 * Constructor.
	 *
	 * @param address The address the link points to.
	 */
	public Hyperlink(String address) {
		this(address, address);
	}


	/**
	 * Constructor.
	 *
	 * @param text The text to display as the hyperlink.
	 * @param address The address the link points to.
	 */
	public Hyperlink(String text, String address) {
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		setAddress(address);
		setText(text);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setFocusable(true);
	}


	/**
	 * Returns the web address this hyperlink points to.
	 *
	 * @return The web address.
	 * @see #setAddress(String)
	 */
	public String getAddress() {
		return address;
	}


	/**
	 * Returns the text displayed by this hyperlink.
	 *
	 * @return The text displayed.
	 * @see #setText(String)
	 */
	public String getDisplayedText() {
		return this.text;
	}


	@Override
	protected void processMouseEvent(MouseEvent e) {
		if (e.getButton()==MouseEvent.BUTTON1) {
			switch (e.getID()) {
				case MouseEvent.MOUSE_CLICKED:
					if (!UIUtil.browse(getAddress())) {
						UIManager.getLookAndFeel().
									provideErrorFeedback(this);
					}
					break;
			}
		}
		super.processMouseEvent(e);
	}


	/**
	 * Sets the address this hyperlink points to.
	 *
	 * @param address The new web address.
	 * @see #getAddress()
	 */
	public void setAddress(String address) {
		this.address = address;
	}


	/**
	 * Sets the text for this hyperlink.
	 *
	 * @param text The new text.
	 * @see #getText()
	 */
	@Override
	public void setText(String text) {
		super.setText(text);
		setToolTipText(address);
	}


	@Override
	public void setUI(LabelUI ui) {
		super.setUI(new HyperlinkUI());
	}


	class HyperlinkUI extends BasicLabelUI {

		public void paint(Graphics g, JComponent c) {

			JLabel label = (JLabel)c;
			String text = label.getText();

			Color fg;
			if (c.isEnabled()) {
				fg = UIManager.getColor(UI_PROPERTY_FOREGROUND);
				if (fg == null) {
					fg = c.getForeground();
				}
			}
			else {
				fg = UIManager.getColor("Label.disabledForeground");
			}
			g.setColor(fg);

			FontMetrics fm = getFontMetrics(g.getFont());
			int textY = fm.getAscent();
			g.drawString(text, 0, textY);
			int width = fm.stringWidth(text);
			g.drawLine(0, textY + 1, width, textY + 1);
		}
	}
}
