/*
 * 01/15/2004
 *
 * RComboBoxModel.java - A combo box model that limits the number of items
 * the combo box wil remember.  It also won't add an item to the combo box
 * if it is already there.
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

import java.io.Serializable;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;


/**
 * A combo box model that limits the number of items the combo box will
 * "remember."  You can use it like so:
 * <br>
 * <code>RComboBoxModel model = new RComboBoxModel();</code><br>
 * <code>model.setMaxNumElements(10);</code><br>
 * <code>JComboBox comboBox = new JComboBox(model);</code><br><br>
 * It also won't let you add an item to the combo box twice (i.e., no
 * duplicates), and it adds new items to the beginning of the list, not
 * the end (as <code>JComboBox</code>'s do by default).<br><br>
 * It defaults to 10 elements remembered.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class RComboBoxModel extends DefaultComboBoxModel
										implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The number of items the combo box will remember.
	 */
	private int maxNumElements;


	/**
	 * Creates a new combo box model with a maximum element count of
	 * <code>8</code>.
	 */
	public RComboBoxModel() {
		setMaxNumElements(8);
	}


	/**
	 * Creates a new combo box model with a maximum element count of
	 * <code>8</code>.
	 *
	 * @param items The initial items to use to populate the combo box.
	 */
	public RComboBoxModel(Object[] items) {
		super(items);
		setMaxNumElements(8);
	}


	/**
	 * Creates a new combo box model with a maximum element count of
	 * <code>8</code>.
	 *
	 * @param v The initial items to use to populate the combo box.
	 */
	public RComboBoxModel(Vector v) {
		super(v);
		setMaxNumElements(8);
	}


	/**
	 * Adds the object (if it's not already in the list) to the front of
	 * the list.  If it's already in the list, move it to the top.
	 *
	 * @param anObject The object to add.
	 */
	public void addElement(Object anObject) {
		insertElementAt(anObject, 0);
	}


	/**
	 * Ensures the number if items remembered by this combo box is valid.
	 */
	private void ensureValidItemCount() {
		while (getSize()>maxNumElements)
			removeElementAt(getSize()-1);
	}


	/**
	 * Returns the maximum number of items this combo box can hold.
	 *
	 * @return The maximum number of items this combo box can hold.
	 */
	public int getMaxNumElements() {
		return maxNumElements;
	}


	/**
	 * Adds an item at a specified index.  The implementation of this method
	 * should notify all registered <code>ListDataListeners</code> that the
	 * item has been added.
	 *
	 * @param anObject The <code>Object</code> to be added.
	 * @param index Location to add the object.
	 */
	public void insertElementAt(Object anObject, int index) {

		int oldPos = getIndexOf(anObject);
		if (oldPos==index) { // Already at the desired location.
			return;
		}
		if (oldPos>-1) {	// Remove it first if it's somewhere else.
			removeElement(anObject);
		}

		super.insertElementAt(anObject, index);
		ensureValidItemCount();

	}


	/**
	 * Sets the maximum number of items this combo box can hold.
	 *
	 * @param numElements The maximum number of items this combo box can hold.
	 *        If <code>numElements <= 0</code>, then the capacity
	 *        of this combo box is set to <code>4</code>.
	 */
	public void setMaxNumElements(int numElements) {
		maxNumElements = numElements<=0 ? 4 : numElements;
		ensureValidItemCount();
	}


}