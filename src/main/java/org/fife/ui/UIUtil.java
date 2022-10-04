/*
 * 09/08/2005
 *
 * UIUtil.java - Utility methods for org.fife.ui classes.
 * Copyright (C) 2005 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
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
public final class UIUtil {

	/**
	 * If a {@code JComponent} has this client property set to {@code Boolean.TRUE},
	 * {@link #setComponentsEnabled(Container, boolean, Component...)} will ignore
	 * that component.
	 */
	public static final String PROPERTY_ALWAYS_IGNORE = "always.ignore";

	/*
	 * -1 => Not yet determined, 0 => no, 1 => yes.
	 */
	private static int nonOpaqueTabbedPaneComponents = -1;

	/**
	 * Used for the color of hyperlinks when a LookAndFeel uses light text
	 * against a dark background.
	 */
	private static final Color LIGHT_HYPERLINK_FG = new Color(0x589df6);

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
	 * Approximate maximum length, in pixels, of a File History entry.
	 * Note that this is only  GUIDELINE, and some filenames
	 * can (and will) exceed this limit.
	 */
	private static final int MAX_FILE_PATH_LENGTH = 250;


	/**
	 * Private constructor so we cannot instantiate this class.
	 */
	private UIUtil() {
	}


	/**
	 * Adds pairs of components to a parent container using {@code SpringLayout}
	 * in a label/value layout, honoring the component orientation.
	 *
	 * @param parent The parent container to add to.
	 * @param o The component orientation.
	 * @param pairs The pairs of strings to add.  This should be an even number
	 *        of strings.
	 * @see #addLabelValuePairs(Container, ComponentOrientation, Component...)
	 * @see #makeSpringCompactGrid(Container, int, int, int, int, int, int)
	 */
	public static void addLabelValuePairs(Container parent, ComponentOrientation o, String... pairs) {

		SelectableLabel[] labels = Arrays.stream(pairs)
			.map(SelectableLabel::new)
			.toArray(SelectableLabel[]::new);
		addLabelValuePairs(parent, o, labels);
	}


	/**
	 * Adds pairs of components to a parent container using {@code SpringLayout}
	 * in a label/value layout, honoring the component orientation.
	 *
	 * @param parent The parent container to add to.
	 * @param o The component orientation.
	 * @param pairs The pairs of components to add.  This should be an even number
	 *        of components.
	 * @see #addLabelValuePairs(Container, ComponentOrientation, String...)
	 * @see #makeSpringCompactGrid(Container, int, int, int, int, int, int)
	 */
	public static void addLabelValuePairs(Container parent, ComponentOrientation o, Component... pairs) {
		if (o.isLeftToRight()) {
			for (int i = 0; i < pairs.length; i += 2) {
				parent.add(pairs[i]);
				parent.add(pairs[i + 1]);
			}
		}
		else {
			for (int i = 0; i < pairs.length; i += 2) {
				parent.add(pairs[i + 1]);
				parent.add(pairs[i]);
			}
		}
	}


	private static boolean alwaysIgnore(Component component) {
		return component instanceof JComponent &&
			Boolean.TRUE.equals(((JComponent)component).getClientProperty(PROPERTY_ALWAYS_IGNORE));
	}


	/**
	 * Attempts to open a web browser to the specified URI.
	 *
	 * @param uri The URI to open.  If this is <code>null</code>, nothing
	 *        happens and this method returns <code>false</code>.
	 * @return Whether the operation was successful
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
	 *        happens and this method returns <code>false</code>.
	 * @return Whether the operation was successful.
	 * @see #browse(String)
	 */
	public static boolean browse(URI uri) {

		boolean success = false;

		if (uri!=null) {
			Desktop desktop = getDesktop();
			if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					desktop.browse(uri);
					success = true;
				} catch (IOException ioe) {
					// Ignore, just return "false" below
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
	public static Container createButtonFooter(JButton ok,
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
	public static Container createButtonFooter(JButton ok,
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
	public static Container createButtonFooter(Container buttons) {
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
	public static Container createButtonFooter(Container buttons,
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
	public static Container createButtonFooter(Container buttons,
			int topPadding, int sidePadding) {

		if (topPadding<0) {
			topPadding = 10;
		}
		if (sidePadding<0) {
			sidePadding = 8;
		}

		// If it's just a single button, size it
		if (buttons instanceof JButton button) {
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
	 * Deletes a file, moving it into the Trash/Recycle Bin if possible/available
	 * on this system.
	 *
	 * @param file The file to delete.
	 * @return Whether the file was successfully deleted.
	 * @see #deleteFile(File, boolean)
	 */
	public static boolean deleteFile(File file) {
		return deleteFile(file, false);
	}


	/**
	 * Deletes a file, possibly moving it into the Trash/Recycle Bin.
	 *
	 * @param file The file to delete.
	 * @param hard If {@code true}, don't try to move to the recycle bin, just
	 *        perform a hard delete (equivalent to {@code File.delete()}).
	 * @return Whether the file was successfully deleted.
	 * @see #deleteFile(File)
	 */
	public static boolean deleteFile(File file, boolean hard) {

		boolean success = false;

		if (!hard) {
			Desktop desktop = getDesktop();
			if (desktop != null && desktop.isSupported(
					Desktop.Action.MOVE_TO_TRASH)) {
				try {
					success = desktop.moveToTrash(file);
				} catch (Exception e) {
					success = false; // We'll just do a hard delete
				}
			}
		}

		if (!success) {
			success = file.delete();
		}
		return success;
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
	 * @return The derived color.
	 */
	public static Color deriveColor(Color orig, int darker) {

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
	public static void ensureButtonWidth(JButton button, int width) {
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
	public static void ensureDefaultButtonWidth(JButton button) {
		ensureButtonWidth(button, DEFAULT_BUTTON_SIZE);
	}


	/**
	 * Expands all nodes in the specified tree.
	 *
	 * @param tree The tree.
	 */
	public static void expandAllNodes(JTree tree) {
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
	public static void fixComboOrientation(JComboBox<?> combo) {
		ListCellRenderer<?> r = combo.getRenderer();
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
	 * See Sun bug https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6333197
	 * for more information.
	 *
	 * @param table The table to update.
	 */
	public static void fixJTableRendererOrientations(JTable table) {
		ComponentOrientation o = ComponentOrientation.
								getOrientation(table.getLocale());
		TableCellRenderer r = table.getDefaultRenderer(Object.class);
		if (r instanceof Component c) { // Never null for JTable
			c.setComponentOrientation(o);
		}
		r = table.getDefaultRenderer(Number.class);
		if (r instanceof Component c) { // Never null for JTable
			c.setComponentOrientation(o);
		}
		r = table.getDefaultRenderer(Boolean.class);
		if (r instanceof Component c) { // Never null for JTable
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
	 * Given a combo box in which the user enters a comma-separated list of
	 * values (such as file extensions), this method returns those
	 * comma-separated values.
	 *
	 * @param combo The combo box.
	 * @return The values, or an empty array for none.
	 */
	public static String[] getCommaSeparatedValues(JComboBox<?> combo) {
		String value = (String)combo.getSelectedItem();
		return value ==  null ? new String[0] : value.trim().split("\\s*,?\\s+");
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
	private static SpringLayout.Constraints getConstraintsForCell(
										int row, int col,
										Container parent, int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
	}


	/**
	 * Returns the singleton <code>java.awt.Desktop</code> instance, or
	 * <code>null</code> if it is unsupported on this platform.
	 *
	 * @return The desktop, or {@code null}.
	 */
	public static Desktop getDesktop() {
		return Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	}


	/**
	 * Attempts to return an "attractive" shortened version of
	 * <code>fullPath</code>.  For example,
	 * <code>/home/lobster/dir1/dir2/dir3/dir4/file.out</code> could be
	 * abbreviated as <code>/home/lobster/dir1/.../file.out</code>.  Note that
	 * this method is still in the works, and isn't fully cooked yet.
	 *
	 * @param parent The component that will render the file path, for example, a
	 *        {@code JMenuItem}.
	 * @param longPath The (possibly long) absolute path to a file.
	 * @return The (possibly abbreviated) path to the file to use in displays.
	 */
	public static String getDisplayPathForFile(Component parent, String longPath) {

		// Initialize some variables.
		FontMetrics fontMetrics = parent.getFontMetrics(parent.getFont());
		int textWidth = getTextWidth(longPath, fontMetrics);

		// If the text width is already short enough to fit, don't do anything to it.
		if (textWidth <= MAX_FILE_PATH_LENGTH) {
			return longPath;
		}

		// If it's too long, we'll have to trim it down some...

		// Will be '\' for Windows, '/' for Unix and derivatives.
		String separator = System.getProperty("file.separator");

		// What we will eventually return.
		String displayString = longPath;

		// If there is no directory separator, then the string is just a file name,
		// and so we can't shorten it.  Just return the sucker.
		int lastSeparatorPos = displayString.lastIndexOf(separator);
		if (lastSeparatorPos==-1)
			return displayString;

		// Get the length of just the file name.
		String justFileName = displayString.substring(
			lastSeparatorPos+1);
		int justFileNameLength = getTextWidth(justFileName, fontMetrics);

		// If even just the file name is too long, return it.
		if (justFileNameLength > MAX_FILE_PATH_LENGTH)
			return "..." + separator + justFileName;

		// Otherwise, just keep adding levels in the directory hierarchy
		// until the name gets too long.
		String endPiece = "..." + separator + justFileName;
		int endPieceLength = getTextWidth(endPiece, fontMetrics);
		int separatorPos = displayString.indexOf(separator);
		String firstPart = displayString.substring(0, separatorPos+1);
		int firstPartLength = getTextWidth(firstPart, fontMetrics);
		String tempFirstPart = firstPart;
		int tempFirstPartLength = firstPartLength;
		while (tempFirstPartLength+endPieceLength < MAX_FILE_PATH_LENGTH) {
			firstPart  = tempFirstPart;
			separatorPos = displayString.indexOf(separator, separatorPos+1);
			if (separatorPos==-1)
				endPieceLength = 9999999;
			else {
				tempFirstPart = displayString.substring(0, separatorPos+1);
				tempFirstPartLength = getTextWidth(tempFirstPart, fontMetrics);
			}
		}

		return firstPart+endPiece;

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
	public static String getHTMLFormatForColor(Color color) {

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
	 * will return <code>Color.BLUE</code> unless it appears that the current
	 * LookAndFeel uses light text on a dark background, in which case a
	 * brighter alternative is returned.
	 *
	 * @return The color to use for hyperlinks.
	 */
	public static Color getHyperlinkForeground() {

		// This property is defined by all standard LaFs, even Nimbus (!),
		// but you never know what crazy LaFs there are...
		Color fg = UIManager.getColor("Label.foreground");
		if (fg==null) {
			fg = new JLabel().getForeground();
		}

		return isLightForeground(fg) ? LIGHT_HYPERLINK_FG : Color.BLUE;

	}


	/**
	 * Returns an image from a file in a safe fashion.
	 *
	 * @param fileName The file from which to get the image (must be .jpg,
	 *        .gif or .png).
	 * @return The image contained in the file, or <code>null</code> if the
	 *         image file was invalid.
	 */
	public static Image getImageFromFile(String fileName) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new URL("file:///" + fileName));
		} catch (MalformedURLException mue) {
			mue.printStackTrace(); // This is our fault.
		} catch (IOException e) {
			// Do nothing.
		}
		return image; // null if there was an exception thrown.
	}


	/**
	 * Returns the mnemonic specified by the given key in a resource bundle.
	 *
	 * @param msg The resource bundle.
	 * @param key The key for the mnemonic.
	 * @return The mnemonic, or <code>0</code> if not found.
	 */
	public static int getMnemonic(ResourceBundle msg, String key) {
		int mnemonic = 0;
		if (msg.containsKey(key)) {
			mnemonic = msg.getString(key).charAt(0);
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

		String string = InputEvent.getModifiersExText(keyStroke.getModifiers());
		if (string!=null && string.length()>0)
			string += "+";
		int keyCode = keyStroke.getKeyCode();
		if (keyCode!=KeyEvent.VK_SHIFT && keyCode!=KeyEvent.VK_CONTROL &&
			keyCode!=KeyEvent.VK_ALT && keyCode!=KeyEvent.VK_META)
			string += KeyEvent.getKeyText(keyCode);
		return  string;

	}


	/**
	 * Determines the width of the given <code>String</code> containing no
	 * tabs.  Note that this is simply a trimmed-down version of
	 * <code>javax.swing.text.getTextWidth</code> that has been
	 * optimized for our use.
	 *
	 * @param s  the source of the text
	 * @param metrics the font metrics to use for the calculation
	 * @return  the width of the text
	 */
	private static int getTextWidth(String s, FontMetrics metrics) {

		int textWidth = 0;

		char[] txt = s.toCharArray();
		for (char c : txt) {
			// Ignore newlines, they take up space and we shouldn't be
			// counting them.
			if (c != '\n')
				textWidth += metrics.charWidth(c);
		}
		return textWidth;
	}


	/**
	 * Returns a translucent version of a given <code>java.awt.Image</code>.
	 *
	 * @param c A component in the application.
	 * @param image The <code>java.awt.Image</code> on which to apply the
	 *        alpha filter.
	 * @param alpha The alpha value to use when defining how translucent you
	 *        want the image to be. This should be in the range 0.0f to 1.0f.
	 * @return The translucent version of the image.
	 */
	public static BufferedImage getTranslucentImage(Component c, Image image,
													float alpha) {

		// Ensure valid alpha value
		alpha = Math.max(0, alpha);
		alpha = Math.min(alpha, 1);

		// Create fast image
		BufferedImage bi;
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		bi = c.getGraphicsConfiguration().createCompatibleImage(w, h);
		Graphics2D g2d = bi.createGraphics();
		try {
			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, w, h);
			g2d.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, alpha));
			g2d.drawImage(image, 0,0, null);
		} finally {
			g2d.dispose();
		}

		return bi;

	}


	/**
	 * Returns whether this operating system should use non-opaque
	 * components in tabbed panes to show off, for example, a gradient effect.
	 *
	 * @return whether non-opaque components should be used in tabbed
	 *         panes.
	 */
	static synchronized boolean getUseNonOpaqueTabbedPaneComponents() {

		if (nonOpaqueTabbedPaneComponents==-1) {

			// Check for Windows XP.
			String osname = System.getProperty("os.name");
			if (osname.toLowerCase().contains("windows")) {
				String osver = System.getProperty("os.version");
				boolean isXPorVista = osver.startsWith("5.1") ||
								osver.startsWith("6.0");
				nonOpaqueTabbedPaneComponents = isXPorVista ? 1 : 0;
			}
			else {
				nonOpaqueTabbedPaneComponents = 0;
			}

		}

		return nonOpaqueTabbedPaneComponents == 1;

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
	 * Returns whether the current look and feel is a "dark" theme.
	 * This is a guess based on the default label foreground color
	 * being light.
	 *
	 * @return Whether a dark look and feel is installed.
	 * @see #isLightForeground(Color)
	 */
	public static boolean isDarkLookAndFeel() {
		return UIUtil.isLightForeground(new JLabel().getForeground());
	}


	/**
	 * Returns whether the specified color is "light" to use as a foreground.
	 * Colors that return <code>true</code> indicate that the current Look and
	 * Feel probably uses light text colors on a dark background.
	 *
	 * @param fg The foreground color.
	 * @return Whether it is a "light" foreground color.
	 * @see #isDarkLookAndFeel()
	 */
	public static boolean isLightForeground(Color fg) {
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
	public static void makeSpringCompactGrid(Container parent, int rows,
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
	private static String mnemonicKey(String key) {
		return key + ".Mnemonic";
	}


	/**
	 * Returns a checkbox with the specified text.  If another property is
	 * defined in the resource bundle with key
	 * <code>key + ".Mnemonic"</code>, then it will be used for the mnemonic
	 * of the checkbox.
	 *
	 * @param bundle The resource bundle for localizing the checkbox.
	 * @param key The key into the bundle containing the string text value.
	 * @return The checkbox.
	 */
	public static JCheckBox newCheckBox(ResourceBundle bundle,
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
	public static JButton newButton(ResourceBundle bundle, String key) {
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
	public static JButton newButton(ResourceBundle bundle,
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
	public static JButton newButton(ResourceBundle bundle,
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
	public static JButton newButton(ResourceBundle bundle,
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
	public static JLabel newLabel(ResourceBundle msg, String key) {
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
	public static JLabel newLabel(ResourceBundle msg, String key,
			Component labelFor) {
		JLabel label = new JLabel(msg.getString(key));
		label.setDisplayedMnemonic(getMnemonic(msg, mnemonicKey(key)));
		if (labelFor!=null) {
			label.setLabelFor(labelFor);
		}
		return label;
	}


	/**
	 * Returns a menu with localized text whose key is <code>key</code>.
	 * If another property is defined in the resource bundle with key
	 * <code>key + ".Mnemonic"</code>, then it will be used for the mnemonic
	 * of the menu.
	 *
	 * @param bundle The resource bundle for localizing the menu.
	 * @param key The key into the bundle containing the string text value.
	 * @return The menu.
	 * @see #newMenuItem(ResourceBundle, String, Action)
	 */
	public static JMenu newMenu(ResourceBundle bundle, String key) {
		JMenu menu = new JMenu(bundle.getString(key));
		menu.setMnemonic(getMnemonic(bundle, mnemonicKey(key)));
		return menu;
	}


	/**
	 * Returns a menu item with the specified text.  If another property is
	 * defined in the resource bundle with key
	 * <code>key + ".Mnemonic"</code>, then it will be used for the mnemonic
	 * of the menu item.
	 *
	 * @param bundle The resource bundle for localizing the menu item.
	 * @param key The key into the bundle containing the string text value.
	 * @param action The action to execute when the menu item is clicked.
	 * @return The menu item.
	 * @see #newMenuItem(Action)
	 * @see #newMenu(ResourceBundle, String)
	 */
	public static JMenuItem newMenuItem(ResourceBundle bundle,
			String key, Action action) {
		JMenuItem menuItem = new JMenuItem(bundle.getString(key));
		menuItem.setMnemonic(getMnemonic(bundle, mnemonicKey(key)));
		menuItem.setAction(action);
		menuItem.setToolTipText(null); // Clear SHORT_DESC being set to tool tip
		return menuItem;
	}


	/**
	 * Returns a menu item configured to use an action.
	 *
	 * @param action The action.
	 * @return The menu item.
	 * @see #newMenuItem(ResourceBundle, String, Action)
	 */
	public static JMenuItem newMenuItem(Action action) {
		JMenuItem menuItem = new JMenuItem(action);
		menuItem.setToolTipText(null); // Clear SHORT_DESC being set to tool tip
		return menuItem;
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
	public static JRadioButton newRadio(ResourceBundle msg,
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
	public static JRadioButton newRadio(ResourceBundle msg,
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
	public static JRadioButton newRadio(ResourceBundle msg,
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
	 * @param text The text to use as the button's label.
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
	 * Remove problematic actions that prevent Ctrl+PageUp/PageDown from
	 * being used for cycling through active documents.
	 *
	 * @param c The component to modify.
	 */
	public static void removeTabbedPaneFocusTraversalKeyBindings(JComponent c) {

		InputMap im = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_DOWN_MASK), "nothing");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_DOWN_MASK), "nothing");

		im = c.getInputMap();
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_DOWN_MASK), "nothing");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_DOWN_MASK), "nothing");

	}


	/**
	 * Enables or disables a container and all child components. Ignore specific children based on
	 * both an explicit list specified in the call, as well as any {@code JComponent}s with the
	 * client property FOO set to {@code Boolean.TRUE}.
	 *
	 * @param root The root component to enable or disable.
	 * @param enabled Whether to enable or disable all components.
	 * @param ignore An optional list of children to always ignore.
	 */
	public static void setComponentsEnabled(Container root, boolean enabled, Component... ignore) {
		Set<Component> ignoreSet = Set.of(ignore);
		setComponentsEnabledImpl(root, enabled, ignoreSet);
	}


	private static void setComponentsEnabledImpl(Container container, boolean enabled, Set<Component> ignore) {

		for (Component child : container.getComponents()) {
			if (!ignore.contains(child) && !alwaysIgnore(child)) {
				child.setEnabled(enabled);
				if (child instanceof Container) {
					setComponentsEnabledImpl((Container)child, enabled, ignore);
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
	 * See
	 * <a
	 * href="https://docs.oracle.com/en/java/javase/14/docs/api/java.desktop/java/awt/doc-files/DesktopProperties.html">
	 * AWT Desktop Properties</a> for more information.
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


	/**
	 * Tweaks a component's font to be a smaller or larger size.
	 *
	 * @param component The component to modify.
	 * @param amount The amount to change the font size by.  If this is
	 *        negative, the font size is decreased.
	 */
	public static void tweakFontSize(Component component, int amount) {
		Font font = component.getFont();
		float newSize = Math.max(1, font.getSize() + amount);
		newSize = Math.min(newSize, 1000);
		component.setFont(font.deriveFont(newSize));
	}


}
