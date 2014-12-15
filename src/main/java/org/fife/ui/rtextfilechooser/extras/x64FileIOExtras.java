/*
 * 11/09/2008
 *
 * FileIOExtras.java - Adds x64-specific functionality to the file chooser.
 * Copyright (C) 2008 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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
	 * {@inheritDoc}
	 */
	@Override
	protected void loadNativeLibrary() {
		System.loadLibrary("x64FileIOExtras");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected native boolean moveToRecycleBinImpl(long hwnd, String[] files,
						boolean confirmation, boolean silent);


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected native boolean showFilePropertiesDialogImpl(long hwnd,
			String file);


}