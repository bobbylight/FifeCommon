/*
 * 11/09/2008
 *
 * FileIOExtras.java - Adds platform-specific functionality to the file chooser.
 * Copyright (C) 2008 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser.extras;

import java.awt.Window;
import java.io.File;


/**
 * Base class for host-specific "IO extras" classes used by the file
 * chooser.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class FileIOExtras {

	/**
	 * Singleton instance.
	 */
	private static FileIOExtras INSTANCE;

	/**
	 * Whether we've attempted to load the singleton instance.
	 */
	private static boolean loaded;


	/**
	 * Protected constructor to prevent instantiation.
	 */
	protected FileIOExtras() {
	}


	/**
	 * Returns the singleton instance of the "IO extras" for this platform.
	 *
	 * @return The instance, or <code>null</code> if this platform
	 *         does not support this feature.
	 */
	public synchronized static FileIOExtras getInstance() {
		if (!loaded) {
			loaded = true;
			String os = System.getProperty("os.name");
			if (os.toLowerCase().indexOf("windows")>-1) {
				String arch = System.getProperty("os.arch");
				try {
					if ("x86".equals(arch)) {
						INSTANCE = new Win32FileIOExtras();
					}
					else if ("amd64".equals(arch)) {
						INSTANCE = new x64FileIOExtras();
					}
				} catch (UnsatisfiedLinkError ule) {
					// dll not found.  We'll fall back to Java's
					// File.delete().
					INSTANCE = null; // Keep FindBugs happy
				}
			}
		}
		return INSTANCE;
	}


	/**
	 * Moves the specified files and/or directories to the recycle bin.
	 *
	 * @param parent The parent window for the dialog.  This may be
	 *        <code>null</code>.
	 * @param files An array of files/directories to move.
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
	public boolean moveToRecycleBin(Window parent, File[] files,
						boolean confirmation, boolean silent) {
		if (files==null || files.length==0) {
			return true;
		}
		String[] fileNames = new String[files.length];
		for (int i=0; i<files.length; i++) {
			fileNames[i] = files[i].getAbsolutePath();
		}
		return moveToRecycleBin(parent, fileNames, confirmation, silent);
	}


	/**
	 * Moves the specified file to the recycle bin.
	 *
	 * @param parent The parent window for the dialog.  This may be
	 *        <code>null</code>.
	 * @param files The names of the files to move.  These should
	 *        be absolute paths.
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
	public abstract boolean moveToRecycleBin(Window parent, String[] files,
						boolean confirmation, boolean silent);


	/**
	 * Displays the OS's "properties" dialog for a file or directory.
	 *
	 * @param parent The parent window for the dialog.  This may be
	 *        <code>null</code>.
	 * @param file The file to display the properties of.
	 * @return Whether the operation was successful.  Implementations that do
	 *         not implement this method can return <code>false</code>.
	 */
	public boolean showFilePropertiesDialog(Window parent, File file) {
		return showFilePropertiesDialog(parent, file.getAbsolutePath());
	}


	/**
	 * Displays the OS's "properties" dialog for a file or directory.
	 *
	 * @param parent The parent window for the dialog.  This may be
	 *        <code>null</code>.
	 * @param file The file to display the properties of.
	 * @return Whether the operation was successful.  Implementations that do
	 *         not implement this method can return <code>false</code>.
	 */
	public abstract boolean showFilePropertiesDialog(Window parent,
			String file);


	/**
	 * Entry point for a simple debugging application.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {

		if (args.length==0) {
			System.err.println("Usage: FileIOExtras <absolute-fileName>");
			System.exit(1);
		}

		FileIOExtras extras = FileIOExtras.getInstance();
		if (extras!=null) {
			boolean success = extras.moveToRecycleBin(null, args, true, true);
			System.out.println("Success: " + success);
		}
		else {
			System.out.println("Extras not supported or dll not found");
		}

	}


}