/*
 * 09/29/2010
 *
 * Java7TranslucencyUtil.java - Utilities for translucent Windows in Java 7+.
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
 * here</a> for more information.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class Java7TranslucencyUtil extends TranslucencyUtil {


	/**
	 * {@inheritDoc}
	 */
	public float getOpacity(Window w) {

		float opacity = 1;

		// If translucency isn't supported, it must be 1f.
		if (isTranslucencySupported(false)) {
			try {
				Method m = Window.class.getMethod("getOpacity", null);
				opacity = ((Float)m.invoke(w, null)).floatValue();
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
			Class enumClazz = Class.forName(
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
				Class deviceClazz = device.getClass();

				// A method that should exist in Java 7
				Method m = deviceClazz.getMethod(
									"isWindowTranslucencySupported",
									new Class[] { enumClazz });
				Boolean res = (Boolean)m.invoke(device,
									new Object[] { transField.get(null) });
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
			Method m = Window.class.getMethod("setOpacity",
											new Class[] { float.class });
			m.invoke(w, new Object[] { new Float(value) });
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
		if (opaque) {
			w.setBackground(Color.white);
		}
		else {
			w.setBackground(new Color(0,0,0, 0));
		}
		return true;
	}


}