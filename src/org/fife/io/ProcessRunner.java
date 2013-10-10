/*
 * 02/14/2006
 *
 * ProcessRunner.java - Runs an external process as safely as possible.
 * This code is a modified form of the following JavaWorld article:
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps_p.html
 *
 * This class is public domain.  Use however you see fit.
 */
package org.fife.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Runs an external process (a program, batch file, shell script, etc.)
 * as safely as possible.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ProcessRunner implements Runnable {

	private File dir;
	private String[] commandLine;
	private Map<String, String> envVars;
	private boolean appendEnv;
	private String stdout;
	private String stderr;
	private ProcessRunnerOutputListener outputListener;
	private int rc;
	private Throwable lastError;


	/**
	 * Constructor.
	 * 
	 * @param commandLine The command line to run, with each item in the
	 *        array being a single parameter.
	 * @throws IllegalArgumentException If <code>commandLine</code> has
	 *         length <code>0</code>.
	 * @throws NullPointerException If <code>commandLine</code> is
	 *         <code>null</code>.
	 */
	public ProcessRunner(String[] commandLine) {
		setCommandLine(commandLine);
		appendEnv = true;
	}


	/**
	 * Clears the <code>stdout</code> and <code>stderr</code> variables.
	 */
	private void clearLastOutput() {
		stdout = stderr = null;
		rc = Integer.MIN_VALUE;
		lastError = null;
	}


	/**
	 * Creates an array of "name=value" elements, suitable for
	 * <code>Runtime.getRuntime().exec()</code>.
	 *
	 * @return The array of environment variables.
	 */
	private String[] createEnvVarArray() {

		Map<String, String> env = new HashMap<String, String>();

		// If we want to append our environment to that of the parent process...
		if (appendEnv) {
			env.putAll(System.getenv());
		}

		// If we have any environment variables to append...
		if (this.envVars!=null) {
			env.putAll(this.envVars);
		}

		// Create an array of "name=value" elements.
		List<String> temp = new ArrayList<String>(env.size());
		for (Map.Entry<String, String> entry : env.entrySet()) {
			temp.add(entry.getKey() + "=" + entry.getValue());
		}
		String[] envp = new String[temp.size()];
		envp = temp.toArray(envp);

		return envp;

	}


	/**
	 * Returns whether any extra environment variables defined for this process
	 * to run with should be appended to the parent process's environment (as
	 * opposed to overwriting it).
	 *
	 * @return Whether to append the parent process's environment.
	 * @see #getEnvironmentVars()
	 * @see #setEnvironmentVars(Map, boolean)
	 */
	public boolean getAppendEnvironmentVars() {
		return appendEnv;
	}


	/**
	 * Returns the command line this external process runner will run as a
	 * string.  Parameters are wrapped in quotes.
	 *
	 * @return The command line this object will run.
	 * @see #setCommandLine(String[])
	 */
	public String getCommandLineString() {
		int count = commandLine.length;
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<count; i++) {
			sb.append(i==0 ? "\"" : " \"").append(commandLine[i]).
												append('\"');
		}
		return sb.toString();
	}


	/**
	 * Returns the directory to run the process in.
	 *
	 * @return The directory the process will run in.  If this is
	 *         <code>null</code>, then the process will run in the same
	 *         directory as this Java process.
	 * @see #setDirectory(File)
	 */
	public File getDirectory() {
		return dir;
	}


	/**
	 * Returns any extra environment variables defined for this process to run
	 * with.
	 *
	 * @return The environment variables.
	 * @see #getAppendEnvironmentVars()
	 * @see #setEnvironmentVars(Map, boolean)
	 */
	public Map<String, String> getEnvironmentVars() {
		Map<String, String> temp = new HashMap<String, String>();
		if (envVars!=null) {
			temp.putAll(envVars);
		}
		return temp;
	}


	/**
	 * Returns the last error thrown when trying to run a process, or
	 * <code>null</code> if the last process ran successfully.
	 *
	 * @return The error that the last-run process ended with, if any.
	 */
	public Throwable getLastError() {
		return lastError;
	}


	/**
	 * Returns the return code of the last process ran.
	 *
	 * @return The return code of the last process ran.
	 */
	public int getReturnCode() {
		return rc;
	}


	/**
	 * Returns the stderr of the process last ran.
	 *
	 * @return The stderr of the last process ran.
	 */
	public String getStderr() {
		return stderr;
	}


	/**
	 * Returns the stdout of the process last ran.
	 *
	 * @return The stdout of the last process ran.
	 */
	public String getStdout() {
		return stdout;
	}


	/**
	 * Runs the current external process.
	 * 
	 * @throws IOException If an I/O error occurs while running the process.
	 * @see #getStdout()
	 * @see #getStderr()
	 */
	public void run() {

		clearLastOutput(); // In case we throw an exception, clear output.

		Process proc = null;
		String[] envp = createEnvVarArray();
		try {
			proc = Runtime.getRuntime().exec(commandLine, envp, dir);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			lastError = ioe;
			if (outputListener!=null) {
				outputListener.processCompleted(proc, rc, lastError);
			}
			return;
		}

		// Create threads to read the stdout and stderr of the external
		// process.  If we do not do it this way, the process may
		// deadlock.
		InputStream errStream = proc.getErrorStream();
		InputStream outStream = proc.getInputStream();
		StreamReaderThread stdoutThread = new StreamReaderThread(proc,
										outStream, outputListener, true);
		StreamReaderThread stderrThread = new StreamReaderThread(proc,
										errStream, outputListener, false);
		stdoutThread.start();
		stderrThread.start();

		try {

			rc = proc.waitFor();
			proc = null;

			// Save the stdout and stderr. Don't interrupt reader threads;
			// just wait for them to terminate normally.
			//stdoutThread.interrupt();
			//stderrThread.interrupt();
			stdoutThread.join();
			stderrThread.join();
			stdout = stdoutThread.getStreamOutput();
			stderr = stderrThread.getStreamOutput();

		} catch (InterruptedException ie) {
			//ie.printStackTrace();
			stdoutThread.interrupt();
			stderrThread.interrupt();
			lastError = ie;
		} finally {
			if (proc!=null) {
				proc.destroy();
			}
		}

		if (outputListener!=null) {
			outputListener.processCompleted(proc, rc, lastError);
		}

	}


	/**
	 * Sets the directory to run the process in.
	 *
	 * @param dir The directory.
	 * @see #getDirectory()
	 */
	public void setDirectory(File dir) {
		this.dir = dir;
	}


	/**
	 * Sets the command line of the process to run.
	 *
	 * @param commandLine The command line parameters to run.
	 * @throws IllegalArgumentException If <code>commandLine</code> has
	 *         length <code>0</code>.
	 * @throws NullPointerException If <code>commandLine</code> is
	 *         <code>null</code>.
	 * @see #getCommandLineString()
	 */
	public void setCommandLine(String[] commandLine)
									throws IllegalArgumentException {
		int size = commandLine.length;
		if (size==0) {
			throw new IllegalArgumentException(
						"Must have at least 1 command line argument");
		}
		this.commandLine = new String[size];
		System.arraycopy(commandLine,0, this.commandLine,0, size);
		clearLastOutput(); // No output from this new command line yet.
	}


	/**
	 * Sets the environment variables to be set for this process.
	 *
	 * @param vars The environment variables.  This may be <code>null</code> if
	 *        none are to be set.
	 * @param append Whether this should be appended to the parent process's
	 *        environment.  If this is <code>false</code>, then the contents of
	 *        <code>vars</code> will be the only environment variables set.
	 * @see #getEnvironmentVars()
	 */
	public void setEnvironmentVars(Map<String, String> vars, boolean append) {
		appendEnv = append;
		if (envVars!=null) {
			envVars.clear();
		}
		else {
			envVars = new HashMap<String, String>();
		}
		envVars.putAll(vars);
	}


	/**
	 * Sets the output listener to receive notification when stdout or
	 * stderr is written to.  This listener will be used for all
	 * subsequent processes run with this <code>ProcessRunner</code>.
	 *
	 * @param listener The new listener.  The previous listener, if any,
	 *        will be removed.  If this is <code>null</code>, there
	 *        will be no output listener.
	 */
	public void setOutputListener(ProcessRunnerOutputListener listener) {
		this.outputListener = listener;
	}


	/**
	 * A thread dedicated to reading either the stdout or stderr stream of
	 * an external process.  These streams are read in a dedicated thread
	 * to ensure they are consumed appropriately to prevent deadlock.  This
	 * idea was taken from
	 * <a href="http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps_p.html">
	 * this JavaWorld article</a>.
	 * 
	 * @author Robert Futrell
	 */
	static class StreamReaderThread extends Thread {

		private Process p;
		private BufferedReader r;
		private StringBuilder buffer;
		private ProcessRunnerOutputListener listener;
		private boolean isStdout;

		/**
		 * Constructor.
		 * 
		 * @param p The running process.
		 * @param in The stream (stdout or stderr) to read from.
		 * @param listener A listener to send notification to as output is
		 *        read.  This can be <code>null</code>.
		 * @param isStdout Whether this thread is reading stdout (as opposed
		 *        to stderr).
		 */
		public StreamReaderThread(Process p, InputStream in,
							ProcessRunnerOutputListener listener,
							boolean isStdout) {
			this.p = p;
			r = new BufferedReader(new InputStreamReader(in));
			this.buffer = new StringBuilder();
			this.listener = listener;
			this.isStdout = isStdout;
		}

		/**
		 * Returns the output read from the stream.
		 * 
		 * @return The stream's output, as a <code>String</code>.
		 */
		public String getStreamOutput() {
			return buffer.toString();
		}

		/**
		 * Continually reads from the output stream until this thread is
		 * interrupted.
		 */
		@Override
		public void run() {
			String line;
			try {
				while ((line=r.readLine())!=null) {
					buffer.append(line).append('\n');
					if (listener!=null) {
						listener.outputWritten(p, line, isStdout);
					}
				}
			} catch (IOException ioe) {
				buffer.append("IOException occurred: " + ioe.getMessage());
			}
		}

	}


}