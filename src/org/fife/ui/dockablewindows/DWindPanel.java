/*
 * 10/21/2005
 *
 * DWindPanel.java - A panel containing a bunch of dockable windows in a
 * tabbed pane.
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
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;


/**
 * A panel containing a bunch of <code>DockableWindow</code>s contained
 * in a tabbed pane.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DWindPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTabbedPane tabbedPane;
	private TitlePanel titlePanel;

	private static final Color titlePanelBG1	= new Color(40,93,220);
	private static final Color titlePanelBG2	= new Color(200,200,255);


	/**
	 * Constructor.
	 */
	public DWindPanel() {
		setLayout(new BorderLayout());
		//tabbedPane = new JTabbedPane();
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM) {
			public void setUI(TabbedPaneUI ui) {
				// Keep using tabbed pane ui so laf stays the same,
				// but need to set a new one to pick up new tabbed
				// pane colors, fonts, etc.
				super.setUI(new DockedWindowTabbedPaneUI());
			}
		};
		add(tabbedPane);
		titlePanel = new TitlePanel("Hello world");
		tabbedPane.addChangeListener(titlePanel);
		add(titlePanel, BorderLayout.NORTH);
	}


	/**
	 * Adds the specified dockable window to the tabbed pane.
	 *
	 * @param window The dockable window to add.
	 * @return <code>true</code> always.
	 */
	public boolean addDockableWindow(DockableWindow window) {
		tabbedPane.addTab(window.getDockableWindowName(),
						window.getIcon(), window);
		// Force title panel to update as sometimes JTabbedPane doesn't
		// fire stateChanged() events (if active index doesn't change).
		titlePanel.stateChanged(null);
		return true;
	}


	/**
	 * Returns whether or not the specified dockable window is contained in
	 * this <code>DWindPanel</code>.
	 *
	 * @param window The dockable window to look for.
	 * @return The index in the tabbed pane of the dockable window, or
	 *         <code>-1</code> if it is not in this panel.
	 */
	public int containsDockableWindow(DockableWindow window) {
		int count = tabbedPane.getTabCount();
		for (int i=0; i<count; i++) {
			Component c = tabbedPane.getComponentAt(i);
			if (c.equals(window)) {
				return i;
			}
		}
		return -1; // Doesn't contain the specified dockable window.
	}


	/**
	 * Returns the number of dockable windows contained in this tabbed
	 * pane.
	 *
	 * @return The dockable window count in this tabbed pane.
	 */
	public int getDockableWindowCount() {
		return tabbedPane.getTabCount();
	}


	/**
	 * Returns an array containing the dockable windows in this tabbed pane.
	 *
	 * @return The dockable windows.
	 */
	public DockableWindow[] getDockableWindows() {
		int count = tabbedPane.getTabCount();
		DockableWindow[] windows = new DockableWindow[count];
		for (int i=0; i<count; i++) {
			windows[i] = (DockableWindow)tabbedPane.getComponentAt(i);
		}
		return windows;
	}


	/**
	 * Removes the specified dockable window from this tabbed pane.
	 *
	 * @param window The dockable window.
	 * @return Whether or not the window was successfully removed.
	 */
	public boolean removeDockableWindow(DockableWindow window) {
		int index = containsDockableWindow(window);
		if (index>-1) {
			tabbedPane.removeTabAt(index);
			// Force title panel to update as sometimes JTabbedPane doesn't
			// fire stateChanged() events (if active index doesn't change).
			titlePanel.stateChanged(null);
			return true;
		}
		return false;
	}


	/**
	 * Panel that displays the currently-active dockable windows' title.
	 */
	private class TitlePanel extends JPanel implements ChangeListener {

		private JLabel label;

		public TitlePanel(String title) {
			super(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
			label = new JLabel(title);
			label.setForeground(Color.WHITE);
			add(label);
		}

		public Dimension getMinimumSize() {
			// So we don't keep "dockable window" panels from getting small.
			Dimension d = getPreferredSize();
			d.width = 32;
			return d;
		}

		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			d.height = 20;
			return d;
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D)g;
			GradientPaint paint = new GradientPaint(
							0,0, titlePanelBG1,
							getWidth(),0, titlePanelBG2);
			Paint oldPaint = g2d.getPaint();
			g2d.setPaint(paint);
			Rectangle bounds = getBounds();
			g2d.fillRect(0,0, bounds.width,bounds.height);
			g2d.setPaint(oldPaint);
		}

		public void setTitle(String title) {
			label.setText(title);
		}

		public void stateChanged(ChangeEvent e) {
			int index = tabbedPane.getSelectedIndex();
			if (index>-1) {
				setTitle(tabbedPane.getTitleAt(index));
			}
		}

	}


}