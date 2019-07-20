/*
 * 09/30/2009
 *
 * Actions.java - Actions used in a file chooser.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.fife.ui.UIUtil;
import org.fife.ui.rtextfilechooser.extras.FileIOExtras;


/**
 * Actions for the file chooser.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface Actions {


	/**
	 * Adds the currently-viewed directory to the file chooser "favorites".
	 */
	class AddToFavoritesAction extends FileChooserAction {

		AddToFavoritesAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(Action.NAME, chooser.getString("AddToFavorites"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			File dir = chooser.getCurrentDirectory();
			chooser.addToFavorites(dir.getAbsolutePath());
			chooser.focusFileNameField(false);
		}

	}


	/**
	 * Copies any files selected in the file chooser's view.
	 */
	class CopyAction extends FileSelectorAction {

		private FileSelector chooser;

		CopyAction(FileSelector chooser) {
			this.chooser = chooser;
			putValue(Action.NAME, getString("Copy"));
			int mod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_C, mod));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			File[] files = getSelectedFiles(chooser);
			if (files.length == 0) {
				return;
			}

			List<File> fileList = Arrays.asList(files);
			FileListTransferable flt = new FileListTransferable(fileList);
			Clipboard clipboard = Toolkit.getDefaultToolkit().
												getSystemClipboard();
			clipboard.setContents(flt, flt);

		}

	}


	/**
	 * Copies the full path of any selected files to the clipboard.
	 */
	class CopyFullPathAction extends FileSelectorAction {

		private FileSelector chooser;

		CopyFullPathAction(FileSelector chooser) {
			this.chooser = chooser;
			putValue(Action.NAME, getString("CopyFullPath"));
			int mod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
			mod |= InputEvent.SHIFT_DOWN_MASK;
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_C, mod));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			File[] files = getSelectedFiles(chooser);
			if (files.length == 0) {
				return;
			}

			Clipboard clipboard = Toolkit.getDefaultToolkit().
					getSystemClipboard();
			StringBuilder sb = new StringBuilder(files[0].getAbsolutePath());
			for (int i = 1; i < files.length; i++) {
				sb.append('\n').append(files[i].getAbsolutePath());
			}

			StringSelection transferable = new StringSelection(sb.toString());
			clipboard.setContents(transferable, transferable);

		}

	}


	/**
	 * Action that handles deleting files.
	 */
	class DeleteAction extends FileChooserAction {

		private boolean hard;

		/**
		 * Constructor.
		 *
		 * @param chooser The file chooser.
		 * @param hard Whether this is a "hard" delete (i.e. permanently delete,
		 *        rather than go through OS means and possibly put into a
		 *        Recycle Bin).
		 */
		DeleteAction(RTextFileChooser chooser, boolean hard) {
			super(chooser);
			putValue(Action.NAME, getString("Delete"));
			this.hard = hard;
			int modifiers = hard ? InputEvent.SHIFT_DOWN_MASK : 0;
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, modifiers));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			// Get the selected files.  If there are no selected files (i.e.,
			// they pressed "delete" when no files were selected), beep.
			File[] files = chooser.getView().getSelectedFiles();
			if (files==null || files.length==0) {
				UIManager.getLookAndFeel().provideErrorFeedback(chooser);
				return;
			}

			// Prompt to confirm the file deletion.
			int count = files.length;
			int choice;
			if (count==1) {
				String fileName = files[0].getName();
				choice = JOptionPane.showConfirmDialog(chooser,
					chooser.getString("DeleteConfirmPrompt") + fileName + "?");
			}
			else { // count > 1
				choice = JOptionPane.showConfirmDialog(chooser,
					chooser.getString("DeleteMultipleConfirmPrompt"));
			}

			// If they chose "yes," delete the files.
			if (choice==JOptionPane.YES_OPTION) {
				for (File file : files) {
					if (!UIUtil.deleteFile(file)) {
						Object[] arguments = {file.getName()};
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
	abstract class FileChooserAction extends FileSelectorAction {

		protected RTextFileChooser chooser;

		FileChooserAction(RTextFileChooser chooser) {
			this.chooser = chooser;
		}

	}


	/**
	 * Base class for all actions that require just a {@code FileSelector}.
	 */
	abstract class FileSelectorAction extends AbstractAction {

		private static ResourceBundle msg = ResourceBundle.getBundle(
			"org.fife.ui.rtextfilechooser.FileChooserPopup");

		/**
		 * Get the selected files.  If there are no selected files (i.e.,
		 * they pressed Ctrl+C when no files were selected), beep.
		 */
		static File[] getSelectedFiles(FileSelector chooser) {

			File[] files;
			if (chooser instanceof RTextFileChooser) {
				// Horrible hack!!!  File chooser shouldn't actually
				// implement FileSelector!  But it's view does...
				files = ((RTextFileChooser)chooser).getView().getSelectedFiles();
			}
			else { // FileSystemTree
				files = chooser.getSelectedFiles();
			}

			if (files==null || files.length==0) {
				UIManager.getLookAndFeel().provideErrorFeedback(null);
				return new File[0];
			}

			return files;
		}

		protected String getString(String key) {
			return msg.getString(key);
		}

	}


	/**
	 * Pastes files into the currently selected directory.
	 */
	class PasteAction extends FileChooserAction {

		PasteAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(NAME, getString("Paste"));
			int mod = chooser.getToolkit().getMenuShortcutKeyMaskEx();
			putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_V, mod));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			File destDir = chooser.getCurrentDirectory();
			boolean copying = false;

			if (destDir.isDirectory()) { // Should always be true

				Clipboard clip = chooser.getToolkit().getSystemClipboard();
				Transferable contents = clip.getContents(null);
				DataFlavor accepted = DataFlavor.javaFileListFlavor;

				try {

					@SuppressWarnings("unchecked")
					List<File> files = (List<File>)contents.
											getTransferData(accepted);
					Window parent = SwingUtilities.getWindowAncestor(chooser);
					FilePasteCallback callback =
							new DefaultFilePasteCallback(parent) {
						@Override
						public void pasteOperationCompleted(int pasteCount){
							super.pasteOperationCompleted(pasteCount);
							chooser.refreshView();
						}
					};

					if (files!=null && files.size()>0) {
						FilePasteThread.paste(parent,
									files, destDir, callback);
						copying = true;
					}

				} catch (UnsupportedFlavorException | IOException ex) {
					ex.printStackTrace();
				}

			}

			if (!copying) {
				UIManager.getLookAndFeel().provideErrorFeedback(chooser);
			}

		}

		/**
		 * Sets the enabled state of this action based on whether the system
		 * clipboard contains a list of files to copy).
		 */
		void checkEnabledState() {
			setEnabled(Utilities.getClipboardContainsFileList());
		}

	}


	/**
	 * Refreshes the files displayed in the file chooser's view.
	 */
	class RefreshAction extends FileChooserAction {

		RefreshAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(Action.NAME, getString("Refresh"));
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			chooser.refreshView();
		}

	}


	/**
	 * Renames a file.
	 */
	class RenameAction extends FileChooserAction {

		RenameAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(Action.NAME, getString("Rename"));
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		}

		@Override
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
	 * Opens a file with the default system editor or viewer.<p>
	 *
	 * NOTE: IntelliJ is wrong about "access can be package-private".  Must
	 * be public for library consumers.
	 */
	public class SystemOpenAction extends FileSelectorAction {

		/*
         * NOTE: This method is a FileChooserAction only so we can use its
         * ResourceBundle.  This is somewhat of a hack.
         */

		private FileSelector chooser;
		private OpenMethod method;

		/**
		 * Simple enumeration of edit vs. open.
		 */
		public enum OpenMethod {

			OPEN(Desktop.Action.OPEN, "SystemOpenViewer"),
			EDIT(Desktop.Action.EDIT, "SystemOpenEditor");

			OpenMethod(Desktop.Action method, String localizationKey) {
				this.method = method;
				this.localizationKey = localizationKey;
			}

			private OpenMethod opposite() {
				return this == OPEN ? EDIT : OPEN;
			}

			private Desktop.Action method;
			private String localizationKey;

		}

		// NOTE: IntelliJ is wrong about "access can be package-private".  Must
		// be public for library consumers
		public SystemOpenAction(FileSelector chooser, OpenMethod method) {
			this.chooser = chooser;
			this.method = method;
			putValue(Action.NAME, getString(method.localizationKey));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			File file = chooser.getSelectedFile();
			if (file==null) {
				UIManager.getLookAndFeel().provideErrorFeedback(null);
				return;
			}

			Desktop desktop = UIUtil.getDesktop();
			if (desktop!=null) {
				// Since OSes can be finicky over whether something is
				// considered a "viewer" or an "editor," and usually folks
				// just want to open the application in *something*, if what
				// they ask for fails we'll try the other application type.
				if (!openImpl(desktop, method, file)) {
					if (!openImpl(desktop, method.opposite(), file)) {
						UIManager.getLookAndFeel().provideErrorFeedback(null);
					}
				}
			}

		}

		private static boolean openImpl(Desktop desktop,
				OpenMethod method, File file) {

			boolean success = false;

			if (desktop.isSupported(method.method)) {
				try {
					switch (method.method) {
						case OPEN:
							desktop.open(file);
							success = true;
							break;
						default:
							desktop.edit(file);
							success = true;
							break;
					}
				} catch (IOException ioe) {
					// Do nothing; success will return false
				}
			}

			return success;

		}

	}


	/**
	 * Displays the "properties" dialog for any selected files.
	 */
	class PropertiesAction extends FileSelectorAction {

		/*
         * This is a File Chooser action only to get at its resource bundle, so
         * this is kind of a hack.
         */

		private FileSelector selector;

		PropertiesAction(FileSelector selector) {
			putValue(NAME, "Properties");
			final int alt = InputEvent.ALT_DOWN_MASK;
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, alt);
			putValue(ACCELERATOR_KEY, ks);
			this.selector = selector;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			FileIOExtras extras = FileIOExtras.getInstance();
			if (extras==null) {
				UIManager.getLookAndFeel().provideErrorFeedback(null);
				return;
			}

			File[] selected = getSelectedFiles(selector);

			Window parent = SwingUtilities.
					getWindowAncestor((Component)selector);
			for (File file : selected) {
				extras.showFilePropertiesDialog(parent, file);
			}

		}

	}


	/**
	 * Action that makes the file chooser display one directory "higher".
	 */
	class UpOneLevelAction extends FileChooserAction {

		UpOneLevelAction(RTextFileChooser chooser) {
			super(chooser);
			putValue(Action.NAME, getString("UpOneLevel"));
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
		}

		@Override
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
