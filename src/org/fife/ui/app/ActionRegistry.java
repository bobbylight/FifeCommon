/*
 * 01/09/2013
 *
 * ActionRegistry - A registry of actions in a GUI application.
 * Copyright (C) 2013 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.Action;
import javax.swing.KeyStroke;


/**
 * A mapping of keys to actions.  Instances of
 * <code>AbstractGUIApplication</code> use this class to keep track of their
 * actions, and to simply loading and saving their customized shortcuts.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public class ActionRegistry {

	private Map actionMap;

	private static final String PROPS_FILE_HEADER =
			"Shortcuts for this application.  Do not modify by hand!";


	public ActionRegistry() {
		actionMap = new HashMap();
	}


	/**
	 * Adds an action to this action registry.
	 *
	 * @param key The key with which to fetch the action via
	 *        <code>getAction</code>.
	 * @param action The action to add.
	 * @see #getAction(String)
	 */
	public void addAction(String key, Action action) {
		if (action==null) {
			throw new NullPointerException("action cannot be null");
		}
		else if (key==null) {
			throw new NullPointerException("key cannot be null");
		}
		actionMap.put(key, action);
	}


	/**
	 * Returns an action by its key.
	 *
	 * @return The action, or <code>null</code> if no action exists for the
	 *         specified key.
	 * @see #addAction(String, Action)
	 * @see #getActions()
	 */
	public Action getAction(String key) {
		return (Action)actionMap.get(key);
	}


	/**
	 * Returns the list of keys of all actions known to this registry.
	 *
	 * @return The list of all action keys.
	 */
	public SortedSet getActionKeys() {
		return new TreeSet(actionMap.keySet());
	}


	/**
	 * Returns all actions as an array.
	 *
	 * @return The actions.  <code>null</code> is returned if the action
	 *         map has not yet been initialized.
	 * @see #getAction(String)
	 */
	public Action[] getActions() {
		Set keySet = actionMap.keySet();
		int size = keySet.size();
		Action[] array = new Action[size];
		int j = 0;
		for (Iterator i=keySet.iterator(); i.hasNext(); ) {
			array[j++] = (Action)actionMap.get(i.next());
		}
		return array;
	}


	/**
	 * Loads shortcuts for the actions from a file.  The file is expected to
	 * be a standard Java properties file; its keys correspond to the action
	 * keys in this registry, and its values correspond go the
	 * {@link KeyStroke}s to use for action accelerators.<p>
	 * 
	 * If any property key doesn't have a corresponding action in this registry,
	 * it is ignored.  If any property value isn't a valid
	 * <code>KeyStroke</code>, it is ignored.
	 * 
	 * @param file The properties file to load.
	 * @throws IOException If an IO error occurs reading the file.
	 * @see #saveShortcuts(File)
	 */
	public void loadShortcuts(File file) throws IOException {

		Properties props = new Properties();

		BufferedInputStream bin = new BufferedInputStream(
				new FileInputStream(file));
		try {
			props.load(bin);
		} finally {
			bin.close();
		}

		Set entries = props.entrySet();
		for (Iterator i=entries.iterator(); i.hasNext(); ) {
			Map.Entry entry = (Map.Entry)i.next();
			String key = (String)entry.getKey();
			Action a = getAction(key);
			if (a!=null) {
				String value = (String)entry.getValue();
				KeyStroke ks = KeyStroke.getKeyStroke(value);
				a.putValue(Action.ACCELERATOR_KEY, ks);
			}
		}

	}


	/**
	 * Saves the shortcuts of all actions in this registry to a properties
	 * file.  Property keys are the action keys, property values are the
	 * <code>KeyStroke</code>s for the shortcuts for each action.  Actions
	 * with no shortcut will have an empty string value for their property.
	 * 
	 * @param file The file to save to.
	 * @throws IOException If an IO error occurs writing the file.
	 * @see #loadShortcuts(File)
	 */
	public void saveShortcuts(File file) throws IOException {

		Properties props = new Properties();
		SortedSet keys = getActionKeys();
		for (Iterator i=keys.iterator(); i.hasNext(); ) {
			String key = (String)i.next();
			Action action = getAction(key);
			KeyStroke ks = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
			String value = ks==null ? "" : ks.toString();
			props.setProperty(key, value);
		}
		
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(file));
		try {
			props.store(out, PROPS_FILE_HEADER);
		} finally {
			out.close();
		}
		
	}


}