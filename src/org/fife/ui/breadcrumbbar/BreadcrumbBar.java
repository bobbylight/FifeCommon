/*
 * 09/26/2009
 *
 * BreadcrumbBar.java - A breadcrumb bar for browsing a file system.
 * Copyright (C) 2009 Robert Futrell
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
package org.fife.ui.breadcrumbbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.basic.BasicBorders;


/**
 * A file/directory "breadcrumb bar," as seen in Windows Vista's File Explorer
 * and other applications.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class BreadcrumbBar extends JComponent {

	private File shownLocation;
	private JLabel iconLabel;
	private JPanel buttonPanel;
	private JToggleButton backButton;
	private Listener listener;
	private FileSystemView fsv = FileSystemView.getFileSystemView();
	private Icon horizArrowIcon;
	private Icon downArrowIcon;
	private Icon backIcon;


	/**
	 * The property change event that signifies the location shown in the
	 * breadcrumb bar has changed.
	 */
	public static final String PROPERTY_LOCATION	= "breadcrumbbar.location";

	private static final String pkg = "org/fife/ui/breadcrumbbar/";


	public BreadcrumbBar() {

		listener = new Listener();
		Color bg = UIManager.getColor("TextField.background");
		if (bg==null) { // Some LaF's might not define UIManager stuff
			bg = SystemColor.text;
		}

		setLayout(new BorderLayout());
System.out.println(UIManager.getBorder("TextField.border"));
System.out.println(UIManager.get("TextField.borderColor"));
setBorder(UIManager.getBorder("TextField.border"));
//		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setBackground(bg);

		iconLabel = new JLabel();
		iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		iconLabel.setOpaque(true);
		iconLabel.setBackground(bg);
		add(iconLabel, BorderLayout.LINE_START);
		buttonPanel = new JPanel(new BreadcrumbButtonLayout());
		buttonPanel.setBackground(bg);
		add(buttonPanel);

		boolean ltr = getComponentOrientation().isLeftToRight();
		horizArrowIcon = loadIcon(ltr ? "right.png":"left.png");
		backIcon = loadIcon(ltr ? "back.png" : "forward.png");
		downArrowIcon = loadIcon("down.png");

		setShownLocation(new File("."));

	}


	public void applyComponentOrientation(ComponentOrientation o) {

		ComponentOrientation old = getComponentOrientation();

		super.applyComponentOrientation(o);

		// Only re-load icons if necessary.
		if (!o.equals(old)) {
			// Fix horizontal arrow for this orientation.
			boolean ltr = o.isLeftToRight();
			Icon oldHorizIcon = horizArrowIcon;
			Icon oldBackIcon = backIcon;
			horizArrowIcon = loadIcon(ltr ? "right.png" : "left.png");
			backIcon = loadIcon(ltr ? "back.png" : "forward.png");
			for (int i=0; i<buttonPanel.getComponentCount(); i++) {
				Component c = buttonPanel.getComponent(i);
				if (c instanceof AbstractButton) {
					AbstractButton b = (AbstractButton)c;
					if (b.getIcon()==oldHorizIcon) {
						b.setIcon(horizArrowIcon);
					}
					else if (b.getIcon()==oldBackIcon) {
						b.setIcon(backIcon);
					}
				}
			}
		}

	}


	private JToggleButton createBackButton() {
		Icon backIcon = getBackIcon();
		JToggleButton b = new JToggleButton(backIcon);
		b.setUI(new BreadcrumbBarToggleButtonUI());
		b.addActionListener(listener);
		b.setOpaque(false);
		b.setBorder(new ButtonBorder());
		return b;
	}


	private AbstractButton createButton(File loc, boolean isArrow) {
		AbstractButton b = null;
		if (isArrow) {
			JToggleButton tb = new JToggleButton(horizArrowIcon);
			tb.setSelectedIcon(downArrowIcon);
tb.addChangeListener(new ChangeListener() {

	public void stateChanged(ChangeEvent e) {
		JToggleButton tb = (JToggleButton)e.getSource();
		Container parent = tb.getParent();
		Boolean activate = Boolean.valueOf(tb.getModel().isRollover() ||
				tb.getModel().isArmed());
		Boolean selected = Boolean.valueOf(tb.isSelected() ||
				tb.getModel().isPressed());
		for (int i=1; i<parent.getComponentCount(); i++) {
			if (parent.getComponent(i)==tb) {
				AbstractButton b = (AbstractButton)parent.getComponent(i-1);
				b.putClientProperty("arrowActivated", activate);
				b.putClientProperty("arrowSelected", selected);
				b.repaint();
				break;
			}
		}
	}
	
});
			b = tb;
			b.setUI(new BreadcrumbBarToggleButtonUI());
		}
		else {
			String name = loc.getName();
			if (name.length()==0 && fsv.isDrive(loc)) {
				name = loc.getAbsolutePath(); // Was "", on Windows at least
			}
			b = new JButton(name);
			b.setUI(new BreadcrumbBarButtonUI());
		}
		b.putClientProperty(PROPERTY_LOCATION, loc);
		b.addActionListener(listener);
		b.setOpaque(false);
		b.setBorder(new ButtonBorder());
		return b;
	}


	/**
	 * Lazily creates and returns the "back" icon.
	 *
	 * @return The back icon.
	 */
	private Icon getBackIcon() {
		if (backIcon==null) {
			String img = getComponentOrientation().isLeftToRight() ?
					"back.png" : "forward.png";
			backIcon = loadIcon(img);
		}
		return backIcon;
	}


	/**
	 * Returns the location shown in this breadcrumb bar.
	 *
	 * @return The location.
	 * @see #setShownLocation(File)
	 */
	public File getShownLocation() {
		return shownLocation;
	}


	public boolean isAncestorOfShownLocation(File loc) {
		File f = shownLocation;
		while (f!=null) {
			if (f.equals(loc)) {
				return true;
			}
			f = f.getParentFile();
		}
		return false;
	}


	/**
	 * Loads an icon for a breadcrumb bar button.
	 *
	 * @param name The name of the image to load.
	 * @return An icon of the iamge.
	 */
	private static Icon loadIcon(String name) {

		Icon icon = null;

		name = pkg + name;
		ClassLoader cl = BreadcrumbBar.class.getClassLoader();

		try {
			InputStream in = cl.getResourceAsStream(name);
			if (in==null) { // Possibly debugging in Eclipse
				in = new FileInputStream("src/" + name);
			}
			BufferedInputStream bin = new BufferedInputStream(in);
			icon = new ImageIcon(ImageIO.read(bin));
			bin.close();
		} catch (IOException ioe) { // Never happens
			ioe.printStackTrace();
		}

		return icon;

	}


	private void refresh() {

		buttonPanel.removeAll();
		File current = shownLocation;
		while (current!=null) {
			AbstractButton b = createButton(current, true);
			buttonPanel.add(b, 0);
			b = createButton(current, false);
			buttonPanel.add(b, 0);
			current = current.getParentFile();
		}
		AbstractButton rootButton = createButton(null, true);
		buttonPanel.add(rootButton, 0);
		backButton = createBackButton();
		buttonPanel.add(backButton, 1);
		buttonPanel.revalidate();
		buttonPanel.repaint();
		iconLabel.setIcon(fsv.getSystemIcon(shownLocation));

	}


	/**
	 * Changes the directory shown by this breadcrumb bar.  This method
	 * fires a property change event of type {@link #PROPERTY_LOCATION}.
	 *
	 * @param loc The new location.
	 * @see #getShownLocation()
	 */
	public void setShownLocation(File loc) {
		if (loc!=null && !loc.equals(shownLocation)) {
			File old = shownLocation;
			try {
				shownLocation = loc.getCanonicalFile();
			} catch (IOException ioe) {
				shownLocation = loc.getAbsoluteFile();
			}
			refresh();
			firePropertyChange(PROPERTY_LOCATION, old, shownLocation);
		}
	}


	private static class BreadcrumbPopupMenuListener
					implements PopupMenuListener {

		private JToggleButton source;

		public BreadcrumbPopupMenuListener(JToggleButton source) {
			this.source = source;
		}

		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			source.setSelected(false);
		}

		public void popupMenuCanceled(PopupMenuEvent e) {
			source.setSelected(false);
		}

	}


	private static class ButtonBorder extends BasicBorders.MarginBorder {

		public ButtonBorder() {
		}

		public void paintBorder(Component c, Graphics g, int x, int y,
								int w, int h) {
			AbstractButton b = (AbstractButton)c;
			if (b.getModel().isRollover() || b.getModel().isArmed() ||
					b.isSelected()) {
				g.setColor(Color.BLACK);
				g.drawLine(x,y, x,y+h-1);
				x += w-1;
				g.drawLine(x,y, x,y+h-1);
			}
			else if (Boolean.TRUE==b.getClientProperty("arrowActivated")) {
				g.setColor(Color.BLACK);
				g.drawLine(x,y, x,y+h-1);
			}
			else if (Boolean.TRUE==b.getClientProperty("arrowSelected")) {
				g.setColor(Color.BLACK);
				g.drawLine(x,y, x,y+h-1);
			}
		}

	}


	private class Listener implements ActionListener {

		private List rootMenuItems;

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (source==backButton) {
				for (int i=2; i<buttonPanel.getComponentCount(); i++) {
					Component c = buttonPanel.getComponent(i);
					if (c.isVisible()) {
						JButton b = (JButton)c;
						ScrollableJPopupMenu popup = new ScrollableJPopupMenu();
						addItemsFor(null, popup);
						popup.addSeparator();
						File loc = (File)b.getClientProperty(PROPERTY_LOCATION);
						addItemsFor(loc.getParentFile(), popup);
						popup.applyComponentOrientation(b.getComponentOrientation());
						popup.addPopupMenuListener(new BreadcrumbPopupMenuListener(backButton));
						displayRelativeTo(popup, backButton);
						break;
					}
				}
			}

			else if (source instanceof JToggleButton) {
				final JToggleButton tb = (JToggleButton)source;
				ScrollableJPopupMenu popup = new ScrollableJPopupMenu();
				addItemsFor((File)tb.getClientProperty(PROPERTY_LOCATION), popup);
				popup.applyComponentOrientation(getComponentOrientation());
				popup.addPopupMenuListener(new BreadcrumbPopupMenuListener(tb));
				displayRelativeTo(popup, tb);
			}

			else if (source instanceof JButton) {
				JButton b = (JButton)source;
				File loc = (File)b.getClientProperty(PROPERTY_LOCATION);
				setShownLocation(loc);
			}

			else if (source instanceof JMenuItem) {
				JMenuItem item = (JMenuItem)source;
				File newLoc = (File)item.getClientProperty(PROPERTY_LOCATION);
				setShownLocation(newLoc);
			}

		}

		private void addItemsFor(File dir, ScrollableJPopupMenu popup) {

			if (dir!=null) {
				File[] children = dir.listFiles(new FileFilter() {
					public boolean accept(File file) {
						return file.isDirectory();
					}
				});
				// children can be null e.g. on Windows, when examining an
				// empty DVD/blu-ray drive
				int count = children!=null ? children.length : 0;
				for (int i=0; i<count; i++) {
					dir = children[i];
					Icon icon = getIcon(dir);
					JMenuItem item = new JMenuItem(dir.getName(), icon);
					if (isAncestorOfShownLocation(dir)) {
						Font font = item.getFont();
						font = font.deriveFont(Font.BOLD);
						item.setFont(font);
					}
					item.putClientProperty(PROPERTY_LOCATION, dir);
					item.addActionListener(this);
					popup.addComponent(item);
				}
			}
			else { // roots
				List roots = getRoots();
				for (int i=0; i<roots.size(); i++) {
					popup.addComponent((Component)roots.get(i));
				}
			}

		}

		private JMenuItem createMenuItem(File root, String name, Icon icon) {
			JMenuItem item = new JMenuItem(name, icon);
			item.putClientProperty(PROPERTY_LOCATION, root);
			return item;
		}

		private void displayRelativeTo(JPopupMenu popup, Component c) {
			int x = 0;
			if (!popup.getComponentOrientation().isLeftToRight()) {
				x = c.getWidth() - popup.getPreferredSize().width;
			}
			popup.show(c, x, c.getHeight());
		}

		private Icon getIcon(File dir) {
			Icon icon = null;
			try {
				icon = fsv.getSystemIcon(dir);
			} catch (NullPointerException npe) {
				// Bugs in 1.4.2, fixed in 1.5+
				// TODO: Grab default directory icon
				icon = null;
			} catch (RuntimeException re) {
				throw re; // FindBugs
			} catch (/*FileNotFound,IO*/Exception e) {
				// Bugs in 1.4.2, fixed in 1.5+
				// This seems to still happen in 1.5, when trying to get an
				// icon for a folder on a DVD/blu-ray/etc.  1.6 is fine
				// TODO: Grab default directory icon
				icon = null;
			}
			return icon;
		}

		private String getName(File dir) {
			String name = null;
			name = fsv.getSystemDisplayName(dir);
			if (name.length()==0) {
				name = dir.getName();
			}
			if (name.length()==0) { // some roots, like A:\, if drive is empty
				name = dir.getAbsolutePath();
			}
			return name;
		}

		private List getRoots() {

			if (rootMenuItems==null) {

				rootMenuItems = new ArrayList(5);

//				File[] rootFiles = fsv.getRoots();
//				if (rootFiles!=null) { // Windows
//					for (int i=0; i<rootFiles.length; i++) {
//						String name = getName(rootFiles[i]);
//						Icon icon = getIcon(rootFiles[i]);
//						JMenuItem item = createMenuItem(rootFiles[i], name, icon);
//						item.addActionListener(this);
//						rootMenuItems.add(item);
//					}
//					rootMenuItems.add(new JSeparator());
//				}

				File[] rootFiles = File.listRoots();
				for (int i=0; i<rootFiles.length; i++) {
					String name = getName(rootFiles[i]);
					Icon icon = getIcon(rootFiles[i]);
					JMenuItem item = createMenuItem(rootFiles[i], name, icon);
					item.addActionListener(this);
					rootMenuItems.add(item);
				}

			}

			return rootMenuItems;

		}

	}


}