/*
 * 09/30/2009
 *
 * Actions.java - Actions used in a file chooser.
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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.fife.ui.rtextfilechooser.extras.FileIOExtras;


/**
 * Actions for the file chooser.
 *
 * @author Robert Futrell
 * @version 1.0
 */
interface Actions {


	/**
	 * Adds the currently-viewed directory to the file chooser "favorites."
	 */
	static class AddToFavoritesAction extends FileChooserAction {

		public AddToFavoritesAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(Action.NAME, chooser.getString("AddToFavorites"));
		}

		public void actionPerformed(ActionEvent e) {
			File dir = chooser.getCurrentDirectory();
			chooser.addToFavorites(dir.getAbsolutePath());
			chooser.focusFileNameField(false);
		}

	}


	/**
	 * Copies any files selected in the file chooser's view.
	 */
	static class CopyAction extends FileChooserAction {

		public CopyAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(Action.NAME, chooser.getString("PopupMenu.Copy"));
			int mod = chooser.getToolkit().getMenuShortcutKeyMask();
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_C, mod));
		}

		public void actionPerformed(ActionEvent e) {

			// Get the selected files.  If there are no selected files (i.e.,
			// they pressed "Ctrl+C" when no files were selected), beep.
			File[] files = chooser.getView().getSelectedFiles();
			if (files==null || files.length==0) {
				UIManager.getLookAndFeel().provideErrorFeedback(chooser);
				return;
			}

			List fileList = Arrays.asList(files);
			FileListTransferable flt = new FileListTransferable(fileList);
			Clipboard clipboard = chooser.getToolkit().getSystemClipboard();
			clipboard.setContents(flt, null);

		}

	}


	/**
	 * Action that handles deleting files.
	 */
	static class DeleteAction extends FileChooserAction {

		public DeleteAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(Action.NAME, chooser.getString("PopupMenu.Delete"));
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		}

		public void actionPerformed(ActionEvent e) {

			// Get the selected files.  If there are no selected files (i.e.,
			// they pressed "delete" when no files were selected), beep.
			File[] files = chooser.getView().getSelectedFiles();
			if (files==null || files.length==0) {
				UIManager.getLookAndFeel().provideErrorFeedback(chooser);
				return;
			}

			FileIOExtras extras = FileIOExtras.getInstance();
			if (extras!=null) {
				handleDeleteNative(files, extras);
			}
			else {
				handleDeleteViaJava(files);
			}

		}

		private void handleDeleteNative(File[] files, FileIOExtras extras) {
			if (extras.moveToRecycleBin(files, true, true)) {
				refresh();
			}
			else {
				UIManager.getLookAndFeel().provideErrorFeedback(chooser);
			}
		}

		private void handleDeleteViaJava(File[] files) {

			// Prompt to confirm the file deletion.
			int count = files.length;
			int choice;
			if (count==1) {
				String fileName = files[0].getName();
				choice = JOptionPane.showConfirmDialog(chooser,
					chooser.getString("DeleteConfirmPrompt") + fileName + "?");
			}
			else { // count>1
				choice = JOptionPane.showConfirmDialog(chooser,
					chooser.getString("DeleteMultipleConfirmPrompt"));
			}

			// If they chose "yes," delete the files.
			if (choice==JOptionPane.YES_OPTION) {
				for (int i=0; i<count; i++) {
					if (!files[i].delete()) {
						Object[] arguments = { files[i].getName() };
						String msg = MessageFormat.format(
									chooser.getString("DeleteFailText"),
									arguments);
						JOptionPane.showMessageDialog(chooser,
									msg, chooser.errorDialogTitle,
									JOptionPane.ERROR_MESSAGE);
					}
				}
				refresh();
			}

		}

		private void refresh() {
			chooser.refreshView();
			// file name field contained names of file(s) to delete, so
			// clear them out.
			chooser.focusFileNameField(true);
		}

	}


	/**
	 * Base class for all file chooser actions.
	 */
	static abstract class FileChooserAction extends AbstractAction {

		protected RTextFileChooser chooser;

		public FileChooserAction(RTextFileChooser chooser) {
			this.chooser = chooser;
		}

	}


	/**
	 * Refreshes the files displayed in the file chooser's view.
	 */
	static class RefreshAction extends FileChooserAction {

		public RefreshAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(Action.NAME, chooser.getString("PopupMenu.Refresh"));
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		}

		public void actionPerformed(ActionEvent e) {
			chooser.refreshView();
		}

	}


	/**
	 * Renames a file.
	 */
	static class RenameAction extends FileChooserAction {

		public RenameAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(Action.NAME, chooser.getString("PopupMenu.Rename"));
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		}

		public void actionPerformed(ActionEvent e) {

			File file = chooser.getView().getSelectedFile();
			if (file==null) {
				UIManager.getLookAndFeel().provideErrorFeedback(chooser);
				return;
			}

			String oldName = file.getName();
			String newName = JOptionPane.showInputDialog(chooser,
				chooser.getString("NewNamePrompt") + oldName + ":", oldName);
			if (newName!=null && !newName.equals(oldName)) {
				try {
					// If they have a separator char in the name, assume
					// they've typed a full path.  Otherwise, just rename
					// it and place it in the same directory.
					if (newName.indexOf(File.separatorChar)==-1) {
						newName = chooser.getCurrentDirectory().
							getCanonicalPath() + File.separatorChar + newName;
					}
					File newFile = new File(newName);
					if (!file.renameTo(newFile)) {
						throw new Exception(chooser.getString("RenameFailText"));
					}
					chooser.refreshView();
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(chooser,
						chooser.getString("RenameErrorMessage") + e2,
						chooser.errorDialogTitle, JOptionPane.ERROR_MESSAGE);
				}
			}

		}

	}


	/**
	 * Action that makes the file chooser display one directory "higher."
	 */
	static class UpOneLevelAction extends FileChooserAction {

		public UpOneLevelAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(Action.NAME, chooser.getString("PopupMenu.UpOneLevel"));
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
		}

		public void actionPerformed(ActionEvent e) {
			File parent = chooser.getCurrentDirectory().getParentFile();
			if (parent!=null) {
				chooser.setCurrentDirectory(parent);
			}
			else { // Should never happen, logic in RTextFileChooser prevents it
				UIManager.getLookAndFeel().provideErrorFeedback(chooser);
			}
		}

	}


}