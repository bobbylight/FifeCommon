/*
 * 02/09/2006
 *
 * DockableWindow.java - A window that can be docked on any of the four
 * sides of a DockableWindowPanel.
 * Copyright (C) 2006 Robert Futrell
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
package org.fife.ui.dockablewindows;

import java.awt.LayoutManager;
import javax.swing.Icon;
import javax.swing.JPanel;


/**
 * A dockable window that is a panel either docked to one of the four sides
 * of a <code>DockableWindowPanel</code>, or in a floating window.
 *
 * @author Robert Futrell
 * @version 0.5
 * @see org.fife.ui.dockablewindows.DockableWindowPanel
 */
public class DockableWindow extends JPanel implements DockableWindowConstants {

	/**
	 * Property meaning the dockable window has changed its "active" state;
	 * that is, it has either been made visible or hidden.
	 */
	public static final String ACTIVE_PROPERTY	= "DockableWindowActive";

	/**
	 * Property fired when a dockable window's name changes.
	 */
	public static final String NAME_PROPERTY	= "DockableWindowName";

	/**
	 * Property fired when a dockable window's title changes.
	 */
	public static final String TITLE_PROPERTY	= "DockableWindowTitle";

	/**
	 * The position of the dockable window with respect to the main
	 * application window.  One of <code>TOP</code>, <code>LEFT</code>,
	 * <code>BOTTOM</code>, <code>RIGHT</code>, or <code>FLOATING</code>.
	 */
	private int position = LEFT;

	/**
	 * Whether this dockable window is active.
	 */
	private boolean active;

	/**
	 * The "name" of this dockable window (displayed in tabs, etc.).
	 */
	private String dockableWindowName;

	/**
	 * The "title" of this dockable window (displayed in the title bar).
	 * If this is <code>null</code>, the name will be displayed instead.
	 */
	private String dockableWindowTitle;

	/**
	 * The icon for this window.
	 */
	private Icon icon;


	/**
	 * Constructor.
	 */
	public DockableWindow() {
	}


	/**
	 * Constructor.
	 *
	 * @param layout The layout for this window's contents.
	 */
	public DockableWindow(LayoutManager layout) {
		super(layout);
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of this dockable window.
	 * @see #getDockableWindowName()
	 */
	public DockableWindow(String name) {
		setDockableWindowName(name);
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of this dockable window.
	 * @param layout The layout for this window's contents.
	 * @see #getDockableWindowName()
	 */
	public DockableWindow(String name, LayoutManager layout) {
		super(layout);
		setDockableWindowName(name);
	}


	/**
	 * Adds a listener to this dockable window.
	 *
	 * @param l The listener to add.
	 * @see #removeDockableWindowListener(DockableWindowListener)
	 */
	public void addDockableWindowListener(DockableWindowListener l) {
		listenerList.add(DockableWindowListener.class, l);
	}


	/**
	 * Notifies all listeners that this dockable window's position changed.
	 *
	 * @param e The event to fire.
	 */
	protected void firePositionChanged(DockableWindowEvent e) {
		// Guaranteed non-null.
		Object[] listeners = listenerList.getListenerList();
		for (int i=listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==DockableWindowListener.class) {
				((DockableWindowListener)listeners[i+1]).
								dockableWindowPositionChanged(e);
			}
		}
	}


	/**
	 * Notifies all listeners that this dockable window's position
	 * will change.
	 *
	 * @param e The event to fire.
	 */
	protected void firePositionWillChange(DockableWindowEvent e) {
		// Guaranteed non-null.
		Object[] listeners = listenerList.getListenerList();
		for (int i=listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==DockableWindowListener.class)
				((DockableWindowListener)listeners[i+1]).
								dockableWindowPositionWillChange(e);
		}
	}


	/**
	 * Returns the name of this dockable window.  The name is
	 * displayed on tabs, etc.
	 *
	 * @return The name of this dockable window.
	 * @see #setDockableWindowName(String)
	 * @see #getDockableWindowTitle()
	 */
	public String getDockableWindowName() {
		return dockableWindowName;
	}


	/**
	 * Returns the title of this dockable window.  The is displayed in the top
	 * colored area of the dockable window tabbed pane, or as the title in
	 * the floating window if this dockable window is floating.
	 *
	 * @return The title of this dockable window.  If no value has been set,
	 *         this will be the name of this dockable window.
	 * @see #setDockableWindowTitle(String)
	 * @see #getDockableWindowName()
	 */
	public String getDockableWindowTitle() {
		return dockableWindowTitle!=null ? dockableWindowTitle :
					getDockableWindowName();
	}


	/**
	 * Returns the icon to display beside the name of this dockable window
	 * in the application's interface.
	 *
	 * @return The icon for this dockable window.  This value may be
	 *         <code>null</code> to represent no icon.
	 * @see #setIcon(Icon)
	 */
	public Icon getIcon() {
		return icon;
	}


	/**
	 * Returns the location of this dockable window.
	 *
	 * @return One of <code>TOP</code>, <code>LEFT</code>, <code>BOTTOM</code>,
	 *         <code>RIGHT</code> or <code>FLOATING</code>.
	 * @see #setPosition
	 */
	public int getPosition() {
		return position;
	}


	/**
	 * Returns whether this dockable window is active (i.e., visible).
	 *
	 * @return Whether this dockable window is not active.
	 * @see #getPosition
	 * @see #setActive
	 */
	public boolean isActive() {
		return active;
	}


	/**
	 * Returns whether or not the specified position is valid.
	 *
	 * @param pos A position.
	 * @return Whether the specified position is valid.
	 */
	public static final boolean isValidPosition(int pos) {
		return pos==TOP || pos==LEFT || pos==BOTTOM ||
				pos==RIGHT || pos==FLOATING;
	}


	/**
	 * Removes a listener from this dockable window.
	 *
	 * @param l The listener to remove.
	 * @see #addDockableWindowListener
	 */
	public void removeDockableWindowListener(DockableWindowListener l) {
		listenerList.remove(DockableWindowListener.class, l);
	}


	/**
	 * Sets whether or not this dockable window should be "active;" i.e.,
	 * visible (I didn't want to use <code>setVisible</code> or
	 * <code>setEnabled</code> since they're already used for different
	 * functions by <code>JComponent</code>).
	 *
	 * @param active Whether this dockable window should be active.
	 * @see #isActive
	 */
	public void setActive(boolean active) {
		if (this.active!=active) {
			this.active = active;
			firePropertyChange(ACTIVE_PROPERTY, !active, active);
		}
	}


	/**
	 * Sets the name of this dockable window.  The name is displayed on tabs,
	 * etc.  This method fires a property change event of type
	 * {@link #NAME_PROPERTY}.
	 *
	 * @param name The name of this dockable window.
	 * @see #getDockableWindowName()
	 * @see #setDockableWindowTitle(String)
	 */
	public void setDockableWindowName(String name) {
		if ((name==null && dockableWindowName!=null) ||
				(name!=null && !name.equals(dockableWindowName))) {
			String old = dockableWindowName;
			dockableWindowName = name;
			firePropertyChange(NAME_PROPERTY, old, dockableWindowName);
		}
	}


	/**
	 * Sets the title of this dockable window.  The is displayed in the top
	 * colored area of the dockable window tabbed pane, or as the title in
	 * the floating window if this dockable window is floating.  This method
	 * fires a property change event of type {@link #TITLE_PROPERTY}.
	 *
	 * @param title The title of this dockable window.  If this is
	 *        <code>null</code>, the name of this window will be used.
	 * @see #getDockableWindowTitle()
	 * @see #setDockableWindowName(String)
	 */
	public void setDockableWindowTitle(String title) {
		if ((title==null && dockableWindowTitle!=null) ||
				(title!=null && !title.equals(dockableWindowTitle))) {
			String old = dockableWindowTitle;
			dockableWindowTitle = title;
			firePropertyChange(TITLE_PROPERTY, old, dockableWindowTitle);
		}
	}


	/**
	 * Sets the icon for this dockable window.
	 *
	 * @param icon The icon.
	 * @see #getIcon()
	 */
	public void setIcon(Icon icon) {
		this.icon = icon;
		// TODO: Update parent UI
	}


	/**
	 * Sets where this dockable window is displayed.  Note that this doesn't
	 * actually change the location of the dockable window; rather, it simply
	 * updates the dockable window's knowledge of where it is.
	 *
	 * @param newPos One of <code>TOP</code>, <code>LEFT</code>,
	 *        <code>BOTTOM</code>, <code>RIGHT</code>, or
	 *        <code>FLOATING</code>.
	 * @see #getPosition()
	 */
	public void setPosition(int newPos) {
		if (newPos!=position && isValidPosition(newPos)) {
			DockableWindowEvent e = new DockableWindowEvent(this,
											position, newPos);
			// Notify the container to update the position.
			firePositionWillChange(e);
			position = newPos;
			firePositionChanged(e);
		}
	}


}