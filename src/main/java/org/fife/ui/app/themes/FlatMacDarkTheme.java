/*
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.themes;


/**
 * An application theme that uses the FlatLaF "dark" theme tuned for macOS.
 */
public class FlatMacDarkTheme extends AbstractAppTheme {

	/**
	 * The ID for this theme.
	 */
	public static final String ID = "flatlaf-mac-dark";

	/**
	 * The display name for this theme.
	 */
	public static final String NAME = "Flat Mac Dark";

	private static final String MACOS_FLAT_DARK_LAF = "com.formdev.flatlaf.themes.FlatMacDarkLaf";

	public FlatMacDarkTheme() {
		super(ID, NAME, MACOS_FLAT_DARK_LAF);
	}

}
