/*
 * 10/21/2005
 *
 * DockedWindowPanel.java - A panel capable of having "windows" docked on any
 * of its four sides, as well as manage floating windows.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.dockablewindows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.plaf.SplitPaneUI;

import org.fife.ui.CleanSplitPaneUI;
import org.fife.ui.WebLookAndFeelUtils;


/**
 * A panel capable of having "windows" docked to any of its four sides,
 * as well as manage "floating" windows.  This can be used as the content pane
 * for applications wishing to have docked windows.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class DockableWindowPanel extends JPanel
				implements DockableWindowListener, PropertyChangeListener,
						DockableWindowConstants {

	public static final int LARGE_ON_SIDES	= 0;
	public static final int LARGE_ON_TOP_AND_BOTTOM	= 1;
	private ContentPanel[] panels;
	private FloatingWindow[] floatingWindows;
	private SortedSet<String> listeningTo;
	private int dockingStyle;
	protected List<DockableWindow> windowList;
	private int[] panelToLocationMap = { 0, 2, 1, 3,
								1, 3, 0, 2 };


	/**
	 * Constructor.
	 */
	public DockableWindowPanel() {

		panels = new ContentPanel[4];
		for (int i=0; i<4; i++) {
			panels[i] = new ContentPanel();
			panels[i].setDockableWindowsLocation(panelToLocationMap[i]);
		}
		for (int i=0; i<4-1; i++) {
			panels[i+1].add(panels[i]);
			//panels[i+1].addWindow(new TestWindow());
		}

		setDockingStyle(LARGE_ON_SIDES);

		// Add all of these panels under us.
		setLayout(new GridLayout(1,1));
		add(panels[3]);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		applyComponentOrientation(orientation);

		setBorder(BorderFactory.createEmptyBorder(0,3,2,3));
		listeningTo = new TreeSet<String>();

	}


	/**
	 * Adds a dockable window.
	 *
	 * @param window The window to add.
	 * @return Whether or not the window was added successfully.
	 * @see #removeDockableWindow
	 */
	public boolean addDockableWindow(DockableWindow window) {

		int pos = window.getPosition();
		if (!DockableWindow.isValidPosition(pos))
			throw new IllegalArgumentException("Invalid position");
		//window.setPosition(pos);

		addDockableWindowToList(window);

		if (!window.isActive()) {
			// Don't add him to a side yet.
			addListeners(window);
			return true;
		}

		switch (pos) {

			case FLOATING:
				if (floatingWindows==null) {
					floatingWindows = new FloatingWindow[1];
				}
				else {
					FloatingWindow[] temp = new FloatingWindow[floatingWindows.length+1];
					System.arraycopy(floatingWindows,0, temp,0, floatingWindows.length);
					floatingWindows = temp;
				}
				int current = floatingWindows.length - 1;
				floatingWindows[current] = createFloatingWindowFrame(window);
				Window focused = KeyboardFocusManager.
						getCurrentKeyboardFocusManager().getFocusedWindow();
				floatingWindows[current].setVisible(true);
				// Keep focus on "main" window.
				if (focused!=null) {
					focused.requestFocus();
				}
				addListeners(window);
				return true;

			default: // TOP, LEFT, BOTTOM or RIGHT.
				for (int p=0; p<4; p++) {
					if (panelToLocationMap[p]==pos) {
						ContentPanel cp = panels[p];
						boolean rc = cp.addDockableWindow(window);
						if (rc) {
							addListeners(window);
						}
						return rc;
					}
				}

		} // End of switch (pos).

		// Shouldn't get here, but if we do, it's an error.
		return false;

	}


	/**
	 * Adds the specified window to the list of windows managed by this
	 * <code>DockableWindowPanel</code>.
	 *
	 * @param window The window.
	 * @see #removeDockableWindowFromList(DockableWindow)
	 */
	private synchronized void addDockableWindowToList(DockableWindow window) {
		if (windowList==null)
			windowList = new ArrayList<DockableWindow>();
		windowList.add(window);
	}


	/**
	 * Adds this panel as a listener to the specified dockable window.
	 *
	 * @param window The dockable window.
	 */
	private void addListeners(DockableWindow window) {
		// We don't remove ourselves as listeners when removing a dockable
		// window, to allow ourselves to listen as apps send re-add
		// notifications.  So, only add ourselves as listeners once.
		if (!listeningTo.contains(window.getDockableWindowName())) {
			window.addDockableWindowListener(this);
			window.addPropertyChangeListener(this);
		}
	}


	/**
	 * Creates a frame to use for a floating dockable window.
	 *
	 * @param window The dockable window.
	 * @return A frame in which to place the dockable window.
	 */
	private FloatingWindow createFloatingWindowFrame(DockableWindow window) {
		FloatingWindow temp = new FloatingWindow(window);
		Window parentWind = SwingUtilities.getWindowAncestor(this);
		if (parentWind instanceof JFrame) {
			temp.setIconImage(((JFrame)parentWind).getIconImage());
		}
		temp.pack();
		return temp;
	}


	/**
	 * Called whenever a dockable window changed its preferred position.
	 * This method removes the dockable window from its old location and
	 * adds it to its new location.
	 *
	 * @param e The dockable window event.
	 */
	public void dockableWindowPositionChanged(DockableWindowEvent e) {
		DockableWindow window = (DockableWindow)e.getSource();
		// Remove window from old position.  Note that we should not
		// check the return value here, as the window may not currently
		// be active (thus removeDockableWindow returns false), but is about
		// to be moved.
		removeDockableWindow(window);
		// And add in the new position.
		if (!addDockableWindow(window))
			throw new InternalError("Couldn't add dockable window");
	}


	/**
	 * This method does nothing.
	 *
	 * @param e The dockable window event.
	 */
	public void dockableWindowPositionWillChange(DockableWindowEvent e) {
	}


	/**
	 * Focuses the specified dockable window group.  Does nothing if there
	 * are no dockable windows at the location specified.
	 *
	 * @param group The dockable window group to focus.  This must be a valid
	 *        value from {@link DockableWindowConstants}.
	 * @return Whether there were dockable windows at the location specified.
	 */
	public boolean focusDockableWindowGroup(int group) {
		if (group<0 || group>=4) {
			throw new IllegalArgumentException("group must be a valid value " +
					"from DockableWindowConstants.");
		}
		ContentPanel cp = panels[panelToLocationMap[group]];
		if (cp.windowPanel!=null) {
			if (cp.collapsed) {
				cp.setCollapsed(false);
			}
			cp.windowPanel.focusActiveDockableWindow();
			return true;
		}
		return false;
	}


	/**
	 * Returns the panel containing the "actual" content (e.g., the stuff
	 * that isn't a dockable window).
	 *
	 * @return The "actual" content.
	 */
	public JPanel getContentPanel() {
		return panels[0].getRegularContent();
	}


	/**
	 * @param splitPane The position of the split pane for which to get
	 *        its divider location; one of
	 *        <code>GUIApplicationConstants.TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code> or
	 *        <code>RIGHT</code>.
	 */
	public int getDividerLocation(int splitPane) {
		return panels[panelToLocationMap[splitPane]].getDividerLocation();
	}


	/**
	 * Returns all dockable windows.  This will return the actual dockable
	 * windows, not deep copies, so any changes made to the returned
	 * array will affect the actual dockable windows themselves.
	 *
	 * @return All dockable windows.  If no windows are being managed, a
	 *         zero-length array is returned.
	 * @see #addDockableWindow
	 * @see #removeDockableWindow
	 */
	public DockableWindow[] getDockableWindows() {
		DockableWindow[] windows = new DockableWindow[windowList.size()];
		return windowList.toArray(windows);
	}


	/**
	 * Returns the focused dockable window group.
	 *
	 * @return The focused window group, or <code>-1</code> if no dockable
	 *         window group is focused.
	 * @see DockableWindowConstants
	 */
	public int getFocusedDockableWindowGroup() {

		Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (focused==null) {
			return -1;
		}

		Container parent = focused.getParent();
		while (parent!=null) {
			if (parent instanceof DockableWindowGroup) {
				for (int i=0; i<4; i++) {
					int panelIndex = panelToLocationMap[i];
					if (panels[panelIndex]!=null && parent==panels[panelIndex].windowPanel) {
						return i;
					}
				}
				return -1;
			}
			else if (parent instanceof JLayeredPane) { // Assume in center
				return -1;
			}
			parent = parent.getParent();
		}

		return -1; // Never happens

	}


	/**
	 * Returns whether dockable windows are at the specified location.
	 *
	 * @param group A constant from {@link DockableWindowConstants}
	 * @return Whether dockable windows are at the specified location.
	 */
	public boolean hasDockableWindowGroup(int group) {
		if (group<0 || group>=4) {
			throw new IllegalArgumentException("Dockable window group must " +
					"be a constant from DockableWindowConstants");
		}
		return panels[panelToLocationMap[group]].getDockableWindowCount()>0;
	}


	/**
	 * The only property we care about is a dockable window becoming
	 * active or inactive (i.e., visible or not visible).
	 *
	 * @param e The property change event.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String name = e.getPropertyName();
		DockableWindow w = (DockableWindow)e.getSource();

		if (DockableWindow.ACTIVE_PROPERTY.equals(name)) {

			/*
			 * This is no longer a valid check, since we docked windows to be
			 * removed while we still listen to their events so we can re-add
			 * them...
			if (!windowList.contains(w)) {
				throw new InternalError("Dockable window list does not " +
					"contain window: " + w);
			}
			*/

			//boolean active = w.isActive();
			// FIXME:  Add window properly.  Removing, then re-adding
			// the window is an inefficient yet simple way to ensure
			// that the window becomes active/inactive as appropriate.
			removeDockableWindow(w);
			addDockableWindow(w);
		}

		else if (DockableWindow.NAME_PROPERTY.equals(name)) {
			boolean found = false;
			for (int i=0; i<panels.length; i++) {
				int index = panels[i].containsDockableWindow(w);
				if (index>-1) {
					panels[i].windowPanel.refreshTabName(index);
					found = true;
					break;
				}
			}
			if (!found) {
				for (int i=0; i<floatingWindows.length; i++) {
					if (w==floatingWindows[i].getDockableWindow()) {
						floatingWindows[i].refreshTitle(); // In case it changed too
					}
				}
			}
		}

		else if (DockableWindow.TITLE_PROPERTY.equals(name)) {
			boolean found = false;
			for (int i=0; i<panels.length; i++) {
				int index = panels[i].containsDockableWindow(w);
				if (index>-1) {
					panels[i].windowPanel.refreshTabTitle(index);
					found = true;
					break;
				}
			}
			if (!found) {
				// floatingWindows should never be null, but I've had it
				// happen while testing stuff out, so err on the side of
				// caution (in case I accidentally leave some buggy code
				// somewhere else).
				int count = floatingWindows==null ? 0 : floatingWindows.length;
				for (int i=0; i<count; i++) {
					if (w==floatingWindows[i].getDockableWindow()) {
						floatingWindows[i].refreshTitle(); // In case it changed too
					}
				}
			}
		}

	}


	/**
	 * Removes the content panel (e.g. the stuff that isn't dockable windows).
	 *
	 * @param contentPanel The content panel.
	 * @return Whether or not the removal was successful.
	 */
	public boolean removeContentPanel(Container contentPanel) {
		// Should only be two.
		Component[] comps = panels[0].getComponents();
		for (int i=0; i<comps.length; i++) {
			if (comps[i].equals(contentPanel)) {
				panels[0].remove(contentPanel);
				return true;
			}
		}
		return false;
	}


	/**
	 * Removes the specified dockable window from this panel's management.
	 *
	 * @param window The dockable window to remove.
	 * @return Whether or not the removal was successful.
	 * @see #addDockableWindow
	 */
	public boolean removeDockableWindow(DockableWindow window) {

		if (!removeDockableWindowFromList(window))
			return false; // Not in master list => just quit.

		// Keep listening to windows so user can toggle visibility off/on.
		//removeListeners(window);

		// NOTE:  We can't bail early even if the window isn't active,
		// as isActive() may be set to false before the window is
		// removed from the GUI.  In other words, isActive() is set to
		// false, then this method is called to physically remove the
		// window.
		//if (!window.isActive())
		//	return true;
		// See if window is docked on one of our 4 sides.
		for (int p=0; p<4; p++) {
			if (panels[p].removeDockableWindowImpl(window)) {
				return true;
			}
		}
		// If it wasn't, see if it was a floating dockable window.
		int numFloating = floatingWindows==null ? 0 :floatingWindows.length;
		for (int p=0; p<numFloating; p++) {
			DockableWindow possible = floatingWindows[p].getDockableWindow();
			if (possible==window) {
				floatingWindows[p].remove(0);
				floatingWindows[p].setVisible(false);
				floatingWindows[p].dispose();
				FloatingWindow[] temp = new FloatingWindow[numFloating-1];
				System.arraycopy(floatingWindows,0, temp,0, p);
				System.arraycopy(floatingWindows,p+1, temp,p, numFloating-p-1);
				floatingWindows = temp;
				return true;
			}
		}

		// Getting here means the dockable window was hidden (rignt?).
		return false;

	}


	/**
	 * Removes a dockable window from the list of managed dockable windows.
	 *
	 * @param window The dockable window.
	 * @return Whether or not the window was successfully removed.
	 * @see #addDockableWindowToList(DockableWindow)
	 */
	private boolean removeDockableWindowFromList(DockableWindow window) {
		return windowList.remove(window);
	}


	/**
	 * Sets the content panel (e.g., the non-dockable window stuff).
	 *
	 * @param contentPanel The new content panel.
	 */
	public void setContentPanel(Container contentPanel) {
		panels[0].add(contentPanel);
	}


	/**
	 * @param splitPane The split pane for which to set the divider
	 *        location; one of <code>DockableWindowConstants.TOP</code>,
	 *        <code>LEFT</code>, <code>BOTTOM</code> or
	 *        <code>RIGHT</code>.
	 * @param pos The position of the divider.
	 */
	public void setDividerLocation(int splitPane, int pos) {
		panels[panelToLocationMap[splitPane]].setDividerLocation(pos);
	}


	/**
	 * Sets the docking style.
	 *
	 * @param style Either <code>LARGE_ON_SIDES</code> or
	 *        <code>LARGE_ON_TOP_AND_BOTTOM</code>.
	 */
	public void setDockingStyle(int style) {
		if (dockingStyle!=style &&
			(style==LARGE_ON_SIDES || style==LARGE_ON_TOP_AND_BOTTOM))
		{
			dockingStyle = style;
			// TODO: Switch necessary panels.
		}
	}


	/**
	 * A panel containing either a single child or a split pane containing a
	 * tabbed pane of dockable windows and one other child.
	 * <code>MainContentPanel</code> contains 4 of these embedded in each other.
	 */
	final class ContentPanel extends JPanel {

		private JSplitPane splitPane;
		private DockableWindowGroup windowPanel;
		private Component mainContent;
		private int dockableWindowsLocation;
		private int dividerLocation;
		private boolean collapsed;
		private CollapsedPanel collapsedPanel;

		public ContentPanel() {
			super(new BorderLayout());
		}

		public boolean addDockableWindow(DockableWindow window) {
			// If this is our first dockable window...
			if (splitPane==null) {
				mainContent = getComponent(0);
				windowPanel = new DockableWindowGroup(this);
				windowPanel.addDockableWindow(window);
				int split;
				double resizeWeight = 0.0;
				Component comp1, comp2;
				switch (dockableWindowsLocation) {
					case TOP:
						split = JSplitPane.VERTICAL_SPLIT;
						resizeWeight = 0.0;
						comp1 = windowPanel;
						comp2 = mainContent;
						break;
					case LEFT:
						split = JSplitPane.HORIZONTAL_SPLIT;
						resizeWeight = 0.0;
						comp1 = windowPanel;
						comp2 = mainContent;
						break;
					case BOTTOM:
						split = JSplitPane.VERTICAL_SPLIT;
						resizeWeight = 1.0;
						comp1 = mainContent;
						comp2 = windowPanel;
						break;
					default: // RIGHT:
						split = JSplitPane.HORIZONTAL_SPLIT;
						resizeWeight = 1.0;
						comp1 = mainContent;
						comp2 = windowPanel;
						break;
				}
				remove(0); // Remove the original contents.
				splitPane = new JSplitPane(split, true, comp1, comp2) {
					@Override
					public void setUI(SplitPaneUI ui) {
						super.setUI(new CleanSplitPaneUI());
					}
				};
				splitPane.setResizeWeight(resizeWeight);
				splitPane.setDividerLocation(dividerLocation);
				splitPane.applyComponentOrientation(getComponentOrientation());
				add(splitPane);
				/*re*/validate();
				return true;
			}
			// We already have some dockable windows...
			boolean added = windowPanel.addDockableWindow(window);
			if (added && collapsed) {
				collapsedPanel.refreshDockableWindowButtons();
			}
			return added;
		}

		public int containsDockableWindow(DockableWindow window) {
			return windowPanel==null ? -1 :
					windowPanel.containsDockableWindow(window);
		}

		public int getDividerLocation() {
			return splitPane!=null ? splitPane.getDividerLocation() : -1;
		}

		int getDockableWindowCount() {
			return windowPanel==null ? 0 : windowPanel.getDockableWindowCount();
		}

		DockableWindowPanel getDockableWindowPanel() {
			return DockableWindowPanel.this;
		}

		// Returns the non-dockable window content this panel contains.
		public JPanel getRegularContent() {
			return (JPanel)mainContent;
		}

		public boolean removeDockableWindowImpl(DockableWindow window) {
			if (windowPanel!=null) {
				boolean rc = windowPanel.removeDockableWindow(window);
				if (collapsed) {
					collapsedPanel.refreshDockableWindowButtons();
				}
				// NASTY!  removeDockableWindow() above may have caused a
				// recursive call into here and set windowPanel to null already.
				// TODO: fix me.
				if (windowPanel!=null && windowPanel.getDockableWindowCount()==0) {
					this.removeAll(); // Remove whatever component we have
					if (splitPane!=null) { // Always true?
						this.dividerLocation = splitPane.getDividerLocation();
					}
					splitPane = null;
					windowPanel = null;
					collapsedPanel = null;
					collapsed = false;
					add(mainContent);
					revalidate();
				}
				return rc;
			}
			// We don't have a window panel => no dockable windows.
			return false;
		}

		public void setCollapsed(boolean collapsed) {
			if (collapsed!=this.collapsed) {
				this.collapsed = collapsed;
				if (collapsed) {
					this.dividerLocation = splitPane.getDividerLocation();
					remove(splitPane);
					if (collapsedPanel==null) {
						collapsedPanel = new CollapsedPanel(dockableWindowsLocation);
					}
					switch (dockableWindowsLocation) {
						case TOP:
							add(collapsedPanel, BorderLayout.NORTH);
							add(splitPane.getBottomComponent());
							break;
						case LEFT:
							add(collapsedPanel, BorderLayout.WEST);
							add(splitPane.getRightComponent());
							break;
						case BOTTOM:
							add(collapsedPanel, BorderLayout.SOUTH);
							add(splitPane.getTopComponent());
							break;
						default: // RIGHT:
							add(collapsedPanel, BorderLayout.EAST);
							add(splitPane.getLeftComponent());
							break;
					}
				}
				else {
					while (getComponentCount()>0) { // Should be 2
						remove(0);
					}
					// Add main component back to split pane
					switch (dockableWindowsLocation) {
						case TOP:
						case LEFT:
							splitPane.setBottomComponent(mainContent);
							break;
						case BOTTOM:
						default: // RIGHT:
							splitPane.setTopComponent(mainContent);
							break;
					}
					add(splitPane);
					if (splitPane!=null) { // Always true?
						splitPane.setDividerLocation(dividerLocation);
					}
				}
				revalidate();
				repaint();
			}
		}

		public void setDividerLocation(int location) {
			if (location!=dividerLocation) {
				dividerLocation = location;
				if (splitPane!=null) {
					splitPane.setDividerLocation(location);
				}
			}
		}

		public void setDockableWindowsLocation(int location) {
			dockableWindowsLocation = location;
		}

		@Override
		public void updateUI() {
			super.updateUI();
			if (splitPane!=null && !splitPane.isDisplayable()) {
				SwingUtilities.updateComponentTreeUI(splitPane);
			}
			if (collapsedPanel!=null && !collapsedPanel.isDisplayable()) {
				SwingUtilities.updateComponentTreeUI(collapsedPanel);
			}
		}

		/**
		 * A panel with buttons for each docked window in this collapsed panel.
		 * Displayed when the user clicks the "minimized" button in a
		 * DockableWindowGroup.
		 */
		private class CollapsedPanel extends JPanel implements MouseListener {

			private JToolBar toolbar;
			private JPopupMenu contextMenu;

			CollapsedPanel(int dockableWindowLocation) {
				setLayout(new BorderLayout());
				int orientation = (dockableWindowLocation==DockableWindow.TOP ||
						dockableWindowLocation==DockableWindow.BOTTOM) ?
								JToolBar.HORIZONTAL : JToolBar.VERTICAL;
				toolbar = new JToolBar(orientation);
				toolbar.setOpaque(false);
				toolbar.setFloatable(false);
				toolbar.setBorder(null);
				//toolbar.add(new JButton(new RestoreAction()));

				refreshDockableWindowButtons();

				boolean isHorizontal = orientation==JToolBar.HORIZONTAL;
				add(toolbar, isHorizontal ?
						BorderLayout.LINE_END : BorderLayout.NORTH);
				setBorder(isHorizontal ? BorderFactory.createEmptyBorder(0, 0, 0, 5) :
					BorderFactory.createEmptyBorder(5, 0, 0, 0));

			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					JButton popupButton = (JButton)e.getSource();
					if (contextMenu==null) {
						contextMenu = Actions.createRedockPopupMenu(
												DockableWindowPanel.this);
					}
					contextMenu.show(popupButton, e.getX(), e.getY());
				}
			}

			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			public int refreshDockableWindowButtons() {
				while (toolbar.getComponentCount()>0) {
					toolbar.getComponent(0).removeMouseListener(this);
					toolbar.remove(0);
				}
				for (int i=0; i<windowPanel.getDockableWindowCount(); i++) {
					DockableWindow dwind = windowPanel.getDockableWindowAt(i);
					Icon icon = dwind.getIcon();
					JButton b = new JButton(icon);
					b.setToolTipText(dwind.getDockableWindowName());
					b.setOpaque(false);
					b.putClientProperty("DockableWindow", dwind);
					final int index = i;
					b.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							windowPanel.setActiveDockableWindow(index);
							setCollapsed(false);
						}
					});
					b.addMouseListener(this);
					toolbar.add(b);
				}
				WebLookAndFeelUtils.fixToolbar(toolbar, true);
				toolbar.revalidate();
				return toolbar.getComponentCount();
			}

			@Override
			public void updateUI() {
				super.updateUI();
				if (toolbar!=null) {
					WebLookAndFeelUtils.fixToolbar(toolbar, true);
				}
			}

		}

	}


}