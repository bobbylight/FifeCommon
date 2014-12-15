/*
 * 09/26/2009
 *
 * BreadcrumbBarTest.java - A simple test application for the breadcrumb bar.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.breadcrumbbar;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.*;


public class BreadcrumbBarTest extends JFrame implements PropertyChangeListener{

	private JTextField tf;
	private static final String substancePkg = "org.pushingpixels.substance.api.skin.Substance";


	public BreadcrumbBarTest() {

		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("File");
		mb.add(menu);
		menu.add(new LookAndFeelAction(UIManager.getSystemLookAndFeelClassName()));
		menu.add(new LookAndFeelAction("javax.swing.plaf.metal.MetalLookAndFeel"));
		menu.add(new LookAndFeelAction("com.sun.java.swing.plaf.motif.MotifLookAndFeel"));
		menu.add(new LookAndFeelAction("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"));
		menu.addSeparator();
		menu.add(new LookAndFeelAction(substancePkg + "GraphiteAquaLookAndFeel"));
		menu.add(new LookAndFeelAction(substancePkg + "GraphiteGlassLookAndFeel"));
		menu.add(new LookAndFeelAction(substancePkg + "CeruleanLookAndFeel"));
		menu.add(new LookAndFeelAction(substancePkg + "CremeLookAndFeel"));
		menu.add(new LookAndFeelAction(substancePkg + "CremeCoffeeLookAndFeel"));
		menu.add(new LookAndFeelAction(substancePkg + "BusinessLookAndFeel"));
		menu.add(new LookAndFeelAction(substancePkg + "MistAquaLookAndFeel"));
		menu.add(new LookAndFeelAction(substancePkg + "DustCoffeeLookAndFeel"));
		setJMenuBar(mb);

		BreadcrumbBar bb = new BreadcrumbBar();
		bb.addPropertyChangeListener(BreadcrumbBar.PROPERTY_LOCATION, this);
		//bb.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		JPanel cp = new JPanel(new BorderLayout());
		cp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		cp.add(bb, BorderLayout.NORTH);

		tf = new JTextField(bb.getShownLocation().getAbsolutePath());
		cp.add(tf, BorderLayout.SOUTH);

		setContentPane(cp);
		setTitle("BreadcrumbBar Test");
		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

	}


	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String substanceClass = substancePkg + "GraphiteGlassLookAndFeel";
				UIManager.installLookAndFeel("Substance", substanceClass);
				String laf = substanceClass;
				try {
					UIManager.setLookAndFeel(laf);
				} catch (Exception e) {
					e.printStackTrace();
				}
				new BreadcrumbBarTest().setVisible(true);
			}
		});
	}


	public void propertyChange(PropertyChangeEvent e) {

		String propName = e.getPropertyName();

		if (BreadcrumbBar.PROPERTY_LOCATION.equals(propName)) {
			File loc = (File)e.getNewValue();
			tf.setText(loc.getAbsolutePath());
		}
	}


	private class LookAndFeelAction extends AbstractAction {

		private String laf;

		public LookAndFeelAction(String laf) {
			this.laf = laf;
			int dot = laf.lastIndexOf('.');
			putValue(NAME, laf.substring(dot+1));
		}

		public void actionPerformed(ActionEvent e) {
			try {
				UIManager.setLookAndFeel(laf);
				SwingUtilities.updateComponentTreeUI(BreadcrumbBarTest.this);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}


}