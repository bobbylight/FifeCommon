/*
 * 04/13/2004
 *
 * CustomizableToolBar.java - A toolbar with a popup right-click menu allowing
 * the user to toggle docking and (in the future) add/remove buttons and
 * separators.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ToolBarUI;
import javax.swing.plaf.basic.BasicToolBarUI;


/**
 * An extension of <code>JToolBar</code> that adds a right-click popup
 * menu allowing the user to toggle docking and add or remove buttons
 * and separators.
 * <p>To use this class, create a subclass of it, and at the end of its
 * constructor, call <code>makeCustomizable</code>.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class CustomizableToolBar extends JToolBar {

	private static final long serialVersionUID = 1L;

	/**
	 * The popup menu for the toolbar.
	 */
	private JPopupMenu popupMenu;

	/**
	 * The menu for adding/removing toolbar buttons.
	 */
	private JMenu addRemoveMenu;

	/**
	 * The mouse listener that listens for right-clicks on this toolbar.
	 */
	private MouseInputAdapter mia;

	/**
	 * Whether text labels should be on the buttons, as well as images.
	 */
	private boolean showText;

	private static final String MSG = "org.fife.ui.CustomizableToolBar";


	/**
	 * Creates a new toolbar.
	 */
	public CustomizableToolBar() {
	}


	/**
	 * Creates a new toolbar with the specified orientation.
	 *
	 * @param orientation The initial orientation (either
	 *        {@link SwingConstants#VERTICAL} or
	 *        {@link SwingConstants#HORIZONTAL}).
	 */
	public CustomizableToolBar(int orientation) {
		super(orientation);
	}


	/**
	 * Creates a new toolbar.
	 *
	 * @param name The name for the tool bar.
	 */
	public CustomizableToolBar(String name) {
		super(name);
	}


	/**
	 * Creates a new toolbar.
	 *
	 * @param name The name for the tool bar.
	 * @param orientation The initial orientation (either
	 *        {@link SwingConstants#VERTICAL} or
	 *        {@link SwingConstants#HORIZONTAL}).
	 */
	public CustomizableToolBar(String name, int orientation) {
		super(name, orientation);
	}


	@Override
	public void addNotify() {
		super.addNotify();
		WebLookAndFeelUtils.fixToolbar(this, true, false);
	}


	/**
	 * Creates a button to add to this tool bar.
	 *
	 * @param a The action for the button.
	 * @return The button.
	 */
	protected JButton createButton(Action a) {
		JButton b = new JButton(a);
		b.setToolTipText((String)a.getValue(Action.NAME)); // May be null
		b.setHorizontalTextPosition(JButton.CENTER);
		b.setVerticalTextPosition(JButton.BOTTOM);
		if (showText) {
			b.setText((String)a.getValue(Action.NAME));
		}
		else {
			b.setText(null);
		}
		String desc = (String)a.getValue(Action.SHORT_DESCRIPTION);
		if (desc!=null) {
			b.getAccessibleContext().setAccessibleDescription(desc);
		}
		return b;
	}


	/**
	 * Creates the popup menu.
	 */
	private void createPopupMenu() {

		ResourceBundle msg = ResourceBundle.getBundle(MSG);

		popupMenu = new JPopupMenu();
		String temp = msg.getString("PopupMenu.LockToolbar.txt");
		AbstractAction lockAction = new LockAction(temp);
		JCheckBoxMenuItem lockMenuItem = new JCheckBoxMenuItem(lockAction);
		lockMenuItem.setMnemonic(KeyEvent.VK_L);
		popupMenu.add(lockMenuItem);

		popupMenu.addSeparator();

		temp = msg.getString("PopupMenu.AddRemoveButtons.txt");
		addRemoveMenu = new JMenu(temp);
		addRemoveMenu.setMnemonic(KeyEvent.VK_A);
		populateAddRemovePopupMenu(msg);
		popupMenu.add(addRemoveMenu);

	}


	/**
	 * Returns whether text labels are to be displayed on buttons, along
	 * with the images.
	 *
	 * @return Whether text labels are shown.
	 * @see #setShowText(boolean)
	 */
	public boolean getShowText() {
		return showText;
	}


	/**
	 * This should be called at the end of the constructor of any toolbar
	 * that overrides this class.  This is the method that sets up the
	 * popup menu for the toolbar.  If you don't call this method, then
	 * a <code>CustomizableToolBar</code> behaves no differently than a
	 * <code>JToolBar</code>.
	 */
	public void makeCustomizable() {

		// Remove an old mouse listener if makeCustomizable() has been
		// called before.
		if (mia!=null) {
			removeMouseListener(mia);
			for (int i=0; i<getComponentCount(); i++) {
				getComponentAtIndex(i).removeMouseListener(mia);
			}
		}

		// Create the action that listens for right-clicks for customization.
		mia = new MouseInputAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			public void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					if (popupMenu==null)
						createPopupMenu();
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
					e.consume();
				}
			}
		};

		// Add the mouse listener to all parts of the toolbar.
		addMouseListener(mia);	// Add to the toolbar itself.
		for (int i=0; i<getComponentCount(); i++) {
			getComponentAtIndex(i).addMouseListener(mia);
		}

	}


	/**
	 * Populates the "Add/Remove Buttons" popup menu.
	 */
	protected void populateAddRemovePopupMenu(ResourceBundle msg) {

		JPopupMenu popupMenu = addRemoveMenu.getPopupMenu();
		popupMenu.removeAll();

		JCheckBoxMenuItem cbMenuItem;

		Component[] components = getComponents();
		int num = components.length;
		for (int i=0; i<num; i++) {
			if (components[i] instanceof JButton) {
				final JButton button = (JButton)components[i];
				String title = button.getText();
				if (title==null)
					title = button.getToolTipText();
				if (title==null)
					title = msg.getString("PopupMenu.Unknown.txt");
				cbMenuItem = new JCheckBoxMenuItem(
					new AbstractAction(title) {
						private static final long serialVersionUID = 1L;
						public void actionPerformed(ActionEvent e) {
							button.setVisible(!button.isVisible());
							repaint();
						}
					}
				);
				cbMenuItem.setSelected(button.isVisible());
				popupMenu.add(cbMenuItem);
			}
		}
		popupMenu.addSeparator();
		String temp = msg.getString("PopupMenu.ResetToolBar.txt");
		ResetAction resetAction = new ResetAction(temp);
		popupMenu.add(new JMenuItem(resetAction));

	}


	/**
	 * Toggles whether text labels are displayed on buttons underneath the
	 * images.
	 *
	 * @param show Whether to show text labels.
	 * @see #getShowText()
	 */
	public void setShowText(boolean show) {
		showText = show;
		for (int i=0; i<getComponentCount(); i++) {
			Component c = getComponent(i);
			if (c instanceof JMenuItem) {
				JMenuItem mi = (JMenuItem)c;
				if (mi.getAction()!=null) {
					String text = show ?
						((String)mi.getAction().getValue(Action.NAME)) : null;
					mi.setText(text);
				}
			}
		}
	}


	@Override
	public void setUI(ToolBarUI ui) {
		super.setUI(ui);
		WebLookAndFeelUtils.fixToolbar(this, false, false);
	}


	/**
	 * Overridden so the popup menu gets its UI redone too.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (popupMenu!=null) {
			SwingUtilities.updateComponentTreeUI(popupMenu);
		}
	}


	/**
	 * Locks the current state of the toolbar.
	 *
	 * @author Robert Futrell
	 */
	private class LockAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public LockAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {

			/* 8.5.2004/pwy: the assumption that getUI returns a BasicToolBarUI
			is not coorect on all platforms. The method used below is more portable. 
				CustomizableToolBar cbt = CustomizableToolBar.this;
				cbt.setFloatable(!cbt.isFloatable());
			05/13/2004 - ref:  The proposed fix actually doesn't work quite right on
			platforms supporting BasicToolBarUI - if you undock the toolbar, then
			right-click and choose "Lock the toolbar" from the popup menu, then close
			the window the floating toolbar is in, you can never get the toolbar to
			reappear in the application!  Thus, I'm simply going to check for the UI
			being a subclass of BasicToolBarUI for now... */

			ToolBarUI ui = getUI();
			if (ui instanceof BasicToolBarUI) {
				boolean floating = ((BasicToolBarUI)ui).isFloating();
				if (floating==true) {
					((BasicToolBarUI)ui).setFloating(false, new Point(0,0));
					setFloatable(false);
				}
				else
					setFloatable(!isFloatable());
			}
			else { // ???
				CustomizableToolBar cbt = CustomizableToolBar.this;
				cbt.setFloatable(!cbt.isFloatable());
			}

		}

	}


	/**
	 * Resets the toolbar so all buttons are visible.
	 *
	 * @author Robert Futrell
	 */
	private class ResetAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ResetAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			Component[] c = getComponents();
			int num = c.length;
			for (int i=0; i<num; i++)
				if (c[i] instanceof JButton)
					c[i].setVisible(true);
		}

	}


}