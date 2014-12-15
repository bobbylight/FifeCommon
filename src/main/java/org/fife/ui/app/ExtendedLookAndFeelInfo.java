/*
 * 03/11/2005
 *
 * ExtendedLookAndFeelInfo.java - Information on a 3rd party Look and Feel in a
 * JAR file.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.UIManager;


/**
 * Information about a 3rd party Look and Feel in a JAR file.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ExtendedLookAndFeelInfo extends UIManager.LookAndFeelInfo {

	private String jarFiles;


	/**
	 * Constructor.
	 *
	 * @param name The name of the Look and Feel.
	 * @param className The name of the main class of the Look and Feel.
	 * @param jarFiles The JAR file(s) containing the Look and Feel.  This is a
	 *        comma-separated set of paths to the JARs that are relative to the
	 *        GUI application using this Look.
	 */
	public ExtendedLookAndFeelInfo(String name, String className,
							String jarFiles) {
		super(name, className);
		this.jarFiles = jarFiles;
	}


	/**
	 * Returns the JAR files containing this Look and Feel.
	 *
	 * @return The JAR files.  These are comma-separated paths relative to a
	 *         GUI application's install location.
	 * @see org.fife.ui.app.GUIApplication#getInstallLocation()
	 */
	public String getJarFiles() {
		return jarFiles;
	}


	/**
	 * Returns a URL array specifying the JAR files containing this Look and
	 * Feel.
	 *
	 * @param root The root directory that the JAR file paths are assumed to
	 *        be relative to.
	 * @return A URL array for the JAR files.  This will never be
	 *         <code>null</code>.
	 * @throws MalformedURLException This should never happen.
	 * @see #getJarFiles()
	 * @see org.fife.ui.app.GUIApplication#getInstallLocation()
	 */
	public URL[] getURLs(String root) throws MalformedURLException {
		String[] jars = jarFiles.split(",");
		URL[] urls = new URL[jars.length];
		for (int i=0; i<jars.length; i++) {
			File jarFile = new File(root, jars[i]);
			urls[i] = jarFile.toURI().toURL();
		}
		return urls;
	}


}