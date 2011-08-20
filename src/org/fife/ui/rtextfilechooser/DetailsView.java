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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;

import org.fife.ui.FileExplorerTableModel;
import org.fife.ui.UIUtil;


/**
 * A "details view" (i.e., a table view) for an <code>RTextFileChooser</code>.
 * This is similar to the Details view found in Microsoft Windows file
 * choosers.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DetailsView extends JTable implements RTextFileChooserView {

	private RTextFileChooser chooser; // The chooser this view is in.
	private MouseListener mouseListener;
	private ViewKeyListener keyListener;
	private SelectionListener selectionListener;

	private String readStr;
	private String writeStr;
	private String readWriteStr;

	/**
	 * Number of file sets this view has displayed.  This is used to help
	 * ensure that notices from Threads actually do apply to the currently
	 * displayed list of files.  Accesses to this field should be synchronized
	 * by {@link #ATTRIBUTES_LOCK}.
	 */
	private transient int displayCount;

	/**
	 * Thread that asynchronously loads attributes for the listed files.
	 * Accesses to this field should be synchronized by
	 * {@link #ATTRIBUTES_LOCK}.
	 */
	private transient Thread attributeThread;

	private static final int MAX_NAME_COLUMN_SIZE		= 150;

	/**
	 * Used to ensure the asynchronous loading of attributes for the table
	 * is handled properly.
	 */
	private static final Object ATTRIBUTES_LOCK = new Object();


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

		readStr = chooser.getString("Read");
		writeStr = chooser.getString("Write");
		readWriteStr = chooser.getString("ReadWrite");


		// Create the table model, then wrap it in a sorter model.
		DetailsViewModel dvm = new DetailsViewModel(nameString, typeString,
							statusString, sizeString, lastModifiedString);
		FileExplorerTableModel sorter = new FileExplorerTableModel(dvm);
		setModel(sorter);
		sorter.setTable(this);

		// Prevent this table from interpreting Enter to mean "move to the
		// next row."
		InputMap tableInputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		tableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "none");

		// Prevent this table from swallowing F2 (I think this causes editable
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
		getColumnModel().getColumn(0).setCellRenderer(new FileNameColumnRenderer());
		for (int i=1; i<columnCount-2; i++) {
			columnModel.getColumn(i).setCellRenderer(new DefaultTableCellRenderer());
		}
		columnModel.getColumn(columnCount-2).setCellRenderer(new FileSizeColumnRenderer());
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
		keyListener = new ViewKeyListener();
		addKeyListener(keyListener);
		selectionListener = new SelectionListener(chooser);
		getSelectionModel().addListSelectionListener(selectionListener);

		setTransferHandler(new FileChooserViewTransferHandler(this));
		setDragEnabled(true);

	}


	/**
	 * Adds the specified file attributes to this table.  This method is
	 * called by our worker thread, but should be run on the EDT via
	 * <code>SwingUtilities.invokeLater()</code>.
	 *
	 * @param batch The set of file attributes to add to the table.
	 */
	private void addFileAttributes(AttributeBatch batch) {

		synchronized (ATTRIBUTES_LOCK) {

			// If we've changed directories/filters/etc., but somehow the
			// attributes-gathering Thread isn't yet dead, ignore its updates.
			if (batch.getDisplayCount()!=displayCount) {
				return;
			}

			FileExplorerTableModel tevm = (FileExplorerTableModel)getModel();
			DetailsViewModel model = (DetailsViewModel)tevm.getTableModel();

			for (int i=0; i<batch.getSize(); i++) {
				int row = batch.getStart() + i;
				FileAttributes attrs = batch.getAttributes(i);
				model.setValueAt(attrs.status, row, 2);
				Long size = new Long(attrs.size);
				model.setValueAt(size, row, 3);
				Long modified = new Long(attrs.modified);
				model.setValueAt(modified, row, 4);
			}

		}

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


	private int getDisplayCount() {
		synchronized (ATTRIBUTES_LOCK) {
			return displayCount;
		}
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
	 * @return The file at that point, or <code>null</code> if there isn't
	 *         one.
	 */
	public File getFileAtPoint(Point p) {
		int row = rowAtPoint(p);
		return row==-1 ? null : (File)getValueAt(row, 0);
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
		File file = (File)getModel().getValueAt(row, 0);
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
		removeKeyListener(keyListener);
		getSelectionModel().removeListSelectionListener(selectionListener);
	}


	private void restartAttributeThread(Vector files) {
		synchronized (ATTRIBUTES_LOCK) {
			displayCount++;
			if (attributeThread!=null) {
				attributeThread.interrupt();
			}
			attributeThread = new Thread(
								new AttributeRunnable(displayCount, files));
			attributeThread.start();
		}
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

		tableModel.setContents(files);
		initFileNameColumnSize();

		restartAttributeThread(files);

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
	 * A list of attributes for a batch of files, to update the table view
	 * with.
	 */
	private class AttributeBatch {

		private int displayCount;
		private int start;
		private List fileAttrs;

		public AttributeBatch(int displayCount, int start) {
			this.displayCount = displayCount;
			this.start = start;
			fileAttrs = new ArrayList();
		}

		public void addAttributes(FileAttributes attrs) {
			fileAttrs.add(attrs);
		}

		public FileAttributes getAttributes(int index) {
			return (FileAttributes)fileAttrs.get(index);
		}

		public int getDisplayCount() {
			return displayCount;
		}

		public int getSize() {
			return fileAttrs.size();
		}

		public int getStart() {
			return start;
		}

	}


	/**
	 * Gathers information about the displayed files and updates the table view
	 * with batches of updates.
	 */
	private class AttributeRunnable implements Runnable {

		private int displayCount;
		private Vector files;

		/**
		 * Arbitrarily-chosen number of files to get attributes for at a time.
		 * This should be relatively small so that the table updates quickly.
		 */
		private static final int BATCH_SIZE = 15;


		public AttributeRunnable(int displayCount, Vector files) {
			this.displayCount = displayCount;
			this.files = files;
		}

		public void run() {

			int i = 0;
			while (i<files.size()) {

				if (shouldStop()) {
					return;
				}

				int max = Math.min(i+BATCH_SIZE, files.size());
				final AttributeBatch batch = new AttributeBatch(displayCount,i);

				for (int j=i; j<max; j++) {
					File file = (File)files.get(j);
					batch.addAttributes(new FileAttributes(file));
				}

				if (shouldStop()) {
					return;
				}

				i = max;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						addFileAttributes(batch);
					}
				});

//				try {
//					Thread.sleep(100);
//				} catch (Exception e) {}

			}

		}

		/**
		 * Returns whether this thread should stop processing prematurely.
		 * It should stop if it was interrupted (the user changed directories
		 * or file filters), and we effectively check for this in two
		 * different ways.
		 *
		 * @return Whether this thread should stop running.
		 */
		private boolean shouldStop() {
			return Thread.currentThread().isInterrupted() ||
					displayCount!=DetailsView.this.getDisplayCount();
		}

	}


	/**
	 * Renders the "last modified" column.
	 */
	private static class DateCellRenderer extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
								Object value, boolean isSelected,
								boolean hasFocus, int row, int column) {

			super.getTableCellRendererComponent(table, value, isSelected,
								hasFocus, row, column);
			if (value!=null) { // Will be null before Thread gets to us
				setText(Utilities.getLastModifiedString(
									((Long)value).longValue()));
			}
			return this;
		}

	}


	/**
	 * Table model for the details view.
	 */
	class DetailsViewModel extends DefaultTableModel {

		Vector tempVector;

		public DetailsViewModel(String nameHeader, String typeHeader,
							String statusHeader,
							String sizeHeader, String lastModifiedHeader) {
			String[] columnNames = new String[5];
			columnNames[0] = nameHeader;
			columnNames[1] = typeHeader;
			columnNames[2] = statusHeader;
			columnNames[3] = sizeHeader;
			columnNames[4] = lastModifiedHeader;
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
		public void setContents(Vector data) {

			// Calling setDataVector() call would be faster, but would require
			// us to reset column 0's renderer to our custom one.  So, for now,
			// we're leaving it as the two substitute lines below.  Although,
			// we could substitute them and just re-add the renderer.
			//setDataVector(manipulated(data));

			setRowCount(0);

			int dataSize = data.size();
			if (dataSize==0)
				return; // For example, if our filter filters out all files in this dir.
			for (int i=0; i<dataSize; i++) {
				dataVector.add(getTableObjectVectorForFile((File)data.get(i)));
			}

		}

		public Class getColumnClass(int column) {
			switch (column) {
				case 0:	// File name
					return File.class;
				case 3: // Size
					// Fall through
				case 4: // Last modified
					return Long.class;
				default:
					return Object.class;
			}
		}

		private final Vector getTableObjectVectorForFile(File file) {
//			boolean isDirectory = file.isDirectory();
//			long length = isDirectory ? -1 : file.length();
			String description = chooser.getDescription(file);
			Vector tempVector = new Vector(5);//tempVector.clear();
			tempVector.add(0, file);
			tempVector.add(1, description);
			tempVector.add(2, null);//status);
			tempVector.add(3, null);//new Long(length));
			tempVector.add(4, null);//new Long(file.lastModified()));
			return tempVector;
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}


	/**
	 * Attributes of a specific file.
	 */
	private class FileAttributes {

		//public File file;
		public String status;
		public long size;
		public long modified;

		public FileAttributes(File file) {
			//this.file = file;
			size = file.isDirectory() ? -1 : file.length();
			boolean canRead = file.canRead();
			boolean canWrite = file.canWrite();
			if (canRead) {
				status = canWrite ? readWriteStr : readStr;
			}
			else if (canWrite) {
				status = writeStr;
			}
			else {
				status = null;
			}
			modified = file.lastModified();
		}

	}


	/**
	 * Sorts two <code>File</code> objects as follows:<br>
	 * If one is a file and the other is a directory, returns that the
	 * directory object comes "before" the file object.  If they are either
	 * both files or both directories, then it uses <code>File</code>'s
	 * standard <code>compareTo</code> method.
	 */
	private static class FileComparator implements Comparator {

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
	private class FileNameColumnRenderer extends DefaultTableCellRenderer {

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
				Map old = UIUtil.setNativeRenderingHints((Graphics2D)g);
				int textX = paintTextR.x;
				int textY = paintTextR.y + fm.getAscent();
				g.setColor(getForeground());
				g.drawString(clippedText, textX,textY);
				if (isAlreadyOpened && chooser.getStyleOpenFiles()) {
					g.drawLine(textX, textY+2, textX+paintTextR.width, textY+2);
				}
				if (old!=null) {
					((Graphics2D)g).addRenderingHints(old);
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


	/**
	 * Renderer for the "file size" column.
	 */
	private static class FileSizeColumnRenderer extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean selected, boolean focused,
				int row, int col) {
			super.getTableCellRendererComponent(table, value, selected,
												focused, row, col);
			if (value!=null) { // is null before Thread gets to it
				Long l = (Long)value;
				long size = l.longValue();
				String text = size==-1 ? "" :
								Utilities.getFileSizeStringFor(size, true);
				setText(text);
			}
			return this;
		}

	}


	/**
	 * Listens for key events in the table, to allow the user to type the name
	 * of a file and have it selected.
	 */
	private class ViewKeyListener extends KeyAdapter {

		private String typed;
		private long lastTime;

		private int getNextMatch(String text, int fromRow) {

			text = text.toUpperCase();
			FileExplorerTableModel model = (FileExplorerTableModel)getModel();

			// First, try everything after the selected row
			for (int row=fromRow; row<getRowCount(); row++) {
				// Get value from the model, as columns may be reordered.
				Object value = model.getValueAt(row, 0);
				String fileName = (value instanceof File) ?
						((File)value).getName() : value.toString();
				fileName = fileName.toUpperCase();
				if (fileName.startsWith(text)) {
					return row;
				}
			}

			// Then, wrap around to before the selected row
			for (int row=0; row<fromRow; row++) {
				// Get value from the model, as columns may be reordered.
				Object value = model.getValueAt(row, 0);
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

			FileExplorerTableModel model = (FileExplorerTableModel)getModel();
			if (model==null || model.getRowCount()==0) {
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

			int startRow = getSelectedRow();
			if (startRow==-1) {
				startRow = 0;
			}

			int matchRow = getNextMatch(typed, startRow);
			if (matchRow!=-1) {
				getSelectionModel().setSelectionInterval(matchRow, matchRow);
				ensureFileIsVisible(getSelectedFile());
			}

		}

	}


}