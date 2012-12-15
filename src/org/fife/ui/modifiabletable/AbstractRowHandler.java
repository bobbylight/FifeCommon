/*
 * 12/14/2012
 *
 * AbstractRowHandler - Base class for RowHandler implementations.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.modifiabletable;


/**
 * A base class for <code>RowHandler</code> implementations.
 * 
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class AbstractRowHandler implements RowHandler {


	/**
	 * The default implementation always returns <code>true</code>, because
	 * typically <code>ModifiableTable</code>s will allow all their rows to be
	 * modified.  Subclasses can override.
	 */
	public boolean canModifyRow(int row) {
		return true;
	}


	/**
	 * Overriden to call {@link #canModifyRow(int)}.  This means by default,
	 * any row that can be modified can also be removed.  Subclasses can
	 * override.
	 */
	public boolean canRemoveRow(int row) {
		return canModifyRow(row);
	}


	/**
	 * The default implementation does nothing.  Subclasses can override.
	 */
	public void updateUI() {
	}


}