/*
 * 10/21/2005
 *
 * DockedWindowPanel.java - A panel capable of having "windows" docked on any
 * of its four sides, as well as manage floating windows.
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
package org.fife.ui.dockablewindows;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.SplitPaneUI;

import org.fife.ui.CleanSplitPaneUI;


/**
 * A panel capable of having "windows" docked to any of its four sides,
 * as well as manage "floating" windows.
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
	private int dockingStyle;
	protected List windowList;
	private int[] panelToLocationMap = { 0, 2, 1, 3,
								1, 3, 0, 2 };


	/**
	 * Constructor.
	 */
	public DockableWindowPanel() {

		panels = new ContentPanel[4];
		for (int i=0; i<4; i++) {
			panels[i] = new ContentPanel(new GridLayout(1,1));
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
				floatingWindows[current].setVisible(true);
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
			windowList = new ArrayList();
		windowList.add(window);
	}


	/**
	 * Adds this panel as a listener to the specified dockable window.
	 *
	 * @param window The dockable window.
	 * @see #removeListeners(DockableWindow)
	 */
	private void addListeners(DockableWindow window) {
		window.addDockableWindowListener(this);
		window.addPropertyChangeListener(this);
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
	 * Returns the panel containing the "actual" content (e.g., the stuff
	 * that isn't a dockable window).
	 *
	 * @return The "acutal" content.
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
		windows = (DockableWindow[])windowList.toArray(windows);
		return windows;
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
			if (!windowList.contains(w)) {
				throw new InternalError("Dockable window list does not " +
					"contain window: " + w);
			}
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
				for (int i=0; i<floatingWindows.length; i++) {
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
		removeListeners(window);

		// NOTE:  We can't bail early even if the window isn't active,
		// as isActive() may be set to false before the window is
		// removed from the GUI.  In other words, isActive() is set to
		// false, then this method is called to physically remove the
		// window.
		//if (!window.isActive())
		//	return true;
		// See if window is docked on one of our 4 sides.
		for (int p=0; p<4; p++) {
			if (panels[p].removeDockableWindow(window)) {
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
	 * Removes this panel as a listener of the specified dockable window.
	 *
	 * @param window The dockable window.
	 * @see #addListeners(DockableWindow)
	 */
	private void removeListeners(DockableWindow window) {
		window.removeDockableWindowListener(this);
		window.removePropertyChangeListener(this);
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
	 *        location; one of <code>GUIApplicationConstants.TOP</code>,
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
	 * A panel containing either a single child or a split pane
	 * containing a tabbed pane of dockable windows and one other
	 * child.  <code>MainContentPanel</code> contains
	 * 4 of these embedded in each other.
	 */
	private static final class ContentPanel extends JPanel {

		private JSplitPane splitPane;
		private DWindPanel windowPanel;
		private int dockableWindowsLocation;
		private int dividerLocation;

		public ContentPanel(LayoutManager lm) {
			super(lm);
		}

		public boolean addDockableWindow(DockableWindow window) {
			// If this is our first dockable window...
			if (splitPane==null) {
				windowPanel = new DWindPanel();
				windowPanel.addDockableWindow(window);
				int split;
				double resizeWeight = 0.0;
				Component comp1, comp2;
				switch (dockableWindowsLocation) {
					case TOP:
						split = JSplitPane.VERTICAL_SPLIT;
						resizeWeight = 0.0;
						comp1 = windowPanel;
						comp2 = getComponent(0);
						break;
					case LEFT:
						split = JSplitPane.HORIZONTAL_SPLIT;
						resizeWeight = 0.0;
						comp1 = windowPanel;
						comp2 = getComponent(0);
						break;
					case BOTTOM:
						split = JSplitPane.VERTICAL_SPLIT;
						resizeWeight = 1.0;
						comp1 = getComponent(0);
						comp2 = windowPanel;
						break;
					default: // RIGHT:
						split = JSplitPane.HORIZONTAL_SPLIT;
						resizeWeight = 1.0;
						comp1 = getComponent(0);
						comp2 = windowPanel;
						break;
				}
				remove(0); // Remove the original contents.
				splitPane = new JSplitPane(split, true, comp1, comp2) {
					public void setUI(SplitPaneUI ui) {
						super.setUI(new CleanSplitPaneUI());
					}
				};
				splitPane.setResizeWeight(resizeWeight);
				splitPane.setDividerLocation(dividerLocation);
				splitPane.applyComponentOrientation(getComponentOrientation());
				add(splitPane);
				validate();
				return true;
			}
			// We already have some dockable windows...
			return windowPanel.addDockableWindow(window);
		}

		public int containsDockableWindow(DockableWindow window) {
			return windowPanel==null ? -1 :
					windowPanel.containsDockableWindow(window);
		}

		public int getDividerLocation() {
			return splitPane!=null ? splitPane.getDividerLocation() : -1;
		}

		// Returns the non-dockable window content this panel contains.
		public JPanel getRegularContent() {
			if (splitPane==null) {
				return (JPanel)getComponent(0);
			}
			switch (dockableWindowsLocation) {
				case TOP:
				case LEFT:
					return (JPanel)splitPane.getRightComponent();
				case BOTTOM:
				case RIGHT:
					return (JPanel)splitPane.getLeftComponent();
			}
			// We never actually get here.
			throw new InternalError("Invalid state in getRegularContent");
		}

		public boolean removeDockableWindow(DockableWindow window) {
			if (windowPanel!=null) {
				boolean rc = windowPanel.removeDockableWindow(window);
				if (windowPanel.getDockableWindowCount()==0) {
					Component content = getRegularContent();
					this.remove(splitPane);
					splitPane = null;
					windowPanel = null;
					add(content);
					validate();
				}
				return rc;
			}
			// We don't have a window panel => no dockable windows.
			return false;
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
	
	}


}