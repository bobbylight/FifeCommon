/*
 * 11/14/2003
 *
 * RFileChooser.java - Just like a JFileChooser, except like Windows' file
 * choosers, it remembers the current directory only when a file is opened or
 * saved (i.e., only if the dialog is not canceled).
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.io.File;
import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.JFileChooser;


/**
 * A file chooser that looks and behaves exactly like <code>JFileChooser</code>,
 * except that it remembers the "current working directory" (i.e., the directory
 * from which you last opened or to which you saved a file).
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RFileChooser extends JFileChooser {

	private static final long serialVersionUID = 1L;


	/**
	 * Pops up an "Open File" file chooser dialog.  Note that the text that
	 * appears in the approve button is determined by the L&F.
	 *
	 * @param parent The parent component of the dialog, can be
	 *        <code>null</code>; see <code>JFileChooser.showDialog</code>
	 *        for details.
	 * @return The return state of the file chooser when it closes:
	 *         <ul>
	 *            <li>RFileChooser.CANCEL_OPTION
	 *            <li>RFileChooser.APPROVE_OPTION
	 *            <li>RFileChooser.ERROR_OPTION if an error occurs or the
	 *                dialog is dismissed.
	 *         </ul>
	 * @throws HeadlessException If this graphics environment is headless.
	 */
	@Override
	public int showOpenDialog(Component parent) throws HeadlessException {
		File temp = getCurrentDirectory();
		int retVal = super.showOpenDialog(parent);
		if (retVal!=RFileChooser.APPROVE_OPTION)
			setCurrentDirectory(temp);
		return retVal;
	}


	/**
	 * Pops up a "Save File" file chooser dialog.  Note that the text that
	 * appears in the approve button is determined by the L&F.
	 *
	 * @param parent The parent component of the dialog, can be
	 *        <code>null</code>; see <code>JFileChooser.showDialog</code>
	 *        for details.
	 * @return The return state of the file chooser when it closes:
	 *         <ul>
	 *            <li>RFileChooser.CANCEL_OPTION
	 *            <li>RFileChooser.APPROVE_OPTION
	 *            <li>RFileChooser.ERROR_OPTION if an error occurs or the
	 *                dialog is dismissed.
	 *         </ul>
	 * @throws HeadlessException
	 */
	@Override
	public int showSaveDialog(Component parent) throws HeadlessException {
		File temp = getCurrentDirectory();
		int retVal = super.showSaveDialog(parent);
		if (retVal!=RFileChooser.APPROVE_OPTION)
			setCurrentDirectory(temp);
		return retVal;
	}


}