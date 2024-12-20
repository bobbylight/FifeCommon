/*
 * 04/13/2013
 *
 * FileSystemTreeActions - Actions in a File System Tree.
 * Copyright (C) 2013 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Component;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.EventObject;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import org.fife.ui.UIUtil;
import org.fife.ui.rtextfilechooser.FileSystemTree.FileSystemTreeNode;


/**
 * Actions used by <code>FileSystemTree</code>s.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FileSystemTreeActions {


	/**
	 * Base class for file system tree-related actions.
	 */
	abstract static class AbstractTreeAction extends AbstractAction {

		protected FileSystemTree tree;
		private static ResourceBundle msg = ResourceBundle.getBundle(
							"org.fife.ui.rtextfilechooser.FileSystemTree");

		AbstractTreeAction(FileSystemTree tree) {
			this.tree = tree;
		}

		protected String getString(String key) {
			return msg.getString(key);
		}

	}


	/**
	 * Action that handles deleting files.
	 */
	static class DeleteAction extends AbstractTreeAction {

		private Window window;
		private boolean hard;

		DeleteAction(Window parent, FileSystemTree tree, boolean hard) {
			super(tree);
			putValue(NAME, getString("Delete"));
			this.hard = hard;
			int modifiers = hard ? InputEvent.SHIFT_DOWN_MASK : 0;
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, modifiers));
			this.window = parent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			if (window==null) { // Often unfortunately null
				window = SwingUtilities.getWindowAncestor(tree);
			}

			// Get the selected files.  If there are no selected files (i.e.,
			// they pressed "delete" when no files were selected), beep.
			File[] files = tree.getSelectedFiles();
			if (files==null || files.length==0) {
				UIManager.getLookAndFeel().provideErrorFeedback(window);
				return;
			}

			// Prompt to confirm the file deletion.
			int count = files.length;
			int choice;
			if (count==1) {
				String fileName = files[0].getName();
				choice = JOptionPane.showConfirmDialog(window,
					RTextFileChooser.MSG.
							getString("DeleteConfirmPrompt") + fileName + "?");
			}
			else { // count>1
				choice = JOptionPane.showConfirmDialog(window,
					RTextFileChooser.MSG.
							getString("DeleteMultipleConfirmPrompt"));
			}

			// If they chose "yes," delete the files.
			if (choice==JOptionPane.YES_OPTION) {
				for (File file : files) {
					if (!UIUtil.deleteFile(file, hard)) {
						Object[] arguments = {file.getName()};
						String msg = MessageFormat.format(
							RTextFileChooser.MSG.getString("DeleteFailText"),
							arguments);
						JOptionPane.showMessageDialog(window,
							msg,
							RTextFileChooser.MSG.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
					}
				}
				refresh();
			}

		}

		private void refresh() {
			FileSystemTreeNode node = (FileSystemTreeNode)tree.
								getLastSelectedPathComponent();
			if (node!=null) {
				FileSystemTreeNode parent= (FileSystemTreeNode)node.getParent();
				tree.refreshChildren(parent);
				((DefaultTreeModel)tree.getModel()).reload(parent);
			}
		}

	}


	/**
	 * Cell editor used when the user adds a new file or folder to the tree.
	 */
	private static class FileTreeCellEditor implements TreeCellEditor {

		private FileSystemTree tree;
		private File root;
		private DefaultTreeCellEditor delegate;

		FileTreeCellEditor(final FileSystemTree fst,
									DefaultTreeCellRenderer renderer,
									File root, final boolean dir) {

			this.tree = fst;
			this.root = root;

			delegate = new DefaultTreeCellEditor(tree, renderer) {

				@Override
				protected TreeCellEditor createTreeCellEditor() {
					TreeCellEditor editor = super.createTreeCellEditor();
					if (editor instanceof DefaultCellEditor dce) { // Always true
						dce.getComponent().addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								if (!e.isTemporary()) {
									cancelCellEditing();
								}
							}
						});
					}
					return editor;
				}

				@Override
				protected void determineOffset(JTree tree, Object value,
						boolean selected, boolean expanded,
						boolean leaf, int row) {
					editingIcon = dir ? fst.iconManager.getFolderIcon() :
										fst.iconManager.fileIcon;
					if (editingIcon != null) {
						offset = renderer.getIconTextGap() +
								editingIcon.getIconWidth();
					}
					else {
						offset = renderer.getIconTextGap();
					}
				}

			};

		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row) {
			FileSystemTreeNode fstn = (FileSystemTreeNode)value;
			value = fstn.getFile().getName();
			return delegate.getTreeCellEditorComponent(tree, value, selected,
													expanded, leaf, row);
		}

		@Override
		public void addCellEditorListener(CellEditorListener l) {
			delegate.addCellEditorListener(l);
		}

		@Override
		public void cancelCellEditing() {
			delegate.cancelCellEditing();
		}

		private boolean fileAlreadyExists() {
			File temp = new File(root, delegate.getCellEditorValue().toString());
			if (temp.exists()) {
				UIManager.getLookAndFeel().provideErrorFeedback(tree);
				return true;
			}
			return false;
		}

		@Override
		public Object getCellEditorValue() {
			return new File(root, delegate.getCellEditorValue().toString());
		}

		@Override
		public boolean isCellEditable(EventObject e) {
			return delegate.isCellEditable(e);
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l) {
			delegate.removeCellEditorListener(l);
		}

		@Override
		public boolean shouldSelectCell(EventObject e) {
			return delegate.shouldSelectCell(e);
		}

		@Override
		public boolean stopCellEditing() {
			if (fileAlreadyExists()) {
				return false;
			}
			return delegate.stopCellEditing();
		}

	}


	/**
	 * A base class for actions that create files or folders.
	 */
	private abstract static class AbstractNewFileOrFolderAction
			extends AbstractTreeAction implements CellEditorListener {

		protected TreeCellEditor editor;

		protected  AbstractNewFileOrFolderAction(FileSystemTree tree,
				String keyRoot) {
			super(tree);
			putValue(NAME, getString(keyRoot));
			putValue(MNEMONIC_KEY,
                    (int)getString(keyRoot + "Mnemonic").charAt(0));
		}

		@Override
		public final void actionPerformed(ActionEvent e) {

			TreePath path = tree.getSelectionPath();

			// If we have a file (should be a directory) selected.
			if (path!=null) {

				FileSystemTreeNode parentNode = (FileSystemTreeNode)path.
							getLastPathComponent();
				File root = parentNode.getFile();

				if (root.isDirectory()) { // Should always be true
					handleNewFileOrFolder(path, parentNode);
				}

				// Should never happen.
				else {
					UIManager.getLookAndFeel().provideErrorFeedback(tree);
				}

			}

			// Nothing selected, but the hidden "root" is a directory.
			else if (tree.getRoot()!=null) {
				FileSystemTreeNode rootNode =
					(FileSystemTreeNode)tree.getModel().getRoot();
				handleNewFileOrFolder(path, rootNode); // path is null here
			}

		}

		protected abstract boolean createFileOrFolderImpl(File file);

		protected abstract FileSystemTreeNode createTreeNode(File root);

		@Override
		public final void editingCanceled(ChangeEvent e) {

			// Removes the node that was being edited.
			TreePath path = tree.getSelectionPath();
			FileSystemTreeNode node = (FileSystemTreeNode)path.
												getLastPathComponent();
			FileSystemTreeNode parent = null;
			if (node!=null) { // Should always be true
				parent = (FileSystemTreeNode)node.getParent();
				tree.refreshChildren(parent);
				((DefaultTreeModel)tree.getModel()).reload(parent);
			}

			editor.removeCellEditorListener(this);
			editor = null;
			tree.setCellEditor(null);
			tree.setEditable(false);

			// Select the parent that was going to be added to.
			// Use parent's file, not node.getFile().getParentFile(),
			// as node.getFile() may be our DUMMY_FILE.
			if (parent!=null) {
				tree.setSelectedFile(parent.getFile());
			}

		}

		@Override
		public final void editingStopped(ChangeEvent e) {

			File file = (File)editor.getCellEditorValue();
			FileSystemTreeNode node = (FileSystemTreeNode)tree.
									getSelectionPath().getLastPathComponent();

			if (node!=null) { // Should always be true
				node.setUserObject(file);
				//System.out.println(file.getAbsolutePath());
				boolean res = createFileOrFolderImpl(file);
				if (!res) {
					UIManager.getLookAndFeel().provideErrorFeedback(tree);
				}
				tree.iconManager.removeIconFor(file);
				FileSystemTreeNode parent = (FileSystemTreeNode)node.getParent();
				tree.refreshChildren(parent);
				((DefaultTreeModel)tree.getModel()).reload(parent);
				// After sorting alphabetically, the selected item may no
				// longer be visible.
				tree.scrollPathToVisible(tree.getSelectionPath());
			}

			editor.removeCellEditorListener(this);
			editor = null;
			tree.setCellEditor(null);
			tree.setEditable(false);
			tree.requestFocusInWindow();

		}

		private void handleNewFileOrFolder(TreePath path,
				FileSystemTreeNode parentNode) {

			File root = parentNode.getFile();
			FileSystemTreeNode newChild = createTreeNode(root);

			tree.refreshChildren(parentNode);
			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
			model.insertNodeInto(newChild, parentNode, 0);

			// JTree only works with TreePaths returned by it (!);
			// creating TreePaths by its constructor or by other means
			// results in NPE's when adding a child node to a newly-
			// added child node.
			// Don't know why, but the lines below are magic!  Can't
			// be simplified...
			tree.expandPath(path); // Expand to show new child
			int parentRow = tree.getRowForPath(path);
			int newRow = parentRow + 1;
			TreePath newChildPath = tree.getPathForRow(newRow);
			tree.setSelectionPath(newChildPath);
			tree.setEditable(true);
			editor = new FileTreeCellEditor(tree,
					(DefaultTreeCellRenderer)tree.getCellRenderer(),
					root, false);
			editor.addCellEditorListener(this);
			tree.setCellEditor(editor);
			tree.startEditingAtPath(newChildPath);

		}

	}


	/**
	 * Allows the user to create a new file.
	 */
	static class NewFileAction extends AbstractNewFileOrFolderAction {

		NewFileAction(FileSystemTree tree) {
			super(tree, "NewFile");
		}

		@Override
		protected boolean createFileOrFolderImpl(File file) {
			boolean res = false;
			try {
				res = file.createNewFile();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			return res;
		}

		@Override
		protected FileSystemTreeNode createTreeNode(File root) {
			String name = File.separatorChar=='/' ? "newFile" : "NewFile.txt";
			File f = new File(root, name);
			return new FileSystemTreeNode(f);
		}

	}


	/**
	 * Allows the user to create a new folder.
	 */
	static class NewFolderAction extends AbstractNewFileOrFolderAction {

		NewFolderAction(FileSystemTree tree) {
			super(tree, "NewFolder");
		}

		@Override
		protected boolean createFileOrFolderImpl(File file) {
			return file.mkdir();
		}

		@Override
		protected FileSystemTreeNode createTreeNode(File root) {
			String name = File.separatorChar == '/' ? "newDir" : "New Folder";
			File f = new File(root, name);
			// Pass in "true" for directory property, as this directory
			// doesn't exist yet.
			return tree.createTreeNodeForImpl(f, true);
		}

	}


	/**
	 * Pastes files into the currently selected directory.
	 */
	static class PasteAction extends AbstractTreeAction {

		PasteAction(FileSystemTree tree) {
			super(tree);
			putValue(NAME, getString("Paste"));
			putValue(MNEMONIC_KEY,
				(int)getString("PasteMnemonic").charAt(0));
			int mod = tree.getToolkit().getMenuShortcutKeyMaskEx();
			putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_V, mod));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			final TreePath path = tree.getSelectionPath();
			boolean copying = false;

			if (path!=null) { // Should always be true.

				final FileSystemTreeNode node = (FileSystemTreeNode)path.
							getLastPathComponent();
				File destDir = node.getFile();

				if (destDir.isDirectory()) { // Should always be true

					Clipboard clip = tree.getToolkit().getSystemClipboard();
					Transferable contents = clip.getContents(null);
					DataFlavor accepted = DataFlavor.javaFileListFlavor;

					try {

						@SuppressWarnings("unchecked")
						List<File> files = (List<File>)contents.
								getTransferData(accepted);
						Window parent = SwingUtilities.getWindowAncestor(tree);
						FilePasteCallback callback =
								new DefaultFilePasteCallback(parent) {
							@Override
							public void pasteOperationCompleted(int pasteCount){
								super.pasteOperationCompleted(pasteCount);
								tree.refreshChildren(node);
								((DefaultTreeModel)tree.getModel()).
													reload(node);
								tree.expandPath(path);
							}
						};

						if (files!=null && !files.isEmpty()) {
							FilePasteThread.paste(parent,
										files, destDir, callback);
							copying = true;
						}

					} catch (UnsupportedFlavorException | IOException ex) {
						ex.printStackTrace();
					}

				}

			}

			if (!copying) {
				UIManager.getLookAndFeel().provideErrorFeedback(tree);
			}

		}

	}


	/**
	 * Action that "refreshes" the currently selected directory in the
	 * directory tree.
	 */
	static class RefreshAction extends AbstractTreeAction {

		RefreshAction(FileSystemTree tree) {
			super(tree);
			putValue(NAME, getString("Refresh"));
			putValue(MNEMONIC_KEY,
                    (int)getString("RefreshMnemonic").charAt(0));
			putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			TreePath path = tree.getSelectionPath();

			// If they've selected a tree node (should be a directory to work)
			if (path!=null) {

				FileSystemTreeNode node = (FileSystemTreeNode)path.
							getLastPathComponent();
				File file = node.getFile();
				DefaultTreeModel model = (DefaultTreeModel)tree.getModel();

				// The file should be a directory.  The only catch is
				// that maybe the directory was deleted since the last
				// time we cached.  NOTE:  We MUST check whether the file
				// exists BEFORE we check whether it's a directory because
				// isDirectory() can return true for a directory that has
				// been removed after the File object was created, despite
				// what the Javadoc says.

				// If the directory no longer exists, refresh the structure
				// from the parent directory down, in case other stuff has
				// changed too.
				if (!file.exists()) {
					int count = path.getPathCount();
					if (count>1) {
						node = (FileSystemTreeNode)path.
										getPathComponent(count-2);
						tree.refreshChildren(node);
						model.reload(node);
					}
					else { // It's the one and only file in the path...
						tree.removeSelectionPath(path);
						model.reload(node);
					}
				}

				// If the directory still exists...
				else if (file.isDirectory()) {
					tree.refreshChildren(node);
					model.reload(node); // Causes repaint properly.
				}

				// Otherwise, they've removed what was a directory when we
				// cached data, and created a regular file in its place...
				// Again, we'll refresh the parent directory's cached data.
				else {
					int count = path.getPathCount();
					if (count>1) {
						node = (FileSystemTreeNode)path.
										getPathComponent(count-2);
						tree.refreshChildren(node);
						model.reload(node);
					}
					else { // It's the one and only file in the path...
						tree.removeSelectionPath(path);
						model.reload(node);
					}
				}

			}

			// No tree node selected, but we're "in" a directory
			else if (tree.getRoot()!=null) {
				tree.refreshChildren(null);
			}

		}

	}


}
