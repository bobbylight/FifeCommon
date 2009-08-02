/*
 * 07/29/2005
 *
 * ModifiableTableListener.java - Listens for modifications to a
 * ModifiableTable.
 * Copyright (C) 2005 Robert Futrell
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