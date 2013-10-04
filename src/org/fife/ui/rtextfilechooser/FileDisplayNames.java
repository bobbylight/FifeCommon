/*
 * 09/15/2012
 *
 * FileDisplayNames.java - Utility class for getting display names for files.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.filechooser.FileSystemView;


/**
 * Utility class for getting file display names.  Useful to avoid potential
 * performance pitfalls, such as removal media drives on Windows.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FileDisplayNames {

	private Map<File, String> rootNameCache;

	private static final FileDisplayNames INSTANCE = new FileDisplayNames();


	/**
	 * Private constructor to prevent instantiation.
	 */
	private FileDisplayNames() {
		rootNameCache = new HashMap<File, String>();
		Iterator<File> i = RootManager.getInstance().iterator();
		while (i.hasNext()) {
			addCachedRootName(i.next());
		}
	}


	/**
	 * Adds a file name to the cached names (for roots).  We cache root
	 * directories' names because FileSystemView.getSystemDisplayName() can
	 * be costly, especially for disk drives on Windows (like A:\).
	 */
	private void addCachedRootName(File aRoot) {
		// Check separator character as a quick "hack" to check for Windows.
		// We don't call getName() for drives because for some reason it can
		// take a long time to get the name if it has one (such as A:\ and
		// C:\).
		if (File.separatorChar=='\\') {
			String absolutePath = aRoot.getAbsolutePath();
			if (absolutePath.length()==3 &&
					absolutePath.endsWith(":\\")) {
				rootNameCache.put(aRoot, absolutePath);
				return;
			}
		}
		rootNameCache.put(aRoot, getName(aRoot));
	}


	/**
	 * Returns the singleton instance of this class.
	 *
	 * @return The singleton instance of this class.
	 */
	public static FileDisplayNames get() {
		return INSTANCE;
	}


	/**
	 * Returns the display name for a given file.
	 *
	 * @param file The file for which to get the display name.
	 * @return The display name.
	 */
	public String getName(File file) {
		if (file==null)
			return null;
		String name = rootNameCache.get(file);
		if (name!=null)
			return name;
		name = FileSystemView.getFileSystemView().getSystemDisplayName(file);
		if (name!=null && name.length()>0) {
			return name;
		}
		name = file.getAbsolutePath();
		if (name.length()==0) { // Root directory "/", on OS X at least...
			name = "/";
		}
		return name;
	}


}