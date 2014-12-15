/*
 * 10/27/2004
 *
 * FileChooserOwner.java - A class that "owns" an RTextFileChooser.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;


/**
 * A class that has an {@link RTextFileChooser}.  This interface is used by
 * {@link FileChooserFavoritesOptionPanel} to get the file chooser to
 * configure.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface FileChooserOwner {


	/**
	 * Returns the file chooser to configure.
	 *
	 * @return The file chooser.
	 */
	public RTextFileChooser getFileChooser();


}