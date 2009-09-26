/*
 * 09/26/2009
 *
 * BreadcrumbBarTest.java - A simple test application for the breadcrumb bar.
 * Copyright (C) 2009 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.ui.breadcrumbbar;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.*;


public class BreadcrumbBarTest extends JFrame implements PropertyChangeListener{

	private JTextField tf;


	public BreadcrumbBarTest() {

		BreadcrumbBar bb = new BreadcrumbBar();
		bb.addPropertyChangeListener(BreadcrumbBar.PROPERTY_LOCATION, this);
bb.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

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
				String laf = UIManager.getSystemLookAndFeelClassName();
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


}