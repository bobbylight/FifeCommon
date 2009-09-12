package org.fife.ui.breadcrumbbar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;


/**
 * The UI for the buttons in a breadcrumb bar.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class BreadcrumbBarButtonUI extends BasicButtonUI {


	protected void installDefaults(AbstractButton b) {
		super.installDefaults(b);
		b.setMargin(new Insets(5, 5, 5, 5));
		b.setRolloverEnabled(true); // Not true by default
		b.setFocusable(false); // Prevent JRootPane default button issues
	}
   

	public void paint(Graphics g, JComponent c) {

		AbstractButton b = (AbstractButton)c;
		ButtonModel model = b.getModel();
		if (Boolean.TRUE==b.getClientProperty("arrowSelected")) {
			Color c1 = new Color(194, 228, 246);
			Color c2 = new Color(146, 204, 235);
			g.setColor(c1);
			g.fillRect(0,0, b.getWidth(),b.getHeight()/2);
			g.setColor(c2);
			g.fillRect(0,b.getHeight()/2, b.getWidth(),b.getHeight()/2);
		}
		else if (Boolean.TRUE==b.getClientProperty("arrowActivated")) {
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

		super.paint(g, c);

//		g.setColor(java.awt.Color.BLACK);
//		g.drawRect(0, 0, c.getWidth()-1, c.getHeight()-1);

	}


}
