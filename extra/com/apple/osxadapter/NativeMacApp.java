package com.apple.osxadapter;

/**
 * An interface that applications can implement so they get better integrated
 * with the Mac menu bar (including preferences, saving, quitting, etc.).
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface NativeMacApp {

	public void about();

	public void openFile(String file);

	public void preferences();

	public void quit();

}