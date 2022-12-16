/*
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.util;

import org.fife.ui.OS;

import javax.swing.*;


/**
 * Utilities to integrate better on macOS. All methods in this class do
 * nothing if the application isn't running on macOS.
 * <p>
 * Note that this class defines several public static constants, but they typically do not
 * need to be used directly; the static methods are usually all that is needed. The property
 * names are provided in case you need to programmatically query for any of them for any
 * reason.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class MacOSUtil {

	/**
	 * macOS-specific system property that determines the color to use for window title bars (light,
	 * dark, or match the system settings).
	 */
	public static final String PROPERTY_APPLICATION_APPEARANCE = "apple.awt.application.appearance";

	/**
	 * macOS-specific system property that sets the application name. This is displayed in the
	 * system menu bar. it must be set on the main thread, before any AWT components are
	 * instantiated.
	 */
	public static final String PROPERTY_APPLICATION_NAME = "apple.awt.application.name";

	/**
	 * macOS-specific client property that determines whether the title bar should be displayed.
	 * If this is {@code false}, the window content starts at the very top of the window,
	 * possibly overlapping the close/minimize/maximize buttons and the window title.
	 */
	public static final String PROPERTY_FULL_WINDOW_CONTENT = "apple.awt.fullWindowContent";

	/**
	 * macOS-specific client property that determines whether title bars arae rendered as
	 * transparent.
	 */
	public static final String PROPERTY_TRANSPARENT_TITLE_BAR = "apple.awt.transparentTitleBar";

	/**
	 * macOS-specific system property that sets whether to use the screen menu bar. This must
	 * be set on the mian thread, before any AWT components are instantiated.
	 */
	public static final String PROPERTY_USE_SCREEN_MENU_BAR = "apple.laf.useScreenMenuBar";

	/**
	 * macOS-specific client property that determines whether a window's title is rendered.
	 * Note that macOS always renders the window title in the color matching the current system
	 * theme (light or dark), which may not match a custom Look and Feel's colors if
	 * {@code PROPERTY_FULL_WINDOW_CONTENT} is set to {@code true}.
	 */
	public static final String PROPERTY_WINDOW_TITLE_VISIBLE = "apple.awt.windowTitleVisible";


	/**
	 * Private constructor to prevent instantiation.
	 */
	private MacOSUtil() {
		// Do nothing (comment for Sonar)
	}


	/**
	 * Returns whether we're running on macOS.
	 *
	 * @return Whether we're running on macOS.
	 */
	public static boolean isMacOs() {
		return OS.MAC_OS_X == OS.get();
	}


	/**
	 * Sets the color of the application's windows title bars. By default,
	 * the JVM always uses "light" window chrome on macOS (as of Java 17).
	 * <p>
	 * Note this must be set on the main thread, before any AWT classes are
	 * loaded, to take effect.
	 * <p>
	 * See <a href="https://bugs.openjdk.org/browse/JDK-8235363">
	 *     https://bugs.openjdk.org/browse/JDK-8235363</a> for more information.
	 *
	 * @param appearance The appearance to use. It is suggested to use
	 *        {@code SYSTEM} to match the system chrome.
	 */
	public static void setApplicationAppearance(AppAppearance appearance) {
		System.setProperty(PROPERTY_APPLICATION_APPEARANCE, appearance.value);
	}


	/**
	 * Sets the application name to display in the Mac menu bar. By default,
	 * the JVM always uses the name of the "main" class of the application.
	 * <p>
	 * Note this must be set on the main thread, before any AWT classes are
	 * loaded, to take effect.
	 *
	 * @param name The application name to display.
	 */
	public static void setApplicationName(String name) {
		System.setProperty(PROPERTY_APPLICATION_NAME, name);
	}


	/**
	 * Toggles whether the window content should extend into the title bar.
	 * This effectively hides the application title and moves the toolbar
	 * into the very top of the window.
	 * <p>
	 * See <a href="https://bugs.openjdk.org/browse/JDK-8211301">
	 *     https://bugs.openjdk.org/browse/JDK-8211301</a> for more information.
	 *
	 * @param rpc The frame or window to modify.
	 * @param full Whether full window content should be used.
	 */
	public static void setFullWindowContent(RootPaneContainer rpc, boolean full) {
		setFullWindowContent(rpc, full, !full);
	}


	/**
	 * Toggles whether the window content should extend into the title bar.
	 * <p>
	 * See <a href="https://bugs.openjdk.org/browse/JDK-8211301">
	 *     https://bugs.openjdk.org/browse/JDK-8211301</a> for more information.
	 *
	 * @param rpc The frame or window to modify.
	 * @param full Whether full window content should be used.
	 * @param titleVisible Whether the title should be visible in the title bar. Frequently if
	 *        {#code full} is {@code true} you'll want to set this to {@code true} as well;
	 *        otherwise you'd have to carefully ensure your content doesn't overlap with the
	 *        window title.
	 */
	public static void setFullWindowContent(RootPaneContainer rpc, boolean full, boolean titleVisible) {
		rpc.getRootPane().putClientProperty(PROPERTY_FULL_WINDOW_CONTENT, full);
		rpc.getRootPane().putClientProperty(PROPERTY_WINDOW_TITLE_VISIBLE, titleVisible);
		setTransparentTitleBar(rpc, full); // Also needed
	}


	/**
	 * Toggles whether the title bar of a window should be transparent.
	 * This results in a more "flat" look, especially with the FlatLaF "light"
	 * theme (less so with the "dark" theme).
	 * <p>
	 * See <a href="https://bugs.openjdk.org/browse/JDK-8211301">
	 *     https://bugs.openjdk.org/browse/JDK-8211301</a> for more information.
	 *
	 * @param rpc The frame or window to modify.
	 * @param transparent Whether the window's title bar sould be transparent.
	 */
	public static void setTransparentTitleBar(RootPaneContainer rpc, boolean transparent) {
		rpc.getRootPane().putClientProperty(PROPERTY_TRANSPARENT_TITLE_BAR, transparent);
	}


	/**
	 * Toggles whether to use the screen menu bar on macOS.
	 *
	 * @param use Whether to use the main screen menu bar.
	 */
	public static void setUseScreenMenuBar(boolean use) {
		String value = use ? "true" : "false";
		System.setProperty(PROPERTY_USE_SCREEN_MENU_BAR, value);
	}


	/**
	 * Enumeration of values for the application appearance (i.e. how
	 * the title bar looks for Windows).
	 */
	public enum AppAppearance {

		SYSTEM("system"),
		LIGHT("NSAppearanceNameAqua"),
		DARK("NSAppearanceNameDarkAqua");

		private final String value;

		AppAppearance(String value) {
			this.value = value;
		}
	}
}
