/*
 * 03/31/2004
 *
 * RColorButton.java - A JButton that lets you pick a color.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;


/**
 * A <code>javax.swing.JButton</code> that lets the user pick
 * a color.  A color is displayed instead of an icon and/or text,
 * and clicking on the button causes a <code>JColorChooser</code>
 * to appear, letting the user change the button's current color.
 * Whenever the button's color changes, a property change event
 * of type <code>COLOR_CHANGED_PROPERTY</code> is fired.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RColorButton extends JButton {

	private static final long serialVersionUID = 1L;

	/**
	 * The property fired when the user changes the color displayed by this
	 * button.
	 */
	public static final String COLOR_CHANGED_PROPERTY	= "RColorButton.colorChanged";


	/**
	 * Creates a new <code>RColorButton</code>.
	 *
	 * @param color The initial color for the button.
	 */
	public RColorButton(Color color) {
		this(color, 40,12);
	}


	/**
	 * Creates a new <code>RColorButton</code>.
	 *
	 * @param color The initial color for the button.
	 * @param width The width of the color in the button.
	 * @param height The height of the color in the button.
	 */
	public RColorButton(Color color, int width, int height) {
		setMargin(new Insets(5,5,5,5));
		setIcon(new ColorIcon(color, width, height));
		setDisabledIcon(new ColorIcon(color.darker(), width,height));
		addActionListener(createActionListener());
	}


	/**
	 * Returns the listener listening for this button to be clicked.
	 * Classes which override <code>RColorButton</code> can return an
	 * <code>ActionListener</code> here to do different things when the button
	 * is clicked.
	 *
	 * @return The action listener for this button.  For a regular
	 *         <code>RColorButton</code>, the returned listener displays a
	 *         <code>JColorChooser</code>.
	 */
	@Override
	public ActionListener createActionListener() {
		return new RColorButtonActionListener();
	}


	/**
	 * Returns the color currently displayed by the button.
	 *
	 * @return The button's color.
	 */
	public Color getColor() {
		return ((ColorIcon)getIcon()).color;
	}


	/**
	 * Sets the button's color.  This method fires a property change of type
	 * <code>COLOR_CHANGED_PROPERTY</code>.
	 *
	 * @param color The new color for the button.  If this value is not
	 *        equal to <code>null</code> and is not equal to the color
	 *        already being displayed, a property change event of type
	 *        <code>COLOR_CHANGED_PROPERTY</code> is fired.
	 */
	public void setColor(Color color) {
		Color oldColor = getColor();
		if (color!=null && !color.equals(oldColor)) {
			ColorIcon oldIcon = (ColorIcon)getIcon();
			int width = oldIcon.getIconWidth();
			int height = oldIcon.getIconHeight();
			setIcon(new ColorIcon(color, width,height));
			setDisabledIcon(new ColorIcon(color.darker(), width,height));
			firePropertyChange(COLOR_CHANGED_PROPERTY, oldColor, color);
		}
	}


	/**
	 * An icon that is simply a rectangle of a given color.
	 */
	protected static class ColorIcon implements Icon {

		Color color;
		int width, height;

		public ColorIcon(Color color, int width, int height) {
			this.color = color;
			this.width = width;
			this.height = height;
		}

		public Color getColor() {
			return color;
		}

		public int getIconWidth() {
			return width;
		}

		public int getIconHeight() {
			return height;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (color!=null) {
				g.setColor(color);
				g.fillRect(x,y, width, height);
			}
		}

	}

	/**
	 * Listens for the user to click on the <code>RColorButton</code> so
	 * a <code>JColorChooser</code> can be displayed.
	 */
	protected class RColorButtonActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Color chosenColor = JColorChooser.showDialog(
									RColorButton.this,
									"Pick a Color",
									RColorButton.this.getColor());
			if (chosenColor!=null) {
				RColorButton.this.setColor(chosenColor);
			}
		}

	}


}