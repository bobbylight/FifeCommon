/*
 * 07/29/2005
 *
 * ModifiableTableListener.java - Listens for modifications to a
 * ModifiableTable.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.modifiabletable;

import java.util.EventListener;


/**
 * An implementor of this class gets notified of the addition, removal, or
 * modification of any rows of a <code>ModifiableTable</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface ModifiableTableListener extends EventListener {


	/**
	 * Called whenever the modified table has a row added, modified or
	 * removed.
	 *
	 * @param e An event describing the change.
	 */
	public void modifiableTableChanged(ModifiableTableChangeEvent e);


}