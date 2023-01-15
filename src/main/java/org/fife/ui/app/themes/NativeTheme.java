/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.themes;

import javax.swing.*;


/**
 * An application theme that installs the system default
 * look and feel.  Applications can extend this class
 * to add additional, app-specific colors and styles for
 * custom components.
 */
public class NativeTheme extends AbstractAppTheme {

	/**
	 * The ID for this theme.
	 */
	public static final String ID = "native";

	/**
	 * The display name for this theme.
	 */
	public static final String NAME;

	static {

		// Fetch the system LookAndFeel class name without loading the full
		// LaF, since it can be heavy.
		UIManager.LookAndFeelInfo systemInfo = null;
		for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			String clazzName = info.getClassName();
			if (clazzName.equals(UIManager.getSystemLookAndFeelClassName())) {
				systemInfo = info;
				break;
			}
		}

		NAME = systemInfo != null ? systemInfo.getName() : "Native";
	}


	public NativeTheme() {
		super(ID, NAME, UIManager.getSystemLookAndFeelClassName());
	}
}
