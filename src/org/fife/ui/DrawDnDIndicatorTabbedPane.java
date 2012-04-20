/*
 * 10/10/2005
 *
 * DrawDnDIndicatorTabbedPane.java - A tabbed pane that draws visual cues
 * indicating where a tab will be dropped onto a DnD-enabled tabbed pane.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
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