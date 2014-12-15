/*
 * 07/09/2009
 *
 * FileListTransferable.java - A list of files transferable via drag-and-drop.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A transferable object for a list of files.  This class could be greatly
 * improved if we didn't require 1.4 compatibility.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileListTransferable implements Transferable, ClipboardOwner {

	/**
	 * The list of files to transfer.
	 */
	private List<File> fileList;

	private DataFlavor uriListFlavor; // RFC 2483, needed for Linux/OS X


	/**
	 * Constructor.
	 *
	 * @param fileList The list of files to transfer.  This should not be
	 *        <code>null</code>.
	 */
	public FileListTransferable(List<File> fileList) {

		this.fileList = fileList;

		// On Linux and OS X, file explorers don't take javaFileListFlavor
		// but do take this
		try {
			uriListFlavor =
				new DataFlavor("text/uri-list;class=java.lang.String");
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public Object getTransferData(DataFlavor flavor)
									throws UnsupportedFlavorException {

		if (DataFlavor.javaFileListFlavor.equals(flavor)) {
			return new ArrayList<File>(fileList); // Deep copy
		}

		else if (DataFlavor.stringFlavor.equals(flavor)) {
			StringBuilder sb = new StringBuilder();
			if (fileList!=null) {
				for (File file : fileList) {
					sb.append(file.getAbsolutePath()).append('\n');
				}
			}
			return sb.substring(0, sb.length()-1);
		}

		else if (uriListFlavor.equals(flavor)) {
			StringBuilder sb = new StringBuilder();
			if (fileList!=null) {
				for (File file : fileList) {
					String uri = file.toURI().toString();
					if (uri.startsWith("file:/") && uri.length()>6 &&
							uri.charAt(6)!='/') { // Always true?
						// Java doesn't form file URI's properly
						uri = "file://localhost/" + uri.substring(6);
					}
					sb.append(uri).append("\r\n");
				}
			}
			System.out.println("Transferring:\n" + sb.toString());
			return sb.toString(); // Trailing \r\n is required by RFC 2483
		}

		else {
			throw new UnsupportedFlavorException(flavor);
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public DataFlavor[] getTransferDataFlavors() {

		List<DataFlavor> flavors = new ArrayList<DataFlavor>();
		flavors.add(DataFlavor.javaFileListFlavor);
		if (uriListFlavor!=null) {
			flavors.add(uriListFlavor);
		}
		flavors.add(DataFlavor.stringFlavor);

		return flavors.toArray(new DataFlavor[flavors.size()]);

	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor[] flavors = getTransferDataFlavors();
		for (int i=0; i<flavors.length; i++) {
			if (flavors[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}


	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Do nothing
	}


}