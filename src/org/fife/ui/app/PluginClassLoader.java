/*
 * 01/10/2005
 *
 * PluginClassLoader.java - Class loader for GUIApplication plugins.
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
import java.io.FileFilter;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


/**
 * The class loader for all <code>AbstractPluggableGUIApplication</code>
 * plugins.
 *
 * @author Robert Futrell
 * @version 0.1
 * @see AbstractPluggableGUIApplication
 */
public class PluginClassLoader extends ClassLoader {

	/**
	 * The GUI application that owns this class loader.
	 */
	private AbstractPluggableGUIApplication app;

	/**
	 * The directory in which all plugin jars reside.
	 */
	private File pluginDir;

	/**
	 * A hashmap mapping resources to the Jar files they're in.
	 */
	private HashMap hashMap;

	private static final String SEPARATOR = File.separator;


	/**
	 * Constructor.  This method is thread-safe; it ensures that all
	 * plugins are added to the GUI on the EDT if necessary.
	 *
	 * @param app The GUI application.
	 * @throws IOException If an I/O error occurs.
	 */
	public PluginClassLoader(AbstractPluggableGUIApplication app)
											throws IOException {

		super(app.getClass().getClassLoader());
		this.app = app;
		hashMap = new HashMap();
		pluginDir = new File(app.getInstallLocation(), "plugins");
		if (!pluginDir.isDirectory())
			return;

		File[] jars = pluginDir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".jar");
			}
		});
		int jarCount = jars.length;
		//System.err.println("... jarCount: " + jarCount);

		ArrayList plugins = new ArrayList();

		for (int i=0; i<jarCount; i++) {
			JarFile jarFile = new JarFile(jars[i]);
			try {
				Enumeration entires = jarFile.entries();
				while (entires.hasMoreElements()) {
					ZipEntry entry = (ZipEntry)entires.nextElement();
					String name = entry.getName();
					if (name.endsWith("Plugin.class")) {
						plugins.add(name);
					}
					hashMap.put(name, jars[i].getAbsolutePath());
				}
			} finally {
				jarFile.close();
			}
		}

		loadPlugins(plugins);

	}


	/**
	 * Finds the specified class.  This method is invoked by
	 * <code>loadClass</code> after checking to see if the class has already
	 * been loaded, and then checking the parent class loader for it.
	 *
	 * @param name The name of the class.
	 * @return The resulting class object.
	 * @throws ClassNotFoundException If the class could not be found.
	 */
	protected Class findClass(String name) throws ClassNotFoundException {
		 // Sometimes we'll get slashes, others dots.
		name = name.replaceAll("\\.", "/");
		Object jarFileName = hashMap.get(name + ".class");
		if (jarFileName==null)
			throw new ClassNotFoundException(name);
		try {
			JarFile jarFile = new JarFile((String)jarFileName);
			ZipEntry entry = jarFile.getEntry(name + ".class");
			if (entry==null) // Should never happen.
				throw new ClassNotFoundException(name);
			InputStream in = jarFile.getInputStream(entry);
			int length = (int)entry.getSize();
			byte[] data = new byte[length];
			int bytesRead = 0;
			int offset = 0;
			while (length>0) {
				int temp = in.read(data, offset,length);
				if (temp==-1)
					throw new ClassNotFoundException(name);
				offset += temp;
				bytesRead += temp;
				length -= temp;
			}
			in.close();
			name = name.replaceAll("/", ".");
			Class c = defineClass(name, data, 0,data.length);
			return c;
		} catch (IOException ioe) {
			app.displayException(ioe);
		}
		throw new ClassNotFoundException(name);
	}


	public URL findResource(String name) {
		Object jarFileName = hashMap.get(name);
		if (jarFileName==null)
			return null;
		String jar = (String)jarFileName;
		if (SEPARATOR.equals("\\"))
			jar = jar.replaceAll("\\\\", "/");
		try {
			return new URL("jar:file:" + jar + "!/" + name);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// Fall through.
		}
		return null;
	}


	private void loadPlugins(ArrayList plugins) {

		int count = plugins.size();

		if (count>0) {

			final Object[] objs = { app };
			Class[] params = { AbstractPluggableGUIApplication.class };

			for (int i=0; i<count; i++) {
				try {
					Thread.sleep(500); // Space the Runnables out a little
					String className = (String)plugins.get(i);
					className = className.substring(0, className.length()-6);
					Class c = loadClass(className);
					// Class ended in "Plugin.class", but if it's not actually
					// a subclass of Plugin, then we have an error.
					if (Plugin.class.isAssignableFrom(c)) {
						final Constructor cnst = c.getConstructor(params);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								try {
									Plugin p = (Plugin)cnst.newInstance(objs);
									app.addPlugin(p);
								} catch (InvocationTargetException ite) {
									Throwable e = ite.getTargetException();
									e.printStackTrace();
									app.displayException(e);
								} catch (Exception e) {
									e.printStackTrace();
									app.displayException(e);
								}
							}
						});
					}
					else {
						throw new InvalidPluginException(
							"Plugin JAR contained a class ending with " +
							"'Plugin' that does not extend class Plugin: " +
							className);
					}
				} catch (final Exception e) {
					e.printStackTrace();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							app.displayException(e);
						}
					});
				}
			}

		}

	}


}