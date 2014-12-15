/*
 * 04/12/2013
 *
 * FilePasteThread - Copies one or more files in a separate thread, and allows
 * for progress notifications on the EDT.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import org.fife.ui.EscapableDialog;
import org.fife.ui.GUIWorkerThread;
import org.fife.ui.RListSelectionModel;
import org.fife.ui.RScrollPane;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;


/**
 * A thread that pastes files into a new location.  It allows for the actual
 * file IO to be performed asynchronously, and for progress to be monitored
 * on the EDT.  Applications register an instance of a
 * {@link FilePasteCallback} to listen as the copy/paste operation executes;
 * they can display a <code>ProgressMonitor</code> or use any other means of
 * allowing the user to watch the paste operation continue while keeping a
 * responsive UI.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FilePasteThread extends GUIWorkerThread {

	private Window parent;
	private List<File> files;
	private File destDir;
	private FilePasteCallback callback;
	private UserDecisions decisions;
	private int pasteCount;
	private int total;

	private String errorDialogTitle;
	private String confirmationDialogTitle;

	private static final String MSG = "org.fife.ui.rtextfilechooser.FilePaste";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	public FilePasteThread(Frame parent, List<File> files, File destDir,
			FilePasteCallback callback) {
		this.parent = parent;
		init(files, destDir, callback);
	}


	public FilePasteThread(Dialog parent, List<File> files, File destDir,
			FilePasteCallback callback) {
		this.parent = parent;
		init(files, destDir, callback);
	}


	@Override
	public Object construct() {

		if (files == null || files.size() == 0) {
			return null;
		}

		pasteCount = 0;
		total = 0;
		List<FileTreeNode> toCopy = new ArrayList<FileTreeNode>();

		for (File file : files) {
			if (file.isDirectory()) {
				toCopy.add(getFilesRecursive(file));
			}
			else {
				toCopy.add(new FileTreeNode(file, false));
			}
			total++; // For either the file or the directory.
		}

		doCopy(toCopy, destDir);
		return null;

	}


	private void copyFileImpl(File source, File dest) {
		try {
			if (!source.equals(dest)) {
				Utilities.copyFile(source, dest);
			}
		} catch (IOException ioe) {
			//ioe.printStackTrace();
			String msg = getString("Error.CopyingFiles", source.getName(),
					ioe.getMessage());
			String title = getErrorDialogTitle();
			int rc = JOptionPane.showConfirmDialog(parent, msg, title,
					JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if (rc!=JOptionPane.YES_OPTION) {
				decisions.cancelEverything = true;
			}
		}
	}


	private static final File createUniqueDestFile(File dir, String name) {

		String orig = name;
		int lastDot = name.lastIndexOf('.');
		if (lastDot>-1) {
			name = name.substring(0, lastDot) + ".copy" +
					name.substring(lastDot);
		}
		else {
			name = name + ".copy";
		}
		File test = new File(dir, name);
		if (!test.exists()) {
			return test;
		}

		int count = 1;
		while (true) {
			if (lastDot>-1) {
				name = orig.substring(0, lastDot) + ".copy." + count +
						orig.substring(lastDot);
			}
			else {
				name = name + ".copy." + count;
			}
			test = new File(dir, name);
			if (!test.exists()) {
				return test;
			}
			count++;
		}

	}


	private void doCopy(List<FileTreeNode> copyMap, File toDir) {

		if (copyMap==null || copyMap.isEmpty()) {
			return;
		}

		for (FileTreeNode node : copyMap) {

			if (decisions.cancelEverything) {
				return;
			}

			File file = node.node;
			List<FileTreeNode> children = node.children;

			if (children==null) { // node.node is a file
				File dest = new File(toDir, file.getName());
				doCopyFile(file, dest);
			}

			else { // A directory

				if (callback!=null) {
					if (callback.filePasteUpdate(++pasteCount, total, file)) {
						decisions.cancelEverything = true;
						return;
					}
				}

				File newDir = new File(toDir, file.getName());

				if (newDir.isFile()) {
					String msg = getString("Error.DirectoryExistsAsFile",
							newDir.getAbsolutePath());
					CannotCopyDialog ccd = null;
					if (parent instanceof Dialog) {
						ccd = new CannotCopyDialog((Dialog)parent, msg);
					}
					else {
						ccd = new CannotCopyDialog((Frame)parent, msg);
					}
					ccd.setVisible(true);
					if (!ccd.continueCopying) {
						decisions.cancelEverything = true;
					}
					continue;
				}

				else if (newDir.isDirectory()) {
					if (decisions.dirNameCollision==UserDecisions.PROMPT) {
						DirExistsDialog ded = null;
						if (parent instanceof Dialog) {
							ded = new DirExistsDialog((Dialog)parent, newDir);
						}
						else {
							ded = new DirExistsDialog((Frame)parent, newDir);
						}
						ded.setVisible(true);
						boolean doCopy = ded.getCopyFilesIntoDirectory();
						boolean rememberDecision = ded.getRememberDecision();
						if (rememberDecision) {
							decisions.dirNameCollision = doCopy ?
									UserDecisions.OVERWRITE : UserDecisions.SKIP;
						}
						if (!doCopy) {
							total -= getSizeRecursive(children);
							if (callback!=null) { // New "total"
								if (callback.filePasteUpdate(pasteCount, total, null)) {
									decisions.cancelEverything = true;
									return;
								}
							}
							continue;
						}
					}
					else if (decisions.dirNameCollision==UserDecisions.SKIP) {
						total -= getSizeRecursive(children);
						if (callback!=null) { // New "total"
							if (callback.filePasteUpdate(pasteCount, total, null)) {
								decisions.cancelEverything = true;
								return;
							}
						}
						continue;
					}
				}

				else {
					if (!makeDir(newDir, file)) {
						return; // Don't copy the files in this directory
					}
				}

				doCopy(children, newDir);

			}

		}

	}


	private void doCopyFile(File file, File dest) {

		// If the destination file already exists...
		if (dest.isFile()) {
			if (callback!=null) {
				if (decisions.nameCollision==UserDecisions.SKIP) {
					total--;
				}
				else {
					if (callback.filePasteUpdate(++pasteCount, total, file)) {
						decisions.cancelEverything = true;
						return;
					}
				}
			}
			if (decisions.nameCollision == UserDecisions.PROMPT) {
				NameCollisionResolver r = new NameCollisionResolver(file, dest);
				try {
					SwingUtilities.invokeAndWait(r);
				} catch (InvocationTargetException ite) {
					ite.printStackTrace();
					return;
				} catch (InterruptedException ie) {
					ie.printStackTrace();
					return;
				}
				int result = r.result;
				boolean doForAll = r.doForAll;
				switch (result) {
					case 0: // Overwrite existing file
						copyFileImpl(file, dest);
						if (doForAll) {
							decisions.nameCollision = UserDecisions.OVERWRITE;
						}
						break;
					case 1: // Skip this file
						if (doForAll) {
							decisions.nameCollision = UserDecisions.SKIP;
						}
						break;
					case 2: // Copy with new file name
						File toDir = dest.getParentFile();
						dest = createUniqueDestFile(toDir, file.getName());
						copyFileImpl(file, dest);
						if (doForAll) {
							decisions.nameCollision = UserDecisions.RENAME;
						}
						break;
					case 3: // Cancel entire operation
						decisions.cancelEverything = true;
						return;
				}
			}
			// UserDecisions.SKIP => Just skip it!
			else if (decisions.nameCollision==UserDecisions.OVERWRITE) {
				copyFileImpl(file, dest);
			}
			else if (decisions.nameCollision == UserDecisions.RENAME) {
				File toDir = dest.getParentFile();
				dest = createUniqueDestFile(toDir, file.getName());
				copyFileImpl(file, dest);
			}
		}

		// If a directory exists with that name...
		else if (dest.isDirectory()) {
			if (callback!=null) {
				if (callback.filePasteUpdate(++pasteCount, total, file)) {
					decisions.cancelEverything = true;
					return;
				}
			}
			String msg = getString("Error.FileExistsAsDirectory",
									dest.getAbsolutePath());
			CannotCopyDialog ccd = null;
			if (parent instanceof Dialog) {
				ccd = new CannotCopyDialog((Dialog)parent, msg);
			}
			else {
				ccd = new CannotCopyDialog((Frame)parent, msg);
			}
			ccd.setVisible(true);
			if (!ccd.continueCopying) {
				decisions.cancelEverything = true;
			}
		}

		// The common case, nothing to overwrite - create the file!
		else {
			if (callback!=null) {
				if (callback.filePasteUpdate(++pasteCount, total, file)) {
					decisions.cancelEverything = true;
				}
			}
			copyFileImpl(file, dest);
		}

	}


	@Override
	public void finished() {
		if (callback!=null) {
			callback.pasteOperationCompleted(pasteCount);
		}
	}


	private String getConfirmationDialogTitle() {
		if (confirmationDialogTitle!=null) {
			return confirmationDialogTitle;
		}
		return msg.getString("ConfirmationDialog.Title");
	}


	private String getErrorDialogTitle() {
		if (errorDialogTitle!=null) {
			return errorDialogTitle;
		}
		return msg.getString("ErrorDialog.Title");
	}


	/**
	 * Similar to <code>File.listFiles()</code>, but recursively lists all
	 * files.
	 *
	 * @param dir The directory of which to retrieve all children recursively.
	 * @return The tree hierarchy of files under <code>dir</code>.
	 */
	private FileTreeNode getFilesRecursive(File dir) {

		FileTreeNode node = new FileTreeNode(dir, true);

		File[] children = dir.listFiles();
		int count = children==null ? 0 : children.length;
		for (int i=0; i<count; i++) {
			File child = children[i];
			if (child.isDirectory()) {
				node.addChild(getFilesRecursive(child));
			}
			total++; // For either the file or the directory.
		}

		return node;

	}


	/**
	 * Returns the size of a map of maps, recursively.
	 *
	 * @param map The map of maps.
	 * @return The size of the map of maps.
	 */
	private static final int getSizeRecursive(List<FileTreeNode> map) {

		int count = 0;

		for (FileTreeNode node : map) {
			count++; // Increment for this file or directory
			if (node.children!=null) { // Directory with children
				count += getSizeRecursive(node.children);
			}
		}

		return count;

	}


	private static final String getString(String key) {
		return msg.getString(key);
	}


	private static final String getString(String key, String... args) {
		String str = msg.getString(key);
		return MessageFormat.format(str, (Object[])args);
	}


	private void init(List<File> files, File destDir,
			FilePasteCallback callback) {
		this.files = files;
		this.destDir = destDir;
		this.callback = callback;
		decisions = new UserDecisions();
	}


	/**
	 * Tries to create a directory, and displays a message asking whether to
	 * continue if the creation fails.
	 *
	 * @param dir The directory to create.
	 * @param contentsToCopyInDir A folder containing contents that will get
	 *        copied into the new directory <code>dir</code>.
	 * @return <code>true</code> if the operation was successful, or failed
	 *         but the user selected to continue anyway; <code>false</code> if
	 *         the operation failed, and the user chose to cancel any further
	 *         copying.
	 */
	private boolean makeDir(File dir, File contentsToCopyInDir) {
		int rc = JOptionPane.YES_OPTION;
		if (!dir.mkdir()) {
			String msg = getString("Error.CreatingDirectory",
					dir.getAbsolutePath(),
					contentsToCopyInDir.getAbsolutePath());
			String title = getConfirmationDialogTitle();
			rc = JOptionPane.showConfirmDialog(null, msg, title,
					JOptionPane.YES_NO_OPTION);
		}
		return rc==JOptionPane.YES_OPTION;
	}


	public static void paste(Window parent, List<File> files, File destDir,
			FilePasteCallback callback) {
		FilePasteThread thread = null;
		if (parent instanceof Frame) {
			thread = new FilePasteThread((Frame)parent, files, destDir,
					callback);
		}
		else {
			thread = new FilePasteThread((Dialog)parent, files, destDir,
					callback);
		}
		thread.start();
	}


	public void setConfirmationDialogTitle(String title) {
		this.confirmationDialogTitle = title;
	}


	public void setErrorDialogTitle(String title) {
		this.errorDialogTitle = title;
	}


	private class CannotCopyDialog extends EscapableDialog
			implements ActionListener {

		private JButton okButton;
		private boolean continueCopying;

		public CannotCopyDialog(Dialog parent, String msg) {
			super(parent);
			init(msg);
		}

		public CannotCopyDialog(Frame parent, String msg) {
			super(parent);
			init(msg);
		}

		private void init(String msg) {

			JPanel cp = new ResizableFrameContentPane(new BorderLayout(0, 15));
			cp.setBorder(UIUtil.getEmpty5Border());

			SelectableLabel desc = new SelectableLabel(msg);
			cp.add(desc, BorderLayout.NORTH);

			okButton = UIUtil.newButton(FilePasteThread.msg,
					"Dialog.CannotCopy.ContinueCopyingFiles");
			okButton.addActionListener(this);
			JButton cancelButton = UIUtil.newButton(FilePasteThread.msg,
					"Dialog.CannotCopy.CancelRemainingCopies");
			cancelButton.addActionListener(this);
			Container footer = UIUtil.createButtonFooter(okButton,cancelButton);
			cp.add(footer, BorderLayout.SOUTH);

			setContentPane(cp);
			getRootPane().setDefaultButton(okButton);
			setModal(true);
			pack();
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setLocationRelativeTo(getParent());

		}

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (okButton==source) {
				continueCopying = true;
			}
			escapePressed();
		}

	}


	private class DirExistsDialog extends JDialog implements ActionListener {

		private JCheckBox rememberCB;
		private JButton yes;
		private JButton no;
		private boolean copyFilesIntoDirectory;

		public DirExistsDialog(Dialog parent, File dir) {
			super(parent);
			init(dir);
		}

		public DirExistsDialog(Frame parent, File dir) {
			super(parent);
			init(dir);
		}

		public boolean getCopyFilesIntoDirectory() {
			return copyFilesIntoDirectory;
		}

		private boolean getRememberDecision() {
			return rememberCB.isSelected();
		}

		private void init(File dir) {

			JPanel cp = new ResizableFrameContentPane(new BorderLayout(0, 20));
			cp.setBorder(javax.swing.BorderFactory.createCompoundBorder(
					UIUtil.getEmpty5Border(), UIUtil.getEmpty5Border()));

			JPanel topPanel = new JPanel(new BorderLayout(20, 0));
			cp.add(topPanel, BorderLayout.NORTH);
			Icon icon = UIManager.getIcon("OptionPane.questionIcon");
			if (icon!=null) {
				JLabel iconLabel = new JLabel(icon);
				iconLabel.setVerticalAlignment(SwingConstants.TOP);
				topPanel.add(iconLabel, BorderLayout.LINE_START);
			}

			Box topText = Box.createVerticalBox();
			String message = getString("Dialog.DirExists.Text",
					dir.getAbsolutePath());
			if (message.startsWith("<html>")) {
				topText.add(new SelectableLabel(message));
			}
			else {
				int newline = 0;
				int lastNewline = 0;
				while ((newline=message.indexOf('\n', lastNewline))>-1) {
					String temp = message.substring(lastNewline, newline);
					if (temp.length()==0) { temp  = " "; }
					topText.add(new JLabel(temp));
					lastNewline = newline + 1;
				}
				if (lastNewline<message.length()-1) {
					topText.add(new JLabel(message.substring(lastNewline)));
				}
			}
			topText.add(Box.createVerticalGlue());
			JPanel temp = new JPanel(new BorderLayout());
			temp.add(topText);
			topPanel.add(temp);

			JPanel bottom = new JPanel(new BorderLayout());
			rememberCB = UIUtil.newCheckBox(msg,
					"Dialog.DirExists.RememberMyDecision");
			temp = new JPanel(new BorderLayout());
			temp.add(rememberCB, BorderLayout.LINE_START);
			bottom.add(temp, BorderLayout.NORTH);
			yes = UIUtil.newButton(msg, "Dialog.DirExists.Yes");
			yes.addActionListener(this);
			no = UIUtil.newButton(msg, "Dialog.DirExists.No");
			no.addActionListener(this);
			Container buttonPanel = UIUtil.createButtonFooter(yes, no, 0);
			buttonPanel.add(rememberCB, BorderLayout.LINE_START);
			bottom.add(buttonPanel, BorderLayout.SOUTH);
			cp.add(bottom, BorderLayout.SOUTH);

			setTitle(getConfirmationDialogTitle());
			setContentPane(cp);
			getRootPane().setDefaultButton(yes);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setModal(true);
			pack();
			Dimension size = getSize();
			if (size.width<400) {
				size.width = 400;
				setSize(size);
			}
			setLocationRelativeTo(getParent());

		}

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (yes==source) {
				copyFilesIntoDirectory = true;
			}
			else if (no==source) {
				copyFilesIntoDirectory = false;
			}
			setVisible(false);
		}

	}


	private static class FileInfoPanel extends JPanel {

		public FileInfoPanel(String title, File file) {

			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(title),
					BorderFactory.createEmptyBorder(0, 5, 0, 5)));
			setLayout(new BorderLayout(10, 0));
			Icon fileIcon = getIcon(file);
			add(new JLabel(fileIcon), BorderLayout.LINE_START);

			String text = "<html><b>" + file.getName() + "</b><br>" +
					file.getAbsolutePath() + "<br>" +
					msg.getString("FileInfo.Size") + " " +
			        Utilities.getFileSizeStringFor(file) + "<br>" +
					msg.getString("FileInfo.DateModified") + " " +
			        Utilities.getLastModifiedString(file.lastModified());
			SelectableLabel label = new SelectableLabel(text);
			add(label);

		}

		private static final Icon getIcon(File file) {

			Icon icon = null;

			try {
				Class<?> sfClazz = Class.forName("sun.awt.shell.ShellFolder");
				Method m = sfClazz.getMethod("getShellFolder",
						new Class[] { File.class });
				Object shellFolder = m.invoke(null, new Object[] { file });
				if (shellFolder!=null) {
					m = sfClazz.getMethod("getIcon",
							new Class[] { boolean.class });
					Image image = (Image)m.invoke(shellFolder,
							new Object[] { Boolean.TRUE });
					if (image!=null) {
						icon = new ImageIcon(image);
					}
				}
			} catch (RuntimeException re) {
				throw re; // FindBugs
			} catch (Exception e) {
				// Swallow; non-Sun JVM, not Windows, or the internal API has
				// changed.
			}

			if (icon==null) {
				icon = FileSystemView.getFileSystemView().getSystemIcon(file);
			}

			return icon;

		}

	}


	private class NameCollisionDialog extends JDialog implements ActionListener{

		private JList list;
		private JCheckBox doForAllCB;
		private JButton okButton;
		private int result;

		public NameCollisionDialog(Dialog parent, File source, File dest) {
			super(parent);
			init(source, dest);
		}

		public NameCollisionDialog(Frame parent, File source, File dest) {
			super(parent);
			init(source, dest);
		}

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (okButton==source) {
				result = list.getSelectedIndex();
				setVisible(false);
			}

		}

		public boolean getDoForAll() {
			return doForAllCB.isSelected();
		}

		public int getResult() {
			return result;
		}

		private Container createTopPanel(File source, File dest) {

			Box topPanel = Box.createVerticalBox();

			String temp = getString("Dialog.NameCollision.FileExists",
									source.getAbsolutePath());
			JLabel label = new JLabel(temp);
			JPanel temp2 = new JPanel(new BorderLayout());
			temp2.add(label, BorderLayout.LINE_START);
			topPanel.add(temp2);
			topPanel.add(Box.createVerticalStrut(5));

			String title = getString("Dialog.NameCollision.CurrentFile");
			topPanel.add(new FileInfoPanel(title, dest));
			topPanel.add(Box.createVerticalStrut(5));

			title = getString("Dialog.NameCollision.FileBeingCopied");
			topPanel.add(new FileInfoPanel(title, source));

			topPanel.add(Box.createVerticalGlue());

			return topPanel;

		}

		private void init(File source, File dest) {

			setTitle(getString("Dialog.NameCollision.Title"));

			JPanel cp = new ResizableFrameContentPane(new BorderLayout(0, 15));
			cp.setBorder(UIUtil.getEmpty5Border());

			cp.add(createTopPanel(source, dest), BorderLayout.NORTH);

			Box middle = Box.createVerticalBox();
			JLabel label = new JLabel(getString("Dialog.NameCollision.WhatToDo"));
			JPanel temp2 = new JPanel(new BorderLayout());
			temp2.add(label, BorderLayout.LINE_START);
			middle.add(temp2);
			String[] choices = {
				getString("Dialog.NameCollision.Overwrite"),
				getString("Dialog.NameCollision.Skip"),
				getString("Dialog.NameCollision.CopyWithNewName"),
				getString("Dialog.NameCollision.CancelRemainingFiles"),
			};
			list = new JList(choices);
			list.setSelectionModel(new RListSelectionModel());
			list.setSelectedIndex(0);
			RScrollPane sp = new RScrollPane(list);
			middle.add(sp);
			middle.add(Box.createVerticalStrut(5));
			doForAllCB = UIUtil.newCheckBox(msg,
					"Dialog.NameCollision.RememberForFutureFiles");
			JPanel temp = new JPanel(new BorderLayout());
			temp.add(doForAllCB, BorderLayout.LINE_START);
			middle.add(temp);
			middle.add(Box.createVerticalGlue());
			temp = new JPanel(new BorderLayout());
			temp.add(middle, BorderLayout.NORTH);
			cp.add(temp);

			okButton = UIUtil.newButton(msg, "Button.OK");
			okButton.addActionListener(this);
			Container footer = UIUtil.createButtonFooter(okButton);
			cp.add(footer, BorderLayout.SOUTH);

			setContentPane(cp);
			getRootPane().setDefaultButton(okButton);
			setModal(true);
			pack();
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setLocationRelativeTo(getParent());

			result = 3; // Cancel this and further pastes

		}

	}


	/**
	 * Prompts the user about a file name collision.  To be executed on the EDT.
	 */
	private class NameCollisionResolver implements Runnable {

		public int result;
		public boolean doForAll;

		private File file;
		private File dest;

		public NameCollisionResolver(File file, File dest) {
			this.file = file;
			this.dest = dest;
			result = 0;
			doForAll = false;
		}

		public void run() {
			NameCollisionDialog ncd = null;
			if (parent instanceof Frame) {
				ncd = new NameCollisionDialog((Frame)parent,
						file, dest);
			}
			else {
				ncd = new NameCollisionDialog((Dialog)parent,
						file, dest);
			}
			ncd.setVisible(true);
			result = ncd.getResult();
			doForAll = ncd.getDoForAll();
		}

	}


	static class UserDecisions {

		public static final int PROMPT = 0;
		public static final int OVERWRITE = 1;
		public static final int RENAME = 2;
		public static final int SKIP = 3;

		public int nameCollision;
		public int dirNameCollision;
		public boolean cancelEverything;

		public UserDecisions() {
			nameCollision = PROMPT;
			dirNameCollision = PROMPT;
		}

	}


	/**
	 * A simple test application for this class.
	 */
	public static void main(String[] args) throws Exception {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		List<File> files = new ArrayList<File>();
		files.add(new File("C:/temp/test.java"));
		files.add(new File("C:/temp/FilePasteTestInput"));

		File dest = new File("C:/temp/FilePasteTest");
		dest.mkdir();

		Dialog parent = null;
		FilePasteCallback callback = new DefaultFilePasteCallback(parent) {
			@Override
			public void pasteOperationCompleted(int pasteCount) {
				super.pasteOperationCompleted(pasteCount);
				System.exit(0);
			}
		};

		FilePasteThread.paste(parent, files, dest, callback);
		
	}


	/**
	 * A cached representation of a file, or a folder and its children.
	 * An instance of this class represents a file if <code>children</code>
	 * is <code>null</code>; if it is non-<code>null</code> (even just empty),
	 * it represents a folder.
	 */
	private static class FileTreeNode {

		private File node;
		private List<FileTreeNode> children;

		private FileTreeNode(File node, boolean isDir) {
			this.node = node;
			if (isDir) {
				children = new ArrayList<FileTreeNode>();
			}
		}

		private void addChild(FileTreeNode child) {
			if (children==null) {
				children = new ArrayList<FileTreeNode>();
			}
			children.add(child);
		}

	}


}