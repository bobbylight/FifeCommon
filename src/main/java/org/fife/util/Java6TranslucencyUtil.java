/*
 * 09/29/2010
 *
 * Java6TranslucencyUtil.java - Utilities for translucent Windows in Java 6u10+.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.util;

import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Utilities for getting and setting the translucency of windows for
 * pre-Java 7.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class Java6TranslucencyUtil extends TranslucencyUtil {

	/**
	 * The class that handles window transparency in 6u10.
	 */
	private static final String CLASS_NAME = "com.sun.awt.AWTUtilities";


	public Java6TranslucencyUtil() {
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getOpacity(Window w) {

		float opacity = 1;

		// If translucency isn't supported, it must be 1f.
		if (isTranslucencySupported(false)) {
			try {
				Class<?> clazz = Class.forName(CLASS_NAME);
				Method m = clazz.getDeclaredMethod("getWindowOpacity",
									Window.class);
				opacity = ((Float)m.invoke(null, w)).floatValue();
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

		String fieldName = perPixel ? "PERPIXEL_TRANSLUCENT" : "TRANSLUCENT";
		boolean supported = false;

		try {

			Field transField = null;

			Class<?> enumClazz = Class.forName(CLASS_NAME + "$Translucency");
			Field[] fields = enumClazz.getDeclaredFields();
			for (int i=0; i<fields.length; i++) {
				if (fieldName.equals(fields[i].getName())) {
					transField = fields[i];
					break;
				}
			}

			if (transField!=null) {
				Class<?> awtUtilClazz = Class.forName(CLASS_NAME);
				Method m = awtUtilClazz.getDeclaredMethod(
						"isTranslucencySupported", enumClazz);
				Boolean res = (Boolean)m.invoke(null, transField.get(null));
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
			Class<?> clazz = Class.forName(CLASS_NAME);
			Method m = clazz.getDeclaredMethod("setWindowOpacity",
								Window.class, float.class);
			m.invoke(null, w, new Float(value));
		} catch (RuntimeException re) { // FindBugs - don't catch RE's
			throw re;
		} catch (Exception e) {
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

		try {
			Class<?> clazz = Class.forName(CLASS_NAME);
			Method m = clazz.getDeclaredMethod("setWindowOpaque",
							Window.class, boolean.class);
			m.invoke(null, w, new Boolean(opaque));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}


}