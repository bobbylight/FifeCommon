/*
 * 09/29/2010
 *
 * Java7TranslucencyUtil.java - Utilities for translucent Windows in Java 7+.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.util;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
//import java.awt.GraphicsDevice.WindowTranslucency;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Uses the "official" API for setting window opacity introduced in Java 7.
 * <p>See 
 * <a href="http://download.oracle.com/javase/tutorial/uiswing/misc/trans_shaped_windows.html">
 * here</a> for more information.<p>
 * 
 * NOTE: Java 7 introduced extra rules not included in the Java 6 com.sun API;
 * windows can not have their opacity values changed while displayable, and
 * they also cannot be non-opaque if they are decorated.  The Java 6 API did
 * not impose these restrictions.  This makes this API virtually useless unless
 * you want a window to stay a certain degree of translucency the entire time
 * it is visible.  Sorry.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class Java7TranslucencyUtil extends TranslucencyUtil {


	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getOpacity(Window w) {

		float opacity = 1;

		// If translucency isn't supported, it must be 1f.
		if (isTranslucencySupported(false)) {
			try {
				Method m = Window.class.getMethod("getOpacity");
				opacity = ((Float)m.invoke(w)).floatValue();
			} catch (RuntimeException re) { // FindBugs - don't catch RE's
				throw re;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return opacity;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTranslucencySupported(boolean perPixel) {

		/*
		 * If we didn't require reflection to allow compilation by Java 1.4:
		WindowTranslucency kind = perPixel ?
				WindowTranslucency.PERPIXEL_TRANSLUCENT :
						WindowTranslucency.TRANSLUCENT;
		return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().
				getDefaultScreenDevice().isWindowTranslucencySupported(kind);
		*/

		String fieldName = perPixel ? "PERPIXEL_TRANSLUCENT" : "TRANSLUCENT";
		boolean supported = false;

		try {

			Field transField = null;

			// An enum that should exist in Java 7.
			Class<?> enumClazz = Class.forName(
							"java.awt.GraphicsDevice$WindowTranslucency");
			Field[] fields = enumClazz.getDeclaredFields();
			for (int i=0; i<fields.length; i++) {
				if (fieldName.equals(fields[i].getName())) {
					transField = fields[i];
					break;
				}
			}

			if (transField!=null) {

				GraphicsEnvironment env = GraphicsEnvironment.
											getLocalGraphicsEnvironment();
				GraphicsDevice device = env.getDefaultScreenDevice();
				Class<?> deviceClazz = device.getClass();

				// A method that should exist in Java 7
				Method m = deviceClazz.getMethod(
									"isWindowTranslucencySupported",
									enumClazz);
				Boolean res = (Boolean)m.invoke(device, transField.get(null));
				supported = res.booleanValue();

			}

		} catch (RuntimeException re) { // FindBugs - don't catch RE's
			throw re;
		} catch (Exception e) {
			e.printStackTrace();
			supported = false; // FindBugs again - non-empty catch block
		}

		return supported;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setOpacity(Window w, float value) {

		if (!isTranslucencySupported(false)) {
			return false;
		}

		boolean supported = true;

		try {
			Method m = Window.class.getMethod("setOpacity", float.class);
			m.invoke(w, new Object[] { new Float(value) });
		} catch (RuntimeException re) { // FindBugs - don't catch RE's
			//re.printStackTrace();
			throw re;
		} catch (Exception e) {
			//e.printStackTrace();
			supported = false; // FindBugs again - non-empty catch block
		}

		return supported;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setOpaque(Window w, boolean opaque) {
		if (!opaque && !isTranslucencySupported(true)) {
			return false;
		}
		if (opaque) {
			w.setBackground(Color.white);
		}
		else {
			w.setBackground(new Color(0,0,0, 0));
		}
		return true;
	}


}