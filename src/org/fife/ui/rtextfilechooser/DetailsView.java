/*
 * 07/14/2004
 *
 * DetailsView.java - The "Details view" (i.e., table view) for an
 * RTextFileChooser.
 * Copyright (C) 2004 Robert Futrell
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
package org.fife.ui.rtextfilechooser;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;

import org.fife.ui.FileExplorerTableModel;


/**
 * A "details view" (i.e., a table view) for an <code>RTextFileChooser</code>.
 * This is similar to the Details view found in Microsoft Windows file
 * choosers.
 *
 * @author Robert Futrell
 * @version 0.1
 */
class DetailsView extends JTable implements RTextFileChooserView {

	private RTextFileChooser chooser; // The chooser this view is in.
	private MouseListener mouseListener;
	private SelectionListener selectionListener;

	private static final int MAX_NAME_COLUMN_SIZE		= 150;


	/**
	 * Creates a details view.
	 *
	 * @param chooser The file chooser that owns this view.
	 * @param nameString The title for the "Name" column.
	 * @param sizeString The title for the "Size" column.
	 * @param typeString The title for the "Type" column.
	 * @param statusString The title for the "Status" column.
	 * @param lastModifiedString The title for the "Date Modified" column.
	 */
	public DetailsView(RTextFileChooser chooser, String nameString,
					String sizeString, String typeString, String statusString,
					String lastModifiedString) {

		this.chooser = chooser;

		// Create the table model, then wrap it in a sorter model.
		DetailsViewModel dvm = new DetailsViewModel(nameString, sizeString,
							typeString, statusString, lastModifiedString);
		FileExplorerTableModel sorter = new FileExplorerTableModel(dvm);
		setModel(sorter);
		sorter.setTable(this);

		// Prevent this table from interpreting Enter to mean "move to the
		// next row."
		InputMap tableInputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		tableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "none");

		// Prevent this table from swalling F2 (I think this causes editable
		// JTables to go into editing mode, but we want our parent dialog to
		// handle this keypress).
		tableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "none");

		// Make this table look just a little nicer.
		setIntercellSpacing(new Dimension(0,0));
		setShowGrid(false);
		setColumnSelectionAllowed(false);

		// Set the renderer for each column.  We must do this even for normal
		// columns because otherwise the columns we sort by won't be
		// gray-highlighted.
		int columnCount = getColumnCount();
		TableColumnModel columnModel = getColumnModel();
		getColumnModel().getColumn(0).setCellRenderer(new FileTableColumnRenderer());
		for (int i=1; i<columnCount-1; i++)
			columnModel.getColumn(i).setCellRenderer(new DefaultTableCellRenderer());
		columnModel.getColumn(columnCount-1).setCellRenderer(new DateCellRenderer());
		sorter.setColumnComparator(File.class, new FileComparator());

		ComponentOrientation orientation = chooser.getComponentOrientation();
		applyComponentOrientation(orientation);
		for (int i=0; i<columnCount; i++) {
			Component c = (Component)columnModel.getColumn(i).getCellRenderer();
			c.applyComponentOrientation(orientation);
		}

		// Add any listeners.
		mouseListener = new MouseListener(chooser);
		addMouseListener(mouseListener);
		selectionListener = new SelectionListener(chooser);
		getSelectionModel().addListSelectionListener(selectionListener);

		setTransferHandler(new FileChooserViewTransferHandler(this));
		setDragEnabled(true);

	}


	/**
	 * Clears all files displayed by this view.
	 */
	public void clearDisplayedFiles() {
		((DefaultTableModel)getModel()).setRowCount(0); // Just so the file list gets erased.
	}


	/**
	 * Makes sure the specified file is visible in the view.
	 *
	 * @param file The file that is to be visible.
	 */
	public void ensureFileIsVisible(File file) {
		int row = getRowFor(file);
		if (row!=-1)
			scrollRectToVisible(getCellRect(row, 0, true));
	}


	/**
	 * Returns the number of files currently being displayed.
	 *
	 * @return The number of files currently being displayed.
	 */
	public int getDisplayedFileCount() {
		return getRowCount();
	}


	/**
	 * Returns the file at the specified point in the view.
	 *
	 * @param p The point at which to look for a file.
	 * @return The file at that point (or <code>null</code> if there isn't
	 *         one???).
	 */
	public File getFileAtPoint(Point p) {
		int row = rowAtPoint(p);
		return (File)getValueAt(row, 0);
	}


	public Dimension getPreferredScrollableViewportSize() {
		// All rows are equal height for our table
		int rowHeight = getRowHeight();
		Dimension d = super.getPreferredScrollableViewportSize();
		if (d==null) {
			d = new Dimension(480, rowHeight*8);
		}
		else {
			d.height = rowHeight * 8;
		}
		return d;
	}


	/**
	 * Returns the row in which the specified file resides in this table
	 * view.
	 *
	 * @param file The file to search for.
	 * @return The row the specified file is in, or <code>-1</code> if it
	 *         isn't in the view.
	 */
	private final int getRowFor(File file) {

		// FIXME:  Is there a better way to find the row of the specified
		// file???
		// We must do a linear search because the files will usually (always?)
		// not be listed in alphabetical order (i.e., folders come first, and
		// the user can sort by column).
		TableColumnModel columnModel = getColumnModel();
		int column = columnModel.getColumn(0).getModelIndex();
		TableModel tableModel = getModel();
		int rowCount = getRowCount();
		for (int i=0; i<rowCount; i++) {
			File temp = (File)tableModel.getValueAt(i, column);
			if (file.equals(temp))
				return i;
		}

		return -1;

	}


	/**
	 * Overridden to ensure the table completely fills the JViewport it is
	 * sitting in.  Note in Java 6 this could be taken care of by the method
	 * JTable#setFillsViewportHeight(boolean).
	 */
	public boolean getScrollableTracksViewportHeight() {
		Component parent = getParent();
		return parent instanceof JViewport ?
			parent.getHeight()>getPreferredSize().height : false;
	}


	/**
	 * Gets the selected file, for use when a single file is selected.
	 *
	 * @return The selected file, or <code>null</code> if no file is
	 *         selected.
	 */
	public File getSelectedFile() {
		int index = getSelectedRow();
		return index==-1 ? null : (File)getValueAt(index, 0);
	}


	/**
	 * Returns all selected files in this view.
	 *
	 * @return An array of all selected files.
	 */
	public File[] getSelectedFiles() {

		int[] selectedRows = getSelectedRows();
		int num = selectedRows.length;

		Object[] objArray = new Object[num];

		int column = convertColumnIndexToView(0);

		for (int i=0; i<num; i++)
			objArray[i] = getValueAt(selectedRows[i], column);

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
		int row = rowAtPoint(e.getPoint());
		if (row==-1)
			return null;
		File file = (File)getValueAt(row, 0);
		if (file==null || file.isDirectory())
			return null;
		tip = chooser.getToolTipFor(file);
		return tip;
	}


	/**
	 * This method picks good column sizes.
	 * If all column heads are wider than the column's cells'
	 * contents, then you can just use column.sizeWidthToFit().
	 */
	private void initFileNameColumnSize() {

		TableModel model = getModel();
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int maxWidth = 0;
		int cellWidth = 0;

		TableCellRenderer headerRenderer = getTableHeader().getDefaultRenderer();

		int col = convertColumnIndexToView(0);
		column = getColumnModel().getColumn(col);
		comp = headerRenderer.getTableCellRendererComponent(
                                this, column.getHeaderValue(),
                                false, false, 0,0);
		headerWidth = comp.getPreferredSize().width;
		TableCellRenderer renderer = getDefaultRenderer(model.getColumnClass(0));
		int rowCount = getRowCount();
		for (int i=0; i<rowCount; i++) {

			comp = renderer.getTableCellRendererComponent(
									this, getValueAt(i,col),
									false, false, i,col);
			cellWidth = comp.getPreferredSize().width;
			if (maxWidth<cellWidth)
				maxWidth = cellWidth;
		}

		int width = Math.min(Math.max(headerWidth, maxWidth), MAX_NAME_COLUMN_SIZE);
		column.setPreferredWidth(width);
		column.setWidth(width);

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
		getSelectionModel().removeListSelectionListener(selectionListener);
	}


	/**
	 * Selects the file at the specified point in the view.  If no file
	 * exists at that point, the selection should be cleared.
	 *
	 * @param p The point at which a file should be selected.
	 */
	public void selectFileAtPoint(Point p) {
		int row = rowAtPoint(p); // -1 if p isn't actually in table.
		setRowSelectionInterval(row, row);
		//ensureIndexIsVisible(row); // Not necessary for JTable.
	}


	/**
	 * Sets the files displayed by this view.
	 *
	 * @param files A vector containing the files to display.
	 */
	public void setDisplayedFiles(Vector files) {

		DetailsViewModel tableModel =
			(DetailsViewModel)((FileExplorerTableModel)getModel()).getTableModel();

		// The setData() call would be faster, but would require us
		// to reset column 0's renderer to our custome one.  So, for
		// now, we're leaving it as the two substitute lines below.
		// Although, we could substitute them and just re-add the
		// renderer.  The real bottleneck though is the File.length()
		// calls...
		//tableModel.setData(dirList);
		tableModel.setRowCount(0);
		tableModel.addRows(files);
		initFileNameColumnSize();

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
			int[] rows = new int[num];
			for (int i=0; i<num; i++) {
				rows[i] = getRowFor(files[i]);
				if (rows[i] != -1)
					addRowSelectionInterval(rows[i], rows[i]);
			}
		}
	}


	/**
	 * Renders the "last modified" column.
	 */
	static class DateCellRenderer extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
								Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {

			super.getTableCellRendererComponent(table, value, isSelected,
								hasFocus, row, column);
			setText(Utilities.getLastModifiedString(
									((Long)value).longValue()));
			return this;
		}

	}


	/**
	 * Table model for the details view.
	 */
	class DetailsViewModel extends DefaultTableModel {

		Vector tempVector;

		public DetailsViewModel(String nameHeader, String sizeHeader,
							String typeHeader, String statusHeader,
							String lastModifiedHeader) {
			String[] columnNames = new String[4];
			columnNames[0] = nameHeader;
			//columnNames[1] = sizeHeader; // Huge performance hit for dirs with many files in Table view...
			columnNames[1] = typeHeader;
			columnNames[2] = statusHeader;
			columnNames[3] = lastModifiedHeader;
			setColumnIdentifiers(columnNames);
		}

		/**
	 	 * Appends a bunch of rows to the end of the table data.  This method
		 * is added because <code>DefaultTableModel</code> has no means of
		 * adding multiple (i.e., many thousands of) rows without repeatedly
		 * calling <code>addRow</code> or <code>insertRow</code>, which
		 * adds a lot of overhead (e.g., notifies listeners of each line
		 * added instead of just all of them at once, etc.).
		 */
		public void addRows(Vector data) {

			int dataSize = data.size();
			if (dataSize==0)
				return; // For example, if our filter filters out all files in this dir.
			int dataVectorSize = dataVector.size();
			dataVector.setSize(dataVectorSize + dataSize); // Ensure we have enough values.
			for (int i=0; i<dataSize; i++) {
				dataVector.set(dataVectorSize++,
						getTableObjectVectorForFile((File)data.get(i)));
			}
		}

		public Class getColumnClass(int column) {
			switch (column) {
				case 0:	// File name.
					return File.class;
				case 3:	// Last modified.
					return Long.class;
				default:
					return Object.class;
			}
		}

		private final Vector getTableObjectVectorForFile(final File file) {
//			boolean isDirectory = file.isDirectory();
//			String length = getFileSizeStringFor(file); // Invalid if file is a directory.
			String description = chooser.getDescription(file);
			boolean canRead = file.canRead();
			boolean canWrite = file.canWrite();
			String status = "";
			if (canRead) {
				if (canWrite)
					status = chooser.readWriteString;
				else
					status = chooser.readString;
			}
			else if (canWrite)
				status = chooser.writeString;
			Vector tempVector = new Vector(5);//tempVector.clear();
			tempVector.add(0, file);
//			tempVector.add(isDirectory ? "" : length);
			tempVector.add(1, description);
			tempVector.add(2, status);
			tempVector.add(3, new Long(file.lastModified()));
			return tempVector;
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

		/**
		 * Takes a Vector (of files), and sets the data represented by this
		 * table to be suitable for the "Details View."
		 */
		public void setData(Vector dataVector) {
			int size = dataVector.size(); // Convert file values to TableObject values.
			for (int i=0; i<size; i++)
				dataVector.setElementAt(getTableObjectVectorForFile((File)dataVector.get(i)), i);
			setDataVector(dataVector, columnIdentifiers);
		}

	}


	/**
	 * Sorts two <code>File</code> objects as follows:<br>
	 * If one is a file and the other is a directory, returns that the
	 * directory object comes "before" the file object.  If they are either
	 * both files or both directories, then it uses <code>File</code>'s
	 * standard <code>compareTo</code> method.
	 */
	static class FileComparator implements Comparator {

		public int compare(Object o1, Object o2) {
			File f1 = (File)o1;
			File f2 = (File)o2;
			boolean f1IsDir = f1.isDirectory();
			boolean f2IsDir = f2.isDirectory();
			if (f1IsDir) {
				if (!f2IsDir)
					return -1;
				// Both are directories.
				return ((Comparable)f1).compareTo(f2);
			}
			// f1 isn't a directory.
			if (f2IsDir)
				return 1;
			// Both are regular files.
			return ((Comparable)f1).compareTo(f2);
		}

	};


	/**
	 * Renderer used for columns displaying <code>File</code>s in a
	 * <code>JTable</code>.
	 */
	class FileTableColumnRenderer extends DefaultTableCellRenderer {

		private Rectangle paintTextR = new Rectangle();
		private Rectangle paintIconR = new Rectangle();
		private Rectangle paintViewR = new Rectangle();
		private boolean isAlreadyOpened;


		public void paintComponent(Graphics g) {

			String text = getText();
			Icon icon = getIcon();
			FontMetrics fm = g.getFontMetrics();

			paintViewR.x = paintViewR.y = 0;
			paintViewR.width = getWidth();
			paintViewR.height = getHeight();

			g.setColor(getBackground());
			g.fillRect(paintViewR.x,paintViewR.y, paintViewR.width,paintViewR.height);

			paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
			paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

			String clippedText = 
					SwingUtilities.layoutCompoundLabel(this,
										fm,
										text,
										icon,
										getVerticalAlignment(),
										getHorizontalAlignment(),
										getVerticalTextPosition(),
										getHorizontalTextPosition(),
										paintViewR,
										paintIconR,
										paintTextR,
										getIconTextGap());

			if (icon != null)
				icon.paintIcon(this, g, paintIconR.x, paintIconR.y);

			if (text != null) {
				int textX = paintTextR.x;
				int textY = paintTextR.y + fm.getAscent();
				g.setColor(getForeground());
				g.drawString(clippedText, textX,textY);
				if (isAlreadyOpened && chooser.getStyleOpenFiles()) {
					g.drawLine(textX, textY+2, textX+paintTextR.width, textY+2);
				}
			}

		}


		public Component getTableCellRendererComponent(JTable table, Object value,
									boolean isSelected, boolean hasFocus,
									int row, int column) 
		{

			super.getTableCellRendererComponent(table, value, isSelected,
										hasFocus, row, column);

			File file = (File)value;
			String fileName = file.getName();

			isAlreadyOpened = chooser.isOpenedFile(file);

			setText(fileName);

			// Set the image according to the file type.
			FileTypeInfo info = chooser.getFileTypeInfoFor(file);
			setIcon(info.icon);
			if (!isSelected) {
				if (chooser.getShowHiddenFiles() && file.isHidden())
					setForeground(chooser.getHiddenFileColor());
				else 
					setForeground(info.labelTextColor);
			}

			return this;

		}

	}


}