/*
 * 09/26/2009
 *
 * BreadcrumbBarButtonUI.java - UI for a breadcrumb bar's buttons.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.breadcrumbbar;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

import org.fife.ui.SubstanceUtils;
import org.fife.ui.UIUtil;


/**
 * The UI for the buttons in a breadcrumb bar.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class BreadcrumbBarButtonUI extends BasicButtonUI {

	private BreadcrumbBarButtonColorSet colors;


	public BreadcrumbBarButtonUI() {
		colors = new BreadcrumbBarButtonColorSet();
	}


	public BreadcrumbBarButtonColorSet getColorSet() {
		return colors;
	}


	@Override
	protected void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		b.setMargin(new Insets(5, 4, 5, 4));
		b.setBorder(new ButtonBorder());
		b.setRolloverEnabled(true); // Not true by default
		b.setFocusable(false); // Prevent JRootPane default button issues
		b.setOpaque(false);
		colors.initialize(b);
		b.putClientProperty("breadcrumbBorderColor", colors.borderColor);
	}


	@Override
	public void paint(Graphics g, JComponent c) {

		AbstractButton b = (AbstractButton)c;
		ButtonModel model = b.getModel();
		if (Boolean.TRUE==b.getClientProperty(BreadcrumbBar.ARROW_SELECTED)) {
			g.setColor(colors.pressedC1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(colors.pressedC2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
			c.setForeground(colors.pressedFG);
		}
		else if (Boolean.TRUE==b.getClientProperty(BreadcrumbBar.ARROW_ACTIVATED)) {
			g.setColor(colors.nonArrowArrowArmedC1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(colors.nonArrowArrowArmedC2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
			c.setForeground(colors.pressedFG);
		}
		else if (model.isPressed()) {
			g.setColor(colors.pressedC1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(colors.pressedC2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
			c.setForeground(colors.pressedFG);
		}
		else if (model.isRollover() || model.isArmed()) {
			g.setColor(colors.rolloverC1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(colors.rolloverC2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
			c.setForeground(colors.rolloverFG);
		}
		else {
			c.setForeground(colors.defaultFG);
		}

		boolean substance = SubstanceUtils.isSubstanceInstalled();
		RenderingHints oldHints = null;
		if (substance) {
			oldHints = UIUtil.setNativeRenderingHints((Graphics2D)g);
		}
		super.paint(g, c);
		if (oldHints!=null) {
			((Graphics2D)g).setRenderingHints(oldHints);
		}

//		g.setColor(java.awt.Color.BLACK);
//		g.drawRect(0, 0, c.getWidth()-1, c.getHeight()-1);

	}


}
