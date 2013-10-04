/*
 * 03/30/2004
 *
 * HTMLFileFilter - A FileFilter that filters everything except HTML files.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser.filters;

import java.io.File;
import javax.swing.filechooser.*;

import org.fife.ui.rtextfilechooser.Utilities;


/**
 * A file filter for <code>JFileChooser</code>s that filters everything except
 * HTML files.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class HTMLFileFilter extends FileFilter {

	/**
	 * Accept all directories and all *.html/*.htm files.
	 *
	 * @param f The file to check.
	 * @return Whether the file passes this filter.
	 */
	@Override
	public boolean accept(File f) {
		// Accept all directories
		if (f.isDirectory()) {
			return true;
		}
		String extension = Utilities.getExtension(f.getName());
		return extension!=null && 
			(extension.equalsIgnoreCase("htm") ||
					extension.equalsIgnoreCase("html"));
	}


	// The description of this filter.
	@Override
	public String getDescription() {
		return "HTML files (*.htm, *.html)";
	}


	/**
	 * Overridden to return the description of this file filter, that way we
	 * render nicely in combo boxes.
	 *
	 * @return A string representation of this filter.
	 */
	@Override
	public String toString() {
		return getDescription();
	}


}