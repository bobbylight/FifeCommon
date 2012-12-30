/*
 * 09/26/2009
 *
 * BreadcrumbBar.java - A breadcrumb bar for browsing a file system.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.breadcrumbbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileSystemView;

import org.fife.ui.FSATextField;
import org.fife.ui.rtextfilechooser.FileDisplayNames;


/**
 * A file/directory "breadcrumb bar," as seen in Windows Vista's File Explorer
 * and other applications.  If the user clicks inside the breadcrumb bar, but
 * not on one of the buttons, it becomes a file system-aware text field.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class BreadcrumbBar extends JComponent {

	/**
	 * The property change event that signifies the location shown in the
	 * breadcrumb bar has changed.
	 */
	public static final String PROPERTY_LOCATION	= "breadcrumbbar.location";

	/**
	 * Mode for when the breadcrumb bar is displaying breadcrumb buttons.
	 */
	public static final int BREADCRUMB_MODE				= 0;

	/**
	 * Mode for when the breadcrumb bar is displaying a file system-aware
	 * text field.
	 */
	public static final int TEXT_FIELD_MODE				= 1;

	private File shownLocation;
	private JLabel iconLabel;
	private JPanel buttonPanel;
	private FSATextField dirField;
	private JToggleButton backButton;
	private Listener listener;
	private Icon horizArrowIcon;
	private Icon downArrowIcon;
	private Icon backIcon;

	static final String ARROW_ACTIVATED		= "arrowActivatedPropety";
	static final String ARROW_SELECTED		= "arrowSelected";

	private static final String pkg = "org/fife/ui/breadcrumbbar/";

	private static final FileSystemView fsv = FileSystemView.getFileSystemView();


	public BreadcrumbBar() {

		listener = new Listener();

		setLayout(new BorderLayout());

		iconLabel = new JLabel();
		iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		iconLabel.setOpaque(false); // See color of BreadcrumbBar

		add(iconLabel, BorderLayout.LINE_START);
		buttonPanel = new JPanel(new BreadcrumbButtonLayout());
		buttonPanel.setOpaque(false); // See color of BreadcrumbBar.
		add(buttonPanel);

		boolean ltr = getComponentOrientation().isLeftToRight();
		horizArrowIcon = loadIcon(ltr ? "right.png":"left.png");
		backIcon = loadIcon(ltr ? "back.png" : "forward.png");
		downArrowIcon = loadIcon("down.png");

		setShownLocation(new File("."));
		updateBorderAndBackground();

		addMouseListener(listener);

	}


	/**
	 * {@inheritDoc}
	 */
	public void applyComponentOrientation(ComponentOrientation o) {

		ComponentOrientation old = getComponentOrientation();

		super.applyComponentOrientation(o);
		if (getMode()==BREADCRUMB_MODE && dirField!=null) {
			dirField.applyComponentOrientation(o);
		}

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
		JToggleButton b = new BreadcrumbBarToggleButton(backIcon);
		b.addActionListener(listener);
		return b;
	}


	private AbstractButton createButton(File loc, boolean isArrow) {
		AbstractButton b = null;
		if (isArrow) {
			JToggleButton tb = new BreadcrumbBarToggleButton(horizArrowIcon);
			tb.setSelectedIcon(downArrowIcon);
tb.addChangeListener(new ChangeListener() {

	public void stateChanged(ChangeEvent e) {
		JToggleButton tb = (JToggleButton)e.getSource();
		Container parent = tb.getParent();
		if (parent==null) {
			return;
		}
		Boolean activate = Boolean.valueOf(tb.getModel().isRollover() ||
				tb.getModel().isArmed());
		Boolean selected = Boolean.valueOf(tb.isSelected() ||
				tb.getModel().isPressed());
		for (int i=1; i<parent.getComponentCount(); i++) {
			if (parent.getComponent(i)==tb) {
				AbstractButton b = (AbstractButton)parent.getComponent(i-1);
				b.putClientProperty(ARROW_ACTIVATED, activate);
				b.putClientProperty(ARROW_SELECTED, selected);
				b.repaint();
				break;
			}
		}
	}
	
});
			b = tb;
		}
		else {
			String name = loc.getName();
			if (name.length()==0 && fsv.isDrive(loc)) {
				name = loc.getAbsolutePath(); // Was "", on Windows at least
			}
			if (name.length()==0) { // Root directory "/", on OS X at least...
				if (File.separatorChar=='\\') {
					name = "\\\\"; // Windows => must be a UNC path
				}
				else {
					name = "/";
				}
			}
			b = new BreadcrumbBarButton(name);
		}
		b.putClientProperty(PROPERTY_LOCATION, loc);
		b.addActionListener(listener);
		return b;
	}


	private FSATextField createDirField() {
		FSATextField dirField = new FSATextField(true, ".");
		dirField.setBorder(null);
		Dimension size = dirField.getPreferredSize();
		size.height = buttonPanel.getHeight();
		dirField.setPreferredSize(size);
		dirField.applyComponentOrientation(getComponentOrientation());
		dirField.addActionListener(listener);
		dirField.addKeyListener(listener);
		return dirField;
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


	/**
	 * Returns a file representing the absolute path for the specified
	 * (possibly relative) path.  If it is relative, it is assumed to be
	 * relative to the current shown location.
	 *
	 * @param path The path.
	 * @return An absolute file path.
	 */
	private File getAbsoluteLocation(String path) {
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(getShownLocation(), path);
		}
		return file;
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


	/**
	 * Returns the mode this breadcrumb bar is in.
	 *
	 * @return Either {@link #BREADCRUMB_MODE} or {@link #TEXT_FIELD_MODE}.
	 * @see #setMode(int)
	 */
	public int getMode() {
		return getComponent(1)==buttonPanel ? BREADCRUMB_MODE : TEXT_FIELD_MODE;
	}


	private String getName(File dir) {
		return FileDisplayNames.get().getName(dir);
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


	protected void paintComponent(java.awt.Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0,0, getWidth(),getHeight());
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
		Icon icon = null;
		try {
			icon = fsv.getSystemIcon(shownLocation);
		} catch (NullPointerException npe) {
			// getSystemIcon() throws an NPE when shownLocation is null.  We
			// check for this before calling refresh(), but we're being
			// defensive here, and using a sensible default if it happens
			// despite our best efforts.
			icon = UIManager.getIcon("FileView.directoryIcon");
		}
		iconLabel.setIcon(icon);

		if (getMode()==TEXT_FIELD_MODE) {
			setMode(BREADCRUMB_MODE); // revalidates for us
		}
		else {
			buttonPanel.revalidate();
			buttonPanel.repaint();
		}

	}


	/**
	 * Sets the mode for this breadcrumb bar.
	 *
	 * @param mode Either {@link #BREADCRUMB_MODE} or {@link #TEXT_FIELD_MODE}.
	 * @see #getMode()
	 */
	public void setMode(int mode) {
		remove(1);
		if (mode==BREADCRUMB_MODE) {
			add(buttonPanel);
		}
		else {
			if (dirField==null) {
				dirField = createDirField();
			}
			add(dirField);
			dirField.setFileSystemAware(false);
			dirField.setText(getShownLocation().getAbsolutePath());
			dirField.setCurrentDirectory(getShownLocation());
			dirField.selectAll();
			dirField.setFileSystemAware(true);
			dirField.requestFocusInWindow();
		}
		revalidate();
		repaint(); // Sometimes needed
	}


	/**
	 * Changes the directory shown by this breadcrumb bar.  This will
	 * automatically change the mode to {@link #BREADCRUMB_MODE}.<p>
	 * 
	 * This method fires a property change event of type
	 * {@link #PROPERTY_LOCATION}.
	 *
	 * @param loc The new location.
	 * @see #getShownLocation()
	 */
	public void setShownLocation(File loc) {
		if (loc!=null) {
			loc = getAbsoluteLocation(loc.getPath());
			if (!loc.exists()) {
				// Just in case they remove a directory out from under us.
				// FileSystemView.getSystemIcon() (called from refresh() below)
				// will fail if the location doesn't exist.
				loc = new File(System.getProperty("user.dir"));
			}
			if (!loc.equals(shownLocation)) {
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
	}


	private void updateBorderAndBackground() {

		// Reset background color on LaF changes, since it is LaF-dependent.
		Color bg = UIManager.getColor("TextField.background");
		if (bg==null) { // Some LaF's might not define UIManager stuff
			bg = new JTextField().getBackground(); // Better match
		}
		setBackground(bg);
		iconLabel.setBackground(bg);

		// Reset border on LaF changes, since it is LaF-dependent.
		Border b = UIManager.getBorder("TextField.border");
		if (b==null) { // e.g. Nimbus
			b = BorderFactory.createLineBorder(Color.BLACK);
		}
		setBorder(b);

	}


	/**
	 * Overridden to ensure even non-visible components get updated.
	 */
	public void updateUI() {

		super.updateUI();

		updateBorderAndBackground();
		listener.updateUI();

		if (getMode()==BREADCRUMB_MODE && dirField!=null) {
			dirField.updateUI();
		}
		else if (getMode()==TEXT_FIELD_MODE) {
			buttonPanel.updateUI();
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


	/**
	 * Listens for events in the breadcrumb bar.
	 */
	private class Listener extends MouseAdapter implements ActionListener,
									KeyListener {

		private List rootMenuItems;

		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (source==dirField) {
				File loc = getAbsoluteLocation(dirField.getText());
				if (loc.isDirectory()) {
					setShownLocation(loc); // Switches to breadcrumb mode
				}
				else {
					UIManager.getLookAndFeel().provideErrorFeedback(dirField);
				}
			}

			else if (source==backButton) {
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

		public void keyPressed(KeyEvent e) {
			Object source = e.getSource();
			if (dirField==source) { // Always true
				if (KeyEvent.VK_ESCAPE==e.getKeyCode()) {
					setMode(BREADCRUMB_MODE); // Keep original shown location
					e.consume();
				}
			}
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}

		public void mouseClicked(MouseEvent e) {
			setMode(TEXT_FIELD_MODE);
		}

		public void updateUI() {
			int count = rootMenuItems==null ? 0 : rootMenuItems.size();
			for (int i=0; i<count; i++) {
				JMenuItem item = (JMenuItem)rootMenuItems.get(i);
				SwingUtilities.updateComponentTreeUI(item);
			}
		}

	}


}