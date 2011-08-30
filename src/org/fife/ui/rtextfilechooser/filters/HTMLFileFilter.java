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
	public String getDescription() {
		return "HTML files (*.htm, *.html)";
	}

}