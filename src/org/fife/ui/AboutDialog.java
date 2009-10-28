/*
 * 09/02/2004
 *
 * AboutDialog.java - An About dialog for an application.
 * Copyright (C) 2003 Robert Futrell
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
package org.fife.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;


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

		ResourceBundle msg= ResourceBundle.getBundle("org.fife.ui.AboutDialog");

		// Set the main content pane for the "About" dialog.
		JPanel contentPane =new ResizableFrameContentPane(new BorderLayout());
		contentPane.setBorder(UIUtil.getEmpty5Border());
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane();
		contentPane.add(tabbedPane);

		// Add a panel to the tabbed pane about the Java environment.
		JPanel temp = UIUtil.createTabbedPanePanel();
		temp = UIUtil.createTabbedPanePanel();
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
		JPanel buttonPanel = new JPanel();
		temp = new JPanel(new GridLayout(1,2, 5,0));
		okButton = UIUtil.createRButton(msg, "OK", "OKButtonMnemonic");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		temp.add(okButton);
		licenseButton = UIUtil.createRButton(msg, "License",
										"LicenseButtonMnemonic");
		licenseButton.setActionCommand("License");
		licenseButton.addActionListener(this);
		temp.add(licenseButton);
		buttonPanel.add(temp);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

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
			ResourceBundle msg = ResourceBundle.
									getBundle("org.fife.ui.AboutDialog");
			GPLLicenseDialog licenseDialog = new GPLLicenseDialog(msg);
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
			ResourceBundle msg = ResourceBundle.
								getBundle("org.fife.ui.AboutDialog");
			String title = msg.getString("Tab.Application");
			tabbedPane.insertTab(title, null, panel, null, 0);
			appPanel = panel;
		}
	}


	/**
	 * A dialog box that displays the GNU GPL.
	 */
	class GPLLicenseDialog extends EscapableDialog implements ActionListener {

		private static final long serialVersionUID = 1L;

		GPLLicenseDialog(ResourceBundle msg) {

			ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());

			setTitle(msg.getString("LicenseDialogTitle"));
			JPanel cp = new ResizableFrameContentPane(new BorderLayout());
			cp.setBorder(UIUtil.getEmpty5Border());
			setContentPane(cp);

			JTextArea textArea = new JTextArea(15, 60);
			textArea.setText(
				"This program is free software; you can redistribute it and/or modify " +
				"it under the terms of the GNU General Public License as published by " +
				"the Free Software Foundation; either version 2 of the License, or " +
				"(at your option) any later version.\n\n" +
				"This program is distributed in the hope that it will be useful, " +
				"but WITHOUT ANY WARRANTY; without even the implied warranty of " +
				"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the " +
				"GNU General Public License for more details.\n\n" +
				"You should have received a copy of the GNU General Public License " +
				"along with this program; if not, write to the Free Software " +
				"Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA");
			textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			textArea.setCaretPosition(0);
			RScrollPane scrollPane = new RScrollPane(textArea);
			cp.add(scrollPane);
			JPanel buttonPanel = new JPanel();
			RButton okButton = UIUtil.createRButton(msg, "OK",
											"OKButtonMnemonic");
			okButton.addActionListener(this);
			buttonPanel.add(okButton);
			cp.add(buttonPanel, BorderLayout.SOUTH);
			setModal(true);
			applyComponentOrientation(o);
			getRootPane().setDefaultButton(okButton);
			pack();
			setLocationRelativeTo(AboutDialog.this);

		}

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
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

		public boolean isCellEditable(int row, int col) {
			return false;
		}

	}


}