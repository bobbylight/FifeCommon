/*
 * 11/14/2003
 *
 * ImageFileFilter - A FileFilter that filters everything except image files
 * processed by Swing (*.jpg, *.gif, *.png and *.bmp (on 1.5+)).
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser.filters;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.filechooser.FileFilter;

import org.fife.ui.rtextfilechooser.Utilities;


/**
 * A file filter for <code>JFileChooser</code>s that filters everything except
 * image files supported by Swing (<code>*.gif</code>, <code>*.jpg</code>,
 * <code>*.png</code>, and <code>*.bmp</code> (on 1.5+)).
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ImageFileFilter extends FileFilter {

	private String description;

	private static final String MSG = "org.fife.ui.rtextfilechooser.filters.ImageFileFilter";


	/**
	 * Constructor.
	 *
	 */
	public ImageFileFilter() {
		ResourceBundle msg = ResourceBundle.getBundle(MSG);
		description = msg.getString("Desc");
	}


	/**
	 * Accepts all gif, jpg, and png image files.
	 *
	 * @param f The file to check.
	 * @return Whether the file is an image file.
	 */
	@Override
	public boolean accept(File f) {

		// Accept the "file" if it is a directory.
		if (f.isDirectory()) {
			return true;
		}

		// Get the extension of the file.
		String extension = Utilities.getExtension(f.getName());
		if (extension!=null) {
			extension = extension.toLowerCase();
		}

		return isValidExtension(extension);

	}


	/**
	 * Returns the description of this file filter.
	 *
	 * @return The description.
	 */
	@Override
	public String getDescription() {
		return description;
	}


	/**
	 * Returns whether <code>ext</code> is a valid (supported) image file
	 * extension.
	 *
	 * @param ext The extension to check.
	 * @return Whether the extension is for a supported image file type.
	 */
	private static final boolean isValidExtension(String ext) {
		return ext!=null &&
			("gif".equals(ext) || "jpg".equals(ext) || "png".equals(ext) ||
					"bmp".equals(ext));
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