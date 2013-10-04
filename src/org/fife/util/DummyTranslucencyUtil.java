/*
 * 09/29/2010
 *
 * DummyTranslucencyUtil.java - Used on systems that don't support window
 * translucency.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.util;

import java.awt.Window;


/**
 * Returned when a system does not support translucency.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DummyTranslucencyUtil extends TranslucencyUtil {


	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getOpacity(Window w) {
		return 1f;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTranslucencySupported(boolean perPixel) {
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setOpacity(Window w, float value) {
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setOpaque(Window w, boolean opaque) {
		return false;
	}


}