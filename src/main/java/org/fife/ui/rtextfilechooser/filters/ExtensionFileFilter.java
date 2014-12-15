/*
 * 03/24/2005
 *
 * ExtensionFileFilter.java - A file filter that filters by extensions.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser.filters;

import java.io.File;
import javax.swing.filechooser.*;

import org.fife.ui.rtextfilechooser.Utilities;


/**
 * A file filter for <code>JFileChooser</code>s that filters using extensions
 * given by the user.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ExtensionFileFilter extends FileFilter {

	public static final int SYSTEM_CASE_CHECK		= 0;
	public static final int CASE_CHECK				= 1;
	public static final int NO_CASE_CHECK			= 2;

	private String description;
	private String[] extensions;
	private boolean caseCheck;


	/**
	 * Constructor.  The created file filter will use the case-sensitivity of
	 * the operating system.
	 *
	 * @param description The description of this file filter, as will be
	 *        displayed in the file chooser.
	 * @param extension The single extension files can have to match this
	 *        filter.  This string should be everything after the final
	 *        "<code>.</code>".
	 */
	public ExtensionFileFilter(String description, String extension) {
		this(description, extension, SYSTEM_CASE_CHECK);
	}


	/**
	 * Constructor.  The created file filter will use the case-sensitivity of
	 * the operating system.
	 *
	 * @param description The description of this file filter, as will be
	 *        displayed in the file chooser.
	 * @param extensions The extensions files can have to match this filter.
	 *        These strings should be everything after the final
	 *        "<code>.</code>".
	 */
	public ExtensionFileFilter(String description, String[] extensions) {
		this(description, extensions, SYSTEM_CASE_CHECK);
	}


	/**
	 * Constructor.
	 *
	 * @param description The description of this file filter, as displayed in
	 *        the file chooser.
	 * @param extension The single extension files can have to match this
	 *        filter.  This string should be everything after the final
	 *        "<code>.</code>".
	 * @param caseCheck Whether the case of the file's extension should be
	 *        taken into consideration when deciding whether files pass this
	 *        filter.
	 */
	public ExtensionFileFilter(String description, String extension,
							int caseCheck) {
		this(description, extension, caseCheck, true);
	}


	/**
	 * Constructor.
	 *
	 * @param description The description of this file filter, as displayed in
	 *        the file chooser.
	 * @param extensions The extensions files can have to match this filter.
	 *        These strings should be everything after the final
	 *        "<code>.</code>".
	 * @param caseCheck Whether the case of the file's extension should be
	 *        taken into consideration when deciding whether files pass this
	 *        filter.
	 */
	public ExtensionFileFilter(String description, String[] extensions,
							int caseCheck) {
		this(description, extensions, caseCheck, true);
	}


	/**
	 * Constructor.
	 *
	 * @param description The description of this file filter, as displayed in
	 *        the file chooser.
	 * @param extension The single extension files can have to match this
	 *        filter.  This string should be everything after the final
	 *        "<code>.</code>".
	 * @param caseCheck Whether the case of the file's extension should be
	 *        taken into consideration when deciding whether files pass this
	 *        filter.
	 * @param showExtensions Whether the accepted extensions should be
	 *        displayed in the description.
	 */
	public ExtensionFileFilter(String description, String extension,
							int caseCheck, boolean showExtensions) {

		this.extensions = new String[1];
		this.caseCheck = ExtensionFileFilter.doCaseCheck(caseCheck);
		this.extensions[0] = this.caseCheck ? extension :
										extension.toLowerCase();

		// Create the description.
		StringBuilder buf = new StringBuilder(description);
		if (showExtensions)
			buf.append(" (*.").append(extensions[0]).append(")");
		this.description = buf.toString();

	}


	/**
	 * Constructor.
	 *
	 * @param description The description of this file filter, as displayed in
	 *        the file chooser.
	 * @param extensions The extensions files can have to match this filter.
	 *        These strings should be everything after the final
	 *        "<code>.</code>".
	 * @param caseCheck Whether the case of the file's extension should be
	 *        taken into consideration when deciding whether files pass this
	 *        filter.
	 * @param showExtensions Whether the accepted extensions should be
	 *        displayed in the description.
	 */
	public ExtensionFileFilter(String description, String[] extensions,
							int caseCheck, boolean showExtensions) {

		int extCount = extensions==null ? 0 : extensions.length;
		this.extensions = new String[extCount];

		this.caseCheck = ExtensionFileFilter.doCaseCheck(caseCheck);
		if (this.caseCheck) {
			System.arraycopy(extensions,0, this.extensions,0, extCount);
		}
		else {
			for (int i=0; i<extCount; i++)
				this.extensions[i] = extensions[i].toLowerCase();
		}

		// Create the description.
		StringBuilder buf = new StringBuilder(description);
		if (showExtensions && extCount>0) {
			buf.append(" (");
			for (int i=0; i<extCount-1; i++)
				buf.append("*.").append(extensions[i]).append(", ");
			buf.append("*.").append(extensions[extCount-1]).append(")");
		}
		this.description = buf.toString();

	}


	/**
	 * Accepts all directories and all files matching the specified extensions.
	 *
	 * @param f The file to check.
	 * @return Whether the file was accepted.
	 */
	@Override
	public boolean accept(File f) {

		// Accept the "file" if it is a directory.
		if (f.isDirectory()) {
			return true;
		}

		// Get the extension of the file.
		String extension = Utilities.getExtension(f.getName());
		if (extension==null) {
			return false;
		}
		else if (caseCheck) {
			extension = extension.toLowerCase();
		}

		// Now, accept the file ONLY if it matches one of our filters.
		int extensionCount = extensions.length;
		for (int i=0; i<extensionCount; i++) {
			if (extensions[i].equals(extension))
				return true;
		}

		// Any other files are not accepted by this filter.
		return false;

	}


	/**
	 * Returns the final yay or nay as to whether to do case checking.
	 *
	 * @param requestedCheck The requested case checking.  If this value is
	 *        <code>SYSTEM_CASE_CHECK</code>, we'll figure it out ourselves.
	 */
	private static final boolean doCaseCheck(int requestedCheck) {
		switch (requestedCheck) {
			case CASE_CHECK:
				return true;
			case NO_CASE_CHECK:
				return false;
			case SYSTEM_CASE_CHECK:
			default:
				String os = System.getProperty("os.name");
				if (os!=null) {
					// Everyone but Windows is case-sensitive.
					boolean notWindows = os.toLowerCase().
											indexOf("windows")==0;
					return notWindows;
				}
				// Don't know the OS?  Just don't worry about it.
				return false;
		}
	}


	/**
	 * Returns the description of this filter, displayed in the file chooser.
	 *
	 * @return The description.
	 */
	@Override
	public String getDescription() {
		return description;
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