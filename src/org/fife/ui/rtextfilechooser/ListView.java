/*
 * 07/14/2004
 *
 * ListView.java - The "List view" for an RTextFileChooser.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Vector;
import javax.swing.*;

import org.fife.ui.UIUtil;


/**
 * The List View for an <code>RTextFileChooser</code>.  This is similar to the
 * List View found in Microsoft Windows file choosers.
 *
 * @author Robert Futrell
 * @version 0.1
 */
class ListView extends JList implements RTextFileChooserView {

	private RTextFileChooser chooser;	// The chooser that owns this list view.
	private MouseListener mouseListener;
	private SelectionListener selectionListener;


	/**
	 * Constructor.
	 *
	 * @param chooser The file chooser that owns this list view.
	 */
	public ListView(RTextFileChooser chooser) {

		super(new DefaultListModel()); // Ensure we have a DefaultListModel.
		this.chooser = chooser;

		// Just some other stuff to keep things looking nice.
		ListViewCellRenderer cellRenderer = new ListViewCellRenderer();
		setCellRenderer(cellRenderer);
		setLayoutOrientation(JList.VERTICAL_WRAP);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				setVisibleRowCount(-1);
			}
		});

		setTransferHandler(new FileChooserViewTransferHandler(this));
		setDragEnabled(true);
		addKeyListener(new ViewKeyListener());

		// Add any listeners.
		mouseListener = new MouseListener(chooser);
		addMouseListener(mouseListener);
		selectionListener = new SelectionListener(chooser);
		addListSelectionListener(selectionListener);

		fixInputMap();

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		applyComponentOrientation(orientation);

	}


	/**
	 * Clears all files displayed by this view.
	 */
	public void clearDisplayedFiles() {
		// setListData() replaces our ListModel, with a non-DefaultListModel
		// model, which we don't want to do
		//setListData(new File[0]);
		setModel(new DefaultListModel());
	}


	/**
	 * Makes sure the specified file is visible in the view.
	 *
	 * @param file The file that is to be visible.
	 */
	public void ensureFileIsVisible(File file) {

		// This will always be true because we explicitly set the
		// model below.
		DefaultListModel model = (DefaultListModel)getModel();

		int index = model.indexOf(file);
		if (index!=-1)
			ensureIndexIsVisible(index);

	}


	/**
	 * Removes keyboard mappings that interfere with our file chooser's
	 * shortcuts.
	 */
	private void fixInputMap() {

		InputMap im = getInputMap();

		// Prevent shift+delete from doing nothing (registered to delete an
		// element?).
		im.put(KeyStroke.getKeyStroke("shift DELETE"), "none");

	}


	/**
	 * {@inheritDoc}
	 */
	public Color getDefaultFileColor() {
		return getForeground();
	}


	/**
	 * Returns the number of files currently being displayed.
	 *
	 * @return The number of files currently being displayed.
	 */
	public int getDisplayedFileCount() {
		return getModel().getSize();
	}


	/**
	 * Returns the file at the specified point in the view.
	 *
	 * @param p The point at which to look for a file.
	 * @return The file at that point (or <code>null</code> if there isn't
	 *         one???).
	 */
	public File getFileAtPoint(Point p) {
		int row = locationToIndex(p);
		return (File)getModel().getElementAt(row);
	}


	/**
	 * Gets the selected file, for use when a single file is selected.
	 *
	 * @return The selected file, or <code>null</code> if no file is
	 *         selected.
	 */
	public File getSelectedFile() {
		return (File)getSelectedValue();
	}


	/**
	 * Returns all selected files in this view.
	 *
	 * @return An array of all selected files.
	 */
	public File[] getSelectedFiles() {

		Object[] objArray = getSelectedValues();
		int length = objArray.length;

		File[] fileArray = new File[length];
		System.arraycopy(objArray,0, fileArray,0, length);

		return fileArray;

	}


	/**
	 * Returns the tool tip to display for a given mouse event.
	 *
	 * @param e The mouse event.
	 * @return The tool tip.
	 */
	public String getToolTipText(MouseEvent e) {
		String tip = null;
		Point p = e.getPoint();
		int index = locationToIndex(p);
		if (index==-1)
			return null;
		Rectangle bounds = getCellBounds(index, index);
		if (bounds.contains(p)) {
			File file = (File)getModel().getElementAt(index);
			if (file==null || file.isDirectory())
				return null;
			tip = chooser.getToolTipFor(file);
		}
		return tip;
	}


	/**
	 * Removes all listeners this view has created and added to itself.  This
	 * method is here to get around the fact that <code>finalize</code> is
	 * not going to be called as long as listeners are still registered for
	 * this view, but nobody else knows about these listeners except for the
	 * view.
	 */
	public void removeAllListeners() {
		removeMouseListener(mouseListener);
		removeListSelectionListener(selectionListener);
	}


	/**
	 * Selects the file at the specified point in the view.  If no file
	 * exists at that point, the selection should be cleared.
	 *
	 * @param p The point at which a file should be selected.
	 */
	public void selectFileAtPoint(Point p) {
		int row = locationToIndex(p);
		Rectangle bounds = getCellBounds(row, row);
		if (bounds.contains(p)) {
			setSelectedIndex(row);
			ensureIndexIsVisible(row);
		}
		else
			clearSelection();
	}


	/**
	 * Sets the files displayed by this view.
	 *
	 * @param files A vector containing the files to display.
	 */
	public void setDisplayedFiles(Vector files) {
		// setListData() replaces our ListModel, with a non-DefaultListModel
		// model, which we don't want to do
		//setListData(files);
		DefaultListModel model = new DefaultListModel();
		for (int i=0; i<files.size(); i++) {
			model.addElement(files.get(i));
		}
		setModel(model);
	}


	/**
	 * Sets whether or not this view allows the selection of multiple files.
	 *
	 * @param enabled Whether or not to allow the selection of multiple
	 *        files.
	 */
	public void setMultiSelectionEnabled(boolean enabled) {
		getSelectionModel().setSelectionMode(
				enabled ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
						ListSelectionModel.SINGLE_SELECTION);
	}


	/**
	 * Selects the specified files in the view.
	 *
	 * @param files The files to select.  If any of the files are not in
	 *        the file chooser's <code>currentDirectory</code>, then
	 *        they are not selected.
	 */
	public void setSelectedFiles(File[] files) {

		int num = files.length;
		if(num>0) {

			ListModel model = getModel();
			int modelSize = model.getSize();

			for (int i=0; i<num; i++) {

				File f1 = files[i];
				if (!f1.exists())
					continue;
				File parentFile = f1.getParentFile();
				if (!parentFile.equals(chooser.currentDirectory))
					continue;

				for (int j=0; j<modelSize; j++) {
					File f2 = (File)model.getElementAt(j);
					if (f1.equals(f2)) {
						addSelectionInterval(j,j);
						break;
					}
				}

			}

		}

	}


	/**
	 * The renderer for the list view.
	 */
	class ListViewCellRenderer extends DefaultListCellRenderer {

		private boolean isAlreadyOpened;

		public ListViewCellRenderer() {
			setOpaque(true);
		}

		private final Color getForegroundColor(File file, FileTypeInfo fti) {
			Color color = null;
			if (chooser.getShowHiddenFiles() && file.isHidden()) {
				color = chooser.getHiddenFileColor();
			}
			else {
				color = fti.labelTextColor;
			}
			return color;
		}

		public Component getListCellRendererComponent(JList list, Object value,
										int index, boolean isSelected,
										boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index,
										isSelected, cellHasFocus);
			File file = (File)value;
			FileTypeInfo info = chooser.getFileTypeInfoFor(file);
			String fileName = file.getName();

			isAlreadyOpened = chooser.isOpenedFile(file);

			// Set the text to display.  Make sure we underline already-
			// opened files.
			String text = fileName;
			if (isAlreadyOpened && chooser.getStyleOpenFiles()) {
				if (info.labelTextColor==null) {
					text = chooser.addOpenFileStyleHtml(fileName);
				}
				else {
					Color fg = null;
					if (isSelected) {
						fg = list.getSelectionForeground();
					}
					else {
						fg = getForegroundColor(file, info);
						if (fg==null) { // Special care for this case.
							fg = list.getSelectionForeground();
						}
					}
					String color = UIUtil.getHTMLFormatForColor(fg);
					text = "<font color=\"" + color + "\">" +
									fileName + "</font>";
					text = chooser.addOpenFileStyleHtml(text);
				}
			}
			setText(text);

			// Set the image according to the file type.
			setIcon(info.icon);
			if (!isSelected) {
				setForeground(getForegroundColor(file, info));
			}

			return this;

		}

		public void setBounds(int x, int y, int width, int height) {
			// TODO: For RTL locales the code below makes the cell renderer
			// content left-aligned.  Come up with a way to keep it right-
			// aligned in this case.
			if (getComponentOrientation().isLeftToRight()) {
				int w2 = Math.min(width, this.getPreferredSize().width+4);
				super.setBounds(x, y, w2, height); 
			}
			else {
				super.setBounds(x,y, width,height);
			}
		}

	}


	/**
	 * Listens for key events in the list, to allow the user to type the name
	 * of a file and have it selected.
	 */
	private class ViewKeyListener extends KeyAdapter {

		private String typed;
		private long lastTime;

		private int getNextMatch(String text, int fromCell) {

			text = text.toUpperCase();
			ListModel model = getModel();

			// First, try everything after the selected row
			for (int row=fromCell; row<model.getSize(); row++) {
				Object value = model.getElementAt(row);
				String fileName = (value instanceof File) ?
						((File)value).getName() : value.toString();
				fileName = fileName.toUpperCase();
				if (fileName.startsWith(text)) {
					return row;
				}
			}

			// Then, wrap around to before the selected row
			for (int row=0; row<fromCell; row++) {
				Object value = model.getElementAt(row);
				String fileName = (value instanceof File) ?
						((File)value).getName() : value.toString();
				fileName = fileName.toUpperCase();
				if (fileName.startsWith(text)) {
					return row;
				}
			}

			return -1;

		}

		public void keyTyped(KeyEvent e) {

			if (getModel().getSize()==0) {
				return;
			}

			long time = e.getWhen();
			if (time<lastTime+1000) {
				if (typed==null) {
					typed = String.valueOf(e.getKeyChar());
				}
				else {
					typed += e.getKeyChar();
				}
			}
			else {
				typed = String.valueOf(e.getKeyChar());
			}
			lastTime = time;

			int startCell = getLeadSelectionIndex();
			if (startCell==-1) {
				startCell = 0;
			}

			int matchCell = getNextMatch(typed, startCell);
			if (matchCell!=-1) {
				setSelectedIndex(matchCell);
				ensureFileIsVisible(getSelectedFile());
			}

		}

	}


}