/*
 * 10/21/2005
 *
 * DockableWindowGroup.java - A panel containing a bunch of dockable windows
 * in a tabbed pane.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.dockablewindows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.table.JTableHeader;

import org.fife.ui.SubstanceUtils;
import org.fife.ui.UIUtil;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.dockablewindows.DockableWindowPanel.ContentPanel;


/**
 * A panel containing a bunch of <code>DockableWindow</code>s contained
 * in a tabbed pane.  Instances of this class contain all docked windows on
 * any edge of a <code>DockableWindowPanel</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DockableWindowGroup extends JPanel {

	private static final long serialVersionUID = 1L;

	private ContentPanel parent;
	private JTabbedPane tabbedPane;
	private TitlePanel titlePanel;


	/**
	 * Constructor.
	 */
	public DockableWindowGroup(ContentPanel parent) {
		this.parent = parent;
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
		// Setting selected index causes flicker in editor caret.
		//tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
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


	public void focusActiveDockableWindow() {
		DockableWindow dwind = getDockableWindowAt(tabbedPane.getSelectedIndex());
		dwind.focused();
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
			//window.setActive(false);
			// Force title panel to update as sometimes JTabbedPane doesn't
			// fire stateChanged() events (if active index doesn't change).
			titlePanel.stateChanged(null);
			return true;
		}
		return false;
	}


	/**
	 * Sets the selected dockable window.  Does nothing if the index is invalid.
	 *
	 * @param index The dockable window to select.
	 */
	public void setActiveDockableWindow(int index) {
		if (index>=0 && index<tabbedPane.getTabCount()) {
			tabbedPane.setSelectedIndex(index);
		}
	}


	/**
	 * The tabbed pane use to switch between multiple grouped docked windows.
	 */
	private class DockedTabbedPane extends JTabbedPane {

		private JPopupMenu popup;

		public DockedTabbedPane() {
			super(BOTTOM);
			enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		}

		private JPopupMenu getPopupMenu() {
			if (popup==null) {
				DockableWindowPanel dwindPanel= parent.getDockableWindowPanel();
				popup = Actions.createRedockPopupMenu(dwindPanel);
			}
			return popup;
		}

		@Override
		protected void paintComponent(Graphics g) {
			// As of Java 6, still no way for custom LaF's to pick up on
			// Swing's (i.e. the OS's) default AA settings without
			// subclassing components.
			Graphics2D g2d = (Graphics2D)g;
			RenderingHints old = UIUtil.setNativeRenderingHints(g2d);
			super.paintComponent(g);
			g2d.setRenderingHints(old);
		}

		@Override
		protected void processMouseEvent(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				if (e.isPopupTrigger()) {
					int x = e.getX();
					int y = e.getY();
					int index = indexAtLocation(x, y);
					if (index!=-1) {
						setSelectedIndex(index);
						DockableWindow dwind = (DockableWindow)getSelectedComponent();
						putClientProperty("DockableWindow", dwind);
						JPopupMenu popup = getPopupMenu();
						popup.show(this, x, y);
					}
				}
			}
			else {
				super.processMouseEvent(e);
			}
		}

		@Override
		public void setSelectedIndex(int index) {
			super.setSelectedIndex(index);
			final DockableWindow dwind = (DockableWindow)getSelectedComponent();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dwind.focused();
				}
			});
		}

		@Override
		public void setUI(TabbedPaneUI ui) {
			// Keep using tabbed pane ui so laf stays the same, but need to
			// set a new one to pick up new tabbed pane colors, fonts, etc.
			super.setUI(new DockedWindowTabbedPaneUI());
		}

	}


	/**
	 * Panel that displays the currently-active dockable windows' title.
	 */
	private class TitlePanel extends JPanel implements ChangeListener {

		private JLabel label;
		private JToolBar tb;
		private JButton minimizeButton;
		private Color gradient1;
		private Color gradient2;

		public TitlePanel(String title) {
			super(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
			refreshGradientColors();
			label = new JLabel(title);
			refreshLabelForeground();
			add(label);
			minimizeButton = new JButton(new MinimizeAction());
			minimizeButton.setOpaque(false);
			tb = new JToolBar();
			tb.setFloatable(false);
			tb.setOpaque(false);
			tb.setBorder(null);
			tb.add(minimizeButton);
			WebLookAndFeelUtils.fixToolbar(tb, true);
			add(tb, BorderLayout.LINE_END);
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

		@Override
		public Dimension getMinimumSize() {
			// So we don't keep "dockable window" panels from getting small.
			Dimension d = getPreferredSize();
			d.width = 32;
			return d;
		}

		@Override
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

		@Override
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
			gradient1 = gradient2 = null;
			if (getUseCustomColors()) {
				gradient1 = new Color(225,233,241);//200,200,255);
				gradient2 = new Color(153,180,209);//40,93,220);
			}
			else if (SubstanceUtils.isSubstanceInstalled()) {
				try {
					gradient1 = SubstanceUtils.getSubstanceColor("ultraLightColor");
					gradient2 = SubstanceUtils.getSubstanceColor("lightColor");
				} catch (RuntimeException re) { // FindBugs
					throw re;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (WebLookAndFeelUtils.isWebLookAndFeelInstalled()) {
				getWebLookAndFeelGradientColors();
			}
			if (gradient1==null) {
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

		private void getWebLookAndFeelGradientColors() {
			JTableHeader header = new JTableHeader();
			Class<?> clazz = header.getUI().getClass();
			try {
				Field f1 = clazz.getDeclaredField("topBgColor");
				Field f2 = clazz.getDeclaredField("bottomBgColor");
				gradient1 = (Color)f1.get(null);
				gradient2 = (Color)f2.get(null);
			} catch (Exception e) {
				gradient1 = gradient2 = null;
				e.printStackTrace();
				// We'll end up taking the (ugly) defaults
			}
		}

		private void refreshLabelForeground() {
			Color c = null;
			if (getUseCustomColors()) {
				// Unfortunately we must force a reset of the Label's
				// foreground, even though its updateUI() should have done so,
				// since we had to install a non-ColorUIResource to get a
				// color change for Nimbus.
				c = UIManager.getColor("Label.foreground");
			}
			else if (SubstanceUtils.isSubstanceInstalled()) {
				c = UIManager.getColor("Label.foreground");
//				try {
//					c = SubstanceUtils.getSubstanceColor("selectionForegroundColor");
//				} catch (RuntimeException re) { // FindBugs
//					throw re;
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
			else if (WebLookAndFeelUtils.isWebLookAndFeelInstalled()) {
				c = new JTableHeader().getForeground();
			}
			if (c==null) {
				c = UIManager.getColor("TextField.selectionForeground");
				if (c==null) {
					c = UIManager.getColor("nimbusSelectedText"); // Nimbus!!!
					if (c==null) {
						c = UIManager.getColor("textHighlightText");
						if (c==null) {
							c = Color.black;
						}
					}
				}
			}
			// Nimbus ignores ColorUIResources (!), but honors Colors, so
			// unfortunately we must ensure we have a true "Color" here.
			c = new Color(c.getRed(), c.getGreen(), c.getBlue());
			label.setForeground(c);
		}

		@Override
		public void updateUI() {
			super.updateUI();
			if (label!=null) {
				label.updateUI();
			}
			refreshGradientColors();
			if (label!=null) {
				refreshLabelForeground();
			}
			if (tb!=null) {
				WebLookAndFeelUtils.fixToolbar(tb, true);
			}
		}

		private class MinimizeAction extends AbstractAction {

			public MinimizeAction() {
				putValue(SHORT_DESCRIPTION, // tool tip
						DockableWindow.getString("Button.Minimize"));
				Icon icon = new ImageIcon(getClass().getResource("minimize.png"));
				putValue(SMALL_ICON, icon);
			}

			public void actionPerformed(ActionEvent e) {
				parent.setCollapsed(true);
			}

		}

	}


}