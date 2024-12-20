/*
 * 07/12/2004
 *
 * FileExplorerTableModel.java - A table model that simulates the functionality
 * found in the table used in Windows' "details view" in Windows Explorer.
 * Copyright (C) 2004 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


/**
 * A table model that simulates the functionality found in the table used in
 * Windows' "details view" in Windows explorer.  This is a WIP, and currently is
 * pretty much just a copy of <code>FileExplorerTableModel.java</code> found in
 * java.sun.com's tutorial section.<p>
 * This model currently allows the user to sort by column, and colors the cells
 * of elements in sorted-by columns slightly darker than normal, to signify that
 * the table is sorted by that row.  Future enhancements include a right-click
 * popup menu for the table header that allows you to add or remove columns.<p>
 *
 * NOTE: If you use this table model in an application that allows the user
 * to change the LaF at runtime, you will get NullPointerExceptions when the
 * user changes from the Windows LaF to another LaF, such as Metal.  This is
 * due to Sun bug 6429812.  This bug is still open as of 6u18.  You'll have to
 * implement a workaround for when the LaF changes if you want to use this
 * class in an application that allows runtime LaF changes.
 * See <a href="http://bugs.sun.com/view_bug.do?bug_id=6429812">6429812</a>
 * for more information.  As an alternative, you can probably use Swing's
 * built-in table sorting support, if you only support Java 6 and up.
 *
 * @author Robert Futrell
 * @version 0.4
 */
public class FileExplorerTableModel extends AbstractTableModel {

	@Serial
	private static final long serialVersionUID = 1L;

	protected TableModel tableModel;

	public static final int DESCENDING = -1;
	public static final int NOT_SORTED = 0;
	public static final int ASCENDING = 1;

	private static final Directive EMPTY_DIRECTIVE = new Directive(-1, NOT_SORTED);

	// How much darker columns get when table is sorted by them.
	private static final int DARK_AMOUNT	= 15;

	/**
	 * Compares two comparable objects by their <code>compareTo</code> method.
	 */
	@SuppressWarnings("unchecked")
	public static final Comparator<?> COMPARABLE_COMPARATOR =
		(Comparator<Object>)(o1, o2) -> ((Comparable<Object>)o1).compareTo(o2);


	/**
	 * Compares two objects by their string (<code>toString</code>) values.
	 */
	public static final Comparator<?> LEXICAL_COMPARATOR = Comparator.comparing(Object::toString);


	private Row[] viewToModel;
	private int[] modelToView;

	private JTableHeader tableHeader;
	private MouseHandler mouseListener;
	private TableModelListener tableModelListener;
	private Map<Class<?>, Comparator<?>> columnComparators =
		new HashMap<>();
	private List<Directive> sortingColumns = new ArrayList<>();

	private JTable table;


	/**
	 * Constructor.
	 */
	public FileExplorerTableModel() {
		this.mouseListener = new MouseHandler();
		this.tableModelListener = new TableModelHandler();
	}


	/**
	 * Constructor.
	 *
	 * @param tableModel ???
	 */
	public FileExplorerTableModel(TableModel tableModel) {
		this();
		setTableModel(tableModel);
	}


	/**
	 * Constructor.
	 *
	 * @param tableModel ???
	 * @param tableHeader ???
	 */
	public FileExplorerTableModel(TableModel tableModel, JTableHeader tableHeader) {
		this();
		setTableHeader(tableHeader);
		setTableModel(tableModel);
	}


	private void cancelSorting() {
		sortingColumns.clear();
		sortingStatusChanged();
	}


	private void clearSortingState() {
		viewToModel = null;
		modelToView = null;
	}


	@SuppressWarnings("rawtypes")
	protected Comparator getComparator(int column) {
		Class<?> columnType = tableModel.getColumnClass(column);
		Comparator<?> comparator = columnComparators.get(columnType);
		if (comparator != null)
			return comparator;
		if (Comparable.class.isAssignableFrom(columnType))
			return COMPARABLE_COMPARATOR;
		return LEXICAL_COMPARATOR;
	}


	private Directive getDirective(int column) {
		for (Directive directive : sortingColumns) {
			if (directive.column == column)
				return directive;
		}
		return EMPTY_DIRECTIVE;
	}


	protected Icon getHeaderRendererIcon(int column, int size) {
		Directive directive = getDirective(column);
		if (directive == EMPTY_DIRECTIVE)
			return null;
		return new Arrow(directive.direction == ASCENDING, size,
					sortingColumns.indexOf(directive));
	}


	private int[] getModelToView() {
		if (modelToView == null) {
			int n = getViewToModel().length;
			modelToView = new int[n];
			for (int i = 0; i < n; i++) {
				modelToView[modelIndex(i)] = i;
			}
		}
		return modelToView;
	}


	/**
	 * Returns the sorting status of a column.
	 *
	 * @param column The index of the column to check.
	 * @return The sorting status of the column.
	 */
	public int getSortingStatus(int column) {
		return getDirective(column).direction;
	}


	/**
	 * Returns the table header being used.
	 *
	 * @return The table header being used.
	 */
	public JTableHeader getTableHeader() {
		return tableHeader;
	}


	/**
	 * Returns the table model being used.
	 *
	 * @return The table model being used.
	 * @see #setTableModel
	 */
	public TableModel getTableModel() {
		return tableModel;
	}


	private Row[] getViewToModel() {
		if (viewToModel == null) {
			int tableModelRowCount = tableModel.getRowCount();
			viewToModel = new Row[tableModelRowCount];
			for (int row = 0; row < tableModelRowCount; row++) {
				viewToModel[row] = new Row(row);
			}

			if (isSorting()) {
				Arrays.sort(viewToModel);
			}
		}
		return viewToModel;
	}


	/**
	 * Returns whether any column is sorted.
	 *
	 * @return Whether any column is sorted.
	 */
	public boolean isSorting() {
		return !sortingColumns.isEmpty();
	}


	/**
	 * Returns the index in the model corresponding to an index
	 * in the view.
	 *
	 * @param viewIndex The index in the view.
	 * @return Tbe corresponding index in the model.
	 */
	public int modelIndex(int viewIndex) {
		return getViewToModel()[viewIndex].modelIndex;
	}


	/**
	 * Sets the comparator to use for columns of a specific type.
	 *
	 * @param type The data type of the column(s) to change.
	 * @param comparator The comparator to install on the column.
	 */
	public void setColumnComparator(Class<?> type, Comparator<?> comparator) {
		if (comparator == null)
			columnComparators.remove(type);
		else
			columnComparators.put(type, comparator);
	}


	/**
	 * Sets the sorting status of a column.
	 *
	 * @param column The column.
	 * @param status The new sorting status.
	 */
	public void setSortingStatus(int column, int status) {

		Directive directive = getDirective(column);

		if (directive != EMPTY_DIRECTIVE)
			sortingColumns.remove(directive);

		if (status != NOT_SORTED)
			sortingColumns.add(new Directive(column, status));

		// Set the color for the columns (darker if sorted).
		if (table!=null) {
			TableColumnModel columnModel = table.getColumnModel();
			Color tableBGColor = table.getBackground();
			Color sortedColor = null;
			int columnCount = table.getColumnCount();
			for (int i=0; i<columnCount; i++) {
				TableColumn col = columnModel.getColumn(table.convertColumnIndexToView(i));
				TableCellRenderer renderer = col.getCellRenderer();
				if (renderer instanceof Component c) {
					directive = getDirective(i);
					if (directive.column!=-1 && directive.direction!=NOT_SORTED) {
						if (sortedColor==null) {
							sortedColor = new Color(Math.max(tableBGColor.getRed()-DARK_AMOUNT,   0),
											Math.max(tableBGColor.getGreen()-DARK_AMOUNT, 0),
											Math.max(tableBGColor.getBlue()-DARK_AMOUNT,  0));
						}
						c.setBackground(sortedColor);
					}
					else {
						c.setBackground(tableBGColor);
					}
				}
			}
		}

		sortingStatusChanged();

	}


	/**
	 * Sets the table this sorter is the model for.  By setting this to a
	 * non-<code>null</code> value, any columns by which the table is sorted
	 * will be colored with a slightly-darker background (similar to how it's
	 * done in Windows XP).
	 *
	 * @param table The table for which this model is the model.
	 */
	public void setTable(JTable table) {
		this.table = table;
		setTableHeader(table.getTableHeader());
	}


	/**
	 * Sets the table header to use.
	 *
	 * @param tableHeader The table header to use.
	 * @see #getTableHeader
	 */
	private void setTableHeader(JTableHeader tableHeader) {

		// Remove our mouse listener from the old table header and restore the
		// default renderer to the old table header.
		if (this.tableHeader != null) {
			this.tableHeader.removeMouseListener(mouseListener);
			TableCellRenderer defaultRenderer = this.tableHeader.getDefaultRenderer();
			if (defaultRenderer instanceof SortableHeaderRenderer shr) {
				this.tableHeader.setDefaultRenderer(shr.delegate);
			}
		}

		// Set our new table header, give it the "sortable header" renderer and
		// add a mouse listener.
		this.tableHeader = tableHeader;
		if (this.tableHeader != null) {
			this.tableHeader.addMouseListener(mouseListener);
			this.tableHeader.setDefaultRenderer(
				new SortableHeaderRenderer(this.tableHeader.getDefaultRenderer()));
		}

	}


	/**
	 * Sets the table model to use.
	 *
	 * @param tableModel The table model to use.
	 * @see #getTableModel
	 */
	public void setTableModel(TableModel tableModel) {

		// Remove the table model listener from the old table model and add it
		// to the new table model.
		if (this.tableModel!=null)
			this.tableModel.removeTableModelListener(tableModelListener);
		this.tableModel = tableModel;
		if (this.tableModel!=null)
			this.tableModel.addTableModelListener(tableModelListener);

		// Housekeeping.
		clearSortingState();
		fireTableStructureChanged();

	}


	private void sortingStatusChanged() {
		clearSortingState();
		fireTableDataChanged();
		if (tableHeader != null)
			tableHeader.repaint();
	}


	// TableModel interface methods

	@Override
	public int getRowCount() {
		return (tableModel == null) ? 0 : tableModel.getRowCount();
	}

	@Override
	public int getColumnCount() {
		return (tableModel == null) ? 0 : tableModel.getColumnCount();
	}

	@Override
	public String getColumnName(int column) {
		return tableModel.getColumnName(column);
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return tableModel.getColumnClass(column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return tableModel.isCellEditable(modelIndex(row), column);
	}

	@Override
	public Object getValueAt(int row, int column) {
		return tableModel.getValueAt(modelIndex(row), column);
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		tableModel.setValueAt(aValue, modelIndex(row), column);
	}


	/**
	 * A simple arrow icon.
	 */
	private static class Arrow implements Icon {

		private boolean ascending;
		private int size;
		private int priority;

		Arrow(boolean ascending, int size, int priority) {
			this.ascending = ascending;
			this.size = size;
			this.priority = priority;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {

			Color color = c == null ? Color.GRAY : c.getBackground();

			// In a compound sort, make each successive triangle 20%
			// smaller than the previous one.
			int dx = (int)(size/2.0*Math.pow(0.8, priority));
			int dy = ascending ? dx : -dx;

			// Align icon (roughly) with font baseline.
			y = y + 5*size/6 + (ascending ? -dy : 0);
			g.translate(x, y);

			Graphics2D g2d = (Graphics2D)g;
			Object old = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

			int[] xs = { 0, dx / 2, dx };
			int[] ys = { dy, 0, dy };
			g.fillPolygon(xs, ys, 3);

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, old);
			g.setColor(color);
			g.translate(-x, -y);

		}

		@Override
		public int getIconWidth() {
			return size;
		}

		@Override
		public int getIconHeight() {
			return size;
		}

	}


	/**
	 * A column and direction paired together.
	 */
	private static class Directive {

		private int column;
		private int direction;

		Directive(int column, int direction) {
			this.column = column;
			this.direction = direction;
		}

	}


	/**
	 * Simple representation of row data.
	 */
	private class Row implements Comparable<Row> {

		private int modelIndex;

		Row(int index) {
			this.modelIndex = index;
		}

		@Override
		@SuppressWarnings("unchecked")
		public int compareTo(Row r2) {

			int row1 = modelIndex;
			int row2 = r2.modelIndex;

			for (Directive directive : sortingColumns) {

				int column = directive.column;
				Object o1 = tableModel.getValueAt(row1, column);
				Object o2 = tableModel.getValueAt(row2, column);

				int comparison;
				// Define null less than everything, except null.
				if (o1 == null && o2 == null)
					comparison = 0;
				else if (o1 == null)
					comparison = -1;
				else if (o2 == null)
					comparison = 1;
				else
					comparison = getComparator(column).compare(o1, o2);
				if (comparison != 0)
					return directive.direction == DESCENDING ? -comparison : comparison;

			}

			return 0;

		}

	}


	/**
	 * Listens for mouse events in this component.
	 */
	private final class MouseHandler extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {

			// Left-clicks mean to sort by the column.
			if (e.getButton()==MouseEvent.BUTTON1) {
				JTableHeader h = (JTableHeader) e.getSource();
				TableColumnModel columnModel = h.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				if (viewColumn>-1) {
					int column = columnModel.getColumn(viewColumn).getModelIndex();
					if (column != -1) {
						int status = getSortingStatus(column);
						if (!e.isControlDown()) {
							cancelSorting();
						}
						// Cycle the sorting states through {NOT_SORTED, ASCENDING, DESCENDING} or
						// {NOT_SORTED, DESCENDING, ASCENDING} depending on whether shift is pressed.
						status = status + (e.isShiftDown() ? -1 : 1);
						status = (status + 4) % 3 - 1; // signed mod, returning {-1, 0, 1}
						setSortingStatus(column, status);
					}
				}
			}
		}

	}


	/**
	 * A cell renderer for column headers for sorted tables.  This renderer
	 * will draw an "up" or "down" arrow beside a column's name if the table
	 * is sorted by that column.
	 */
	public class SortableHeaderRenderer implements TableCellRenderer, UIResource {

		private TableCellRenderer delegate;

		public SortableHeaderRenderer(TableCellRenderer tableCellRenderer) {
			this.delegate = tableCellRenderer;
		}

		public TableCellRenderer getDelegateRenderer() {
			return delegate;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
											Object value,
											boolean isSelected,
											boolean hasFocus,
											int row,
											int column) {
			Component c = delegate.getTableCellRendererComponent(table,
									value, isSelected, hasFocus, row, column);
			if (c instanceof JLabel l) {
				l.setHorizontalTextPosition(JLabel.LEFT);
				int modelColumn = table.convertColumnIndexToModel(column);
				l.setIcon(getHeaderRendererIcon(modelColumn, l.getFont().getSize()));
			}
			return c;
		}

		/**
		 * Provides a hook to change the orientation on the delegated-to
		 * renderer.
		 *
		 * @param o The new orientation.
		 */
		public void applyComponentOrientation(ComponentOrientation o) {
			if (delegate instanceof Component) {
				((Component)delegate).applyComponentOrientation(o);
			}
		}

	}


	/**
	 * Listens for changes in the table model.
	 */
	private final class TableModelHandler implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {

			// If we're not sorting by anything, just pass the event along.
			if (!isSorting()) {
				clearSortingState();
				fireTableChanged(e);
				return;
			}

			// If the table structure has changed, cancel the sorting; the
			// sorting columns may have been either moved or deleted from
			// the model.
			if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
				cancelSorting();
				fireTableChanged(e);
				return;
			}

			// We can map a cell event through to the view without widening
			// when the following conditions apply:
			//
			// a) all the changes are on one row (e.getFirstRow() == e.getLastRow()) and,
			// b) all the changes are in one column (column != TableModelEvent.ALL_COLUMNS) and,
			// c) we are not sorting on that column (getSortingStatus(column) == NOT_SORTED) and,
			// d) a reverse lookup will not trigger a sort (modelToView != null)
			//
			// Note: INSERT and DELETE events fail this test as they have column == ALL_COLUMNS.
			//
			// The last check, for (modelToView != null) is to see if modelToView
			// is already allocated. If we don't do this check; sorting can become
			// a performance bottleneck for applications where cells
			// change rapidly in different parts of the table. If cells
			// change alternately in the sorting column and then outside of
			// it this class can end up re-sorting on alternate cell updates -
			// which can be a performance problem for large tables. The last
			// clause avoids this problem.
			int column = e.getColumn();
			if (e.getFirstRow() == e.getLastRow() &&
				column != TableModelEvent.ALL_COLUMNS &&
				getSortingStatus(column) == NOT_SORTED &&
				modelToView != null) {
				int viewIndex = getModelToView()[e.getFirstRow()];
				fireTableChanged(new TableModelEvent(FileExplorerTableModel.this,
											viewIndex, viewIndex,
											column, e.getType()));
				return;
			}

			// Something has happened to the data that may have invalidated the row order.
			clearSortingState();
			fireTableDataChanged();

		}

	}


}
