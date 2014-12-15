/*
 * 11/14/2003
 *
 * TopicsFoundDialog.java - Dialog used by HelpDialog.  Allows the user to
 * choose exactly one of multiple choices.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.help;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.Box;

import org.fife.ui.RListSelectionModel;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.RScrollPane;
import org.fife.ui.UIUtil;


/**
 * Used by an instance of <code>org.fife.help.HelpDialog</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TopicsFoundDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JList choicesList;
	private int selectedIndex = -1;

	private static final String MSG = "org.fife.help.TopicsFoundDialog";


	/**
	 * Creates a new <code>TopicsFoundDialog</code>.
	 *
	 * @param owner The HelpDialog that spawns this TopicsFoundDialog.
	 * @param choices An array of nodes to use as the choices (the "topics
	 *        found").
	 */
	TopicsFoundDialog(JFrame owner, List<HelpTreeNode> choices) {

		// Call parent's constructor and set the dialog's title.
		super(owner);

		ResourceBundle msg = ResourceBundle.getBundle(MSG);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		// Create a label for the dialog.
		JLabel instruction = new JLabel(msg.getString("Instructions"));
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
		labelPanel.add(instruction);
		labelPanel.add(Box.createHorizontalGlue());
		labelPanel.setBorder(UIUtil.getEmpty5Border());

		TopicsFoundListener listener = new TopicsFoundListener();

		// Create a panel for the buttons.
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		JButton button = new JButton(msg.getString("Display"));
		button.setActionCommand("Display");
		button.addActionListener(listener);
		buttonPanel.add(button);
		buttonPanel.add(Box.createHorizontalStrut(3));
		button = new JButton(msg.getString("Cancel"));
		button.setActionCommand("Cancel");
		button.addActionListener(listener);
		buttonPanel.add(button);
		buttonPanel.setBorder(UIUtil.getEmpty5Border());

		// Create the list of choices.
		choicesList = new JList(choices.toArray());
		choicesList.addMouseListener(listener);
		choicesList.addKeyListener(listener);
		choicesList.setSelectionModel(new RListSelectionModel());
		choicesList.setSelectedIndex(0);
		RScrollPane sp = new RScrollPane(choicesList);
		sp.setVerticalScrollBarPolicy(RScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel choicesPanel = new JPanel(new BorderLayout());
		choicesPanel.add(sp);
		choicesPanel.setBorder(UIUtil.getEmpty5Border());

		// Arrange the dialog!
		JPanel temp = new ResizableFrameContentPane(new BorderLayout());
		temp.add(labelPanel, BorderLayout.NORTH);
		temp.add(choicesPanel, BorderLayout.CENTER);
		temp.add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(temp);
		setLocationRelativeTo(owner);
		setModal(true);
		setTitle(msg.getString("Title"));
		applyComponentOrientation(orientation);
		pack();

	}


	/**
	 * Returns the index the user selected, or <code>-1</code> if they
	 * canceled the dialog.
	 *
	 * @return The index they selected, or <code>-1</code> if they canceled
	 *         the dialog.
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class TopicsFoundListener extends MouseAdapter
						implements ActionListener, KeyListener {

		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();
			if (actionCommand.equals("Display")) {
				selectedIndex = choicesList.getSelectedIndex();
				setVisible(false);
			}
			else if (actionCommand.equals("Cancel")) {
				selectedIndex = -1;
				setVisible(false);
			}
		}

		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					selectedIndex = choicesList.getSelectedIndex();
					setVisible(false);
					break;
				case KeyEvent.VK_ESCAPE:
					selectedIndex = -1;
					setVisible(false);
					break;
			}
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount()==2) {
				selectedIndex = choicesList.getSelectedIndex();
				setVisible(false);
			}
		}

	}


}