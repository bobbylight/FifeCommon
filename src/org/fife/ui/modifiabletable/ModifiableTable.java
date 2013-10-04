/*
 * 07/28/2005
 *
 * ModifiableTable.java - A table modified by Add, Remove, and Modify buttons.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.modifiabletable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;

import org.fife.ui.RScrollPane;
import org.fife.ui.UIUtil;


/**
 * A table with "Add," "Remove," and possibly "Modify" buttons.<p>
 *
 * Basically, the caller provides the table model for the data, along
 * with a "handler" that is called when adding/editing/removing data
 * from the model.  When you want to retrieve the data back from the
 * table, you can get it via {@link #getDataVector()}.
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
	public static final int MOVE_UP_DOWN	= 8;
	public static final int ALL_BUTTONS		= ADD_REMOVE_MODIFY|MOVE_UP_DOWN;

	public static final String TOP			= BorderLayout.NORTH;
	public static final String BOTTOM			= BorderLayout.SOUTH;
	public static final String LEFT			= BorderLayout.WEST;
	public static final String RIGHT			= BorderLayout.EAST;

	/**
	 * If this system property is set to <code>true</code>, then the panels
	 * created containing the buttons ("Add", "Modify", "Remove", etc.) will
	 * be non-opaque.  This is useful on OS's such as Windows XP and Vista,
	 * where tabbed panes have a gradient background.  By default this is
	 * <code>false</code> in case it messes up some other LookAndFeel.
	 */
	public static final String PROPERTY_PANELS_NON_OPAQUE =
										"ModifiableTable.nonOpaquePanels";

	private JTable table;
	private JButton addButton;
	private JButton removeButton;
	private JButton modifyButton;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private RowHandler rowHandler;
	private int firstMovableRow;
	private Listener listener;
	private EventListenerList listenerList;

	private static final String ADD_COMMAND			= "AddCommand";
	private static final String REMOVE_COMMAND		= "RemoveCommand";
	private static final String MODIFY_COMMAND		= "ModifyCommand";
	private static final String MOVE_UP_COMMAND		= "MoveUpCommand";
	private static final String MOVE_DOWN_COMMAND	= "MoveDownCommand";


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
	 * @see #setRowHandler(RowHandler)
	 * @see #ADD_REMOVE
	 * @see #ADD_REMOVE_MODIFY
	 * @see #MOVE_UP_DOWN
	 * @see #ALL_BUTTONS
	 */
	public ModifiableTable(DefaultTableModel model, String buttonLocation,
						int buttons) {
		this(model, null, buttonLocation, buttons);
	}


	/**
	 * Constructor.
	 *
	 * @param model The model to use for the table.
	 * @param buttonLocation The location of the buttons, relative to the
	 *        table.
	 * @param buttons A bit flag representing what buttons to display.
	 * @param customButtons A list of {@link Action}s for "extra"
	 *        buttons to add.  This may be <code>null</code> if no custom
	 *        buttons are to be added.
	 * @see #setRowHandler(RowHandler)
	 * @see #ADD_REMOVE
	 * @see #ADD_REMOVE_MODIFY
	 * @see #MOVE_UP_DOWN
	 * @see #ALL_BUTTONS
	 */
	public ModifiableTable(DefaultTableModel model, String buttonLocation,
						int buttons, List<? extends Action> customButtons) {
		this(model, null, buttonLocation, buttons, customButtons);
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
		this(model, columnNames, buttonLocation, buttons, null);
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
	 * @param customButtons A list of {@link Action}s for "extra"
	 *        buttons to add.  This may be <code>null</code> if no custom
	 *        buttons are to be added.
	 * @see #setRowHandler
	 */
	public ModifiableTable(DefaultTableModel model, Object[] columnNames, 
						String buttonLocation, int buttons,
						List<? extends Action> customButtons) {

		if (Boolean.getBoolean(PROPERTY_PANELS_NON_OPAQUE)) {
			setOpaque(false);
		}

		listener = new Listener();
		this.table = createTable(model, columnNames);
		listenerList = new EventListenerList();

		setLayout(new BorderLayout());
		add(new RScrollPane(table));
		add(createButtonPanel(buttonLocation, buttons, customButtons),
								buttonLocation);

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
	 * @param customButtons A list of {@link Action}s for "extra"
	 *        buttons to add.  This may be <code>null</code> if no custom
	 *        buttons are to be added.
	 * @return The panel of buttons.
	 */
	protected JPanel createButtonPanel(String buttonLocation, int buttons,
			List<? extends Action> customButtons) {

		// Get panel and spacing ready.
		JPanel panel = null;
		JPanel buttonPanel = null;

		if (Boolean.getBoolean(PROPERTY_PANELS_NON_OPAQUE)) {
			panel = UIUtil.newTabbedPanePanel();
			buttonPanel = UIUtil.newTabbedPanePanel(new BorderLayout());
		}
		else {
			panel = new JPanel();
			buttonPanel = new JPanel(new BorderLayout());
		}

		ResourceBundle msg = ResourceBundle.getBundle(
										ModifiableTable.class.getName());

		// Gather the desired buttons.
		int buttonCount = 0;
		if ((buttons&ADD)==ADD) {
			addButton = new JButton(msg.getString("Button.Add"));
			addButton.setActionCommand(ADD_COMMAND);
			addButton.addActionListener(listener);
			panel.add(addButton);
			buttonCount++;
		}
		if ((buttons&REMOVE)==REMOVE) {
			removeButton = new JButton(msg.getString("Button.Remove"));
			removeButton.setActionCommand(REMOVE_COMMAND);
			removeButton.addActionListener(listener);
			removeButton.setEnabled(false);
			panel.add(removeButton);
			buttonCount++;
		}
		if ((buttons&MODIFY)==MODIFY) {
			modifyButton = new JButton(msg.getString("Button.Modify"));
			modifyButton.setActionCommand(MODIFY_COMMAND);
			modifyButton.addActionListener(listener);
			modifyButton.setEnabled(false);
			panel.add(modifyButton);
			buttonCount++;
		}
		if ((buttons&MOVE_UP_DOWN)==MOVE_UP_DOWN) {
			moveUpButton = new JButton(msg.getString("Button.MoveUp"));
			moveUpButton.setActionCommand(MOVE_UP_COMMAND);
			moveUpButton.addActionListener(listener);
			moveUpButton.setEnabled(false);
			panel.add(moveUpButton);
			moveDownButton = new JButton(msg.getString("Button.MoveDown"));
			moveDownButton.setActionCommand(MOVE_DOWN_COMMAND);
			moveDownButton.addActionListener(listener);
			moveDownButton.setEnabled(false);
			panel.add(moveDownButton);
			buttonCount += 2;
		}

		// Any custom buttons specified by the user.  These currently must
		// always stay enabled.
		if (customButtons!=null) {
			for (Action a : customButtons) {
				JButton extraButton = new JButton(a);
				panel.add(extraButton);
				buttonCount++;
			}
		}

		// Lay out the panel properly.
		String buttonLoc2 = null;
		if (RIGHT.equals(buttonLocation)) {
			panel.setLayout(new GridLayout(buttonCount,1, 5,5));
			buttonLoc2 = BorderLayout.PAGE_START;
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		}
		else if (LEFT.equals(buttonLocation)) {
			panel.setLayout(new GridLayout(buttonCount,1, 5,5));
			buttonLoc2 = BorderLayout.PAGE_START;
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		}
		else if (TOP.equals(buttonLocation)) {
			panel.setLayout(new GridLayout(1,buttonCount, 5,5));
			buttonLoc2 = BorderLayout.LINE_START;
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		}
		else { // BOTTOM, or invalid value.
			panel.setLayout(new GridLayout(1,buttonCount, 5,5));
			buttonLoc2 = BorderLayout.LINE_START;
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
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
			@Override
			public boolean getScrollableTracksViewportHeight() {
				Component parent = getParent();
				return parent instanceof JViewport ?
					parent.getHeight()>getPreferredSize().height : false;
			}
			/**
			 * Overridden so that, if this component is disabled, the table
			 * also appears disabled (why doesn't this happen by default???).
			 */
			@Override
			public Component prepareRenderer(TableCellRenderer renderer,
											int row, int column) {
				Component comp = super.prepareRenderer(renderer, row, column);
				comp.setEnabled(isEnabled());  // Enable/disable renderer same as table.
				return comp;
			}
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		table.setShowGrid(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(listener);
		table.addMouseListener(listener);
		return table;
	}


	/**
	 * Ensure that the currently selected row in the table is visible.  If no
	 * row is selected, this method does nothing.
	 */
	private void ensureSelectedRowIsVisible() {
		int row = table.getSelectedRow();
		if (row>-1) {
	        Rectangle cellBounds = table.getCellRect(row, 0, true);
	        if (cellBounds != null) {
	            table.scrollRectToVisible(cellBounds);
	        }
		}
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
	public Vector<?> getDataVector() {
		return ((DefaultTableModel)getTable().getModel()).getDataVector();
	}


	/**
	 * Returns the "first movable row."  If this property is set to a value
	 * greater than <code>0</code>, then the rows
	 * <code>0</code> through <code>getFirstMovableRow()-1</code> can not be
	 * moved up or down, and other rows cannot be moved up into them.<p>
	 * 
	 * This property is ignored if this modifiable table is not displaying the
	 * "move up" and "move down" buttons.
	 *
	 * @return The first movable row.
	 * @see #setFirstMovableRow(int)
	 */
	public int getFirstMovableRow() {
		return firstMovableRow;
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
	 * @see #addRow()
	 * @see #removeRow()
	 */
	protected void modifyRow() {
		if (rowHandler!=null) {
			int selectedRow = table.getSelectedRow();
			if (selectedRow>-1 && // Should always be true
					rowHandler.canModifyRow(selectedRow)) {
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
			else {
				UIManager.getLookAndFeel().provideErrorFeedback(table);
			}
		}
	}


	/**
	 * Swaps the selected row with another.  If the selected row cannot be
	 * moved the amount specified, nothing happens.
	 *
	 * @param amt The offset between the selected row and the row to swap it
	 *        with; e.g. "1" or "-1".
	 */
	protected void moveRow(int amt) {

		int selectedRow = table.getSelectedRow();

		if (selectedRow>=0 && (selectedRow+amt)>=0 &&
				(selectedRow+amt)<table.getRowCount()) {
			Object[] row1 = getContentsOfRow(selectedRow+amt);
			Object[] row2 = getContentsOfRow(selectedRow);
			int colCount = row1.length;
			TableModel model = table.getModel();
			for (int i=0; i<colCount; i++) {
				// Call model's setValueAt(), not table's, so that if
				// they've moved columns around it's still okay.
				model.setValueAt(row1[i], selectedRow, i);
				model.setValueAt(row2[i], selectedRow+amt, i);
			}
			table.getSelectionModel().setSelectionInterval(selectedRow+amt,
															selectedRow+amt);
			ensureSelectedRowIsVisible();
			fireModifiableTableEvent(
					ModifiableTableChangeEvent.MODIFIED, selectedRow+amt);
			fireModifiableTableEvent(
					ModifiableTableChangeEvent.MODIFIED, selectedRow);
		}

		else {
			UIManager.getLookAndFeel().provideErrorFeedback(table);
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
			if (row==-1) { // Should never happen
				UIManager.getLookAndFeel().provideErrorFeedback(this);
			}
			else if (rowHandler.canRemoveRow(row)) {
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
	 * Toggles whether this modifiable table is enabled.
	 *
	 * @param enabled Whether this modifiable table is enabled.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (enabled!=isEnabled()) {
			super.setEnabled(enabled);
			if (addButton!=null) {
				addButton.setEnabled(enabled);
			}
			listener.valueChanged(null);
			table.setEnabled(enabled);
		}
	}


	/**
	 * Sets the "first movable row."  If this property is set to a value
	 * greater than <code>0</code>, then the rows
	 * <code>0</code> through <code>getFirstMovableRow()-1</code> can not be
	 * moved up or down, and other rows cannot be moved up into them.<p>
	 * 
	 * Typically, if this is modified, you'll also want to ensure your
	 * <code>RowHandler</code> forbids the removal of the unmovable rows.<p>
	 * 
	 * This property is ignored if this modifiable table is not displaying the
	 * "move up" and "move down" buttons.
	 * 
	 * @param firstMovableRow The first movable row.  This should be greater
	 *        than <code>0</code>.
	 * @see #getFirstMovableRow()
	 */
	public void setFirstMovableRow(int firstMovableRow) {
		this.firstMovableRow = Math.max(0, firstMovableRow);
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
	@Override
	public void updateUI() {
		super.updateUI();
		if (rowHandler!=null) { // Should always be true
			rowHandler.updateUI();
		}
	}


	/**
	 * Listens for events in this modifiable table.
	 */
	class Listener extends MouseAdapter implements ActionListener,
							ListSelectionListener {

		public Listener() {
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (ADD_COMMAND.equals(command)) {
				addRow();
			}
			else if (REMOVE_COMMAND.equals(command)) {
				removeRow();
			}
			else if (MODIFY_COMMAND.equals(command)) {
				modifyRow();
			}
			else if (MOVE_UP_COMMAND.equals(command)) {
				moveRow(-1);
				
			}
			else if (MOVE_DOWN_COMMAND.equals(command)) {
				moveRow(1);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount()==2 && e.getButton()==MouseEvent.BUTTON1) {
				modifyRow();
			}
		}

		public void valueChanged(ListSelectionEvent e) {
			if (rowHandler==null)
				return;
			int row = table.getSelectedRow();
			boolean selection = row>-1 && isEnabled();
			if (modifyButton!=null) {
				boolean canModify = rowHandler.canModifyRow(row);
				modifyButton.setEnabled(selection && canModify);
			}
			if (removeButton!=null) {
				boolean canRemove = rowHandler.canRemoveRow(row);
				removeButton.setEnabled(selection && canRemove);
			}
			if (moveUpButton!=null) {
				moveUpButton.setEnabled(selection && row>getFirstMovableRow());
				moveDownButton.setEnabled(selection &&
						row>=getFirstMovableRow() && row<table.getRowCount()-1);
			}
		}

	}


}