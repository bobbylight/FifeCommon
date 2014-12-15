/*
 * 01/14/2013
 *
 * SubstanceKeyStrokeCellRenderer.java - Renderers Keystrokes in a JTable when
 * Substance is installed.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;



/**
 * Renderer for KeyStrokes in a JTable when Substance is installed.  I love
 * how Substance makes custom renderers a pain in the ass!
 *
 * @author Robert Futrell
 * @version 1.0
 * @see KeyStrokeCellRenderer
 */
public class SubstanceKeyStrokeCellRenderer
		extends SubstanceDefaultTableCellRenderer {


	@Override
	public Component getTableCellRendererComponent(JTable table,
							Object value, boolean selected, boolean focused,
							int row, int column) {
		super.getTableCellRendererComponent(table, value, selected,
											focused, row, column);
		KeyStrokeCellRenderer.configure(this, table, (KeyStroke)value);
		return this;
	}


}