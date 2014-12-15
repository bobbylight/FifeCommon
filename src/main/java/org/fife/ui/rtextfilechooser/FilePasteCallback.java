/*
 * 04/13/2013
 *
 * FilePasteCallback - Listens for events from a FilePasteThread.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.io.File;


/**
 * Listens as a copy/paste operation is performed by an instance of
 * FilePasteThread.  Implementations can display a progress bar in the
 * UI, or a <code>ProgressMonitor</code>, to keep the user updated on the
 * operation's progress.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see DefaultFilePasteCallback
 */
public interface FilePasteCallback {


	/**
	 * Called when a file is pasted, or the total number of files to
	 * paste has been recalculated.
	 *
	 * @param pasteCount The number of files pasted thus far.
	 * @param total The total number of files to paste.
	 * @param justPastedFile The name of a file just pasted, or
	 *        <code>null</code> if this event is simply specifying a new
	 *        value for <code>total</code>.
	 * @return Whether the operation should be prematurely terminated.  If
	 *         this is <code>true</code>, any remaining files will not be
	 *         copied.
	 */
	public boolean filePasteUpdate(int pasteCount, int total,
					File justPastedFile);


	/**
	 * Called when the paste operation has completed.
	 *
	 * @param pasteCount The total number of files ultimately pasted.
	 */
	public void pasteOperationCompleted(int pasteCount);


}