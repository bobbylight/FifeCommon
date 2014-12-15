/*
 * 07/09/2009
 *
 * FileChooserViewTransferHandler.java - Transfer handler for file chooser
 * views.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * Transfer handler for copying files from a file chooser's view.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FileChooserViewTransferHandler extends TransferHandler {

	private RTextFileChooserView view;


	public FileChooserViewTransferHandler(RTextFileChooserView view) {
		this.view = view;
	}


	@Override
	public boolean canImport(JComponent comp, DataFlavor[] flavors) {
		return false;
	}


	@Override
	protected Transferable createTransferable(JComponent c) {
		List<File> list = null;
		File[] files = view.getSelectedFiles();
		if (files!=null && files.length>0) {
			list = Arrays.asList(files);
		}
		return new FileListTransferable(list);
	}


	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}


}