/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app.themes;


/**
 * An application theme that uses the FlatLaF "dark" theme.
 */
public class FlatDarkTheme extends AbstractAppTheme {

	/**
	 * The ID for this theme.
	 */
	public static final String ID = "flatlaf-dark";

	/**
	 * The display name for this theme.
	 */
	public static final String NAME = "Flat Dark";


	public FlatDarkTheme() {
		super(ID, NAME, "com.formdev.flatlaf.FlatDarkLaf");
	}

}
