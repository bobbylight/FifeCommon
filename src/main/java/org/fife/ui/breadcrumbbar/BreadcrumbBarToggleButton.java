/*
 * 09/28/2009
 *
 * BreadcrumbBarToggleButton.java - A toggle button for a breadcrumb bar.
 * Copyright (C) 2009 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.breadcrumbbar;

import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.plaf.ButtonUI;


/**
 * Button that always uses a special UI.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class BreadcrumbBarToggleButton extends JToggleButton {


	public BreadcrumbBarToggleButton(Icon icon) {
		super(icon);
	}


	@Override
	public void setUI(ButtonUI ui) {
		super.setUI(new BreadcrumbBarToggleButtonUI());
	}


}