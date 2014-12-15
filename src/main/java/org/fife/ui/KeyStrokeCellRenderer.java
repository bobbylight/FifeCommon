/*
 * 11/05/2009
 *
 * KeyStrokeCellRenderer.java - Renderers Keystrokes in a JTable.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;


/**
 * Renderer for KeyStrokes in a JTable.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see SubstanceKeyStrokeCellRenderer
 */
public class KeyStrokeCellRenderer extends DefaultTableCellRenderer {


	public static void configure(DefaultTableCellRenderer renderer,
			JTable table, KeyStroke ks) {
		renderer.setText(UIUtil.getPrettyStringFor(ks));
		renderer.setComponentOrientation(table.getComponentOrientation());
	}


	/**
	 * Creates and returns a cell renderer for key strokes.  The returned
	 * object may not be of type <code>KeyStrokeCellRenderer</code>; Substance,
	 * for example, requires you to subclass its own base renderer class.  I
	 * love how Substance makes custom rendering a pain in the ass!
	 *  
	 * @return A cell renderer to use for key strokes.
	 */
	public static TableCellRenderer create() {
		if (SubstanceUtils.isSubstanceInstalled()) {
			//  Use reflection to avoid compile-time dependencies form this
			// class to Substance.
			String clazzName = "org.fife.ui.SubstanceKeyStrokeCellRenderer";
			try {
				Class<?> clazz = Class.forName(clazzName);
				return (TableCellRenderer)clazz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				// Fall through
			}
		}
		return new KeyStrokeCellRenderer();
	}


	@Override
	public Component getTableCellRendererComponent(JTable table,
							Object value, boolean selected, boolean focused,
							int row, int column) {
		super.getTableCellRendererComponent(table, value, selected,
											focused, row, column);
		configure(this, table, (KeyStroke)value);
		return this;
	}


}