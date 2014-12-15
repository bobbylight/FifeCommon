/*
 * 01/13/2010
 *
 * GetKeyStrokeDialog.java - A dialog that allows the user to edit a
 * key stroke.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;


/**
 * A dialog for editing a key stroke.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class GetKeyStrokeDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private KeyStroke stroke;
	private KeyStrokeField textField;
	private boolean canceled;

	private static final String MSG = "org.fife.ui.GetKeyStrokeDialog";
	private static final ResourceBundle msg = ResourceBundle.getBundle(MSG);


	/**
	 * Constructor.
	 *
	 * @param parent The parent dialog of this dialog.
	 * @param initial The initial key stroke to display.
	 */
	public GetKeyStrokeDialog(Dialog parent, KeyStroke initial) {
		super(parent, msg.getString("Dialog.KeyStroke.Title"));
		createUI(initial);
	}
	
	
	/**
	 * Constructor.
	 *
	 * @param parent The parent frame of this dialog.
	 * @param initial The initial key stroke to display.
	 */
	public GetKeyStrokeDialog(Frame parent, KeyStroke initial) {
		super(parent, msg.getString("Dialog.KeyStroke.Title"));
		createUI(initial);
	}


	/**
	 * Creates the contents of this dialog.
	 */
	private void createUI(KeyStroke initial) {

		ComponentOrientation orientation = ComponentOrientation.
								getOrientation(getLocale());
		Listener listener = new Listener();
		Box contentPane = Box.createVerticalBox();
		contentPane.setBorder(UIUtil.getEmpty5Border());

		JPanel temp = new JPanel(new BorderLayout());
		JLabel prompt = UIUtil.newLabel(msg, "Dialog.KeyStroke.Prompt");
		temp.add(prompt, BorderLayout.LINE_START);
		contentPane.add(temp);
		contentPane.add(Box.createVerticalStrut(8));

		textField = new KeyStrokeField();
		JLabel charLabel=UIUtil.newLabel(msg, "Dialog.KeyStroke.Key",textField);
		temp = new JPanel(new BorderLayout());
		temp.add(charLabel, BorderLayout.LINE_START);
		temp.add(textField);
		contentPane.add(temp);

		JButton ok = UIUtil.newButton(msg, "OK", listener);
		ok.setActionCommand("OK");
		JButton cancel = UIUtil.newButton(msg, "Cancel", listener);
		cancel.setActionCommand("Cancel");
		JPanel buttonPanel = (JPanel)UIUtil.createButtonFooter(ok, cancel);
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 5, 0),
				buttonPanel.getBorder()));

		JPanel realCP = new ResizableFrameContentPane(new BorderLayout());
		realCP.add(contentPane, BorderLayout.NORTH);
		realCP.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(realCP);
		setKeyStroke(initial);
		setModal(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		applyComponentOrientation(orientation);
		pack();

	}


	/**
	 * Whether the dialog was canceled.
	 *
	 * @return Whether the dialog was canceled.
	 * @see #getKeyStroke()
	 */
	public boolean getCancelled() {
		return canceled;
	}


	/**
	 * Returns the key stroke the user entered.
	 *
	 * @return The key stroke, or <code>null</code> if the user canceled the
	 *         dialog.
	 * @see #setKeyStroke(KeyStroke)
	 */
	public KeyStroke getKeyStroke() {
		return stroke;
	}

	/**
	 * Sets the key stroke displayed in this dialog.
	 *
	 * @param stroke The key stroke.
	 * @see #getKeyStroke()
	 */
	public void setKeyStroke(KeyStroke stroke) {
		this.stroke = stroke;
		textField.setKeyStroke(stroke);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			canceled = true; // Default to canceled.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					textField.requestFocusInWindow();
					textField.selectAll();
				}
			});
		}
		super.setVisible(visible);
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if ("OK".equals(command)) {
				stroke = textField.getKeyStroke();
				canceled = false;
				setVisible(false);
			}
			else if ("Cancel".equals(command)) {
				stroke = null;
				setVisible(false);
			}
		}

	}


}