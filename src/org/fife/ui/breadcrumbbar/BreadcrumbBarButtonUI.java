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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.Map;

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


	protected void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		b.setMargin(new Insets(5, 4, 5, 4));
		b.setBorder(new ButtonBorder());
		b.setRolloverEnabled(true); // Not true by default
		b.setFocusable(false); // Prevent JRootPane default button issues
		b.setOpaque(false);
	}
   

	public void paint(Graphics g, JComponent c) {

		AbstractButton b = (AbstractButton)c;
		ButtonModel model = b.getModel();
		if (Boolean.TRUE==b.getClientProperty(BreadcrumbBar.ARROW_SELECTED)) {
			Color c1 = new Color(194, 228, 246);
			Color c2 = new Color(146, 204, 235);
			g.setColor(c1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(c2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
		}
		else if (Boolean.TRUE==b.getClientProperty(BreadcrumbBar.ARROW_ACTIVATED)) {
			Color c1 = new Color(240,240,240);
			Color c2 = new Color(212,212,212);
			g.setColor(c1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(c2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
		}
		else if (model.isPressed()) {
			Color c1 = new Color(194, 228, 246);
			Color c2 = new Color(146, 204, 235);
			g.setColor(c1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(c2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
		}
		else if (model.isRollover() || model.isArmed()) {
			Color c1 = new Color(223, 242, 252);
			Color c2 = new Color(177, 223, 249);
			g.setColor(c1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(c2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
		}

		boolean substance = SubstanceUtils.isSubstanceInstalled();
		Map oldHints = null;
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
