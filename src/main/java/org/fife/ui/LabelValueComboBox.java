/*
 * 12/09/2004
 *
 * LabelValueComboBox.java - A combo box that renders label/value pairs.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;


/**
 * An extension of <code>JComboBox</code> that renders label/value pairs.
 * The labels are displayed in the combo, and values are returned.
 *
 * @param <L> The label type.
 * @param <V> The value type.
 * @author Robert Futrell
 * @version 0.1
 */
public class LabelValueComboBox<L, V> extends JComboBox {

	private static final long serialVersionUID = 1L;

	private List<V> values;


	/**
	 * Constructor.
	 */
	public LabelValueComboBox() {
		values = new ArrayList<V>(1);
	}


	/**
	 * Adds an label/value pair to this combo box. You should use this method
	 * to add items to this combo box instead of the standard
	 * <code>addItem</code> method.
	 *
	 * @param label The label of the entry.
	 * @param value The value of the entry.
	 */
	public void addLabelValuePair(L label, V value) {
		values.add(value);
		super.addItem(label);
	}


	/**
	 * Returns the value associated with the currently selected item.
	 *
	 * @return The value.
	 */
	public V getSelectedValue() {
		return getValueAt(getSelectedIndex());
	}


	/**
	 * Returns the value at the specified index.
	 *
	 * @param index The index.
	 * @return The value
	 */
	public V getValueAt(int index) {
		return values.get(index);
	}


	/**
	 * Inserts an item at the specified location in the combo box's list.
	 * This method should be used instead of <code>JComboBox</code>'s
	 * <code>insertItemAt</code> method.
	 *
	 * @param label The label of the entry to insert.
	 * @param index The index at which to add the object.
	 * @param value The value of the entry to insert.
	 */
	public void insertLabelValueAt(L label, int index, V value) {
		values.add(index, value);
		super.insertItemAt(label, index);
	}


	/**
	 * Returns whether the specified string is a registered value.
	 *
	 * @param possibleValue The parameter to check for.
	 * @return Whether it is a known value.
	 */
	public boolean isValue(V possibleValue) {
		return values.indexOf(possibleValue)>-1;
	}


	/**
	 * Sets the selected combo box item by value.
	 *
	 * @param value The value whose item to select.  If this is not
	 *        a registered value, the selected item does not change.
	 * @return The index of the item selected, or <code>-1</code> if there
	 *         is no such value (so the selection did not change).
	 */
	public int setSelectedValue(V value) {
		int index = values.indexOf(value);
		if (index!=-1) {
			setSelectedIndex(index);
		}
		return index;
	}


}