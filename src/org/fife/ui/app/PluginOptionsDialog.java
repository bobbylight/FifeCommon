/*
 * 04/28/2005
 *
 * PluginOptionsDialog.java - Dialog that displays the options for all plugins
 * in an AbstractPluggableGUIApplication.
 * Copyright (C) 2005 Robert Futrell
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
package org.fife.ui.app;

import java.util.ArrayList;

import org.fife.ui.OptionsDialog;
import org.fife.ui.OptionsDialogPanel;


/**
 * A dialog that displays all options for all plugins in an
 * <code>AbstractPluggableGUIApplication</code>.
 *
 * @author Robert Futrell
 * @version 0.5
 * @see AbstractPluggableGUIApplication
 */
class PluginOptionsDialog extends OptionsDialog {

	/**
	 * Constructor.
	 * 
	 * @param app The GUI application.
	 */
	public PluginOptionsDialog(AbstractPluggableGUIApplication app) {

		super(app);

		Plugin[] plugins = app.getPlugins();
		int count = plugins.length;
		ArrayList optionsPanelList = new ArrayList();
		for (int i=0; i<count; i++) {
			OptionsDialogPanel panel = plugins[i].getOptionsDialogPanel();
			if (panel!=null) {
				optionsPanelList.add(panel);
			}
		}
		count = optionsPanelList.size();
		OptionsDialogPanel[] optionsPanels = new OptionsDialogPanel[count];
		optionsPanels = (OptionsDialogPanel[])optionsPanelList.
											toArray(optionsPanels);
		setOptionsPanels(optionsPanels);
		setLocationRelativeTo(app);

	}


}