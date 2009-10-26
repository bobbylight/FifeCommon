/*
 * 10/26/2009
 *
 * Interface for components that allow the selection of files.
 * Copyright (C) 2009 Robert Futrell
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
package org.fife.ui.rtextfilechooser;

import java.io.File;


/**
 * Interface for components that allow file selection.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface FileSelector {


	/**
	 * Gets the selected file, for use when a single file is selected.
	 *
	 * @return The selected file, or <code>null</code> if no file is
	 *         selected.
	 */
	public File getSelectedFile();


	/**
	 * Returns all selected files in this view.
	 *
	 * @return An array of all selected files, or an empty array if no files
	 *         are selected.
	 */
	public File[] getSelectedFiles();


}