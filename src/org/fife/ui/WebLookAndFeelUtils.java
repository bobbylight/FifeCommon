/*
 * 03/30/2013
 *
 * WebLookAndFeelUtils.java - Utility methods for applications looking to
 * support WebLookAndFeel.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Color;
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
 * Everything in this class works as of the WebLaF 1.4 preview release.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class WebLookAndFeelUtils {

	private static final String LAF_CLASS_NAME = "com.alee.laf.WebLookAndFeel";
	private static final String BUTTON_UI_CLASS_NAME = "com.alee.laf.button.WebButtonUI";
	private static final String STYLE_CONSTANTS_CLASS = "com.alee.laf.StyleConstants";


	/**
	 * Sets properties needed for toolbar buttons to look nice in
	 * WebLookAndFeel.
	 *
	 * @param toolBar The toolbar to update.
	 */
	public static void fixToolbarButtons(JToolBar toolBar) {

		try {

			for (int i=0; i<toolBar.getComponentCount(); i++) {
				Component comp = toolBar.getComponent(i);
System.out.println("--- " + comp);
				if (comp instanceof JButton) {
					JButton button = (JButton)comp;
					fixToolbarButtonImpl(button);
				}
			}

		} catch (RuntimeException re) { // FindBugs
			throw re;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private static final void fixToolbarButtonImpl(JButton button)
			throws Exception {

		ButtonUI ui = button.getUI();

		if (ui.getClass().getName().equals(BUTTON_UI_CLASS_NAME)) {

			Class clazz = ui.getClass();
			Class[] params = { boolean.class };
			Method m = clazz.getDeclaredMethod("setRolloverDecoratedOnly", params);
			m.invoke(ui, new Object[] { Boolean.TRUE });
			params = new Class[] { int.class };
			m = clazz.getMethod("setRound", params);
			ClassLoader cl = clazz.getClassLoader();
			clazz = Class.forName(STYLE_CONSTANTS_CLASS, true, cl);
			Field smallRound = clazz.getField("smallRound");
			int value = smallRound.getInt(null);
			m.invoke(ui, new Object[] { new Integer(value) });

		}

	}


	public static final void installWebLookAndFeelProperties(ClassLoader cl) {

		// Don't override non-UIResource borders!
		// This is only in our custom build, based off of 1.4, for now.
		//String honorBorders = WebLookAndFeel.PROPERTY_HONOR_USER_BORDERS;
		String honorBorders = "WebLookAndFeel.honorUserBorders";
		System.setProperty(honorBorders, "true");

		try {

			// Decorating frames is disabled as for some reason it sets our
			// initial size to 0, which breaks RSTA's addNotify() method.
			// Perhaps it has to do with the frame "fading in"?
			//WebLookandFeel.setDecorateFrames(true);
			//WebLookAndFeel.setDecorateDialogs(true);
			Class clazz = Class.forName(LAF_CLASS_NAME, true, cl);
			Class[] classes = { boolean.class };
			//Method m = clazz.getDeclaredMethod("setDecorateFrames", classes);
			//m.invoke(null, new Object[] { Boolean.TRUE });
			Method m = clazz.getDeclaredMethod("setDecorateDialogs", classes);
			m.invoke(null, new Object[] { Boolean.TRUE });
			
			//StyleConstants.shadeColor = new Color(0xc0c0ff);
			//StyleConstants.innerShadeColor = new Color(0xd0d0ff);
			clazz = Class.forName(STYLE_CONSTANTS_CLASS, true, cl);
//			Field shadeColor = clazz.getDeclaredField("shadeColor");
//			Field innerShadeColor = clazz.getDeclaredField("innerShadeColor");
//			shadeColor.set(null, new Color(0xc0c0ff));
//			innerShadeColor.set(null, new Color(0xd0d0ff));

			Field topBGColor = clazz.getDeclaredField("topBgColor");
			Field bottomBGColor = clazz.getDeclaredField("bottomBgColor");
			topBGColor.set(null, new Color(0xffefef));
			bottomBGColor.set(null, new Color(0xdfdfdf));

//			Field rolloverDecoratedOnly = clazz.getDeclaredField("rolloverDecoratedOnly");
//			rolloverDecoratedOnly.set(null, Boolean.TRUE);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public static final boolean isWebLookAndFeel(String laf) {
		return laf.equals(LAF_CLASS_NAME);
	}


	public static final boolean isWebLookAndFeelInstalled() {
		LookAndFeel laf = UIManager.getLookAndFeel();
		return isWebLookAndFeel(laf.getClass().getName());
	}


}