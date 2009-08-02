/*
 * 10/27/2004
 *
 * FileChooserOwner.java - A class that "owns" an RTextFileChooser.
 * Copyright (C) 2004 Robert Futrell
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
package org.fife.ui.rtextfilechooser;


/**
 * A class that has an {@link RTextFileChooser}.  This interface is used by
 * {@link FileChooserFavoritesOptionPanel} to get the file chooser to
 * configure.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface FileChooserOwner {


	/**
	 * Returns the file chooser to configure.
	 *
	 * @return The file chooser.
	 */
	public RTextFileChooser getFileChooser();


}