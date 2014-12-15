/*
 * 07/26/2004
 *
 * StatusBar.java - A generic status bar containing a status message and an
 * optional size grip, similar to those found in Microsoft Windows applications.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;


/**
 * A generic status bar containing a status message and a size grip,
 * similar to those found in Microsoft Windows applications.<p>
 * This component can be used as-is, or can be subclassed and have additional
 * components added to it.  If the latter, the user MUST add instances of
 * {@link StatusBarPanel} and they MUST be added via the
 * <code>addStatusBarComponent</code> methods; if you instead use one of
 * <code>Container</code>'s <code>add</code> methods, there is no guarantee as
 * to the flow/layout of the status bar.
 *
 * @author Robert Futrell
 * @version 0.7
 */
public class StatusBar extends StatusBarPanel implements MouseListener {

	private static final long serialVersionUID = 1L;

	public static final String STYLE_PROPERTY		= "StatusBar.style";

	public static final int WINDOWS_98_STYLE		= 0;
	public static final int WINDOWS_XP_STYLE		= 1;
	private static final int MIN_STYLE_CONSTANT		= 0;
	private static final int MAX_STYLE_CONSTANT		= 1;

	public static final String DEFAULT_STATUS_MESSAGE = "Ready";

	private JLabel statusMessage;
	private String defaultMessage;
	private GridBagLayout gridBag;
	private SizeGrip sizeGrip;


	/**
	 * Creates the status bar with a default status message and a size grip.
	 */
	public StatusBar() {
		this(DEFAULT_STATUS_MESSAGE);
	}


	/**
	 * Creates the status bar with the specified default message and a size
	 * grip.
	 *
	 * @param defaultStatusMessage The default message to display.
	 */
	public StatusBar(String defaultStatusMessage) {
		this(defaultStatusMessage, WINDOWS_XP_STYLE);
	}


	/**
	 * Creates the status bar.
	 *
	 * @param defaultStatusMessage The default message to display.
	 * @param style The style with which to paint the status bar.
	 */
	public StatusBar(String defaultStatusMessage, int style) {

		// Initialize private variables.
		defaultMessage = defaultStatusMessage;
		statusMessage = new JLabel(defaultStatusMessage);

		// Make the layout such that different items can be different sizes.
		gridBag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridBag);
		c.fill = GridBagConstraints.BOTH;

		// Create and add a panel containing the "status message."
		// The message panel uses GridLayout to keep the message left-aligned.
		c.weightx = 1.0;
		JPanel status = new StatusBarPanel(new GridLayout(1,1,8,8),
									statusMessage);
		gridBag.setConstraints(status, c);
		add(status);

		// Add the size grip.
		this.sizeGrip = new SizeGrip();
		c.weightx = 0.0;
		gridBag.setConstraints(this.sizeGrip, c);
		add(this.sizeGrip);

		setStyle(style);
		setBorder(getStatusBarBorder());

	}


	/**
	 * Adds a component to this status bar.
	 *
	 * @param panel The panel to add.
	 * @param index The index at which to place this component.  A value of
	 *        <code>0</code> means "right after the status message."
	 * @param constraints The constraints with which to add the panel.
	 */
	public void addStatusBarComponent(StatusBarPanel panel,
								int index,
								GridBagConstraints constraints) {
		gridBag.setConstraints(panel, constraints);
		panel.setBorder(getStatusBarComponentBorder());
		index++; // As we're 0-based, but 0==status message panel.
		if (index<1)
			index = 1;
		else if (index>getComponentCount()-fromRight())
			index = getComponentCount() - fromRight();
		add(panel, index);
		revalidate(); // Get the new component to display.
	}


	/**
	 * Adds a component to the end of this status bar (but before the size
	 * grip).  This is the preferred method of adding components.
	 *
	 * @param panel The panel to add.
	 * @param constraints The constraints with which to add the panel.
	 */
	public void addStatusBarComponent(StatusBarPanel panel,
								GridBagConstraints constraints) {
		// Put in front of the size grip.
		int index = getComponentCount() - fromRight();
		addStatusBarComponent(panel, index, constraints);
	}


	/**
	 * Overridden to ensure that the slight border between the status
	 * text and the edge of the status bar is on the correct side of
	 * the status bar, depending on whether we're LTR or RTL.
	 *
	 * @param o The new orientation.
	 */
	@Override
	public void applyComponentOrientation(ComponentOrientation o) {
		super.applyComponentOrientation(o);
		setBorder(getStatusBarBorder());
	}


	/**
	 * Determines how many panels "over" from the right to add a new panel.
	 * This method is here because the XP-style uses a "spacer" filler panel
	 * to have space between the size grip and the line/column indicator, so
	 * for different styles, panels must be added/removed in different places.
	 *
	 * @return The number of panels on the right of the status bar that must
	 *         stay on the right.
	 */
	private final int fromRight() {
		if (getStyle()==WINDOWS_XP_STYLE)
			return 2;
		return 1;
	}


	/**
	 * Returns the default message displayed by this status bar.
	 *
	 * @return The default message.
	 */
	public String getDefaultStatusMessage() {
		return defaultMessage;
	}


	/**
	 * Returns a border for the status bar.
	 *
	 * @return The border.
	 */
	private Border getStatusBarBorder() {
		// Gives a little buffer between the status text and the edge of
		// the status bar.
		if (getComponentOrientation().isLeftToRight()) {
			return BorderFactory.createEmptyBorder(0,5,0,0);
		}
		return BorderFactory.createEmptyBorder(0,0,0,5);
	}


	/**
	 * Returns a border suitable for most status bar components.
	 *
	 * @return The border.
	 */
	private Border getStatusBarComponentBorder() {

		switch (getStyle()) {

			case WINDOWS_98_STYLE:
				return BorderFactory.createCompoundBorder(
					BorderFactory.createBevelBorder(BevelBorder.LOWERED),
					BorderFactory.createEmptyBorder(0,3,0,3));

			case WINDOWS_XP_STYLE:
				return BorderFactory.createCompoundBorder(
					new BevelDividerBorder(BevelDividerBorder.LEFT, 2),
					BorderFactory.createEmptyBorder(0,3,0,3));

			default:
				return null;

		}

	}


	/**
	 * Returns the message in the status bar.
	 *
	 * @return The message in the status bar.
	 * @see #setStatusMessage
	 */
	public String getStatusMessage() {
		return statusMessage.getText();
	}


	/**
	 * Returns the "style" used to paint this status bar.
	 *
	 * @return The style of this status bar, such as
	 *         <code>WINDOWS_XP_STYLE</code>.
	 * @see #setStyle
	 */
	public int getStyle() {
		return sizeGrip.getSizeGripIcon().getStyle();
	}


	/**
	 * Called whenever the mouse is clicked in a component listened to by this
	 * status bar.  This method should not be overridden.
	 *
	 * @param e The mouse event.
	 */
	public void mouseClicked(MouseEvent e) {
	}


	/**
	 * Called whenever the mouse enters a component listened to by this
	 * status bar.  This method should not be overridden.
	 *
	 * @param e The mouse event.
	 */
	public void mouseEntered(MouseEvent e) {

		Component c = e.getComponent();
		String description = null;

		// First check to see if we have a button with an action whose
		// description is defined.
		if (c instanceof AbstractButton) {
			Action a = ((AbstractButton)c).getAction();
			if (a!=null)
				description = (String)a.getValue(Action.SHORT_DESCRIPTION);
		}

		// Otherwise, check the accessible context of the component.
		if (description==null)
			description = c.getAccessibleContext().
							getAccessibleDescription();

		if (description != null)
			setStatusMessage(description);

	}


	/**
	 * Called whenever the mouse exits in a component listened to by this
	 * status bar.  This method should not be overridden.
	 *
	 * @param e The mouse event.
	 */
	public final void mouseExited(MouseEvent e) {
		setStatusMessage(getDefaultStatusMessage());
	}


	/**
	 * Called whenever the mouse is pressed in a component listened to by this
	 * status bar.  This method should not be overridden.
	 *
	 * @param e The mouse event.
	 */
	public void mousePressed(MouseEvent e) {
	}


	/**
	 * Called whenever the mouse is released in a component listened to by this
	 * status bar.  This method should not be overridden.
	 *
	 * @param e The mouse event.
	 */
	public void mouseReleased(MouseEvent e) {
	}


	/**
	 * Setter function for message in the status bar.
	 *
	 * @param newMessage The new message to display.
	 * @see #getStatusMessage
	 */
	public void setStatusMessage(String newMessage) {
		statusMessage.setText(newMessage);
		// We paint immediately for components like
		// org.fife.ui.FindInFilesDialog.
		statusMessage.paintImmediately(statusMessage.getBounds());
	}


	/**
	 * Sets the "style" used for this status bar.  This method fires a
	 * property change event of type <code>STYLE_PROPERTY</code>.
	 *
	 * @param style The style to use.  If this value is invalid,
	 *        <code>WINDOWS_98_STYLE</code> is used.
	 * @see #getStyle
	 */
	public void setStyle(int style) {

		// Be sure the passed-in style is valid.
		if (style<MIN_STYLE_CONSTANT || style>MAX_STYLE_CONSTANT)
			style = MIN_STYLE_CONSTANT;

		// Only change styles if the new style is different.
		int oldStyle = sizeGrip.getSizeGripIcon().getStyle();
		if (oldStyle!=style) {

			sizeGrip.getSizeGripIcon().setStyle(style);

			// Stuff appropriate for all styles.
			Component[] comps = getComponents();
			int count = comps.length;
			if (sizeGrip!=null)
				count--; // Skip the size grip if necessary.
			for (int i=1; i<count; i++) {
				((JComponent)comps[i]).
							setBorder(getStatusBarComponentBorder());
			}

			// Any specialization needed.
			switch (style) {

				case WINDOWS_98_STYLE:
					if (oldStyle==WINDOWS_XP_STYLE) {
						remove(count-1); // Remove the filler panel.
					}
					break;

				case WINDOWS_XP_STYLE:
					// Add the filler panel.
					GridBagConstraints c = new GridBagConstraints();
					c.fill = GridBagConstraints.BOTH;
					c.weightx = 0.0;
					JPanel filler = new StatusBarPanel();//JPanel();
					gridBag.setConstraints(filler, c);
					// We can't use addStatusBarComponent as the filler is
					// a special case.
					add(filler, count);
					break;

			}

		}

	}


}