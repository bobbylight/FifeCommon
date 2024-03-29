/*
 * 09/26/2009
 *
 * ScrollableJPopupMenu.java - A popup menu that will allow the user to scroll
 * through its contents if its contents get too long.
 * Copyright (C) 2009 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.breadcrumbbar;

import org.fife.ui.UIUtil;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;


/**
 * A <code>JPopupMenu</code> that allows the user to scroll through its
 * elements if there are more than a certain amount.  This is useful for menus
 * with (possibly) too many menu items to display on the user's screen
 * comfortably.<p>
 *
 * The user can define the maximum number of menu items to display.  If more
 * than this number of components are added to the menu, then "up" and "down"
 * arrows are added to the top and bottom of the menu, which can be used to
 * scroll through the larger list of items.  The mouse wheel can also be used
 * to scroll through the menu items.<p>
 *
 * If the number of menu items added is less than the threshold, this menu
 * acts just like a standard <code>JPopupMenu</code>.<p>
 *
 * This class is based off of code on Sun's Java forums, posted by
 * DarrylBurke
 * <a href="http://forums.sun.com/thread.jspa?forumID=57&amp;threadID=5362822">here</a>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ScrollableJPopupMenu extends JPopupMenu {

	private int visibleRowCount;
	private List<Component> children;
	private JMenuItem previousItem;
	private JMenuItem nextItem;
	private int firstItemIndex;

	private static final int DEFAULT_ROW_COUNT	= 15;
	private static final int DELAY				= 100;
	private static final String PROPERTY_TIMER	= "ScrollableJPopupMenu.timer";


	/**
	 * Constructor.
	 */
	public ScrollableJPopupMenu() {
		this(DEFAULT_ROW_COUNT);
	}


	/**
	 * Constructor.
	 *
	 * @param rowCount The number of rows that will be displayed before
	 *        scrolling arrows are drawn.
	 */
	public ScrollableJPopupMenu(int rowCount) {

		String root = "org/fife/ui/breadcrumbbar/";
		if (UIUtil.isLightForeground(getForeground())) {
			root += "dark/";
		}

		ClassLoader cl = ScrollableJPopupMenu.class.getClassLoader();
		URL url = cl.getResource(root + "up.png");
		Icon upIcon = new ImageIcon(url);
		url = cl.getResource(root + "down.png");
		Icon downIcon = new ImageIcon(url);

		enableEvents(java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK);
		visibleRowCount = rowCount;
		children = new ArrayList<>(rowCount);
		MenuMouseAdapter adapter = new MenuMouseAdapter();
		previousItem = new ArrowMenuItem(upIcon);
		Timer previousTimer = new Timer(DELAY, new MenuScrollAction(-1));
		previousItem.putClientProperty(PROPERTY_TIMER, previousTimer);
		previousItem.addMouseListener(adapter);
		nextItem = new ArrowMenuItem(downIcon);
		Timer nextTimer = new Timer(DELAY, new MenuScrollAction(1));
		nextItem.putClientProperty(PROPERTY_TIMER, nextTimer);
		nextItem.addMouseListener(adapter);
		refresh();
	}


	/**
	 * This method should be used to add items to this popup menu, not the
	 * standard <code>add</code> methods.
	 *
	 * @param c The component to add.
	 */
	public void addComponent(Component c) {
		children.add(c);
	}


	/**
	 * Overridden so we can keep track of what is added to the popup menu.
	 */
	@Override
	public void addSeparator() {
		addComponent(new JPopupMenu.Separator());
	}


	/**
	 * Overridden to apply the new orientation to our (possibly hidden)
	 * child components.
	 *
	 * @param o The new component orientation.
	 */
	@Override
	public void applyComponentOrientation(ComponentOrientation o) {
		super.applyComponentOrientation(o);
		for (Component c : children) {
			c.applyComponentOrientation(o);
		}
	}


	@Override
	public Dimension getPreferredSize() {
		refresh();
		return super.getPreferredSize();
	}


	/**
	 * Overridden to enable scrolling through the menu via the mouse wheel.
	 *
	 * @param e The event.
	 */
	@Override
	protected void processMouseWheelEvent(MouseWheelEvent e) {
		if (getComponent(0)==previousItem) { // i.e., scroll arrows are visible
			int amt = e.getUnitsToScroll()>0 ? 1 : -1;
			firstItemIndex += amt;
			refresh();
		}
		super.processMouseWheelEvent(e);
	}


	private void refresh() {

		removeAll();
		int itemCount = children.size();

		if (itemCount>visibleRowCount) {

			// Determine the best width for the popup.
			for (Component child : children) {
				add(child);
			}
			int w = super.getPreferredSize().width;
			removeAll();

			firstItemIndex = Math.min(itemCount-visibleRowCount, firstItemIndex);
			firstItemIndex = Math.max(0, firstItemIndex);

			previousItem.setEnabled(firstItemIndex > 0);
			nextItem.setEnabled(firstItemIndex < itemCount - visibleRowCount);
			add(previousItem);
			for (int i=0; i<visibleRowCount && firstItemIndex+i<itemCount; i++) {
				add(children.get(firstItemIndex + i));
			}
			add(nextItem);
			Dimension size = super.getPreferredSize();
			size.width = w;
			setSize(size);
			revalidate();
			repaint(); // Needed to refresh arrow menu items

		}

		else {
			for (Component child : children) {
				add(child);
			}
		}

	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			refresh();
		}
		super.setVisible(visible);
	}


	/**
	 * A menu item that's just a "down" arrow, denoting the menu has more items than are visible.
	 */
	private static class ArrowMenuItem extends JMenuItem {

		private Icon arrowIcon;
		private Icon disabledArrowIcon;

		ArrowMenuItem(Icon icon) {
			// We only temporarily set the menu item's standard icon, just so
			// we can use Swing to get our "disabled" version.  Then we clear
			// it, so we can draw the icon ourselves, in the text area.
			super(icon);
			arrowIcon = icon;
			disabledArrowIcon = getDisabledIcon();
			setIcon(null);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Icon icon = isEnabled() ? arrowIcon : disabledArrowIcon;
			int x = (getWidth() - icon.getIconWidth()) / 2;
			int y = (getHeight() - icon.getIconHeight()) / 2;
			icon.paintIcon(this, g, x, y);
		}

	}


	/**
	 * Listens for mousse events in the menu.
	 */
	private static final class MenuMouseAdapter extends MouseAdapter {

		@Override
		public void mouseEntered(MouseEvent e) {
			JMenuItem item = (JMenuItem)e.getSource();
			Timer timer = (Timer)item.getClientProperty(PROPERTY_TIMER);
			timer.start();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			JMenuItem item = (JMenuItem)e.getSource();
			Timer timer = (Timer)item.getClientProperty(PROPERTY_TIMER);
			timer.stop();
		}
	}


	/**
	 * Programmatically scrolls the menu.
	 */
	private class MenuScrollAction extends AbstractAction {

		private int increment;

		MenuScrollAction(int increment) {
			this.increment = increment;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			firstItemIndex += increment;
			refresh();
		}

	}


}
