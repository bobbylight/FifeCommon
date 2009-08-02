/*
 * 03/30/2004
 *
 * HTMLFileFilter - A FileFilter that filters everything except HTML files.
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
package org.fife.ui.rtextfilechooser.filters;

import java.io.File;
import javax.swing.filechooser.*;


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
	public boolean accept(File f) {

		// Accept the "file" if it is a directory.
		if (f.isDirectory()) {
			return true;
		}

		// Get the extension of the file.
		String extension = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i>0 && i<s.length()-1)
			extension = s.substring(i+1).toLowerCase();

		// Now, accept the file ONLY if it is a .txt file.
		if (extension!=null && 
			(extension.equals("htm") || extension.equals("html")))
			return true;

		// Any other files are not accepted by this filter.
		return false;

	}


	// The description of this filter.
	public String getDescription() {
		return "HTML files (*.htm, *.html)";
	}

}