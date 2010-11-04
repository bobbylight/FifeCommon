/*
 * 03/11/2005
 *
 * ExtendedLookAndFeelInfo.java - Information on a 3rd party Look and Feel in a
 * JAR file.
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
	 * @param The root directory that the JAR file paths are assumed to be
	 *        relative to.
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
			urls[i]=new File(root, jars[i]).toURI().toURL();
		}
		return urls;
	}


}