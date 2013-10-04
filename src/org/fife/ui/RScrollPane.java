/*
 * 11/14/2003
 *
 * RScrollPane.java - An extension of JScrollPane that adds right-click popup
 * menus on the scrollbars for scrolling.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.*;


/**
 * An extension of <code>JScrollPane</code> that adds popup menus on its
 * scrollbars, allowing the user to scroll, page over, etc.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RScrollPane extends JScrollPane implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JPopupMenu vertSBMenu;
	private JPopupMenu horizSBMenu;

	// Mouse position RELATIVE TO THE SCROLLBAR when the user opens a
	// scrollbar's popup menu.
	private int mouseX, mouseY;	


	/**
	 * Creates an <code>RScrollPane</code> with no view.  Note that if you use
	 * this constructor, you should then call <code>setViewportView</code>.
	 */
	public RScrollPane() {
		initialize();
	}


	/**
	 * Creates an <code>RScrollPane</code> with the specified view.
	 *
	 * @param view The component this scrollpane contains.
	 */
	public RScrollPane(Component view) {
		super(view);
		initialize();
	}


	/**
	 * Creates an <code>RScrollPane</code> with the specified size and
	 * specified view.
	 *
	 * @param width The preferred width of the scrollpane.
	 * @param height The preferred height of the scrollpane.
	 * @param view The component this scrollpane contains.
	 */
	public RScrollPane(int width, int height, Component view) {
		super(view);
		setPreferredSize(new Dimension(width, height));
		initialize();
	}


	/**
	 * Listens for scrollbars' popup menus' actions.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		// Scroll to the position the mouse points to horizontally.
		if ("ScrollHereHorizontal".equals(command)) {

			int width = getHorizontalScrollBar().getWidth();
			float howFarIn = ((float)mouseX) / ((float)width);

			BoundedRangeModel brm = getHorizontalScrollBar().getModel();
			int newPosition = java.lang.Math.round(howFarIn *
				(brm.getMaximum() - brm.getMinimum())) - brm.getExtent()/2;
			brm.setValue(newPosition);

	       }

		// Scroll to the position the mouse points to vertically.
		else if ("ScrollHereVertical".equals(command)) {

			int height = getVerticalScrollBar().getHeight();
			float howFarIn = ((float)mouseY) / ((float)height);

			BoundedRangeModel brm = getVerticalScrollBar().getModel();
			int newPosition = java.lang.Math.round(howFarIn *
				(brm.getMaximum() - brm.getMinimum())) - brm.getExtent()/2;
			brm.setValue(newPosition);

		}

		// Scroll to the top.
		else if ("Top".equals(command)) {
			BoundedRangeModel brm = getVerticalScrollBar().getModel();
			brm.setValue(0);
		}

		// Scroll to the bottom.
		else if ("Bottom".equals(command)) {
			BoundedRangeModel brm = getVerticalScrollBar().getModel();
			brm.setValue(brm.getMaximum());
		}

		// Scroll one page up in the document.
		else if ("PageUp".equals(command)) {
			JViewport viewport = getViewport();
			Point p = viewport.getViewPosition();
			int viewportHeight = viewport.getExtentSize().height;
			p.translate(0, -viewportHeight);
			if (p.getY()<0)
				p.setLocation(p.getX(), 0);
			viewport.setViewPosition(p);
	       }

		// Scroll one page down in the document.
		else if ("PageDown".equals(command)) {
			JViewport viewport = getViewport();
			Point p = viewport.getViewPosition();
			int viewportHeight = viewport.getExtentSize().height;
			Component view = viewport.getView();
			double tempY = p.getY() + viewportHeight;
			if (view.getHeight() >= tempY + viewportHeight)
				p.setLocation(p.getX(), tempY);
			else
				p.setLocation(p.getX(), view.getHeight() - viewportHeight);
			viewport.setViewPosition(p);
		}

		// Scroll one unit up in the document.
		else if ("ScrollUp".equals(command)) {
			JViewport viewport = getViewport();
			Point p = viewport.getViewPosition();
			int unitIncrement = getVerticalScrollBar().getUnitIncrement(-1);
			if (p.getY() > unitIncrement) {
				p.translate(0, -unitIncrement);
				viewport.setViewPosition(p);
			}
			else {
				p.setLocation(p.getX(), 0);
				viewport.setViewPosition(p);
			}
		}

		// Scroll one unit down in the document.
		else if ("ScrollDown".equals(command)) {
			JViewport viewport = getViewport();
			Point p = viewport.getViewPosition();
			int unitIncrement = getVerticalScrollBar().getUnitIncrement(1);
			Component view = viewport.getView();
			if (p.getY() < view.getHeight() - viewport.getHeight() - unitIncrement) {
				p.translate(0, unitIncrement);
				viewport.setViewPosition(p);
			}
			else {
				p.setLocation(p.getX(), view.getHeight() - viewport.getHeight());
				viewport.setViewPosition(p);
			}
		}

		// Scroll all the way to the left.
		else if ("LeftEdge".equals(command)) {
			BoundedRangeModel brm = getHorizontalScrollBar().getModel();
			brm.setValue(0);
		}

		// Scroll all the way to the right.
		else if ("RightEdge".equals(command)) {
			BoundedRangeModel brm = getHorizontalScrollBar().getModel();
			brm.setValue(brm.getMaximum());
		}

		// Scroll one page to the left.
		else if ("PageLeft".equals(command)) {
			JViewport viewport = getViewport();
			Point p = viewport.getViewPosition();
			int viewportWidth = viewport.getExtentSize().width;
			p.translate(-viewportWidth, 0);
			if (p.getX()<0)
				p.setLocation(0, p.getY());
			viewport.setViewPosition(p);
		}

		// Scroll one page to the right.
		else if ("PageRight".equals(command)) {
			JViewport viewport = getViewport();
			Point p = viewport.getViewPosition();
			Component view = viewport.getView();
			int viewportWidth = viewport.getExtentSize().width;
			p.translate(viewportWidth, 0);
			if (p.getX() > view.getWidth()-viewportWidth)
				p.setLocation(view.getWidth()-viewportWidth, p.getY());
			viewport.setViewPosition(p);
		}

		// Scroll one element (pixel) to the left.
		else if ("ScrollLeft".equals(command)) {
			JViewport viewport = getViewport();
			Point p = viewport.getViewPosition();
			int unitIncrement = getHorizontalScrollBar().getUnitIncrement(-1);
			if (p.getX() > unitIncrement) {
				p.translate(-unitIncrement, 0);
				viewport.setViewPosition(p);
			}
			else {
				p.setLocation(0, p.getY());
				viewport.setViewPosition(p);
			}
		}

		// Scroll one element (pixel) to the right.
		else if ("ScrollRight".equals(command)) {
			JViewport viewport = getViewport();
			Point p = viewport.getViewPosition();
			int unitIncrement = getHorizontalScrollBar().getUnitIncrement(1);
			Component view = viewport.getView();
			if (p.getX() < view.getWidth() - viewport.getWidth() - unitIncrement) {
				p.translate(unitIncrement, 0);
				viewport.setViewPosition(p);
			}
			else {
				p.setLocation(view.getWidth() - viewport.getWidth(), p.getY());
				viewport.setViewPosition(p);
			}
		}

	}


	/**
	 * Adds a menu item to a menu.
	 *
	 * @param key A key into this scroll pane's resource bundle for the menu
	 *        item's value.
	 * @param actionCommand The command sent to the listener (the scroll pane).
	 * @param menu The popup menu to which to add the item.
	 * @param msg The resource bundle containing text, etc.
	 */
	private final void addMenuItem(String key, String actionCommand,
							JPopupMenu menu, ResourceBundle msg) {
		JMenuItem item = new JMenuItem(msg.getString(key));
		item.setActionCommand(actionCommand);
		item.addActionListener(this);
		menu.add(item);
	}


	/**
	 * Sets up horizontal scrollbar's popup menu.
	 */
	private void createHorizontalScrollBarMenu() {

		horizSBMenu = new JPopupMenu();
		ResourceBundle msg = ResourceBundle.getBundle(RScrollPane.class.getName());

		addMenuItem("ScrollHere", "ScrollHereHorizontal", horizSBMenu, msg);
		horizSBMenu.addSeparator();
		addMenuItem("LeftEdge", "LeftEdge", horizSBMenu, msg);
		addMenuItem("RightEdge", "RightEdge", horizSBMenu, msg);
		horizSBMenu.addSeparator();
		addMenuItem("PageLeft", "PageLeft", horizSBMenu, msg);
		addMenuItem("PageRight", "PageRight", horizSBMenu, msg);
		horizSBMenu.addSeparator();
		addMenuItem("ScrollLeft","ScrollLeft",horizSBMenu, msg);
		addMenuItem("ScrollRight", "ScrollRight", horizSBMenu, msg);

		horizSBMenu.applyComponentOrientation(getComponentOrientation());

	}


	/**
	 * Sets up vertical scrollbar's popup menu.
	 */
	private void createVerticalScrollBarMenu() {

		vertSBMenu = new JPopupMenu();
		ResourceBundle msg = ResourceBundle.getBundle(RScrollPane.class.getName());

		addMenuItem("ScrollHere","ScrollHereVertical", vertSBMenu, msg);
		vertSBMenu.addSeparator();
		addMenuItem("Top", "Top", vertSBMenu, msg);
		addMenuItem("Bottom", "Bottom", vertSBMenu, msg);
		vertSBMenu.addSeparator();
		addMenuItem("PageUp", "PageUp", vertSBMenu, msg);
		addMenuItem("PageDown", "PageDown", vertSBMenu, msg);
		vertSBMenu.addSeparator();
		addMenuItem("ScrollUp", "ScrollUp", vertSBMenu, msg);
		addMenuItem("ScrollDown", "ScrollDown", vertSBMenu, msg);

		vertSBMenu.applyComponentOrientation(getComponentOrientation());

	}


	private void initialize() {

		// Create scrollbars' popup menus.
		MouseListener popupListener = new PopupListener();
		getVerticalScrollBar().addMouseListener(popupListener);
		getHorizontalScrollBar().addMouseListener(popupListener);

	}


	/**
	 * Resets the UI property with a value from the current look and feel.
	 * This overrides <code>JComponent</code>'s <code>updateUI</code> method,
	 * so that the popup menus are updated as well.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (vertSBMenu!=null)
			SwingUtilities.updateComponentTreeUI(vertSBMenu);
		if (horizSBMenu!=null)
			SwingUtilities.updateComponentTreeUI(horizSBMenu);
	}


	/**
	 * Class to listen for, and respond do, popup menu requests.
	 */
	class PopupListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				mouseX = e.getX();
				mouseY = e.getY();
				if (e.getComponent().equals(getVerticalScrollBar())) {
					if (vertSBMenu==null)
						createVerticalScrollBarMenu();
					vertSBMenu.show(getVerticalScrollBar(),
											mouseX,mouseY);
				}
				else {
					if (horizSBMenu==null)
						createHorizontalScrollBarMenu();
					horizSBMenu.show(getHorizontalScrollBar(),
											mouseX,mouseY);
				}
			}
		}

	}


}