/*
 * 04/12/2013
 *
 * DefaultFilePasteCallback - Displays the progress of a file paste operation
 * using a ProgressMonitor.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Component;
import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.ProgressMonitor;


/**
 * A default file paste callback suitable for most applications.  Uses a
 * standard Swing <code>ProgressMonitor</code> to display a progress bar for
 * long-running paste operations.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class DefaultFilePasteCallback implements FilePasteCallback {

	private ProgressMonitor monitor;
	private String noteFormat;

	private static final String MSG = "org.fife.ui.rtextfilechooser.FilePaste";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Constructor.
	 */
	public DefaultFilePasteCallback(Component parent) {
		monitor = new ProgressMonitor(parent,
				msg.getString("Dialog.ProgressMonitor.Title"), "", 0, 1);
	}


	/**
	 * Updates the progress monitor to display the number of files remaining,
	 * the number of files copied, and the name of the file currently being
	 * copied.
	 */
	public boolean filePasteUpdate(int pasteCount, int total,
			File justPastedFile) {
		monitor.setProgress(pasteCount-1);
		monitor.setMaximum(total);
		if (justPastedFile!=null) {
			if (noteFormat==null) {
				noteFormat = msg.getString("Dialog.ProgressMonitor.NoteFormat");
			}
			// TODO: When Java 1.4 support dropped, use Integer.valueOf()
			Object[] args = { new Integer(pasteCount), new Integer(total),
					justPastedFile.getAbsolutePath() };
			String note = MessageFormat.format(noteFormat, args);
			monitor.setNote(note);
			//System.out.println(note);
		}
		return monitor.isCanceled();
	}


	/**
	 * Hides the progress monitor, if it is visible.
	 */
	public void pasteOperationCompleted(int pasteCount) {
		monitor.close();
	}


}