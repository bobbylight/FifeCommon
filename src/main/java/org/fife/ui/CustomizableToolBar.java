/*
 * 04/13/2004
 *
 * CustomizableToolBar.java - A toolbar with a popup right-click menu allowing
 * the user to toggle docking and (in the future) add/remove buttons and
 * separators.
 * Copyright (C) 2004 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import org.fife.util.MacOSUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ResourceBundle;
import javax.swing.*;
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

	@Serial
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
	 * @param name The name for the toolbar.
	 */
	public CustomizableToolBar(String name) {
		super(name);
	}


	/**
	 * Creates a new toolbar.
	 *
	 * @param name The name for the toolbar.
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
		addNotifyUpdatesForMacOS();
	}


	/**
	 * Updates this toolbar as necessary if we're running on macOS.
	 *
	 * @see #removeNotifyUpdatesForMacOS()
	 */
	protected void addNotifyUpdatesForMacOS() {
		// See MacOSUtil - If the app was configured to effectively not have a title
		// bar, we need to force space for the close/minimize/maximize buttons.
		// See https://www.formdev.com/flatlaf/macos/
		if (getOnMacOSWithNoTitleBar()) {
			add(Box.createHorizontalStrut(70), 0);
		}
	}


	/**
	 * Creates a button to add to this toolbar.
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

		// Only allow unlocking of this toolbar if it's not integrted
		// into the title bar
		if (!getOnMacOSWithNoTitleBar()) {

			String temp = msg.getString("PopupMenu.LockToolbar.txt");
			AbstractAction lockAction = new LockAction(temp);
			JCheckBoxMenuItem lockMenuItem = new JCheckBoxMenuItem(lockAction);
			lockMenuItem.setMnemonic(KeyEvent.VK_L);
			popupMenu.add(lockMenuItem);

			popupMenu.addSeparator();
		}

		String temp = msg.getString("PopupMenu.AddRemoveButtons.txt");
		addRemoveMenu = new JMenu(temp);
		addRemoveMenu.setMnemonic(KeyEvent.VK_A);
		populateAddRemovePopupMenu(msg);
		popupMenu.add(addRemoveMenu);

	}


	/**
	 * Returns whether we're on macOS with no title bar. We render ourselves
	 * slightly differently in that case.
	 *
	 * @return Whether we're on macOS and there is no title bar.
	 * @see #addNotifyUpdatesForMacOS()
	 */
	protected boolean getOnMacOSWithNoTitleBar() {
		// The client property gets set regardless of the OS so we must check it here
		return MacOSUtil.isMacOs() && getRootPane() != null &&
			Boolean.TRUE.equals(getRootPane().getClientProperty(MacOSUtil.PROPERTY_FULL_WINDOW_CONTENT));
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


	@Override
	public boolean isFloatable() {
		// Never allow making the toolbar floatable if on macOS
		return !getOnMacOSWithNoTitleBar() && super.isFloatable();
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
	 *
	 * @param msg The resource bundle to use for localization.
	 */
	protected void populateAddRemovePopupMenu(ResourceBundle msg) {

		JPopupMenu popupMenu = addRemoveMenu.getPopupMenu();
		popupMenu.removeAll();

		JCheckBoxMenuItem cbMenuItem;

		Component[] components = getComponents();
		for (Component component : components) {
			if (component instanceof final JButton button) {
				String title = button.getText();
				if (title == null)
					title = button.getToolTipText();
				if (title == null)
					title = msg.getString("PopupMenu.Unknown.txt");
				cbMenuItem = new JCheckBoxMenuItem(
					new AbstractAction(title) {
						@Serial
						private static final long serialVersionUID = 1L;

						@Override
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


	@Override
	public void removeNotify() {
		super.removeNotify();
		removeNotifyUpdatesForMacOS();
	}


	/**
	 * Updates this toolbar as necessary if we're running on macOS.
	 *
	 * @see #addNotifyUpdatesForMacOS()
	 */
	protected void removeNotifyUpdatesForMacOS() {
		if (getOnMacOSWithNoTitleBar()) {
			remove(0);
		}
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
			if (c instanceof JMenuItem mi) {
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

		@Serial
		private static final long serialVersionUID = 1L;

		LockAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			/* 8.5.2004/pwy: the assumption that getUI returns a BasicToolBarUI
			is not correct on all platforms. The method used below is more portable.
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
				if (floating) {
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

		@Serial
		private static final long serialVersionUID = 1L;

		ResetAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Component[] c = getComponents();
			for (Component component : c)
				if (component instanceof JButton)
					component.setVisible(true);
		}

	}

	/**
	 * Utility method for testing.
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame();
			frame.setLayout(new BorderLayout());

			CustomizableToolBar toolBar = new CustomizableToolBar();
			JButton button = new JButton("normal");
			System.out.println(button.getBorder());
			toolBar.add(button);
			button = new JButton("noBorderPaint");
			button.setBorderPainted(false);
			toolBar.add(button);
			button = new JButton("noBorderPaintOpaque");
			button.setBorderPainted(false);
			button.setOpaque(true);
			toolBar.add(button);
			button = new JButton("nullBorder");
			button.setBorder(null);
			toolBar.add(button);
			button = new JButton("nullBorderOpaque");
			button.setBorder(null);
			button.setOpaque(true);
			toolBar.add(button);
			frame.add(toolBar, BorderLayout.NORTH);

			frame.setLocationByPlatform(true);
			frame.pack();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		});
	}
}
