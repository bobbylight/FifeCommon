/*
 * 04/14/2012
 *
 * RecentFilesMenu.java - A menu with items to open recently-opened files.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * A menu that keeps track of recently opened files.  Whenever a file is
 * opened, it is moved to the "top" of the list, as it is assumed that popular
 * files will get re-opened over and over again.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class RecentFilesMenu extends JMenu {

	private int maxFileHistorySize;
	
	/**
	 * A cache of the full paths of files in this menu, to allow us to know
	 * the index of a specific item to move it to the top later.
	 */
	private List fileHistory;

	private static final int DEFAULT_MAX_SIZE = 20;
	

	/**
	 * Constructor.
	 *
	 * @param name The name for this menu item, e.g. "Recent Files".
	 */
	public RecentFilesMenu(String name) {
		this(name, (String[])null);
	}


	/**
	 * Constructor.
	 *
	 * @param name The name for this menu item, e.g. "Recent Files".
	 * @param initialContents The initial contents of this menu.
	 */
	public RecentFilesMenu(String name, List initialContents) {
		super(name);
		this.maxFileHistorySize = DEFAULT_MAX_SIZE;
		fileHistory = new ArrayList(maxFileHistorySize);
		if (initialContents!=null) {
			for (Iterator i=initialContents.iterator(); i.hasNext(); ) {
				addFileToFileHistory((String)i.next());
			}
		}
	}


	/**
	 * Constructor.
	 *
	 * @param name The name for this menu item, e.g. "Recent Files".
	 * @param initialContents The initial contents of this menu.
	 */
	public RecentFilesMenu(String name, String[] initialContents) {
		super(name);
		this.maxFileHistorySize = DEFAULT_MAX_SIZE;
		fileHistory = new ArrayList(maxFileHistorySize);
		if (initialContents!=null) {
			for (int i=0; i<initialContents.length; i++) {
				addFileToFileHistory(initialContents[i]);
			}
		}
	}


	/**
	 * Adds the file specified to the file history.
	 *
	 * @param fileFullPath Full path to a file to add to the file history in
	 *        the File menu.
	 * @see #getShouldIgnoreFile(String)
	 */
	public void addFileToFileHistory(String fileFullPath) {

		if (getShouldIgnoreFile(fileFullPath)) {
			return;
		}

		JMenuItem menuItem;

		// If the file history already contains this file, remove it and add
		// it back to the top of the list; this keeps the list in a "most
		// recently opened" order.
		int index = fileHistory.indexOf(fileFullPath);
		if (index>-1) {
			// Remove it physically from the menu and add it back at the
			// top, then remove it from the path history and it its path
			// to the top of that.
			menuItem = (JMenuItem)getMenuComponent(index);
			remove(index);
			insert(menuItem, 0);
			Object temp = fileHistory.remove(index);
			fileHistory.add(0, temp);
			return;
		}

		// Add the new file to the top of the file history list.
		menuItem = new JMenuItem(createOpenAction(fileFullPath));
		insert(menuItem, 0);
		fileHistory.add(0, fileFullPath);

		// Too many files?  Oust the file in history added least recently.
		if (getItemCount()>maxFileHistorySize) {
			remove(getItemCount()-1);
			fileHistory.remove(fileHistory.size()-1);
		}

	}


	/**
	 * Creates the action that will open the specified file in the application
	 * when its menu item is selected.  Subclasses should override.
	 *
	 * @param fileFullPath The selected file.
	 * @return The action that will be invoked.
	 */
	protected abstract Action createOpenAction(String fileFullPath);


	/**
	 * Returns the full path to the specified file in this history.
	 *
	 * @param index The index of the file.
	 * @return The file.
	 */
	public String getFileFullPath(int index) {
		return (String)fileHistory.get(index);
	}


	/**
	 * Returns the maximum number of files the file history in the File menu
	 * will remember.
	 *
	 * @return The maximum size of the file history.
	 */
	public int getMaximumFileHistorySize() {
		return maxFileHistorySize;
	}


	/**
	 * Provides a hook for subclasses to not remember specific files.  If
	 * <code>addFileToFileHistory()</code> is called but the file specified
	 * does not pass this method's criteria, it will not get added to history.
	 * The default implementation does nothing.
	 *
	 * @param fileFullPath The file to (possibly) ignore.
	 * @return Whether to ignore the file.  The default implementation always
	 *         returns <code>false</code>.
	 * @see #addFileToFileHistory(String)
	 */
	protected boolean getShouldIgnoreFile(String fileFullPath) {
		return false;
	}


	/**
	 * Sets the maximum number of files to remember in history.
	 *
	 * @param newSize The new size of the file history.
	 */
	public void setMaximumFileHistorySize(int newSize) {

		if (newSize<0)
			return;

		// Remember the new size.
		maxFileHistorySize = newSize;

		// If we're bigger than the new max size, trim down
		while (getItemCount()>maxFileHistorySize) {
			remove(getItemCount()-1);
			fileHistory.remove(fileHistory.size()-1);
		}

	}


}