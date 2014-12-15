/*
 * 02/08/2010
 *
 * PickyDocumentFilter.java - A document filter that only allows certain
 * characters to be entered.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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


	@Override
	public void insertString(DocumentFilter.FilterBypass fb, int offset,
				String text, AttributeSet attr) throws BadLocationException {
		fb.insertString(offset, cleanse(text), attr);
	}


	@Override
	public void remove(DocumentFilter.FilterBypass fb,
				int offset, int length)
					throws BadLocationException {
		fb.remove(offset, length);
	}


	@Override
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
			String text, AttributeSet attr) throws BadLocationException {
		fb.replace(offset, length, cleanse(text), attr);
	}


}