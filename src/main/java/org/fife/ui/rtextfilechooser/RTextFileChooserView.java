/*
 * 07/14/2004
 *
 * RTextFileChooserView.java - Specifies methods common to all RTextFileChooser
 * views.
 * Copyright (C) 2004 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.util.List;


/**
 * Specifies methods common to all <code>RTextFileChooser</code> views.  The
 * goal is to abstract out enough information so that the
 * <code>RTextFileChooser</code> doesn't care what type of view it is showing;
 * it can just stick to a single API to refresh its file list, etc.
 *
 * @author Robert Futrell
 * @version 0.1
 */
interface RTextFileChooserView extends FileSelector {


	/**
	 * Clears all files displayed by this view.
	 */
	void clearDisplayedFiles();


	/**
	 * Makes sure there are no selected files in this view.
	 */
	void clearSelection();


	/**
	 * Makes sure the specified file is visible in the view.
	 *
	 * @param file The file that is to be visible.
	 */
	void ensureFileIsVisible(File file);


	/**
	 * Returns the color used to paint the name of files with unknown type.
	 *
	 * @return The color used.
	 */
	Color getDefaultFileColor();


	/**
	 * Returns the number of files currently being displayed.
	 *
	 * @return The number of files currently being displayed.
	 */
	int getDisplayedFileCount();


	/**
	 * Returns the file at the specified point in the view.
	 *
	 * @param p The point at which to look for a file.
	 * @return The file at that point (or <code>null</code> if there isn't
	 *         one???  This may very-well be view-dependent).
	 */
	File getFileAtPoint(Point p);


	/**
	 * Removes all listeners this view has created and added to itself.  This
	 * method is here to get around the fact that <code>finalize</code> is
	 * not going to be called as long as listeners are still registered for
	 * this view, but nobody else knows about these listeners except for the
	 * view.
	 */
	void removeAllListeners();


	/**
	 * Selects the file at the specified point in the view.  If no file
	 * exists at that point, the selection should be cleared.
	 *
	 * @param p The point at which a file should be selected.
	 */
	void selectFileAtPoint(Point p);


	/**
	 * Sets the files displayed by this view.
	 *
	 * @param files The list of files to display.  These files
	 *        are not necessarily sorted by file name.
	 */
	void setDisplayedFiles(List<File> files);


	/**
	 * Sets whether this view allows the selection of multiple files.
	 *
	 * @param enabled whether to allow the selection of multiple
	 *        files.
	 */
	void setMultiSelectionEnabled(boolean enabled);


	/**
	 * Selects the specified files in the view.
	 *
	 * @param files The files to select.  If any of the files are not in
	 *        the file chooser's <code>currentDirectory</code>, then
	 *        they are not selected.
	 */
	void setSelectedFiles(File[] files);


}
