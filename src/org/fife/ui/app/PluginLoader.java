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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


/**
 * Loads {@link Plugin}s for an {@link AbstractPluggableGUIApplication}.  This
 * should be instantiated in a separate thread; it handles adding any found
 * <code>Plugin</code>s to the parent application on the EDT properly.
 *
 * @author Robert Futrell
 * @version 0.5
 * @see AbstractPluggableGUIApplication
 */
class PluginLoader {

	/**
	 * The manifest attribute that plugin jars must define that specifies
	 * the main <code>Plugin</code> class.
	 */
	public static final String PLUGIN_CLASS_ATTR = "Fife-Plugin-Class";

	/**
	 * The GUI application that owns this class loader.
	 */
	private AbstractPluggableGUIApplication app;

	/**
	 * The directory in which all plugin jars reside.
	 */
	private File pluginDir;

	/**
	 * Class loader pointing to all plugin jars.
	 */
	private URLClassLoader ucl;


	/**
	 * Constructor.  This method is thread-safe; it ensures that all
	 * plugins are added to the GUI on the EDT if necessary.
	 *
	 * @param app The GUI application.
	 * @throws IOException If an I/O error occurs.
	 */
	public PluginLoader(AbstractPluggableGUIApplication app)
											throws IOException {

		this.app = app;
		pluginDir = new File(app.getInstallLocation(), "plugins");
		if (!pluginDir.isDirectory()) {
			return;
		}

		// Get all jars in the plugin directory.
		File[] jars = pluginDir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".jar");
			}
		});
		int jarCount = jars.length;

		ArrayList plugins = new ArrayList();
		ArrayList urlList = new ArrayList();

		for (int i=0; i<jarCount; i++) {

			urlList.add(jars[i].toURI().toURL());

			JarFile jarFile = new JarFile(jars[i]);
			try {
				// If this jar contains a plugin, remember the class to load.
				Manifest mf = jarFile.getManifest();
				if (mf!=null) {
					String clazz= mf.getMainAttributes().
												getValue(PLUGIN_CLASS_ATTR);
					if (clazz!=null) {
						plugins.add(clazz);
					}
				}
			} finally {
				jarFile.close();
			}

		}

		// Create the ClassLoader that does the actual dirty-work.
		URL[] urls = (URL[])urlList.toArray(new URL[urlList.size()]);
		ucl = new URLClassLoader(urls, app.getClass().getClassLoader());

		loadPlugins(plugins);

	}


	/**
	 * Loads the main plugin classes, and adds the resulting {@link Plugin}s
	 * to the application.
	 *
	 * @param plugins The list of plugin classes.
	 */
	private void loadPlugins(ArrayList plugins) {

		int count = plugins.size();

		if (count>0) {

			final Object[] objs = { app };
			Class[] params = { AbstractPluggableGUIApplication.class };

			for (int i=0; i<count; i++) {
				try {
					Thread.sleep(500); // Space the Runnables out a little
					String className = (String)plugins.get(i);
					Class c = ucl.loadClass(className);
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