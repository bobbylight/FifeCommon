/*
 * 05/04/2012
 *
 * IOUtil.java - Obligatory IO utility methods.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Obligatory IO utilities.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class IOUtil {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private IOUtil() {
	}


	/**
	 * Reads all text from an input stream.  The stream will be closed when
	 * this method returns.
	 *
	 * @param in The input stream to read from.  Will be closed on return.
	 * @return The text read from the stream.
	 * @throws IOException If an IO error occurs.
	 */
	public static String readFully(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line = null;
		try {
			while ((line=r.readLine())!=null) {
				sb.append(line).append('\n');
			}
		} finally {
			r.close();
		}
		return sb.toString();
	}


}