/*
 * 04/13/2005
 *
 * Utilities.java - Utility methods for dealing with RTextFileChooser.
 * Copyright (C) 2005 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.ui.rtextfilechooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import org.fife.ui.rtextfilechooser.filters.ExtensionFileFilter;


/**
 * Utility classes for dealing with an <code>RTextFileChooser</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 * @see RTextFileChooser
 */
public class Utilities {

	private static final String EXTENSION		= "Extension";
	private static final String EXTENSION_FILTER	= "ExtensionFileFilter";
	private static final String IGNORE_CASE		= "ignoreCase";
	private static final String NAME			= "name";
	private static final String ROOT_ELEMENT	= "ExtraFileFilters";
	private static final String SHOW_EXTENSIONS	= "showExtensions";

	private static final DateFormat lastModifiedDateFormat =
							new SimpleDateFormat("MM/dd/yyyy hh:mm a");


	/**
	 * Adds user-defined file filters to a file chooser, as specified in a
	 * file.<p>
	 *
	 * The file should have the following format:<p>
	 * <pre>
	 *   &lt;?xml version="1.0"?&gt;
	 *
	 *   &lt;ExtraFileFilters&gt;
	 *      &lt;ExtensionFileFilter name="filter-name" ignoreCase="true|false"
	 *            showExtensions="true|false"&gt;
	 *         &lt;Extension&gt;ext-with-no-dot&lt;/Extension&gt;
	 *         ...
	 *      &lt;/ExtensionFileFilter&gt;
	 *   &lt;/ExtraFileFilters&gt;
	 * </pre>
	 *
	 * where <code>filter-name</code> is the description of the filter as
	 * displayed in the file chooser, <code>ignoreCase</code> designates
	 * whether the filter should ignore case when filtering files, and
	 * <code>showExtensions</code> designates whether the extensions should
	 * be displayed beside the description in the file chooser.<p>
	 *
	 * The values within <code>Extension</code> tags are the extensions of
	 * files to be accepted by the filter (excluding the initial period).<p>
	 *
	 * <code>ignoreCase</code> and <code>showExtensions</code> both have a
	 * default value of <code>true</code>.
	 *
	 * @param file The XML file, as described above.
	 * @param chooser The file chooser to which to add the file filters.
	 * @return Whether the operation was successful.  This will return false
	 *         if the file is not found, for example.
	 * @throws IOException If an I/O error occurs while reading the file.
	 */
	public static boolean addFileFilters(File file,
						RTextFileChooser chooser) throws IOException {

		if (file.exists()) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.
												newInstance();
			DocumentBuilder db = null;
			Document doc = null;
			try {
				db = dbf.newDocumentBuilder();
InputSource is = new InputSource(new FileReader(file));
//				InputSource is = new InputSource(new UnicodeReader(
//							new BufferedInputStream(
//							new FileInputStream(xmlFile)), "UTF-8"));
//				is.setEncoding("UTF-8");
				doc = db.parse(is);
			} catch (IOException ioe) {
				throw ioe;
			} catch (Exception e) {
				throw new IOException("XML error:  Error parsing file");
			}
			initializeFromXMLFile(doc, chooser);
			return true;
		}

		return false; // File did not exist

	}


	/**
	 * Copies a file from one location to another.
	 *
	 * @param sourceFile The file to copy.
	 * @param destFile The location for the new copy.
	 * @throws IOException If an error occurs.
	 */
	public static void copyFile(File sourceFile, File destFile)
									throws IOException {

		if(!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}

	}


	/**
	 * Returns a date string for the file's "last modified" time.
	 *
	 * @param date The date, as a <code>long</code>.
	 * @return A string representation of the date.
	 */
	public static final String getLastModifiedString(long date) {
		Date date2 = new Date(date);
		return lastModifiedDateFormat.format(date2); // Okay as called on EDT
	}


	/**
	 * Reads user-defined file filters from an XML file and adds them to a
	 * file chooser.  See the description of the XML file format in the
	 * Javadoc for method {@link #addFileFilters}.
	 *
	 * @param node The XML node from which to read.
	 * @param chooser The file chooser to which to add file filters.
	 * @throws IOException If an I/O error occurs.
	 */
	private static void initializeFromXMLFile(Node node,
						RTextFileChooser chooser) throws IOException {

		if (node==null)
			throw new IOException("XML error:  node==null!");

		int type = node.getNodeType();
		switch (type) {

			// The document node is ???
			case Node.DOCUMENT_NODE:
				initializeFromXMLFile(
							((Document)node).getDocumentElement(),
							chooser);
				break;

			// Handle element nodes.
			case Node.ELEMENT_NODE:

				String nodeName = node.getNodeName();

				// Might be the "topmost" node.
				if (nodeName.equals(ROOT_ELEMENT)) {
					NodeList childNodes = node.getChildNodes();
					int childCount = childNodes==null ? 0 :
											childNodes.getLength();
					for (int i=0; i<childCount; i++) {
						Node n = childNodes.item(i);
						initializeFromXMLFile(n, chooser);
					}
				}

				// Might be a filter definition
				else if (nodeName.equals(EXTENSION_FILTER)) {
					// Children are the extensions accepted by
					// this filter (or whitespace nodes).
					NodeList childNodes = node.getChildNodes();
					int childCount = childNodes==null ? 0 :
									childNodes.getLength();
					ArrayList extList = new ArrayList(childCount/3);
					for (int i=0; i<childCount; i++) {
						Node node2 = childNodes.item(i);
						if (EXTENSION.equals(node2.getNodeName())) {
							NodeList cn2 = node2.getChildNodes();
							if (cn2==null || cn2.getLength()==0)
								throw new IOException("No child " +
									"nodes for Extension tag");
							int childCount2 = cn2.getLength();
							for (int j=0; j<childCount2; j++) {
								Node n3 = cn2.item(j);
								if (n3.getNodeType()==Node.TEXT_NODE)
									extList.add(n3.getNodeValue());
							}
						}
					}
					String[] extensions = new String[extList.size()];
					extensions = (String[])extList.toArray(extensions);
					// name, ignore-case, and show-extensions.
					String name = null;
					boolean ignoreCase = true;
					boolean showExtensions = true;
					NamedNodeMap attributes = node.getAttributes();
					int attributeCount = attributes==null ? 0 :
										attributes.getLength();
					for (int i=0; i<attributeCount; i++) {
						Node node2 = attributes.item(i);
						nodeName = node2.getNodeName();
						String nodeValue = node2.getNodeValue();
						if (nodeName.equals(NAME))
							name = nodeValue;
						else if (nodeName.equals(IGNORE_CASE))
							ignoreCase = Boolean.valueOf(nodeValue).
												booleanValue();
						else if (nodeName.equals(SHOW_EXTENSIONS))
							showExtensions = Boolean.valueOf(nodeValue).
												booleanValue();
						else
							throw new IOException("XML error: unknown " +
								"attribute: '" + nodeName + "'");
					}
					chooser.addChoosableFileFilter(
						new ExtensionFileFilter(name, extensions,
							ignoreCase ?
									ExtensionFileFilter.NO_CASE_CHECK :
									ExtensionFileFilter.CASE_CHECK,
							showExtensions));
				}

				// Anything else is an error.
				else {
					throw new IOException("XML error:  Unknown element " +
						"node: " + nodeName);
				}

				break;

			// Whitespace nodes.
			case Node.TEXT_NODE:
				break;

			// An error occurred?
			default:
				throw new IOException("XML error:  Unknown node type: " +
					type);

		} // End of switch (type).

	}


}