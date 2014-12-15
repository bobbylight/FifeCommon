/*
 * 01/13/2013
 *
 * FileChooserViewRendererFactory - Creates appropriate renderers for file
 * chooser views.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.lang.reflect.Constructor;

import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.fife.ui.SubstanceUtils;


/**
 * Creates renderers to use for various file chooser views (list, details,
 * etc.).  If Substance is the current Look and Feel, a Substance-specific
 * renderer is dynamically loaded and used, otherwise, suitable defaults
 * are used.  I love how Substance makes it a pain in the ass to do custom
 * renderers!
 * 
 * @author Robert Futrell
 * @version 1.0
 */
class FileChooserViewRendererFactory {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private FileChooserViewRendererFactory() {
	}


	/**
	 * Returns the renderer to use for all non-special columns in "details
	 * view."
	 *
	 * @return the renderer to use.
	 */
	public static TableCellRenderer createDefaultTableRenderer() {
		if (SubstanceUtils.isSubstanceInstalled()) {
			try {
				Class<?> clazz = Class.forName(
					"org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer");
				return (TableCellRenderer)clazz.newInstance();
			} catch (Exception e) { // Should never happen
				e.printStackTrace();
			}
		}
		return new DefaultTableCellRenderer();
	}


	/**
	 * Returns the renderer to use in the "list view."
	 *
	 * @param chooser The file chooser.
	 * @return The renderer.
	 */
	public static ListCellRenderer createListViewRenderer(
			RTextFileChooser chooser) {

		ListCellRenderer renderer = null;

		if (SubstanceUtils.isSubstanceInstalled()) {
			String clazzName =
				"org.fife.ui.rtextfilechooser.ListViewSubstanceCellRenderer";
			// Use reflection to avoid compile dependency in this class to
			// Substance
			try {
				Class<?> clazz = Class.forName(clazzName);
				Constructor<?> c = clazz.getConstructor(new Class[] { RTextFileChooser.class });
				renderer = (ListCellRenderer)c.newInstance(new Object[] { chooser });
			} catch (Exception e) { // Should never happen
				e.printStackTrace();
			}
		}

		if (renderer==null) {
			renderer = new ListViewCellRenderer(chooser);
		}

		return renderer;

	}


	/**
	 * Returns the renderer to use for file names in the "details view."
	 *
	 * @param chooser The file chooser.
	 * @return The renderer to use.
	 */
	public static TableCellRenderer createTableFileNameRenderer(
			RTextFileChooser chooser) {

		TableCellRenderer renderer = null;

		if (SubstanceUtils.isSubstanceInstalled()) {
			// Use reflection to avoid compile dependency in this class to
			// Substance
			try {
				Class<?> clazz = Class.forName(
					"org.fife.ui.rtextfilechooser.DetailsViewSubstanceFileNameRenderer");
				Constructor<?> c = clazz.getConstructor(new Class[] { RTextFileChooser.class });
				renderer = (TableCellRenderer)c.newInstance(new Object[] { chooser });
			} catch (Exception e) { // Should never happen
				e.printStackTrace();
			}
		}

		if (renderer==null) {
			renderer = new DetailsViewFileNameRenderer(chooser);
		}

		return renderer;

	}


}