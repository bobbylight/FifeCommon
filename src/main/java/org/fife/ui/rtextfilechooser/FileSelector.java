/*
 * 10/26/2009
 *
 * Interface for components that allow the selection of files.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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