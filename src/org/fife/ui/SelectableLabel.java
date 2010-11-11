/*
 * 10/15/2009
 *
 * SelectableLabel.java - A label that can have its text selected.
 * Copyright (C) 2009 Robert Futrell
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

import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.TextUI;
import javax.swing.text.html.HTMLDocument;


/**
 * A "label" component that can have its text selected.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SelectableLabel extends JTextPane {


	/**
	 * Constructor.
	 */
	public SelectableLabel() {
		this(null);
	}


	/**
	 * Returns a hex string for the specified color, suitable for HTML.
	 *
	 * @param c The color.
	 * @return The string representation, in the form "<code>#rrggbb</code>",
	 *         or <code>null</code> if <code>c</code> is <code>null</code>.
	 */
	private static final String getHexString(Color c) {

		if (c==null) {
			return null;
		}

		StringBuffer sb = new StringBuffer("#");
		int r = c.getRed();
		if (r<16) {
			sb.append('0');
		}
		sb.append(Integer.toHexString(r));
		int g = c.getGreen();
		if (g<16) {
			sb.append('0');
		}
		sb.append(Integer.toHexString(g));
		int b = c.getBlue();
		if (b<16) {
			sb.append('0');
		}
		sb.append(Integer.toHexString(b));

		return sb.toString();

	}


	/**
	 * Constructor.
	 *
	 * @param text The text to display.  This can be HTML.
	 */
	public SelectableLabel(String text) {
		setText(text);
		labelize();
	}


	/**
	 * Makes this editor pane look like a label.
	 */
	private void labelize() {

		setBorder(null);
		setEditable(false);
		setOpaque(false);
		setBackground(new Color(0, 0, 0, 0)); // Needed for Nimbus

		// In case text component fonts are different from label fonts.
		Font font = UIManager.getFont("Label.font");
		if (font==null) { // Not required to be defined
			font = new JLabel().getFont();
		}
		setFont(font);

		// In case text component fg is different from label fg.
		Color fg = UIManager.getColor("Label.foreground");
		if (fg!=null) {
			//System.out.println("Foreground" + fg);
			setForeground(fg);
		}

		// Some LaFs have different selection colors for editable vs.
		// non-editable text components; we want to use the editable color.
		Color sel = UIManager.getColor("TextField.selectionBackground");
		if (sel!=null) {
			//System.out.println("Selection background: " + sel);
			setSelectionColor(sel);
		}

	}


	/**
	 * Sets the text displayed by this label.
	 *
	 * @param text The text to display.  If this begins with
	 *        "<code>&lt;html&gt;</code>", the text will be rendered as
	 *        HTML.
	 */
	public void setText(String text) {
		if (text!=null && text.startsWith("<html>")) {
			setContentType("text/html");
			updateDefaultHtmlFont();
		}
		else {
			setContentType("text/plain");
		}
		super.setText(text);
	}


	/**
	 * Overridden to keep our special properties, to keep us looking like
	 * a label across <code>LookAndFeels</code>.
	 */
	public void setUI(TextUI ui) {
		super.setUI(ui);
		labelize();
		if (getDocument() instanceof HTMLDocument) {
			updateDefaultHtmlFont();
		}
	}


	/**
	 * Add a CSS rule to force body tags to use the default label font
	 * instead of the value in javax.swing.text.html.default.css.  This was
	 * adapted from:
	 * http://explodingpixels.wordpress.com/2008/10/28/make-jeditorpane-use-the-system-font/
	 */
	private void updateDefaultHtmlFont() {

		Font font = UIManager.getFont("Label.font");
		// This property is defined by all standard LaFs, even Nimbus (!), but
		// you never know what crazy LaFs there are...
		if (font==null) {
			font = new JLabel().getFont();
		}

		// When rendering HTML, the JEditorPane foreground color is ignored;
		// we have to set it in the CSS as well.  This is only needed for
		// LAF's whose label color isn't (extremely close to) black.
		Color fg = getForeground();
		String bodyRule = "body { font-family: " + font.getFamily() +
						"; font-size: " + font.getSize() + "pt" +
						"; color: " + getHexString(fg) + "; }";
		((HTMLDocument)getDocument()).getStyleSheet().addRule(bodyRule);

	}


}