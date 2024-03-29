/*
 * 03/30/2013
 *
 * WebLookAndFeelUtils.java - Utility methods for applications looking to
 * support WebLookAndFeel.
 * Copyright (C) 2003 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;


/**
 * Utilities for using WebLookAndFeel.  It's super nice, but like Substance,
 * it assumes you're building your application from the ground up with it,
 * so we have to tweak it somewhat to make it look OK with an application like
 * RText that uses "standard" Swing paradigms.<p>
 *
 * Note that WebLookAndFeel is a little dangerous for applications to support,
 * since it uses lots of Sun-internal APIs and so may break at a moment's
 * notice.<p>
 *
 * WebLookAndFeel also requires Java 6+, so everything is done by reflection.
 * Everything in this class works as of the WebLaF 1.26 beta.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class WebLookAndFeelUtils {

	private static final String LAF_CLASS_NAME = "com.alee.laf.WebLookAndFeel";
	private static final String BUTTON_UI_CLASS_NAME = "com.alee.laf.button.WebButtonUI";
	private static final String MENU_BAR_STYLE_CLASS = "com.alee.laf.menu.WebMenuBarStyle";
	private static final String STYLE_CONSTANTS_CLASS = "com.alee.laf.StyleConstants";

	private static boolean decorateFrames = true;


	/**
	 * Private constructor to prevent instantiation.
	 */
	private WebLookAndFeelUtils() {
	}


	/**
	 * Sets properties needed for toolbar buttons to look nice in
	 * WebLookAndFeel.  Does nothing if WebLookAndFeel is not installed.
	 *
	 * @param toolBar The toolbar to update.
	 */
	public static void fixToolbar(JToolBar toolBar) {
		fixToolbar(toolBar, false);
	}


	/**
	 * Sets properties needed for toolbar buttons to look nice in
	 * WebLookAndFeel.  Does nothing if WebLookAndFeel is not installed.
	 *
	 * @param toolBar The toolbar to update.
	 * @param flatten Whether the toolbar should have a "flat" look.
	 */
	public static void fixToolbar(JToolBar toolBar, boolean flatten) {
		fixToolbar(toolBar, flatten, true);
	}


	/**
	 * Sets properties needed for toolbar buttons to look nice in
	 * WebLookAndFeel.  Does nothing if WebLookAndFeel is not installed.
	 *
	 * @param toolBar The toolbar to update.
	 * @param flatten Whether the toolbar should have a "flat" look.
	 * @param attached Whether the toolbar should have an "attached" look.
	 */
	public static void fixToolbar(JToolBar toolBar, boolean flatten,
			boolean attached) {

		if (WebLookAndFeelUtils.isWebLookAndFeelInstalled()) {

			try {

				for (int i=0; i<toolBar.getComponentCount(); i++) {
					Component comp = toolBar.getComponent(i);
					if (comp instanceof JButton button) {
						fixToolbarButtonImpl(button);
					}
				}

				Class<?> uiClazz = toolBar.getUI().getClass();
				ClassLoader cl = uiClazz.getClassLoader();

				if (flatten) {
					Method m = uiClazz.getDeclaredMethod(
							"setUndecorated", boolean.class);
					m.invoke(toolBar.getUI(), true);
				}

				// flatten => not painted at all
				else if (attached) {

					Class<?> styleClazz = Class.forName(
							"com.alee.laf.toolbar.ToolbarStyle", true, cl);

					Method m = uiClazz.getDeclaredMethod(
							"setToolbarStyle", styleClazz);
					Field f = styleClazz.getDeclaredField("attached");
					Object style = f.get(null);
					m.invoke(toolBar.getUI(), style);

				}

			} catch (RuntimeException re) { // FindBugs
				throw re;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}


	private static void fixToolbarButtonImpl(JButton button)
			throws Exception {

		ButtonUI ui = button.getUI();

		String clazzName = ui.getClass().getName();
		if (BUTTON_UI_CLASS_NAME.equals(clazzName)) {

			Class<?> uiClazz = ui.getClass();
			Method m = uiClazz.getDeclaredMethod("setRolloverDecoratedOnly",
					boolean.class);
			m.invoke(ui, true);
			m = uiClazz.getMethod("setRound", int.class);
			ClassLoader cl = uiClazz.getClassLoader();
			Class<?> clazz = Class.forName(STYLE_CONSTANTS_CLASS, true, cl);
			Field smallRound = clazz.getField("smallRound");
			int value = smallRound.getInt(null);
			m.invoke(ui, value);

			// By default, buttons have 4 pixel spacing on their left and
			// right, which looks ridiculous for toolbar buttons.  WebLaF
			// provides factory methods for icon-only buttons to work around
			// this, but we are avoiding WebLaF-specific APIs.
			m = uiClazz.getMethod("setLeftRightSpacing", int.class);
			m.invoke(ui, 0);

		}

	}


	/**
	 * Returns whether frames should be decorated.
	 *
	 * @return Whether frames should be decorated.
	 * @see #setDecorateFrames(boolean)
	 */
	public static boolean getDecorateFrames() {
		return decorateFrames;
	}


	/**
	 * Installs system properties specific to this Look and Feel.
	 */
	public static void installWebLookAndFeelProperties() {

		// Don't override non-UIResource borders!
		//String honorBorders = WebLookAndFeel.PROPERTY_HONOR_USER_BORDERS;
		String honorBorders = "WebLookAndFeel.honorUserBorders";
		System.setProperty(honorBorders, "true");

		try {

			// Decorating frames is disabled as for some reason it sets our
			// initial size to 0, which breaks RSTA's addNotify() method.
			// Perhaps it has to do with the frame "fading in"?
			//WebLookAndFeel.setDecorateFrames(true);
			//WebLookAndFeel.setDecorateDialogs(true);
			Class<?> clazz = Class.forName(LAF_CLASS_NAME, true, null);
			Method m;
			if (decorateFrames) {
				m = clazz.getDeclaredMethod("setDecorateFrames", boolean.class);
				m.invoke(null, Boolean.TRUE);
			}
			m = clazz.getDeclaredMethod("setDecorateDialogs", boolean.class);
			m.invoke(null, Boolean.TRUE);

			//clazz = Class.forName(STYLE_CONSTANTS_CLASS, true, cl);
			//Field rolloverDecoratedOnly = clazz.getDeclaredField("rolloverDecoratedOnly");
			//rolloverDecoratedOnly.set(null, Boolean.TRUE);

			if (decorateFrames) {
				clazz = Class.forName(MENU_BAR_STYLE_CLASS, true, null);
				Field f = clazz.getDeclaredField("undecorated");
				f.set(null, Boolean.TRUE);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * Returns whether the specified class name is for the Web Look and Feel.
	 *
	 * @param laf The class name.
	 * @return Whether that class name is for the Web Look and Feel.
	 */
	public static boolean isWebLookAndFeel(String laf) {
		return laf.equals(LAF_CLASS_NAME);
	}


	/**
	 * Returns whether the Web Look and Feel is installed.
	 *
	 * @return Whether the Web Look and Feel is installed.
	 */
	public static boolean isWebLookAndFeelInstalled() {
		LookAndFeel laf = UIManager.getLookAndFeel();
		return isWebLookAndFeel(laf.getClass().getName());
	}


	/**
	 * Sets whether frames should be decorated.
	 *
	 * @param decorateFrames Whether frames should be decorated.
	 * @see #getDecorateFrames()
	 */
	public static void setDecorateFrames(boolean decorateFrames) {
		WebLookAndFeelUtils.decorateFrames = decorateFrames;
	}


}
