/*
 * 09/26/2009
 *
 * BreadcrumbBarTest.java - A simple test application for the breadcrumb bar.
 * Copyright (C) 2009 Robert Futrell
 * https://bobbylight.github.io/RText/
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


/**
 * A test class for the {@code BreadcrumbBar} class.
 */
public class BreadcrumbBarTest extends JFrame implements PropertyChangeListener{

	private JTextField tf;
	private static final String SUBSTANCE_PKG = "org.pushingpixels.substance.api.skin.Substance";


	public BreadcrumbBarTest() {

		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("File");
		mb.add(menu);
		menu.add(new LookAndFeelAction(UIManager.getSystemLookAndFeelClassName()));
		menu.add(new LookAndFeelAction("javax.swing.plaf.metal.MetalLookAndFeel"));
		menu.add(new LookAndFeelAction("com.sun.java.swing.plaf.motif.MotifLookAndFeel"));
		menu.add(new LookAndFeelAction("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"));
		menu.addSeparator();
		menu.add(new LookAndFeelAction(SUBSTANCE_PKG + "GraphiteAquaLookAndFeel"));
		menu.add(new LookAndFeelAction(SUBSTANCE_PKG + "GraphiteGlassLookAndFeel"));
		menu.add(new LookAndFeelAction(SUBSTANCE_PKG + "CeruleanLookAndFeel"));
		menu.add(new LookAndFeelAction(SUBSTANCE_PKG + "CremeLookAndFeel"));
		menu.add(new LookAndFeelAction(SUBSTANCE_PKG + "CremeCoffeeLookAndFeel"));
		menu.add(new LookAndFeelAction(SUBSTANCE_PKG + "BusinessLookAndFeel"));
		menu.add(new LookAndFeelAction(SUBSTANCE_PKG + "MistAquaLookAndFeel"));
		menu.add(new LookAndFeelAction(SUBSTANCE_PKG + "DustCoffeeLookAndFeel"));
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


	/**
	 * Entry point for a quick visual test.
	 *
	 * @param args Command lines arguments.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			String substanceClass = SUBSTANCE_PKG + "GraphiteGlassLookAndFeel";
			UIManager.installLookAndFeel("Substance", substanceClass);
			try {
				UIManager.setLookAndFeel(substanceClass);
			} catch (Exception e) {
				e.printStackTrace();
			}
			new BreadcrumbBarTest().setVisible(true);
		});
	}


	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String propName = e.getPropertyName();

		if (BreadcrumbBar.PROPERTY_LOCATION.equals(propName)) {
			File loc = (File)e.getNewValue();
			tf.setText(loc.getAbsolutePath());
		}
	}


	/**
	 * Performs a Look and Feel change.
	 */
	private class LookAndFeelAction extends AbstractAction {

		private String laf;

		LookAndFeelAction(String laf) {
			this.laf = laf;
			int dot = laf.lastIndexOf('.');
			putValue(NAME, laf.substring(dot+1));
		}

		@Override
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
