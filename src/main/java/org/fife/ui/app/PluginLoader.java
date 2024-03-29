/*
 * 01/10/2005
 *
 * PluginClassLoader.java - Class loader for GUIApplication plugins.
 * Copyright (C) 2005 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


/**
 * Loads {@link Plugin}s for an {@link AbstractPluggableGUIApplication}.  This
 * should be instantiated in a separate thread; it handles adding any found
 * <code>Plugin</code>s to the parent application on the EDT properly.
 *
 * @author Robert Futrell
 * @version 0.6
 * @see AbstractPluggableGUIApplication
 */
class PluginLoader {

	/**
	 * The manifest attribute that plugin jars must define that specifies
	 * the main <code>Plugin</code> class.
	 */
	public static final String PLUGIN_CLASS_ATTR = "Fife-Plugin-Class";


	/**
	 * The manifest attribute that plugin jars can optionally define to
	 * specify the priority with which to load the plugin.  If this is not
	 * specified or an invalid value, then "<code>normal</code>" is used.
	 *
	 * @see #LOAD_PRIORITIES
	 */
	public static final String PLUGIN_LOAD_PRIORITY = "Fife-Plugin-Load-Priority";


	/**
	 * Valid values for {@link #PLUGIN_LOAD_PRIORITY}.
	 */
	public static final String[] LOAD_PRIORITIES = {
		"highest",
		"high",
		"normal",
		"low",
		"lowest",
	};


	/**
	 * The GUI application that owns this class loader.
	 */
	private AbstractPluggableGUIApplication<?> app;

	/**
	 * The directory in which all plugin jars reside.
	 */
	private File pluginDir;

	/**
	 * Class loader pointing to all plugin jars.
	 */
	private URLClassLoader ucl;

	/**
	 * Indicates whether all plugins have been submitted to load.  Access to
	 * this member should be synchronized.
	 *
	 * @see #loadingPluginCount
	 */
	private boolean pluginSubmissionsCompleted;

	/**
	 * Indicates how many plugins have been submitted to load, but not yet
	 * loaded.  Access to this member should be synchronized.
	 *
	 * @see #pluginSubmissionsCompleted
	 */
	private int loadingPluginCount;

	/**
	 * The amount of time to sleep, in milliseconds, between loading each
	 * plugin.
	 */
	private static final int SLEEP_TIME						= 200;


	/**
	 * Constructor.
	 *
	 * @param app The GUI application.
	 */
	PluginLoader(AbstractPluggableGUIApplication<?> app) {
		this.app = app;
		pluginDir = getPluginDir(app);
	}


	/**
	 * Returns the single-argument constructor for a plugin that accepts
	 * our application.  Note this checks for both constructors that take
	 * the specific application class type, as well as superclasses,
	 * for plugins that are not app-specific.
	 *
	 * @param pluginClazz The plugin class.
	 * @return The constructor that takes an application.
	 */
	private Constructor<?> getApplicationConstructor(Class<?> pluginClazz) {

		Constructor<?>[] constructors = pluginClazz.getConstructors();
		Class<?> appClazz = app.getClass();

		for (Constructor<?> constructor : constructors) {
			Class<?>[] paramTypes = constructor.getParameterTypes();
			if (paramTypes.length == 1 && (paramTypes[0].isAssignableFrom(appClazz))) {
				return constructor;
			}
		}
		throw new IllegalArgumentException(
			"Plugin class has no single-arg app constructor: " + pluginClazz.getName());
	}


	/**
	 * Returns the priority with which the plugin should be loaded.
	 *
	 * @param attrs The main manifest attributes.
	 * @return The priority with which to load the plugin.
	 */
	private static int getLoadPriority(Attributes attrs) {

		int priority = 2;

		String temp = attrs.getValue(PLUGIN_LOAD_PRIORITY);
		if (temp!=null) {
			for (int i=0; i<LOAD_PRIORITIES.length; i++) {
				if (LOAD_PRIORITIES[i].equalsIgnoreCase(temp)) {
					priority = i;
					break;
				}
			}
		}

		return priority;

	}


	/**
	 * Returns the directory in which to look for plugin jars.
	 *
	 * @param app The application.
	 * @return The plugin jar directory.
	 */
	private static File getPluginDir(
			AbstractPluggableGUIApplication<?> app) {
		return new File(app.getInstallLocation(), "plugins");
	}


	/**
	 * Returns whether plugin loading has completed.
	 *
	 * @return Whether plugin loading has completed.
	 */
	public synchronized boolean isPluginLoadingComplete() {
		return pluginSubmissionsCompleted && loadingPluginCount==0;
	}


	/**
	 * Loads a single plugin.
	 *
	 * @param className The class name of the plugin.
	 * @throws Exception If an error occurs.
	 */
	private void loadPluginImpl(String className) throws Exception {

		final Object[] objs = { app };
		Class<?> c = ucl.loadClass(className);

		// This should be true unless there was an error in the manifest
		if (Plugin.class.isAssignableFrom(c)) {
			synchronized (this) {
				loadingPluginCount++;
			}
			final Constructor<?> cnst = getApplicationConstructor(c);
			SwingUtilities.invokeLater(() -> {
				try {
					Plugin<?> p = (Plugin<?>)cnst.newInstance(objs);
					app.addPlugin(p);
				} catch (InvocationTargetException ite) {
					Throwable e = ite.getTargetException();
					e.printStackTrace();
					app.displayException(e);
				} catch (Exception e) {
					e.printStackTrace();
					app.displayException(e);
				} finally {
					synchronized (this) {
						loadingPluginCount--;
					}
				}
			});
		}

		else {
			throw new InvalidPluginException(
				"Specified plugin does not implement the Plugin interface: " +
				className);
		}

	}


	/**
	 * Loads any plugins for this application.  This method is thread-safe; it
	 * ensures all plugins are added to the GUI on the EDT if necessary.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	public void loadPlugins() throws IOException {

		if (!pluginDir.isDirectory()) {
			return;
		}

		// Get all jars in the plugin directory.
		File[] jars = pluginDir.listFiles(f -> f.getName().endsWith(".jar"));

		List<List<String>> plugins = new ArrayList<>(
			LOAD_PRIORITIES.length);
		for (int i=0; i<LOAD_PRIORITIES.length; i++) {
			plugins.add(new ArrayList<>(3)); // Small
		}
		List<URL> urlList = new ArrayList<>();

		for (File jar : jars) {

			urlList.add(jar.toURI().toURL());

			try (JarFile jarFile = new JarFile(jar)) {
				// If this jar contains a plugin, remember the class to load.
				Manifest mf = jarFile.getManifest();
				if (mf != null) {
					Attributes attrs = mf.getMainAttributes();
					String clazz = attrs.getValue(PLUGIN_CLASS_ATTR);
					if (clazz != null) {
						int priority = getLoadPriority(attrs);
						plugins.get(priority).add(clazz);
					}
				}
			}

		}

		// Create the ClassLoader that does the actual dirty-work.
		URL[] urls = urlList.toArray(new URL[0]);
		ucl = new URLClassLoader(urls, app.getClass().getClassLoader());

		loadPluginsImpl(plugins);

	}


	/**
	 * Loads the main plugin classes, and adds the resulting {@link Plugin}s
	 * to the application.
	 *
	 * @param plugins The list of list of plugin classes.  Each element in the
	 *        "first" list is a list of plugins of a specific priority, or
	 *        <code>null</code> if no plugins of that priority were discovered.
	 */
	private void loadPluginsImpl(List<List<String>> plugins) {

		for (List<String> pluginList : plugins) {
			if (pluginList!=null) {
				for (String className : pluginList) {
					try {
						Thread.sleep(SLEEP_TIME); // Space the Runnables out
						loadPluginImpl(className);
					} catch (final Exception e) {
						e.printStackTrace();
						SwingUtilities.invokeLater(() -> app.displayException(e));
					}
				}
			}
		}

		// Specify that plugins have all been submitted to load
		synchronized (this) {
			pluginSubmissionsCompleted = true;
		}

	}


}
