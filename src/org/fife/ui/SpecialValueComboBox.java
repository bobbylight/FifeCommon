/*
 * 12/09/2004
 *
 * SpecialValueComboBox.java - A combo box with special strings associated
 * with each of its elements.
 * Copyright (C) 2004 Robert Futrell
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

import java.util.Vector;
import javax.swing.JComboBox;


/**
 * An extension of <code>JComboBox</code> that remembers a special string
 * for each of its contained items.  This is useful for when you need a combo
 * box whose choices correspond to strings, but you don't want those strings
 * to be the values shown in the combo box.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class SpecialValueComboBox extends JComboBox {

	private static final long serialVersionUID = 1L;

	private Vector values;


	/**
	 * Constructor.
	 */
	public SpecialValueComboBox() {
		values = new Vector(1);
	}


	/**
	 * Adds an object/"special value" (parameter) pair to this combo box.
	 * You should use this method to add items to this combo box instead
	 * of the standard <code>addItem</code> method.
	 *
	 * @param anObject The object to add to/display in the combo box.
	 * @param value The "special value" (parameter) to associate with the
	 *        object.
	 */
	public void addSpecialItem(Object anObject, String value) {
		values.add(value);
		super.addItem(anObject);
	}


	/**
	 * Returns the "special value" (parameter) associated with the currently
	 * selected item.
	 *
	 * @return The special item.
	 */
	public String getSelectedSpecialItem() {
		return getSpecialItemAt(getSelectedIndex());
	}


	/**
	 * Returns the "special value" (parameter) at the specified index.
	 *
	 * @param index The index.
	 * @return The special item.
	 */
	public String getSpecialItemAt(int index) {
		return (String)values.get(index);
	}


	/**
	 * Inserts an item at the specified location in the combo box's list.
	 * This method should be used instead of <code>JComboBox</code>'s
	 * <code>insertItemAt</code> method.
	 *
	 * @param anObject The object to add to/display in the combo box.
	 * @param index The index at which to add the object.
	 * @param value The "special value" (parameter) to associate with the
	 *        object.
	 */
	public void insertSpecialItemAt(Object anObject, int index,
							String value) {
		values.add(index, value);
		super.insertItemAt(anObject, index);
	}


	/**
	 * Returns whether the specified string is a registered "special value"
	 * (parameter).
	 *
	 * @param possibleValue The parameter to check for.
	 * @return Whether it is a "special value" (parameter).
	 */
	public boolean isSpecialItem(String possibleValue) {
		return values.indexOf(possibleValue)>-1;
	}


	/**
	 * Sets the selected combo box item by "special value" (parameter).
	 *
	 * @param value The parameter whose item to select.  If this is not
	 *        a registered special item, the selected item does not change.
	 * @return The index of the item selected, or <code>-1</code> if there
	 *         is no such special value (so the selection did not change).
	 */
	public int setSelectedSpecialItem(String value) {
		int index = values.indexOf(value);
		if (index!=-1) {
			setSelectedIndex(index);
		}
		return index;
	}


}