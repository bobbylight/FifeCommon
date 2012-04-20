/*
 * 02/14/2006
 *
 * GUIWorkerThread.java - Thread to do non-GUI-related work so an
 * application's GUI remains responsive.
 * Copyright (C) 2006 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import javax.swing.SwingUtilities;


/**
 * This is a helper class that will do non-GUI-related work in a separate,
 * dedicated thread, to keep the GUI responsive while doing CPU-heavy
 * tasks.<p>
 *
 * This class is a direct ripoff of SwingWorker 3.
 *
 */
public abstract class GUIWorkerThread {

	/**
	 * The value we are creating; for example, if you set up this thread to
	 * load a large image to keep the GUI responsive, this value could be
	 * the image loaded.  You may not use this parameter in your subclass.
	 */
	private Object value;

	/**
	 * Keeps track internally of the thread that's doing our dirty work.
	 */
	private ThreadVar threadVar;


	/**
	 * Start a thread that will call the <code>construct</code> method
	 * and then exit.  You need to call the <code>start</code> method on
	 * this newly-created worker thread next.
	 */
	public GUIWorkerThread() {
		reset();
	}


	/** 
	 * Compute the value to be returned by the <code>get</code> method.  This
	 * method should be overridden to do all the CPU-intensive stuff that
	 * you don't want to do in the event dispatch thread.
	 */
	public abstract Object construct();


	/**
	 * Called on the event dispatching thread (not on the worker thread)
	 * after the <code>construct</code> method has returned.<p>
	 *
	 * This method should be overridden to do any work with the value
	 * returned from <code>get</code> to prevent deadlock.
	 */
	public void finished() {
	}


	/**
	 * Return the value created by the <code>construct</code> method.  
	 * Returns null if either the constructing thread or the current
	 * thread was interrupted before a value was produced.
	 *
	 * Note that this method could cause deadlock if it is called outside
	 * of the <code>finished</code> method.
	 * 
	 * @return The value created by the <code>construct</code> method.
	 */
	public Object get() {
		while (true) {
			Thread t = threadVar.get();
			if (t == null)
				return getValue();
			try {
				t.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // propagate
				return null;
			}
		}
	}


	/** 
	 * Get the value produced by the worker thread, or null if it 
	 * hasn't been constructed yet.
	 */
	protected synchronized Object getValue() { 
		return value; 
	}


	/**
	 * Call this method to force the worker to stop what it's doing.
	 */
	public void interrupt() {
		Thread t = threadVar.get();
		if (t != null)
			t.interrupt();
		threadVar.clear();
	}


	/**
	 * "Resets" this class so its <code>start</code> method can be called a
	 * second time.  If this thread completes (either normally or after you
	 * call <code>interrupt</code> on it), you must call this method before
	 * calling <code>start</code> again.
	 */
	public void reset() {

		// Create a thread to call the finished() method on the
		// event-dispatch thread after our computations are done.
		final Runnable doFinished = new Runnable() {
								public void run() {
									finished();
								}
							};

		// Create the thread that will actually do the dirty work and then
		// set up our doFinished thread to run on the event-dispatch thread.
		Runnable doConstruct = new Runnable() { 
					public void run() {
						try {
							setValue(construct());
						} finally {
							threadVar.clear();
						}
						SwingUtilities.invokeLater(doFinished);
					}
		};

		Thread t = new Thread(doConstruct);
		threadVar = new ThreadVar(t);

	}


	/** 
	 * Set the value produced by worker thread 
	 */
	private synchronized void setValue(Object x) { 
		value = x; 
	}


	/**
	 * Start the worker thread.
	 */
	public void start() {
		Thread t = threadVar.get();
		if (t != null)
			t.start();
	}


	/** 
	 * Class to maintain reference to current worker thread
	 * under separate synchronization control.
	 */
	private static class ThreadVar {
		private Thread thread;
		ThreadVar(Thread t) { thread = t; }
		synchronized Thread get() { return thread; }
		synchronized void clear() { thread = null; }
	}


}