/*
 * 03/11/2005
 *
 * ThirdPartyLookAndFeelManager.java - Class that can read an XML file
 * specifying all 3rd party Look and Feels available to a GUIApplication.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import org.fife.ui.app.ExtendedLookAndFeelInfo;


/**
 * NOTE: Specifying LookAndFeels in this XML file is done at your own risk!
 * If a LookAndFeel throws an Exception on the EDT, for any reason (needs
 * special configuration not handled by this LAF manager, requires a newer
 * JRE version than is specified in the XML, etc.), it can hose the
 * <code>GUIApplication</code> and keep it from running!<p>
 *
 * A class capable of reading an XML file specifying 3rd party Look and Feel
 * JAR files, and returning an array of information about the Look and Feels,
 * so your <code>GUIApplication</code> can use them.<p>
 *
 * The XML file read should have the following format:<p>
 * <pre>
 *   &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
 *   &lt;ThirdPartyLookAndFeels&gt;
 *      &lt;LookAndFeel name="name" class="class" jars="jar"/&gt;
 *      &lt;LookAndFeel name="name" class="class" jars="jar" minJavaVersion="1.6"/&gt;
 *      ... other LookAndFeel tags if desired ...
 *   &lt;/ThirdPartyLookAndFeels&gt;
 * </pre>
 *
 * where <code>name</code> is the name of the Look and Feel (as appears in
 * RText's menu), <code>class</code> is the main Look and Feel class, such as
 * <code>org.fife.plaf.OfficeXP.OfficeXPLookAndFeel</code>, and
 * <code>jars</code> is the path(s) to the JAR file(s) containing the Look and
 * Feel, relative to the install location of the specified
 * <code>GUIApplication</code>.  The <code>minJavaVersion</code> attribute is
 * optional, and specifies the minimum Java version the JRE must be for the
 * application to offer this LookAndFeel as a choice.  This should be a double
 * value, such as "1.5", "1.6", etc.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ThirdPartyLookAndFeelManager {

	/**
	 * The root directory of the application.
	 */
	private String appRoot;

	private List<ExtendedLookAndFeelInfo> lnfInfo;
	private URLClassLoader lafLoader;

	private static final String CLASS				= "class";
	private static final String JARS				= "jars";
	private static final String DIR					= "dir";
	private static final String LOOK_AND_FEEL		= "LookAndFeel";
	private static final String MIN_JAVA_VERSION	= "minJavaVersion";
	private static final String NAME				= "name";


	/**
	 * Constructor.
	 */
	public ThirdPartyLookAndFeelManager(String appRoot) {

		this.appRoot = appRoot;
		URL[] urls = null;

		lnfInfo = load3rdPartyLookAndFeelInfo("lnfs/lookandfeels.xml");

		try {
			int count = lnfInfo==null ? 0 : lnfInfo.size();
			// 3rd party Look and Feel jars?  Add them to classpath.
			// NOTE:  The lines of code below MUST be in the order they're
			// in or stuff breaks for some reason; I'm not sure why...
			if (count>0) {
				List<URL> lnfJarUrlList = new ArrayList<URL>();
				for (ExtendedLookAndFeelInfo info : lnfInfo) {
					urls = info.getURLs(appRoot);
					for (URL url : urls) {
						if (!lnfJarUrlList.contains(url)) {
							lnfJarUrlList.add(url);
						}
					}
				}
				urls = lnfJarUrlList.toArray(new URL[0]);
			}
		} catch (RuntimeException re) { // FindBugs
			throw re;
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (urls==null) {
			urls = new URL[0];
		}

		// Specifying a parent ClassLoader other than the default hoses some
		// LAFs, such as Substance.
		lafLoader = new URLClassLoader(urls);//this.getClass().getClassLoader());

	}


	/**
	 * Returns an array of information on the JAR files containing 3rd party
	 * Look and Feels.  These jars were dynamically loaded from an XML file
	 * relative to the root directory you gave this manager instance.
	 *
	 * @return An array of URLs for JAR files containing Look and Feels.
	 */
	public ExtendedLookAndFeelInfo[] get3rdPartyLookAndFeelInfo() {
		if (lnfInfo==null) {
			return new ExtendedLookAndFeelInfo[0];
		}
		ExtendedLookAndFeelInfo[] array =
					new ExtendedLookAndFeelInfo[lnfInfo.size()];
		return lnfInfo.toArray(array);
	}


	private static String getJarsFromDirectory(String dirName) {
		StringBuilder sb = new StringBuilder();
		File dir = new File(dirName);
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jar");
			}
		});
		int count = files==null ? 0 : files.length;
		for (int i=0; i<count; i++) {
			if (i>0) {
				sb.append(',');
			}
			sb.append(files[i].getPath());
		}
		//System.out.println("--- " + sb.toString());
		return sb.length()==0 ? null : sb.toString();
	}


	public ClassLoader getLAFClassLoader() {
		return lafLoader;
	}


	/**
	 * Returns an array with each element representing a 3rd party Look and
	 * Feel available to your GUI application.
	 *
	 * @param xmlFile The XML file specifying the 3rd party Look and Feels.
	 * @return A list of {@link ExtendedLookAndFeelInfo}s.
	 */
	private List<ExtendedLookAndFeelInfo>
	load3rdPartyLookAndFeelInfo(String xmlFile) {

		File file = new File(appRoot, xmlFile);
		if (!file.isFile()) {
			return null;
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document doc = null;
		try {
			db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new BufferedReader(
					new InputStreamReader(new FileInputStream(file), "UTF-8")));
			is.setEncoding("UTF-8");
			doc = db.parse(is);
		} catch (RuntimeException re) { // FindBugs
			throw re;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		// Traverse the XML tree.
		List<ExtendedLookAndFeelInfo> lafInfo =
				new ArrayList<ExtendedLookAndFeelInfo>(1);
		try {
			loadFromXML(doc.getDocumentElement(), lafInfo);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return lafInfo;

	}


	/**
	 * Used in parsing the XML file containing the 3rd party look and feels.
	 *
	 * @param node The root node of the parsed XML document.
	 * @param lafInfo An array list of <code>ExtendedLookAndFeelInfo</code>s.
	 * @throws IOException If an error occurs while parsing the XML.
	 */
	private static void loadFromXML(Element root,
			List<ExtendedLookAndFeelInfo> lafInfo) throws IOException {

		if (root==null) {
			throw new IOException("XML error:  node==null!");
		}

		NodeList children = root.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {

			Node child = children.item(i);
			if (child.getNodeType()==Node.ELEMENT_NODE) {

				String elemName = child.getNodeName();

				// Might be a Look and Feel declaration.
				if (LOOK_AND_FEEL.equals(elemName)) {
	
					// Shouldn't have any children.
					NodeList childNodes = child.getChildNodes();
					if (childNodes!=null && childNodes.getLength()>0) {
						throw new IOException("XML error:  LookAndFeel " +
							"tags shouldn't have children!");
					}
					NamedNodeMap attributes = child.getAttributes();
					if (attributes==null || attributes.getLength()<3) {
						throw new IOException("XML error: LookAndFeel " +
							"tags should have three attributes!");
					}
					String name = null;
					String className = null;
					String jars = null;
					double minVersion = 0;
					for (int j=0; j<attributes.getLength(); j++) {
						Node node2 = attributes.item(j);
						String attr = node2.getNodeName();
						if (NAME.equals(attr)) {
							name = node2.getNodeValue();
						}
						else if (CLASS.equals(attr)) {
							className = node2.getNodeValue();
						}
						else if (JARS.equals(attr)) {
							jars = node2.getNodeValue();
						}
						else if (DIR.equals(attr)) {
							jars = getJarsFromDirectory(node2.getNodeValue());
						}
						else if (MIN_JAVA_VERSION.equals(attr)) {
							try {
								minVersion = Double.parseDouble(node2.getNodeValue());
							} catch (NumberFormatException nfe) {
								nfe.printStackTrace();
							}
						}
						else {
							throw new IOException("XML error: unknown " +
								"attribute: '" + attr + "'");
						}
					}
					if (name==null) {
						throw new IOException("lookandfeels.xml: At least one " +
								"LookAndFeel had no 'name' attribute.");
					}
					if (className==null) {
						throw new IOException("lookandfeels.xml: LookandFeel " +
							name + " is missing required 'className' attribute.");
					}
					if (jars==null) {
						throw new IOException("lookandfeels.xml: LookandFeel " +
							name + " is missing required 'jars' or 'dir' attribute.");
					}
					boolean add = true;
					if (minVersion>0) {
						String javaSpecVersion = System.getProperty("java.specification.version");
						try {
							double javaSpecVersionVal = Double.parseDouble(javaSpecVersion);
							add = javaSpecVersionVal >= minVersion;
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
					}
					if (add) {
						lafInfo.add(new ExtendedLookAndFeelInfo(name,
														className, jars));
					}
				}

				// Anything else is an error.
				else {
					throw new IOException("XML error:  Unknown element " +
						"node: " + elemName);
				}

			}

		}

	}


}