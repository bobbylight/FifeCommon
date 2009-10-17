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
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.TextUI;


/**
 * A "label" component that can have its text selected.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SelectableLabel extends JEditorPane {


	/**
	 * Constructor.
	 */
	public SelectableLabel() {
		this(null);
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
	}


}