/*
 * 04/13/2005
 *
 * Utilities.java - Utility methods for dealing with RTextFileChooser.
 * Copyright (C) 2005 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public final class Utilities {

	private static final String EXTENSION		= "Extension";
	private static final String EXTENSION_FILTER	= "ExtensionFileFilter";
	private static final String IGNORE_CASE		= "ignoreCase";
	private static final String NAME			= "name";
	private static final String SHOW_EXTENSIONS	= "showExtensions";

	/**
	 * Should only be accessed on the EDT.
	 */
	private static final DateFormat LAST_MODIFIED_DATE_FORMAT =
							new SimpleDateFormat("MM/dd/yyyy hh:mm a");

	/**
	 * Should only be accessed on the EDT.
	 */
	private static NumberFormat fileSizeFormat;

	static {
		fileSizeFormat = NumberFormat.getNumberInstance();
		fileSizeFormat.setGroupingUsed(true);
		fileSizeFormat.setMinimumFractionDigits(0);
		fileSizeFormat.setMaximumFractionDigits(1);
	}


	/**
	 * Private constructor to prevent instantiation.
	 */
	private Utilities() {
		// Do nothing
	}


	/**
	 * <p>
	 * Adds user-defined file filters to a file chooser, as specified in a
	 * file.</p>
	 *
	 * The file should have the following format:
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
	 * be displayed beside the description in the file chooser.
	 *
	 * <p>
	 * The values within <code>Extension</code> tags are the extensions of
	 * files to be accepted by the filter (excluding the initial period).
	 * </p>
	 *
	 * <p>
	 * <code>ignoreCase</code> and <code>showExtensions</code> both have a
	 * default value of <code>true</code>.
	 * </p>
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
			DocumentBuilder db;
			Document doc;
			try {
				db = dbf.newDocumentBuilder();
				InputSource is = new InputSource(new FileReader(file));
				doc = db.parse(is);
			} catch (IOException ioe) {
				throw ioe;
			} catch (Exception e) {
				throw new IOException("XML error:  Error parsing file");
			}
			initializeFromXMLFile(doc.getDocumentElement(), chooser);
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

		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		try (FileChannel source = new FileInputStream(sourceFile).getChannel();
			 	FileChannel destination = new FileOutputStream(destFile).getChannel()) {
			destination.transferFrom(source, 0, source.size());
		}

	}


	/**
	 * Returns whether the system clipboard currently contains one or more
	 * files for pasting.
	 *
	 * @return Whether the current clipboard buffer contains files.
	 */
	public static boolean getClipboardContainsFileList() {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clip.getContents(null);
		DataFlavor accepted = DataFlavor.javaFileListFlavor;
		return contents.isDataFlavorSupported(accepted);
	}


	/**
	 * Returns the extension of a file name.
	 *
	 * @param fileName The file name.
	 * @return The extension, or <code>null</code> if the file name has no
	 *         extension.
	 */
	public static String getExtension(String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		return lastDot>-1 ? fileName.substring(lastDot+1) : null;
	}


	/**
	 * Returns a string representation of a file size, such as "842 bytes",
	 * "1.43 KB" or "3.4 MB".
	 *
	 * @param file The file for which you want its size converted into an
	 *        appropriate string.
	 * @return The string.  Note that this will be invalid if <code>file</code>
	 *         is a directory.
	 */
	public static String getFileSizeStringFor(File file) {
		return getFileSizeStringFor(file.length(), false);
	}


	/**
	 * Returns a string representation of a file size, such as "842 bytes",
	 * "1.43 KB" or "3.4 MB".  This method should only be called on the EDT.
	 *
	 * @param size the size of a file, in bytes.
	 * @param reportInKB Whether to always report the file size in KB, as
	 *        opposed to the largest relevant size.
	 * @return The string.
	 * @see #getFileSizeStringFor(File)
	 */
	public static String getFileSizeStringFor(long size,
										boolean reportInKB) {

		String str;

		if (reportInKB) {
			String suffix = " bytes";
			if (size>=1024) {
				size /= 1024;
				suffix = " KB";
			}
			str = fileSizeFormat.format(size) + suffix;
		}

		else {

			int count = 0;
			double tempSize = size;
			double prevSize = tempSize;

			// Keep dividing by 1024 until you get the largest unit that goes
			// into this file's size.
			while (count<4 && ((tempSize = prevSize/1024f)>=1)) {
				prevSize = tempSize;
				count++;
			}

			String suffix = switch (count) {
				case 0 -> "bytes";
				case 1 -> "KB";
				case 2 -> "MB";
				case 3 -> "GB";
				case 4 -> "TB";
				default -> null;
			};

			str = fileSizeFormat.format(prevSize) + " " + suffix;

		}

		return str;

	}


	/**
	 * Returns a date string for the file's "last modified" time.
	 *
	 * @param date The date, as a <code>long</code>.
	 * @return A string representation of the date.
	 */
	public static String getLastModifiedString(long date) {
		Date date2 = new Date(date);
		return LAST_MODIFIED_DATE_FORMAT.format(date2); // Okay as called on EDT
	}


	/**
	 * Reads user-defined file filters from an XML file and adds them to a
	 * file chooser.  See the description of the XML file format in the
	 * Javadoc for method {@link #addFileFilters}.
	 *
	 * @param root The XML node from which to read.
	 * @param chooser The file chooser to which to add file filters.
	 * @throws IOException If an I/O error occurs.
	 */
	private static void initializeFromXMLFile(Element root,
						RTextFileChooser chooser) throws IOException {

		if (root==null) {
			throw new IOException("XML error:  null root node received!");
		}

		NodeList filterList = root.getElementsByTagName(EXTENSION_FILTER);
		int childCount = filterList==null ? 0 : filterList.getLength();
		for (int i=0; i<childCount; i++) {
			Element filterElem = (Element)filterList.item(i);
			parseExtensionFilterXml(chooser, filterElem);
		}

	}


	/**
	 * Used in reading file filters from an XML file and adding them to a file
	 * chooser.
	 *
	 * @param chooser The file chooser.
	 * @param elem An XML element that specifies a file extension.
	 * @throws IOException If an IO error occurs.
	 */
	private static void parseExtensionFilterXml(RTextFileChooser chooser,
			Element elem) throws IOException {

		// Child elements are the extensions accepted by this filter.
		NodeList extElems = elem.getElementsByTagName(EXTENSION);
		int extElemCount = extElems==null ? 0 : extElems.getLength();
		List<String> extList = new ArrayList<>(extElemCount / 3);

		for (int i=0; i<extElemCount; i++) {
			Node extElem = extElems.item(i);
			String extension = extElem.getTextContent();
			extList.add(extension);
		}

		String[] extensions = new String[extList.size()];
		extensions = extList.toArray(extensions);

		// Get the name, ignore-case, and show-extensions.
		String name = null;
		boolean ignoreCase = true;
		boolean showExtensions = true;

		NamedNodeMap attributes = elem.getAttributes();
		int attributeCount = attributes==null ? 0 : attributes.getLength();
		for (int i=0; i<attributeCount; i++) {
			Node node2 = attributes.item(i);
			String nodeName = node2.getNodeName();
			String nodeValue = node2.getNodeValue();
			switch (nodeName) {
				case NAME -> name = nodeValue;
				case IGNORE_CASE -> ignoreCase = Boolean.parseBoolean(nodeValue);
				case SHOW_EXTENSIONS -> showExtensions = Boolean.parseBoolean(nodeValue);
				default -> throw new IOException("XML error: unknown attribute: '" +
					nodeName + "'");
			}
		}

		chooser.addChoosableFileFilter(
			new ExtensionFileFilter(name, ignoreCase ?
					ExtensionFileFilter.CaseCheck.NO_CASE_CHECK :
					ExtensionFileFilter.CaseCheck.CASE_CHECK,
				showExtensions,
				extensions));

	}


}
