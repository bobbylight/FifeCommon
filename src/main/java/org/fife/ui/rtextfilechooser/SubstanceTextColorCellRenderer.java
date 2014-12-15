/*
 * 01/19/2013
 *
 * SubstanceTextColorCellRenderer - Renderer for file chooser file names in
 * the Options panel when Substance is installed.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;

import org.fife.ui.ColorCellRenderer;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;
import org.pushingpixels.substance.internal.ui.SubstanceTableUI;
import org.pushingpixels.substance.internal.ui.SubstanceTableUI.TableCellId;


/**
 * The renderer for file chooser file names in the Options panel when Substance
 * is installed.  I love how Substance is a pain in the ass for custom
 * rendering!
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SubstanceTextColorCellRenderer extends SubstanceDefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;


	/**
	 * Returns the component (this object itself) to use to display the cell.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object color,
						boolean selected, boolean focused, int row, int col) {

		// SubstanceDefaultTableCellRenderer ignores this argument when deciding
		// how to render cell backgrounds (!).  Substance sucks.
		//selected = false;
		super.getTableCellRendererComponent(table, color, selected, focused,
				row, col);

		// Since default renderer always renders backgrounds "properly," we
		// must take care here.
		if (!selected && !substanceAndRollover(table, row, col)) {
			if (table.isEnabled()) {
				setForeground((Color)color);
			}
			else {
				setForeground(((Color)color).darker());
			}
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


	private static final boolean substanceAndRollover(JTable table, int row,
			int col) {
		// Already know we're Substance if this renderer is installed...
		SubstanceTableUI ui = (SubstanceTableUI)table.getUI();
		TableCellId cellId = new TableCellId(row, col);
		ComponentState state = ui.getCellState(cellId);
		return state==ComponentState.ROLLOVER_ARMED ||
				state==ComponentState.ROLLOVER_SELECTED ||
				state==ComponentState.ROLLOVER_UNSELECTED;
	}


}