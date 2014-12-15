/*
 * 06/24/2005
 *
 * RootManager.java - Maintains information on the "roots" of the local file
 * system.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.filechooser.FileSystemView;


/**
 * Keeps information on the roots of the local file system.  This class
 * is a singleton and is used by all <code>RTextFileChooser</code>s and
 * <code>FileSystemTree</code>s.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class RootManager {

	private File[] roots;
	private int rootCount;

	private static final RootManager INSTANCE = new RootManager();


	/**
	 * Private constructor.
	 */
	private RootManager() {
		roots = RootManager.getAllRoots();
		rootCount = roots.length;
	}


	/**
	 * Gets the roots from the FileSystemView and File.
	 *
	 * @return An array of the roots of the local file system.
	 */
	private static final File[] getAllRoots() {
		FileSystemView fsv = FileSystemView.getFileSystemView();
		ArrayList<File> list = new ArrayList<File>(Arrays.asList(fsv.getRoots()));
		File[] moreRoots = File.listRoots();
		for (int i=0; i<moreRoots.length; i++)
			if (!list.contains(moreRoots[i]))
				list.add(moreRoots[i]);
		Collections.sort(list);
		File[] fileArray = new File[list.size()];
		return list.toArray(fileArray);
	}


	/**
	 * Returns the singleton instance of the <code>RootManager</code>.
	 *
	 * @return The singleton instance.
	 */
	public static RootManager getInstance() {
		return INSTANCE;
	}


	/**
	 * Returns the root for a given file.
	 *
	 * @param file The file.
	 * @return The root for the file.
	 */
	public File getRootForFile(final File file) {
		File f2 = file.getAbsoluteFile(); // Needed for e.g. 'new File(".");'
		while (f2!=null) {
			for (int i=0; i<rootCount; i++) {
				if (f2.equals(roots[i]))
					return f2;
			}
			f2 = f2.getParentFile();
		}
		return null; // Should never happen!
	}


	/**
	 * Returns the root for a given file.
	 *
	 * @param file The file.
	 * @return The root for the file.
	 */
	public File getRootForFile(final String file) {
		return getRootForFile(new File(file));
	}


	/**
	 * Returns whether or not the given file is a root (either by
	 * <code>File</code> standards or <code>FileSystemView</code> standards).
	 *
	 * @param file The file to check.
	 * @return Whether or not <code>file</code> is a root.
	 */
	public boolean isRoot(final File file) {
		for (int i=0; i<rootCount; i++) {
			if (roots[i].equals(file))
				return true;
		}
		return false;
	}


	/**
	 * Returns an iterator for going through the roots in the local file
	 * system.
	 *
	 * @return The iterator.
	 */
	public Iterator<File> iterator() {
		return new RootIterator();
	}


	/**
	 * Iterator for the file system's root nodes.
	 */
	private class RootIterator implements Iterator<File> {

		private int i = 0;

		public boolean hasNext() {
			return i<rootCount;
		}

		public File next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return roots[i++];
		}

		public void remove() {
			throw new UnsupportedOperationException("Cannot remove roots");
		}

	}


}