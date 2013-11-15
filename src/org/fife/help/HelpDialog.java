/*
 * 11/14/2003
 *
 * HelpDialog.java - A "Help" dialog box for use in Java applications.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.help;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.html.HTMLDocument;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import org.fife.ui.RListSelectionModel;
import org.fife.ui.RScrollPane;
import org.fife.ui.RTreeSelectionModel;
import org.fife.ui.StatusBar;
import org.fife.ui.UIUtil;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.app.GUIApplication;


/**
 * A help dialog similar to those found in most Microsoft Windows programs.
 * The dialog contains a pane with Contents, Index, and Search tabs, each
 * of which yields the expected way to find help.  Help pages should be
 * HTML.<p>
 * 
 * Features of <code>HelpDialog</code> include:
 * <ul>
 *    <li>Contents/Index/Search pane - Works just like most Windows programs'
 *        Contents/Index/Search.  Pick your favorite way to find help.</li>
 *    <li>Search/Index options automatically find all help documents (which you
 *        pass to HelpDialog) containing the word/phrase for which the user
 *        wants to find help.</li>
 *    <li>History - The HelpDialog class will remember the help documents
 *        the user has previously visited, and the user can use the Back and
 *        Forward buttons to easily traverse previous help.
 *    <li>HTML Help - Help documents are HTML, to allow for easy formatting.
 *        </li>
 * </ul>
 *
 * Notes:
 * <ul>
 *    <li>In your HTML documentation, anchors can be used as hyperlinks;
 *    however, if you have a hyperlink that is simply an anchor to another place
 *    in the current HTML file (<code>a href="#anchor"</code> as opposed to
 *    <code>a href="file2.html#anchor"</code>), you need to explicitly state
 *    that the anchor is in the current file (i.e.,
 *    <code>a href="file2.html#anchor"</code>) for this class to parse it
 *    correctly.</li>
 *    <li>If the specified URL starts with <code>http://</code> and we're
 *    running in a Java 6 or newer JVM, the link is opened in the system
 *    default web browser.</li>
 * </ul>
 *
 * @author Robert Futrell
 * @version 1.1
 */
public class HelpDialog extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private GUIApplication app;

	private JTabbedPane tabbedPane;
	private DefaultMutableTreeNode root;
	private JToolBar toolBar;

	private JTree tocTree;				// Tree laying out the table of contents.
	private boolean rootVisible;

	private JEditorPane editorPane;		// Right-hand pane; displays actual help.

	private JList indexList;				// List of all elements in our index.
	private JButton indexDisplayButton;	// Display button on the "Index" tab.
	private JTextField indexField;		// Text field in which they can type in an index value.

	private JList searchList;			// List of all found documents on Search panel.
	private JButton searchDisplayButton;	// Display button on the "Search" tab.
	private JButton listTopicsButton;		// Button to display help topics matching searchString.
	private JTextField searchField;		// Text field to type a string to search for.

	private JButton backButton;
	private JButton forwardButton;

	private String searchString;		// What to look for in the search tab.
	private boolean highlightSearchString;

	private List<HelpTreeNode> history;		// All HelpTreeNodes the user has previously viewed.
	private int historyPos;			// Position in history array we're at.
	private boolean updateHistory;	// If true, selecting a tocTree node updates history.

	private boolean clickedOnTOCTree;	// Whether they clicked on tocTree to go to a help page.

	private JLabel keywordToFindLabel;
	private JLabel keywordToFindLabel2;
	private JLabel topicToDisplayLabel;

	private String baseDir;
	private URL baseURL;
	private String noMatchHTML; // Probably never used.

	private boolean webUrlsInRealBrowser;

	private ResourceBundle treeBundle;

	private String[] indexElements;

	private static final String HTML_TYPE		= "text/html";
	private static final String TEXT_TYPE		= "text/plain";

	// tags used in nodes.
	private static final String INDEXITEMS		= "IndexItems";
	private static final String NAME			= "name";
	private static final String PAGE			= "Page";
	private static final String PAGE_VALUE		= "page";
	private static final String PROPERTIES_FILE	= "PropertiesFile";
	private static final String ROOT_ELEMENT	= "HelpDialogContents";
	private static final String TREE_NODE		= "Node";
	private static final String TREE_ROOT_NODE	= "RootNode";
	private static final String VISIBLE		= "visible";


	/**
	 * Creates a new help dialog.
	 *
	 * @param owner The frame that owns this dialog.
	 * @param contentsFile The XML file specifying the structure of this
	 *        online help.
	 * @param baseDir The directory in which all help stuff is stored.  This
	 *        will also be used as the base directory for relative links in
	 *        the HTML of the online help.
	 */
	public HelpDialog(GUIApplication owner, String contentsFile,
										String baseDir) {

		// Make the title of the dialog appropriate.
		this.app = owner;

		Border border8080 = BorderFactory.createEmptyBorder(8,0,8,0);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());
		boolean ltr = orientation.isLeftToRight();

		// Create some stuff we'll need below.
		ResourceBundle msg = getHelpBundle();
		HelpListener listener = new HelpListener();
		webUrlsInRealBrowser = true;

		noMatchHTML = "<html><body><h2>" + msg.getString("NoMatch") +
				"</h2><HR ALIGN=\"center\" WIDTH=\"100%\"></body></html>";

		// Set the base URL.
		this.baseDir = baseDir;
		try {
			setBaseURL(new File(baseDir).toURI().toURL());
		} catch (Exception e) {
			app.displayException(this, e);
		}

		// Create the DefaultMutableTreeNode tree that will be our "tree" of
		// help pages.
		createRoot(contentsFile);

		// Make a text area for the right-component of the split pane (the HTML help).
		editorPane = new JEditorPane();
		editorPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
		editorPane.setPreferredSize(new Dimension(81*8,300));
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(listener);
		editorPane.setContentType("text/html");
		editorPane.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);

		// Create contents subpanel for the left-component of the split pane.
		JPanel tocPanel = UIUtil.newTabbedPanePanel();
		tocPanel.setLayout(new BoxLayout(tocPanel, BoxLayout.Y_AXIS));
		tocPanel.setBorder(UIUtil.getEmpty5Border());
		tocTree = new JTree(root);
		tocTree.setRootVisible(rootVisible);	// Set in initializeFromXMLFile().
		tocTree.setSelectionModel(new RTreeSelectionModel());
		//tocTree.setToggleClickCount(1);
		tocTree.addTreeSelectionListener(listener);
		tocTree.addKeyListener(listener);
		JScrollPane scrollPane = new RScrollPane(1,1, tocTree);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		tocPanel.add(scrollPane);

		// Create the index subpanel for the left-component of the split pane.
		JPanel indexPanel = UIUtil.newTabbedPanePanel();
		indexPanel.setLayout(new BorderLayout());
		indexPanel.setBorder(UIUtil.getEmpty5Border());
		JPanel indexInputPanel = UIUtil.newTabbedPanePanel();
		indexInputPanel.setLayout(new BoxLayout(indexInputPanel, BoxLayout.Y_AXIS));
		keywordToFindLabel = new JLabel(msg.getString("KeywordLabel"));
		JPanel ilPanel = UIUtil.newTabbedPanePanel();
		ilPanel.setLayout(new BorderLayout());
		ilPanel.add(keywordToFindLabel, BorderLayout.LINE_START);
		indexField = new JTextField();
		indexField.setMaximumSize(new Dimension(3000,20));
		indexField.getDocument().addDocumentListener(listener);
		indexField.addKeyListener(listener);
		indexInputPanel.add(ilPanel);
		indexInputPanel.add(indexField);
		indexList = new JList(indexElements);
		indexElements = null;
		indexList.addMouseListener(listener);
		indexList.addKeyListener(listener);
		indexList.setSelectionModel(new RListSelectionModel());
		JScrollPane indexScrollPane = new RScrollPane(1,1, indexList);
		indexScrollPane.setPreferredSize(new Dimension(100,200));
		indexScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel indexButtonPanel = UIUtil.newTabbedPanePanel();
		indexButtonPanel.setLayout(new BoxLayout(indexButtonPanel, BoxLayout.LINE_AXIS));
		indexDisplayButton = UIUtil.newTabbedPaneButton(msg.getString("Display"));
		indexDisplayButton.setActionCommand("Display");
		indexDisplayButton.addActionListener(this);
		indexButtonPanel.add(Box.createHorizontalGlue());
		indexButtonPanel.add(indexDisplayButton);
		indexPanel.add(indexInputPanel, BorderLayout.NORTH);
		JPanel temp = UIUtil.newTabbedPanePanel();
		temp.setLayout(new BorderLayout());
		temp.setBorder(border8080);
		temp.add(indexScrollPane);
		indexPanel.add(temp);
		indexPanel.add(indexButtonPanel, BorderLayout.SOUTH);
		indexList.setSelectedIndex(0);

		// Create the search subpanel for the left-component of the split pane.
		JPanel searchPanel = UIUtil.newTabbedPanePanel();
		searchPanel.setLayout(new BorderLayout());
		searchPanel.setBorder(UIUtil.getEmpty5Border());
		JPanel searchInputPanel = UIUtil.newTabbedPanePanel();
		searchInputPanel.setLayout(new BoxLayout(searchInputPanel, BoxLayout.Y_AXIS));
		keywordToFindLabel2 = new JLabel(msg.getString("KeywordLabel"));
		JPanel slPanel = UIUtil.newTabbedPanePanel();
		slPanel.setLayout(new BorderLayout());
		slPanel.add(keywordToFindLabel2, BorderLayout.LINE_START);
		searchField = new JTextField();
		searchField.setMaximumSize(new Dimension(3000,20));
		searchField.getDocument().addDocumentListener(listener);
		searchField.addKeyListener(listener);
		listTopicsButton = UIUtil.newTabbedPaneButton(msg.getString("ListTopics"));
		listTopicsButton.setActionCommand("ListTopics");
		listTopicsButton.addActionListener(this);
		listTopicsButton.setEnabled(false);
		JPanel ltbPanel = UIUtil.newTabbedPanePanel();
		ltbPanel.setLayout(new BoxLayout(ltbPanel, BoxLayout.LINE_AXIS));
		ltbPanel.add(Box.createHorizontalGlue());
		ltbPanel.add(listTopicsButton);
		topicToDisplayLabel = new JLabel(msg.getString("TopicToDisplay"));
		JPanel stlPanel = UIUtil.newTabbedPanePanel();
		stlPanel.setLayout(new BorderLayout());
		stlPanel.add(topicToDisplayLabel, BorderLayout.LINE_START);
		searchInputPanel.add(slPanel);
		searchInputPanel.add(searchField);
		searchInputPanel.add(Box.createVerticalStrut(5));
		searchInputPanel.add(ltbPanel);
		searchInputPanel.add(Box.createVerticalStrut(5));
		searchInputPanel.add(stlPanel);
		searchList = new JList();
		searchList.addMouseListener(listener);
		searchList.addKeyListener(listener);
		searchList.setSelectionModel(new RListSelectionModel());
		JScrollPane searchScrollPane = new RScrollPane(1,1, searchList);
		searchScrollPane.setPreferredSize(new Dimension(100,200));
		searchScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel searchButtonPanel = UIUtil.newTabbedPanePanel();
		searchButtonPanel.setLayout(new BoxLayout(searchButtonPanel, BoxLayout.LINE_AXIS));
		searchDisplayButton = UIUtil.newTabbedPaneButton(msg.getString("Display"));
		searchDisplayButton.setActionCommand("Display");
		searchDisplayButton.addActionListener(this);
		searchDisplayButton.setEnabled(false);
		searchButtonPanel.add(Box.createHorizontalGlue());
		searchButtonPanel.add(searchDisplayButton);
		searchPanel.add(searchInputPanel, BorderLayout.NORTH);
		temp = UIUtil.newTabbedPanePanel();
		temp.setLayout(new BorderLayout());
		temp.setBorder(border8080);
		temp.add(searchScrollPane);
		searchPanel.add(temp);
		searchPanel.add(searchButtonPanel, BorderLayout.SOUTH);

		// Put the subpanels for the left frame into a tabbed pane.
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(listener);
		tabbedPane.addTab(msg.getString("Contents"), tocPanel);
		tabbedPane.addTab(msg.getString("Index"), indexPanel);
		tabbedPane.addTab(msg.getString("Search"), searchPanel);

		// Create the Help dialog's left panel.
		JPanel leftPanel = new JPanel(new GridLayout(1,1));
		leftPanel.setPreferredSize(new Dimension(300,400));
		leftPanel.add(tabbedPane);

		// Create the split pane.
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		JScrollPane rightScrollPane = new RScrollPane(400,200, editorPane);
		rightScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		if (ltr) {
			splitPane.setLeftComponent(leftPanel);
			splitPane.setRightComponent(rightScrollPane);
		}
		else {
			splitPane.setLeftComponent(rightScrollPane);
			splitPane.setRightComponent(leftPanel);
		}

		// Make the toolbar.
		toolBar = new JToolBar() {
			@Override
			public void updateUI() {
				super.updateUI();
				WebLookAndFeelUtils.fixToolbar(this);
			}
		};
		backButton = UIUtil.newButton(msg, "Back", this);
		backButton.setActionCommand("Back");
		backButton.setEnabled(false);
		toolBar.add(backButton);
		forwardButton = UIUtil.newButton(msg, "Forward", this);
		forwardButton.setActionCommand("Forward");
		forwardButton.setEnabled(false);
		toolBar.add(forwardButton);
		toolBar.setFloatable(false);
		WebLookAndFeelUtils.fixToolbar(toolBar);

		// Make our help dialog!
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(splitPane, BorderLayout.CENTER);
		contentPane.add(new StatusBar(""), BorderLayout.SOUTH);
		pack();

		// Initially, the use hasn't viewed any nodes.
		history = new ArrayList<HelpTreeNode>();
		updateHistory = true;
		clickedOnTOCTree = true;		// Initially, we assume they click on tocTree first.
		historyPos = -1;				// Initially, there is no history to point to.

		// By default, our tree is open to the first node.
		// This will select it in the tocTree and bring up first help page.
		tocTree.setSelectionRow(0);

		// We must reset backButton to be disabled, as setSelectionRow enables it.
		backButton.setEnabled(false);

		// Get ready to go!
		setTitle(msg.getString("Title"));
		msg = null; // May help in GC.
		setIconImage(owner.getIconImage());
		applyComponentOrientation(orientation);
		pack();

	}


	// Callback for when an action occurs.
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();
		String actionCommand = e.getActionCommand();

		// If they click on the "Display" button on the index tab, show
		// the help they selected.
		if (source.equals( indexDisplayButton )) {
			loadSelectedHelpPageIndex();
		}

		// If they click on the "Display" button on the search tab, show the
		// help they selected.
		else if (source.equals( searchDisplayButton )) {
			loadSelectedHelpPageSearch();
		}

		// If they click on the "List Topics" button on the search tab,
		// show matching topics.
		else if (actionCommand.equals("ListTopics")) {
			searchString = searchField.getText();		// Remember the searched-for text.
			populateSearchList();
		}

		else if (actionCommand.equals("Back")) {

			// No, we don't want history updated (we've already seen the
			// coming page).
			updateHistory = false;
			highlightSearchString = false;

			// Increment the "position" in history and update the page.
			if (historyPos>0) {
				historyPos--;
				setHelpPageURL(history.get(historyPos).url);
			}
			else {
				// Root must be visible but not contain a page.  So we'll
				// just select the root.
				setHelpPageURL(null);
			}

			// If they've gone backward to the last page, they can't go
			// back any further.
			if (historyPos==0)
				backButton.setEnabled(false);

			// They can go forward if there are pages "ahead" of this one.
			if (historyPos < history.size()-1)
				forwardButton.setEnabled(true);

		}

		else if (actionCommand.equals("Forward")) {

			// No, you don't want history updated (we've already seen the
			// coming page).
			updateHistory = false;
			highlightSearchString = false;

			// Increment the "position" in history and update the page.
			historyPos++;
			setHelpPageURL(history.get(historyPos).url);

			// If they've gone forward to the final page, they can't go
			// forward any longer.
			if (historyPos==history.size()-1)
				forwardButton.setEnabled(false);

			// They can go backward if there are pages "behind" this one.
			if (historyPos>0)
				backButton.setEnabled(true);

		}

	}


	/**
	 * Creates the tree of help we'll be displaying and stores the root in
	 * <code>root</code>.
	 */
	private void createRoot(String helpXMLFile) {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		org.w3c.dom.Document doc = null;
		try {
			db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new FileReader(helpXMLFile));
		//	InputSource is = new InputSource(new UnicodeReader(
		//						new FileInputStream(file), "UTF-8"));
		//	is.setEncoding("UTF-8");
			doc = db.parse(is);//db.parse(file);
		} catch (Exception ex) {
			app.displayException(this, ex);
			return;
		}

		root = initializeFromXMLFile(doc.getDocumentElement());

	}


	/**
	 * Returns a list of all nodes under <code>root</code>'s tree
	 * with URL's containing <code>searchString</code>.
	 */
	private List<HelpTreeNode> getTreeNodesContaining(
			DefaultMutableTreeNode root, String searchString) {

		// Our return value.
		List<HelpTreeNode> arrayList =
				new ArrayList<HelpTreeNode>();

		// Loop through all children of root.
		int count = root.getChildCount();
		String searchStringLower = searchString.toLowerCase();
		for (int i=0; i<count; i++) {

			// Get the current child.
			DefaultMutableTreeNode child =
							(DefaultMutableTreeNode)root.getChildAt(i);

			// Search for searchString in its text.
			// Strip the HTML tags out of this line so they can't search for
			// like "HTML" and have everything be a match.
			HelpTreeNode node = (HelpTreeNode)child.getUserObject();
			String contents = getContents(node); // will be non-null.
			contents = contents.replaceAll("<[^>]*>", "");
			if (contents.toLowerCase().indexOf(searchStringLower) != -1)
				arrayList.add(node);

			// If this node has children, we must search them too for
			// searchString.
			if (child.getChildCount()>0) {
				List<HelpTreeNode> temp = getTreeNodesContaining(child, searchString);
				if (temp.size() > 0)
					arrayList.addAll(temp);
			}

		} // End of for (int i=0; i<count; i++).

		// We have our list of URL's containing searchString, so return it.
		return arrayList;

	}


	/**
	 * Returns the contents of the HelpTreeNode's URL as a String,
	 * highlighting all occurrences of the String searchString if desired.
	 *
	 * @param node The node from whose URL you wish to get the contents.
	 * @return The contents.  If the URL was invalid or an
	 *         <code>Exception</code> was thrown, an empty string is
	 *         returned.
	 */
	private String getContents(HelpTreeNode node) {

		// The String that will hole the text in the file.
		String contents = "";

		// node.url may be null if we're in a branch node (or may not).
		if (node!=null && node.url!=null) {

			// Read in the file.
			try {

				BufferedReader in = new BufferedReader(
					new InputStreamReader(node.url.openConnection().
												getInputStream()));
				try {
					contents = HelpDialog.read(in);
				} finally {
					in.close();
				}

			} catch (IOException e) {
				app.displayException(this, e);
			}

			// Highlight all occurrences of searchString if desired.
			if (highlightSearchString) {

				// Keep highlighting the searched-for text until the end of
				// the string is reached.
				int searchStringLength = searchString.length();
				String searchStringLower = searchString.toLowerCase();
				int pos = contents.toLowerCase().indexOf(searchStringLower);
				while (pos != -1) {

					// An attempt at seeing if found text is in a tag.  If
					// so, skip it.  We assume that this is proper HTML
					// with &gt; and &lt; used where necessary.
					int gtPos = contents.indexOf(">", pos);
					if (gtPos<contents.indexOf("<", pos)) {
						pos = contents.toLowerCase().indexOf(searchStringLower, gtPos+1);
						continue;
					}

					// Otherwise, insert HTML tag to "highlight" found text in yellow.
					int tempEnd = pos + searchStringLength;
					contents = contents.substring(0, pos) + "<font bgcolor=\"#FFFF00\">"
							+ contents.substring(pos, tempEnd) + "</font>"
							+ contents.substring(tempEnd, contents.length());
					int startPos = pos + searchStringLength +
								"<font bgcolor=\"#FFFF00\">".length() +
								"</font>".length();
					pos = contents.toLowerCase().indexOf(searchStringLower, startPos);

				}

			} // End of if (highlightSearchString).

		} // End of if (node!=null && node.url!=null).

		// Return whatever of the file (if any) you got.
		return contents;

	}


	/**
	 * Returns the label on the "Contents" tab.
 	 *
	 * @return The text on the "Contents" tab.
	 * @see #setContentsTabText
	 */
	public final String getContentsTabText() {
		return tabbedPane.getTitleAt(0);
	}


	/**
	 * Returns the label on the "Display" button on the Index and Search
	 * panels.
 	 *
	 * @return The text on the "Display" buttons.
	 * @see #setDisplayButtonText
	 */
	public final String getDisplayButtonText() {
		return indexDisplayButton.getText();
	}


	/**
	 * Returns the resource bundle for the help dialog.
	 *
	 * @return The resource bundle.
	 */
	private final ResourceBundle getHelpBundle() {
		return ResourceBundle.getBundle(
				"org.fife.help.HelpDialog", getLocale());
	}


	/**
	 * Returns the label on the "Index" tab.
 	 *
	 * @return The text on the "Index" tab.
	 * @see #setIndexTabText
	 */
	public final String getIndexTabText() {
		return tabbedPane.getTitleAt(1);
	}


	/**
	 * Returns the text label above the "Type in a keyword to find" text boxes.
	 *
	 * @return The text label.
	 * @see #setKeywordFieldLabelText
	 */
	public final String getKeywordFieldLabelText() {
		// Arbitrarily one of this and the "2" brother.
		return keywordToFindLabel.getText();
	}


	/**
	 * Returns the label on the "List Topics" button on the Search panels.
 	 *
	 * @return The text on the "List Topics" button.
	 * @see #setListTopicsButtonText
	 */
	public final String getListTopicsButtonText() {
		return listTopicsButton.getText();
	}


	/**
	 * Returns whether URLs starting with "<code>http://</code>" should be
	 * opened in a real web browser (assuming Java 6), instead of this Help
	 * dialog.  The default value of this property is <code>true</code>, since
	 * it's assumed this is usually done to go to project home pages or more
	 * extensive online documentation, and JEditorPane can't really handle
	 * "real" HTML.
	 *
	 * @return Whether URL's starting with "<code>http://</code>" are opened in
	 *         a real browser.
	 * @see #setOpenWebUrlsInRealBrowser(boolean)
	 */
	public boolean getOpenWebUrlsInRealBrowser() {
		return webUrlsInRealBrowser;
	}


	/**
	 * Returns the label on the "Search" tab.
 	 *
	 * @return The text on the "Search" tab.
	 * @see #setSearchTabText
	 */
	public final String getSearchTabText() {
		return tabbedPane.getTitleAt(2);
	}


	/**
	 * Returns the text label above the "Select topic to display" text box.
	 *
	 * @return The text label.
	 * @see #setTopicToDisplayFieldLabelText
	 */
	public final String getTopicToDisplayFieldLabelText() {
		return topicToDisplayLabel.getText();
	}


	/**
	 * Takes a stab at the content type (text/html vs. text/plain) for
	 * the file specified by the given path.  This is used to figure out
	 * whether the help browser should display a page as text or HTML.
	 *
	 * @param path The path to the file for which to guess the content type.
	 * @return The content type.
	 */
	private static final String guessContentType(String path) {
		if (path!=null) {
			String lower = path.toLowerCase();
			if (lower.endsWith("html") || lower.endsWith("htm") ||
					lower.endsWith("jsp"))
				return HTML_TYPE;
		}
		return TEXT_TYPE; // Assume plain text.
	}


	/**
	 * Handles a {@link #PAGE} node.
	 *
	 * @param node The node to parse.
	 * @return The tree node to add.
	 */
	private DefaultMutableTreeNode handleNodePage(Node node) {

		NamedNodeMap attributes = node.getAttributes();
		if (attributes==null || attributes.getLength()!=2)
			return null;

		String name = null;
		String file = null;
		for (int i=0; i<2; i++) {
			Node node2 = attributes.item(i);
			String v = node2.getNodeValue();
			if (node2.getNodeName().equals(NAME))
				name = treeBundle.getString(v);
			else if (node2.getNodeName().equals(PAGE_VALUE))
				file = baseDir + v;
		}

		if (name==null || file==null)
			return null;

		HelpTreeNode tempNode = new HelpTreeNode(name, file);

		return new DefaultMutableTreeNode(tempNode);

	}


	/**
	 * Handles an {@link #INDEXITEMS} node.
	 *
	 * @param node The node.
	 * @return The tree node to add.
	 */
	private DefaultMutableTreeNode handleNodeIndexItems(Node node) {

		NodeList childNodes = node.getChildNodes();

		if (childNodes!=null) {
			int length = childNodes.getLength();
			// Cache to avoid all of the text (whitespace) elements.
			List<String> elements = new ArrayList<String>(length/2);
			for (int i=0; i<length; i++) {
				Node node2 = childNodes.item(i);
				if (node2.getNodeType()==Node.TEXT_NODE)
					continue; // Whitespace between elements.
				NamedNodeMap attributes = node2.getAttributes();
				if (attributes==null || attributes.getLength()!=1)
					return null;
				node2 = attributes.item(0);
				if (!validateAttributeNode(node2, NAME))
					continue;
				String name = node2.getNodeValue();
				elements.add(name);
			}
			int size = elements.size();
			indexElements = elements.toArray(new String[size]);
		}

		return null;

	}


	/**
	 * Parses {@link #PROPERTIES_FILE} nodes.
	 *
	 * @param node The node.
	 * @return <code>null</code> always.
	 */
	private DefaultMutableTreeNode handleNodePropertiesFile(Node node) {

		NamedNodeMap attributes = node.getAttributes();
		if (attributes==null || attributes.getLength()!=1)
			return null;

		Node node2 = attributes.item(0);
		if (!validateAttributeNode(node2, NAME))
			return null;

		String name = node2.getNodeValue();
		try {
			treeBundle = new PropertyResourceBundle(
					new FileInputStream(baseDir + name));
		} catch (Exception e) {
			// Keep app as owner as this dialog isn't yet displayable
			app.displayException(e);
			return null;
		}

		return null;

	}


	/**
	 * Used in parsing an XML document containing a macro.  This method
	 * initializes the help tree and index, as well as sets the "root
	 * visible" property of the help tree.
	 *
	 * @param node The root node of the parsed XML document.
	 * @return The root node of the help tree (<code>JTree</code>).
	 */
	private DefaultMutableTreeNode initializeFromXMLFile(Node node) {

		if (node==null) {
			return null;
		}

		int type = node.getNodeType();
		if (type==Node.ELEMENT_NODE) { // We only handle elements

			String nodeName = node.getNodeName();

			// This is the root of the entire XML file.  What we do
			// here is loop through its children (which should be an
			// IndexList element, a PropertiesFile element and a
			// RootNode element).
			if (nodeName.equals(ROOT_ELEMENT)) {
				NodeList childNodes = node.getChildNodes();
				if (childNodes!=null) {
					int length = childNodes.getLength();
					for (int i=0; i<length; i++) {
						initializeFromXMLFile(childNodes.item(i));
					}
				}
				treeBundle = null; // To help GC.
				return root;
			}

			// The first element in the XML file should tell where the
			// properties file is that maps tree node keys to their
			// corresponding (localized) strings.
			else if (nodeName.equals(PROPERTIES_FILE)) {
				return handleNodePropertiesFile(node);
			}

			// This is the "RootNode" element in the XML file; that is,
			// the node that details the tree structure for the help.
			else if (nodeName.equals(TREE_ROOT_NODE)) {

				NamedNodeMap attributes = node.getAttributes();
				if (attributes==null || attributes.getLength()<2)
					return null;
				String name = null;
				String file = null;
				int count = attributes.getLength();
				for (int i=0; i<count; i++) {
					Node node2 = attributes.item(i);
					String v = node2.getNodeValue();
					if (node2.getNodeName().equals(NAME))
						name = treeBundle.getString(v);
					else if (node2.getNodeName().equals(VISIBLE))
						rootVisible = new Boolean(v).booleanValue();
					else if (node2.getNodeName().equals(PAGE_VALUE))
						file = baseDir + v;
				}
				if (name==null)
					return null; // "name" attribute is required.
				HelpTreeNode helpRoot = file==null ?
								new HelpTreeNode(name) :
								new HelpTreeNode(name, file);

				// Set our global root.
				root = new DefaultMutableTreeNode(helpRoot);

				NodeList childNodes = node.getChildNodes();
				if (childNodes!=null) {
					int length = childNodes.getLength();
					for (int i=0; i<length; i++) {
						DefaultMutableTreeNode dmtn =
							initializeFromXMLFile(childNodes.item(i));
						// null could mean properties file or error.
						if (dmtn!=null)
							root.add(dmtn);
					}
				}
				return root;
			}

			// This represents a "Node" structure; that is, a node in
			// the tree help structure that contains child nodes.
			else if (nodeName.equals(TREE_NODE)) {
				NamedNodeMap attributes = node.getAttributes();
				if (attributes==null || attributes.getLength()<1)
					return null;
				String name = null;
				String file = null;
				int count = attributes.getLength();
				for (int i=0; i<count; i++) {
					Node node2 = attributes.item(i);
					String v = node2.getNodeValue();
					if (node2.getNodeName().equals(NAME))
						name = treeBundle.getString(v);
					else if (node2.getNodeName().equals(PAGE_VALUE))
						file = baseDir + v;
				}
				HelpTreeNode tempNode = file==null ?
								new HelpTreeNode(name) :
								new HelpTreeNode(name, file);
				DefaultMutableTreeNode dmtn = new
								DefaultMutableTreeNode(tempNode);
				NodeList childNodes = node.getChildNodes();
				if (childNodes!=null) {
					int length = childNodes.getLength();
					for (int i=0; i<length; i++) {
						DefaultMutableTreeNode newNode =
							initializeFromXMLFile(childNodes.item(i));
						if (newNode!=null)
							dmtn.add(newNode);
					}
				}
				return dmtn;
			}

			// This is a "leaf" in the tree node structure; that is,
			// an actual help HTML page.
			else if (nodeName.equals(PAGE)) {
				return handleNodePage(node);
			}

			// This is the list of words to put in the index.
			else if (nodeName.equals(INDEXITEMS)) {
				return handleNodeIndexItems(node);
			}

			throw new InternalError("Should never get here (nodename=='" + nodeName + "')");

		}

		// Everything went poorly!
		return null;

	}


	/**
	 * Called whenever a user selects an index item (through double-
	 * clicking or the "Display" button).  This function searches through
	 * all of the help HTML for the string selected from the index; if
	 * only one match is found, that page is displayed, but if more than
	 * one match is found, the user is prompted to select the desired
	 * page to display.
	 */
	private void loadSelectedHelpPageIndex() {

		String selected = (String)indexList.getSelectedValue();

		// Search through all of the help pages to see where this item is.
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tocTree.getModel().getRoot();
		List<HelpTreeNode> matchNodes = getTreeNodesContaining(root, selected);
		int size = matchNodes.size();

		// If there's only one match, just display it.
		if (size==1) {
			HelpTreeNode node = matchNodes.get(0);
			if (node.url!=null) {
				updateHistory = true;
				highlightSearchString = false;
				setHelpPageURL(node.url);
			}
		}

		// If there is > 1 match found, have the user pick the one they want.
		else if (size>1) {
			TopicsFoundDialog tfDialog = new TopicsFoundDialog(this, matchNodes);
			tfDialog.setVisible(true);
			int selectedIndex = tfDialog.getSelectedIndex();
			if (selectedIndex != -1) {
				HelpTreeNode node = matchNodes.get(selectedIndex);
				if (node.url!=null) {
					updateHistory = true;
					highlightSearchString = false;
					setHelpPageURL(node.url);
				}
			}

		}

		// If there are no matches (shouldn't happen if set up right), say so.
		else if (size==0) {
			editorPane.setText(noMatchHTML);
		}

	}


	// Helper method; called whenever a user selects an search item
	// (through double-clicking or the "Display" button).
	private void loadSelectedHelpPageSearch() {

		// Get the HelpTreeNode they chose.
		HelpTreeNode chosenNode = (HelpTreeNode)searchList.getSelectedValue();

		// Now, set the html in the right-hand pane to be the page associated with this node.
		if (chosenNode.url != null) {

			// We do want this page remembered in the history, and we
			// want searchString highlighted.
			updateHistory = true;
			highlightSearchString = true;
			setHelpPageURL(chosenNode.url);

		} // End of if (chosenNode.url != null).

	}


	// Populates the "Search Results" panel with results matching the user's search.
	private void populateSearchList() {

			String selected = searchField.getText();

			// Search through all of the help pages to see where this item is.
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)tocTree.
											getModel().getRoot();
			List<HelpTreeNode> matchNodes = getTreeNodesContaining(root, selected);

			// Populate the searchList panel with possible places to go.
			searchList.setListData(matchNodes.toArray());

			// Make sure "Display" button is active or not correctly.
			if (matchNodes.size() > 0) {
				searchDisplayButton.setEnabled(true);
				searchList.setSelectedIndex(0);
			}
			else
				searchDisplayButton.setEnabled(false);

	}


	/**
	 * Reads the text from a specified reader and returns it in a
	 * <code>String</code>.  This method is stolen from
	 * <code>DefaultEditorKit</code> and modified to save into a string
	 * rather than a <code>Document</code>.</p>
	 *
	 * Any of CR, LF, and CR/LF will be interpreted as the end-of-line
	 * marker if found, and will be transformed into <code>\n</code> in
	 * the string.
	 *
	 * @param in The reader from which to read.
	 * @return The text read from the reader.
	 * @throws IOException If an error occurs while reading.
	 */
	private static final int BUF_SIZE	= 16384;
	private static String read(Reader in) throws IOException {
	
		char[] buff = new char[BUF_SIZE];
		int nch;
		boolean lastWasCR = false;
		int last;
		StringBuilder sb = new StringBuilder();

		// Read in a block at a time, mapping \r\n to \n, as well as single
		// \r's to \n's. If a \r\n is encountered, \r\n will be set as the
		// newline string for the document, if \r is encountered it will
		// be set as the newline character, otherwise the newline property
		// for the document will be removed.
		while ((nch = in.read(buff, 0, buff.length)) != -1) {
			last = 0;
			for(int counter=0; counter<nch; counter++) {
				switch(buff[counter]) {
					case '\r':
						if (lastWasCR) {
							if (counter == 0) {
								sb.append("\n");
							}
							else {
								buff[counter - 1] = '\n';
							}
						}
						else {
							lastWasCR = true;
						}
						break;
					case '\n':
						if (lastWasCR) {
							if (counter > (last + 1))
								sb.append(buff,
										last,counter-last-1);
							// else nothing to do, can skip \r, next write will
							// write \n
							lastWasCR = false;
							last = counter;
						}
						break;
					default:
						if (lastWasCR) {
							if (counter == 0) {
								sb.append("\n");
							}
							else {
								buff[counter - 1] = '\n';
							}
							lastWasCR = false;
						}
						break;
				}
			}
			if (last < nch) {
				if(lastWasCR) {
					if (last < (nch - 1)) {
						sb.append(buff, last,nch-last-1);
					}
				}
				else {
					sb.append(buff, last,nch-last);
				}
			}
			if (lastWasCR) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}


	/**
	 * Sets the icon to use for the "back" button.
	 *
	 * @param icon The icon to use.  If <code>null</code>, no icon
	 *        will be used.
	 * @see #setForwardButtonIcon
	 */
	public void setBackButtonIcon(Icon icon) {
		if (backButton.getComponentOrientation().isLeftToRight()) {
			backButton.setIcon(icon);
		}
		else {
			forwardButton.setIcon(icon);
		}
	}


	/**
	 * Sets the "base URL" from which all links will be searched for.
	 * That is, all HTML pages loaded will have their links treated as
	 * relative to this URL.
	 *
	 * @param url The base URL.
	 */
	public void setBaseURL(URL url) {
		baseURL = url;
	}


	/**
	 * Sets the label on the "Contents" tab.
 	 *
	 * @param text The text for the "Contents" tab.
	 * @see #getContentsTabText
	 */
	public void setContentsTabText(String text) {
		tabbedPane.setTitleAt(0, text);
	}


	/**
	 * Sets the label on the "Display" button on the Index and Search panels.
 	 *
	 * @param text The text for the "Display" button.
	 * @see #getDisplayButtonText
	 */
	public void setDisplayButtonText(String text) {
		indexDisplayButton.setText(text);
		searchDisplayButton.setText(text);
	}


	/**
	 * Sets the icon to use for the "forward" button.
	 *
	 * @param icon The icon to use.  If <code>null</code>, no icon
	 *        will be used.
	 * @see #setBackButtonIcon
	 */
	public void setForwardButtonIcon(Icon icon) {
		if (forwardButton.getComponentOrientation().isLeftToRight()) {
			forwardButton.setIcon(icon);
		}
		else {
			backButton.setIcon(icon);
		}
	}


	/**
	 * Both updates the tocTree AND updates the help pane to display the
	 * correct Help page.  This method simply searches for the specified URL
	 * in the help tree.  If it is found, it is selected, which triggers the
	 * HTML pane to update itself accordingly.  If it is not found (which
	 * would mean an error on the caller's part), nothing is selected and
	 * an error message is given.
	 *
	 * @param url The URL to load.  This URL should not contain an anchor
	 *        (reference) as this will mess up the search for the URL in
	 *        the help tree.
	 */
	private void setHelpPageURL(URL url) {

		// If we got into this method, the user changed the help screen some
		// way other than clicking directly on tocTree.  We need to know this
		// to correctly update the history.
		clickedOnTOCTree = false;

		// Select the root or first node if url==null.
		if (url==null) {
			tocTree.setSelectionRow(0);
			return;
		}

		// Anything on the "real" web should probably be displayed in a real
		// browser.
		if (("http".equals(url.getProtocol()) && webUrlsInRealBrowser) ||
				"ftp".equals(url.getProtocol())) {
			if (!UIUtil.browse(url.toString())) {
				UIManager.getLookAndFeel().provideErrorFeedback(this);
			}
			return;
		}

		// In the tocTree, make the new page the selected one.
		// This fires a TreeEvent action to occur, and method valueChanged()
		// will set the new Help page for us.
		// NOTE: getRowCount() returns number of EXPANDED, visible rows, so we
		// will try to expand each node as we go along to be sure we hit all
		// possible nodes.
		for (int i=0; i<tocTree.getRowCount(); i++) {
			DefaultMutableTreeNode temp = (DefaultMutableTreeNode)tocTree.
								getPathForRow(i).getLastPathComponent();
			tocTree.expandRow(i);
			HelpTreeNode htn = (HelpTreeNode)temp.getUserObject();
			if (htn.url!=null && htn.url.equals(url)) {
				tocTree.setSelectionRow(i);
				tocTree.scrollRowToVisible(i);
				return;
			}
		}

		// If the page wasn't found, tell the user so.
		ResourceBundle msg = getHelpBundle();
		JOptionPane.showMessageDialog(this,
							msg.getString("PageNotFound"),
							msg.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
		msg = null; // May help GC.

	}


	/**
	 * Sets the label on the "Index" tab.
 	 *
	 * @param text The text for the "Index" tab.
	 * @see #getIndexTabText
	 */
	public void setIndexTabText(String text) {
		tabbedPane.setTitleAt(1, text);
	}


	/**
	 * Sets the text label above the "Type in a keyword to find" text boxes.
	 *
	 * @param text The text label.
	 * @see #getKeywordFieldLabelText
	 */
	public void setKeywordFieldLabelText(String text) {
		keywordToFindLabel.setText(text);
		keywordToFindLabel2.setText(text);
	}


	/**
	 * Sets the label on the "List Topics" button on the Search panels.
 	 *
	 * @param text The text for the "List Topics" button.
	 * @see #getListTopicsButtonText
	 */
	public void setListTopicsButtonText(String text) {
		listTopicsButton.setText(text);
	}


	/**
	 * Toggles whether URLs starting with "<code>http://</code>" should be
	 * opened in a real web browser (assuming Java 6), instead of this Help
	 * dialog.  The default value of this property is <code>true</code>, since
	 * it's assumed this is usually done to go to project home pages or more
	 * extensive online documentation, and JEditorPane can't really handle
	 * "real" HTML.
	 *
	 * @param inRealBrowser Whether to open URL's starting with
	 *        "<code>http://</code>" in a real browser.
	 * @see #getOpenWebUrlsInRealBrowser()
	 */
	public void setOpenWebUrlsInRealBrowser(boolean inRealBrowser) {
		webUrlsInRealBrowser = inRealBrowser;
	}


	/**
	 * Sets the label on the "Search" tab.
 	 *
	 * @param text The text for the "Search" tab.
	 * @see #getSearchTabText
	 */
	public void setSearchTabText(String text) {
		tabbedPane.setTitleAt(2, text);
	}


	/**
	 * Sets the text label above the "Select topic to display" text box.
	 *
	 * @param text The text label.
	 * @see #getTopicToDisplayFieldLabelText
	 */
	public void setTopicToDisplayFieldLabelText(String text) {
		topicToDisplayLabel.setText(text);
	}


	/**
	 * Helper function to validate that a given node is indeed an Attribute
	 * node with the specified name.
	 *
	 * @param node The node to check.
	 * @param name The name the node should have.
	 * @return Whether the specified node is an Attribute node with the
	 *         specified name.
	 */
	private static final boolean validateAttributeNode(Node node, String name) {
		return (node!=null && node.getNodeType()==Node.ATTRIBUTE_NODE &&
				node.getNodeName().equals(name));
	}


	/**
	 * Listens for events in the help dialog.
	 */
	protected class HelpListener extends MouseAdapter
			implements ChangeListener, HyperlinkListener, DocumentListener,
					KeyListener, TreeSelectionListener {

		public void changedUpdate(DocumentEvent e) {
		}

		/**
		 * Called whenever the user clicks on a hyperlink in editorPane.
		 */
		public void hyperlinkUpdate(HyperlinkEvent e) {

			HyperlinkEvent.EventType eventType = e.getEventType();

			if (eventType.equals(HyperlinkEvent.EventType.ACTIVATED)) {
				// Yes, you want this to update the memory of pages visited.
				updateHistory = true;
				highlightSearchString = false;
				// This is a hack so that, for their href, they can specify a "relative path"
				// to any HTML help, which especially comes in handy in jar files.
				URL url = e.getURL();
				if (url==null) {
					try {
						url = new URL("file://" + baseURL.getPath() + e.getDescription());
					} catch (MalformedURLException mue) {
						app.displayException(HelpDialog.this, mue);
					}
				}
				String anchor = null;
				if (url!=null && url.getRef()!=null) {
					anchor = url.getRef();
					try {
						String protocol = url.getProtocol();
						if (protocol==null || protocol.equals(""))
							protocol = "file://";
						String urlString = "file://" + url.getPath();
						url = new URL(urlString); // No anchor.
					} catch (MalformedURLException mue) {
						mue.printStackTrace();
					}
				}
				setHelpPageURL(url);
				if (anchor!=null)
					editorPane.scrollToReference(anchor);
			}

			else if (eventType.equals(HyperlinkEvent.EventType.ENTERED)) {
				editorPane.setToolTipText(e.getDescription());
			}

			else if (eventType.equals(HyperlinkEvent.EventType.EXITED)) {
				editorPane.setToolTipText(null);
			}

		}

		/**
		 * Called whenever indexField or searchField's contents are
		 * inserted into.
		 */
		public void insertUpdate(DocumentEvent e) {
			Document doc = e.getDocument();
			// If it was the index text field that changed...
			if ( doc.equals(indexField.getDocument()) ) {
				// Find closest match to entered text in index list and highlight it.
				int closestMatch = indexList.getNextMatch(
						indexField.getText(), 0, Position.Bias.Forward);
				if (closestMatch != -1) {
					indexList.setSelectedIndex(closestMatch);
					indexList.ensureIndexIsVisible(indexList.getSelectedIndex());
				}
			}
			// If it was the search text field that changed...
			else if ( doc.equals(searchField.getDocument()) ) {
				// Ensure that the "List Topics" button is enabled.
				listTopicsButton.setEnabled(true);
			}
		}

		/**
		 * Called whenever the user presses a key in indexField or searchField.
		 */
		public void keyPressed(KeyEvent e) {

			// The only keypress we're interested in is the "Enter" key.
			if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {

				Object source = e.getSource();

				// If they pressed Enter while in the index tab, load the
				// help page currently selected.
				if ( source.equals(indexField) || source.equals(indexList) ) {
					if (indexList.getSelectedIndex()!=-1)
						loadSelectedHelpPageIndex();
				}

				// If they press Enter while in searchField, display a list
				// of Help topics.
				else if (source.equals( searchField )) {
					searchString = searchField.getText();	// Remember the searched-for text.
					if (!searchString.equals(""))
						populateSearchList();
				}

				// If they press Enter while in searchList, display the
				// help topic they choose.
				else if (source.equals( searchList )) {
					if (indexList.getSelectedIndex()!=-1)
						loadSelectedHelpPageSearch();
				}

				// If they press enter while on an expandable node in the
				// tocTree, toggle whether it's expanded.
				else if (source.equals( tocTree )) {
					int row = tocTree.getMaxSelectionRow();
					// These are okay since tree is single-selection.
					if (tocTree.isExpanded(row)==true)
						tocTree.collapseRow(row);
					else
						tocTree.expandRow(row);
				}

			}

		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}

		/**
		 * We're listening for when the user clicks in one of our components.
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			// They must double-click for anything to happen.
			if (e.getClickCount()==2) {
				// If they double-click in the index panel's JList,
				// load the clicked-on help page.
				if (e.getComponent().equals(indexList))
					loadSelectedHelpPageIndex();
				// If they double-click in the search panel's JList,
				// load the clicked-on help page.
				else if (e.getComponent().equals(searchList))
					loadSelectedHelpPageSearch();
			}
		}

		/**
		 * Called whenever indexField or searchField's contents are shortened.
		 */
		public void removeUpdate(DocumentEvent e) {
			Document doc = e.getDocument();
			// If it was the index text field that changed...
			if ( doc.equals(indexField.getDocument()) ) {
				// Find closest match to entered text in index list and
				// highlight it.
				int closestMatch = indexList.getNextMatch(
						indexField.getText(), 0, Position.Bias.Forward);
				if (closestMatch != -1) {
					indexList.setSelectedIndex(closestMatch);
					indexList.ensureIndexIsVisible(indexList.getSelectedIndex());
				}
			}
			// If it was the search text field that changed...
			else if ( doc.equals(searchField.getDocument()) ) {
				// If there is no more text in the search field,
				// disable the "List Topics" button.
				if (doc.getLength() == 0)
					listTopicsButton.setEnabled(false);
			}
		}

		/**
		 * Called whenever the user clicks on the Contents, Index, or
		 * Search tabs.
		 */
		public void stateChanged(ChangeEvent e) {
			int selection = ((JTabbedPane)e.getSource()).getSelectedIndex();
			JTextField field = null;
			// If they selected the Index or Search tab, select the top
			// text field.
			if (selection==1)
				field = indexField;
			else if (selection==2)
				field = searchField;
			if (field!=null) {
				final JTextField field2 = field;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						field2.requestFocusInWindow();
						field2.selectAll();
					}
				});
			}
		}

		/**
		 * Called whenever an item in the TOC tree is selected/deselected.
		 * This will display the help HTML corresponding to the TOC tree node
		 * selected in the right-hand pane.
		 */
		public void valueChanged(TreeSelectionEvent tse) {

			// Get the node they clicked on.
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)
							tocTree.getLastSelectedPathComponent();

			// Selected node could be null if they selected a leaf, then
			// collapsed its parent tree node, leaving "no node" selected.
			if (selectedNode==null)
				return;
			HelpTreeNode htn = (HelpTreeNode)selectedNode.getUserObject();

			// Now, set the html in the right-hand pane to be the page
			// associated with this node.  NOTE: Must check for null URL's
			// as often, branch nodes don't have HTML associated with them.
			if (htn.url != null) {

				// As we support both plain text and HTML as help pages,
				// first check whether our text should be displayed as HTML.
				// We must re-set the IgnoreCharsetDirective property as
				// setContentType() gives us a new document.
				String contentType = guessContentType(htn.url.getPath());
				if (!contentType.equals(editorPane.getContentType())) {
					editorPane.setContentType(contentType);
					//editorPane.setDocument(editorPane.getEditorKit().createDefaultDocument());
					// The Document class does not yet handle charsets properly -
					// without the line below, you'll get a CharSetChangedException...
					editorPane.getDocument().
						putProperty("IgnoreCharsetDirective", Boolean.TRUE);
				}

				String allText = getContents(htn);

				// If the document is HTML, ensure that the it uses the
				// correct base URL so links work.
				Document document = editorPane.getDocument();
				if (document instanceof HTMLDocument) {
					HTMLDocument htmldoc = (HTMLDocument)document;
					htmldoc.setBase(baseURL);
				}

				editorPane.setText(allText);
				editorPane.setCaretPosition(0);

				// If they want to remember this page in the history...
				if (updateHistory || clickedOnTOCTree) {
					// This will be the "last" page.  Remove all pages
					// ahead of the current page and replace them with
					//this one.
					while (history.size()-1 > historyPos)
						history.remove(history.size()-1);
					history.add(htn);
					historyPos++;
					// The user can definitely go backward and not forward.
					backButton.setEnabled(true);
					forwardButton.setEnabled(false);
				}

				// Next time through, we'll assume we clicked on tocTree
				// unless told otherwise
				clickedOnTOCTree = true;

			}

		}

	}


}