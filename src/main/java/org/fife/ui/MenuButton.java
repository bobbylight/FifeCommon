/*
 * 07/14/2004
 *
 * MenuButton.java - A button that, when clicked, presents a popup menu.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.PopupMenuListener;


/**
 * A button that, when clicked, presents the user with a popup menu.
 *
 * @author Robert Futrell
 * @version 0.9
 */
public class MenuButton extends JButton {

	private static final long serialVersionUID = 1L;

	private JPopupMenu popupMenu;
	private CompoundIcon compoundIcon;


	/**
	 * Constructor.
	 */
	public MenuButton(Icon icon) {
		popupMenu = new JPopupMenu();
		setAction(new ButtonPressedAction());
		setIcon(icon); // Set AFTER setAction so it isn't overridden.
	}


	/**
	 * Adds a menu item to the popup menu.
	 *
	 * @param action The action to make into a menu item.
	 * @see #addMenuItem(JMenuItem)
	 * @see #addSeparator()
	 * @see #insertMenuItem(JMenuItem, int)
	 * @see #removeItem(int)
	 */
	public void addMenuItem(Action action) {
		addMenuItem(new JMenuItem(action));
	}


	/**
	 * Adds a menu item to the popup menu.
	 *
	 * @param menuItem The menu item to add.
	 * @see #addMenuItem(Action)
	 * @see #addSeparator()
	 * @see #insertMenuItem(JMenuItem, int)
	 * @see #removeItem(int)
	 */
	public void addMenuItem(JMenuItem menuItem) {
		popupMenu.add(menuItem);
	}


	/**
	 * Adds a listener to this menu button's popup menu.
	 *
	 * @param l The listener to add.
	 * @see #removePopupMenuListener(PopupMenuListener)
	 */
	public void addPopupMenuListener(PopupMenuListener l) {
		popupMenu.addPopupMenuListener(l);
	}


	/**
	 * Adds a separator to the popup menu.
	 * 
	 * @see #addMenuItem(JMenuItem)
	 * @see #insertMenuItem(JMenuItem, int)
	 * @see #removeItem(int)
	 */
	public void addSeparator() {
		popupMenu.addSeparator();
	}


	/**
	 * Sets the orientation of this component.  This is overridden to also
	 * update the orientation of the popup menu.
	 *
	 * @param o The new orientation.
	 */
	@Override
	public void applyComponentOrientation(ComponentOrientation o) {
		super.applyComponentOrientation(o);
		if (popupMenu!=null) {
			popupMenu.applyComponentOrientation(o);
		}
	}


	/**
	 * Returns the number of menu items and separators in the popup menu.
	 *
	 * @return The number of menu items and separators.
	 * @see #addMenuItem(JMenuItem)
	 * @see #addSeparator()
	 * @see #insertMenuItem(JMenuItem, int)
	 * @see #removeItem(int)
	 */
	public int getItemCount() {
		return popupMenu.getComponentCount();
	}


	/**
	 * Overridden to give just a little extra space since our icon is longer.
	 * FIXME:  Figure out why we really have to do this...
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		if (size!=null)
			size.width += CompoundIcon.SPACING;
		return size;
	}


	/**
	 * Inserts a menu item at the specified index.
	 *
	 * @param item The menu item to insert.
	 * @param index The index at which to add the menu item.
	 * @see #addMenuItem(JMenuItem)
	 * @see #addSeparator()
	 * @see #removeItem(int)
	 */
	public void insertMenuItem(JMenuItem item, int index) {
		popupMenu.insert(item, index);
	}


	/**
	 * Removes the menu item or separator at the specified index from this
	 * button's popup menu.
	 *
	 * @param index The index of the item to remove.
	 * @return The item removed.
	 * @throws IllegalArgumentException If the value of <code>pos&lt;0</code>,
	 *         or if the value of pos is greater than the number of items.
	 * @see #getItemCount()
	 * @see #addMenuItem(JMenuItem)
	 * @see #addSeparator()
	 */
	public Component removeItem(int index) {
		Component old = popupMenu.getComponent(index);
		popupMenu.remove(index);
		return old;
	}


	/**
	 * Removes the specified popup menu listener.
	 *
	 * @param l The listener to remove.
	 * @see #addPopupMenuListener(PopupMenuListener)
	 */
	public void removePopupMenuListener(PopupMenuListener l) {
		popupMenu.removePopupMenuListener(l);
	}


	/**
	 * Sets the icon to be used for this button.  This is overridden so that
	 * a little "down arrow" is put beside of the icon to signify that there
	 * is a pulldown menu.
	 *
	 * @param icon The icon to use for this button.
	 */
	@Override
	public void setIcon(Icon icon) {
		if (compoundIcon==null)
			compoundIcon = new CompoundIcon(icon);
		else
			compoundIcon.setOtherIcon(icon);
		super.setIcon(compoundIcon);
	}


	/**
	 * Overridden so the popup menu gets updated as well.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (popupMenu!=null) {// First time this is called, popupMenu is null.
			SwingUtilities.updateComponentTreeUI(popupMenu);
		}
	}


	/**
	 * Action performed when the menu button is pressed.
	 */
	private class ButtonPressedAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			Point location = getLocation();
			location.y = getHeight();
			popupMenu.applyComponentOrientation(
						MenuButton.this.getComponentOrientation());
			popupMenu.show(MenuButton.this, 0,location.y);
		}

	}


	/**
	 * An icon that combines another icon with a down-arrow icon.
	 */
	private static class CompoundIcon implements Icon {

		private Icon arrowIcon;
		private Icon otherIcon;
		private static final int SPACING	= 3;

		public CompoundIcon(Icon icon) {
			setOtherIcon(icon);
			arrowIcon = new DownArrowIcon();
		}

		public int getIconHeight() {
			if (otherIcon==null)
				return arrowIcon.getIconHeight();
			return Math.max(otherIcon.getIconHeight(),
						arrowIcon.getIconHeight());
		}

		public int getIconWidth() {
			if (otherIcon==null)
				return arrowIcon.getIconWidth();
			return otherIcon.getIconWidth() + SPACING +
					arrowIcon.getIconWidth();
		}

		private int _paintArrowIcon(Component c, Graphics g, int x, int y) {
			y = (c.getHeight()-arrowIcon.getIconHeight())/2;
			Graphics2D g2d = (Graphics2D)g;
			Object old = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_ON);
			arrowIcon.paintIcon(c, g2d, x,y);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, old);
			return x + arrowIcon.getIconWidth() + SPACING;
		}

		private int _paintOtherIcon(Component c, Graphics g, int x, int y) {
			if (otherIcon!=null) {
				otherIcon.paintIcon(c, g, x,y);
				x += otherIcon.getIconWidth() + SPACING;
			}
			return x;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (c.getComponentOrientation().isLeftToRight()) {
				x = _paintOtherIcon(c, g, x, y);
				_paintArrowIcon(c, g, x, y);
			}
			else {
				x = _paintArrowIcon(c, g, x, y);
				_paintOtherIcon(c, g, x, y);
			}
		}

		public void setOtherIcon(Icon otherIcon) {
			this.otherIcon = otherIcon;
		}

	}


	/**
	 * The arrow implying that there is a menu underneath this button.
	 */
	private static class DownArrowIcon implements Icon {

		int[] xcoord;
		int[] ycoord;
		private static final int SIZE		= 6;

		public DownArrowIcon() {
			xcoord = new int[3];
			ycoord = new int[3];
		}

		public int getIconHeight() {
			return SIZE;
		}

		public int getIconWidth() {
			return SIZE;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color color = UIManager.getColor("Label.foreground");
			if (color==null) { // The button's foreground, should be the same
				color = c.getForeground();
			}
			g.setColor(color);
			xcoord[0] = x;
			xcoord[1] = x + SIZE;
			// Use unsigned rshift to prevent overflow
			xcoord[2] = (xcoord[0] + xcoord[1])>>>1;//(xcoord[0] + xcoord[1])/2;
			ycoord[0] = ycoord[1] = y;
			ycoord[2] = y + SIZE;
			g.fillPolygon(xcoord,ycoord, 3);
		}

	}


}