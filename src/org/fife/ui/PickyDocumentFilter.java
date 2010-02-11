/*
 * 02/08/2010
 *
 * PickyDocumentFilter.java - A document filter that only allows certain
 * characters to be entered.
 * Copyright (C) 2010 Robert Futrell
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

import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;


/**
 * A document filter that only allows certain characters to be entered.
 * Subclasses can simply override {@link #cleanseImpl(String)}, and either
 * remove invalid chars from the returned string, or return
 * <code>null</code> if there are any invalid characters, depending on how
 * they want the behavior of their text component.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class PickyDocumentFilter extends DocumentFilter {


	/**
	 * "Cleanses" the text to insert.  This can remove unsupported characters,
	 * or cause the insert to do nothing by returning <code>null</code>.
	 *
	 * @param text The text to possibly insert.
	 * @return The "cleansed" text to insert.
	 * @see #cleanse(String)
	 */
	protected abstract String cleanseImpl(String text);


	/**
	 * "Cleans" the input text specified by passing it to
	 * {@link #cleanseImpl(String)}.  If it's "cleansed" in any way, a beep
	 * is played.

	 * @param text The text to possibly insert.
	 * @return The "cleansed" text to insert.
	 * @see #cleanseImpl(String)
	 */
	protected String cleanse(String text) {
		boolean beep = false;
		if (text!=null) {
			String text2 = cleanseImpl(text);
			if (!text.equals(text2)) {
				text = text2;
				beep = true;
			}
		}
		if (beep) {
			UIManager.getLookAndFeel().provideErrorFeedback(null);
		}
		return text;
	}


	public void insertString(DocumentFilter.FilterBypass fb, int offset,
				String text, AttributeSet attr) throws BadLocationException {
		fb.insertString(offset, cleanse(text), attr);
	}


	public void remove(DocumentFilter.FilterBypass fb,
				int offset, int length)
					throws BadLocationException {
		fb.remove(offset, length);
	}


	public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
			String text, AttributeSet attr) throws BadLocationException {
		fb.replace(offset, length, cleanse(text), attr);
	}


}