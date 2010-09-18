package org.fife.ui.rtextfilechooser;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import org.fife.ui.rtextfilechooser.FileSystemTree.FileSystemTreeNode;


/**
 * Actions used by <code>FileSystemTree</code>s.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FileSystemTreeActions {


	/**
	 * Cell editor used when the user adds a new file or folder to the tree.
	 */
	private static class FileTreeCellEditor implements TreeCellEditor {

		private FileSystemTree tree;
		private File root;
		private DefaultTreeCellEditor delegate;

		public FileTreeCellEditor(final FileSystemTree fst,
									DefaultTreeCellRenderer renderer,
									File root, final boolean dir) {

			this.tree = fst;
			this.root = root;

			delegate = new DefaultTreeCellEditor(tree, renderer) {

				protected TreeCellEditor createTreeCellEditor() {
					TreeCellEditor editor = super.createTreeCellEditor();
					if (editor instanceof DefaultCellEditor) { // Always true
						DefaultCellEditor dce = (DefaultCellEditor)editor;
						dce.getComponent().addFocusListener(new FocusAdapter() {
							public void focusLost(FocusEvent e) {
								if (!e.isTemporary()) {
									cancelCellEditing();
								}
							}
						});
					}
					return editor;
				}

				protected void determineOffset(JTree tree, Object value,
						boolean selected, boolean expanded,
						boolean leaf, int row) {
					editingIcon = dir ? fst.iconManager.getFolderIcon() :
										fst.iconManager.fileIcon;
					if(editingIcon != null) {
						offset = renderer.getIconTextGap() +
								editingIcon.getIconWidth();
					}
					else {
						offset = renderer.getIconTextGap();
					}
				}

			};

		}

		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row) {
			FileSystemTreeNode fstn = (FileSystemTreeNode)value;
			value = fstn.getFile().getName();
			return delegate.getTreeCellEditorComponent(tree, value, selected,
													expanded, leaf, row);
		}

		public void addCellEditorListener(CellEditorListener l) {
			delegate.addCellEditorListener(l);
		}

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

		public Object getCellEditorValue() {
			return new File(root, delegate.getCellEditorValue().toString());
		}

		public boolean isCellEditable(EventObject e) {
			return delegate.isCellEditable(e);
		}

		public void removeCellEditorListener(CellEditorListener l) {
			delegate.removeCellEditorListener(l);
		}

		public boolean shouldSelectCell(EventObject e) {
			return delegate.shouldSelectCell(e);
		}

		public boolean stopCellEditing() {
			if (fileAlreadyExists()) {
				return false;
			}
			return delegate.stopCellEditing();
		}

	}


	/**
	 * Allows the user to create a new file.
	 */
	static class NewFileAction extends AbstractAction
								implements CellEditorListener {

		private FileSystemTree tree;
		private TreeCellEditor editor;

		public NewFileAction(FileSystemTree tree, ResourceBundle bundle) {
			super(bundle.getString("NewFile"));
			putValue(Action.MNEMONIC_KEY,
				new Integer(bundle.getString("NewFileMnemonic").charAt(0)));
			this.tree = tree;
		}

		public void actionPerformed(ActionEvent e) {

			TreePath path = tree.getSelectionPath();

			if (path!=null) { // Should always be true.

				FileSystemTreeNode node = (FileSystemTreeNode)path.
							getLastPathComponent();
				File root = node.getFile();

				if (root.isDirectory()) { // Should always be true

					String name = File.separatorChar=='/' ? "newFile" :
													"NewFile.txt";
					File f = new File(root, name);
					FileSystemTreeNode newChild = new FileSystemTreeNode(f);

					tree.expandPath(path);
					node.add(newChild);
					DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
					model.reload(node);
					tree.setSelectionPath(new TreePath(newChild.getPath()));
					tree.setEditable(true);
					editor = new FileTreeCellEditor(tree,
							(DefaultTreeCellRenderer)tree.getCellRenderer(),
							root, false);
					editor.addCellEditorListener(this);
					tree.setCellEditor(editor);
					tree.startEditingAtPath(tree.getSelectionPath());

				}
				else {
					UIManager.getLookAndFeel().provideErrorFeedback(tree);
				}

			}

		}

		public void editingCanceled(ChangeEvent e) {

			// Removes the node that was being edited.
			FileSystemTreeNode node = (FileSystemTreeNode)tree.
								getSelectionPath().getLastPathComponent();
			if (node!=null) { // Should always be true
				FileSystemTreeNode parent = (FileSystemTreeNode)node.getParent();
				tree.refreshChildren(parent);
				((DefaultTreeModel)tree.getModel()).reload(parent);
			}

			editor.removeCellEditorListener(this);
			editor = null;
			tree.setCellEditor(null);
			tree.setEditable(false);

		}

		public void editingStopped(ChangeEvent e) {

			File file = (File)editor.getCellEditorValue();
			FileSystemTreeNode node = (FileSystemTreeNode)tree.
									getSelectionPath().getLastPathComponent();

			if (node!=null) { // Should always be true
				node.setUserObject(file);
				//System.out.println(file.getAbsolutePath());
				boolean res = false;
				try {
					res = file.createNewFile();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
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

		}

	}


	/**
	 * Allows the user to create a new folder.
	 */
	static class NewFolderAction extends AbstractAction
								implements CellEditorListener {

		private FileSystemTree tree;
		private TreeCellEditor editor;

		public NewFolderAction(FileSystemTree tree, ResourceBundle bundle) {
			super(bundle.getString("NewFolder"));
			putValue(Action.MNEMONIC_KEY, new Integer(bundle.getString(
					"NewFolderMnemonic").charAt(0)));
			this.tree = tree;
		}

		public void actionPerformed(ActionEvent e) {

			TreePath path = tree.getSelectionPath();

			if (path != null) { // Should always be true.

				FileSystemTreeNode node = (FileSystemTreeNode) path
						.getLastPathComponent();
				File root = node.getFile();

				if (root.isDirectory()) { // Should always be true

					String name = File.separatorChar == '/' ? "newDir"
							: "New Folder";
					File f = new File(root, name);
					FileSystemTreeNode newChild = new FileSystemTreeNode(f);

					tree.expandPath(path);
					node.add(newChild);
					DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
					model.reload(node);
					tree.setSelectionPath(new TreePath(newChild.getPath()));
					tree.setEditable(true);
					editor = new FileTreeCellEditor(tree,
							(DefaultTreeCellRenderer)tree.getCellRenderer(),
							root, true);
					editor.addCellEditorListener(this);
					tree.setCellEditor(editor);
					tree.startEditingAtPath(tree.getSelectionPath());

				} else {
					UIManager.getLookAndFeel().provideErrorFeedback(tree);
				}

			}

		}

		public void editingCanceled(ChangeEvent e) {

			// Removes the node that was being edited.
			FileSystemTreeNode node = (FileSystemTreeNode)tree.
									getSelectionPath().getLastPathComponent();
			if (node != null) { // Should always be true
				FileSystemTreeNode parent = (FileSystemTreeNode) node
						.getParent();
				tree.refreshChildren(parent);
				((DefaultTreeModel)tree.getModel()).reload(parent);
			}

			editor.removeCellEditorListener(this);
			editor = null;
			tree.setCellEditor(null);
			tree.setEditable(false);

		}

		public void editingStopped(ChangeEvent e) {

			File file = (File) editor.getCellEditorValue();
			FileSystemTreeNode node = (FileSystemTreeNode)tree.
									getSelectionPath().getLastPathComponent();
			if (node != null) { // Should always be true
				node.setUserObject(file);
				// System.out.println(file.getAbsolutePath());
				if (!file.mkdir()) {
					UIManager.getLookAndFeel().provideErrorFeedback(tree);
				}
				tree.iconManager.removeIconFor(file);
				FileSystemTreeNode parent = (FileSystemTreeNode) node
						.getParent();
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

		}

	}



	/**
	 * Action that "refreshes" the currently selected directory in the
	 * directory tree.
	 */
	static class RefreshAction extends AbstractAction {

		private FileSystemTree tree;

		public RefreshAction(FileSystemTree tree, ResourceBundle bundle) {
			super(bundle.getString("Refresh"));
			putValue(Action.MNEMONIC_KEY,
				new Integer(bundle.getString("RefreshMnemonic").charAt(0)));
			this.tree = tree;
		}

		public void actionPerformed(ActionEvent e) {

			TreePath path = tree.getSelectionPath();

			if (path!=null) { // Should always be true.

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

		}

	}


}