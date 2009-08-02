/*
 * 11/14/2003
 *
 * ImagePreviewPane.java - An accessory for JFileChoosers that displays a
 * preview of image (gif, png or jpg) files.
 * Copyright (C) 2003 Robert Futrell
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
package org.fife.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;


/**
 * An accessory panel for <code>javax.swing.JFileChooser</code>s that are used
 * for picking image files (<code>gif</code>, <code>png</code> or
 * <code>jpg</code>).  The panel displays a "preview" of the currently selected
 * image.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ImagePreviewPane extends JComponent
								implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private JLabel imageLabel;

	private static final int WIDTH			= 120;
	private static final int HEIGHT			= WIDTH;


	/**
	 * Creates a new <code>ImagePreviewPane</code>.
	 */
	public ImagePreviewPane() {
		setLayout(new GridLayout(1,1));
		setBorder(BorderFactory.createTitledBorder("Preview"));
		imageLabel = new JLabel();
		imageLabel.setPreferredSize(new Dimension(WIDTH,HEIGHT));
		add(imageLabel);
	}


	/**
	 * Listens for when the user selects a new file in the parent
	 * <code>javax.swing.JFileChooser</code>.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String propName = e.getPropertyName();

		if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(propName)) {
			File file = (File)e.getNewValue();
			if (file!=null) {
				ImageIcon icon = null;
				try {
					Image image = ImageIO.read(file);
					if (image!=null) {
						image = image.getScaledInstance(WIDTH,HEIGHT,
											Image.SCALE_DEFAULT);
						icon = new ImageIcon(image);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				imageLabel.setIcon(icon);
				repaint();
			}
		}

	}


}