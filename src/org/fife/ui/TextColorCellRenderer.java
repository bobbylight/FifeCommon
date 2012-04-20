/*
 * 10/22/2004
 *
 * TextColorCellRenderer.java - Renderer for a JTable that displays text in
 * a color.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;


/**
 * A renderer for a <code>JTable</code> that displays text in a color.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class TextColorCellRenderer extends ColorCellRenderer
									implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	/**
	 * How much darker to make colors when they are disabled.
	 */
	private static final int DARKER_AMOUNT		= 20;


	/**
	 * Constructor.
	 */
	public TextColorCellRenderer(String text) {
		setText(text);
		setOpaque(false); // Undo super's stuff.
	}


	/**
	 * Returns the color displayed (in this case, the foreground color).
	 *
	 * @return The "displayed" color.
	 */
	public Color getDisplayedColor() {
		return getForeground();
	}


	/**
	 * Returns the component (this object itself) to use to display the cell.
	 */
	public Component getTableCellRendererComponent(JTable table, Object color,
								boolean isSelected, boolean hasFocus,
								int row, int column) {
		if (table.isEnabled()) {
			setBackground(UIManager.getColor("List.background"));
			setForeground((Color)color);
		}
		else {
			setBackground(UIUtil.deriveColor(
							UIManager.getColor("List.background"),
							DARKER_AMOUNT));
			setForeground(((Color)color).darker());
		}
		setComponentOrientation(table.getComponentOrientation());
		return this;
	}


}