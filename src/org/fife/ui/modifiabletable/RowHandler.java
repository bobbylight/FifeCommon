/*
 * 07/28/2005
 *
 * RowHandler.java - Handles the adding, removing, and modifying of rows in a
 * ModifiableTable.
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


/**
 * A <code>RowHandler</code> is called whenever the user wants to add, modify,
 * or remove a row from a <code>ModifiableTable</code>.  It is responsible for
 * getting new data from the user, and has the power to veto a removal.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface RowHandler {


	/**
	 * Called when the user chooses to add or modify a row in the table.
	 *
	 * @param oldData The old values of the cells in this row.  If the user
	 *        is adding a new row, this value is <code>null</code>.
	 * @return The new values for the cells in this row.  If the user
	 *         cancels the add/modify operation, this method should return
	 *         <code>null</code>.
	 */
	public Object[] getNewRowInfo(Object[] oldData);


	/**
	 * Called when the user chooses to remove a row in the table.  This method
	 * gives the program a chance to veto the removal, prompt the user to
	 * validate the removal, etc.
	 *
	 * @param row The row that might be removed.
	 * @return Whether the row should be removed.
	 */
	public boolean shouldRemoveRow(int row);


	/**
	 * If this row handler has any Swing components/windows in it, this method
	 * will update their UI's.  This method is called in response to an
	 * LaF change.
	 */
	public void updateUI();


}