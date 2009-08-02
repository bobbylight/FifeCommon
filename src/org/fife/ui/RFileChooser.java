/*
 * 11/14/2003
 *
 * RFileChooser.java - Just like a JFileChooser, except like Windows' file
 * choosers, it remembers the current directory only when a file is opened or
 * saved (i.e., only if the dialog is not canceled).
 * Copyright (C) 2003 Robert Futrell
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
package org.fife.ui;

import java.io.File;
import java.io.Serializable;
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
public class RFileChooser extends JFileChooser implements Serializable {

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
	public int showSaveDialog(Component parent) throws HeadlessException {
		File temp = getCurrentDirectory();
		int retVal = super.showSaveDialog(parent);
		if (retVal!=RFileChooser.APPROVE_OPTION)
			setCurrentDirectory(temp);
		return retVal;
	}


}