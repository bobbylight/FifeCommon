/*
 * 11/09/2008
 *
 * FileIOExtras.java - Adds x64-specific functionality to the file chooser.
 * Copyright (C) 2008 Robert Futrell
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
package org.fife.ui.rtextfilechooser.extras;


/**
 * Extra file IO stuff specific to x64.
 *
 * @author Robert Futrell
 * @version 1.1
 */
class x64FileIOExtras extends Win32FileIOExtras {


	/**
	 * Loads the native library used by this class.
	 *
	 * @throws UnsatisfiedLinkError If the required native dll is not found.
	 */
	protected void loadNativeLibrary() {
		System.loadLibrary("x64FileIOExtras");
	}


	/**
	 * Moves the specified file to the recycle bin.
	 *
	 * @param hwnd The handle to the parent window, or <code>0</code> if
	 *        unknown.
	 * @param fileName The name of the file to move.  This should
	 *        be an absolute path.
	 * @param confirmation Whether a "Are you sure you want to send XXX to
	 *        the Trash?" should be displayed to the user.
	 * @param silent If this is <code>true</code>, no progress dialog is
	 *        displayed to the user.  If this is <code>false</code>, then
	 *        a progress bar is displayed as the file(s) are being deleted.
	 *        On Windows, this parameter appears to only be honored if
	 *        <code>confirmation</code> is <code>true</code>.
	 * @return Whether the operation was successful.  Note that this method
	 *         returns <code>true</code> even if the user manually aborts
	 *         the delete operation in the UI.  This method only fails if
	 *         a serious internal error occurs (out of memory, etc.).
	 */
	protected native boolean moveToRecycleBinImpl(long hwnd, String[] files,
						boolean confirmation, boolean silent);


}