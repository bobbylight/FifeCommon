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
 * @version 0.1
 */
public class ExtendedLookAndFeelInfo extends UIManager.LookAndFeelInfo {

	private String jarFile;


	/**
	 * Constructor.
	 *
	 * @param name The name of the Look and Feel.
	 * @param className The name of the main class of the Look and Feel.
	 * @param jarFile The JAR file containing the Look and Feel.  This is a
	 *        path to the JAR that is relative to the GUI application using
	 *        this Look.
	 */
	public ExtendedLookAndFeelInfo(String name, String className,
							String jarFile) {
		super(name, className);
		this.jarFile = jarFile;
	}


	/**
	 * Returns the JAR file containing this Look and Feel.
	 *
	 * @return The JAR file.  This path is relative to a GUI application's
	 *         install location.
	 * @see org.fife.ui.app.GUIApplication#getInstallLocation
	 */
	public String getJarFile() {
		return jarFile;
	}


	/**
	 * Returns a URL specifying the JAR file containing this Look and Feel.
	 *
	 * @param app The GUI application for which this Look and Feel is to be
	 *        available.  The JAR file path is assumed to be relative to its
	 *        install location.
	 * @return A URL for the JAR file.
	 * @throws MalformedURLException This should never happen.
	 * @see #getJarFile
	 * @see org.fife.ui.app.GUIApplication#getInstallLocation
	 */
	public URL getURL(final GUIApplication app) throws MalformedURLException {
		return new File(app.getInstallLocation(), jarFile).toURI().toURL();
	}


}