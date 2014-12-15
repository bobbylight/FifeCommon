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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
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
	private HorizArrowIcon horizArrowIcon;
	private DownArrowIcon downArrowIcon;
	private Icon backIcon;
	private BufferedImage backImage;

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

		horizArrowIcon = new HorizArrowIcon();
		downArrowIcon = new DownArrowIcon();

		setShownLocation(new File("."));
		updateBorderAndBackground();

		addMouseListener(listener);

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applyComponentOrientation(ComponentOrientation o) {

		ComponentOrientation old = getComponentOrientation();

		super.applyComponentOrientation(o);
		if (getMode()==BREADCRUMB_MODE && dirField!=null) {
			dirField.applyComponentOrientation(o);
		}

		// Only re-load icons if necessary.
		if (!o.equals(old)) {
			// Fix horizontal arrow for this orientation.
			Icon oldBackIcon = backIcon;
			backIcon = createBackIcon();
			for (int i=0; i<buttonPanel.getComponentCount(); i++) {
				Component c = buttonPanel.getComponent(i);
				if (c instanceof AbstractButton) {
					AbstractButton b = (AbstractButton)c;
					if (b.getIcon()==oldBackIcon) {
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


	/**
	 * Creates and returns the "back" icon, with colors appropriate for the
	 * current Look and Feel.
	 *
	 * @return The back icon.
	 * @see #getBackIcon()
	 */
	private Icon createBackIcon() {
		String img = getComponentOrientation().isLeftToRight() ?
					"back.png" : "forward.png";
		backImage = loadImage(img);
		return new ImageIcon(backImage);
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


	private void configureDirField(FSATextField dirField) {
		dirField.setBorder(null);
		Dimension size = dirField.getPreferredSize();
		size.height = buttonPanel.getHeight();
		dirField.setPreferredSize(size);
		dirField.applyComponentOrientation(getComponentOrientation());
	}


	private FSATextField createDirField() {
		FSATextField dirField = new FSATextField(true, ".");
		configureDirField(dirField);
		dirField.addActionListener(listener);
		dirField.addKeyListener(listener);
		return dirField;
	}


	private static final JMenuItem createMenuItem(File root, String name,
			Icon icon) {
		JMenuItem item = new JMenuItem(name, icon);
		item.putClientProperty(PROPERTY_LOCATION, root);
		return item;
	}


	private static final void displayRelativeTo(JPopupMenu popup, Component c) {
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
	 * @see #createBackIcon()
	 */
	private Icon getBackIcon() {
		if (backIcon==null) {
			backIcon = createBackIcon();
		}
		return backIcon;
	}


	private static final Icon getIcon(File dir) {
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


	private static final String getName(File dir) {
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
	 * Loads an image for a breadcrumb bar button.
	 *
	 * @param name The name of the image to load.
	 * @return The image
	 */
	private static final BufferedImage loadImage(String name) {

		BufferedImage image = null;

		name = pkg + name;
		ClassLoader cl = BreadcrumbBar.class.getClassLoader();

		try {
			InputStream in = cl.getResourceAsStream(name);
//			if (in==null) { // Possibly debugging in Eclipse
//				in = new FileInputStream("src/" + name);
//			}
			BufferedInputStream bin = new BufferedInputStream(in);
			image = ImageIO.read(bin);
			bin.close();
		} catch (IOException ioe) { // Never happens
			ioe.printStackTrace();
		}

		return image;

	}


	@Override
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


//	private static final void replaceColor(BufferedImage image, Color oldColor,
//										Color newColor) {
//
//		int[] imgArray = ((DataBufferInt)image.getRaster().
//				getDataBuffer()).getData();
//		int o = oldColor.getRGB();
//		int n = newColor.getRGB();
//		
//		for (int i=0; i<imgArray.length; i++) {
//			if (imgArray[i]==o) {
//				imgArray[i] = n;
//			}
//		}
//
//	}


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

		JTextField tempField = null;

		// Reset background color on LaF changes, since it is LaF-dependent.
		Color bg = UIManager.getColor("TextField.background");
		if (bg==null) { // Some LaF's might not define UIManager stuff
			tempField = new JTextField();
			bg = tempField.getBackground(); // Better match
		}
		setBackground(bg);
		iconLabel.setBackground(bg);

		// Reset border on LaF changes, since it is LaF-dependent.
		Border border = UIManager.getBorder("TextField.border");
		if (border==null) { // e.g. Nimbus
//			if (tempField==null) {
//				tempField = new JTextField();
//			}
//			border = tempField.getBorder();
//			if (border==null || border instanceof EmptyBorder) { // WebLaF
				border = BorderFactory.createLineBorder(Color.BLACK);
//			}
		}
		setBorder(border);

		Icon oldBackIcon = backIcon;
		backIcon = createBackIcon();
		for (int i=0; i<buttonPanel.getComponentCount(); i++) {
			Component c = buttonPanel.getComponent(i);
			if (c instanceof AbstractButton) {
				AbstractButton b = (AbstractButton)c;
				if (b.getIcon()==oldBackIcon) {
					b.setIcon(backIcon);
				}
			}
		}

	}


	/**
	 * Overridden to ensure even non-visible components get updated.
	 */
	@Override
	public void updateUI() {

		super.updateUI();

		updateBorderAndBackground();
		listener.updateUI();

		if (buttonPanel!=null) {
			buttonPanel.updateUI();
		}
		if (dirField!=null) {
			dirField.updateUI();
			configureDirField(dirField); // Depends on buttonPanel being updated
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


	private class DownArrowIcon implements Icon {

		public void paintIcon(Component c, Graphics g, int x, int y) {

			Color fg = c.getForeground();
			g.setColor(fg);

			((Graphics2D)g).translate(x, y);
			g.drawLine(0,3, 6,3);
			g.drawLine(1,4, 5,4);
			g.drawLine(2,5, 4,5);
			g.drawLine(3,6, 3,6);
			((Graphics2D)g).translate(-x, -y);

		}

		public int getIconWidth() {
			return 8;
		}

		public int getIconHeight() {
			return 10;
		}

	}


	private class HorizArrowIcon implements Icon {

		private final int brighten(int component) {
			return component<230 ? (component+20) : component;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {

			Color fg = c.getForeground();
			g.setColor(fg);
			Color highlight = new Color(brighten(fg.getRed()),
					brighten(fg.getGreen()), brighten(fg.getBlue()));

			((Graphics2D)g).translate(x, y);
			ComponentOrientation o = getComponentOrientation();
			if (o.isLeftToRight()) {
				g.drawLine(2,1, 2,6);
				g.drawLine(3,2, 3,5);
				g.drawLine(4,3, 4,4);
				g.setColor(highlight);
				g.drawLine(2,0, 2,0);
				g.drawLine(3,1, 3,1);
				g.drawLine(4,2, 4,2);
				g.drawLine(5,3, 5,3);
			}
			else {
				g.drawLine(5,1, 5,6);
				g.drawLine(4,2, 4,5);
				g.drawLine(3,3, 3,4);
				g.setColor(highlight);
				g.drawLine(5,0, 5,0);
				g.drawLine(4,1, 4,1);
				g.drawLine(3,2, 3,2);
				g.drawLine(2,3, 2,3);
			}
			((Graphics2D)g).translate(-x, -y);

		}

		public int getIconWidth() {
			return 8;
		}

		public int getIconHeight() {
			return 8;
		}

	}


	/**
	 * Listens for events in the breadcrumb bar.
	 */
	private class Listener extends MouseAdapter implements ActionListener,
									KeyListener {

		private List<JMenuItem> rootMenuItems;

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
				List<JMenuItem> roots = getRoots();
				for (int i=0; i<roots.size(); i++) {
					popup.addComponent(roots.get(i));
				}
			}

		}

		private List<JMenuItem> getRoots() {

			if (rootMenuItems==null) {

				rootMenuItems = new ArrayList<JMenuItem>(5);

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

		@Override
		public void mouseClicked(MouseEvent e) {
			setMode(TEXT_FIELD_MODE);
		}

		public void updateUI() {
			int count = rootMenuItems==null ? 0 : rootMenuItems.size();
			for (int i=0; i<count; i++) {
				JMenuItem item = rootMenuItems.get(i);
				SwingUtilities.updateComponentTreeUI(item);
			}
		}

	}


}