/*
 * 03/11/2005
 *
 * ThirdPartyLookAndFeelManager.java - Class that can read an XML file
 * specifying all 3rd party Look and Feels available to a GUIApplication.
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
package org.fife.ui.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import org.fife.ui.app.ExtendedLookAndFeelInfo;


/**
 * A class capable of reading an XML file specifying 3rd party Look and Feel
 * JAR files, and returning an array of information about the Look and Feels,
 * so your <code>GUIApplication</code> can use them.<p>
 *
 * The XML file read should have the following format:<p>
 * <pre>
 *   &lt;?xml version="1.0"?&gt;
 *   &lt;ThirdPartyLookAndFeels&gt;
 *      &lt;LookAndFeel name="name" class="class" jar="jar"/&gt;
 *      ... other LookAndFeel tags if desired ...
 *   &lt;/ThirdPartyLookAndFeels&gt;
 * </pre>
 *
 * where <code>name</code> is the name of the Look and Feel (as appears in
 * RText's menu), <code>class</code> is the main Look and Feel class, such as
 * <code>org.fife.plaf.OfficeXP.OfficeXPLookAndFeel</code>, and <code>jar</code>
 * is the path to the JAR file containing the Look and Feel, relative to the
 * install location of the specified <code>GUIApplicatoin</code>.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class ThirdPartyLookAndFeelManager {

	private static final String CLASS			= "class";
	private static final String JAR			= "jar";
	private static final String LOOK_AND_FEEL	= "LookAndFeel";
	private static final String NAME			= "name";
	private static final String ROOT_ELEMENT	= "ThirdPartyLookAndFeels";


	/**
	 * Constructor.
	 */
	private ThirdPartyLookAndFeelManager() {
	}


	/**
	 * Returns an array with each element representing a 3rd party Look and
	 * Feel available to your GUI application.
	 *
	 * @param app The GUI application.
	 * @param xmlFile The XML file specifying the 3rd party Look and Feels.
	 * @throws IOException If an error occurs while reading
	 *         <code>xmlFile</code>.
	 */
	public static ExtendedLookAndFeelInfo[] get3rdPartyLookAndFeelInfo(
								GUIApplication app, String xmlFile)
									throws IOException {

		File file = new File(app.getInstallLocation(), xmlFile);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document doc = null;
		try {
			db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new BufferedReader(
					new InputStreamReader(new FileInputStream(file), "UTF-8")));
			is.setEncoding("UTF-8");
			doc = db.parse(is);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			throw fnfe; // So we don't lose the "file not found" part.
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("XML error:  Error parsing file");
		}

		// Traverse the XML tree.
		ArrayList lnfInfo = new ArrayList(1);
		initializeFromXMLFile(doc, lnfInfo);

		// Convert our List into an actual array and return it.
		ExtendedLookAndFeelInfo[] info = new ExtendedLookAndFeelInfo[
												lnfInfo.size()];
		info = (ExtendedLookAndFeelInfo[])lnfInfo.toArray(info);
		return info;

	}


	/**
	 * Used in parsing the XML file containing the 3rd party look and feels.
	 *
	 * @param node The root node of the parsed XML document.
	 * @param lnfInfo An array list of <code>ExtendedLookAndFeelInfo</code>s.
	 * @throws IOException If an error occurs while parsing the XML.
	 */
	private static void initializeFromXMLFile(Node node,
							ArrayList lnfInfo) throws IOException {

		if (node==null)
			throw new IOException("XML error:  node==null!");

		int type = node.getNodeType();
		switch (type) {

			case Node.DOCUMENT_NODE:
				initializeFromXMLFile(
						((Document)node).getDocumentElement(), lnfInfo);
				break;

			// Handle element nodes.
			case Node.ELEMENT_NODE:

				String nodeName = node.getNodeName();

				// Might be the "topmost" node.
				if (nodeName.equals(ROOT_ELEMENT)) {
					NodeList childNodes = node.getChildNodes();
					int childCount = childNodes==null ? 0 :
									childNodes.getLength();
					for (int i=0; i<childCount; i++)
						initializeFromXMLFile(childNodes.item(i),
											lnfInfo);
				}

				// Might be a Look and Feel declaration.
				else if (nodeName.equals(LOOK_AND_FEEL)) {
					// Shouldn't have any children.
					NodeList childNodes = node.getChildNodes();
					if (childNodes!=null && childNodes.getLength()>0) {
						throw new IOException("XML error:  language " +
							"tags shouldn't have children!");
					}
					NamedNodeMap attributes = node.getAttributes();
					if (attributes==null || attributes.getLength()!=3) {
						throw new IOException("XML error: LookAndFeel " +
							"tags should have three attributes!");
					}
					String name = null;
					String className = null;
					String jar = null;
					for (int i=0; i<3; i++) {
						Node node2 = attributes.item(i);
						nodeName = node2.getNodeName();
						if (nodeName.equals(NAME))
							name = node2.getNodeValue();
						else if (nodeName.equals(CLASS))
							className = node2.getNodeValue();
						else if (nodeName.equals(JAR))
							jar = node2.getNodeValue();
						else
							throw new IOException("XML error: unknown " +
								"attribute: '" + nodeName + "'");
					}
					if (name==null || className==null || jar==null) {
						throw new IOException("XML error: LookAndFeel " +
							"must have attributes 'name', 'class' and " +
							"'jar'.");
					}
					lnfInfo.add(new ExtendedLookAndFeelInfo(name,
											className, jar));
				}

				// Anything else is an error.
				else {
					throw new IOException("XML error:  Unknown element " +
						"node: " + nodeName);
				}

				break;

			// Whitespace nodes.
			case Node.TEXT_NODE:
			case Node.COMMENT_NODE:
			case Node.CDATA_SECTION_NODE:
				break;

			// An error occurred?
			default:
				throw new IOException("XML error:  Unknown node type: " +
					type);

		} // End of switch (type).

	}


}