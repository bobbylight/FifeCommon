/*
 * 09/08/2005
 *
 * UIUtil.java - Utility methods for org.fife.ui classes.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;


/**
 * Utility methods for <code>org.fife.ui</code> GUI components.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class UIUtil {

	private static boolean desktopCreationAttempted;
	private static Object desktop;
	private static final Object LOCK_DESKTOP_CREATION = new Object();

	/*
	 * -1 => Not yet determined, 0 => no, 1 => yes.
	 */
	private static int nonOpaqueTabbedPaneComponents = -1;

	/**
	 * Used for the color of hyperlinks when a LookAndFeel uses light text
	 * against a dark background.
	 */
	private static final Color LIGHT_HYPERLINK_FG = new Color(0xd8ffff);

	/**
	 * A very common border that can be shared across many components.
	 */
	private static final Border EMPTY_5_BORDER		=
							BorderFactory.createEmptyBorder(5,5,5,5);

	/**
	 * Buttons look better when they have a minimum width.  Windows does this
	 * automatically, for example.
	 */
	private static final int DEFAULT_BUTTON_SIZE = 85;


	/**
	 * Private constructor so we cannot instantiate this class.
	 */
	private UIUtil() {
	}


	/**
	 * Attempts to open a web browser to the specified URI.
	 *
	 * @param uri The URI to open.  If this is <code>null</code>, nothing
	          happens and this method returns <code>false</code>.
	 * @return Whether the operation was successful.  This will be
	 *         <code>false</code> on JRE's older than 1.6.
	 * @see #browse(URI)
	 */
	public static boolean browse(String uri) {
		if (uri==null) {
			return false;
		}
		try {
			return browse(new URI(uri));
		} catch (URISyntaxException e) {
			return false;
		}
	}


	/**
	 * Attempts to open a web browser to the specified URI.
	 *
	 * @param uri The URI to open.  If this is <code>null</code>, nothing
	          happens and this method returns <code>false</code>.
	 * @return Whether the operation was successful.  This will be
	 *         <code>false</code> on JRE's older than 1.6.
	 * @see #browse(String)
	 */
	public static boolean browse(URI uri) {

		boolean success = false;

		if (uri!=null) {
			Object desktop = getDesktop();
			if (desktop!=null) {
				try {
					Method m = desktop.getClass().getDeclaredMethod(
								"browse", new Class[] { URI.class });
					m.invoke(desktop, new Object[] { uri });
					success = true;
				} catch (RuntimeException re) {
					throw re; // Keep FindBugs happy
				} catch (Exception e) {
					// Ignore, just return "false" below.
				}
			}
		}

		return success;

	}


	/**
	 * Creates a "footer" containing two buttons (typically OK and Cancel)
	 * for a dialog.
	 * 
	 * @param ok The OK button.
	 * @param cancel The Cancel button.
	 * @return The footer component for the dialog.
	 * @see #createButtonFooter(Container) 
	 */
	public static final Container createButtonFooter(JButton ok,
			JButton cancel) {
		return createButtonFooter(ok, cancel, -1);
	}


	/**
	 * Creates a "footer" containing two buttons (typically OK and Cancel)
	 * for a dialog.
	 * 
	 * @param ok The OK button.
	 * @param cancel The Cancel button.
	 * @param topPadding The amount of padding to place above the buttons.  If
	 *        this is less than <code>0</code>, a default value of 10 pixels
	 *        is used.
	 * @return The footer component for the dialog.
	 * @see #createButtonFooter(Container) 
	 */
	public static final Container createButtonFooter(JButton ok,
			JButton cancel, int topPadding) {
		JPanel temp = new JPanel(new GridLayout(1,2, 5,5));
		temp.add(ok);
		temp.add(cancel);
		// The GridLayout forces the two buttons to be the same size, so we
		// ensure that at least one of the buttons is >= 85 pixels.
		Dimension prefSize = ok.getPreferredSize();
		if (prefSize.width<DEFAULT_BUTTON_SIZE) {
			ensureDefaultButtonWidth(cancel);
		}
		return createButtonFooter(temp, topPadding);
	}


	/**
	 * Creates a "footer" component, typically containing buttons, for a
	 * dialog.
	 *  
	 * @param buttons The container of buttons, or whatever components that
	 *        should be in the footer component.
	 * @return The footer component for the dialog.
	 * @see #createButtonFooter(JButton, JButton) 
	 */
	public static final Container createButtonFooter(Container buttons) {
		return createButtonFooter(buttons, -1);
	}


	/**
	 * Creates a "footer" component, typically containing buttons, for a
	 * dialog.
	 *  
	 * @param buttons The container of buttons, or whatever components that
	 *        should be in the footer component.
	 * @param topPadding The amount of padding to place above the buttons.  If
	 *        this is less than <code>0</code>, a default value of 10 pixels
	 *        is used.
	 * @return The footer component for the dialog.
	 * @see #createButtonFooter(JButton, JButton) 
	 * @see #createButtonFooter(Container, int, int)
	 */
	public static final Container createButtonFooter(Container buttons,
			int topPadding) {
		return createButtonFooter(buttons, topPadding, -1);
	}


	/**
	 * Creates a "footer" component, typically containing buttons, for a
	 * dialog.
	 *  
	 * @param buttons The container of buttons, or whatever components that
	 *        should be in the footer component.
	 * @param topPadding The amount of padding to place above the buttons.  If
	 *        this is less than <code>0</code>, a default value of 10 pixels
	 *        is used.
	 * @param sidePadding The amount of padding to place to the side of the
	 *        buttons.  If this is less than <code>0</code>, a default value of
	 *        8 pixels is used.
	 * @return The footer component for the dialog.
	 * @see #createButtonFooter(JButton, JButton)
	 * @see #createButtonFooter(Container, int)
	 */
	public static final Container createButtonFooter(Container buttons,
			int topPadding, int sidePadding) {

		if (topPadding<0) {
			topPadding = 10;
		}
		if (sidePadding<0) {
			sidePadding = 8;
		}

		// If it's just a single button, size it
		if (buttons instanceof JButton) {
			JButton button = (JButton)buttons;
			Dimension preferredSize = button.getPreferredSize();
			if (preferredSize.width<DEFAULT_BUTTON_SIZE) {
				preferredSize.width = DEFAULT_BUTTON_SIZE;
				button.setPreferredSize(preferredSize);
			}
		}

		JPanel panel = new JPanel(new BorderLayout());
		ComponentOrientation o = buttons.getComponentOrientation();
		int left = o.isLeftToRight() ? 0 : sidePadding;
		int right = o.isLeftToRight() ? sidePadding : 0;

		panel.setBorder(BorderFactory.createEmptyBorder(topPadding, left, 0,
							right));
		panel.add(buttons, BorderLayout.LINE_END);

		return panel;

	}


	/**
	 * Derives a color from another color by linearly shifting its blue, green,
	 * and blue values.
	 *
	 * @param orig The original color.
	 * @param darker The amount by which to decrease its r, g, and b values.
	 *        Note that you can use negative values for making a color
	 *        component "brighter."  If this makes any of the three values
	 *        less than zero, zero is used for that component value; similarly,
	 *        if it makes any value greater than 255, 255 is used for that
	 *        component's value.
	 */
	public static final Color deriveColor(Color orig, int darker) {

		int red = orig.getRed()-darker;
		int green = orig.getGreen()-darker;
		int blue = orig.getBlue()-darker;

		if (red<0) red=0; else if (red>255) red=255;
		if (green<0) green=0; else if (green>255) green=255;
		if (blue<0) blue=0; else if (blue>255) blue=255;

		return new Color(red, green, blue);

	}


	/**
	 * Ensures a button has a specific minimum width.  This can be useful if
	 * you have a dialog with very small-labeled buttons, such as "OK", for
	 * example.  Often, very small buttons look unprofessional, so artificially
	 * widening them helps.
	 *
	 * @param button The button to possibly elongate.
	 * @param width The minimum (preferred) width for the button.
	 * @see #ensureDefaultButtonWidth(JButton)
	 */
	public static final void ensureButtonWidth(JButton button, int width) {
		Dimension prefSize = button.getPreferredSize();
		if (prefSize.width<width) {
			prefSize.width = width;
			button.setPreferredSize(prefSize);
		}
	}


	/**
	 * Ensures a button has a specific minimum width, similar to what Windows
	 * does.  This usually makes the UI look a little better, especially with
	 * small buttons such as those displaying an "OK" label, for example.
	 *
	 * @param button The button to possibly elongate.
	 * @see #ensureButtonWidth(JButton, int)
	 */
	public static final void ensureDefaultButtonWidth(JButton button) {
		ensureButtonWidth(button, DEFAULT_BUTTON_SIZE);
	}


	/**
	 * Expands all nodes in the specified tree.
	 *
	 * @param tree The tree.
	 */
	public static void expandAllNodes(final JTree tree) {
		// Do separately for nested panels.
		int j=0;
		while (j<tree.getRowCount()) {
			tree.expandRow(j++);
		}
	}


	/**
	 * Fixes the orientation of the renderer of a combo box.  I can't believe
	 * Swing standard LaFs don't handle this on their own.
	 *
	 * @param combo The combo box.
	 */
	public static void fixComboOrientation(JComboBox combo) {
		ListCellRenderer r = combo.getRenderer();
		if (r instanceof Component) {
			ComponentOrientation o = ComponentOrientation.
							getOrientation(Locale.getDefault());
			((Component)r).setComponentOrientation(o);
		}
	}


	/**
	 * Fixes the orientation of the default JTable renderers (for Object,
	 * Number and Boolean) to match that of the current <code>Locale</code>
	 * (e.g. <code>ComponentOrientation.getOrientation(table.getLocale())</code>).
	 * This is needed because <code>DefaultTableCellRenderer</code> does not
	 * do this, even though <code>DefaultListCellRenderer</code> and
	 * <code>DefaultTreeCellRenderer</code> do.<p>
	 *
	 * See Sun bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6333197
	 * for more information.
	 *
	 * @param table The table to update.
	 */
	public static void fixJTableRendererOrientations(JTable table) {
		ComponentOrientation o = ComponentOrientation.
								getOrientation(table.getLocale());
		TableCellRenderer r = table.getDefaultRenderer(Object.class);
		if (r instanceof Component) { // Never null for JTable
			Component c = (Component)r;
			c.setComponentOrientation(o);
		}
		r = table.getDefaultRenderer(Number.class);
		if (r instanceof Component) { // Never null for JTable
			Component c = (Component)r;
			c.setComponentOrientation(o);
		}
		r = table.getDefaultRenderer(Boolean.class);
		if (r instanceof Component) { // Never null for JTable
			Component c = (Component)r;
			c.setComponentOrientation(o);
		}
		if (table.getTableHeader()!=null) {
			r = table.getTableHeader().getDefaultRenderer();
			if (r instanceof Component) {
				((Component)r).applyComponentOrientation(o);
			}
			else if (r instanceof
					FileExplorerTableModel.SortableHeaderRenderer) {
				r = ((FileExplorerTableModel.SortableHeaderRenderer)r).
														getDelegateRenderer();
				if (r instanceof Component) {
					((Component)r).applyComponentOrientation(o);
				}
			}
		}
	}


	/**
	 * Used by makeSpringCompactGrid.  This is ripped off directly from
	 * <code>SpringUtilities.java</code> in the Sun Java Tutorial.
	 *
	 * @param parent The container whose layout must be an instance of
	 *        <code>SpringLayout</code>.
	 * @return The spring constraints for the specified component contained
	 *         in <code>parent</code>.
	 */
	private static final SpringLayout.Constraints getConstraintsForCell(
										int row, int col,
										Container parent, int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
	}


	/**
	 * Returns the singleton <code>java.awt.Desktop</code> instance, or
	 * <code>null</code> if it is unsupported on this platform (or the JRE
	 * is older than 1.6).
	 *
	 * @return The desktop, as an {@link Object}.
	 */
	private static Object getDesktop() {

		synchronized (LOCK_DESKTOP_CREATION) {

			if (!desktopCreationAttempted) {

				desktopCreationAttempted = true;

				try {
					Class<?> desktopClazz = Class.forName("java.awt.Desktop");
					Method m = desktopClazz.
						getDeclaredMethod("isDesktopSupported");

					boolean supported = ((Boolean)m.invoke(null)).
												booleanValue();
					if (supported) {
						m = desktopClazz.getDeclaredMethod("getDesktop");
						desktop = m.invoke(null);
					}

				} catch (RuntimeException re) {
					throw re; // Keep FindBugs happy
				} catch (Exception e) {
					// Ignore; keeps desktop as null.
				}

			}

		}

		return desktop;

	}


	/**
	 * Returns an empty border of width 5 on all sides.  Since this is a
	 * very common border in GUI's, the border returned is a singleton.
	 *
	 * @return The border.
	 */
	public static Border getEmpty5Border() {
		return EMPTY_5_BORDER;
	}


	/**
	 * Returns a <code>String</code> of the form "#xxxxxx" good for use
	 * in HTML, representing the given color.
	 *
	 * @param color The color to get a string for.
	 * @return The HTML form of the color.  If <code>color</code> is
	 *         <code>null</code>, <code>#000000</code> is returned.
	 */
	public static final String getHTMLFormatForColor(Color color) {

		if (color==null) {
			return "#000000";
		}

		StringBuilder sb = new StringBuilder("#");
		int r = color.getRed();
		if (r<16) {
			sb.append('0');
		}
		sb.append(Integer.toHexString(r));

		int g = color.getGreen();
		if (g<16) {
			sb.append('0');
		}
		sb.append(Integer.toHexString(g));

		int b = color.getBlue();
		if (b<16) {
			sb.append('0');
		}
		sb.append(Integer.toHexString(b));

		return sb.toString();

	}


	/**
	 * Returns the color to use for hyperlink-style components.  This method
	 * will return <code>Color.blue</code> unless it appears that the current
	 * LookAndFeel uses light text on a dark background, in which case a
	 * brighter alternative is returned.
	 *
	 * @return The color to use for hyperlinks.
	 */
	public static final Color getHyperlinkForeground() {

		// This property is defined by all standard LaFs, even Nimbus (!),
		// but you never know what crazy LaFs there are...
		Color fg = UIManager.getColor("Label.foreground");
		if (fg==null) {
			fg = new JLabel().getForeground();
		}

		return isLightForeground(fg) ? LIGHT_HYPERLINK_FG : Color.blue;

	}


	/**
	 * Returns the mnemonic specified by the given key in a resource bundle.
	 * 
	 * @param msg The resource bundle.
	 * @param key The key for the mnemonic.
	 * @return The mnemonic, or <code>0</code> if not found.
	 */
	public static final int getMnemonic(ResourceBundle msg, String key) {
		int mnemonic = 0;
		try {
			Object value = msg.getObject(key);
			if (value instanceof String) {
				mnemonic = ((String)value).charAt(0);
			}
		} catch (MissingResourceException mre) {
			// Swallow.  TODO: When we drop 1.4/1.5 support, use containsKey().
		}
		return mnemonic;
	}


	/**
	 * Returns a pretty string value for a KeyStroke, suitable for display as
	 * the keystroke's value in a GUI.
	 *
	 * @param keyStroke The keystroke.
	 * @return The string value of the keystroke.
	 */
	public static String getPrettyStringFor(KeyStroke keyStroke) {

		if (keyStroke==null)
			return "";

		String string = KeyEvent.getKeyModifiersText(keyStroke.getModifiers());
		if (string!=null && string.length()>0)
			string += "+";
		int keyCode = keyStroke.getKeyCode();
		if (keyCode!=KeyEvent.VK_SHIFT && keyCode!=KeyEvent.VK_CONTROL &&
			keyCode!=KeyEvent.VK_ALT && keyCode!=KeyEvent.VK_META)
			string += KeyEvent.getKeyText(keyCode);
		return  string;

	}


	/**
	 * Returns whether or not this operating system should use non-opaque
	 * components in tabbed panes to show off, for example, a gradient effect.
	 *
	 * @return Whether or not non-opaque components should be used in tabbed
	 *         panes.
	 */
	static synchronized boolean getUseNonOpaqueTabbedPaneComponents() {

		if (nonOpaqueTabbedPaneComponents==-1) {

			// Check for Windows XP.
			String osname = System.getProperty("os.name");
			if (osname.toLowerCase().indexOf("windows")>-1) {
				String osver = System.getProperty("os.version");
				boolean isXPorVista = osver.startsWith("5.1") ||
								osver.startsWith("6.0");
				nonOpaqueTabbedPaneComponents = isXPorVista ? 1 : 0;
			}
			else {
				nonOpaqueTabbedPaneComponents = 0;
			}

		}

		return nonOpaqueTabbedPaneComponents==1 ? true : false;

	}


	/**
	 * Tweaks certain LookAndFeels (i.e., Windows XP) to look just a tad more
	 * like the native Look.
	 */
	public static void installOsSpecificLafTweaks() {

		String lafName = UIManager.getLookAndFeel().getName();
		String os = System.getProperty("os.name");

		// XP has insets between the edge of popup menus and the selection.
		if ("Windows XP".equals(os) && "Windows".equals(lafName)) {

			Border insetsBorder = BorderFactory.createEmptyBorder(2, 3, 2, 3);

			String key = "PopupMenu.border";
			Border origBorder = UIManager.getBorder(key);
			UIResource res = new BorderUIResource.CompoundBorderUIResource(
										origBorder, insetsBorder);
			//UIManager.put(key, res);
			UIManager.getLookAndFeelDefaults().put(key, res);

		}

	}


	/**
	 * Returns whether the specified color is "light" to use as a foreground.
	 * Colors that return <code>true</code> indicate that the current Look and
	 * Feel probably uses light text colors on a dark background.
	 *
	 * @param fg The foreground color.
	 * @return Whether it is a "light" foreground color.
	 */
	private static final boolean isLightForeground(Color fg) {
		return fg.getRed()>0xa0 && fg.getGreen()>0xa0 && fg.getBlue()>0xa0;
	}


	/**
	 * This method is ripped off from <code>SpringUtilities.java</code> found
	 * on Sun's Java Tutorial pages.  It takes a component whose layout is
	 * <code>SpringLayout</code> and organizes the components it contains into
	 * a nice grid.
	 * Aligns the first <code>rows</code> * <code>cols</code> components of
	 * <code>parent</code> in a grid. Each component in a column is as wide as
	 * the maximum preferred width of the components in that column; height is
	 * similarly determined for each row.  The parent is made just big enough
	 * to fit them all.
	 *
	 * @param parent The container whose layout is <code>SpringLayout</code>.
	 * @param rows The number of rows of components to make in the container.
	 * @param cols The number of columns of components to make.
	 * @param initialX The x-location to start the grid at.
	 * @param initialY The y-location to start the grid at.
	 * @param xPad The x-padding between cells.
	 * @param yPad The y-padding between cells.
	 */
	public static final void makeSpringCompactGrid(Container parent, int rows,
								int cols, int initialX, int initialY,
								int xPad, int yPad) {

		SpringLayout layout;
		try {
			layout = (SpringLayout)parent.getLayout();
		} catch (ClassCastException cce) {
			System.err.println("The first argument to makeCompactGrid " +
							"must use SpringLayout.");
			return;
		}

		//Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width,
						getConstraintsForCell(
									r, c, parent, cols).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints =
							getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		//Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height,
					getConstraintsForCell(r, c, parent, cols).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints =
							getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		//Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);

	}


	/**
	 * Returns the default key for a button or menu item's mnemonic, based
	 * on its root key.
	 * 
	 * @param key The key.
	 * @return The mnemonic key.
	 */
	private static final String mnemonicKey(String key) {
		return key + ".Mnemonic";
	}


	/**
	 * Returns a check box with the specified text.  If another property is
	 * defined in the resource bundle with key
	 * <code>key + ".Mnemonic"</code>, then it will be used for the mnemonic
	 * of the check box.
	 *
	 * @param bundle The resource bundle for localizing the check box.
	 * @param key The key into the bundle containing the string text value.
	 * @return The check box.
	 */
	public static final JCheckBox newCheckBox(ResourceBundle bundle,
			String key) {
		JCheckBox cb = new JCheckBox(bundle.getString(key));
		cb.setMnemonic(getMnemonic(bundle, mnemonicKey(key)));
		return cb;
	}


	/**
	 * Returns a button with the specified text.  If another property is
	 * defined in the resource bundle with key
	 * <code>key + ".Mnemonic"</code>, then it will be used for the mnemonic
	 * of the button.
	 *
	 * @param bundle The resource bundle for localizing the button.
	 * @param key The key into the bundle containing the string text value.
	 * @return The button.
	 * @see #newButton(ResourceBundle, String, String)
	 * @see #newButton(ResourceBundle, String, ActionListener)
	 * @see #newButton(ResourceBundle, String, String, ActionListener)
	 */
	public static final JButton newButton(ResourceBundle bundle, String key) {
		return newButton(bundle, key, mnemonicKey(key));
	}


	/**
	 * Returns a button with the specified text.  If another property is
	 * defined in the resource bundle with key
	 * <code>key + ".Mnemonic"</code>, then it will be used for the mnemonic
	 * of the button.
	 *
	 * @param bundle The resource bundle for localizing the button.
	 * @param key The key into the bundle containing the string text value.
	 * @param listener If non-<code>null</code>, this listener will be
	 *        added to the button.
	 * @return The button.
	 * @see #newButton(ResourceBundle, String, String)
	 * @see #newButton(ResourceBundle, String, String, ActionListener)
	 */
	public static final JButton newButton(ResourceBundle bundle,
			String key, ActionListener listener) {
		return newButton(bundle, key, mnemonicKey(key), listener);
	}


	/**
	 * Returns a button with the specified text and mnemonic.
	 *
	 * @param bundle The resource bundle for localizing the button.
	 * @param textKey The key into the bundle containing the string text value.
	 * @param mnemonicKey The key into the bundle containing a single-char
	 *        <code>String</code> value for the mnemonic.
	 * @return The button.
	 * @see #newButton(ResourceBundle, String)
	 * @see #newButton(ResourceBundle, String, ActionListener)
	 * @see #newButton(ResourceBundle, String, String, ActionListener)
	 */
	public static final JButton newButton(ResourceBundle bundle,
								String textKey, String mnemonicKey) {
		return newButton(bundle, textKey, mnemonicKey, null);
	}


	/**
	 * Returns a button with the specified text and mnemonic.
	 *
	 * @param bundle The resource bundle for localizing the button.
	 * @param textKey The key into the bundle containing the string text value.
	 * @param mnemonicKey The key into the bundle containing a single-char
	 *        <code>String</code> value for the mnemonic.
	 * @param listener If non-<code>null</code>, this listener will be
	 *        added to the button.
	 * @return The button.
	 * @see #newButton(ResourceBundle, String)
	 * @see #newButton(ResourceBundle, String, ActionListener)
	 */
	public static final JButton newButton(ResourceBundle bundle,
			String textKey, String mnemonicKey, ActionListener listener) {
		JButton b = new JButton(bundle.getString(textKey));
		b.setMnemonic(getMnemonic(bundle, mnemonicKey));
		if (listener!=null) {
			b.addActionListener(listener);
		}
		return b;
	}


	/**
	 * Returns an <code>JLabel</code> with the specified text.  If another
	 * property with name <code>key + ".Mnemonic"</code> is defined, it is
	 * used as the mnemonic for the label.
	 *
	 * @param msg The resource bundle.
	 * @param key The key into the bundle containing the string text value.
	 * @return The <code>JLabel</code>.
	 */
	public static final JLabel newLabel(ResourceBundle msg, String key) {
		return newLabel(msg, key, null);
	}


	/**
	 * Returns an <code>JLabel</code> with the specified text.  If another
	 * property with name <code>key + ".Mnemonic"</code> is defined, it is
	 * used as the mnemonic for the label.
	 *
	 * @param msg The resource bundle.
	 * @param key The key into the bundle containing the string text value.
	 * @param labelFor If non-<code>null</code>, the label is a label for
	 *        that specific component.
	 * @return The <code>JLabel</code>.
	 */
	public static final JLabel newLabel(ResourceBundle msg, String key,
			Component labelFor) {
		JLabel label = new JLabel(msg.getString(key));
		label.setDisplayedMnemonic(getMnemonic(msg, mnemonicKey(key)));
		if (labelFor!=null) {
			label.setLabelFor(labelFor);
		}
		return label;
	}


	/**
	 * Returns a radio button with the specified properties.
	 *
	 * @param msg The resource bundle in which to get properties.
	 * @param keyRoot The key into the bundle containing the radio button's
	 *        label.  If another property is defined with the name
	 *        <code>keyRoot + ".Mnemonic"</code>, it is used for the
	 *        mnemonic for the radio button.
	 * @param bg If non-<code>null</code>, the radio button is added to the
	 *        button group.
	 * @return The <code>JRadioButton</code>.
	 * @see #newRadio(ResourceBundle, String, ButtonGroup, ActionListener)
	 * @see #newRadio(ResourceBundle, String, ButtonGroup, ActionListener, boolean)
	 */
	public static final JRadioButton newRadio(ResourceBundle msg,
				String keyRoot, ButtonGroup bg) {
		return newRadio(msg, keyRoot, bg, null, false);
	}


	/**
	 * Returns a radio button with the specified properties.
	 *
	 * @param msg The resource bundle in which to get properties.
	 * @param keyRoot The key into the bundle containing the radio button's
	 *        label.  If another property is defined with the name
	 *        <code>keyRoot + ".Mnemonic"</code>, it is used for the
	 *        mnemonic for the radio button.
	 * @param bg If non-<code>null</code>, the radio button is added to the
	 *        button group.
	 * @param listener If non-<code>null</code>, the listener is added to
	 *        the radio button.
	 * @return The <code>JRadioButton</code>.
	 * @see #newRadio(ResourceBundle, String, ButtonGroup)
	 * @see #newRadio(ResourceBundle, String, ButtonGroup, ActionListener, boolean)
	 */
	public static final JRadioButton newRadio(ResourceBundle msg,
				String keyRoot, ButtonGroup bg, ActionListener listener) {
		return newRadio(msg, keyRoot, bg, listener, false);
	}


	/**
	 * Returns a radio button with the specified properties.
	 *
	 * @param msg The resource bundle in which to get properties.
	 * @param keyRoot The key into the bundle containing the radio button's
	 *        label.  If another property is defined with the name
	 *        <code>keyRoot + ".Mnemonic"</code>, it is used for the
	 *        mnemonic for the radio button.
	 * @param bg If non-<code>null</code>, the radio button is added to the
	 *        button group.
	 * @param listener If non-<code>null</code>, the listener is added to
	 *        the radio button.
	 * @param selected Whether the radio button is initially selected.
	 * @return The <code>JRadioButton</code>.
	 * @see #newRadio(ResourceBundle, String, ButtonGroup)
	 * @see #newRadio(ResourceBundle, String, ButtonGroup, ActionListener)
	 */
	public static final JRadioButton newRadio(ResourceBundle msg,
				String keyRoot, ButtonGroup bg, ActionListener listener,
				boolean selected) {
		JRadioButton radio = new JRadioButton(msg.getString(keyRoot));
		radio.setMnemonic(getMnemonic(msg, mnemonicKey(keyRoot)));
		if (bg!=null) {
			bg.add(radio);
		}
		if (listener!=null) {
			radio.addActionListener(listener);
		}
		radio.setSelected(selected);
		return radio;
	}


	/**
	 * Returns a button to add to a panel in a tabbed pane.  This method
	 * checks system properties to determine the operating system this JVM is
	 * running in; if it is determined that this OS paints its tabbed panes
	 * in a special way (such as the gradient tabbed panes in Windows XP),
	 * then the button returned is not opaque.  Otherwise, a regular (opaque)
	 * button is returned.
	 *
	 * @return A button to add to a <code>JTabbedPane</code>.
	 * @see #newTabbedPanePanel()
	 */
	public static JButton newTabbedPaneButton(String text) {
		JButton button = new JButton(text);
		if (getUseNonOpaqueTabbedPaneComponents())
			button.setOpaque(false);
		return button;
	}


	/**
	 * Returns an opaque panel so we get the cool gradient effect on Windows
	 * XP and Vista.
	 *
	 * @return A panel to add to a <code>JTabbedPane</code>.
	 * @see #newTabbedPanePanel(LayoutManager)
	 * @see #newTabbedPaneButton(String)
	 */
	public static JPanel newTabbedPanePanel() {
		JPanel panel = new JPanel();
		if (getUseNonOpaqueTabbedPaneComponents())
			panel.setOpaque(false);
		return panel;
	}


	/**
	 * Returns an opaque panel so we get the cool gradient effect on Windows
	 * XP and Vista.
	 *
	 * @param layout The layout for the panel.
	 * @return A panel to add to a <code>JTabbedPane</code>.
	 * @see #newTabbedPanePanel()
	 * @see #newTabbedPaneButton(String)
	 */
	public static JPanel newTabbedPanePanel(LayoutManager layout) {
		JPanel panel = new JPanel(layout);
		if (getUseNonOpaqueTabbedPaneComponents())
			panel.setOpaque(false);
		return panel;
	}


	/**
	 * Make a table use the right grid color on Windows Vista, when using the
	 * Windows Look and Feel.
	 *
	 * @param table The table to update.
	 */
	public static void possiblyFixGridColor(JTable table) {
		String laf = UIManager.getLookAndFeel().getClass().getName();
		if (laf.endsWith("WindowsLookAndFeel")) {
			if (Color.white.equals(table.getBackground())) {
				Color gridColor = table.getGridColor();
				if (gridColor!=null && gridColor.getRGB()<=0x808080) {
					table.setGridColor(new Color(0xe3e3e3));
				}
			}
		}
	}


	/**
	 * Sets the accessible description on the specified component.
	 *
	 * @param comp The component on which to set the accessible description.
	 * @param msg A resource bundle from which to get the description.
	 * @param key The key for the description in the resource bundle.
	 */
	public static void setDescription(JComponent comp, ResourceBundle msg,
								String key) {
		comp.getAccessibleContext().setAccessibleDescription(
											msg.getString(key));
	}


	/**
	 * Sets the rendering hints on a graphics object to those closest to the
	 * system's desktop values.<p>
	 * 
	 * See <a href="http://download.oracle.com/javase/6/docs/api/java/awt/doc-files/DesktopProperties.html">AWT
	 * Desktop Properties</a> for more information.
	 *
	 * @param g2d The graphics context.
	 * @return The old rendering hints.
	 */
	public static RenderingHints setNativeRenderingHints(Graphics2D g2d) {

		RenderingHints old = g2d.getRenderingHints();

		// Try to use the rendering hint set that is "native".
		Map<?, ?> hints = (Map<?, ?>)Toolkit.getDefaultToolkit().
						getDesktopProperty("awt.font.desktophints");
		if (hints!=null) {
			g2d.addRenderingHints(hints);
		}

		return old;

	}


}