/*
 * 07/29/2005
 *
 * ModifiableTableChangeEvent.java - The event fired when a ModifiableTable
 * changes.
 * Copyright (C) 2005 Robert Futrell
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
package org.fife.ui.modifiabletable;

import java.util.EventObject;


/**
 * An event representing the addition, removal or modification of a row in a
 * <code>ModifiableTable</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ModifiableTableChangeEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private static final int MIN_CHANGE	= 0;
	public static final int ADDED			= 0;
	public static final int MODIFIED		= 1;
	public static final int REMOVED		= 2;
	private static final int MAX_CHANGE	= 2;

	private int change;
	private int row;


	/**
	 * Constructor.
	 *
	 * @param source The table that changed.
	 * @param change One of <code>ADDED</code>, <code>MODIFIED</code> or
	 *        <code>REMOVED</code>.
	 * @param row The row that was added, modified or removed.
	 */
	public ModifiableTableChangeEvent(ModifiableTable source, int change,
								int row) {
		super(source);
		if (change<MIN_CHANGE || change>MAX_CHANGE)
			throw new IllegalArgumentException("Invalid change: " + change);
		this.change = change;
		this.row = row;
	}


	/**
	 * Returns the change that occurred.
	 *
	 * @return One of <code>ADDED</code>, <code>MODIFIED</code> or
	 *         <code>REMOVED</code>.
	 */
	public int getChange() {
		return change;
	}


	/**
	 * Returns the modifiable table that has changed.
	 *
	 * @return The modifiable table.
	 */
	public ModifiableTable getModifiableTable() {
		return (ModifiableTable)getSource();
	}


	/**
	 * Returns the row that was added, removed or modified.
	 *
	 * @return The row number.
	 */
	public int getRow() {
		return row;
	}


}