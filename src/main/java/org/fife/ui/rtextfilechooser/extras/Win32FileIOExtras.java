/*
 * 11/09/2008
 *
 * FileIOExtras.java - Adds win32-specific functionality to the file chooser.
 * Copyright (C) 2008 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser.extras;

import java.awt.Component;
import java.awt.Window;
import java.lang.reflect.Field;


/**
 * Extra file IO stuff specific to win32.
 *
 * @author Robert Futrell
 * @version 1.1
 */
class Win32FileIOExtras extends FileIOExtras {


	/**
	 * Constructor.
	 *
	 * @throws UnsatisfiedLinkError If the required native dll is not found.
	 */
	public Win32FileIOExtras() {
		loadNativeLibrary();
	}


	/**
	 * Gets the handle for the native peer of a component.
	 *
	 * @param c The component.
	 * @return The handle for that component's peer, or <code>null</code> if
	 *         it cannot be determined.
	 */
	public static long getHwnd(Component c) {
		long hwnd = 0;
		try {
			Class<?> clazz = Class.forName("sun.awt.windows.WComponentPeer");
			Field hwndField = clazz.getDeclaredField("hwnd");
			hwndField.setAccessible(true);
			Object val = hwndField.get(c.getPeer());
			if (val instanceof Long) {
				hwnd = ((Long)val).longValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hwnd;
	}


	/**
	 * Loads the native library used by this class.
	 *
	 * @throws UnsatisfiedLinkError If the required native dll is not found.
	 */
	protected void loadNativeLibrary() {
		System.loadLibrary("Win32FileIOExtras");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean moveToRecycleBin(Window parent, String[] files,
						boolean confirmation, boolean silent) {
		long hwnd = parent!=null ? getHwnd(parent) : 0;
		return moveToRecycleBinImpl(hwnd, files, confirmation, silent);
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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean showFilePropertiesDialog(Window parent, String file) {
		long hwnd = parent!=null ? getHwnd(parent) : 0;
		return showFilePropertiesDialogImpl(hwnd, file);
	}


	/**
	 * Does the dirty work of displaying the properties dialog for a file.
	 * 
	 * @param hwnd The handle to the parent window, or <code>0</code> if
	 *        unknown.
	 * @param file The file whose properties should be displayed.
	 * @return <code>true</code>, unless an error occurs.
	 */
	protected native boolean showFilePropertiesDialogImpl(long hwnd,
			String file);


}