package org.fife.ui.breadcrumbbar;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
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
 * <a href="http://forums.sun.com/thread.jspa?forumID=57&threadID=5362822">here</a>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ScrollableJPopupMenu extends JPopupMenu {

	private int visibleRowCount;
	private List children;
	JMenuItem previousItem = new JMenuItem("    ^");
	JMenuItem nextItem = new JMenuItem("    v");
	private Timer previousTimer = new Timer(DELAY, new MenuScrollAction(-1));
	private Timer nextTimer = new Timer(DELAY, new MenuScrollAction(1));
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
		enableEvents(java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK);
		visibleRowCount = rowCount;
		children = new ArrayList(rowCount);
		MenuMouseAdapter adapter = new MenuMouseAdapter();
		previousItem.putClientProperty(PROPERTY_TIMER, previousTimer);
		previousItem.addMouseListener(adapter);
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
	public void addSeparator() {
		addComponent(new JPopupMenu.Separator());
	}


	/**
	 * Overridden to apply the new orientation to our (possibly hidden)
	 * child components.
	 *
	 * @param o The new component orientation.
	 */
	public void applyComponentOrientation(ComponentOrientation o) {
		super.applyComponentOrientation(o);
		for (Iterator i=children.iterator(); i.hasNext(); ) {
			Component c = (Component)i.next();
			c.applyComponentOrientation(o);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public Dimension getPreferredSize() {
		refresh();
		return super.getPreferredSize();
	}


	/**
	 * Overridden to enable scrolling through the menu via the mouse wheel.
	 *
	 * @param e The event.
	 */
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
			for (int i=0; i<itemCount; i++) {
				add((Component)children.get(i));
			}
			int w = super.getPreferredSize().width;
			removeAll();

			firstItemIndex = Math.min(itemCount - visibleRowCount, firstItemIndex);
			firstItemIndex = Math.max(0, firstItemIndex);

			previousItem.setEnabled(firstItemIndex > 0);
			nextItem.setEnabled(firstItemIndex < itemCount - visibleRowCount);
			add(previousItem);
			for (int i=0; i<visibleRowCount && firstItemIndex+i<itemCount; i++) {
				add((Component)children.get(firstItemIndex + i));
			}
			add(nextItem);
			Dimension size = super.getPreferredSize();
			size.width = w;
			setSize(size);
			revalidate();

		}

		else {
			for (int i=0; i<itemCount; i++) {
				add((Component)children.get(i));
			}
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			refresh();
		}
		super.setVisible(visible);
	}


	private static class MenuMouseAdapter extends MouseAdapter {
		   
		public void mouseEntered(MouseEvent e) {
			JMenuItem item = (JMenuItem)e.getSource();
			Timer timer = (Timer)item.getClientProperty(PROPERTY_TIMER);
			timer.start();
		}

		public void mouseExited(MouseEvent e) {
			JMenuItem item = (JMenuItem)e.getSource();
			Timer timer = (Timer)item.getClientProperty(PROPERTY_TIMER);
			timer.stop();
		}
	}


	private class MenuScrollAction extends AbstractAction {

		private int increment;

		public MenuScrollAction(int increment) {
			this.increment = increment;
		}

		public void actionPerformed(ActionEvent e) {
			firstItemIndex += increment;
			refresh();
		}

	}


}