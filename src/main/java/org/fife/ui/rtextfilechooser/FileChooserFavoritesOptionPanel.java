/*
 * 01/16/2008
 *
 * FileChooserFavoritesOptionPanel.java - Option panel for managing the
 * file chooser "Favorite Directories" list.
 * Copyright (C) 2008 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.fife.ui.EscapableDialog;
import org.fife.ui.OptionsDialogPanel;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.SubstanceUtils;
import org.fife.ui.UIUtil;
import org.fife.ui.modifiabletable.AbstractRowHandler;
import org.fife.ui.modifiabletable.ModifiableTable;
import org.fife.ui.modifiabletable.ModifiableTableChangeEvent;
import org.fife.ui.modifiabletable.ModifiableTableListener;


/**
 * Option panel for managing the "Favorite Directories" of an
 * <code>RTextFileChooser</code>.  This option panel can be added to an
 * options dialog if this file chooser has "favorites" enabled.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class FileChooserFavoritesOptionPanel extends OptionsDialogPanel
						implements ModifiableTableListener {

	private static final long serialVersionUID = 1L;

	public static final String FAVORITES_PROPERTY	= "Favorites";

	private FavoritesTableModel model;
	private ModifiableTable modifiableTable;

	private static final String MSG =
			"org.fife.ui.rtextfilechooser.FileChooserFavoritesOptionPanel";

	private static final String EDIT_FAVORITES_DIALOG_MSG =
					"org.fife.ui.rtextfilechooser.EditFavoriteDialog";

	private static final String SUBSTANCE_TABLE_RENDERER_CLASS =
		"org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer";


	/**
	 * Constructor.  All strings in the file chooser are initialized via the
	 * current locale.
	 */
	public FileChooserFavoritesOptionPanel() {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle(MSG);
	
		setName(msg.getString("Favorites"));
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());

		JPanel favoritesPanel = new JPanel(new BorderLayout());
		favoritesPanel.setBorder(new OptionPanelBorder(
								msg.getString("FavoritesSection")));
		String header = msg.getString("FavoriteTable.Header");
		model = new FavoritesTableModel(header);
		modifiableTable = new ModifiableTable(model,
				BorderLayout.SOUTH, ModifiableTable.ALL_BUTTONS);
		modifiableTable.setRowHandler(new FavoritesRowHandler());
		modifiableTable.addModifiableTableListener(this);
		JTable favoritesTable = modifiableTable.getTable();
		favoritesTable.setDefaultRenderer(Object.class, createCellRenderer());
		favoritesTable.setShowGrid(false);
		favoritesPanel.add(modifiableTable);
		add(favoritesPanel);

		applyComponentOrientation(orientation);

	}


	/**
	 * Creates and returns the renderer used in this option panel's table.
	 *
	 * @return The renderer.
	 */
	private DefaultTableCellRenderer createCellRenderer() {
		// Explicitly use a DefaultTableCellRenderer in case some future
		// version of Java uses a different one.  We'll manipulate it
		// later.
		DefaultTableCellRenderer r = null;
		if (SubstanceUtils.isSubstanceInstalled()) {
			// Use reflection to avoid hard dependency on Substance
			try {
				Class<?> clazz = Class.forName(SUBSTANCE_TABLE_RENDERER_CLASS);
				r = (DefaultTableCellRenderer)clazz.newInstance();
			} catch (Exception e) { // Never happens
				e.printStackTrace();
			}
		}
		if (r==null) { // All other LaFs, or Substance freakishly fails
			r = new DefaultTableCellRenderer();
		}
		r.setIcon(FileChooserIconManager.createFolderIcon());
		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		r.setComponentOrientation(orientation);
		return r;
	}


	/**
	 * Applies the settings entered into this dialog on the specified
	 * application.
	 *
	 * @param owner The application.  This application should implement
	 *        {@link FileChooserOwner}.
	 * @throws IllegalArgumentException If <code>owner</code> is not a
	 *         {@link FileChooserOwner}.
	 */
	@Override
	protected void doApplyImpl(Frame owner) {
		if (!(owner instanceof FileChooserOwner)) {
			throw new IllegalArgumentException(
									"owner must be a FileChooserOwner");
		}
		FileChooserOwner fco = (FileChooserOwner)owner;
		installFavorites(fco.getFileChooser());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		return null;
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 */
	@Override
	public JComponent getTopJComponent() {
		return modifiableTable;
	}


	/**
	 * Changes the specified file chooser's favorites so they match
	 * those entered in this options panel.
	 *
	 * @param chooser The file choose whose favorites to modify.
	 * @see #setFavorites(RTextFileChooser)
	 */
	public void installFavorites(RTextFileChooser chooser) {
		model.setChooserFavorites(chooser);
	}


	/**
	 * Called whenever the extension/color mapping table is changed.
	 *
	 * @param e An event describing the change.
	 */
	public void modifiableTableChanged(ModifiableTableChangeEvent e) {
		hasUnsavedChanges = true;
		firePropertyChange(FAVORITES_PROPERTY, null, new Integer(e.getRow()));
	}


	/**
	 * Sets the favorites displayed in this option panel to those
	 * known by the specified file chooser.
	 *
	 * @param chooser The file chooser
	 * @see #installFavorites(RTextFileChooser)
	 */
	public void setFavorites(RTextFileChooser chooser) {
		model.initFavorites(chooser);
	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.  This should implement
	 *        {@link FileChooserOwner}.
	 * @throws IllegalArgumentException If <code>owner</code> is not a
	 *         {@link FileChooserOwner}.
	 * @see #setValues(Frame)
	 */
	@Override
	protected void setValuesImpl(Frame owner) {
		if (!(owner instanceof FileChooserOwner)) {
			throw new IllegalArgumentException(
									"owner must be a FileChooserOwner");
		}
		FileChooserOwner fco = (FileChooserOwner)owner;
		setFavorites(fco.getFileChooser());
	}


	/**
	 * Updates this panel's UI in response to a LaF change.  This is
	 * overridden to update the table's renderer.
	 */
	@Override
	public void updateUI() {
		if (modifiableTable!=null) {
			// We explicitly set a DefaultTableCellRenderer, so this cast
			// is safe. Update the renderer's icon to the folder icon used
			// in this LaF.
			DefaultTableCellRenderer r = (DefaultTableCellRenderer)
				modifiableTable.getTable().getDefaultRenderer(Object.class);
			r.setIcon(FileChooserIconManager.createFolderIcon());
		}
		super.updateUI();
	}


	/**
	 * The dialog that allows the user to add or modify Favorite.
	 */
	private static class EditFavoriteDialog extends EscapableDialog
				implements ActionListener, DocumentListener {

		static final int OK		= 0;
		static final int CANCEL	= 1;

		private JTextField dirField;
		private JButton okButton;
		private JButton cancelButton;
		private int rc;

		public EditFavoriteDialog(JDialog owner) {

			super(owner);
			ComponentOrientation orientation = ComponentOrientation.
										getOrientation(getLocale());
			ResourceBundle msg = ResourceBundle.
								getBundle(EDIT_FAVORITES_DIALOG_MSG);
			JPanel contentPane = new ResizableFrameContentPane(
											new BorderLayout());
			contentPane.setBorder(UIUtil.getEmpty5Border());

			// Panel containing main stuff.
			Box topPanel = Box.createVerticalBox();
			JPanel temp = new JPanel(new BorderLayout(5, 0));
			temp.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
			JLabel label = UIUtil.newLabel(msg, "Directory");
			temp.add(label, BorderLayout.LINE_START);
			dirField = new JTextField(35);
			dirField.getDocument().addDocumentListener(this);
			label.setLabelFor(dirField);
			temp.add(dirField);
			JButton browseButton = new JButton(msg.getString("Browse.Text"));
			browseButton.setMnemonic((int)msg.getString("Browse.Mnemonic").
													charAt(0));
			browseButton.setActionCommand("Browse");
			browseButton.addActionListener(this);
			temp.add(browseButton, BorderLayout.LINE_END);
			topPanel.add(temp);
			contentPane.add(topPanel, BorderLayout.NORTH);

			// Panel containing buttons for the bottom.
			okButton = UIUtil.newButton(msg,
								"OK.Text", "OK.Mnemonic");
			okButton.addActionListener(this);
			cancelButton = UIUtil.newButton(msg,
								"Cancel.Text", "Cancel.Mnemonic");
			cancelButton.addActionListener(this);
			Container buttons=UIUtil.createButtonFooter(okButton, cancelButton);
			contentPane.add(buttons, BorderLayout.SOUTH);

			// Get ready to go.
			setTitle(msg.getString("Title"));
			setContentPane(contentPane);
			getRootPane().setDefaultButton(okButton);
			setModal(true);
			applyComponentOrientation(orientation);
			pack();

		}

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source==okButton) {
				rc = OK;
				escapePressed();
			}
			else if (source==cancelButton) {
				escapePressed();
			}
			else {
				String command = e.getActionCommand();
				if ("Browse".equals(command)) {
					RDirectoryChooser chooser =
						new RDirectoryChooser((JDialog)getOwner());
					chooser.setChosenDirectory(new File(getDirectory()));
					chooser.setVisible(true);
					String chosenDir = chooser.getChosenDirectory();
					if (chosenDir!=null) {
						dirField.setText(chosenDir);
					}
				}
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public String getDirectory() {
			return dirField.getText();
		}

		public void insertUpdate(DocumentEvent e) {
			okButton.setEnabled(true);
		}

		public void removeUpdate(DocumentEvent e) {
			okButton.setEnabled(dirField.getDocument().getLength()>0);
		}

		public void setData(String dir) {
			dirField.setText(dir);
		}

		public int showEditFavoriteDialog() {
			rc = CANCEL; // Set here in case they "X" the dialog out.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dirField.requestFocusInWindow();
					dirField.selectAll();
				}
			});
			setLocationRelativeTo(getOwner());
			okButton.setEnabled(false);
			setVisible(true);
			return rc;
		}

	}


	/**
	 * Handles the addition, removal, and modifying of rows in the Favorites
	 * table.
	 */
	class FavoritesRowHandler extends AbstractRowHandler {

		private EditFavoriteDialog dialog;

		public Object[] getNewRowInfo(Object[] oldData) {
			if (dialog==null) {
				dialog = new EditFavoriteDialog(getOptionsDialog());
			}
			dialog.setData(oldData==null ? null : ((String)oldData[0]));
			int rc = dialog.showEditFavoriteDialog();
			if (rc==EditFavoriteDialog.OK) {
				return new Object[] { dialog.getDirectory() };
			}
			return null;
		}

		/**
		 * Overridden to update the UI of the cached dialog, if necessary.
		 */
		@Override
		public void updateUI() {
			if (dialog!=null) {
				SwingUtilities.updateComponentTreeUI(dialog);
			}
		}

	}


	/**
	 * The table model used by the Favorites table.
	 */
	private static class FavoritesTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames;

		public FavoritesTableModel(String favoriteHeader) {
			columnNames = new String[1];
			columnNames[0] = favoriteHeader;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		public void initFavorites(RTextFileChooser chooser) {
			setRowCount(0);
			String[] favorites = chooser.getFavorites(); // non-null
			for (int i=0; i<favorites.length; i++) {
				// DefaultTableModel uses Vectors internally, so we'll
				// use them here too.
				Vector<String> v = new Vector<String>(1);
				v.add(favorites[i]);
				addRow(v);
			}
		}

		public void setChooserFavorites(RTextFileChooser chooser) {
			chooser.clearFavorites();
			for (int i=0; i<getRowCount(); i++) {
				String favorite = (String)getValueAt(i, 0);
				chooser.addToFavorites(favorite);
			}
		}

	}


}