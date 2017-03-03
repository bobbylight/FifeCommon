/*
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.util.ResourceBundle;

import org.fife.ui.StandardAction;


/**
 * The action type used by all instances of <code>GUIApplication</code>.  This
 * class not only provides ease-of-use methods, but knows how to look up its
 * properties in a resource bundle.<p>
 *
 * For example, you could define an action to create a new document like so in
 * a properties file:
 *
 * <pre>
 * NewAction=New
 * NewAction.Mnemonic=N
 * NewAction.ShortDesc=Creates a new text file.
 * NewAction.Accelerator=default N
 * </pre>
 *
 * <p>
 * This creates an action that can be used for a menu item (for example), with
 * label "New", mnemonic 'N', an appropriate short description that gets
 * displayed in the application's status bar on rollover, and an accelerator.
 * </p>
 *
 * <p>
 * For accelerators, the standard syntax for key strokes defined
 * <a href="https://docs.oracle.com/javase/7/docs/api/javax/swing/KeyStroke.html#getKeyStroke(java.lang.String)">here</a>
 * can be used.  In addition, the string literal "default" maps to "ctrl" on
 * Windows and Linux, and "meta" on OS X.
 * </p>
 *
 * <p>
 * In addition, OS-specific accelerators can be defined, for example:
 * </p>
 *
 * <pre>
 * NextTabAction.Accelerator.OSX=meta shift BRACELEFT
 * NextTabAction.Accelerator.Windows=ctrl PAGE_DOWN
 * NextTabAction.Accelerator.Linux=ctrl TAB
 * </pre>
 *
 * <p>
 * If the appropriate OS-specific accelerator is defined for an action, it is
 * used, otherwise, the OS-agnostic accelerator is used, if defined.
 * </p>
 *
 * @param <T> The parent application's class.
 * @author Robert Futrell
 * @version 0.6
 * @see org.fife.ui.app.GUIApplication
 */
public abstract class AppAction<T extends GUIApplication> extends StandardAction {

	/**
	 * The application that owns this action.
	 */
	private T app;


	/**
	 * Creates an action with no name, description, shortcut, or other such
	 * properties.
	 *
	 * @param app The parent application.
	 */
	public AppAction(T app) {
		this.app = app;
	}


	/**
	 * Creates an action, initializing its properties from the parent
	 * application's resource bundle. The name of the action is found using the
	 * specified key.  If keys exist with the names:
	 * <ul>
	 *    <li><code>key + ".Mnemonic"</code>
	 *    <li><code>key + ".Accelerator"</code>
	 *    <li><code>key + ".ShortDesc"</code>
	 * </ul>
	 * then those properties are set as well.  Further, if an accelerator is
	 * defined, it is set as both the action's active accelerator and default
	 * accelerator.<p>
	 *
	 * You can provide OS-specific accelerators for actions by defining any of
	 * the following properties:
	 * <ul>
	 *    <li><code>key + ".Accelerator.OSX"</code>
	 *    <li><code>key + ".Accelerator.Windows"</code>
	 *    <li><code>key + ".Accelerator.Linux"</code> (applies to Unix as well)
	 * </ul>
	 * This is useful for instances where different operating systems have
	 * different "standard" shortcuts for things.
	 * If the appropriate OS-specific accelerator is not defined, then the
	 * default value (<code>key + ".Accelerator"</code>) is used.
	 *
	 * @param app The parent application.
	 * @param key The key in the bundle for the name of this action.
	 */
	public AppAction(T app, String key) {
		super(app.getResourceBundle(), key);
		this.app = app;
	}


	/**
	 * Creates an action, initializing its properties from the parent
	 * application's resource bundle. The name of the action is found using the
	 * specified key.  If keys exist with the names:
	 * <ul>
	 *    <li><code>key + ".Mnemonic"</code>
	 *    <li><code>key + ".Accelerator"</code>
	 *    <li><code>key + ".ShortDesc"</code>
	 * </ul>
	 * then those properties are set as well.  Further, if an accelerator is
	 * defined, it is set as both the action's active accelerator and default
	 * accelerator.<p>
	 *
	 * You can provide OS-specific accelerators for actions by defining any of
	 * the following properties:
	 * <ul>
	 *    <li><code>key + ".Accelerator.OSX"</code>
	 *    <li><code>key + ".Accelerator.Windows"</code>
	 *    <li><code>key + ".Accelerator.Linux"</code> (applies to Unix as well)
	 * </ul>
	 * This is useful for instances where different operating systems have
	 * different "standard" shortcuts for things.
	 * If the appropriate OS-specific accelerator is not defined, then the
	 * default value (<code>key + ".Accelerator"</code>) is used.
	 *
	 * @param app The parent application.
	 * @param key The key in the bundle for the name of this action.
	 * @param icon The name of the icon resource for this action, or
	 *        <code>null</code> for no icon.  An icon will be fetched via
	 *        <pre>setIcon(getClass().getResource(res))</pre>.
	 * @see #setIcon(javax.swing.Icon)
	 */
	public AppAction(T app, String key, String icon) {
		super(app.getResourceBundle(), key);
		this.app = app;
		if (icon != null) {
			setIcon(icon);
		}
	}


	/**
	 * Creates an action, initializing its properties from a resource bundle.
	 * The name of the action is found using the specified key.  If keys exist
	 * with the names:
	 * <ul>
	 *    <li><code>key + ".Mnemonic"</code>
	 *    <li><code>key + ".Accelerator"</code>
	 *    <li><code>key + ".ShortDesc"</code>
	 * </ul>
	 * then those properties are set as well.  Further, if an accelerator is
	 * defined, it is set as both the action's active accelerator and default
	 * accelerator.<p>
	 *
	 * You can provide OS-specific accelerators for actions by defining any of
	 * the following properties:
	 * <ul>
	 *    <li><code>key + ".Accelerator.OSX"</code>
	 *    <li><code>key + ".Accelerator.Windows"</code>
	 *    <li><code>key + ".Accelerator.Linux"</code> (applies to Unix as well)
	 * </ul>
	 * This is useful for instances where different operating systems have
	 * different "standard" shortcuts for things.
	 * If the appropriate OS-specific accelerator is not defined, then the
	 * default value (<code>key + ".Accelerator"</code>) is used.
	 *
	 * @param app The parent application.
	 * @param msg The bundle to localize from.  If this is <code>null</code>,
	 *        then <code>app.getResourceBundle()</code> is used.
	 * @param key The key in the bundle for the name of this action.
	 */
	public AppAction(T app, ResourceBundle msg, String key) {
		super(msg, key);
		this.app = app;
	}


	/**
	 * Returns the application.
	 *
	 * @return The application.
	 */
	public T getApplication() {
		return app;
	}


}
