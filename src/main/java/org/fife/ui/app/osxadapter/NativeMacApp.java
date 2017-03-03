package org.fife.ui.app.osxadapter;

/**
 * An interface that applications can implement so they get better integrated
 * with the Mac menu bar (including preferences, saving, quitting, etc.).
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface NativeMacApp {

	void about();

	void openFile(String file);

	void preferences();

	void quit();

}