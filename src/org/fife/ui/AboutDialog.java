/*
 * 09/02/2004
 *
 * AboutDialog.java - An About dialog for an application.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.fife.io.IOUtil;


/**
 * An "About Dialog" for an application.
 *
 * @author Robert Futrell
 * @version 0.8
 */
public class AboutDialog extends EscapableDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JButton okButton;
	private JButton licenseButton;
	private JTabbedPane tabbedPane;
	private Component appPanel;

	private static final ResourceBundle msg =
		ResourceBundle.getBundle("org.fife.ui.AboutDialog");


	/**
	 * Creates a new <code>AboutDialog</code>.
	 *
	 * @param parent The owner of this dialog.
	 */
	public AboutDialog(Frame parent) {
		this(parent, null);
	}


	/**
	 * Creates a new <code>AboutDialog</code>.
	 *
	 * @param parent The owner of this dialog.
	 * @param title The title of the about dialog.
	 */
	public AboutDialog(Frame parent, String title) {

		super(parent);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		// Set the main content pane for the "About" dialog.
		JPanel contentPane =new ResizableFrameContentPane(new BorderLayout());
		contentPane.setBorder(UIUtil.getEmpty5Border());
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane();
		contentPane.add(tabbedPane);

		// Add a panel to the tabbed pane about the Java environment.
		JPanel temp = UIUtil.newTabbedPanePanel();
		temp = UIUtil.newTabbedPanePanel();
		temp.setLayout(new BorderLayout());
		temp.setBorder(UIUtil.getEmpty5Border());
		temp.add(new JLabel(msg.getString("EnvironInfo")),
						BorderLayout.NORTH);
		JTable table = new JTable(new PropertiesTableModel(msg)) {
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
		};
		UIUtil.fixJTableRendererOrientations(table);
		table.setTableHeader(null);
		// Make scroll pane's visible area "too small" so that the About dialog
		// will only be as large as the largest panel added by the user.
		temp.add(new RScrollPane(10,10, table));
		tabbedPane.add(msg.getString("Environment"), temp);

		JPanel aboutAppPanel = createAboutApplicationPanel();
		if (aboutAppPanel!=null) { // Should always be true.
			setApplicationPanel(aboutAppPanel);
		}

		// Add the OK and license buttons.
		okButton = UIUtil.newButton(msg, "OK", "OKButtonMnemonic");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		licenseButton = UIUtil.newButton(msg, "License",
										"LicenseButtonMnemonic");
		licenseButton.setActionCommand("License");
		licenseButton.addActionListener(this);
		Container buttons = UIUtil.createButtonFooter(okButton, licenseButton);
		contentPane.add(buttons, BorderLayout.SOUTH);

		// Put everything into a neat little package.
		getRootPane().setDefaultButton(okButton);
		setTitle(title!=null ? title : msg.getString("AboutDialogTitle"));
		setModal(true);
		applyComponentOrientation(orientation);
		pack();

		// Ensure that the first tab is the one initially shown
		// (starting in J2SE 5.0, this isn't the default behavior;
		// the last tab added is initially shown).
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tabbedPane.setSelectedIndex(0);
			}
		});

	}


	/**
	 * Called whenever an action occurs in this dialog.
	 */
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand.equals("OK")) {
			setVisible(false);
		}
		else if (actionCommand.equals("License")) {
			JDialog licenseDialog = createLicenseDialog();
			licenseDialog.setVisible(true);
		}
	}


	/**
	 * Adds a panel to the tabbed pane.
	 *
	 * @param title The title for the panel.
	 * @param panel The panel to add.
	 * @see #setApplicationPanel(Component)
	 */
	public void addPanel(String title, Component panel) {
		// Keep the "Environment" tab last.
		tabbedPane.insertTab(title, null, panel, null,
							tabbedPane.getTabCount()-1);
	}


	/**
	 * Creates the panel for the tabbed pane about this application.
	 * Subclasses want to override this method and return a panel with stuff
	 * like a logo and author information.  Alternatively, you can simply
	 * create an instance of this class, then call
	 * {@link #setApplicationPanel(Component)} on it.
	 *
	 * @return The panel.  The default implementation returns <code>null</code>.
	 * @see #setApplicationPanel(Component)
	 */
	protected JPanel createAboutApplicationPanel() {
		return null;
	}


	/**
	 * Returns the dialog that displays this application's license.  By default,
	 * a dialog displaying the modified BSD license is displayed.  Subclasses
	 * can override.
	 *
	 * @return The license dialog.
	 */
	protected JDialog createLicenseDialog() {
		return new LicenseDialog(this, getModifiedBsdText());
	}


	private static final String getModifiedBsdText() {
		String text = null;
		try {
			text = IOUtil.readFully(AboutDialog.class. // Subclasses
					getResourceAsStream("modifiedBsdLicense.txt"));
		} catch (IOException ioe) { // Never happens
			text = ioe.getMessage();
		}
		return text;
	}


	/**
	 * Sets the main "about this application" panel.
	 *
	 * @param panel The panel.
	 * @see #addPanel(String, Component)
	 */
	public void setApplicationPanel(Component panel) {
		if (appPanel!=null) { // Replacing old app panel (uncommon)
			tabbedPane.removeTabAt(0);
			tabbedPane.setComponentAt(0, panel);
			appPanel = panel;
		}
		else if (panel==null) { // Removing app panel (uncommon)
			tabbedPane.removeTabAt(0);
			appPanel = null;
		}
		else { // Adding app panel
			String title = msg.getString("Tab.Application");
			tabbedPane.insertTab(title, null, panel, null, 0);
			appPanel = panel;
		}
	}


	/**
	 * A dialog box that displays the license for the application.
	 */
	protected class LicenseDialog extends EscapableDialog
			implements ActionListener {

		private static final long serialVersionUID = 1L;

		public LicenseDialog(Dialog parent, String licenseText) {

			super(parent);
			ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());

			setTitle(msg.getString("LicenseDialogTitle"));
			JPanel cp = new ResizableFrameContentPane(new BorderLayout());
			cp.setBorder(UIUtil.getEmpty5Border());
			setContentPane(cp);

			JTextArea textArea = new JTextArea(22, 81);
			textArea.setText(licenseText);
			textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			textArea.setCaretPosition(0);
			RScrollPane scrollPane = new RScrollPane(textArea);
			cp.add(scrollPane);
			JButton okButton = UIUtil.newButton(msg, "OK",
											"OKButtonMnemonic");
			okButton.addActionListener(this);
			Container buttons = UIUtil.createButtonFooter(okButton);
			cp.add(buttons, BorderLayout.SOUTH);
			setModal(true);
			applyComponentOrientation(o);
			getRootPane().setDefaultButton(okButton);
			pack();
			setLocationRelativeTo(AboutDialog.this);

		}

		public void actionPerformed(ActionEvent e) {
			escapePressed();
		}

	}


	/**
	 * Table model displaying basic system properties.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	private static class PropertiesTableModel extends DefaultTableModel {

		public PropertiesTableModel(ResourceBundle msg) {
			super(9, 2);
			setValueAt(msg.getString("Environment.Label.JavaVendor"), 0,0);
			setValueAt(System.getProperty("java.vendor"), 0,1);
			setValueAt(msg.getString("Environment.Label.JavaVMVersion"), 1,0);
			setValueAt(System.getProperty("java.vm.version"), 1,1);
			setValueAt(msg.getString("Environment.Label.JavaVMVendor"), 2,0);
			setValueAt(System.getProperty("java.vm.vendor"), 2,1);
			setValueAt(msg.getString("Environment.Label.JavaSpecification"), 3,0);
			setValueAt(System.getProperty("java.specification.name"), 3,1);
			setValueAt(msg.getString("Environment.Label.JavaSpecificationVersion"), 4,0);
			setValueAt(System.getProperty("java.specification.version"), 4,1);
			setValueAt(msg.getString("Environment.Label.Classpath"), 5,0);
			setValueAt(System.getProperty("java.class.path"), 5,1);
			setValueAt(msg.getString("Environment.Label.OS"), 6,0);
			setValueAt(System.getProperty("os.name"), 6,1);
			setValueAt(msg.getString("Environment.Label.OSVersion"), 7,0);
			setValueAt(System.getProperty("os.version"), 7,1);
			setValueAt(msg.getString("Environment.Label.Architecture"), 8,0);
			setValueAt(System.getProperty("os.arch"), 8,1);
			setColumnIdentifiers(new String[] { "", "" });
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

	}


}