/*
 * 01/13/2013
 *
 * DetailsViewFileNameRenderer - Default renderer for the "file name" column
 * of the Details view.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import org.fife.ui.UIUtil;


/**
 * Cell renderer for file names in the Details View when Substance is not
 * installed.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see DetailsViewSubstanceFileNameRenderer
 */
class DetailsViewFileNameRenderer extends DefaultTableCellRenderer {

	private RTextFileChooser chooser;
	private Rectangle paintTextR = new Rectangle();
	private Rectangle paintIconR = new Rectangle();
	private Rectangle paintViewR = new Rectangle();
	private boolean isAlreadyOpened;


	public DetailsViewFileNameRenderer(RTextFileChooser chooser) {
		this.chooser = chooser;
	}


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);

		File file = (File) value;
		String fileName = file.getName();

		isAlreadyOpened = chooser.isOpenedFile(file);

		setText(fileName);

		// Set the image according to the file type.
		FileTypeInfo info = chooser.getFileTypeInfoFor(file);
		setIcon(info.icon);
		if (!isSelected) {
			if (chooser.getShowHiddenFiles() && file.isHidden())
				setForeground(chooser.getHiddenFileColor());
			else
				setForeground(info.labelTextColor);
		}

		return this;

	}


	@Override
	public void paintComponent(Graphics g) {

		String text = getText();
		Icon icon = getIcon();
		FontMetrics fm = g.getFontMetrics();

		paintViewR.x = paintViewR.y = 0;
		paintViewR.width = getWidth();
		paintViewR.height = getHeight();

		g.setColor(getBackground());
		g.fillRect(paintViewR.x,paintViewR.y, paintViewR.width,paintViewR.height);

		paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
		paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

		String clippedText = 
				SwingUtilities.layoutCompoundLabel(this,
									fm,
									text,
									icon,
									getVerticalAlignment(),
									getHorizontalAlignment(),
									getVerticalTextPosition(),
									getHorizontalTextPosition(),
									paintViewR,
									paintIconR,
									paintTextR,
									getIconTextGap());

		if (icon != null)
			icon.paintIcon(this, g, paintIconR.x, paintIconR.y);

		if (text != null) {
			RenderingHints old = UIUtil.setNativeRenderingHints((Graphics2D)g);
			int textX = paintTextR.x;
			int textY = paintTextR.y + fm.getAscent();
			g.setColor(getForeground());
			g.drawString(clippedText, textX,textY);
			if (isAlreadyOpened && chooser.getStyleOpenFiles()) {
				g.drawLine(textX, textY+2, textX+paintTextR.width, textY+2);
			}
			if (old!=null) {
				((Graphics2D)g).addRenderingHints(old);
			}
		}

	}


}