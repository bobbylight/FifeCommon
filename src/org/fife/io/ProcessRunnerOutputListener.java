/*
 * 01/21/2008
 *
 * ProcessRunnerOutputListener.java - Receives notification of lines
 * written to stdout/stderr from a ProcessRunner.
 *
 * This class is public domain.  Use however you see fit.
 */
package org.fife.io;


/**
 * Listens for lines being written to stdout and stderr by a
 * <code>ProcessRunner</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface ProcessRunnerOutputListener {


	/**
	 * Called when output was written to stdout or stderr.
	 *
	 * @param output The output written.
	 * @param stdout If <code>true</code>, this is stdout output.
	 *        If <code>false</code>, it is stderr.
	 */
	public void outputWritten(String output, boolean stdout);


}