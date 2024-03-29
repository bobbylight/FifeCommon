/*
 * 09/28/2009
 *
 * BreadcrumbBarButton.java - A button for a breadcrumb bar.
 * Copyright (C) 2009 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.breadcrumbbar;

import javax.swing.JButton;
import javax.swing.plaf.ButtonUI;


/**
 * A button that permanently uses a special UI.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class BreadcrumbBarButton extends JButton {


	BreadcrumbBarButton(String text) {
		super(text);
	}


	@Override
	public void setUI(ButtonUI ui) {
		super.setUI(new BreadcrumbBarButtonUI());
	}


}
