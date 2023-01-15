/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.themes;


/**
 * An application theme that uses the FlatLaF "light" theme tuned for macOS.
 */
public class FlatMacLightTheme extends AbstractAppTheme {

	/**
	 * The ID for this theme.
	 */
	public static final String ID = "flatlaf-mac-light";

	/**
	 * The display name for this theme.
	 */
	public static final String NAME = "Flat Mac Light";

	private static final String MACOS_FLAT_LIGHT_LAF = "com.formdev.flatlaf.themes.FlatMacLightLaf";


	public FlatMacLightTheme() {
		super(ID, NAME, MACOS_FLAT_LIGHT_LAF);
	}

}
