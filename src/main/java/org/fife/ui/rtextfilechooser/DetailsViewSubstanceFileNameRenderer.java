/*
 * 01/13/2013
 *
 * DetailsViewSubstanceFileNameRenderer - Renderer for the "file name" column
 * of the Details view when Substance is installed.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Component;
import java.io.File;
import javax.swing.JTable;

import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;
import org.pushingpixels.substance.internal.ui.SubstanceTableUI;
import org.pushingpixels.substance.internal.ui.SubstanceTableUI.TableCellId;


/**
 * Renderer used for columns displaying <code>File</code>s in a
 * <code>JTable</code> when Substance is installed.
 * 
 * @author Robert Futrell
 * @version 1.0
 * @see DetailsViewFileNameRenderer
 */
class DetailsViewSubstanceFileNameRenderer
		extends SubstanceDefaultTableCellRenderer{

	private RTextFileChooser chooser;


	public DetailsViewSubstanceFileNameRenderer(RTextFileChooser chooser) {
		this.chooser = chooser;
	}


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
				boolean selected, boolean focused, int row, int column)  {

		super.getTableCellRendererComponent(table, value, selected,
									focused, row, column);

		File file = (File)value;
		String fileName = file.getName();

		if (chooser.isOpenedFile(file)) {
			fileName = "<html><u>" + fileName + "</u>";
		}
		setText(fileName);

		// Set the image according to the file type.
		FileTypeInfo info = chooser.getFileTypeInfoFor(file);
		setIcon(info.icon);
		if (!selected && !substanceAndRollover(table, row, column)) {
			if (chooser.getShowHiddenFiles() && file.isHidden()) {
				setForeground(chooser.getHiddenFileColor());
			}
			else {
				setForeground(info.labelTextColor);
			}
		}

		return this;

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