/*
 * 07/07/2004
 *
 * FileTypeInfo.java - A wrapper class containing an icon and a color, used to
 *                     describe a style used when painting files of a given type
 *                     in an RTextFileChooser.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Color;
import javax.swing.Icon;


/**
 * An icon and a color grouped together.  This data structure is used
 * to associate a "style" with a given file type (i.e., files ending in
 * a given extension) in an <code>RTextFileChooser</code>.
 *
 * @author Robert Futrell
 * @version 0.2
 */
public class FileTypeInfo {

	/**
	 * An icon to use in the file chooser when a file of the specified type is
	 * displayed.
	 */
	public Icon icon;

	/**
	 * The color to paint the name of the file in the file chooser.
	 */
	public Color labelTextColor;


	/**
	 * Constructor.
	 *
	 * @param icon The icon to use.
	 * @param labelTextColor The color to use for the label.
	 */
	public FileTypeInfo(Icon icon, Color labelTextColor) {
		this.icon = icon;
		this.labelTextColor = labelTextColor;
	}


}