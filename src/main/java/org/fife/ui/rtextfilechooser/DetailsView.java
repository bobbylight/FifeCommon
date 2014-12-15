/*
 * 07/14/2004
 *
 * DetailsView.java - The "Details view" (i.e., table view) for an
 * RTextFileChooser.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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

		fixKeyboardShortcuts();

		// Make this table look just a little nicer.
		setIntercellSpacing(new Dimension(0,0));
		setShowGrid(false);
		setColumnSelectionAllowed(false);

		// Set the renderer for each column.  We must do this even for normal
		// columns because otherwise the columns we sort by won't be
		// gray-highlighted.
		int columnCount = getColumnCount();
		TableColumnModel columnModel = getColumnModel();
		TableCellRenderer renderer = FileChooserViewRendererFactory.createTableFileNameRenderer(chooser);
		getColumnModel().getColumn(0).setCellRenderer(renderer);
		for (int i=1; i<columnCount; i++) {
			renderer = FileChooserViewRendererFactory.createDefaultTableRenderer();
			columnModel.getColumn(i).setCellRenderer(renderer);
		}
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
				FileSizeWrapper sizeWrapper = new FileSizeWrapper(attrs.size);
				model.setValueAt(sizeWrapper, row, 3);
				FileModifiedWrapper modWrapper = new FileModifiedWrapper(
						attrs.modified);
				model.setValueAt(modWrapper, row, 4);
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


	/**
	 * Removes keyboard mappings that interfere with our file chooser's
	 * shortcuts.
	 */
	private void fixKeyboardShortcuts() {

		InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		// Prevent this table from interpreting Enter to mean "move to the
		// next row."
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "none");

		// Prevent this table from swallowing F2 (I think this causes editable
		// JTables to go into editing mode, but we want our parent dialog to
		// handle this keypress).
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "none");

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


	@Override
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
	@Override
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
	@Override
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


	private void restartAttributeThread(List<File> files) {
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
	 * @param files The files to display.
	 */
	public void setDisplayedFiles(List<File> files) {

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
	 * Overridden to refresh our cell renderers on LAF changes.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		TableColumnModel tcm = getColumnModel();
		for (int i=0; i<getColumnCount(); i++) {
			TableCellRenderer r = tcm.getColumn(i).getCellRenderer();
			if (r instanceof JComponent) {
				((JComponent)r).updateUI();
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
		private List<FileAttributes> fileAttrs;

		public AttributeBatch(int displayCount, int start) {
			this.displayCount = displayCount;
			this.start = start;
			fileAttrs = new ArrayList<FileAttributes>();
		}

		public void addAttributes(FileAttributes attrs) {
			fileAttrs.add(attrs);
		}

		public FileAttributes getAttributes(int index) {
			return fileAttrs.get(index);
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
		private List<File> files;

		/**
		 * Arbitrarily-chosen number of files to get attributes for at a time.
		 * This should be relatively small so that the table updates quickly.
		 */
		private static final int BATCH_SIZE = 15;


		public AttributeRunnable(int displayCount, List<File> files) {
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
					File file = files.get(j);
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
	 * Table model for the details view.
	 */
	private class DetailsViewModel extends DefaultTableModel {

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
		@SuppressWarnings("unchecked")
		public void setContents(Collection<File> data) {

			// Calling setDataVector() call would be faster, but would require
			// us to reset column 0's renderer to our custom one.  So, for now,
			// we're leaving it as the two substitute lines below.  Although,
			// we could substitute them and just re-add the renderer.
			//setDataVector(manipulated(data));

			setRowCount(0);

			for (File file : data) {
				dataVector.add(getTableObjectVectorForFile(file));
			}

		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
				case 0:	// File name
					return File.class;
				case 3: // Size
					return FileSizeWrapper.class;
				case 4: // Last modified
					return FileModifiedWrapper.class;
				default:
					return Object.class;
			}
		}

		private final Vector<?> getTableObjectVectorForFile(File file) {
//			boolean isDirectory = file.isDirectory();
//			long length = isDirectory ? -1 : file.length();
			String description = chooser.getDescription(file);
			Vector<Object> temp = new Vector<Object>(5);
			temp.add(0, file);
			temp.add(1, description);
			temp.add(2, null);//status);
			temp.add(3, null);//new FileSizeWrapper(length));
			temp.add(4, null);//new FileModifiedWrapper(file.lastModified()));
			return temp;
		}

		@Override
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
	private static class FileComparator implements Comparator<File> {

		public int compare(File f1, File f2) {
			boolean f1IsDir = f1.isDirectory();
			boolean f2IsDir = f2.isDirectory();
			if (f1IsDir) {
				if (!f2IsDir)
					return -1;
				// Both are directories.
				return f1.compareTo(f2);
			}
			// f1 isn't a directory.
			if (f2IsDir)
				return 1;
			// Both are regular files.
			return f1.compareTo(f2);
		}

	}


	/**
	 * A simple wrapper for the "date modified" column, so we don't have to
	 * have two different renderers for Substance vs. all other LookAndFeels.
	 */
	private static class FileModifiedWrapper
	implements Comparable<FileModifiedWrapper> {

		private long modified;

		public FileModifiedWrapper(long modified) {
			this.modified = modified;
		}

		public int compareTo(FileModifiedWrapper w2) {
			if (modified==w2.modified) {
				return 0;
			}
			return modified<w2.modified ? -1 : 1;
		}

		@Override
		public String toString() {
			return modified==-1 ? "" :
				Utilities.getLastModifiedString(modified);
		}

	}


	/**
	 * A simple wrapper for the file size column, so we don't have to have
	 * two different renderers for Substance vs. all other LookAndFeels.
	 */
	private static class FileSizeWrapper implements Comparable<FileSizeWrapper>{

		private long size;

		public FileSizeWrapper(long size) {
			this.size = size;
		}

		public int compareTo(FileSizeWrapper w2) {
			if (size==w2.size) {
				return 0;
			}
			return size<w2.size ? -1 : 1;
		}

		@Override
		public String toString() {
			return size==-1 ? "" : Utilities.getFileSizeStringFor(size, true);
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

		@Override
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