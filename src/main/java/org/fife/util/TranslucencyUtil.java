/*
 * 09/29/2010
 *
 * TranslucencyUtil.java - Utilities for translucent Windows in Java 7+.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.util;

import java.awt.*;


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
 * your Look and Feel implements its own window decorations, such as Darcula or
 * Substance.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class TranslucencyUtil {

	private static TranslucencyUtil instance;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private TranslucencyUtil() {
		// Do nothing (comment for Sonar)
	}

	/**
	 * Returns the singleton instance of this class.
	 *
	 * @return The singleton instance.
	 */
	public static TranslucencyUtil get() {
		if (instance==null) {
			instance = new TranslucencyUtil();
		}
		return instance;
	}

	public float getOpacity(Window w) {

		float opacity = 1;

		// If translucency isn't supported, it must be 1f.
		if (isTranslucencySupported(false)) {
			opacity = w.getOpacity();
		}

		return opacity;
	}


	private boolean isDecorated(Window w) {

		if (w instanceof Dialog) {
			return !((Dialog)w).isUndecorated();
		}

		if (w instanceof Frame) {
			return !((Frame)w).isUndecorated();
		}

		// Direct subclasses of Window can't have decorations
		return true;
	}


	public boolean isTranslucencySupported(boolean perPixel) {

		GraphicsDevice.WindowTranslucency kind = perPixel ?
				GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT :
						GraphicsDevice.WindowTranslucency.TRANSLUCENT;
		return GraphicsEnvironment.getLocalGraphicsEnvironment().
				getDefaultScreenDevice().isWindowTranslucencySupported(kind);
	}


	public boolean setOpacity(Window w, float value) {

		if (isDecorated(w) || !isTranslucencySupported(false)) {
			return false;
		}

		w.setOpacity(value);
		return true;
	}


	public boolean setOpaque(Window w, boolean opaque) {
		if (isDecorated(w) || (!opaque && !isTranslucencySupported(true))) {
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
