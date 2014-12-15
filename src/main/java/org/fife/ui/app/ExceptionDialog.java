/*
 * 12/28/2004
 *
 * ExceptionDialog.java - A dialog for displaying program Exceptions to the
 * user.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
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
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.fife.ui.EscapableDialog;
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
			detailsButton.toggleCollapsed();
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
		StringBuilder buffer = new StringBuilder();
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
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

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
		desc = MessageFormat.format(descFormat, desc);
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

		JButton okButton = new JButton(msg.getString("Close"));
		okButton.setMnemonic(msg.getString("CloseMnemonic").charAt(0));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		Container buttons = UIUtil.createButtonFooter(okButton, detailsButton);
		contentPane.add(buttons, BorderLayout.SOUTH);

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
	@Override
	public void pack() {

		super.pack();

		if (getWidth()>MAX_WIDTH && getHeight()<200) {
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
		// Force wrapping HTML at word boundaries:
		descArea.firePropertyChange("wrapStyleWord", false, true);
		// NOTE: Why must we reset cp's preferred size to keep pack()
		// actually working here?
		// Need cast for setPreferredSize() to work in 1.4.
		JPanel cp = (JPanel)getContentPane(); // Okay cast since we made it
		cp.setPreferredSize(null);
		pack(); // Resize for new message
	}


	private static class DetailsButton extends JButton {

		private String mainText;
		private boolean collapsed;

		public DetailsButton(String text) {
			mainText = text;
			setCollapsed(true);
		}

		public void setCollapsed(boolean collapsed) {
			this.collapsed = collapsed;
			String text = null;
			ComponentOrientation o = getComponentOrientation();
			if (collapsed) {
				text = o.isLeftToRight() ? (mainText + " >>") :
											(mainText + " <<");
			}
			else {
				text = o.isLeftToRight() ? (mainText + " <<") :
					(mainText + " >>");
			}
			setText(text);
		}

		public void toggleCollapsed() {
			setCollapsed(!collapsed);
		}

	}


}