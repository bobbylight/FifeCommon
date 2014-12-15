/*
 * 10/22/2004
 *
 * TextColorCellRenderer - Renderer for file chooser file names in the Options
 * panel.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.fife.ui.ColorCellRenderer;


/**
 * The renderer for file chooser file names in the Options panel.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TextColorCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;


	/**
	 * Returns the component (this object itself) to use to display the cell.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object color,
						boolean selected, boolean focused, int row, int col) {

		selected = false; // Always render as unselected.
		super.getTableCellRendererComponent(table, color, selected, focused,
				row, col);

		if (table.isEnabled()) {
			setForeground((Color)color);
		}
		else {
			setForeground(((Color)color).darker());
		}

		String text = null;
		if (row==0) {
			text = "filename.ext";
		}
		else {
			String ext = table.getModel().getValueAt(row, 0).toString();
			text = "filename." + ext;
		}
		setText(text);

		setComponentOrientation(table.getComponentOrientation());
		return this;
	}


	/**
	 * Returns the tool tip text for this renderer, which is the RGB value
	 * of the renderer pointed to.
	 *
	 * @return The tool tip text.
	 */
	@Override
	public String getToolTipText() {
		return ColorCellRenderer.getToolTipText(getForeground());
	}


}