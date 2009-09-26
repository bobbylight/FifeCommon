/*
 * 07/28/2005
 *
 * ModifiableTable.java - A table modified by Add, Remove, and Modify buttons.
 * Copyright (C) 2005 Robert Futrell
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
package org.fife.ui.modifiabletable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;

import org.fife.ui.RButton;
import org.fife.ui.RScrollPane;
import org.fife.ui.UIUtil;


/**
 * A table with "Add," "Remove," and possibly "Modify" buttons.<p>
 *
 * Basically, the caller provides the table model for the data, along
 * with a "handler" that is called when adding/editing/removing data
 * from the model.  When you want to retrieve the data back from the
 * table, you can get it via {@link #getDataVector}.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see ModifiableTableListener
 */
public class ModifiableTable extends JPanel {

	public static final int ADD				= 1;
	public static final int REMOVE			= 2;
	public static final int MODIFY			= 4;
	public static final int ADD_REMOVE			= ADD|REMOVE;
	public static final int ADD_REMOVE_MODIFY	= ADD|REMOVE|MODIFY;

	public static final String TOP			= BorderLayout.NORTH;
	public static final String BOTTOM			= BorderLayout.SOUTH;
	public static final String LEFT			= BorderLayout.WEST;
	public static final String RIGHT			= BorderLayout.EAST;

	private JTable table;
	private RButton addButton;
	private RButton removeButton;
	private RButton modifyButton;
	private RowHandler rowHandler;
	private Listener listener;
	private EventListenerList listenerList;

	private static final String ADD_COMMAND		= "AddCommand";
	private static final String REMOVE_COMMAND	= "RemoveCommand";
	private static final String MODIFY_COMMAND	= "ModifyCommand";

	private static final String BUNDLE_NAME		=
						"org.fife.ui.modifiabletable.ModifiableTable";


	/**
	 * Creates a <code>ModifiableTable</code> with <code>Add</code>,
	 * <code>Remove</code>, and <code>Modify</code> buttons below the table.
	 *
	 * @param model The model to use for the table.
	 * @see #setRowHandler
	 */
	public ModifiableTable(DefaultTableModel model) {
		this(model, null);
	}


	/**
	 * Creates a <code>ModifiableTable</code> with <code>Add</code>,
	 * <code>Remove</code>, and <code>Modify</code> buttons below the table.
	 *
	 * @param model The model to use for the table.
	 * @param columnNames Objects to use for names of the columns.  This value
	 *        can be <code>null</code> if the model already takes care of its
	 *        column names.
	 * @see #setRowHandler
	 */
	public ModifiableTable(DefaultTableModel model, Object[] columnNames) {
		this(model, columnNames, BOTTOM, ADD_REMOVE_MODIFY);
	}


	/**
	 * Constructor.
	 *
	 * @param model The model to use for the table.
	 * @param buttonLocation The location of the buttons, relative to the
	 *        table.
	 * @param buttons A bit flag representing what buttons to display.
	 * @see #setRowHandler
	 */
	public ModifiableTable(DefaultTableModel model, String buttonLocation,
						int buttons) {
		this(model, null, buttonLocation, buttons);
	}


	/**
	 * Constructor.
	 *
	 * @param model The model to use for the table.
	 * @param columnNames Objects to use for names of the columns.  This value
	 *        can be <code>null</code> if the model already takes care of its
	 *        column names.
	 * @param buttonLocation The location of the buttons, relative to the
	 *        table.
	 * @param buttons A bit flag representing what buttons to display.
	 * @see #setRowHandler
	 */
	public ModifiableTable(DefaultTableModel model, Object[] columnNames, 
						String buttonLocation, int buttons) {

		listener = new Listener();
		this.table = createTable(model, columnNames);
		listenerList = new EventListenerList();

		setLayout(new BorderLayout());
		add(new RScrollPane(table));
		add(createButtonPanel(buttonLocation, buttons), buttonLocation);

		UIUtil.fixJTableRendererOrientations(table);

	}


	/**
	 * Adds a listener to this modifiable table.
	 *
	 * @param l The listener to add.
	 * @see #removeModifiableTableListener
	 */
	public void addModifiableTableListener(ModifiableTableListener l) {
		listenerList.add(ModifiableTableListener.class, l);
	}


	/**
	 * Gets information from the user on a new row to add and adds it to
	 * the table.  This method is called whenever the user presses the
	 * <code>Add</code> button.
	 *
	 * @see #modifyRow
	 * @see #removeRow
	 */
	protected void addRow() {
		if (rowHandler!=null) {
			Object[] newData = rowHandler.getNewRowInfo(null);
			if (newData!=null) {
				((DefaultTableModel)table.getModel()).addRow(newData);
				fireModifiableTableEvent(ModifiableTableChangeEvent.ADDED,
									table.getRowCount()-1);
			}
		}
	}


	/**
	 * Returns the panel of buttons for modifying the table.
	 *
	 * @param buttonLocation The location of the buttons, relative to the
	 *        table.
	 * @param buttons A bit flag representing what buttons to display.
	 * @return The panel of buttons.
	 */
	protected JPanel createButtonPanel(String buttonLocation, int buttons) {

		// Get panel and spacing ready.
		JPanel panel = new JPanel();
		JPanel buttonPanel = new JPanel(new BorderLayout());
		String buttonLoc2 = null;
		if (RIGHT.equals(buttonLocation)) {
			panel.setLayout(new GridLayout(3,1, 5,5));
			buttonLoc2 = BorderLayout.PAGE_START;
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		}
		else if (LEFT.equals(buttonLocation)) {
			panel.setLayout(new GridLayout(3,1, 5,5));
			buttonLoc2 = BorderLayout.PAGE_START;
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		}
		else if (TOP.equals(buttonLocation)) {
			panel.setLayout(new GridLayout(1,3, 5,5));
			buttonLoc2 = BorderLayout.LINE_START;
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		}
		else { // BOTTOM, or invalid value.
			panel.setLayout(new GridLayout(1,3, 5,5));
			buttonLoc2 = BorderLayout.LINE_START;
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		}

		ResourceBundle msg = ResourceBundle.getBundle(BUNDLE_NAME);

		// Gather the desired buttons.
		if ((buttons&ADD)==ADD) {
			addButton = new RButton(msg.getString("Button.Add"));
			addButton.setActionCommand(ADD_COMMAND);
			addButton.addActionListener(listener);
			panel.add(addButton);
		}
		if ((buttons&REMOVE)==REMOVE) {
			removeButton = new RButton(msg.getString("Button.Remove"));
			removeButton.setActionCommand(REMOVE_COMMAND);
			removeButton.addActionListener(listener);
			removeButton.setEnabled(false);
			panel.add(removeButton);
		}
		if ((buttons&MODIFY)==MODIFY) {
			modifyButton = new RButton(msg.getString("Button.Modify"));
			modifyButton.setActionCommand(MODIFY_COMMAND);
			modifyButton.addActionListener(listener);
			modifyButton.setEnabled(false);
			panel.add(modifyButton);
		}

		// Get ready to go.
		buttonPanel.add(panel, buttonLoc2);
		return buttonPanel;

	}


	/**
	 * Returns a table using the specified table model with properties set
	 * correctly for a <code>ModifiableTable</code>.
	 *
	 * @param model The table model.
	 * @param columnNames Objects to use for names of the columns.  This value
	 *        can be <code>null</code> if the model already takes care of its
	 *        column names.
	 * @return The table.
	 */
	private final JTable createTable(DefaultTableModel model,
									Object[] columnNames) {
		if (columnNames!=null)
			model.setColumnIdentifiers(columnNames);
		JTable table = new JTable(model) {
			/**
			 * Overridden to ensure the table completely fills the JViewport it
			 * is sitting in.  Note in Java 6 this could be taken care of by the
			 * method JTable#setFillsViewportHeight(boolean).
			 */
			public boolean getScrollableTracksViewportHeight() {
				Component parent = getParent();
				return parent instanceof JViewport ?
					parent.getHeight()>getPreferredSize().height : false;
			}
		};
		table.setShowGrid(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(listener);
		return table;
	}


	/**
	 * Notifies all listeners of a row being added, removed, or modified.
	 *
	 * @param change The change that occurred.
	 * @param row The row that was added, removed or modified.
	 */
	protected void fireModifiableTableEvent(int change, int row) {
		// Guaranteed to return a non-null array.
		Object[] listeners = listenerList.getListenerList();
		if (listeners.length>0) {
			ModifiableTableChangeEvent e =
						new ModifiableTableChangeEvent(this, change, row);
			// Process the listeners last to first, notifying
			// those that are interested in this event.
			for (int i=listeners.length-2; i>=0; i-=2) {
				if (listeners[i]==ModifiableTableListener.class) {
					((ModifiableTableListener)listeners[i+1]).
										modifiableTableChanged(e);
				}
			}
		}
	}


	/**
	 * Returns the data in the specified row of the table as an array of
	 * <code>Object</code>s.
	 *
	 * @param row The row in the table.
	 * @return The data.
	 */
	private Object[] getContentsOfRow(int row) {
		int columnCount = table.getColumnCount();
		Object[] data = new Object[columnCount];
		// NOTE:  We get values from the model as opposed to just the
		// table to keep logical column values in order, even if the user
		// changes their physical order by dragging.
		TableModel model = table.getModel();
		for (int i=0; i<columnCount; i++) {
			data[i] = model.getValueAt(row, i);
		}
		return data;
	}


	/**
	 * Retrieves the data stored in the table as a <code>Vector</code> of
	 * <code>Vector</code>s.  Each <code>Vector</code> inside of the
	 * returned <code>Vector</code> represents a single row.<p>
	 *
	 * This method is simply a wrapper for
	 * <code>((DefaultTableModel)getTable().getModel()).getDataVector()</code>.
	 *
	 * @return The data in the table.
	 */
	public Vector getDataVector() {
		return ((DefaultTableModel)getTable().getModel()).getDataVector();
	}


	/**
	 * Returns the currently selected row.
	 *
	 * @return The currently selected row, or <code>-1</code> if no
	 *         row is selected.
	 */
	public int getSelectedRow() {
		return table.getSelectedRow();
	}


	/**
	 * Returns the table displayed in this modifiable table.
	 *
	 * @return The table.
	 */
	public JTable getTable() {
		return table;
	}


	/**
	 * Modifies the contents of a row in the table.  This method is called
	 * whenever the user presses the <code>Modify</code> button.
	 *
	 * @see #addRow
	 * @see #removeRow
	 */
	protected void modifyRow() {
		if (rowHandler!=null) {
			int selectedRow = table.getSelectedRow();
			Object[] oldData = getContentsOfRow(selectedRow);
			Object[] newData = rowHandler.getNewRowInfo(oldData);
			if (newData!=null) {
				int columnCount = table.getColumnCount();
				for (int i=0; i<columnCount; i++) {
					// Call model's setValueAt(), not table's, so that if
					// they've moved columns around it's still okay.
					table.getModel().
							setValueAt(newData[i], selectedRow, i);
				}
				fireModifiableTableEvent(
						ModifiableTableChangeEvent.MODIFIED, selectedRow);
			}
		}
	}


	/**
	 * Removes a listener from this modifiable table.
	 *
	 * @param l The listener to remove.
	 * @see #addModifiableTableListener
	 */
	public void removeModifiableTableListener(ModifiableTableListener l) {
		listenerList.remove(ModifiableTableListener.class, l);
	}


	/**
	 * Removes the contents of the selected row in the table.  This method
	 * is called whenever the user presses the <code>Remove</code> button.
	 *
	 * @see #addRow
	 * @see #modifyRow
	 */
	protected void removeRow() {
		if (rowHandler!=null) {
			int row = table.getSelectedRow();
			if (rowHandler.shouldRemoveRow(row)) {
				DefaultTableModel model = (DefaultTableModel)table.
													getModel();
				model.removeRow(row);
				// Select a new row as old "current" row was just removed.
				int rowCount = model.getRowCount();
				if (rowCount>row)
					table.setRowSelectionInterval(row, row);
				else if (rowCount>0 && rowCount==row)
					table.setRowSelectionInterval(row-1, row-1);
				fireModifiableTableEvent(ModifiableTableChangeEvent.REMOVED,
									row);
			}
		}
	}


	/**
	 * Sets the handler used when the user adds or modifies a row in the
	 * table.
	 *
	 * @param handler The new handler.
	 * @see RowHandler
	 */
	public void setRowHandler(RowHandler handler) {
		this.rowHandler = handler;
	}


	/**
	 * Updates the Look and Feel of this table.  Overridden to also update
	 * the LaF of the <code>RowHandler</code>, if it is a Swing component.
	 */
	public void updateUI() {
		super.updateUI();
		if (rowHandler!=null) { // Should always be true
			rowHandler.updateUI();
		}
	}


	/**
	 * Listens for events in this modifiable table.
	 */
	class Listener implements ActionListener, ListSelectionListener {

		public Listener() {
		}

		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();
			if (ADD_COMMAND.equals(actionCommand)) {
				addRow();
			}
			else if (REMOVE_COMMAND.equals(actionCommand)) {
				removeRow();
			}
			else if (MODIFY_COMMAND.equals(actionCommand)) {
				modifyRow();
			}
		}

		public void valueChanged(ListSelectionEvent e) {
			if (rowHandler==null)
				return;
			int row = table.getSelectedRow();
			boolean selection = row>-1;
			if (modifyButton!=null) {
				modifyButton.setEnabled(selection);
			}
			if (removeButton!=null) {
				boolean canRemove = rowHandler.shouldRemoveRow(row);
				removeButton.setEnabled(selection && canRemove);
			}
		}

	}


}