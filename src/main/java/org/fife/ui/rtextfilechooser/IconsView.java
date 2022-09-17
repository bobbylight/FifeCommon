/*
 * 07/14/2004
 *
 * IconsView.java - The "Icons view" for an RTextFileChooser.
 * Copyright (C) 2004 Robert Futrell
 * https://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.Serial;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.DesktopPaneUI;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.*;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;


/**
 * An icons view for a file chooser similar to the "Icons" view found in
 * Microsoft Windows file choosers.<p>
 * Note that this class is NOT efficient when compared to other file chooser
 * view such as <code>ListView</code> and <code>DetailsView</code> because it
 * does not paint its "files" via a renderer; rather, all files are represented
 * by separate <code>Component</code>s.  This ends up taking a huge amount of
 * memory when viewing many files.
 *
 * @author Robert Futrell
 * @version 0.2
 */
class IconsView extends IconDesktopPane implements RTextFileChooserView {

	private static final int SPACING			= 10;	// Spacing between icons.
	private static final int DEFAULT_ICON_WIDTH	= 64;
	private static final int DEFAULT_ROW_SIZE	= 5;		// Default # of icons per row.

	private RTextFileChooser chooser;
	private Color selectionForeground;
	private Color selectionBackground;
	private MouseListener mouseListener;


	/**
	 * Constructor.
	 *
	 * @param chooser The owning file chooser.
	 */
	IconsView(RTextFileChooser chooser) {

		this.chooser = chooser;
		setImportantColors();

		ComponentOrientation orientation = chooser.getComponentOrientation();
		applyComponentOrientation(orientation);

		// Add any listeners.
		mouseListener = new MouseListener(chooser);
		addMouseListener(mouseListener);
		addComponentListener(new ComponentAdapter() {
						@Override
						public void componentShown(ComponentEvent e) {
							refresh();// This makes icons organized neatly on first appearance.
						}
						});

	}


	/**
	 * Clears all files displayed by this view.
	 */
	@Override
	public void clearDisplayedFiles() {

		// Selected files are kept in a separate array so we clear them separately.
		clearSelection();

		Component[] components = getComponents();
		int componentCount = components.length;
		for (Component component : components) {
			if (component instanceof IconInternalFrame) {
				remove(component);
			}
		}
		if (componentCount>0)
			repaint();
	}


	/**
	 * Makes sure there are no selected files in this view.
	 */
	@Override
	public void clearSelection() {
		JInternalFrame[] frames = getSelectedFrames();
		if (frames==null)
			return;
		for (JInternalFrame frame : frames) {
			try {
				frame.setSelected(false);
			} catch (PropertyVetoException pve) {
				// Do nothing
			}
		}
	}


	/**
	 * Makes sure the specified file is visible in the view.
	 *
	 * @param file The file that is to be visible.
	 */
	@Override
	public void ensureFileIsVisible(File file) {
		System.err.println("Not implemented!");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Color getDefaultFileColor() {
		return getForeground();
	}


	/**
	 * Returns the number of files currently being displayed.
	 *
	 * @return The number of files currently being displayed.
	 */
	@Override
	public int getDisplayedFileCount() {
		return getComponentCount();
	}


	/**
	 * Returns the file at the specified point in the view.
	 *
	 * @param p The point at which to look for a file.
	 * @return The file at that point (or <code>null</code> if there isn't
	 *         one???  This may very-well be view-dependent).
	 */
	@Override
	public File getFileAtPoint(Point p) {
		Component c = getComponentAt(p);
		if (c instanceof IconInternalFrame)
			return ((IconInternalFrame)c).getFile();
		return null;
	}


	/**
	 * Gets the selected file, for use when a single file is selected.
	 *
	 * @return The selected file, or <code>null</code> if no file is
	 *         selected.
	 */
	@Override
	public File getSelectedFile() {
		IconInternalFrame frame = (IconInternalFrame)getSelectedFrame();
		if (frame!=null)
			return frame.getFile();
		return null;
	}


	/**
	 * Returns all selected files in this view.
	 *
	 * @return An array of all selected files.
	 */
	@Override
	public File[] getSelectedFiles() {
		JInternalFrame[] frames = getSelectedFrames();
		File[] files;
		if (frames!=null) {
			int num = frames.length;
			files = new File[num];
			for (int i=0; i<num; i++)
				files[i] = ((IconInternalFrame)frames[i]).getFile();
			return files;
		}
		return new File[0]; // Necessary for standards...
	}



	/**
	 * Organizes the icons nicely in the visible screen space.
	 */
	public void refresh() {

		// Resize so that our width is that of the viewport in the scrollpane.
		// Remember there's spacing at the front too.
		Dimension size = getVisibleRect().getSize();
		int defaultWidth = (SPACING+DEFAULT_ICON_WIDTH)*DEFAULT_ROW_SIZE;
		int width = Math.max(size.width, defaultWidth);

		int x = SPACING;
		int y = SPACING;

		JInternalFrame[] frames = getAllFrames();
		int num = frames==null ? 0 : frames.length;
		for (int i=0; i<num; i++) {
			frames[i].setLocation(x,y);
			x += DEFAULT_ICON_WIDTH + SPACING;
			if (x+DEFAULT_ICON_WIDTH>width) {
				x = SPACING;
				y += frames[i].getHeight() + SPACING;
			}

		}

		size.height = y+DEFAULT_ICON_WIDTH;
		setPreferredSize(size);
		setSize(size);			// Causes the scroll pane to revalidate.

	}


	/**
	 * Removes all listeners this view has created and added to itself.  This
	 * method is here to get around the fact that <code>finalize</code> is
	 * not going to be called as long as listeners are still registered for
	 * this view, but nobody else knows about these listeners except for the
	 * view.
	 */
	@Override
	public void removeAllListeners() {
		clearDisplayedFiles(); // Just in case.
		removeMouseListener(mouseListener);
	}


	/**
	 * Selects the file at the specified point in the view.  If no file
	 * exists at that point, the selection should be cleared.
	 *
	 * @param p The point at which a file should be selected.
	 */
	@Override
	public void selectFileAtPoint(Point p) {
		clearSelection();
		Component c = getComponentAt(p);
		if (c instanceof IconInternalFrame) { // could return "this".
			IconInternalFrame frame = (IconInternalFrame)c;
			addSelectedFrame(frame);
			try {
				frame.setSelected(true);
			} catch (PropertyVetoException pve) {}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDisplayedFiles(java.util.List<File> files) {

		// Clears view and also clears "selected files" vector.
		clearDisplayedFiles();

		for (File file : files) {

			// Create the internal frame.
			// Set the image and text color according to the file type.
			FileTypeInfo info = chooser.getFileTypeInfoFor(file);
			Color fg = (chooser.getShowHiddenFiles() && file.isHidden()) ?
							chooser.getHiddenFileColor() :
							info.labelTextColor;
			IconInternalFrame frame = new IconInternalFrame(file,
									info.icon, fg);

			add(frame);
			try {
				frame.setSelected(false);
			} catch (PropertyVetoException pve) {
				// Do nothing
			}

		}

		refresh();

	}


	/**
	 * Sets important colors that aren't quite right for some LookAndFeels
	 * (since we do custom painting) after the LaF is changed.
	 */
	private void setImportantColors() {
		// More foolproof to use actual components, as not all LAFs will
		// set "List.foreground", etc.
		setForeground(new JLabel().getForeground());
		JList<Object> list = new JList<>();
		setBackground(list.getBackground());
		selectionForeground = list.getSelectionForeground();
		selectionBackground = list.getSelectionBackground();
	}


	/**
	 * Sets whether this view allows the selection of multiple files.
	 *
	 * @param enabled whether to allow the selection of multiple
	 *        files.
	 */
	@Override
	public void setMultiSelectionEnabled(boolean enabled) {
		// This method doesn't do anything; rather, the view actually polls
		// the file chooser when a new file is selected, to find out whether
		// or not the user can multi-select.
	}


	/**
	 * Selects the specified files in the view.
	 *
	 * @param files The files to select.  If any of the files are not in
	 *        the file chooser's <code>currentDirectory</code>, then
	 *        they are not selected.
	 */
	@Override
	public void setSelectedFiles(File[] files) {

		clearSelection();

		int componentCount = getComponentCount();

		for (File file : files) {
			for (int j = 0; j < componentCount; j++) {
				IconInternalFrame frame = (IconInternalFrame)getComponent(j);
				if (frame.getFile().equals(file)) {
					addSelectedFrame(frame);
					break; // Go to the next iteration in the "i" loop.
				}
			}
		}

	}


	@Override
	public void setUI(DesktopPaneUI ui) {
		super.setUI(ui);
		setImportantColors();
	}


	/**
	 * A label that renders an icon.
	 */
	class IconLabel extends JLabel {

		@Serial
		private static final long serialVersionUID = 1L;

		IconLabel(String name, Icon icon, int format) {
			super(name, icon, format);
			setToolTipText("freaky"); // We must do this for tooltips to work (???).
		}

		@Override
		public Point getToolTipLocation(MouseEvent e) {
			Point p = e.getPoint();
			p.x += 10;
			p.y += 10;
			return p;
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			String tip = null;
			// Put (x,y) in IconsView coordinate system.
			MouseEvent e2 = SwingUtilities.convertMouseEvent(this, e, IconsView.this);
			Component component = IconsView.this.getComponentAt(e2.getPoint());
			// Must check class as we can get IconsView...
			if (component instanceof IconInternalFrame) {
				IconInternalFrame frame = (IconInternalFrame)component;
				File file = frame.getFile();
				if (file==null || file.isDirectory())
					return null;
				tip = chooser.getToolTipFor(file);
			}
			return tip;
		}

		@Override
		public boolean isOpaque() {
			return true;
		}

		// Overridden so we can paint the background like it looks in Windows'
		// "Icon View."
		@Override
		public void paint(Graphics g) {
			g.setColor(IconsView.this.getBackground());
			Rectangle bounds = getBounds();
			IconInternalFrame frame = (IconInternalFrame)getParent().getParent().getParent().getParent();
			if (frame==null || !frame.isSelected()) {
				g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
			}
			else {
				Icon icon = getIcon();
				int iconHeight = icon.getIconHeight();
				g.fillRect(0,0, bounds.width,iconHeight);
				g.setColor(selectionBackground);
				g.fillRect(0,iconHeight+1,bounds.width,bounds.height-iconHeight);
			}
			getUI().paint(g, this);
		}

	}


	/**
	 * An internal frame representing a file.
	 */
	@SuppressWarnings("checkstyle:MemberName")
	class IconInternalFrame extends JInternalFrame implements
					java.awt.event.MouseListener, MouseMotionListener {

		@Serial
		private static final long serialVersionUID = 1L;

		private JLabel label;
		private File file;
		private int defaultHeight;
		private Color unselectedFG;

		IconInternalFrame(File file, Icon icon, Color fg) {
			this(file, icon, fg, 0,0);
		}

		IconInternalFrame(File file, Icon icon, Color color, int x, int y) {

			this.file = file;
			unselectedFG = color;

			// We want to receive mouse events about ourselves.
			enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);


			label = new IconLabel(file.getName(), icon, JLabel.CENTER);
			label.addMouseListener(this);
			label.addMouseMotionListener(this);
			label.setVerticalTextPosition(JLabel.BOTTOM);
			label.setHorizontalTextPosition(JLabel.CENTER);

			getContentPane().add(label);
			//setBorder(BorderFactory.createLineBorder(Color.BLACK));
			setBorder(null);
			pack();
			setLocation(x,y);
			defaultHeight = getPreferredSize().height;
			setSize(DEFAULT_ICON_WIDTH, defaultHeight);
			setVisible(true);

		}

		public File getFile() {
			return file;
		}

		public String getHTMLFileName(String fileName) {

			int br;
			int count = 0;
			Font font = getFont();
			if (font==null) return ""; // Happens at the very beginning.
			FontMetrics fm = getFontMetrics(font);

			StringBuilder result = new StringBuilder();

			do {
				// Subtract 8 from the width for a little "cushion" on
				// each side, and to prevent the HTML view from messing
				// us up view-wise.
				Segment s = new Segment(fileName.toCharArray(), 0,fileName.length());
				br = Utilities.getBreakLocation(s, fm, 0f, getWidth()-8,
											null, 0);
				result.append(fileName, 0, br);
				fileName = fileName.substring(br);
				if (fileName.length()>0)
					result.append("<br>");
				count++;
			} while (fileName.length()>0);

			// Make sure the icon is big enough to display all the text...
			setSize(DEFAULT_ICON_WIDTH, defaultHeight+(count-1)*fm.getHeight());

			return result.toString();

		}

		@Override
		public void setSelected(boolean selected) throws PropertyVetoException {

			super.setSelected(selected);

			// Have the text field/combo box updated too.
			chooser.synchronizeTextFieldWithView();

			if (selected) {

				// Set colors.
				label.setForeground(selectionForeground);

				// Set text to display.
				String text = file.getName();
				String beginning;
				String ending;
				if (chooser.isOpenedFile(file) && chooser.getStyleOpenFiles()) {
					beginning = "<html><body><center><u>";
					ending = "</u></center></body></html>";
				}
				else {
					beginning = "<html><body><center>";
					ending = "</center></body></html>";
				}

				label.setText(beginning + getHTMLFileName(text) + ending);

			}
			// FIXME:  Underlining messes up view due to implied <p> usage...
			else {
				// Set colors and text to display.
				label.setForeground(unselectedFG);
				String beginning;
				String ending;
				if (chooser.isOpenedFile(file)) {
					beginning = "";//"<html><u>";
					ending = "";//"</u></html>";
				}
				else {
					beginning = "";
					ending = "";
				}
				label.setText(beginning + file.getName() + ending);
				setSize(DEFAULT_ICON_WIDTH, defaultHeight);
			}
		}

		// Overridden to always have our special UI.
		@Override
		public void setUI(InternalFrameUI ui) {
			super.setUI(new BasicInternalFrameUI(this) {
					@Override
					protected MouseInputAdapter createBorderListener(JInternalFrame w) {
						return null;
					}
					@Override
					protected void installMouseHandlers(JComponent c) {
					}
					});
			setRootPaneCheckingEnabled(false);
			((BasicInternalFrameUI)getUI()).setNorthPane(null);
			setRootPaneCheckingEnabled(true);
		}

		// _x & _y are the mousePressed location in absolute coordinate system
		int _x;
		int _y;
		// __x & __y are the mousePressed location in source view's coordinate system
		int __x;
		int __y;
		Rectangle startingBounds;

		@Override
		public void mousePressed(MouseEvent e) {

			int modifiers = e.getModifiersEx();
			if (SwingUtilities.isLeftMouseButton(e)) {

				Point p = SwingUtilities.convertPoint(this,
											e.getX(), e.getY(), null);
				__x = e.getX();
				__y = e.getY();
				_x = p.x;
				_y = p.y;
				startingBounds = getBounds();

				// If multiselection isn't enabled, or if they don't hold
				// down shift or control while clicking, we need to clear
				// the selection.
				if (!chooser.isMultiSelectionEnabled() ||
					((modifiers&InputEvent.SHIFT_DOWN_MASK)==0 &&
					 (modifiers&InputEvent.CTRL_DOWN_MASK)==0)) {
					clearSelection();
				}

				// Now, select this newly-clicked on file.
				try {
					setSelected(true);
				} catch (PropertyVetoException e1) { }

				Insets i = getInsets();

				Point ep = new Point(__x, __y);

				if (ep.x > i.left && ep.y > i.top && ep.x < getWidth() - i.right) {
					getDesktopManager().beginDraggingFrame(this);
				}

			} // End of if ((e.getModifiers()&InputEvent.BUTTON1_MASK)!=0)

		}


		@Override
		public void mouseReleased(MouseEvent e) {

			// If they just released a left-click, stop any dragging.
			if (SwingUtilities.isLeftMouseButton(e)) {
				getDesktopManager().endDraggingFrame(this);
				_x = 0;
				_y = 0;
				__x = 0;
				__y = 0;
				startingBounds = null;
			}

			// If they just released a right-click, select the item under the
			// mouse.  This check is because of the internal frames and their
			// labels evidently not sending out mouseClicked events unless
			// they are clicked when they already have focus...
			else {

				// If they clicked on a label and not the icons view, we need to
				// convert the click-point to the proper space.
				Point p = e.getPoint();
				Component source = (Component)e.getSource(); // Always an internal frame?
				if (source!=IconsView.this)
					p = SwingUtilities.convertPoint(source, p, IconsView.this);

				// Pretend like the view is the one who received the mouse click,
				// not the internal frame.  Also pretend like it's a click-
				// event and not a release-event, so the popup menu appears.
				mouseListener.mouseClicked(
					new MouseEvent(IconsView.this, MouseEvent.MOUSE_CLICKED,
								e.getWhen(), e.getModifiersEx(),
								p.x,p.y, 1,
								e.isPopupTrigger(), e.getButton())
				);

			}

		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (startingBounds == null) {
				// (STEVE) Yucky work around for bug ID 4106552
				return;
			}

			Point p = SwingUtilities.convertPoint(this,
										e.getX(), e.getY(), null);
			int deltaX = _x - p.x;
			int deltaY = _y - p.y;
			int newX;
			int newY;
			Insets i = getInsets();

			// Move the window.
			if (!SwingUtilities.isLeftMouseButton(e)) {
				// don't allow moving of frames if the left mouse
				// button was not used.
				return;
			}

			int pWidth;
			int pHeight;
			Dimension s = getParent().getSize();
			pWidth = s.width;
			pHeight = s.height;


			newX = startingBounds.x - deltaX;
			newY = startingBounds.y - deltaY;

			// Make sure we stay in-bounds
			if(newX + i.left <= -__x)
				newX = -__x - i.left + 1;
			if(newY + i.top <= -__y)
				newY = -__y - i.top + 1;
			if(newX + __x + i.right >= pWidth)
				newX = pWidth - __x - i.right - 1;
			if(newY + __y + i.bottom >= pHeight)
				newY =  pHeight - __y - i.bottom - 1;

			getDesktopManager().dragFrame(this, newX, newY);

		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {

			// If they clicked on a label and not the icons view, we need to
			// convert the click-point to the proper space.
			Point p = e.getPoint();
			Component source = (Component)e.getSource(); // Always an internal frame?
			if (source!=IconsView.this)
				p = SwingUtilities.convertPoint(source, p, IconsView.this);

			// Pretend like the view is the one who received the mouse click,
			// not the internal frame.
			mouseListener.mouseClicked(
				new MouseEvent(IconsView.this, e.getID(), e.getWhen(), e.getModifiersEx(),
							p.x,p.y, e.getClickCount(),
							e.isPopupTrigger(), e.getButton())
			);

		}

		// This is only called for the internal frame itself, not its JLabel,
		// so we never really see it called.  But it's still safe to have it,
		// and it prevents other listeners from hearing stuff.
		@Override
		public void processMouseEvent(MouseEvent e) {
			switch (e.getID()) {
				case MouseEvent.MOUSE_PRESSED:
					mousePressed(e);
					return;
				case MouseEvent.MOUSE_RELEASED:
					mouseReleased(e);
					return;
				case MouseEvent.MOUSE_CLICKED:
					mouseClicked(e);
			}
			//super.processMouseEvent(e); // Don't need this...
		}

		@Override
		public void processMouseMotionEvent(MouseEvent e) {
			switch (e.getID()) {
				case MouseEvent.MOUSE_DRAGGED:
					mouseDragged(e);
					return;
			}
			//super.processMouseMotionEvent(e); // Don't need this...
		}

		@Override
		public String toString() {
			return "[IconInternalFrame: file==" + getFile() + "]";
		}

	}


}
