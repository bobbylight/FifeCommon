/*
 * 09/24/2004
 *
 * TabbedPaneTransferHandler.java - A transfer handler that can transfer
 * tabs between JTabbedPanes (or in the same tabbed pane).
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;


/**
 * A transfer handler for <code>JTabbedPane</code>s.  This handler can move tabs
 * between tabbed panes, as well as move the position of tabs within a tabbed
 * pane.<p>
 *
 * If the tabbed pane receiving the drop implements the
 * <code>DrawDnDIndicatorTabbedPane</code> interface, it will be sent a
 * rectangle that it can paint to signify where the dropped tab will be placed.
 *
 * @author Robert Futrell
 * @version 0.5
 * @see DrawDnDIndicatorTabbedPane
 */
public class TabbedPaneTransferHandler extends TransferHandler
									implements DropTargetListener {

	private static final long serialVersionUID = 1L;

	/**
	 * The "data type" that starts and ends the drag-and-drops are instances
	 * of <code>JTabbedPane</code>.  While we're actually dnd'ing a "tab" and
	 * its contents, the component both sending and receiving is a JTabbedPane.
	 */
	private static final String mimeType =
							DataFlavor.javaJVMLocalObjectMimeType +
							";class=javax.swing.JTabbedPane";

	/**
	 * The data flavor corresponding to the above MIME type.
	 */
	private DataFlavor tabFlavor;

	/**
	 * The location of the mouse cursor throughout the drag-and-drop.
	 * This is here because of a deficiency in TransferHandler's design; you
	 * have no way of knowing the exact drop location in the component with a
	 * plain TransferHandler unless you implement DropTargetListener and get
	 * it that way.
	 */
	protected Point mouseLocation;

	private TabTransferable currentTransferable;


	/**
	 * Constructor.
	 */
	public TabbedPaneTransferHandler() {
		try {
			tabFlavor = new DataFlavor(mimeType);
		} catch (ClassNotFoundException e) {
			e.printStackTrace(); // Never happens
		}
	}



	/**
	 * Overridden to include a check for a TabData flavor.
	 */
	@Override
	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		return hasTabFlavor(flavors);
	}


	@Override
	protected Transferable createTransferable(JComponent c) {
		currentTransferable = new TabTransferable((JTabbedPane)c);
		return currentTransferable;
	}


	public void dragEnter(DropTargetDragEvent e) {}


	public void dragExit(DropTargetEvent e) {
		Component c = e.getDropTargetContext().getComponent();
		if (c instanceof DrawDnDIndicatorTabbedPane) {
			((DrawDnDIndicatorTabbedPane)c).clearDnDIndicatorRect();
		}
	}


	/**
	 * Called when a drag-and-drop operation is pending, and the mouse is
	 * hovering over the destination component.
	 */
	public void dragOver(DropTargetDragEvent e) {

		mouseLocation = e.getLocation();

		Component c = e.getDropTargetContext().getComponent();
		JTabbedPane destTabbedPane = (JTabbedPane)c;

		// If the tabbed pane wants to paint the drop location, figure out
		// the rectangle to paint for the new tab.
		if (destTabbedPane instanceof DrawDnDIndicatorTabbedPane) {

			// Verify it's a tab transferable (as this class may be
			// subclassed to support other transferable types).
			TabTransferable t = currentTransferable;
			if (t!=null) {

				Rectangle transferredTabBounds = t.getTabBounds();
				int tab = getDroppedTabIndex(destTabbedPane, mouseLocation);
				Rectangle iBounds = destTabbedPane.
									getBoundsAt(tab==0 ? 0 : tab-1);

				// Make rectangle sit on same y-axis as the target tab span.
				iBounds.y = iBounds.y + iBounds.height -
							transferredTabBounds.height;

				// Adjust rectangle so it is "in-between" two tabs.
				int tabPlacement = destTabbedPane.getTabPlacement();
				switch (tabPlacement) {
					case JTabbedPane.TOP:
					case JTabbedPane.BOTTOM:
						iBounds.x += tab==0 ? 0 : iBounds.width;
						iBounds.x -= transferredTabBounds.width/2;
						break;
					case JTabbedPane.LEFT:
					case JTabbedPane.RIGHT:
						iBounds.y -= transferredTabBounds.height/2;
						break;
				}

				// Pass the rectangle to the tabbed pane.
				((DrawDnDIndicatorTabbedPane)c).setDnDIndicatorRect(
										iBounds.x, iBounds.y,
										transferredTabBounds.width,
										transferredTabBounds.height);

			}

		}

	}


	public void drop(DropTargetDropEvent e) {
		Component c = e.getDropTargetContext().getComponent();
		if (c instanceof DrawDnDIndicatorTabbedPane) {
			((DrawDnDIndicatorTabbedPane)c).clearDnDIndicatorRect();
		}
	}


	public void dropActionChanged(DropTargetDragEvent e) {}


	/**
	 * Returns the index at which to add a tab if it is dropped at the mouse
	 * location specified by <code>p</code>.
	 *
	 * @param tabbedPane The tabbed pane who would be receiving the tab.
	 * @param p The mouse location.
	 * @return The index at which to add the tab.
	 */
	protected int getDroppedTabIndex(JTabbedPane tabbedPane, Point p) {

		// First see if the mouse is actually in a tab.
		int tab = tabbedPane.indexAtLocation(
						mouseLocation.x,mouseLocation.y);

		// If it isn't, find the "closest" tab and use it.
		if (tab==-1) {
			int tabCount = tabbedPane.getTabCount();
			int tabPlacement = tabbedPane.getTabPlacement();
			switch (tabPlacement) {
				case JTabbedPane.TOP:
				case JTabbedPane.BOTTOM:
					int x = p.x;
					int dy = Integer.MAX_VALUE;
					for (int i=0; i<tabCount; i++) {
						Rectangle b = tabbedPane.getBoundsAt(i);
						if (x>=b.x && x<(b.x+b.width)) {
							int dy2 = Math.abs(b.y-p.y);
							if (dy2<dy) {
								tab = i;
								dy = dy2;
							}
						}
					}
					break;
				case JTabbedPane.LEFT:
				case JTabbedPane.RIGHT:
					int y = p.y;
					int dx = Integer.MAX_VALUE;
					for (int i=0; i<tabCount; i++) {
						Rectangle b = tabbedPane.getBoundsAt(i);
						if (y>=b.y && y<(b.y+b.height)) {
							int dx2 = Math.abs(b.x-p.x);
							if (dx2<dx) {
								tab = i;
								dx = dx2;
							}
						}
					}
					break;
			}
		}

		// If they're "off to the side" of all tabs, default to
		// adding to the end of the tabs.
		if (tab==-1)
			tab = tabbedPane.getTabCount();

		return tab;

	}


	/**
	 * We can only move tabs, we cannot copy them.
	 *
	 * @param c This parameter is ignored.
	 * @return <code>TransferHandler.MOVE</code>, as we can only move tabs.
	 */
	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	
	/**
	 * Does the flavor list have a Tab flavor?
	 */
	protected boolean hasTabFlavor(DataFlavor[] flavors) {
		if (tabFlavor == null) {
			return false;
		}
		for (int i = 0; i < flavors.length; i++) {
			if (tabFlavor.equals(flavors[i])) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Called when the drag-and-drop operation has just completed.  This
	 * creates a new tab identical to the one "dragged" and places it in the
	 * destination <code>JTabbedPane</code>.
	 *
	 * @param c The component receiving the "drop" (the instance of
	 *        <code>JTabbedPane</code>).
	 * @param t The data being transfered (information about the tab and the
	 *        component contained by the tab).
	 * @return Whether or not the import was successful.
	 */
	@Override
	public boolean importData(JComponent c, Transferable t) {

		boolean successful = false;

		if (hasTabFlavor(t.getTransferDataFlavors()) && mouseLocation!=null) {

			try {

				// Physically insert the tab.
				JTabbedPane tabbedPane = (JTabbedPane)c;
				int tab = getDroppedTabIndex(tabbedPane, mouseLocation);

				TabTransferable.TabTransferData td =
							(TabTransferable.TabTransferData)t.
									getTransferData(tabFlavor);
				JTabbedPane sourcePane = td.sourceTabbedPane;
				int sourceIndex = td.tabIndex;
				String tabName = sourcePane.getTitleAt(sourceIndex);
				Icon icon = sourcePane.getIconAt(sourceIndex);
				Component comp = sourcePane.getComponentAt(sourceIndex);
				String toolTip = sourcePane.getToolTipTextAt(sourceIndex);
				Color foreground = sourcePane.getForegroundAt(sourceIndex);

				tabbedPane.insertTab(tabName, icon, comp, toolTip, tab);

				// Here's the deal:  Even though we inserted the tab,
				// the tabbed pane does not automatically give the tab
				// we just inserted focus.  We must manually give the
				// just-dragged tab focus (on the EDT).  Further, we
				// must figure out what tab was just dragged by
				// checking the contents of each tab one at a time;
				// this is because  we may have "moved" this tab from
				// one location to another in the same tabbed pane,
				// thus messing up the value of getTabCount() (why it
				// may not yet be valid, I'm not sure; I guess
				// insertTab() doesn't actually physically add the
				// component to the tabbed pane until later on the
				// EDT?...).
				int count = tabbedPane.getTabCount();
				for (int i=0; i<count; i++) {
					Component comp2 = tabbedPane.getComponentAt(i);
					if (comp2!=null && comp2.equals(comp)) {
						tabbedPane.setForegroundAt(i, foreground);
						selectTab(tabbedPane, i);
						break;
					}
				}

				successful = true;
				if (c instanceof DrawDnDIndicatorTabbedPane) {
					((DrawDnDIndicatorTabbedPane)c).clearDnDIndicatorRect();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		currentTransferable = null;
		return successful;

	}


	/**
	 * Selects the specified tab in the specified tabbed pane.  This method
	 * can be overridden by subclasses to do more stuff than simply select
	 * the tab.
	 *
	 * @param tabbedPane The tabbed pane.
	 * @param index The index of the tab to select.
	 */
	protected void selectTab(final JTabbedPane tabbedPane, final int index) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tabbedPane.setSelectedIndex(index);
			}
		});
	}


	/**
	 * Transferable representing a tab from a tabbed pane and its contents.
	 */
    class TabTransferable implements Transferable {

		private TabTransferData transferData;

		/**
		 * The data remembered about the tab.
		 */
		class TabTransferData {

			private JTabbedPane sourceTabbedPane;
			private int tabIndex;

			TabTransferData(JTabbedPane tabbedPane, int tabIndex) {
				this.sourceTabbedPane = tabbedPane;
				this.tabIndex = tabIndex;
			}

		}

		TabTransferable(JTabbedPane tabbedPane) {
			int index = tabbedPane.getSelectedIndex();
			transferData = new TabTransferData(tabbedPane, index);
		}

		public Rectangle getTabBounds() {
			return transferData.sourceTabbedPane.
								getBoundsAt(transferData.tabIndex);
		}

		public Object getTransferData(DataFlavor flavor)
								throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return transferData;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { tabFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return tabFlavor.equals(flavor);
		}

	}


}