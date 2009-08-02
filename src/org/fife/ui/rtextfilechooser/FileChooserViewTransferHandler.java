/*
 * 07/09/2009
 *
 * FileChooserViewTransferHandler.java - Transfer handler for file chooser
 * views.
 * Copyright (C) 2009 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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


	public boolean canImport(JComponent comp, DataFlavor[] flavors) {
		return false;
	}


	protected Transferable createTransferable(JComponent c) {
		List list = null;
		File[] files = view.getSelectedFiles();
		if (files!=null && files.length>0) {
			list = Arrays.asList(files);
		}
		return new FileListTransferable(list);
	}


	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}


}