/*
 * 06/24/2005
 *
 * RootManager.java - Maintains information on the "roots" of the local file
 * system.
 * Copyright (C) 2005 Robert Futrell
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
		ArrayList list = new ArrayList(Arrays.asList(fsv.getRoots()));
		File[] moreRoots = File.listRoots();
		for (int i=0; i<moreRoots.length; i++)
			if (!list.contains(moreRoots[i]))
				list.add(moreRoots[i]);
		Collections.sort(list);
		File[] fileArray = new File[list.size()];
		return (File[])list.toArray(fileArray);
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
	public Iterator iterator() {
		return new RootIterator();
	}


	/**
	 * Iterator for the file system's root nodes.
	 */
	class RootIterator implements Iterator {

		private int i = 0;

		public boolean hasNext() {
			return i<rootCount;
		}

		public Object next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return roots[i++];
		}

		public void remove() {
			throw new UnsupportedOperationException("Cannot remove roots");
		}

	}


}