/*
 * 11/09/2008
 *
 * FileIOExtras.java - Adds x64-specific functionality to the file chooser.
 * Copyright (C) 2008 Robert Futrell
 * https://bobbylight.github.io/RText/
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
@SuppressWarnings("checkstyle:TypeName")
class x64FileIOExtras extends Win32FileIOExtras {


	@Override
	protected void loadNativeLibrary() {
		System.loadLibrary("x64FileIOExtras");
	}


	@Override
	protected native boolean moveToRecycleBinImpl(long hwnd, String[] files,
						boolean confirmation, boolean silent);


	@Override
	protected native boolean showFilePropertiesDialogImpl(long hwnd,
			String file);


}
