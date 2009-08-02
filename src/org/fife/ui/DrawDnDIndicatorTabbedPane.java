/*
 * 10/10/2005
 *
 * DrawDnDIndicatorTabbedPane.java - A tabbed pane that draws visual cues
 * indicating where a tab will be dropped onto a DnD-enabled tabbed pane.
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
package org.fife.ui;


/**
 * Tabbed panes who implement this interface will receive notification when
 * a drop operation is pending over them, so they can draw an indicator
 * rectangle where the dropped tab will occur.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public interface DrawDnDIndicatorTabbedPane {


	/**
	 * Clears the rectangle indicating where the new tab will be "dropped."
	 *
	 * @see #setDnDIndicatorRect
	 */
	public void clearDnDIndicatorRect();


	/**
	 * Sets the rectangle indicating where the new tab will be "dropped."
	 *
	 * @param x The x-location of the rectangle.
	 * @param y The y-location of the rectangle.
	 * @param width The width of the rectangle.
	 * @param height The height of the rectangle.
	 * @see #clearDnDIndicatorRect
	 */
	public void setDnDIndicatorRect(int x, int y, int width, int height);


}