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

import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.fife.util.SubstanceUtil;
import org.pushingpixels.substance.api.renderer.SubstanceDefaultTableCellRenderer;

import java.io.File;


/**
 * Creates renderers to use for various file chooser views (list, details,
 * etc.).  If Substance is the current Look and Feel, a Substance-specific
 * renderer is loaded and used, otherwise, suitable defaults are used.
 * I love how Substance makes it a pain in the ass to do custom renderers!
 *
 * @author Robert Futrell
 * @version 1.0
 */
final class FileChooserViewRendererFactory {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private FileChooserViewRendererFactory() {
	}


	/**
	 * Returns the renderer to use for all non-special columns in "details
	 * view".
	 *
	 * @return the renderer to use.
	 */
	public static TableCellRenderer createDefaultTableRenderer() {
		if (SubstanceUtil.isSubstanceInstalled()) {
			return new SubstanceDefaultTableCellRenderer();
		}
		return new DefaultTableCellRenderer();
	}


	/**
	 * Returns the renderer to use in the "list view".
	 *
	 * @param chooser The file chooser.
	 * @return The renderer.
	 */
	@SuppressWarnings("unchecked") // Must use DefaultListCellRenderer
	public static ListCellRenderer<File> createListViewRenderer(
			RTextFileChooser chooser) {

		ListCellRenderer renderer;

		if (SubstanceUtil.isSubstanceInstalled()) {
			renderer = new ListViewSubstanceCellRenderer(chooser);
		}
		else {
			renderer = new ListViewCellRenderer(chooser);
		}

		return renderer;

	}


	/**
	 * Returns the renderer to use for file names in the "details view".
	 *
	 * @param chooser The file chooser.
	 * @return The renderer to use.
	 */
	public static TableCellRenderer createTableFileNameRenderer(
			RTextFileChooser chooser) {

		TableCellRenderer renderer;

		if (SubstanceUtil.isSubstanceInstalled()) {
			renderer = new DetailsViewSubstanceFileNameRenderer(chooser);
		}
		else {
			renderer = new DetailsViewFileNameRenderer(chooser);
		}

		return renderer;

	}


}
