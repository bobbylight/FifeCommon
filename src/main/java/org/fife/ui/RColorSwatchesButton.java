/*
 * 09/17/2004
 *
 * RColorSwatchesButton.java - A JButton that lets you pick a color via
 * a popup menu.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicMenuItemUI;


/**
 * A color button that, when clicked, displays a popup menu containing several
 * color "swatches" to chooser from.  The popup also contains a menu item that
 * brings up a <code>JColorChooser</code> if the user wants a color that does
 * not have a swatch.
 *
 * @author Robert Futrell
 * @version 0.6
 */
public class RColorSwatchesButton extends RColorButton {

	private static final long serialVersionUID = 1L;

	private static final Dimension SWATCH_MENUITEM_SIZE = new Dimension(24,24);
	private static final int NUM_COLUMNS			  = 6;
	private static final String MSG	  = "org.fife.ui.RColorSwatchesButton";

	private static final Color[] predefinedColors = new Color[] {
		Color.RED,
		new Color(255,128,128),
		new Color(255,153,0),
		new Color(204,102,0),
		Color.ORANGE,
		new Color(255,240,204),
		Color.YELLOW,
		new Color(255,255,164),
		new Color(148,148,0),
		new Color(188,188,40),
		new Color(0,128,0),
		Color.GREEN,
		new Color(48,255,47),
		new Color(218,254,218),
		new Color(0,128,164),
		Color.BLUE,
		Color.CYAN,
		new Color(231,231,255),
		Color.PINK,
		new Color(255,224,240),
		Color.MAGENTA,
		new Color(173,0,128),
		new Color(128,0,0),
		new Color(100,0,200),
		Color.WHITE,
		new Color(224,224,224),
		Color.LIGHT_GRAY,
		Color.GRAY,
		Color.DARK_GRAY,
		Color.BLACK,
	};

	private ColorPopupMenu popup;
	private PopupListener popupListener;


	/**
	 * Creates a new <code>RColorSwatchesButton</code> defaulting to black.
	 */
	public RColorSwatchesButton() {
		this(Color.BLACK);
	}


	/**
	 * Creates a new <code>RColorSwatchesButton</code>.
	 *
	 * @param color The initial color for the button.
	 */
	public RColorSwatchesButton(Color color) {
		super(color);
	}


	/**
	 * Creates a new <code>RColorSwatchesButton</code>.
	 *
	 * @param color The initial color for the button.
	 * @param width The width of the color in the button.
	 * @param height The height of the color in the button.
	 */
	public RColorSwatchesButton(Color color, int width, int height) {
		super(color, width, height);
	}


	/**
	 * Returns the listener listening for this button to be clicked.
	 *
	 * @return The action listener for this button.  For an
	 *         <code>RColorSwatchesButton</code>, the returned listener
	 *         displays a popup menu with color swatches, with an item that
	 *         pops up a <code>JColorChooser</code>.
	 */
	@Override
	public ActionListener createActionListener() {
		return new SwatchesActionListener();
	}


	/**
	 * Overridden so the popup menu gets its LnF updated too.
	 */
	@Override
	public void updateUI() {

		super.updateUI();

		// We don't just call SwingUtilites.updateComponentTreeUI() because
		// we don't want to update the custom UI on the swatches.
		if (popup!=null) {
			//SwingUtilities.updateComponentTreeUI(popup);
			popup.updateUI();
			// We only need to update the UI of the last child, as it is the
			// "More colors..." menu item.  All of the others are swatches,
			// and updating them would get rid of our custom UI.
			((JComponent)popup.getComponent(popup.getComponentCount()-1)).
													updateUI();
			SwatchMenuItemUI.refreshMenuItemBackground();
		}

	}


	/**
	 * Listens for the user to click on the <code>RColorSwatchesButton</code>
	 * so it can display the popup menu.
	 */
	private class SwatchesActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (popup==null) {
				popup = new ColorPopupMenu();
				popup.applyComponentOrientation(getComponentOrientation());
			}
			popup.show(RColorSwatchesButton.this,
				0, 0+RColorSwatchesButton.this.getHeight());
		}

	}


	/**
	 * The popup menu for this button.  It contains several color swatches,
	 * as well as an item that brings up a <code>JColorChooser</code> so the
	 * user can customize the color chosen.
	 */
	private class ColorPopupMenu extends JPopupMenu {

		private static final long serialVersionUID = 1L;

		public ColorPopupMenu() {

			// Create the listener.
			popupListener = new PopupListener();

			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);
			GridBagConstraints c = new GridBagConstraints();

			int i = 0;
			int length = predefinedColors.length;
			while (i<length) {
		
				c.fill = GridBagConstraints.BOTH;
				c.weightx = 1.0;
				c.gridwidth = 1; // The default value.
				for (int j=0; j<NUM_COLUMNS-1; j++)
					addSwatch(predefinedColors[i+j], layout, c);

				c.gridwidth = GridBagConstraints.REMAINDER; //end row
				addSwatch(predefinedColors[i+NUM_COLUMNS-1], layout, c);

				i += NUM_COLUMNS;

			}

			SwatchMenuItemUI.refreshMenuItemBackground();
			ResourceBundle msg = ResourceBundle.getBundle(MSG);

			c.weightx = 0.0;		   //reset to the default
			JMenuItem item = new JMenuItem(msg.getString("MoreColors.txt"));
			item.addActionListener(popupListener);
			layout.setConstraints(item, c);
			add(item);
			pack();

		}

		private void addSwatch(Color color, GridBagLayout gridbag,
							GridBagConstraints c) {
			// We want a menu item that uses a custom UI even when the
			// user changes it.
			JMenuItem item = new JMenuItem(new ColorIcon(color, 16,16));
			item.setToolTipText(createToolTipText(color));
			item.setUI(new SwatchMenuItemUI());
			item.addActionListener(popupListener);
			gridbag.setConstraints(item, c);
			add(item);
		}

		private String createToolTipText(Color color) {
			return "(" + color.getRed() + ", " + color.getGreen() + ", " +
					color.getBlue() + ")";
		}

	}


	/**
	 * Listens for the user clicking on one of the menu items in the popup
	 * menu and responds accordingly.
	 */
	protected class PopupListener extends RColorButtonActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (source instanceof JMenuItem) {

				JMenuItem item = (JMenuItem)source;
				Icon icon = item.getIcon();

				// If it has an icon, it must be a swatch.
				if (icon!=null) {
					ColorIcon colorIcon = (ColorIcon)icon;
					Color color = colorIcon.getColor();
					RColorSwatchesButton.this.setColor(color);
				}

				// Otherwise, it is the "More colors..." menu item.
				else {
					e.setSource(RColorSwatchesButton.this);
					super.actionPerformed(e);
				}

			}

		}

	}


	/**
	 * UI for a "color swatch" menu item in the popup menu.  This is
	 * overridden to only display a color icon without any extra spacing.
	 */
	private static class SwatchMenuItemUI extends BasicMenuItemUI {

		private static Color background;

		public SwatchMenuItemUI() {
			// When LAF is set to something other than a subclass of 
			// BasicLookAndFeel (e.g. GTK, Synth, Nimbus), it's possible
			// that the LAF doesn't set some properties set in
			// BasicLookAndFeel that BasicMenuItemUI (and hence its
			// subclasses) assume are set.  See:
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6634504
			String prefix = getPropertyPrefix();
			UIManager.put(prefix + ".borderPainted", Boolean.FALSE);
		}

		@Override
		public Dimension getMaximumSize(JComponent c) {
			return getPreferredSize(c);
		}

		@Override
		protected Dimension getPreferredMenuItemSize(JComponent c,
					Icon checkIcon, Icon arrowIcon, int defaultTextIconGap) {
			return getPreferredSize(c);
		}

		@Override
		public Dimension getPreferredSize(JComponent c) {
			return SWATCH_MENUITEM_SIZE;
		}

		@Override
		public void paint(Graphics g, JComponent c) {

			JMenuItem menuItem = (JMenuItem)c;
			ButtonModel model = menuItem.getModel();

			// Get color through UIManager in case of LnF change.
			g.setColor(background);
			Rectangle bounds = c.getBounds();
			g.fillRect(0,0, bounds.width,bounds.height);

			// Swatches are always enabled, so we only have to check
			// whether or not they are armed.
			if (model.isArmed()) {
				g.setColor(Color.BLACK);
				g.drawRect(1,1, bounds.width-3,bounds.height-3);
			}

			Icon icon = menuItem.getIcon();
			int x = (bounds.width-icon.getIconWidth())/2;
			int y = (bounds.height-icon.getIconHeight())/2;
			icon.paintIcon(menuItem, g, x,y);

		}

		/**
		 * Workaround for the fact that not all LookAndFeels define
		 * MenuItem.background (such as MacLookAndFeel and WebLookAndFeel).
		 */
		private static void refreshMenuItemBackground() {
			background = UIManager.getColor("MenuItem.background");
			if (background==null ||
					System.getProperty("os.name").toLowerCase().contains("os x") ||
					WebLookAndFeelUtils.isWebLookAndFeelInstalled()) {
				background = new JMenuItem().getBackground();
			}
		}

	}


}