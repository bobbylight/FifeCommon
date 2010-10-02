/*
 * 09/29/2010
 *
 * DummyTranslucencyUtil.java - Used on systems that don't support window
 * translucency.
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
	public float getOpacity(Window w) {
		return 1f;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isTranslucencySupported(boolean perPixel) {
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean setOpacity(Window w, float value) {
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean setOpaque(Window w, boolean opaque) {
		return false;
	}


}