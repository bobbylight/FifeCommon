/*
 * 10/21/2005
 *
 * DockedWindowTabbedPaneUI.java - UI used by the tabbed panes holding
 * dockable windows in a DockableWindowPanel.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.dockablewindows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

import org.fife.ui.SubstanceUtils;


/**
 * The UI for tabbed panes holding dockable windows in a
 * {@link DockableWindowPanel}.  This UI is designed to resemble tabbed panes
 * in Microsoft Visual Studio 2008.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see org.fife.ui.dockablewindows.DockableWindow
 * @see org.fife.ui.dockablewindows.DockableWindowPanel
 */
class DockedWindowTabbedPaneUI extends BasicTabbedPaneUI {

	/**
	 * A list of the (possibly) cropped titles for each tab.
	 */
	private List<String> croppedTitlesList;

	private Paint unselectedTabPaint;

	private static final int SELECTED_TAB_BOOST				= 1;


	/**
	 * Constructor.
	 */
	public DockedWindowTabbedPaneUI() {
		croppedTitlesList = new ArrayList<String>();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int calculateTabHeight(int tabPlacement, int tabIndex,
									int fontHeight) {
		int height = super.calculateTabHeight(tabPlacement,tabIndex,fontHeight);
		if (tabPane.getSelectedIndex()==tabIndex) {
			height += SELECTED_TAB_BOOST;
		}
		return height;
	}


	/**
	 * Invoked by <code>installUI</code> to create a layout manager
	 * object to manage the <code>JTabbedPane</code>.
	 *
	 * @return A layout manager object.
	 * @see TabbedPaneLayout
	 * @see javax.swing.JTabbedPane#getTabLayoutPolicy
	 */
	@Override
	protected LayoutManager createLayoutManager() {
		return new PluginTabbedPaneLayout();
	}


	/**
	 * Returns an instance of this UI for the specified tabbed pane.
	 *
	 * @param c The tabbed pane.
	 * @return An instance of this UI.
	 */
	public static ComponentUI createUI(JComponent c) {
		return new DockedWindowTabbedPaneUI();
	}


	/**
	 * Returns the minimum size for the tabbed pane.  This is overridden so
	 * that the tabbed pane can be given a very small width.  Tabbed panes'
	 * layout managers by default give them a minimum width equal to one of
	 * their tabs' widths, but we don't want to do that.
	 *
	 * @param c The tabbed pane.
	 * @return The minimum size for this tabbed pane.
	 */
	@Override
	public Dimension getMinimumSize(JComponent c) {
		return new Dimension(60, 64);
	}


	/**
	 * Caches insets, colors, fonts, etc.  This method is overridden so we
	 * can set custom values for insets.
	 */
	@Override
	protected void installDefaults() {

		// Nimbus sets this value to false, but we need it to be true, so that
		// our custom tabs get painted.  The private field
		// BasicTabbedPaneUI#tabsOpaque gets set to this value, and it is used
		// to determine whether to paint the tabs' backgrounds.
		UIManager.put("TabbedPane.tabsOpaque", Boolean.TRUE);

		super.installDefaults();

		tabInsets = new Insets(2,2,2,2);
		contentBorderInsets = new Insets(1,1,4,1);
		tabAreaInsets = new Insets(2, 4, 0, 4);

	}


	/**
	 * Arranges the text, icon, etc. on a tab, and caches the "cropped"
	 * version of the tab's title, if necessary.
	 */
	@Override
	protected void layoutLabel(int tabPlacement, 
						FontMetrics metrics, int tabIndex,
						String title, Icon icon,
						Rectangle tabRect, Rectangle iconRect, 
						Rectangle textRect, boolean isSelected ) {

		textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

		View v = getTextViewForTab(tabIndex);
		if (v != null)
			tabPane.putClientProperty("html", v);

		String croppedTitle = null;
		try {
			croppedTitle = SwingUtilities.layoutCompoundLabel(
											tabPane,
											metrics, title, icon,
											SwingUtilities.CENTER,
											SwingUtilities.CENTER,
											SwingUtilities.CENTER,
											SwingUtilities.TRAILING,
											tabRect,
											iconRect,
											textRect,
											textIconGap);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			// Workaround for bug in some older 1.6 JRE's, which manifests
			// itself seemingly only when a RTL locale is used (such as
			// Arabic), and the "plugins" area of a split pane is made so
			// small that the plugins' names on the tabs has to be abbreviated
			// into "...".  If this happens on startup, it can cause some bad
			// problems.
			croppedTitle = "...";
		}

		int croppedCount = croppedTitlesList.size();
		int tabCount = tabPane.getTabCount();
		if (croppedCount<tabCount) {
			for (int i=croppedCount; i<tabCount; i++) {
				croppedTitlesList.add("");
			}
		}
		croppedTitlesList.set(tabIndex, croppedTitle);

		tabPane.putClientProperty("html", null);

	}


	@Override
	protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
									int selectedIndex, 
									int x, int y, int w, int h) {
	}


	@Override
	protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
									int selectedIndex, 
									int x, int y, int w, int h) {

		// Top line, touching bottom of contents
		g.setColor(DockableWindowUtil.getDockableWindowBorderColor());
		y = y + h - contentBorderInsets.bottom;
		g.drawLine(x,y, x+w-1,y);

		// Space between contents and tabs
		g.setColor(UIManager.getColor("TabbedPane.highlight"));
		g.fillRect(x+1, y+1, w-1, contentBorderInsets.bottom-1);

		// Bottom line, touching the tabs
		g.setColor(DockableWindowUtil.getDockableWindowBorderColor());
		y += contentBorderInsets.bottom - 1;
		if ((selectedIndex<0)) { // No tab selected (never happens?)
			g.drawLine(x,y, x+w-1,y);
		}
		else {
			// Break line to show visual connection to selected tab
			Rectangle selRect = getTabBounds(selectedIndex, calcRect);
			int x2 = selRect.x - 2;
			if (selectedIndex==0) {
				x2++;
			}
			g.drawLine(x,y, x2,y);
			if (selRect.x+selRect.width < x+w-2) {
				g.drawLine(selRect.x+selRect.width,y, x+w-1,y);
			}
		}

	}


	@Override
	protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
										int selectedIndex, 
										int x, int y, int w, int h) {
		g.setColor(DockableWindowUtil.getDockableWindowBorderColor());
//		g.drawLine(x, y, x, y+h-1);
int y2 = y + h - 1;// - contentBorderInsets.bottom;
g.drawLine(x,y, x,y2);
	}


	@Override
	protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
										int selectedIndex, 
										int x, int y, int w, int h) {
		g.setColor(DockableWindowUtil.getDockableWindowBorderColor());
//		g.drawLine(x+w-1, y, x+w-1, y+h-1);
int y2 = y + h - 1;// - contentBorderInsets.bottom;
g.drawLine(x+w-1,y, x+w-1,y2);
	}

    @Override
	protected void paintTab(Graphics g, int tabPlacement,
            Rectangle[] rects, int tabIndex, 
            Rectangle iconRect, Rectangle textRect) {
    	super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
    }
 
	/**
	 * Paints the background of a tab.
	 */
	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement,
			int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		if (isSelected) {
			Color c = UIManager.getColor("TabbedPane.highlight");
			if (SubstanceUtils.isSubstanceInstalled()) {
				try {
					c = SubstanceUtils.getSubstanceColor(
							SubstanceUtils.EXTRA_LIGHT_COLOR);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			g.setColor(c);
			g.fillRect(x,y, w,h);
		}
		else {
			Graphics2D g2d = (Graphics2D)g;
			Paint old = g2d.getPaint();
			if (unselectedTabPaint==null) {
				if (SubstanceUtils.isSubstanceInstalled()) {
					try {
						unselectedTabPaint = SubstanceUtils.getSubstanceColor(
								SubstanceUtils.ULTRA_LIGHT_COLOR);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (unselectedTabPaint==null) {
					unselectedTabPaint = new GradientPaint(0,0, Color.LIGHT_GRAY,
							0,h/2, UIManager.getColor("TabbedPane.highlight"));
				}
			}
			g2d.setPaint(unselectedTabPaint);
			// Transform is needed in case we're using the gradient.
			AffineTransform trans = g2d.getTransform();
			g2d.translate(0, y);
			g2d.fillRect(x, 0, w, h);
			g2d.setTransform(trans);
			g2d.setPaint(old);
		}
	}


	/**
	 * Paints the border of a tab.
	 */
	@Override
	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
						int x, int y, int w, int h, boolean isSelected) {

		// NOTE: We assume the tab placement is JTabbedPane.BOTTOM.
		//g.setColor(UIManager.getColor("TabbedPane.shadow"));
		int x2 = x + w - 1;
		g.setColor(DockableWindowUtil.getDockableWindowBorderColor());

		// Draw the bottom and right of the border.
		int y2 = y + h - 1;
		g.drawLine(x,y2, x2,y2);
		g.drawLine(x2,y2, x2,y);

		// Only draw the left if we're the first tab.
		if (tabIndex==0) {
			g.drawLine(x,y, x,y2);
		}

		if (!isSelected) {

			// Draw the "rest" of the left side of the selected tab, if we're
			// the tab to its left (since no tab but tab 0 draws its left-hand
			// side).
			int selectedIndex = tabPane.getSelectedIndex();
			if (tabIndex==selectedIndex-1) {
				g.drawLine(x2,y2, x2,y+maxTabHeight-1);
			}

			// Draw a highlight beside one of our sides, to make things look
			// a little nicer (mimics VS 2008).
			g.setColor(UIManager.getColor("TabbedPane.highlight"));
			if (tabIndex<tabPane.getSelectedIndex()) {
				x = x2 - 1;
			}
			else {
				if (tabIndex==0) {
					x++;
				}
			}
			g.drawLine(x,y, x,y2-1);

		}

	}


	/**
	 * Paints the text on a tab (the tab's title).
	 */
	@Override
	protected void paintText(Graphics g, int tabPlacement,
						Font font, FontMetrics metrics, int tabIndex,
						String title, Rectangle textRect, 
						boolean isSelected) {

		// Get our (probably) cropped title; don't use the uncropped one.
		title = croppedTitlesList.get(tabIndex);
		g.setFont(font);

		Color fg = tabPane.getForegroundAt(tabIndex);
		if (isSelected) {
			fg = UIManager.getColor("Label.foreground");
			if (fg==null) {
				fg = UIManager.getColor("textText");
				if (fg==null) {
					fg = java.awt.SystemColor.textText;
				}
			}
		}
		else {
			fg = UIManager.getColor("Label.disabledForeground");
			if (fg==null) {
				fg = UIManager.getColor("textInactiveText");
				if (fg==null) {
					fg = Color.GRAY;
				}
			}
		}

		g.setColor(fg);
		g.drawString(title, textRect.x, textRect.y+metrics.getAscent());

	} 


	/**
	 * Tab layout for a plugin tabbed pane.  All tabs area always in a single
	 * run (row).  If there is not enough room for all of the tabs in one run,
	 * the space available is divided equally among all of the tabs.<p>
	 *
	 * Much of this code is stolen from <code>BasicTabbedPaneUI</code>'s
	 * layout subclasses.
	 */
	class PluginTabbedPaneLayout extends TabbedPaneLayout {

		public PluginTabbedPaneLayout() {
		}

		@Override
		protected int preferredTabAreaHeight(int tabPlacement, int width) {
			return calculateMaxTabHeight(tabPlacement);
		}

		@Override
		protected int preferredTabAreaWidth(int tabPlacement, int height) {
			return calculateMaxTabWidth(tabPlacement);
		}

		@Override
		public void layoutContainer(Container parent) {

			int tabPlacement = tabPane.getTabPlacement();
			Insets insets = tabPane.getInsets();
			int selectedIndex = tabPane.getSelectedIndex();
			Component visibleComponent = getVisibleComponent();

			calculateLayoutInfo();

			if (selectedIndex < 0) {
				if (visibleComponent != null) {
					// The last tab was removed, so remove the component
					setVisibleComponent(null);
				}
			}
			else {
				Component selectedComponent = tabPane.getComponentAt(selectedIndex);
				if (selectedComponent != null) {
					setVisibleComponent(selectedComponent);
				}

				int /*tx, ty, tw, */th; // tab area bounds
				int cx, cy, cw, ch; // content area bounds
				Insets contentInsets = getContentBorderInsets(tabPlacement);
				Rectangle bounds = tabPane.getBounds();
				int numChildren = tabPane.getComponentCount();

				if (numChildren > 0) {

					// NOTE: Here we're assuming tabs are on the bottom.
					// Calculate tab area bounds
					//tw = bounds.width - insets.left - insets.right;
					th = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
					//tx = insets.left;
					//ty = bounds.height - insets.bottom - th;

					// calculate content area bounds
					cx = insets.left + contentInsets.left;
					cy = insets.top + contentInsets.top;
					cw = bounds.width - insets.left - insets.right - 
						contentInsets.left - contentInsets.right;
					ch = bounds.height - insets.top - insets.bottom - th -
						contentInsets.top - contentInsets.bottom;
					for (int i=0; i < numChildren; i++) {
						Component child = tabPane.getComponent(i);
						child.setBounds(cx, cy, cw, ch);
					}

				}

			}

		}

		/**
		 * Calculates the bounds for each tab.
		 *
		 * @param tabPlacement Always <code>JTabbedPane.BOTTOM</code>.
		 * @param tabCount The number of tabs.
		 */
		@Override
		protected void calculateTabRects(int tabPlacement, int tabCount) {

			FontMetrics metrics = getFontMetrics();
			int fontHeight = metrics.getHeight();
			Dimension size = tabPane.getSize();
			Insets insets = tabPane.getInsets(); 
			Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
			//int fontHeight = metrics.getHeight();
			//int selectedIndex = tabPane.getSelectedIndex();
			boolean leftToRight = tabPane.getComponentOrientation().isLeftToRight();

			// Since we'll only ever have one run of tabs, the tab area's
			// height should be the height of the tallest tab.
			maxTabHeight = calculateMaxTabHeight(tabPlacement);

			// Get the starting x and y for the tab run.
			int x = tabAreaInsets.left;
			int y = size.height - insets.bottom - tabAreaInsets.bottom - maxTabHeight;

			runCount = 1;	// Always 1 run count (needed by other methods).

			if (tabCount == 0)
				return;

			// Compute the width of the area in which to put the tabs.
			int tabAreaWidth = size.width - insets.left - insets.right -
							tabAreaInsets.left - tabAreaInsets.right;

			// Compute the width the tabs will take up if they all get the
			// amount of space they want.
			int allTabsPreferredWidth = 0;
			for (int i=0; i<tabCount; i++) {
				allTabsPreferredWidth += calculateTabWidth(tabPlacement, i, metrics);
			}

			// First check to see whether all tabs fit in the tab area
			// width fully.  If they do, give them all the space they need.
			if (allTabsPreferredWidth <= tabAreaWidth) {
				for (int i=0; i<tabCount; i++) {
					Rectangle rect = rects[i];
					if (i > 0) {
						rect.x = rects[i-1].x + rects[i-1].width;
					}
					else {
						tabRuns[0] = 0;
						maxTabWidth = 0;
						rect.x = x;
					}
					rect.width = calculateTabWidth(tabPlacement, i, metrics);
					maxTabWidth = Math.max(maxTabWidth, rect.width);
					rect.y = y;
					rect.height = calculateTabHeight(tabPlacement, i, fontHeight);
				}
			}

			// If they all don't fit, make each tab have an equal share of
			// the space available.
			else {
				maxTabWidth = tabAreaWidth/tabCount;
				for (int i=0; i<tabCount; i++) {
					Rectangle rect = rects[i];
					rect.x = x + i*maxTabWidth;
					rect.y = y;
					rect.width = maxTabWidth;
					rect.height = calculateTabHeight(tabPlacement, i, fontHeight);
				}
			}

			// If right to left, flip x positions and adjust by widths
			if (!leftToRight) {
				int rightMargin = size.width 
							- (insets.right + tabAreaInsets.right);
				for (int i=0; i<tabCount; i++)
					rects[i].x = rightMargin - rects[i].x - rects[i].width;
			}

		}

	}


}