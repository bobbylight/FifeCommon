/*
 * 09/29/2010
 *
 * Java6TranslucencyUtil.java - Utilities for translucent Windows in Java 6u10+.
 * Copyright (C) 2010 Robert Futrell
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
	public float getOpacity(Window w) {

		float opacity = 1;

		// If translucency isn't supported, it must be 1f.
		if (isTranslucencySupported(false)) {
			try {
				Class clazz = Class.forName(CLASS_NAME);
				Method m = clazz.getDeclaredMethod("getWindowOpacity",
									new Class[] { Window.class });
				opacity = ((Float)m.invoke(null, new Object[] { w })).
															floatValue();
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
	public boolean isTranslucencySupported(boolean perPixel) {

		String fieldName = perPixel ? "PERPIXEL_TRANSLUCENT" : "TRANSLUCENT";
		boolean supported = false;

		try {

			Field transField = null;

			Class enumClazz = Class.forName(CLASS_NAME + "$Translucency");
			Field[] fields = enumClazz.getDeclaredFields();
			for (int i=0; i<fields.length; i++) {
				if (fieldName.equals(fields[i].getName())) {
					transField = fields[i];
					break;
				}
			}

			if (transField!=null) {
				Class awtUtilClazz = Class.forName(CLASS_NAME);
				Method m = awtUtilClazz.getDeclaredMethod(
								"isTranslucencySupported",
								new Class[] { enumClazz });
				Boolean res = (Boolean)m.invoke(null, new Object[] {
											transField.get(null) });
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
	public boolean setOpacity(Window w, float value) {

		if (!isTranslucencySupported(false)) {
			return false;
		}

		boolean supported = true;

		try {
			Class clazz = Class.forName(CLASS_NAME);
			Method m = clazz.getDeclaredMethod("setWindowOpacity",
								new Class[] { Window.class, float.class });
			m.invoke(null, new Object[] { w, new Float(value) });
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
	public boolean setOpaque(Window w, boolean opaque) {

		if (!opaque && !isTranslucencySupported(true)) {
			return false;
		}

		try {
			Class clazz = Class.forName(CLASS_NAME);
			Method m = clazz.getDeclaredMethod("setWindowOpaque",
							new Class[] { Window.class, boolean.class });
			m.invoke(null, new Object[] { w, new Boolean(opaque) });
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}


}