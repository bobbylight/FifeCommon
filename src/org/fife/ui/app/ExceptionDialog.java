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
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.fife.ui.EscapableDialog;
import org.fife.ui.RButton;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.RScrollPane;
import org.fife.ui.SelectableLabel;
import org.fife.ui.UIUtil;


/**
 * The dialog displayed to the user when an <code>Exception</code> is
 * caught.
 *
 * @author Robert Futrell
 * @version 0.3
 */
public class ExceptionDialog extends EscapableDialog implements ActionListener {

	private SelectableLabel descArea;
	private DetailsButton detailsButton;
	private JPanel textPanel;
	private String desc;

	private static final int MIN_HEIGHT				= 150;
	private static final int MAX_WIDTH				= 600;

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

		JPanel topPanel = new JPanel(new BorderLayout());
		Icon icon = UIManager.getIcon("OptionPane.errorIcon");
		if (icon!=null) {
			JLabel iconLabel = new JLabel(icon);
			topPanel.add(iconLabel, BorderLayout.LINE_START);
		}
		descArea = new SelectableLabel();
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
		topPanel.add(temp2);
		detailsButton = new DetailsButton(msg.getString("Details"));
		detailsButton.setActionCommand("ToggleDetails");
		detailsButton.addActionListener(this);
		topPanel.setBorder(UIUtil.getEmpty5Border());
		contentPane.add(topPanel, BorderLayout.NORTH);

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

		JPanel buttonPanel = new JPanel(new GridLayout(1,2, 5,5));
		RButton okButton = new RButton(msg.getString("Close"));
		okButton.setMnemonic(msg.getString("CloseMnemonic").charAt(0));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonPanel.add(okButton);
		buttonPanel.add(detailsButton);
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(buttonPanel);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);

		setContentPane(contentPane);
		getRootPane().setDefaultButton(okButton);
		applyComponentOrientation(orientation);
		pack();
		setModal(true);

	}


	/**
	 * Workaround for the fact that, if our SelectableLabel (e.g. JEditorPane)
	 * is displaying a long line of HTML, the preferred size returned will be
	 * way too long.  Make us a reasonable width and guess at a height that
	 * will likely hold all of the text, while still not looking way too tall.
	 */
	public void pack() {

		super.pack();

System.out.println("*** " + getWidth() + ", " + getPreferredSize().width);
		if (getWidth()>MAX_WIDTH && getHeight()<200) {
System.out.println("Here!!!");
			setSize(MAX_WIDTH, 200);
		}

		// Keep one-liner errors from creating a dialog that is "too thin."
		if (getHeight()<MIN_HEIGHT) {
			setSize(getWidth(), MIN_HEIGHT);
		}

	}


	/**
	 * Sets the short description of the error.
	 *
	 * @param desc A short description of the error.  This cannot be
	 *        <code>null</code>.
	 */
	public void setDescription(String desc) {
		descArea.setText(desc);
		// NOTE: Why must we reset cp's preferred size to keep pack()
		// actually working here?
		// Need cast for setPreferredSize() to work in 1.4.
		JPanel cp = (JPanel)getContentPane(); // Okay cast since we made it
		cp.setPreferredSize(null);
		pack(); // Resize for new message
	}


	public void setVisible(final boolean visible) {
		if (visible) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ExceptionDialog.super.setVisible(visible);
				}
			});
		}
		else {
			super.setVisible(visible);
		}
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

		private static final int WIDTH		= 10;
		private static final int HEIGHT		= 10;

		public ArrowIcon() {
			x = new int[] { WIDTH/2, 0, WIDTH };
			y = new int[] { 0, HEIGHT, HEIGHT };
			x2 = new int[3];
			y2 = new int[3];
		}

		public int getIconWidth() {
			return WIDTH;
		}

		public int getIconHeight() {
			return HEIGHT;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D)g;
			Object old = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_ON);
			Color fg = UIManager.getColor("Button.foreground");
			if (fg==null) { // Not guaranteed to be set in UIDefaults
				fg = Color.black;
			}
			g2d.setColor(fg);
			for (int i=0; i<3; i++) {
				x2[i] = this.x[i] + x;
				y2[i] = this.y[i] + y;
			}
			g2d.fillPolygon(x2,y2, 3);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, old);
		}

		public void toggle() {
			if (x[0]==(getIconWidth())/2) {
				x[0] = 0; x[1] = getIconWidth(); x[2] = getIconWidth()/2;
				y[0] = 0; y[1] = 0; y[2] = getIconHeight();
			}
			else {
				x[0] = getIconWidth()/2; x[1] = 0; x[2] = getIconWidth();
				y[0] = 0; y[1] = getIconHeight(); y[2] = y[1];
			}
		}

	}


}