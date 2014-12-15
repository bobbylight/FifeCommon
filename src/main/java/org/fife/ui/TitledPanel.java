/*
 * 09/08/2004
 *
 * TitledPanel.java - A panel suitable for a "heading" for another panel, such
 * as a panel in an Options dialog.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;


/**
 * A panel suitable for a "heading" for another <code>JPanel</code>.  This
 * panel is similar to that found in Elipse's "New" and "Preferences" dialogs,
 * depending on how you initialize it.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class TitledPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public static final int BEVEL_BORDER	= 0;
	public static final int LINE_BORDER	= 1;

	private JLabel label;
	private JLabel iconLabel;

	private static final int GRADIENT_WIDTH	= 80;
	private static final Font DEFAULT_FONT	= new Font("dialog", Font.BOLD, 12);


	public TitledPanel(String title, Component content, int border) {
		this(title, DEFAULT_FONT, content, border);
	}


	public TitledPanel(String title, Font font, Component content,
					int border) {

		// Initialize our title label and icon label.
		setTitle(title);
		setIcon(null);
		label.setFont(font);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		boolean ltr = orientation.isLeftToRight();

		// The top panel is the gradient-colored panel.
		JPanel topPanel = new GradientPanel(GRADIENT_WIDTH);
		topPanel.setLayout(new BorderLayout());

		// Set up the top panel.
		label.setBorder(UIUtil.getEmpty5Border());
		topPanel.add(label, BorderLayout.LINE_START);
		iconLabel.setBorder(ltr ? BorderFactory.createEmptyBorder(5,5,5,10) :
							BorderFactory.createEmptyBorder(5,10,5,5));
		topPanel.add(iconLabel, BorderLayout.LINE_END);
		topPanel.setBorder(border==BEVEL_BORDER ? new BottomBevelBorder() :
							BorderFactory.createLineBorder(Color.GRAY));
		
		// Add the "top panel" to the top of us, and the content in our
		// middle.
		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);
		add(content);
		applyComponentOrientation(orientation);

	}


	/**
	 * Returns the icon displayed by this titled panel.
	 *
	 * @return The icon.
	 * @see #setIcon
	 */
	public Icon getIcon() {
		return iconLabel.getIcon();
	}


	/**
	 * Returns the title of this titled panel.
	 *
	 * @return The title.
	 * @see #setTitle
	 */
	public String getTitle() {
		return label.getText(); // label cannot be null here.
	}


	/**
	 * Sets the icon to display in this titled panel.
	 *
	 * @param icon The icon to display.
	 * @see #getIcon
	 */
	public void setIcon(Icon icon) {
		if (iconLabel==null)
			iconLabel = new JLabel(icon);
		else
			iconLabel.setIcon(icon);
	}


	/**
	 * Sets the title of this titled panel.
	 *
	 * @param title The new title.
	 * @see #getTitle
	 */
	public void setTitle(String title) {
		if (label==null) {
			label = new JLabel(title);
		}
		else {
			label.setText(title);
		}
	}


	private static class GradientPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private int gradientWidth;
		private Color background;
		private Color gradientBG;

		GradientPanel(int gradientWidth) {
			this.gradientWidth = gradientWidth;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Rectangle bounds = getBounds();
			int w = 0;
			if (bounds.width>gradientWidth) {
				g.setColor(background);
				w = bounds.width - gradientWidth;
				g.fillRect(0,0, w,bounds.height);
			}
			Graphics2D g2d = (Graphics2D)g;
			GradientPaint paint = new GradientPaint(w,0, background,
									bounds.width,0, gradientBG);
			g2d.setPaint(paint);
			g2d.fill(new Rectangle(w,0, gradientWidth,bounds.height));
		}

		@Override
		public void updateUI() {

			super.updateUI();

			// Set by BasicLookAndFeels and Substance
			background = UIManager.getColor("TextField.background");
			if (background==null) {
				// Assume a "light" default background...
				background = Color.white;
			}

			gradientBG = UIManager.getColor("TextField.selectionBackground");
			if (gradientBG!=null) {
				gradientBG = gradientBG.brighter();
			}
			else {
				gradientBG = new Color(160,160,255); // How to pick a color?
			}
		}

	}


	public static class BottomBevelBorder implements Border {

		private static final Insets insets = new Insets(0, 0, 2, 0);

		private Color light, dark;

		public BottomBevelBorder() {
			this(null, null);
		}

		public BottomBevelBorder(Color light, Color dark) {
			this.light = light;
			this.dark = dark;
		}

		public Insets getBorderInsets(Component c) {
			return insets;
		}

		public boolean isBorderOpaque() {
			return true;
		}

		public void paintBorder(Component c, Graphics g, int x, int y,
							int width, int height) {
			Rectangle bounds = c.getBounds();
			y += bounds.height - 2;
			g.setColor(dark==null ? c.getBackground().darker() : dark);
			g.drawLine(bounds.x,y, bounds.x+bounds.width,y);
			g.setColor(light==null ? c.getBackground().brighter() : light);
			g.drawLine(bounds.x,y+1, bounds.x+bounds.width,y+1);
		}

	}


}