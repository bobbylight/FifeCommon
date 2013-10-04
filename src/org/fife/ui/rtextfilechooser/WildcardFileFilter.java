/*
 * 07/14/2004
 *
 * WildcardFileFilter.java - A file filter that matches all files.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.io.File;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileFilter;


/**
 * A file filter that takes a wildcard string for its filter.  This class is
 * pretty much 100% ripped off from Java's BasicFileChooserUI's wildcard filter
 * class.
 */
class WildcardFileFilter extends FileFilter {

	private Pattern pattern;


	/**
	 * Sets the pattern to match with.
	 *
	 * @param globPattern The pattern to match with.
	 */
	public void setPattern(String globPattern) {

		char[] gPat = globPattern.toCharArray();
		int gpatLength = gPat.length;
		char[] rPat = new char[gpatLength * 2];
		boolean isWin32 = (File.separatorChar == '\\');
		boolean inBrackets = false;
		int j = 0;

		if (isWin32) {

			// On Windows, ending with *.* is equal to ending with *
			int len = gpatLength;
			if (globPattern.endsWith("*.*"))
				len -= 2;


			for (int i=0; i<len; i++) {

				switch(gPat[i]) {

					// Change "*" into ".*" for the regex.
					case '*':
						rPat[j++] = '.';
						rPat[j++] = '*';
						break;

					// Change "\" into "\\" for the regex.
					case '\\':
						rPat[j++] = '\\';
						rPat[j++] = '\\';
						break;

					// backslash-out any chars with special meaning for
					// the regex, but are valid Windows filename chars;
					// then add this char (and the backslash if any).
					default:
						if ("+()^$.{}[]".indexOf(gPat[i]) >= 0)
						    rPat[j++] = '\\';
						rPat[j++] = gPat[i];
						break;

				}

			} // End of for(int i=0; i<len; i++).

		} // End of if (isWin32).

		// Non-Windows (probably UNIX-based).
		else {

			for (int i = 0; i < gpatLength; i++) {

				switch(gPat[i]) {

					case '*':
						if (!inBrackets)
							rPat[j++] = '.';
						rPat[j++] = '*';
						break;

					case '?':
						rPat[j++] = inBrackets ? '?' : '.';
						break;

					case '[':
						inBrackets = true;
						rPat[j++] = gPat[i];

						if (i < gpatLength - 1) {
							switch (gPat[i+1]) {
								case '!':
								case '^':
									rPat[j++] = '^';
									i++;
									break;

								case ']':
									rPat[j++] = gPat[++i];
									break;
							}
						}
						break;

					case ']':
						rPat[j++] = gPat[i];
						inBrackets = false;
						break;

					case '\\':
						if (i == 0 && gpatLength > 1 && gPat[1] == '~')
							rPat[j++] = gPat[++i];
						else {
							rPat[j++] = '\\';
							if (i < gPat.length - 1 && "*?[]".indexOf(gPat[i+1]) >= 0)
								rPat[j++] = gPat[++i];
							else
								rPat[j++] = '\\';
						}
						break;

					default:
						if (!Character.isLetterOrDigit(gPat[i]))
							rPat[j++] = '\\';
						rPat[j++] = gPat[i];
						break;

				} // End of switch(gPat[i]).

			} // End of for (int i = 0; i < gPat.length; i++).

		} // End of else.

		this.pattern = Pattern.compile(new String(rPat, 0, j),
										Pattern.CASE_INSENSITIVE);

	}


	/**
	 * Returns whether or not the sepcified file is matched by this filter.
	 *
	 * @param f The file to see whether or not it passes this filter.
	 * @return Whether or not this filter accepts <code>f</code>.
	 */
	@Override
	public boolean accept(File f) {
		if (f==null)
			return false;
		if (f.isDirectory())
			return true;
		return pattern.matcher(f.getName()).matches();
	}


	/**
	 * Returns a description of the file filter.
	 */
	@Override
	public String getDescription() {
		return "You never see me!";
	}


}