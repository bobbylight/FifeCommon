/*
 * 06/24/2005
 *
 * FileSystemIconManager.java - Manages icons for RTextFileChooser and
 * FileSystemTree.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;


class FileChooserIconManager {

	protected Icon folderIcon;
	protected Icon hardDriveIcon;
	protected Icon floppyDriveIcon;
	protected Icon computerIcon;
	protected Icon fileIcon;

	private static final FileSystemView fileSystemView = FileSystemView.
												getFileSystemView();
	private Map<File, Icon> iconCache;


	public FileChooserIconManager() {
		iconCache = new HashMap<File, Icon>(50);
		createDefaultIcons();
	}


	/**
	 * Clears the icon cache.  Use this if you want to save memory between
	 * displays of the file tree.
	 */
	public void clearIconCache() {
		iconCache.clear();
	}


	/**
	 * Creates the icons used by this file chooser.  This method can be
	 * called when the user changes the Look and Feel, and icons used in this
	 * file chooser will change to reflect the new Look and Feel.
	 */
	protected void createDefaultIcons() {

		// The folder (directory) icon is treated specially, as there may
		// be other parts of code that want to create the system "directory"
		// icon, so we make that method available.
		folderIcon = createFolderIcon();

		// Get file chooser icons for hard drives, floppies and files.
		// We only need the floppy drive icon if the getSystemIcon above
		// returned null.
		hardDriveIcon    = UIManager.getIcon("FileView.hardDriveIcon");
		floppyDriveIcon  = UIManager.getIcon("FileView.floppyDriveIcon");
		fileIcon		  = UIManager.getIcon("FileView.fileIcon");

		// If UIManager returned null for any of the above, just get ones
		// we drew ourselves.
		String path = "org/fife/ui/rtextfilechooser/images/";
		ClassLoader cl = this.getClass().getClassLoader();
		if (hardDriveIcon==null)
			hardDriveIcon = loadIcon(cl, path + "harddrive.gif");
		if (floppyDriveIcon==null)
			floppyDriveIcon = loadIcon(cl, path + "floppydrive.gif");
		if (fileIcon==null)
			fileIcon = loadIcon(cl, path + "file.gif");

	}


	/**
	 * Creates and returns the "folder" icon used for regular directories
	 * in the current look and feel.
	 *
	 * @return The icon.
	 */
	public static Icon createFolderIcon() {

		Icon folderIcon = null;

		// Now, on Windows, FileSystemView.getSystemIcon throws an exception
		// if you pass it a file that doesn't exist.  So, we need to have
		// a file that we KNOW is a directory (and not hidden, etc.) to use
		// when we check for the system icon.  Since there is no
		// File.createTempDirectory method, we must create a temporary file,
		// delete it, then recreate it as a directory...
		try {
			File temp = File.createTempFile("FileSystemTree", ".tmp");
			temp.delete();
			temp.mkdir();
			if (temp.isDirectory()) {
				folderIcon = FileSystemView.getFileSystemView().
											getSystemIcon(temp);
				temp.delete();
			}
		} catch (Exception e) {}

		// An (IO)Exception was thrown, or the FilesystemView didn't return
		// a folder icon.
		if (folderIcon==null) {

			folderIcon  = UIManager.getIcon("FileView.directoryIcon");

			// If UIManager returned null, just get one we drew ourselves.
			if (folderIcon==null) {
				String path = "org/fife/ui/rtextfilechooser/images/";
				ClassLoader cl = FileChooserIconManager.class.
											getClassLoader();
				folderIcon = loadIcon(cl, path + "directory.gif");
			}

		}

		return folderIcon;

	}


	/**
	 * Returns the standard folder icon.  This is basically a hack to get the
	 * folder icon without looking through/saving to the icon cache (e.g., for
	 * <code>DirectoryTree</code>).
	 *
	 * @return The icon for folders.
	 */
	public Icon getFolderIcon() {
		return folderIcon;
	}


	/**
	 * Returns the icon used for this type of file by this file chooser.
	 *
	 * @param f The file for which you want to get its icon.
	 * @return The icon used.
	 */
	public Icon getIcon(File f) {

		Icon icon = null;

		if (f != null) {

			// First check to see if we've already got this
			// icon and cached it.
			icon = iconCache.get(f);
			if (icon!=null)
				return icon;

			// See if the system has an icon for this file.  FileSystemView
			// will write a stack trace to stderr (!) if the file does not
			// and is not a root folder, so we must guard against that here.
			if (f.exists() || RootManager.getInstance().isRoot(f)) {
				try {
					icon = fileSystemView.getSystemIcon(f);
				} catch (/*FileNotFound*/Exception fnfe) {
					// This happens, for example, on Windows when no such
					// file "f" exists - the FileSystemView must check for
					// the existence of the icon first.
					//fnfe.printStackTrace();
					// Leave icon as null, it'll get set below.
				}
			}

			// If it didn't, see if it matches one of our defaults.
			if (icon==null) {
				if (fileSystemView.isFloppyDrive(f)) {
					icon = floppyDriveIcon;
				}
				else if (fileSystemView.isDrive(f)) {
					icon = hardDriveIcon;
				}
				else if (fileSystemView.isComputerNode(f)) {
					icon = computerIcon;
				}
				else if (f.isDirectory()) {
					icon = folderIcon;
				}
				else { // Must be a regular file.
					icon = fileIcon;
				}
			}

			// Remember the icon for this file.
			iconCache.put(f, icon);

		}

		return icon;

	}


	/**
	 * Creates and returns an icon from an image on disk.
	 *
	 * @param cl The classloader to locate the resource.
	 * @param file The file name.
	 * @return The icon.
	 */
	private static Icon loadIcon(ClassLoader cl, String file) {
		return new ImageIcon(cl.getResource(file));
	}


	/**
	 * Removes the cached icon for a single file, allowing it to be recreated.
	 *
	 * @param file The file whose icon should be removed.
	 * @return The old icon, or <code>null</code> if there was none.
	 */
	public Icon removeIconFor(File file) {
		return iconCache.remove(file);
	}


}