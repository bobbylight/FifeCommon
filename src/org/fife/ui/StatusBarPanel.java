/*
 * 09/22/2004
 *
 * StatusBarPanel.java - Panel used by StatusBar; it can have a "shadow" effect
 * displayed at its top and bottom.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.LayoutManager;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * A panel used internally by <code>StatusBar</code>; it can have a "shadow"
 * effect displayed at its top and bottom.  This shadow can be toggled on and
 * off.  This class is used to mimic the status bar look found in Microsoft
 * Windows XP-style status bars.
 *
 * @author Robert Futrell
 * @version 0.5
 */
public class StatusBarPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Whether or not the "shadow" effect is on.
	 */
	private boolean shadowEnabled;


	/**
	 * Constructor.
	 */
	public StatusBarPanel() {
		shadowEnabled = true;
	}


	/**
	 * Constructor.
	 *
	 * @param layout The layout for the panel.
	 */
	public StatusBarPanel(LayoutManager layout) {
		super(layout);
		shadowEnabled = true;
	}


	/**
	 * Creates a <code>StatusBarPanel</code> containing a label.  This is a
	 * convenience constructor, since many status bar panels contain merely
	 * a label.
	 *
	 * @param layout The layout to use for the panel.
	 * @param label The label this panels hould contain.
	 */
	public StatusBarPanel(LayoutManager layout, JLabel label) {
		super(layout);
		add(label);
		shadowEnabled = true;
	}


	/**
	 * Returns whether the shadow is enabled.
	 *
	 * @return Whether the shadow is enabled.
	 * @see #setShadowEnabled
	 */
	public boolean isShadowEnabled() {
		return shadowEnabled;
	}


	/**
	 * Paints this panel with the (optional) shadow effect.
	 *
	 * @param g The graphics context with which to paint.
	 */
	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g); // Fill in background.

		// Draw the shadow.
		if (isShadowEnabled()==true) {

			int width = getWidth();
			int height = getHeight();
			Color bg = getBackground();
			// top.
			Color color = bg;
			color = UIUtil.deriveColor(color, 15);
			g.setColor(color);
			g.drawLine(0,2, width,2);
			color = UIUtil.deriveColor(color, 24);
			g.setColor(color);
			g.drawLine(0,1, width,1);
			color = UIUtil.deriveColor(color, 40);
			g.setColor(color);
			g.drawLine(0,0, width,0);

			// bottom.
			color = UIUtil.deriveColor(bg, 8);
			g.setColor(color);
			g.drawLine(0,height-1, width,height-1);
			color = UIUtil.deriveColor(color, 4);
			g.setColor(color);
			g.drawLine(0,height-2, width,height-2);
			color = UIUtil.deriveColor(color, -3);
			g.setColor(color);
			g.drawLine(0,height-3, width,height-3);
			color = UIUtil.deriveColor(color, -5);
			g.setColor(color);
			g.drawLine(0,height-4, width,height-4);

		}

	}


	/**
	 * Sets whether or not the shadow effect is enabled.
	 *
	 * @param enabled Whether the shadow effect should be enabled.
	 * @see #isShadowEnabled
	 */
	public void setShadowEnabled(boolean enabled) {
		if (this.shadowEnabled!=enabled) {
			this.shadowEnabled = enabled;
			repaint();
		}
	}


}