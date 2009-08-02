/*
 * 11/14/2003
 *
 * RButton.java - A JButton that displays a hand cursor when the mouse is over
 * it.
 * Copyright (C) 2003 Robert Futrell
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

import java.awt.Cursor;
import java.io.Serializable;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;


/**
 * An extension of <code>javax.swing.JButton</code> that displays a hand cursor
 * when the mouse is over it.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RButton extends JButton implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * If this property is defined, <code>RButton</code>s will use the hand
	 * cursor.
	 */
	private static final boolean USE_HAND_CURSOR =
							!Boolean.getBoolean("normalButtonCursors");


	/**
	 * Creates a button with no set text or icon.
	 */
	public RButton() {
		init();
	}


	/**
	 * Creates a button where properties are taken from the <code>Action</code>
	 * supplied.
	 *
	 * @param a The <code>Action</code> used to specify the new button.
	 */
	public RButton(Action a) {
		super(a);
		init();
	}


	/**
	 * Creates a button with an icon.
	 *
	 * @param icon The <code>Icon</code> image to display on the button.
	 */
	public RButton(Icon icon) {
		super(icon);
		init();
	}


	/**
	 * Creates a button with text.
	 *
	 * @param text The text of the button.
	 */
	public RButton(String text) {
		super(text);
		init();
	}


	/**
	 * Creates a button with initial text and an icon.
	 *
	 * @param text The text of the button.
	 * @param icon The <code>Icon</code> image to display on the button.
	 */
	public RButton(String text, Icon icon) {
		super(text, icon);
		init();
	}


	/**
	 * Does initialization common to all constructors.
	 */
	private void init() {
		if (USE_HAND_CURSOR) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}


}