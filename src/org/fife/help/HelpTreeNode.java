/*
 * 11/14/2003
 *
 * HelpTreeNode.java - Class used by HelpDialog.  Represents a "node" in the
 * Contents tree.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.help;

import java.io.File;
import java.net.URL;


/**
 * Helper class to {@link HelpDialog}; your
 * <code>DefaultMutableTreeNodes</code> need to
 * contain instances of this class when creating a <code>HelpDialog</code>.
 *
 * @author Robert Futrell
 *
 * @version 1.0
 */
public class HelpTreeNode {

	/**
	 * The help page that will be loaded when the user clicks on this node in
	 * the Contents tree.
	 */
	public URL url;

	/**
	 * The text of the node.
	 */
	public String title;


	/**
	 * Creates a <code>HelpTreeNode</code> with no URL.  This constructor should
	 * be used for nodes that you do not wish to associate a help page with, such
	 * as expandable ("folder") nodes.
	 *
	 * @param title The text displayed beside this Contents tree node.
	 */
	public HelpTreeNode(String title) {
		this.title = title;
		this.url = null;
	}


	/**
	 * Creates a <code>HelpTreeNode</code>.
	 *
	 * @param title The text displayed beside this Contents tree node.
	 * @param url The help page that will be loaded when the user clicks on
	 *        this node.
	 */
	public HelpTreeNode(String title, URL url) {
		this.title = title;
		this.url = url;
	}


	/**
	 * Creates a <code>HelpTreeNode</code>.
	 *
	 * @param title The text displayed beside this Contents tree node.
	 * @param urlString The URL path to the help page that will be loaded when
	 *        the user clicks on this node.
	 */
	public HelpTreeNode(String title, String urlString) {
		this.title = title;
		try {
			url = new File(urlString).toURI().toURL();
		} catch (Exception e) {}
		//System.err.println(url);
	}


	/**
	 * Returns the title of this node.
	 */
	@Override
	public String toString() {
		return title;
	}


}