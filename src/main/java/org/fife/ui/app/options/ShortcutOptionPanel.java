/*
 * 8/21/2004
 *
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.options;

import org.fife.ui.GetKeyStrokeDialog;
import org.fife.ui.KeyStrokeCellRenderer;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.UIUtil;
import org.fife.ui.app.AbstractGUIApplication;
import org.fife.ui.modifiabletable.AbstractRowHandler;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.ResourceBundle;


/**
 * Option panel letting the user configure shortcut keys for an
 * {@code AbstractGUIApplication}.
 *
 * @author Robert Futrell
 * @version 0.2
 */
public class ShortcutOptionPanel extends OptionsDialogPanel
					implements ActionListener, ModifiableTableListener {

	private final ModifiableTable shortcutTable;
	private final DefaultTableModel model;
	private final AbstractGUIApplication<?> app;
	private Action[] masterActionList;

	private static final String MSG_BUNDLE = "org.fife.ui.app.options.ShortcutOptionPanel";
	private static final ResourceBundle MSG = ResourceBundle.getBundle(MSG_BUNDLE);

	/**
	 * Constructor.
	 *
	 * @param app The owner of the options dialog in which this panel
	 *        appears.
	 */
	public ShortcutOptionPanel(final AbstractGUIApplication<?> app) {

		super(MSG.getString("Options.Shortcut.Name"));
		this.app = app;

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		setLayout(new BorderLayout());
		setBorder(UIUtil.getEmpty5Border());
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(
					new OptionPanelBorder(MSG.getString("Options.Shortcut.Label")));
		add(contentPane);

		model = new DefaultTableModel(new Object[] {
				MSG.getString("Options.Shortcut.Col1"), MSG.getString("Options.Shortcut.Col2")}, 0);
		shortcutTable = new ModifiableTable(model, ModifiableTable.BOTTOM,
										ModifiableTable.MODIFY);
		shortcutTable.addModifiableTableListener(this);
		shortcutTable.setRowHandler(new ShortcutTableRowHandler());
		JTable table = shortcutTable.getTable();
		table.getColumn(MSG.getString("Options.Shortcut.Col2")).setCellRenderer(
									KeyStrokeCellRenderer.create());
		table.setPreferredScrollableViewportSize(new Dimension(300,300));
		contentPane.add(shortcutTable);

		JButton defButton = new JButton(MSG.getString("RestoreDefaults"));
		defButton.setActionCommand("RestoreDefaults");
		defButton.addActionListener(this);
		JPanel temp = new JPanel(new BorderLayout());
		temp.setBorder(BorderFactory.createEmptyBorder(COMPONENT_VERTICAL_SPACING,0,0,0));
		temp.add(defButton, BorderLayout.LINE_START);
		add(temp, BorderLayout.SOUTH);
		applyComponentOrientation(orientation);

	}


	/**
	 * Listens for actions in this panel.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if ("RestoreDefaults".equals(actionCommand)) {
			app.restoreDefaultAccelerators(); // Does mainView too.
			setActions(app);
		}

	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.
	 */
	@Override
	protected void doApplyImpl(Frame owner) {

		Action[] actions = getActions();
		int actionsLength = actions.length;
		Action[] realActions = ((AbstractGUIApplication<?>)owner).getActions();
		int j;

		for (Action realAction : realActions) {
			String name = (String)realAction.getValue(Action.NAME);
			for (j = 0; j < actionsLength; j++) {
				String name2 = (String)actions[j].getValue(Action.NAME);
				if (name.equals(name2)) {
					realAction.putValue(Action.ACCELERATOR_KEY,
						actions[j].getValue(Action.ACCELERATOR_KEY));
					break;
				}
			}
			if (j == actionsLength) {
				System.err.println("Internal error: could not find config for action: " +
					name);
			}
		}

	}


	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns the actions and their shortcuts defined by the user.
	 *
	 * @return The actions and their shortcuts.
	 */
	private Action[] getActions() {
		updateMasterActionList();
		return masterActionList;
	}


	@Override
	public JComponent getTopJComponent() {
		return shortcutTable.getTable();
	}


	/**
	 * Called whenever the extension/color mapping table is changed.
	 *
	 * @param e An event describing the change.
	 */
	@Override
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		setDirty(true);
	}


	/**
	 * Sets the actions to display as configurable.
	 *
	 * @param app The application whose actions should be displayed.
	 */
	private void setActions(AbstractGUIApplication<?> app) {

		masterActionList = app.getActions();

		Arrays.sort(masterActionList, (a1, a2) -> {
			String name1 = (String)a1.getValue(Action.NAME);
			String name2 = (String)a2.getValue(Action.NAME);
			if (name1 == null) {
				if (name2 == null)
					return 0;
				return -1;
			}
			if (name2 == null) // name1!=null && name2==null.
				return 1;
			return name1.compareTo(name2);
		});

		// Action count may change from initial value as plugins might
		// add Actions to application.
		model.setRowCount(masterActionList.length);

		for (int i=0; i<masterActionList.length; i++) {
			model.setValueAt(masterActionList[i].getValue(Action.NAME), i,0);
			model.setValueAt(masterActionList[i].getValue(Action.ACCELERATOR_KEY), i,1);
		}

	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	@Override
	protected void setValuesImpl(Frame owner) {
		setActions(app);
	}


	/**
	 * Synchronizes the master action list with the values entered in the
	 * table.
	 */
	private void updateMasterActionList() {
		int num = masterActionList.length;
		for (int i=0; i<num; i++) {
			masterActionList[i].putValue(Action.ACCELERATOR_KEY,
									model.getValueAt(i,1));
		}
	}


	/**
	 * Handles modification of shortcut table values.
	 */
	private final class ShortcutTableRowHandler extends AbstractRowHandler {

		private GetKeyStrokeDialog ksDialog;

		@Override
		public Object[] getNewRowInfo(Object[] oldData) {
			KeyStroke keyStroke = (KeyStroke)oldData[1];
			String action = (String)oldData[0];
			if (ksDialog==null) {
				ksDialog = new GetKeyStrokeDialog(getOptionsDialog(), null);
			}
			ksDialog.setKeyStroke(keyStroke);
			ksDialog.setLocationRelativeTo(ShortcutOptionPanel.this);
			ksDialog.setVisible(true);
			if (!ksDialog.getCancelled()) {
				KeyStroke temp = ksDialog.getKeyStroke();
				if ((temp==null && keyStroke!=null) ||
						(temp!=null && !temp.equals(keyStroke))) {
					return new Object[] { action, temp };
				}
			}
			return null;
		}

		@Override
		public boolean canRemoveRow(int row) {
			return false; // Can modify any row, but can't remove any
		}

		/**
		 * Overridden to update the cached dialog, if necessary.
		 */
		@Override
		public void updateUI() {
			if (ksDialog!=null) {
				SwingUtilities.updateComponentTreeUI(ksDialog);
			}
		}

	}


}
