/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.themes;


/**
 * An application theme that uses the FlatLaF "light" theme.
 */
public class FlatLightTheme extends AbstractAppTheme {

	/**
	 * The ID for this theme.
	 */
	public static final String ID = "flatlaf-light";

	/**
	 * The display name for this theme.
	 */
	public static final String NAME = "Flat Light";

	public FlatLightTheme() {
		super(ID, NAME, "com.formdev.flatlaf.FlatLightLaf");
	}

}
