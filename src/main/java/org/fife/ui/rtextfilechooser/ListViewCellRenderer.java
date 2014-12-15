/*
 * 01/13/2013
 *
 * ListViewCellRenderer - Default renderer for the "list view" of the file
 * chooser.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.fife.ui.UIUtil;


/**
 * The default renderer for the "list view" of the file chooser.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
class ListViewCellRenderer extends DefaultListCellRenderer {

	private RTextFileChooser chooser;


	public ListViewCellRenderer(RTextFileChooser chooser) {
		this.chooser = chooser;
		setOpaque(true);
	}


	private final Color getForegroundColor(File file, FileTypeInfo fti) {
		Color color = null;
		if (chooser.getShowHiddenFiles() && file.isHidden()) {
			color = chooser.getHiddenFileColor();
		}
		else {
			color = fti.labelTextColor;
		}
		return color;
	}


	@Override
	public Component getListCellRendererComponent(JList list, Object value,
									int index, boolean isSelected,
									boolean cellHasFocus) {

		super.getListCellRendererComponent(list, value, index,
									isSelected, cellHasFocus);
		File file = (File)value;
		FileTypeInfo info = chooser.getFileTypeInfoFor(file);
		String fileName = file.getName();

		boolean isAlreadyOpened = chooser.isOpenedFile(file);

		// Set the text to display.  Make sure we underline already-
		// opened files.
		String text = fileName;
		if (isAlreadyOpened && chooser.getStyleOpenFiles()) {
			if (info.labelTextColor==null) {
				text = chooser.addOpenFileStyleHtml(fileName);
			}
			else {
				Color fg = null;
				if (isSelected) {
					fg = list.getSelectionForeground();
				}
				else {
					fg = getForegroundColor(file, info);
					if (fg==null) { // Special care for this case.
						fg = list.getSelectionForeground();
					}
				}
				String color = UIUtil.getHTMLFormatForColor(fg);
				text = "<font color=\"" + color + "\">" +
								fileName + "</font>";
				text = chooser.addOpenFileStyleHtml(text);
			}
		}
		setText(text);

		// Set the image according to the file type.
		setIcon(info.icon);
		if (!isSelected) {
			setForeground(getForegroundColor(file, info));
		}

		return this;

	}


	@Override
	public void setBounds(int x, int y, int width, int height) {
		// TODO: For RTL locales the code below makes the cell renderer
		// content left-aligned.  Come up with a way to keep it right-
		// aligned in this case.
		if (getComponentOrientation().isLeftToRight()) {
			int w2 = Math.min(width, this.getPreferredSize().width+4);
			super.setBounds(x, y, w2, height); 
		}
		else {
			super.setBounds(x,y, width,height);
		}
	}


}