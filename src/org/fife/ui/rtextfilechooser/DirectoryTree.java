/*
 * 07/26/2004
 *
 * DirectoryTree.java - A JTree containing all directories in the local host's
 * file system.
 * Copyright (C) 2004 Robert Futrell
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
	 * Does any filtering and sorting of an array of files so that they will
	 * be displayed properly.  For this class, this method takes only the
	 * directories (not regular files) from the array and sorts them
	 * alphabetically.
	 *
	 * @param files The array of files to filter and sort.
	 * @return The filtered and sorted array of directories.
	 */
	protected File[] filterAndSort(File[] files) {

		int num = files.length;
		ArrayList dirList = new ArrayList();

		// First, separate the directories from regular files so we can
		// sort them individually.  This part could be made more compact,
		// but it isn't just for a tad more speed.
		for (int i=0; i<num; i++) {
			if (files[i].isDirectory())
				dirList.add(files[i]);
		}

		Collections.sort(dirList);

		File[] fileArray = new File[dirList.size()];
		return (File[])dirList.toArray(fileArray);

	}


}