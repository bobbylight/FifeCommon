/*
 * 12/10/2004
 *
 * RDirectoryChooser.java - A dialog allowing the user to select a single
 * directory on their system.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.fife.ui.EscapableDialog;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.RScrollPane;
import org.fife.ui.UIUtil;


/**
 * A directory chooser component.  This component is preferable to using
 * either <code>RTextFileChooser</code> or <code>JFileChooser</code>
 * in directory-mode because that design simply isn't intuitive.<p>
 *
 * An <code>RDirectoryChooser</code> is simply a tree representing
 * all directories in the user's file system.  System icons are placed
 * beside directory names if available (e.g. Windows directory choosers).<p>
 *
 * NOTE:  If you experience long load times when expanding a directory
 * node for a network directory, it's not my fault, I swear; Java's
 * file I/O performance over a network (especially for calls such as
 * <code>File.isDirectory()</code>) is horrendous.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class RDirectoryChooser extends EscapableDialog {

	private JButton okButton;
	private JButton cancelButton;
	private DirectoryTree directoryTree;

	private String chosenDirectory;


	/**
	 * Constructor.
	 */
	public RDirectoryChooser() {
		this((Frame)null);
	}


	/**
	 * Constructor.
	 *
	 * @param parent The dialog that owns this directory chooser.
	 */
	public RDirectoryChooser(Dialog parent) {
		this(parent, null);
	}


	/**
	 * Constructor.
	 *
	 * @param parent The window that owns this directory chooser.
	 */
	public RDirectoryChooser(Frame parent) {
		this(parent, null);
	}


	/**
	 * Constructor.
	 *
	 * @param parent The dialog that owns this directory chooser.
	 * @param title The title for this directory chooser.  If
	 *        <code>null</code>, a default title will be used.
	 */
	public RDirectoryChooser(Dialog parent, String title) {
		super(parent);
		init(parent, title);
	}


	/**
	 * Constructor.
	 *
	 * @param parent The window that owns this directory chooser.
	 * @param title The title for this directory chooser.
	 *        If <code>null</code>, a default title will be used.
	 */
	public RDirectoryChooser(Frame parent, String title) {
		super(parent);
		init(parent, title);
	}


	/**
	 * Returns the directory chosen by the user.
	 *
	 * @return The chosen directory.  If the user canceled the dialog, then
	 *         <code>null</code> is returned.
	 * @see #setChosenDirectory(File)
	 */
	public String getChosenDirectory() {
		return chosenDirectory;
	}


	@Override
	public void escapePressed() {
		chosenDirectory = null;
		super.escapePressed();
	}


	/**
	 * Initializes this directory chooser.  This is here because JDialog's
	 * parent must be either a Frame or a Dialog, and not a Window; thus,
	 * we need a separate constructor for each of these cases...
	 * very unfortunate...
	 *
	 * @param parent The window that owns this directory chooser.
	 * @param title The title for this directory chooser.
	 */
	private void init(Window parent, String title) {

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		Listener listener = new Listener();

		// Get our localized messages.
		ResourceBundle msg = ResourceBundle.getBundle(
						"org.fife.ui.rtextfilechooser.DirectoryChooser");

		// The panel that will contain everything.
		JPanel contentPane =new ResizableFrameContentPane(new BorderLayout());
		contentPane.setBorder(UIUtil.getEmpty5Border());

		// Add a panel with the directory tree.
		JPanel treePanel = new JPanel(new GridLayout(1,1));
		directoryTree = new DirectoryTree();
		directoryTree.getSelectionModel().addTreeSelectionListener(listener);
		directoryTree.addPropertyChangeListener(listener);
		RScrollPane scrollPane = new RScrollPane(directoryTree);
		scrollPane.setHorizontalScrollBarPolicy(
							RScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(
							RScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		treePanel.add(scrollPane);
		contentPane.add(treePanel);

		// Add a panel with the OK and Cancel buttons.
		okButton = UIUtil.newButton(msg, "OK", "OKMnemonic");
		okButton.setEnabled(false);
		okButton.setActionCommand("OK");
		okButton.addActionListener(listener);
		cancelButton = UIUtil.newButton(msg, "Cancel", "CancelMnemonic");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(listener);
		Container buttons = UIUtil.createButtonFooter(okButton, cancelButton);
		contentPane.add(buttons, BorderLayout.SOUTH);

		// Get ready to go!
		setContentPane(contentPane);
		setTitle(title==null ? msg.getString("DefaultTitle") : title);
		applyComponentOrientation(orientation);
		pack();
		setModal(true);
		setLocationRelativeTo(parent);

	}


	/**
	 * Selects the specified directory, if it exists.  Otherwise, the
	 * selection is cleared.
	 *
	 * @param dir The directory to select.
	 * @return Whether the directory exists and was selected.
	 * @see #getChosenDirectory()
	 */
	public boolean setChosenDirectory(File dir) {
		return directoryTree.setSelectedFile(dir);
	}


	/**
	 * Listens for all events in this directory chooser.
	 */
	private class Listener implements ActionListener, TreeSelectionListener,
							PropertyChangeListener {

		public Listener() {
		}

		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();
			if (actionCommand.equals("OK")) {
				chosenDirectory = directoryTree.getSelectedFileName();
				setVisible(false);
			}
			else if (actionCommand.equals("Cancel")) {
				escapePressed();
			}
		}

		public void propertyChange(PropertyChangeEvent e) {
			String property = e.getPropertyName();
			if (property.equals(DirectoryTree.WILL_EXPAND_PROPERTY)) {
				RDirectoryChooser.this.setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
			else if (property.equals(DirectoryTree.EXPANDED_PROPERTY)) {
				RDirectoryChooser.this.setCursor(
					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		public void valueChanged(TreeSelectionEvent e) {
			okButton.setEnabled(e.getNewLeadSelectionPath()!=null);
		}

	}


}