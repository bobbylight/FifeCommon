/*
 * 09/26/2009
 *
 * BreadcrumbBarToggleButtonUI.java - A UI for breadcrumb bar toggle buttons.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.breadcrumbbar;

import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicToggleButtonUI;


/**
 * UI for toggle buttons.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class BreadcrumbBarToggleButtonUI extends BasicToggleButtonUI {

	private BreadcrumbBarButtonColorSet colors;


	public BreadcrumbBarToggleButtonUI() {
		colors = new BreadcrumbBarButtonColorSet();
	}


	public BreadcrumbBarButtonColorSet getColorSet() {
		return colors;
	}


	@Override
	protected void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		b.setMargin(new Insets(5, 3, 5, 3));
		b.setRolloverEnabled(true); // Not true by default.
		b.setFocusable(false); // Prevent JRootPane default button issues
		b.setBorder(new ButtonBorder());
		b.setOpaque(false);
		colors.initialize(b);
		b.putClientProperty("breadcrumbBorderColor", colors.borderColor);
	}
   

	@Override
	public void paint(Graphics g, JComponent c) {

		AbstractButton b = (AbstractButton)c;
		ButtonModel model = b.getModel();

		if (model.isSelected() || model.isPressed()) {
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

		super.paint(g, c);

//		g.setColor(java.awt.Color.BLACK);
//		g.drawRect(0, 0, c.getWidth()-1, c.getHeight()-1);

	}


}