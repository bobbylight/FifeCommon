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


	/**
	 * Constructor.
	 */
	public DWindPanel() {
		setLayout(new BorderLayout());
		tabbedPane = new DockedTabbedPane();
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
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
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
			if (c==window) {
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
	 * Returns the dockable window at the specified index.
	 *
	 * @param index The index.
	 * @return The dockable window.
	 */
	public DockableWindow getDockableWindowAt(int index) {
		return (DockableWindow)tabbedPane.getComponentAt(index);
	}


	/**
	 * Refreshes the name of the specified dockable window tab.  This will
	 * also refresh the text in the title bar (since a dockable window's title
	 * defaults to its name if none is specified) if necessary.
	 *
	 * @param index The index of the dockable window to refresh.
	 * @see #refreshTabTitle(int)
	 */
	public void refreshTabName(int index) {
		if (index>=0 && index<tabbedPane.getTabCount()) {
			DockableWindow w = (DockableWindow)tabbedPane.getComponentAt(index);
			String name = w.getDockableWindowName();
			tabbedPane.setTitleAt(index, name);
			refreshTabTitle(index);
		}
	}


	/**
	 * Refreshes a dockable window tab's title.  All this does is check whether
	 * a dockable window is the selected one, and if it is, ensures that the
	 * title in the title pane is displaying the dockable window's current
	 * title.
	 *
	 * @param index The index of the dockable window to refresh.
	 * @see #refreshTabName(int)
	 */
	public void refreshTabTitle(int index) {
		if (index==tabbedPane.getSelectedIndex()) {
			DockableWindow w = (DockableWindow)tabbedPane.getComponentAt(index);
			String title = w.getDockableWindowTitle();
			titlePanel.setTitle(title);
		}
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
	 * The tabbed pane use to switch between multiple grouped docked windows.
	 */
	private class DockedTabbedPane extends JTabbedPane {

		public DockedTabbedPane() {
			super(BOTTOM);
		}

		protected void paintComponent(Graphics g) {
			// As of Java 6, still no way for custom LaF's to pick up on
			// Swing's (i.e. the OS's) default AA settings without
			// subclassing components.
			Graphics2D g2d = (Graphics2D)g;
			RenderingHints.Key key = RenderingHints.KEY_TEXT_ANTIALIASING;
			Object old = g2d.getRenderingHint(key);
			g2d.setRenderingHint(key, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			super.paintComponent(g);
			g2d.setRenderingHint(key, old);
		}

		public void setUI(TabbedPaneUI ui) {
			if (!(getUI() instanceof DockedWindowTabbedPaneUI)) {
				// Keep using tabbed pane ui so laf stays the same, but need to
				// set a new one to pick up new tabbed pane colors, fonts, etc.
				super.setUI(new DockedWindowTabbedPaneUI());
			}
			else {
				// At a minimum, set the font of the tabbed pane to what it
				// should be, as different LaFs have different default fonts
				// (for example, Metal's default font is bold, for some reason,
				// while Windows XP's is Tahoma.  Usually though it's "Sans
				// Serif").
				Font font = UIManager.getFont("TabbedPane.font");
				//System.out.println("... font==" + font.getFamily());
				if (font!=null) {
					setFont(font);
				}
			}
		}

	}


	/**
	 * Panel that displays the currently-active dockable windows' title.
	 */
	private class TitlePanel extends JPanel implements ChangeListener {

		private JLabel label;
		private Color gradient1;
		private Color gradient2;

		public TitlePanel(String title) {
			super(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
			refreshGradientColors();
			label = new JLabel(title);
			refreshLabelForeground();
			add(label);
		}

		/**
		 * Performs a gentler "darker" operation than Color.darker().
		 *
		 * @param c
		 * @return
		 */
		public Color darker(Color c) {
			final double FACTOR = 0.85;
			return new Color(Math.max((int)(c.getRed()  *FACTOR), 0), 
					Math.max((int)(c.getGreen()*FACTOR), 0),
					Math.max((int)(c.getBlue() *FACTOR), 0));
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

		private boolean getUseCustomColors() {
			String laf = UIManager.getLookAndFeel().getClass().getName();
			return laf.endsWith("WindowsLookAndFeel") ||
					laf.endsWith("MetalLookAndFeel");
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D)g;
			GradientPaint paint = new GradientPaint(
												0,0, gradient1,
												0,getHeight(),gradient2);
			Paint oldPaint = g2d.getPaint();
			g2d.setPaint(paint);
			Rectangle bounds = getBounds();
			g2d.fillRect(0,0, bounds.width,bounds.height);
			g2d.setPaint(oldPaint);
g2d.setColor(DockableWindowUtil.getDockableWindowBorderColor());
g2d.drawLine(0,0, bounds.width-1,0);
g2d.drawLine(0,0, 0,bounds.height-1);
g2d.drawLine(bounds.width-1,0, bounds.width-1,bounds.height-1);
g2d.drawLine(0,bounds.height-1, bounds.width-1,bounds.height-1);
		}

		public void setTitle(String title) {
			label.setText(title);
		}

		public void stateChanged(ChangeEvent e) {
			int index = tabbedPane.getSelectedIndex();
			if (index>-1) {
				DockableWindow w = getDockableWindowAt(index);
				setTitle(w.getDockableWindowTitle());
			}
		}

		private void refreshGradientColors() {
			if (getUseCustomColors()) {
				gradient1 = new Color(225,233,241);//200,200,255);
				gradient2 = new Color(153,180,209);//40,93,220);
			}
			else {
				gradient1 = UIManager.getColor("TextField.selectionBackground");
				if (gradient1==null) {
					gradient1 = UIManager.getColor("textHighlight");
					if (gradient1==null) {
						gradient1 = new Color(153,180,209);
					}
				}
				gradient2 = darker(gradient1);
			}
		}

		private void refreshLabelForeground() {
			if (getUseCustomColors()) {
				// Unfortunately we must force a reset of the Label's
				// foreground, even though its updateUI() should have done so,
				// since we had to install a non-ColorUIResource to get a
				// color change for Nimbus.
				Color c = UIManager.getColor("Label.foreground");
				if (c!=null) {
					label.setForeground(c);
				}
			}
			else {
				Color c = UIManager.getColor("TextField.selectionForeground");
				if (c==null) {
					c = UIManager.getColor("nimbusSelectedText"); // Nimbus!!!
					if (c==null) {
						c = UIManager.getColor("textHighlightText");
						if (c==null) {
							c = Color.black;
						}
					}
				}
				// Nimbus ignores ColorUIResources (!), but honors Colors, so
				// unfortunately we must ensure we have a true "Color" here.
				c = new Color(c.getRed(), c.getGreen(), c.getBlue());
				label.setForeground(c);
			}
		}

		public void updateUI() {
			super.updateUI();
			if (label!=null) {
				label.updateUI();
			}
			refreshGradientColors();
			if (label!=null) {
				refreshLabelForeground();
			}
		}

	}


}