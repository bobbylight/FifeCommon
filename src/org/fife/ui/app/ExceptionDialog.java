/*
 * 12/28/2004
 *
 * ExceptionDialog.java - A dialog for displaying program Exceptions to the
 * user.
 * Copyright (C) 2004 Robert Futrell
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
package org.fife.ui.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.fife.ui.RButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.RScrollPane;
import org.fife.ui.UIUtil;


/**
 * The dialog displayed to the user when an <code>Exception</code> is
 * caught.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class ExceptionDialog extends JDialog implements ActionListener {

	private JEditorPane descArea;
	private DetailsButton detailsButton;
	private JPanel textPanel;
	private String desc;

	// Don't use a tab as tab==8 space by default => too much space!
	private static final String TRACE_STEP_BEGINNING	= "    at ";

	private static final String MSG = "org.fife.ui.app.ExceptionDialog";


	/**
	 * Creates an exception dialog for an unexpected exception.
	 *
	 * @param owner The parent window.
	 * @param t The exception that was thrown.
	 */
	public ExceptionDialog(Frame owner, Throwable t) {
		super(owner);
		init(t);
	}


	/**
	 * Creates an exception dialog for an unexpected exception.
	 *
	 * @param owner The parent dialog.
	 * @param t The exception that was thrown.
	 */
	public ExceptionDialog(Dialog owner, Throwable t) {
		super(owner);
		init(t);
	}


	/**
	 * Called when a widget is used in this dialog.
	 *
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if ("ToggleDetails".equals(command)) {
			detailsButton.getArrowIcon().toggle();
			detailsButton.repaint();
			// Need cast for setPreferredSize() to work in 1.4.
			JPanel cp = (JPanel)getContentPane(); // Okay cast - we made it
			Dimension d = null;
			if (textPanel.getParent()==null) {
				cp.add(textPanel);
				d = new Dimension(cp.getSize().width, 350);
			}
			else {
				cp.remove(textPanel);
				int w = cp.getSize().width;
				cp.setPreferredSize(null); // Reset preferred size to parent
				d = new Dimension(w, cp.getPreferredSize().height);
			}
			cp.setPreferredSize(d);
			pack();
		}

	}


	/**
	 * Returns an editor pane to use for the description text.
	 *
	 * @return The editor pane.
	 */
	private JEditorPane createDescArea() {
		JEditorPane descArea = new JEditorPane();
//		descArea.setContentType("text/html");
		descArea.setBorder(null);
		descArea.setEditable(false);
		descArea.setOpaque(false);
		descArea.setBackground(new Color(0, 0, 0, 0)); // Needed for Nimbus
		descArea.setFont(UIManager.getFont("Label.font"));
		return descArea;
	}


	/**
	 * Returns the text component to use to display the exception.  By
	 * default, this method returns an instance of <code>JTextArea</code>.
	 * You can override this method to return a different type of text
	 * component (such as an instance of
	 * <code>org.fife.ui.rsyntaxtextarea.RSyntaxTextArea</code> set to syntax
	 * highlight Java exceptions).
	 *
	 * @return The text component.
	 */
	protected JTextComponent createTextComponent() {
		return new JTextArea(12, 25);
	}


	/**
	 * Returns a string representing the stack trace for this throwable.
	 *
	 * @param t The throwable.
	 * @return The stack trace.
	 */
	private static final String getStackTraceText(Throwable t) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(t.toString()).append("\n");
		StackTraceElement[] ste = t.getStackTrace();
		int count = ste.length;
		for (int i=0; i<count; i++) {
			buffer.append(TRACE_STEP_BEGINNING).append(ste[i].toString()).
													append("\n");
		}
		return buffer.toString();
	}


	/**
	 * Initializes the dialog.
	 *
	 * @param t The <code>Throwable</code> to display.
	 */
	private void init(Throwable t) {

		ResourceBundle msg = ResourceBundle.getBundle(MSG);

		ComponentOrientation orientation = ComponentOrientation.
								getOrientation(Locale.getDefault());
		setTitle(msg.getString("Title"));

		JPanel contentPane =new ResizableFrameContentPane(new BorderLayout());

		JPanel temp = new JPanel(new BorderLayout());
		Icon icon = UIManager.getIcon("OptionPane.errorIcon");
		if (icon!=null) {
			JLabel iconLabel = new JLabel(icon);
			temp.add(iconLabel, BorderLayout.LINE_START);
		}
		descArea = createDescArea();
		String descFormat = msg.getString("DescriptionFormat");
		desc = t.getMessage();
		if (desc==null) {
			desc = t.toString();
		}
		desc = MessageFormat.format(descFormat, new String[] { desc });
		setDescription(desc);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		temp2.add(descArea);
		temp.add(temp2);
		detailsButton = new DetailsButton(msg.getString("Details"));
		detailsButton.setActionCommand("ToggleDetails");
		detailsButton.addActionListener(this);
		temp2 = new JPanel(new BorderLayout());
		temp2.add(detailsButton, BorderLayout.SOUTH);
		temp.add(temp2, BorderLayout.LINE_END);
		temp.setBorder(UIUtil.getEmpty5Border());
		contentPane.add(temp, BorderLayout.NORTH);

		JTextComponent textArea = createTextComponent();
		String stackTraceText = getStackTraceText(t);
		while (t.getCause()!=null) {
			t = t.getCause();
			stackTraceText += "Caused by: " + getStackTraceText(t);
		}
		try {
			textArea.setText(stackTraceText);
		} catch (Throwable t2) {
			// I'm paranoid about custom text areas (such as RSyntaxTextArea)
			// trying to do stuff like syntax highlight the stack trace and
			// failing during the attempt...
			// Can't call createTextComponent as it was likely overridden.
			textArea = new JTextArea(15, 50);
			textArea.setText(stackTraceText);
		}
		textArea.setCaretPosition(0);
		textArea.setEditable(false);
		textPanel = new JPanel(new BorderLayout());
		textPanel.setBorder(UIUtil.getEmpty5Border());
		textPanel.add(new RScrollPane(textArea));
		//contentPane.add(textPanel);

		JPanel buttonPanel = new JPanel();
		RButton okButton = new RButton(msg.getString("Close"));
		okButton.setMnemonic(msg.getString("CloseMnemonic").charAt(0));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonPanel.add(okButton);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(contentPane);
		getRootPane().setDefaultButton(okButton);
		applyComponentOrientation(orientation);
		contentPane.setPreferredSize(
			new Dimension(400, contentPane.getPreferredSize().height));
		pack();
		setModal(true);

	}


	/**
	 * Sets the short description of the error.
	 *
	 * @param desc A short description of the error.  This cannot be
	 *        <code>null</code>.
	 */
	public void setDescription(String desc) {
		this.desc = desc;
		if (desc!=null && desc.startsWith("<html>")) {
			descArea.setContentType("text/html");
		}
		else {
			descArea.setContentType("text/plain");
		}
		descArea.setText(desc);
		// NOTE: Why must we reset cp's preferred size to keep pack()
		// actually working here?
		// Need cast for setPreferredSize() to work in 1.4.
		JPanel cp = (JPanel)getContentPane(); // Okay cast since we made it
		cp.setPreferredSize(null);
		pack(); // Resize for new message
	}


	private static class DetailsButton extends JButton {

		public DetailsButton(String text) {
			setText(text);
			setIcon(new ArrowIcon());
		}

		public ArrowIcon getArrowIcon() {
			return (ArrowIcon)getIcon();
		}

	}


	private static class ArrowIcon implements Icon {

		private int[] x;
		private int[] y;
		private int[] x2;
		private int[] y2;

		public ArrowIcon() {
			x = new int[] { 6, 0, 11 };
			y = new int[] { 0, 11, 11 };
			x2 = new int[3];
			y2 = new int[3];
		}

		public int getIconWidth() {
			return 12;
		}

		public int getIconHeight() {
			return 12;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(UIManager.getColor("Button.foreground"));
			for (int i=0; i<3; i++) {
				x2[i] = this.x[i] + x;
				y2[i] = this.y[i] + y;
			}
			g.fillPolygon(x2,y2, 3);
		}

		public void toggle() {
			if (x[0]==6) {
				x[0] = 0; x[1] = 11; x[2] = 6;
				y[0] = 0; y[1] = 0; y[2] = 11;
			}
			else {
				x[0] = 6; x[1] = 0; x[2] = 11;
				y[0] = 0; y[1] = 11; y[2] = 11;
			}
		}

	}


}