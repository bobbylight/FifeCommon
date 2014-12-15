/*
 * 07/26/2004
 *
 * DirectoryTree.java - A JTree containing all directories in the local host's
 * file system.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


/**
 * A tree containing all directories in the local host's file system.  This is
 * the tree used by <code>RDirectoryChooser</code>; however, it can also be used
 * independently in other dialogs/components.  If you wish to do so, you may
 * want to add a <code>PropertyChangeListener</code> to this tree; you will
 * receive the following events (besides the regular <code>JTree</code> ones):
 *
 * <ul>
 *   <li><code>WILL_EXPAND_PROPERTY</code> - When the tree is about to
 *       expand one of its nodes.  Upon receiving this property change event,
 *       you could set the cursor to the system wait cursor, for example (as
 *       the expansion could take some time; Java seems to have some trouble
 *       calling <code>File.isDirectory()</code> on network files...).</li>
 *   <li><code>EXPANDED_PROPERTY</code> - When the tree has completed
 *       expanding the node.  Upon receiving this property change event, you
 *       could set the cursor back to the default.</li>
 * </ul>
 *
 * @author Robert Futrell
 * @version 0.7
 */
public class DirectoryTree extends FileSystemTree {


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configurePopupMenuActions() {
		super.configurePopupMenuActions();
		newFileAction.setEnabled(false);
	}


	/**
	 * Overridden to take only the directories (not regular files) from the
	 * array and sorts them alphabetically.
	 *
	 * @param files The array of files to filter and sort.
	 * @return The filtered and sorted array of directories.
	 */
	@Override
	protected File[] filterAndSort(File[] files) {

		int num = files.length;
		ArrayList<File> dirList = new ArrayList<File>();

		// First, separate the directories from regular files so we can
		// sort them individually.  This part could be made more compact,
		// but it isn't just for a tad more speed.
		for (int i=0; i<num; i++) {
			if (files[i].isDirectory())
				dirList.add(files[i]);
		}

		Collections.sort(dirList);

		File[] fileArray = new File[dirList.size()];
		return dirList.toArray(fileArray);

	}


}