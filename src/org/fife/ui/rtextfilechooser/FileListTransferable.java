/*
 * 07/09/2009
 *
 * FileListTransferable.java - A list of files transferable via drag-and-drop.
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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
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
	private List fileList;

	private DataFlavor uriListFlavor; // RFC 2483, needed for Linux/OS X


	/**
	 * Constructor.
	 *
	 * @param fileList The list of files to transfer.  This should not be
	 *        <code>null</code>.
	 */
	public FileListTransferable(List fileList) {

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
			return new ArrayList(fileList); // Deep copy
		}

		else if (DataFlavor.stringFlavor.equals(flavor)) {
			StringBuffer sb = new StringBuffer();
			if (fileList!=null) {
				for (Iterator i=fileList.iterator(); i.hasNext(); ) {
					File f = (File)i.next();
					sb.append(f.getAbsolutePath()).append('\n');
				}
			}
			return sb.subSequence(0, sb.length()-1).toString();
		}

		else if (uriListFlavor.equals(flavor)) {
			StringBuffer sb = new StringBuffer();
			if (fileList!=null) {
				for (Iterator i=fileList.iterator(); i.hasNext(); ) {
					File f = (File)i.next();
					String uri = f.toURI().toString();
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

		ArrayList flavors = new ArrayList();
		flavors.add(DataFlavor.javaFileListFlavor);
		if (uriListFlavor!=null) {
			flavors.add(uriListFlavor);
		}
		flavors.add(DataFlavor.stringFlavor);

		return (DataFlavor[])flavors.toArray(new DataFlavor[flavors.size()]);

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