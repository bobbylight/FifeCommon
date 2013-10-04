/*
 * 06/30/2004
 *
 * RTextFileChooser - A Microsoft Windows-style file chooser for any
 * Look-and-Feel.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import org.fife.ui.FSATextField;
import org.fife.ui.MenuButton;
import org.fife.ui.RScrollPane;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;
import org.fife.ui.breadcrumbbar.BreadcrumbBar;
import org.fife.ui.rtextfilechooser.filters.AcceptAllFileFilter;


/**
 * A powerful, flexible text file chooser.  Its UI is similar to the Windows LnF
 * <code>JFileChooser</code>, but it is not affected by Look-and-Feel changes.
 * This file chooser has the following features:
 * <ul>
 *   <li>List/Details/Icon views
 *   <li>A "Favorite Directories" list, which can be serialized and reloaded
 *   <li>Auto-completion of filenames
 *   <li>Select files only, directories only, or both files and directories
 *   <li>Rename and delete files directly from the file chooser (with Recycle
 *       Bin support on Windows)
 *   <li>Drag-and-drop support to copy files from a directory (cannot drop into
 *       the directory viewed just yet)
 *   <li>Select the file encoding when opening/saving text files (optional)
 *   <li>Set the text color used to identify different file types
 *   <li>Currently-open files can be displayed with a special style (e.g.
 *       underline, italic, etc.)
 *   <li>Utilizes a "breadcrumb bar" component like that found in Windows
 *       Vista for top-level directory navigation.
 * </ul>
 * This component has many of the features found in <code>JFileChooser</code>
 * and is designed to be a drop-in replacement for opening text files.
 *
 * @author Robert Futrell
 * @version 0.7
 */
public class RTextFileChooser extends ResizableFrameContentPane
			implements ActionListener, PropertyChangeListener, FileSelector {

	public static final int LIST_MODE				= 0;
	public static final int DETAILS_MODE			= 1;
	public static final int ICONS_MODE				= 3;

	public static final int APPROVE_OPTION			= JFileChooser.APPROVE_OPTION;
	public static final int CANCEL_OPTION			= JFileChooser.CANCEL_OPTION;
	public static final int ERROR_OPTION			= JFileChooser.ERROR_OPTION;

	public static final int FILES_ONLY				= JFileChooser.FILES_ONLY;
	public static final int DIRECTORIES_ONLY		= JFileChooser.DIRECTORIES_ONLY;
	public static final int FILES_AND_DIRECTORIES	= JFileChooser.FILES_AND_DIRECTORIES;

	public static final int OPEN_DIALOG				= JFileChooser.OPEN_DIALOG;
	public static final int SAVE_DIALOG				= JFileChooser.SAVE_DIALOG;

	// Style constants for "open file style".
	public static final int STYLE_BOLD				= 0;
	public static final int STYLE_ITALIC			= 1;
	public static final int STYLE_UNDERLINE			= 2;

	static final boolean IGNORE_CASE = Utilities.isCaseSensitiveFileSystem();

	private FileSystemView fileSystemView;
	private ItemListener itemListener;
	private AcceptAllFileFilter acceptAllFilter;

	private BreadcrumbBar lookInBreadcrumbBar;
	private FSATextField fileNameTextField;
	private JComboBox filterCombo;
	private JComboBox encodingCombo;

	/*
	 * Any renderers/listeners.
	 */
	private TextFieldListener textFieldListener;

	/*
	 * The buttons at the top of the dialog.
	 */
	private JButton upOneLevelButton;
	private JButton newFolderButton;
	private MenuButton viewButton;
	private MenuButton favoritesButton;

	private JButton acceptButton;
	private JButton cancelButton;

	/**
	 * The "view" of the current directory (either list, details or icons).
	 */
	protected RTextFileChooserView view;
	private RScrollPane viewScrollPane;

	/**
	 * The context menu used by views.
	 */
	private JPopupMenu popupMenu;

	/**
	 * The "glob" (wildcard) filter that shows all files.
	 */
	private WildcardFileFilter globFilter;

	/*
	 * Icons.
	 */
	private Icon upFolderIcon;
	private Icon newFolderIcon;
	private Icon detailsViewIcon;
	private Icon listViewIcon;
	private Icon iconsViewIcon;
	private Icon favoritesIcon;
	private FileChooserIconManager iconManager;

	/*
	 * Strings used by the file chooser.
	 */
	private String customTitle;
	private String saveButtonText;
	private String saveDialogTitleText;
	private String saveButtonToolTipText;
	private int saveButtonMnemonic;
	private String openButtonText;
	private String openDialogTitleText;
	private String openButtonToolTipText;
	private int openButtonMnemonic;
	private String newFolderPrompt;
	private String errorNewDirPrompt;
	String errorDialogTitle;
	private String directoryText;
	private String fileText;
	private String nameString;
	private String sizeString;
	private String typeString;
	private String statusString;
	private String lastModifiedString;
	private String openString;
	private String noFavoritesDefinedString;
	private String favoriteDNERemoveString;

	/*
	 * Dialog actions.
	 */
	private Actions.SystemOpenAction systemEditAction;
	private Actions.SystemOpenAction systemViewAction;
	private Actions.CopyAction copyAction;
	private Actions.DeleteAction deleteAction;
	private Actions.DeleteAction hardDeleteAction;
	private Actions.PasteAction pasteAction;
	private Actions.RefreshAction refreshAction;
	private Actions.RenameAction renameAction;
	private Actions.UpOneLevelAction upOneLevelAction;
	private Actions.PropertiesAction propertiesAction;

	/*
	 * Internal stuff.
	 */
	private int mode					= -1; // So that we don't get an NPE initially.
	private boolean multiSelectionEnabled	= false;
	private int fileSelectionMode			= FILES_ONLY;
	private boolean fileSystemAware;
	private boolean autoCompleteFileNames;
	private String encoding;
	private FileFilter filterToSelect;

	private FileTypeInfo tempInfo;	// Used internally.
	private Map<String, Color> customColors; // Mapping of extensions to colors.
	private boolean showHiddenFiles;
	private Color hiddenFileColor;
	private boolean styleOpenFiles;
	private int openFilesStyle;

	File currentDirectory;
	private File[] selectedFiles;

	private Dimension lastSize;
	private int lastType;

	private Vector<FileFilter> fileFilters = new Vector<FileFilter>(5,1);
	private FileFilter currentFileFilter;

	private boolean isChangingDirectories;

	private File[] filesToSelect;	// Any files to select.
	private File[] openedFiles;	// Files whose names are underlined (they are "opened" in the app).

	private JDialog dialog;		// The dialog containing the file chooser.

	private boolean guiInitialized;

	private boolean showEncodingCombo;

	/**
	 * Sorted list of "favorite" directories.
	 */
	private java.util.List<String> favoriteList;

	/**
	 * The return value given after showOpenDialog or showSaveDialog.
	 */
	private int retVal;

	/**
	 * The default directory for the file chooser.
	 */
	private static final File DEFAULT_START_DIRECTORY =
									new File(System.getProperty("user.dir"));

	/**
	 * The encoding used for writing "Favorites" files.
	 */
	private static final String FAVORITES_ENCODING	= "UTF-8";

	/**
	 * Whether we're running in a Java 6 or higher JVM.
	 */
	private static final boolean IS_JAVA_6_PLUS;

	/**
	 * The resource bundle for file choosers.
	 */
	static final ResourceBundle msg = ResourceBundle.getBundle(
									"org.fife.ui.rtextfilechooser.FileChooser");

	/**
	 * Creates a new <code>RTextFileChooser</code>.
	 */
	public RTextFileChooser() {
		this(DEFAULT_START_DIRECTORY);
	}


	/**
	 * Creates a new <code>RTextFileChooser</code>.
	 *
	 * @param showEncodingCombo Whether the encoding combo box should be
	 *        visible.  This should be <code>true</code> if this is a chooser
	 *        for text files, and <code>false</code> if it is a chooser for
	 *        binary files.
	 */
	public RTextFileChooser(boolean showEncodingCombo) {
		this(showEncodingCombo, DEFAULT_START_DIRECTORY);
	}


	/**
	 * Creates a new <code>RTextFileChooser</code>.
	 *
	 * @param startDirectory The directory for the file chooser to "start" in.
	 */
	public RTextFileChooser(String startDirectory) {
		this(new File(startDirectory));
	}


	/**
	 * Creates a new <code>RTextFileChooser</code>.
	 *
	 * @param startDirectory The directory for the file chooser to "start" in.
	 */
	public RTextFileChooser(File startDirectory) {
		this(true, startDirectory);
	}

	/**
	 * Creates a new <code>RTextFileChooser</code>.
	 *
	 * @param showEncodingCombo Whether the encoding combo box should be
	 *        visible.  This should be <code>true</code> if this is a chooser
	 *        for text files, and <code>false</code> if it is a chooser for
	 *        binary files.
	 * @param startDirectory The directory for the file chooser to "start" in.
	 */
	public RTextFileChooser(boolean showEncodingCombo, File startDirectory) {

		this.showEncodingCombo = showEncodingCombo;

		fileSystemView = FileSystemView.getFileSystemView();
		iconManager = new FileChooserIconManager();

		itemListener = new RTextFileChooserItemListener();

		// Get the "current directory" for the file chooser.
		if (startDirectory==null || !startDirectory.isDirectory()) {
			startDirectory = DEFAULT_START_DIRECTORY;
		}
		currentDirectory = startDirectory;

		// Read and set the user's preferences for the file chooser.
		// We need to do this after all components are added above.
		FileChooserPreferences prefs = FileChooserPreferences.load();
		tempInfo = new FileTypeInfo(null, null);
		customColors = prefs.customColors;
		setShowHiddenFiles(prefs.showHiddenFiles);
		setHiddenFileColor(prefs.hiddenFileColor);
		setFileSystemAware(prefs.fileSystemAware);
		setAutoCompleteFileNames(prefs.autoCompleteFileNames);
		setStyleOpenFiles(prefs.styleOpenFiles);
		setOpenFilesStyle(prefs.openFilesStyle);
		// Do NOT call setViewMode() yet, as we can do without its overhead.
		this.mode = prefs.viewMode;

		guiInitialized = false;


	}


	/**
	 * Adds the specified directory to the user's "favorites" list.  This
	 * should be an absolute path.
	 *
	 * @param dir The directory to add to the "favorites" list.
	 * @return Whether the item was added.  If this returns <code>false</code>,
	 *         then the directory specified is already in the "Favorites" list.
	 * @throws NullPointerException If <code>dir</code> is <code>null</code>.
	 * @see #getFavorites()
	 * @see #clearFavorites()
	 * @see #loadFavorites(File)
	 * @see #saveFavorites(File)
	 */
	public boolean addToFavorites(String dir) {

		if (dir==null) {
			throw new NullPointerException("dir cannot be null");
		}

		if (favoriteList==null) {
			favoriteList = new ArrayList<String>(1); // Usually not many.
		}

		if (!favoriteList.contains(dir)) {
			favoriteList.add(dir);
			return true;
		}

		return false;

	}


	/**
	 * Initializes the GUI components for the file chooser.  This stuff
	 * isn't done in the constructor as it can be time-consuming (actually,
	 * Java checking for roots of the file system can be time-consuming).
	 * Instead, we put it in this method and defer it until the file chooser
	 * is actually displayed, making sure it is only done once.  This way
	 * the user can pull up the Options dialog and change properties of the
	 * file chooser without the GUI having to be initialized.
	 */
	protected synchronized void initializeGUIComponents() {

		// Only initialize the GUI once.
		if (guiInitialized) {
			return;
		}

		ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());
		boolean ltr = o.isLeftToRight();

		// Get the icons and actions for all stuff below.
		createActions();
		getIcons();

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		Box topPanel = new Box(BoxLayout.LINE_AXIS);
		topPanel.setOpaque(true); // Boxes extend JComponent -> not opaque

		JLabel lookInLabel = new JLabel(getString("LookInLabel"));

		int horizStrutSize = 4;
		Border empty3Border = BorderFactory.createEmptyBorder(3,3,3,3);

		lookInBreadcrumbBar = new BreadcrumbBar();
		populateLookInComboBox();
		lookInBreadcrumbBar.addPropertyChangeListener(
									BreadcrumbBar.PROPERTY_LOCATION, this);

		upOneLevelButton = new JButton(upOneLevelAction);
		upOneLevelButton.setText(null); // Remove text from action.
		upOneLevelButton.setIcon(upFolderIcon);
		upOneLevelButton.setToolTipText(getString("UpOneLevelTTT"));
		upOneLevelButton.setBorder(empty3Border);
		newFolderButton = new JButton(newFolderIcon);
		newFolderButton.setToolTipText(getString("NewFolderTTT"));
		newFolderButton.setActionCommand("CreateNewDirectory");
		newFolderButton.addActionListener(this);
		newFolderButton.setBorder(empty3Border);

		viewButton = new MenuButton(listViewIcon);
		viewButton.setBorder(empty3Border);
		JMenuItem  rbItem = new JRadioButtonMenuItem(getString("ListViewTTT"), listViewIcon, mode==LIST_MODE);
		rbItem.setActionCommand("ListButton");
		rbItem.addActionListener(this);
		viewButton.addMenuItem(rbItem);
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbItem);
		rbItem = new JRadioButtonMenuItem(getString("DetailsViewTTT"), detailsViewIcon, mode==DETAILS_MODE);
		rbItem.setActionCommand("DetailsButton");
		rbItem.addActionListener(this);
		viewButton.addMenuItem(rbItem);
		bg.add(rbItem);
		rbItem = new JRadioButtonMenuItem(getString("IconsViewTTT"), iconsViewIcon, mode==ICONS_MODE);
		rbItem.setActionCommand("IconsButton");
		rbItem.addActionListener(this);
		viewButton.addMenuItem(rbItem);
		bg.add(rbItem);

		favoritesButton = new MenuButton(favoritesIcon);
		favoritesButton.setToolTipText(getString("FavoritesTTT"));
		favoritesButton.setBorder(empty3Border);
		favoritesButton.addSeparator();
		JMenuItem item  = new JMenuItem(new Actions.AddToFavoritesAction(this));
		favoritesButton.addMenuItem(item);
		favoritesButton.addPopupMenuListener(new FavoritesPopupListener());

		topPanel.add(lookInLabel);
		topPanel.add(lookInBreadcrumbBar);
		topPanel.add(Box.createHorizontalStrut(horizStrutSize));
		topPanel.add(upOneLevelButton);
		topPanel.add(Box.createHorizontalStrut(horizStrutSize));
		topPanel.add(newFolderButton);
		topPanel.add(Box.createHorizontalStrut(horizStrutSize));
		topPanel.add(viewButton);
		topPanel.add(Box.createHorizontalStrut(horizStrutSize));
		topPanel.add(favoritesButton);

		add(topPanel, BorderLayout.NORTH);

		// Initialize listeners.
		textFieldListener = new TextFieldListener();

		// Initialize either the list view or the details (table) view.
		installDetailsViewStrings();
		setViewModeImpl(mode);
		Box centerPanel = Box.createVerticalBox();
		centerPanel.add(Box.createVerticalStrut(5));
		centerPanel.add(viewScrollPane);
		centerPanel.add(Box.createVerticalStrut(5));
		add(centerPanel);

		// Create a panel for the bottom stuff.
		JPanel bottomPanel = new JPanel(new SpringLayout());

		JLabel fileNameLabel = new JLabel(getString("FileNameLabel"));
		JLabel filterLabel = new JLabel(getString("FilterLabel"));
		JLabel encodingLabel = new JLabel(getString("EncodingLabel"));

		fileNameTextField = new FSATextField(false, currentDirectory);

		// This renderer is too slow when used to render files on a mounted
		// network drive.
		//fileNameTextField.setListCellRenderer(
		//					new FileNameFieldListCellRenderer());
		fileNameTextField.setFileSystemAware(getFileSystemAware());
		fileNameTextField.setAutoCompleteFileName(getAutoCompleteFileNames());
		fileNameTextField.addFocusListener(textFieldListener);
		fileNameTextField.getDocument().addDocumentListener(textFieldListener);
		fileNameLabel.setLabelFor(fileNameTextField);

		filterCombo = new JComboBox();
		filterCombo.setMaximumRowCount(12);
		filterLabel.setLabelFor(filterCombo);
		filterCombo.addItemListener(itemListener);

		int bottomPanelRowCount = 2;
		if (showEncodingCombo) {
			encodingCombo = new JComboBox();
			UIUtil.fixComboOrientation(encodingCombo);
			encodingCombo.setMaximumRowCount(12);
			encodingLabel.setLabelFor(encodingCombo);
			bottomPanelRowCount++;
		}

		if (ltr) {
			bottomPanel.add(fileNameLabel);
			bottomPanel.add(fileNameTextField);
			bottomPanel.add(filterLabel);
			bottomPanel.add(filterCombo);
			if (showEncodingCombo) {
				bottomPanel.add(encodingLabel);
				bottomPanel.add(encodingCombo);
			}
		}
		else {
			bottomPanel.add(fileNameTextField);
			bottomPanel.add(fileNameLabel);
			bottomPanel.add(filterCombo);
			bottomPanel.add(filterLabel);
			if (showEncodingCombo) {
				bottomPanel.add(encodingCombo);
				bottomPanel.add(encodingLabel);
			}
		}

		JPanel temp = new JPanel(new GridLayout(2,1, 0,5));

		acceptButton = new JButton();
		acceptButton.setActionCommand("AcceptButtonPressed");
		acceptButton.addActionListener(this);
		UIUtil.ensureButtonWidth(acceptButton, 80);
		temp.add(acceptButton);

		String cancelText = UIManager.getString("FileChooser.cancelButtonText");
		int cancelMnemonic = getMnemonic("FileChooser.cancelButtonMnemonic");
		String cancelToolTip = UIManager.getString("FileChooser.cancelButtonToolTipText");
		cancelButton = new JButton(cancelText);
		cancelButton.setMnemonic(cancelMnemonic);
		cancelButton.setToolTipText(cancelToolTip);
		cancelButton.setActionCommand("CancelButtonPressed");
		cancelButton.addActionListener(this);
		UIUtil.ensureButtonWidth(cancelButton, 80);
		temp.add(cancelButton);

		JPanel bottomButtonPanel = new JPanel(new BorderLayout());
		bottomButtonPanel.add(temp, BorderLayout.NORTH);

		temp= new JPanel(new BorderLayout());
		temp.add(bottomPanel);
		temp.add(bottomButtonPanel, BorderLayout.LINE_END);

		add(temp, BorderLayout.SOUTH);
		int initX = ltr ? 0 : 5;
		UIUtil.makeSpringCompactGrid(bottomPanel,
								bottomPanelRowCount, 2,	//rows, cols
								initX,0,		//initX, initY
								6, 6);		//xPad, yPad

		// The "accept all files" file filter.
		if (acceptAllFilter==null) {
			acceptAllFilter = new AcceptAllFileFilter();
		}
		addChoosableFileFilter(acceptAllFilter);
		populateFilterComboBox();

		// Localize and get ready to go!
		installStrings();
		applyComponentOrientation(o);
		guiInitialized = true;

		// We must call this AFTER guiInitialized is set to true, so that
		// the encoding combo displays the correct encoding.
		refreshEncodingComboBox();//setEncoding(encoding);

	}


	/**
	 * Listens for actions in this file dialog.
	 */
	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if ("AcceptButtonPressed".equals(actionCommand)) {
			approveSelection();
		}

		else if ("CancelButtonPressed".equals(actionCommand)) {
			cancelSelection();
		}

		else if ("CreateNewDirectory".equals(actionCommand)) {
			String newDirName = JOptionPane.showInputDialog(this, newFolderPrompt);
			if (newDirName!=null) {
				boolean result = new File(currentDirectory, newDirName).mkdir();
				if (result==false) {
					JOptionPane.showMessageDialog(this, errorNewDirPrompt,
								errorDialogTitle, JOptionPane.ERROR_MESSAGE);
				}
				else {
					refreshView();
					fileNameTextField.setText(newDirName);
				}
			}
			fileNameTextField.requestFocusInWindow();
		}

		else if ("PopupOpen".equals(actionCommand)) {
			actionPerformed(new ActionEvent(view,
						ActionEvent.ACTION_PERFORMED, "AcceptButtonPressed"));
		}

		else if ("ListButton".equals(actionCommand)) {
			setViewMode(LIST_MODE);
			viewButton.setIcon(listViewIcon);
		}

		else if ("DetailsButton".equals(actionCommand)) {
			setViewMode(DETAILS_MODE);
			viewButton.setIcon(detailsViewIcon);
		}

		else if ("IconsButton".equals(actionCommand)) {
			setViewMode(ICONS_MODE);
			viewButton.setIcon(iconsViewIcon);
		}

		else if ("SetDir".equals(actionCommand)) { // A "Favorites" menu item
			JMenuItem item = (JMenuItem)e.getSource();
			String dirName = item.getText();
			File dir = new File(dirName);
			if (!dir.isDirectory()) {
				String text = MessageFormat.format(favoriteDNERemoveString,
										new Object[] { dirName });
				int rc = JOptionPane.showConfirmDialog(this,
						text, errorDialogTitle,JOptionPane.YES_NO_OPTION);
				if (rc==JOptionPane.YES_OPTION) {
					favoriteList.remove(dirName);
				}
			}
			else {
				setCurrentDirectory(dir);
			}
			fileNameTextField.requestFocusInWindow();
		}

	}


	/**
	 * Adds a file filter to the filter combo box.
	 *
	 * @param filter The file filter to add.
	 * @see #removeChoosableFileFilter
	 * @see #getChoosableFileFilters
	 */
	public void addChoosableFileFilter(FileFilter filter) {
		setFileFilterImpl(filter, false); // Takes care of everything for us.
	}


	/**
	 * Returns the specified string, wrapped inside HTML tags to display
	 * it with the "open file style" of this file chooser (assuming this
	 * property is enabled).<p>
	 *
	 * This method is called by child views to ensure they render opened
	 * files' names properly.
	 *
	 * @param addTo The string to add HTML styles to.
	 * @return The string, with style HTML added.  If styling opened files is
	 *         disabled, the string is returned unchanged.
	 * @see #getStyleOpenFiles()
	 * @see #getOpenFilesStyle()
	 */
	String addOpenFileStyleHtml(String addTo) {
		if (getStyleOpenFiles()) {
			switch (getOpenFilesStyle()) {
				case STYLE_BOLD:
					// Fall through, only underlining is currently
					// implemented by the views.
				case STYLE_ITALIC:
					// Fall through, only underlining is currently
					// implemented by the views.
				case STYLE_UNDERLINE:
				default:
					addTo = "<html><u>" + addTo + "</u></html>";
					break;
			}
		}
		return addTo;
	}


	/**
	 * Ensures that the file chooser never starts off too big or too small
	 * (as this is influenced by the file list displayed).  This should ONLY
	 * be called when <code>dialog</code> is created.
	 */
	private void adjustSizeIfNecessary() {

		Dimension size = dialog.getSize();
		Dimension oldSize = new Dimension(size);

		if (size.width>800)
			size.width = 800;
		else if (size.width<300)
			size.width = 300;
		if (size.height>800)
			size.height = 800;
		else if (size.height<300)
			size.height = 300;

		if (size.width!=oldSize.width || size.height!=oldSize.height)
			dialog.setSize(size);

	}


	/**
	 * Called when the user clicks the "Approve" button.  You can also call
	 * this programmatically.  Note that this does NOT necessarily mean the
	 * dialog will close; for example, if they have selected a directory
	 * and the file selection mode is <code>FILES_ONLY</code>, then the
	 * file dialog will simply change to that directory.<p>
	 *
	 * Users who wish to subclass this class and provide a means of canceling
	 * the dialog closing (such as an "are you sure you wish to overwrite?"
	 * message) should override {@link #approveSelectionImpl()}.  That method
	 * is called at the end of this one, and the file chooser will only close
	 * if that method returns <code>true</code>.
	 */
	public void approveSelection() {

		selectedFiles = getFilesFromFileNameTextField();
		if (selectedFiles==null) {
			// Some views allow you to click outside of all files
			// (i.e. IconsView).
			return;
		}

		// Make sure the file paths are absolute.
		ensureAbsoluteFilePaths(selectedFiles);

		// Some "corrections:"  If we're in files-only mode and
		// they have selected more than one directory, or one
		// or more files and a directory, then we simply take
		// the last-selected value and use that.
		if (fileSelectionMode==FILES_ONLY) {
			if (containsFilesAndDirectories(selectedFiles)) {
				File[] temp = new File[1];
				temp[0] = selectedFiles[0];
				if (temp[0].isDirectory()) {
					temp[0] = getCanonicalFileFor(temp[0]);
					setCurrentDirectory(temp[0]);
					return;
				}
				selectedFiles = temp;
			}
			else if (containsOnlyDirectories(selectedFiles)) {
				selectedFiles[0] = getCanonicalFileFor(selectedFiles[0]);
				setCurrentDirectory(selectedFiles[0]);
				return;
			}
		}

		if (approveSelectionImpl()) {
			iconManager.clearIconCache(); // To keep cache from growing huge.
			retVal = APPROVE_OPTION;
			dialog.setVisible(false);
		}

	}


	/**
	 * Called at the end of {@link #approveSelection()}; the file chooser
	 * dialog will only close if this method returns <code>true</code>.
	 * Subclasses can override this method to add things such as an "Are you
	 * sure you want to overwrite?" message.
	 *
	 * @return Whether to close this file chooser.  The default implementation
	 *         always returns <code>true</code>.
	 */
	protected boolean approveSelectionImpl() {
		return true;
	}


	/**
	 * Called when the user clicks the Cancel button.  You can also call this
	 * programatically.  Any file selections are nixed and the dialog closes.
	 */
	public void cancelSelection() {
		iconManager.clearIconCache(); // To keep cache from growing huge.
		selectedFiles = null;
		retVal = CANCEL_OPTION;
		dialog.setVisible(false);
	}


	/**
	 * Removes all values from the extension-to-color map.
	 *
	 * @see #getColorForExtension
	 * @see #setColorForExtension
	 */
	public void clearExtensionColorMap() {
		customColors.clear();
	}


	/**
	 * Removes all favorites from the "Favorites" list.
	 *
	 * @see #addToFavorites(String)
	 * @see #getFavorites()
	 * @see #loadFavorites(File)
	 * @see #saveFavorites(File)
	 */
	public void clearFavorites() {
		if (favoriteList!=null) {
			favoriteList.clear();
		}
	}


	private static final boolean containsFilesAndDirectories(Object[] files) {
		int num = files.length;
		boolean containsFile = false;
		boolean containsDirectory = false;
		for (int i=0; i<num; i++) {
			if (((File)files[i]).isDirectory())
				containsDirectory = true;
			else if (((File)files[i]).isFile())
				containsFile = true;
			if (containsDirectory && containsFile)
				return true;
		}
		return false;
	}


	private static final boolean containsOnlyDirectories(Object[] files) {
		int num = files.length;
		for (int i=0; i<num; i++) {
			if (!((File)files[i]).isDirectory())
				return false;
		}
		return true;
	}


	/**
	 * Creates actions for keystrokes in a file chooser dialog.
	 */
	private void createActions() {

		// We need to use the view's "selected files" for the edit/view actions.
		// Since our view can change, we wrap it in this class.
		FileSelector selector = new FileSelector() {
			public File getSelectedFile() {
				return getView().getSelectedFile();
			}
			public File[] getSelectedFiles() {
				return getView().getSelectedFiles();
			}
		};

		systemEditAction = new Actions.SystemOpenAction(selector, "edit");
		systemViewAction = new Actions.SystemOpenAction(selector, "open");
		renameAction = new Actions.RenameAction(this);
		copyAction = new Actions.CopyAction(this);
		deleteAction = new Actions.DeleteAction(this, false);
		hardDeleteAction = new Actions.DeleteAction(this, true);
		pasteAction = new Actions.PasteAction(this);
		refreshAction = new Actions.RefreshAction(this);
		upOneLevelAction = new Actions.UpOneLevelAction(this);
		propertiesAction = new Actions.PropertiesAction(this);

	}


	/**
	 * Creates a dialog for the given parent frame.
	 *
	 * @param parent The window that is to be the parent of the created dialog.
	 * @return The dialog.
	 */
	protected JDialog createDialog(Window parent) throws HeadlessException {

		Window wind = parent!=null ? parent : JOptionPane.getRootFrame();

		// NOTE: In 1.6, they (finally) added a JDialog(Window, boolean)
		// constructor that we could use instead of this silly conditional.
		JDialog dialog = (wind instanceof Frame) ?
			new JDialog((Frame)wind, true) : new JDialog((JDialog)wind, true);

		dialog.setContentPane(this);
		JRootPane rootPane = dialog.getRootPane();
		rootPane.setDefaultButton(acceptButton);

		if (JDialog.isDefaultLookAndFeelDecorated()) {
			boolean supportsWindowDecorations = 
				UIManager.getLookAndFeel().getSupportsWindowDecorations();
			if (supportsWindowDecorations)
				dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
		}

		// Make it so if they "x-out" the dialog, they effectively cancel it.
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancelSelection();//returnValue = CANCEL_OPTION;
			}
		});

		// Make the Escape key hide the dialog, etc.
		installActions(dialog);

		ComponentOrientation o = getComponentOrientation();
		dialog.applyComponentOrientation(o);
		return dialog;

	}


	/**
	 * Creates the right-click popup menu.
	 */
	private void createPopupMenu() {

		popupMenu = new JPopupMenu() {
			@Override
			public void show(Component c, int x, int y) {

				// Enable the file stuff (open, rename, ...) only if there
				// is a file highlighted.
				int count = view.getSelectedFiles().length;
				boolean filesSelected = count>0;
				((JMenuItem)getComponent(0)).setEnabled(filesSelected);
				((JMenuItem)getComponent(1)).setEnabled(
										IS_JAVA_6_PLUS && filesSelected);
				((JMenuItem)getComponent(2)).setEnabled(filesSelected);
				((JMenuItem)getComponent(4)).setEnabled(filesSelected);
				((JMenuItem)getComponent(5)).setEnabled(filesSelected);
				((JMenuItem)getComponent(6)).setEnabled(filesSelected);

				// Only enable the "Up one level" item if we can actually
				// go up a level.
				JMenuItem upOneLevel = (JMenuItem)getComponent(8);
				upOneLevel.setEnabled(upOneLevelButton.isEnabled());

				((JMenuItem)getComponent(12)).setEnabled(filesSelected);

				super.show(c, x,y);

			}
		};

		JMenuItem menuItem = new JMenuItem(openString);
		menuItem.setActionCommand("PopupOpen");
		menuItem.addActionListener(this);
		popupMenu.add(menuItem);

		JMenu subMenu = new JMenu(msg.getString("PopupMenu.OpenIn"));
		popupMenu.add(subMenu);
		subMenu.add(new JMenuItem(systemEditAction));
		subMenu.add(new JMenuItem(systemViewAction));

		popupMenu.add(new JMenuItem(renameAction));
		popupMenu.addSeparator();
		popupMenu.add(new JMenuItem(copyAction));
		popupMenu.add(new JMenuItem(pasteAction));
		popupMenu.add(new JMenuItem(deleteAction));
		popupMenu.addSeparator();
		popupMenu.add(new JMenuItem(upOneLevelAction));
		popupMenu.addSeparator();
		popupMenu.add(new JMenuItem(refreshAction));
		popupMenu.addSeparator();
		popupMenu.add(new JMenuItem(propertiesAction));

		ComponentOrientation o = getComponentOrientation();
		popupMenu.applyComponentOrientation(o);
		
	}

	synchronized void displayPopupMenu(JComponent view, int x, int y) {
		if (popupMenu==null)
			createPopupMenu();
		popupMenu.show(view, x,y);
	}


	private void ensureAbsoluteFilePaths(File[] files) {
		int num = files.length;
		for (int i=0; i<num; i++) {
			if (!files[i].isAbsolute()) {
				files[i] = new File(currentDirectory, files[i].getPath());
			}
		}
	}


	/**
	 * Makes sure that <code>currentDirectory</code> still exists.  If it
	 * doesn't, it is set to be the user's home directory.
	 */
	private void ensureCurrentDirectoryExists() {
		// If the "current directory" was removed from under us since the last
		// time we were displayed, we'll just default to the user's directory.
		if (!currentDirectory.isDirectory()) {
			currentDirectory = new File(System.getProperty("user.dir"));
		}
	}


	/**
	 * Focuses the "file name" text field.
	 *
	 * @param clear Whether the field's contents should be cleared.
	 */
	void focusFileNameField(boolean clear) {
		if (clear) {
			fileNameTextField.setText(null);
		}
		fileNameTextField.requestFocusInWindow();
	}


	/**
	 * Returns whether the filename text field auto-completes the next matching
	 * filename when the drop-down list is visible.  Note that this property
	 * is only honored when this text field is "file system aware".
	 *
	 * @see #setAutoCompleteFileNames(boolean)
	 */
	public boolean getAutoCompleteFileNames() {
		// We keep this property in a boolean instead of querying the
		// text field directly because the text field may not yet be
		// created.  We delay creation of GUI objects until a file chooser
		// is actually displayed, to help speed up the creation of the
		// file chooser option panel.
		return autoCompleteFileNames;
	}


	/**
	 * Returns the "canonical" path for a given directory.  For example, if
	 * the given file has a name "org", we will prepend this with the file
	 * chooser's <code>currentDirectory</code>.
	 *
	 * @param file The file for which you want a canonical path.
	 */
	private final File getCanonicalFileFor(File dir) {
		if (!dir.isAbsolute()) {
			dir = new File(currentDirectory, dir.getPath());
		}
		return dir;
	}


	/**
	 * Gets the list of user-choosable file filters.
	 *
	 * @return A <code>FileFilter</code> array containing all the choosable
	 *         file filters.
	 * @see #addChoosableFileFilter
	 * @see #removeChoosableFileFilter
	 */
	public FileFilter[] getChoosableFileFilters() {
		FileFilter[] filterArray = new FileFilter[fileFilters.size()];
		fileFilters.copyInto(filterArray);
		return filterArray;
	}


	/**
	 * Returns the color associated with a file type.  This method will take
	 * care of the extension's case (i.e., if running on Windows, all
	 * file extensions will be compared lower-case to ensure case doesn't
	 * matter).
	 *
	 * @param extension The extension for which to get the associated image.
	 * @return The color associated with this extension, or <code>null</code>
	 *         if there is none.
	 * @see #setColorForExtension
	 * @see #clearExtensionColorMap
	 */
	public Color getColorForExtension(final String extension) {
		return customColors.get(
				IGNORE_CASE ? extension.toLowerCase() : extension);
	}


	/**
	 * Returns the directory this <code>RTextFileChooser</code> is currently
	 * in.
	 *
	 * @return The current directory.
	 * @see #setCurrentDirectory
	 */
	public final File getCurrentDirectory() {
		return currentDirectory;
	}


	/**
	 * Returns the map containing all file extensions and the colors used
	 * to color the names of files of those types.  Note that changes
	 * to this map will cause changes to the file chooser (it is a shallow
	 * copy).<p>
	 * So, for example, with the returned <code>HashMap</code> you can iterate
	 * through its keys (the extensions) to get the color used by this
	 * file chooser to display files with those extensions.
	 *
	 * @return The hash map of file extensions to file information.
	 */
	public Map<String, Color> getCustomColorsMap() {
		return customColors;
	}


	/**
	 * Returns the custom title to use for this file chooser.
	 *
	 * @return The custom title, or <code>null</code> if default values for
	 *         "Open" and "Save" dialogs should be used.
	 * @see #setCustomTitle(String)
	 */
	public String getCustomTitle() {
		return customTitle;
	}


	/**
	 * Returns the default encoding for this operating system.
	 *
	 * @return The default encoding.
	 */
	public static final String getDefaultEncoding() {
		// TODO: Change to "Charset.defaultCharset().name() when 1.4 support
		// is no longer needed.
		// NOTE:  file.encoding is not required to be set, so we provide a
		// fallback method for determing default encoding.
		String encoding = System.getProperty("file.encoding");
		if (encoding==null) {
			try {
				File f = File.createTempFile("rtext", null);
				FileWriter w = new FileWriter(f);
				encoding = w.getEncoding();
				w.close();
				f.delete();
			} catch (IOException ioe) {
				encoding = "US-ASCII";
			}
		}
		return encoding;
	}


	/**
	 * Returns the color used to paint the name of files with unknown type.
	 *
	 * @return The color used.
	 */
	public Color getDefaultFileColor() {
		// We cheat here and return a label's foreground if our view has not
		// yet been created.  This happens when the user opens the options
		// dialog before this component.  This isn't a big deal since, in
		// practice, all LookAndFeels set list/table foregrounds to the same
		// value as label foregrounds, and the option panel doesn't allow the
		// user to change the value.
		return view!=null ? view.getDefaultFileColor() :
			new JLabel().getForeground();
	}


	/**
	 * Returns the description for the file displayed in the file chooser.
	 *
	 * @param file The file for which to get the description.
	 * @return The description.
	 */
	public String getDescription(File file) {
		String desc = fileSystemView.getSystemTypeDescription(file);
		if (desc==null) {
			if (file.isDirectory())
				desc = directoryText;
			else
				desc = fileText;
		}
		return desc;
	}


	/**
	 * Returns the last type of dialog displayed.
	 *
	 * @return Either {@link #OPEN_DIALOG} or {@link #SAVE_DIALOG}.
	 * @see #showOpenDialog(Window)
	 * @see #showSaveDialog(Window)
	 */
	public int getDialogType() {
		return lastType;
	}


	/**
	 * Returns a string representing the encoding chosen by the user.
	 *
	 * @return A string representing the chosen encoding.
	 * @see #setEncoding
	 */
	public String getEncoding() {
		if (encoding==null)
			encoding = RTextFileChooser.getDefaultEncoding();
		return encoding;
	}


	/**
	 * Checks whether a file is Unicode, and returns the proper encoding if
	 * so.  Otherwise, returns the system default encoding.
	 *
	 * @param file The file to check.
	 * @return A best guess at the file's encoding (Unicode or system default).
	 * @throws IOException If an error occurs reading the file.
	 */
	private static String getEncodingOf(File file) throws IOException {

		byte bom[] = new byte[4];
		int n;
		FileInputStream in = new FileInputStream(file);
		try {
			n = in.read(bom, 0, bom.length);
		} finally {
			in.close();
		}
		String encoding = null;

		if ((bom[0]==(byte)0x00) && (bom[1]==(byte)0x00) &&
				(bom[2]==(byte)0xFE) && (bom[3]==(byte)0xFF)) {
			encoding = "UTF-32BE";
		}

		else if (n==4 && // Last 2 bytes are 0; could be an empty UTF-16
				(bom[0]==(byte)0xFF) && (bom[1]==(byte)0xFE) &&
				(bom[2]==(byte)0x00) && (bom[3]==(byte)0x00)) {
			encoding = "UTF-32LE";
		}
		
		else if ((bom[0]==(byte)0xEF) &&
			(bom[1]==(byte)0xBB) &&
			(bom[2]==(byte)0xBF)) {
			encoding = "UTF-8";
		}

		else if ((bom[0]==(byte)0xFE) && (bom[1] == (byte)0xFF)) {
			encoding = "UTF-16BE";
		}

		else if ((bom[0]==(byte)0xFF) && (bom[1]== (byte)0xFE)) {
			encoding = "UTF-16LE";
		}

		else {
			encoding = getDefaultEncoding();
		}

		return encoding;

	}


	/**
	 * Returns the "Favorite directories" of this file chooser, as an array
	 * of strings.  Modifying this array does not modify the favorites used
	 * by this file chooser.
	 *
	 * @return An array of "favorites".  If no favorites are known, a
	 *         zero-length array is returned.
	 * @see #addToFavorites(String)
	 * @see #clearFavorites()
	 * @see #loadFavorites(File)
	 * @see #saveFavorites(File)
	 */
	public String[] getFavorites() {
		if (favoriteList==null) {
			return new String[0];
		}
		String[] array = new String[favoriteList.size()];
		return favoriteList.toArray(array);
	}


	/**
	 * Returns the currently-active file filter.
	 *
	 * @return The currently active file filter.
	 * @see #setFileFilter
	 */
	public FileFilter getFileFilter() {
		return currentFileFilter;
	}


	/**
	 * Returns the file selection mode.
	 *
	 * @return One of <code>FILES_ONLY</code>, <code>DIRECTORIES_ONLY</code>,
	 *         or <code>FILES_AND_DIRECTORIES</code>.
	 * @see #setFileSelectionMode
	 */
	public int getFileSelectionMode() {
		return fileSelectionMode;
	}


	/**
	 * Returns the files from the "File Name" combo box.  This method is
	 * called when the user clicks the accept button but has no files
	 * highlighted in the view.
	 */
	private File[] getFilesFromFileNameTextField() {

		File[] files = null;

		String text = fileNameTextField.getText();
		if (text==null)
			return null;
		text = text.trim();
		if (text.length()==0)
			return null;

		// Just a single file...
		if (text.charAt(0)!='"') {

			// Create the file.
			files = new File[1];
			files[0] = fileSystemView.createFileObject(text);

			// Make real file name for the "exists" call below.
			if (!files[0].isAbsolute())
				files[0] = fileSystemView.getChild(currentDirectory, text);

			// If the file doesn't exist and contains wildcards, do a
			// wildcard filter!
			if (!files[0].exists() && isGlobPattern(text)) {
				if (globFilter==null)
					globFilter = new WildcardFileFilter();
				try {
					globFilter.setPattern(text);
					refreshView(true); // Show all files matching this pattern.
					fileNameTextField.setFileSystemAware(false);
					fileNameTextField.setText(null);
					fileNameTextField.setFileSystemAware(true);
				} catch (Exception e) {
					// Invalid pattern was entered...
					JOptionPane.showMessageDialog(this, "Invalid pattern: " + text +
						"\n" + e,
						errorDialogTitle, JOptionPane.ERROR_MESSAGE);
				}
				return null; // This is called from actionPerformed and will simply bail out.
			} // End of if (!files[0].exists() && isGlobPattern(files[0])).

		} // End of if (text.charAt(0)!='"').

		// Multiple files surrounded by '"''s.
		else {

			ArrayList<String> fileNames = new ArrayList<String>();
			int i;

			// Parse the text for filenames in '"''s.
			while ( (i = text.indexOf('"', 1)) != -1 ) {
				fileNames.add(text.substring(1,i));
				text = text.substring(i);	// => '" "File2.txt"'.
				i = text.indexOf('"', 1);	// => j == 2.
				if (i==-1) {
					if (text.length()>1)
						return null;		// i.e., text was originally '"File1.txt" File2.txt'
					text = "";			// To trick part below.
					break;
				}
				text = text.substring(i);	// => '"file2.txt"'.
			}
			// "Leftovers" if they left off the ending '"'.
			if (text.length()>1)
				fileNames.add(text.substring(1));

			int numFileNames = fileNames.size();
			files = new File[numFileNames];
			for (i=0; i<numFileNames; i++)
				files[i] = new File(fileNames.get(i));

		}

		return files;

	}


	/**
	 * Returns whether or not the filename text field is file-system aware.
	 *
	 * @return Whether the filename text field provides possible matches
	 *        while you are typing in it.
	 * @see #setFileSystemAware
	 */
	public boolean getFileSystemAware() {
		// We keep this property in a boolean instead of querying the
		// text field directly because the text field may not yet be
		// created.  We delay creation of GUI objects until a file chooser
		// is actually displayed, to help speed up the creation of the
		// file chooser option panel.
		return fileSystemAware;
	}


	/**
	 * Returns the file system view for this file chooser.
	 *
	 * @return The file system view.
	 */
	public FileSystemView getFileSystemView() {
		return fileSystemView;
	}


	/**
	 * Returns the file type info (color) to use when painting
	 * the specified file in this file chooser.
	 *
	 * @param file The file.
	 * @return The file's color to use when painting it.  The color is
	 *         the color with which to paint the file's name when it is not
	 *         "selected" in the view.
	 */
	public FileTypeInfo getFileTypeInfoFor(File file) {

		if (file.isDirectory()) {
			tempInfo.labelTextColor = getDefaultFileColor();
		}
		else {
			// If there's an extension, check for a custom color.
			String fileName = file.getName();
			String extension = Utilities.getExtension(fileName);
			if (extension!=null && extension.length()>0) {
				Color color = getColorForExtension(extension);
				if (color==null)
					color = getDefaultFileColor();
				tempInfo.labelTextColor = color;
			}
			// No extension => use defaults.
			else {
				tempInfo.labelTextColor = getDefaultFileColor();
			}
		}

		tempInfo.icon = iconManager.getIcon(file);
		return tempInfo;

	}


	/**
	 * Returns the color used to paint hidden files if hidden files are being
	 * shown.
	 *
	 * @return The color used to paint hidden file names.
	 * @see #setHiddenFileColor
	 */
	public Color getHiddenFileColor() {
		return hiddenFileColor;
	}


	/**
	 * Creates the icons used by this file chooser.  This method can be
	 * called when the user changes the Look and Feel, and icons used in this
	 * file chooser will change to reflect the new Look and Feel.
	 */
	public void getIcons() {

		// Icons for buttons at the top.  Use pretty, homemade defaults
		// if any of these aren't found.
		newFolderIcon    = UIManager.getIcon("FileChooser.newFolderIcon");
		upFolderIcon     = UIManager.getIcon("FileChooser.upFolderIcon");
		//homeFolderIcon   = UIManager.getIcon("FileChooser.homeFolderIcon");
		detailsViewIcon  = UIManager.getIcon("FileChooser.detailsViewIcon");
		listViewIcon     = UIManager.getIcon("FileChooser.listViewIcon");

		ClassLoader cl = this.getClass().getClassLoader();
		String path = "org/fife/ui/rtextfilechooser/images/";
		if (newFolderIcon==null) {
			newFolderIcon = new ImageIcon(cl.getResource(path + "createnewdirectory.gif"));
		}
		if (upFolderIcon==null) {
			upFolderIcon = new ImageIcon(cl.getResource(path + "uponelevel.gif"));
		}
		if (detailsViewIcon==null) {
			detailsViewIcon = new ImageIcon(cl.getResource(path + "detailsview.gif"));
		}
		if (listViewIcon==null) {
			listViewIcon = new ImageIcon(cl.getResource(path + "listview.gif"));
		}
		iconsViewIcon = new ImageIcon(cl.getResource(path + "listview.gif"));
		if (favoritesIcon==null) {
			favoritesIcon = new ImageIcon(cl.getResource(path + "book.png"));
		}

	}


	/**
	 * Returns the mnemonic for the given key.
	 *
	 * @param key The key.
	 * @return The mnemonic.
	 */
	private static final int getMnemonic(String key) {

		Object value = UIManager.get(key);

		if (value == null)
			return 0;
		else if (value instanceof Integer) {
			return ((Integer)value).intValue();
		}
		else if (value instanceof String) {
			try {
				return Integer.parseInt((String)value);
			} catch (NumberFormatException nfe) { }
		}

		return 0;

	}


	/**
	 * Returns the name of the file.
	 *
	 * @param f The file for which you want its name.
	 * @return The file's name.
	 */
	public String getName(File f) {
		return FileDisplayNames.get().getName(f);
	}


	/**
	 * Returns the special styling used when displaying the names of
	 * opened files.
	 *
	 * @return The style.  This will be one of <code>STYLE_BOLD</code>,
	 *         <code>STYLE_ITALIC</code> or <code>STYLE_UNDERLINE</code>.
	 * @see #setOpenFilesStyle(int)
	 * @see #getStyleOpenFiles()
	 */
	public int getOpenFilesStyle() {
		return openFilesStyle;
	}


	/**
	 * Returns the file selected by the user.  Note that if the user
	 * selected more than one file, only the first file selected is
	 * returned.
	 *
	 * @return The file selected by the user.
	 * @see #getSelectedFiles()
	 * @see #setSelectedFile(File)
	 * @see #setSelectedFiles(File[])
	 */
	public File getSelectedFile() {
		return selectedFiles[0];
	}


	/**
	 * Returns the files selected by the user.
	 *
	 * @return The files selected by the user.
	 * @see #getSelectedFile
	 * @see #setSelectedFile
	 * @see #setSelectedFiles
	 */
	public File[] getSelectedFiles() {
		return selectedFiles.clone();
	}


	/**
	 * Returns whether hidden files and directories are shown by the file
	 * chooser.
	 *
	 * @return Whether hidden files are displayed.
	 * @see #setShowHiddenFiles(boolean)
	 */
	public boolean getShowHiddenFiles() {
		return showHiddenFiles;
	}


	/**
	 * Returns the localized text for a given key.
	 *
	 * @param key The key.
	 * @return the localized text.
	 */
	public String getString(String key) {
		return msg.getString(key);
	}


	/**
	 * Returns whether "opened" files should have a special style applied
	 * to them when they are displayed in this file chooser (for example,
	 * being underlined).
	 *
	 * @return Whether opened files should have a special style applied to
	 *         them.
	 * @see #setStyleOpenFiles(boolean)
	 * @see #setOpenFilesStyle(int)
	 */
	public boolean getStyleOpenFiles() {
		return styleOpenFiles;
	}


	/**
	 * Returns a string to use as the tool tip for a file in a view.  This
	 * tool tip will be localized according to the current locale.
	 *
	 * @param file The file for which to get the tool tip.
	 * @return The tool tip.
	 */
	String getToolTipFor(File file) {
		return "<html><body>&nbsp;" + nameString + file.getName() +
			"<br>&nbsp;" + lastModifiedString +
			Utilities.getLastModifiedString(file.lastModified()) +
			"<br>&nbsp;" + sizeString + Utilities.getFileSizeStringFor(file) +
			"</body></html>";
	}


	/**
	 * Returns the view used by this file chooser.
	 *
	 * @return The view.
	 */
	RTextFileChooserView getView() {
		return view;
	}


	/**
	 * Returns the view mode.
	 *
	 * @return One of {@link #LIST_MODE}, {@link #DETAILS_MODE}, or
	 *         {@link #ICONS_MODE}.
	 * @see #setViewMode(int)
	 */
	public int getViewMode() {
		return mode;
	}


	/**
	 * Creates and installs keyboard actions for this file chooser dialog,
	 * such as "F2" => Rename file.
	 *
	 * @param dialog The dialog in which to install the actions.
	 * @see #createActions()
	 */
	private void installActions(JDialog dialog) {

		JRootPane rootPane = dialog.getRootPane();
		InputMap inputMap = rootPane.getInputMap(
								JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = rootPane.getActionMap();

		// Make the Escape key hide the dialog, etc.
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "OnEsc");
		actionMap.put("OnEsc",new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					cancelButton.doClick(0);
				}
			}
		);

		KeyStroke ks = (KeyStroke)renameAction.getValue(Action.ACCELERATOR_KEY);
		inputMap.put(ks, "OnRename");
   		actionMap.put("OnRename", renameAction);

		ks = (KeyStroke)deleteAction.getValue(Action.ACCELERATOR_KEY);
		inputMap.put(ks, "OnDelete");
		actionMap.put("OnDelete", deleteAction);

		ks = (KeyStroke)hardDeleteAction.getValue(Action.ACCELERATOR_KEY);
		inputMap.put(ks, "OnHardDelete");
		actionMap.put("OnHardDelete", hardDeleteAction);

		ks = (KeyStroke)pasteAction.getValue(Action.ACCELERATOR_KEY);
		inputMap.put(ks, "OnPaste");
		actionMap.put("OnPaste", pasteAction);

		// Have Backspace go "up one level."
		// This causes problems on OS X (action occurs even it back space is
		// done in a text field - text field processes the key stroke, then the
		// dialog does too!).
		if (System.getProperty("os.name").toLowerCase().indexOf("os x")==1) {
			ks = (KeyStroke)upOneLevelAction.getValue(Action.ACCELERATOR_KEY);
			inputMap.put(ks, "OnBackspace");
			actionMap.put("OnBackspace", upOneLevelAction);
		}

		// Have F5 refresh the displayed files.
		ks = (KeyStroke)refreshAction.getValue(Action.ACCELERATOR_KEY);
		inputMap.put(ks, "OnRefresh");
		actionMap.put("OnRefresh", refreshAction);

		// Alt+Enter shows the properties of the selected files.
		ks = (KeyStroke)propertiesAction.getValue(Action.ACCELERATOR_KEY);
		inputMap.put(ks, "OnProperties");
		actionMap.put("OnProperties", propertiesAction);

	}


	/**
	 * Install strings needed specifically by the "Details View."  This
	 * is done separately from the rest of the strings due to poor design.
	 */
	private void installDetailsViewStrings() {
		nameString = getString("Name");
		sizeString = getString("Size");
		typeString = getString("Type");
		statusString = getString("Status");
		lastModifiedString = getString("LastModified");
	}


	/**
	 * Gets the strings used on the various file chooser widgets.
	 */
	private void installStrings() {

		saveButtonText   = UIManager.getString("FileChooser.saveButtonText");
		openButtonText   = UIManager.getString("FileChooser.openButtonText");
		saveDialogTitleText = UIManager.getString("FileChooser.saveDialogTitleText");
		openDialogTitleText = UIManager.getString("FileChooser.openDialogTitleText");

		saveButtonMnemonic   = getMnemonic("FileChooser.saveButtonMnemonic");
		openButtonMnemonic   = getMnemonic("FileChooser.openButtonMnemonic");

		saveButtonToolTipText   = UIManager.getString("FileChooser.saveButtonToolTipText");
		openButtonToolTipText   = UIManager.getString("FileChooser.openButtonToolTipText");

		// Populate the combo box with all available encodings.
		if (encodingCombo!=null) {
			Map<String, Charset> availcs = Charset.availableCharsets();
			for (String key : availcs.keySet()) {
				encodingCombo.addItem(key);
			}
			encodingCombo.addItemListener(itemListener);
		}

		newFolderPrompt = getString("NewFolderPrompt");
		errorNewDirPrompt = getString("ErrorNewDirPrompt");
		errorDialogTitle = getString("Error");

		directoryText = getString("Directory"); // "Directory"
		fileText = getString("File"); // "File"

		openString = getString("PopupMenu.Open");

		noFavoritesDefinedString = getString("NoFavoritesDefined");
		favoriteDNERemoveString = getString("FavoriteDoesNotExistRemoveIt");

	}


	/**
	 * Returns whether or not this filename contains wildcards, depending on
	 * the OS on which we're running.
	 *
	 * @param filename The filename to check for wildcards.
	 * @return Whether or not the file name contained wildcards.
	 */
	private static boolean isGlobPattern(String filename) {
		return ((File.separatorChar == '\\' && filename.indexOf('*') >= 0)
				|| (File.separatorChar == '/' && (filename.indexOf('*') >= 0
				|| filename.indexOf('?') >= 0
				|| filename.indexOf('[') >= 0)));
	}


	/**
	 * Returns whether or not multi-selection is enabled.
	 *
	 * @return Whether or not multi-selection is enabled.
	 * @see #setMultiSelectionEnabled
	 */
	public final boolean isMultiSelectionEnabled() {
		return multiSelectionEnabled;
	}


	/**
	 * Returns whether or not a file should be underlined when displayed by
	 * a view of this file chooser.
	 *
	 * @param file A file.
	 * @return Whether or not <code>file</code> should be underlined by the
	 *         view.
	 * @see #setOpenedFiles(File[])
	 */
	public boolean isOpenedFile(File file) {
		int num = openedFiles!=null ? openedFiles.length : 0;
		for (int i=0; i<num; i++) {
			if (file.equals(openedFiles[i]))
				return true;
		}
		return false;
	}


	/**
	 * Loads a list of "Favorites" from a file.  This should be a plain
	 * text, UTF-8 file, with one favorite (full path) listed per-line.
	 * Empty lines and lines starting with the <code>#</code>' character
	 * are ignored.
	 *
	 * @param file The favorites file to load.
	 * @return The number of favorites added.
	 * @throws IOException If an IO error occurs.
	 * @see #addToFavorites(String)
	 * @see #clearFavorites()
	 * @see #getFavorites()
	 * @see #saveFavorites(File)
	 */
	public int loadFavorites(File file) throws IOException {
		int count = 0;
		String line = null;
		BufferedReader r = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), FAVORITES_ENCODING));
		try {
			while ((line=r.readLine())!=null) {
				if (line.length()>0 && !line.startsWith("#")) {
					addToFavorites(line);
					count++;
				}
			}
		} finally {
			r.close();
		}
		return count;
	}


	/**
	 * Ensures that the "File Filter" combo box contains all file filters
	 * known by this file dialog.
	 */
	private void populateFilterComboBox() {
		filterCombo.removeAllItems();
		int max = fileFilters.size();
		for (int i=0; i<max; i++)
			filterCombo.addItem(fileFilters.get(i));
	}


	/**
	 * Populates the "Look in" combo box with proper values for the current
	 * directory.
	 */
	private void populateLookInComboBox() {
		lookInBreadcrumbBar.setShownLocation(currentDirectory);
	}


	/**
	 * Called when a property we're listening to changes.
	 *
	 * @param e The event.
	 */
	public void propertyChange(PropertyChangeEvent e) {

		String name = e.getPropertyName();

		if (BreadcrumbBar.PROPERTY_LOCATION.equals(name)) {
			setCurrentDirectory(lookInBreadcrumbBar.getShownLocation());
		}

	}


	/**
	 * If the GUI has been initialized, this sets the encoding combo box to
	 * display the proper value.
	 */
	protected void refreshEncodingComboBox() {

		if (!guiInitialized || encodingCombo==null) {
			return;
		}

		if (encoding==null) {
			encoding = getDefaultEncoding();
		}
		Charset cs1 = Charset.forName(encoding);
		int count = encodingCombo.getItemCount();
		for (int i=0; i<count; i++) {
			String item = (String)encodingCombo.getItemAt(i);
			Charset cs2 = Charset.forName(item);
			if (cs1.equals(cs2)) {
				encodingCombo.setSelectedIndex(i);
				return;
			}
		}

		// Encoding not found: select default.
		String defaultEncoding = getDefaultEncoding();
		cs1 = Charset.forName(defaultEncoding);
		for (int i=0; i<count; i++) {
			String item = (String)encodingCombo.getItemAt(i);
			Charset cs2 = Charset.forName(item);
			if (cs1.equals(cs2)) {
				encodingCombo.setSelectedIndex(i);
				return;
			}
		}

	}


	/**
	 * Refreshes either the list view or table view, whichever is visible.
	 */
	final void refreshView() {
		refreshView(false);
	}


	/**
	 * Refreshes the view (list, table, or details).
	 * This should be called with a parameter of <code>true</code> when the
	 * user types in a wildcard expression for a file name.
	 *
	 * @param useGlobFilter Whether or not to use the glob file filter.
	 */
	private final void refreshView(boolean useGlobFilter) {

		if (!guiInitialized)
			return;

		ensureCurrentDirectoryExists();

		File[] files = fileSystemView.getFiles(currentDirectory,
						!showHiddenFiles); // "useFileHiding".

		if (files!=null) {

			int num = files.length;
			Vector<File> dirList = new Vector<File>();
			Vector<File> fileList = new Vector<File>();

			// First, separate the directories from regular files so we can
			// sort them individually.  This part of the code could be made
			// more compact, but it isn't just for a tad more speed.
			FileFilter filter = (useGlobFilter ? globFilter : currentFileFilter);
			if (fileSelectionMode==DIRECTORIES_ONLY) {
				for (int i=0; i<num; i++) {
					if (files[i].isDirectory())
						dirList.add(files[i]);
				}
			}
			else { // FILES_AND_DIRECTORIES or FILES_ONLY.
				for (int i=0; i<num; i++) {
					if (files[i].isDirectory())
						dirList.add(files[i]);
					else if (filter.accept(files[i]))
						fileList.add(files[i]);
				}
			}


			// Details mode automagically sorts its data via the table's model;
			// however, list mode doesn't, so we'll go ahead and sort for it.
			if (mode!=DETAILS_MODE) {
				Collections.sort(fileList);
				Collections.sort(dirList);
			}

			if (fileSelectionMode!=DIRECTORIES_ONLY)
				dirList.addAll(fileList);

			view.setDisplayedFiles(dirList);

		}
		else { // files==null.
			view.clearDisplayedFiles();
		}

		// Ensure JScrollPane is at "beginning" and clear any selection.
		JViewport viewport = viewScrollPane.getViewport();
		if (viewScrollPane.getComponentOrientation().isLeftToRight()) {
			viewport.setViewPosition(new Point(0,0));
		}
		else {
			Dimension size = viewport.getViewSize();
			viewport.setViewPosition(new Point(size.width, 0));
		}
		view.clearSelection();

	}


	/**
	 * Removes a filter from the list of user choosable file filters.
	 *
	 * @return <code>true</code> if the file filter was removed.
	 *
	 * @see #addChoosableFileFilter
	 * @see #getChoosableFileFilters
	 */ 
	public boolean removeChoosableFileFilter(FileFilter f) {
		if (fileFilters.contains(f)) {
			if(getFileFilter() == f)
				setFileFilter(null);
			fileFilters.removeElement(f);
			return true;
		}
		return false;
	}


	/**
	 * Saves the list of "Favorites" of this file chooser to a file.
	 * The format of the file is the same as that specified in the description
	 * of {@link #loadFavorites(File)}.  If there are no Favorites for this
	 * file chooser, an empty file is created.
	 *
	 * @param file The file to write to.  If it already exists, it will be
	 *        overwritten.
	 * @throws IOException If an IO error occurs writing the file.
	 * @see #addToFavorites(String)
	 * @see #clearFavorites()
	 * @see #getFavorites()
	 * @see #loadFavorites(File)
	 */
	public void saveFavorites(File file) throws IOException {
		PrintWriter w = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file),
										FAVORITES_ENCODING)));
		try {
			if (favoriteList!=null) {
				for (String favorite : favoriteList) {
					w.println(favorite);
				}
			}
		} finally {
			w.close();
		}
	}


	/**
	 * Saves this file chooser's preferences (colors used for filenames, etc.).
	 * A program should call this method when shutting down if it wishes for
	 * its file chooser to have the same properties the next time the
	 * application is started.
	 */
	public void savePreferences() {
		FileChooserPreferences.save(this);
	}


	/**
	 * Sets whether the filename text field auto-completes the next matching
	 * filename when the drop-down list is visible.  Note that this property
	 * is only honored when this text field is "file system aware".
	 *
	 * @param auto Whether the next matching filename is auto-inserted.
	 * @see #getAutoCompleteFileNames()
	 */
	public void setAutoCompleteFileNames(boolean auto) {
		// We keep this property in a boolean instead of querying the
		// text field directly because the text field may not yet be
		// created.  We delay creation of GUI objects until a file chooser
		// is actually displayed, to help speed up the creation of the
		// file chooser option panel.
		this.autoCompleteFileNames = auto;
		if (fileNameTextField!=null) {
			fileNameTextField.setAutoCompleteFileName(auto);
		}
	}


	/**
	 * Sets the color associated with a file type.  This method will take
	 * care of the extension's case (i.e., if running on Windows, all
	 * file extensions will be compared lower-case to ensure case doesn't
	 * matter).
	 *
	 * @param extension The extension for which to set the associated color.
	 * @param color The new color to associate with this extension.  A value
	 *        of <code>null</code> makes the default color used.
	 * @see #getColorForExtension
	 * @see #clearExtensionColorMap
	 */
	public void setColorForExtension(String extension, Color color) {
		if (IGNORE_CASE) {
			extension = extension.toLowerCase();
		}
		customColors.put(extension, color);
	}


	/**
	 * Sets the directory this <code>RTextFileChooser</code> is currently in.
	 *
	 * @param dir The new current directory.  If this value isn't a valid
	 *        directory, The user's home directory is used.
	 * @see #getCurrentDirectory
	 */
	public void setCurrentDirectory(File dir) {

		// We update several pieces of this dialog box that have listeners
		// attached to them that do things like call setCurrentDirectory.
		// So, to keep from going into an infinite loop, we set this state
		// variable to true so all listeners know not to do stuff.
		isChangingDirectories = true;

		if (guiInitialized) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		// We need to check if it is a root because, for example, if they
		// choose "A:" on a PC but there is no disk in the drive,
		// dir.isDirectory() returns false.
		if (dir.isDirectory() || RootManager.getInstance().isRoot(dir)) {
			// We need to get the canonical path for when the user types
			// ".." or ".", etc.
			try {
				currentDirectory = new File(dir.getCanonicalPath());
			} catch (Exception e) {
				// This happens, for example, when the user selects "A:\"
				// on Windows and the drive doesn't contain a disk.
				if (RootManager.getInstance().isRoot(dir)) // As in the case of A:\ above.
					currentDirectory = new File(dir.getAbsolutePath());
				else
					currentDirectory = new File(System.getProperty("user.dir"));
			}
		}
		else
			currentDirectory = new File(System.getProperty("user.dir"));

		// We erase any selected files and refresh the list display.
		selectedFiles = null;

		if (guiInitialized) {
			refreshView();

			// Enable/disable the "Up one level" button appropriately.
			File parentFile = currentDirectory.getParentFile();
			upOneLevelAction.setEnabled(parentFile!=null &&
						parentFile.isDirectory() && parentFile.canRead());

			// Enable the "Create new folder" button iff we can write to
			// this directory.
			// FIXME:  Always returns false?
			//newFolderButton.setEnabled(fileSystemView.createFileObject(
			//					currentDirectory, "foo").canWrite());

			// Update the "Look in" combo box.
			populateLookInComboBox();

			// Set the listed "selected" files to nothing.
			fileNameTextField.setText(null);
			fileNameTextField.requestFocusInWindow();

			// Make the "Accept" button disabled.
			acceptButton.setEnabled(false);

			// Make the file name combo box use the same current directory.
			fileNameTextField.setCurrentDirectory(currentDirectory);

			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		}

		isChangingDirectories = false;

	}


	/**
	 * Sets the directory this <code>RTextFileChooser</code> is currently in.
	 *
	 * @param dir The new current directory.  If this value isn't a valid
	 *        directory, The user's home directory is used.
	 * @see #getCurrentDirectory
	 */
	public void setCurrentDirectory(String dir) {
		setCurrentDirectory(new File(dir));
	}


	/**
	 * Returns the title to use for this file chooser, whether it is an open
	 * dialog or a save dialog.
	 *
	 * @param title The new title.  If this is <code>null</code>, a default
	 *        title will be used appropriately (e.g. a localized version of
	 *        "Open" or "Save").
	 * @see #getCustomTitle()
	 */
	public void setCustomTitle(String title) {
		customTitle = title;
	}


	/**
	 * Sets the encoding selected by this file chooser.
	 *
	 * @param encoding The desired encoding.  If this value is invalid or
	 *        not supported by this OS, a system default is used.
	 * @see #getEncoding()
	 */
	public void setEncoding(String encoding) {
		if (encoding==null) {
			encoding = getDefaultEncoding();
		}
		if (!encoding.equals(this.encoding)) {
			this.encoding = encoding;
			refreshEncodingComboBox();
		}
	}


	/**
	 * Sets the current file filter. The file filter is used by the
	 * file chooser to filter out files from the user's view.
	 *
	 * @param filter The new current file filter to use.  If this is
	 *        <code>null</code>, the "all files" filter is selected.
	 * @see #getFileFilter()
	 */
	public void setFileFilter(FileFilter filter) {
		setFileFilterImpl(filter, true);
	}


	/**
	 * Sets the current file filter. The file filter is used by the
	 * file chooser to filter out files from the user's view.
	 *
	 * @param filter The new current file filter to use.  If this is
	 *        <code>null</code>, the "all files" filter is selected.
	 * @param cacheIfGUINotRealized If the GUI isn't created, whether or not
	 *        the selected file filter should be set to this filter when it
	 *        is created.  This parameter is here because
	 *        <code>addChoosableFileFilter</code> calls this method, but that
	 *        method should not change the selected file filter.
	 * @see #getFileFilter
	 */
	protected void setFileFilterImpl(FileFilter filter,
							boolean cacheIfGUINotRealized) {

		if (filter==null) {
			if (acceptAllFilter==null) {
				acceptAllFilter = new AcceptAllFileFilter();
			}
			filter = acceptAllFilter;
		}

		// Add the file filter to the filter combo if it isn't already there.
		if (filter!=null && !fileFilters.contains(filter)) {
			int size = fileFilters.size();
			if (size==0)
				fileFilters.add(filter);
			else
				fileFilters.insertElementAt(filter, size);
			if (guiInitialized) {
				populateFilterComboBox();
			}
		}

		// Everything below this is GUI-related, so don't do it if the GUI
		// hasn't been created yet.
		if (!guiInitialized) {
			if (cacheIfGUINotRealized)
				filterToSelect = filter;
			return;
		}

		filterCombo.setSelectedItem(filter);

		if (filter != null) {
			if (isMultiSelectionEnabled() && selectedFiles!=null &&
					selectedFiles.length>0)
			{
				List<File> fList = new ArrayList<File>();
				boolean failed = false;
				int num = selectedFiles.length;
				for (int i=0; i<num; i++) {
					if (filter.accept(selectedFiles[i])) {
						fList.add(selectedFiles[i]);
					}
					else
						failed = true;
				}
				if (failed)
					setSelectedFiles((fList.size() == 0) ? null :
						fList.toArray(new File[fList.size()]));
			}
			else if (selectedFiles!=null && selectedFiles.length>0 &&
				selectedFiles[0]!=null && !filter.accept(selectedFiles[0]))
				setSelectedFile(null);
		}

	}


	/**
	 * Sets whether the user can select files only, directories only, or
	 * both files and directories from this file chooser.
	 *
	 * @param mode One of <code>FILES_ONLY</code>, <code>DIRECTORIES_ONLY</code>,
	 *        or <code>FILES_AND_DIRECTORIES</code>.
	 * @see #getFileSelectionMode
	 */
	public void setFileSelectionMode(int mode) {
		if (fileSelectionMode==mode)
			return;
		if (mode!=FILES_ONLY && mode!=DIRECTORIES_ONLY && mode!=FILES_AND_DIRECTORIES)
			mode = FILES_ONLY;
		fileSelectionMode = mode;
		refreshView();
	}


	/**
	 * Sets whether the filename text field is "file system aware."
	 *
	 * @param aware Whether the filename text field will be helpful and
	 *        provide possible filename matches while you are typing in it.
	 *        This feature can be disabled for slow networks.
	 * @see #getFileSystemAware
	 */
	public void setFileSystemAware(boolean aware) {
		// We keep this property in a boolean instead of querying the
		// text field directly because the text field may not yet be
		// created.  We delay creation of GUI objects until a file chooser
		// is actually displayed, to help speed up the creation of the
		// file chooser option panel.
		fileSystemAware = aware;
		if (fileNameTextField!=null)
			fileNameTextField.setFileSystemAware(aware);
	}


	/**
	 * Sets the color used to display hidden files' names.
	 *
	 * @param color The new color to use for the names of hidden files.
	 * @throws NullPointerException If <code>color</code> is <code>null</code>.
	 * @see #getHiddenFileColor
	 */
	public void setHiddenFileColor(Color color) {
		if (color==null)
			throw new NullPointerException("Hidden file color cannot be " +
									"null");
		hiddenFileColor = color;
	}


	/**
	 * Adds a color to associate with a file type.  This method will take
	 * care of the extension's case (i.e., if running on Windows, all file
	 * extensions will be compared lower-case to ensure case doesn't
	 * matter).
	 *
	 * @param extension The extension to associate an image and color with.
	 * @param c The color to associate with this extension.
	 */
	public void setInfoForExtension(final String extension, final Color c) {
		String ext = IGNORE_CASE ? extension.toLowerCase() : extension;
		customColors.put(ext, c);
	}


	/**
	 * Sets whether or not multi-selection is enabled.
	 *
	 * @param enabled Whether or not multi-selection is to be enabled.
	 * @see #isMultiSelectionEnabled
	 */
	public void setMultiSelectionEnabled(boolean enabled) {
		multiSelectionEnabled = enabled;
		if (guiInitialized)
			view.setMultiSelectionEnabled(enabled);
	}


	/**
	 * Sets the list of files to be underlined if displayed in the file
	 * chooser.  This is useful if, for example, you wish to underline the
	 * files that are currently already open in a text editor.
	 *
	 * @param files The files to underline.  Note that they need not all
	 *        reside in the same directory.  To clear all underlined
	 *        files, pass <code>null</code> in for this value.
	 * @see #isOpenedFile(File)
	 */
	public void setOpenedFiles(File[] files) {
		openedFiles = files;
		refreshView();
	}


	/**
	 * Sets the style that "opened" files should have applied to them
	 * when they are displayed in this file chooser (for example,
	 * being underlined).
	 *
	 * @param style The style to apply.  If this is invalid,
	 *        <code>STYLE_UNDERLINE</code> is used.
	 * @see #getOpenFilesStyle()
	 * @see #setStyleOpenFiles(boolean)
	 */
	public void setOpenFilesStyle(int style) {
		if (style!=STYLE_BOLD && style!=STYLE_ITALIC &&
				style!=STYLE_UNDERLINE) {
			style = STYLE_UNDERLINE;
		}
		openFilesStyle = style;
	}


	/**
	 * Sets the file selected by the user.
	 *
	 * @param file The file selected by the user.
	 * @see #getSelectedFile
	 * @see #getSelectedFiles
	 * @see #setSelectedFiles
	 */
	public void setSelectedFile(File file) {
		// Use can pass in null to signify "no selected files."
		if (file==null) {
			selectedFiles = new File[0];
		}
		else {
			selectedFiles = new File[1];
			selectedFiles[0] = file;
		}
		setSelectedFiles(selectedFiles);
	}


	/**
	 * Sets the files selected by the user.
	 *
	 * @param files The files selected by the user.
	 * @see #getSelectedFile
	 * @see #getSelectedFiles
	 * @see #setSelectedFile
	 */
	public void setSelectedFiles(List<File> files) {
		int size = files.size();
		File[] fileArray = new File[size];
		setSelectedFiles(files.toArray(fileArray));
	}


	/**
	 * Sets the files selected by the user.
	 *
	 * @param files The files selected by the user.
	 * @see #getSelectedFile
	 * @see #getSelectedFiles
	 * @see #setSelectedFile
	 */
	public void setSelectedFiles(File[] files) {

		if (isChangingDirectories)
			return;

		// This array will be checked at showOpenDialog and showSaveDialog to
		// see whether files should be selected...
		filesToSelect = files.clone();
		selectedFiles = filesToSelect;

	}


	/**
	 * Sets whether hidden files and directories are shown by the file
	 * chooser.
	 *
	 * @param show Whether to show hidden files.
	 * @see #getShowHiddenFiles
	 */
	public void setShowHiddenFiles(boolean show) {
		if (show!=showHiddenFiles) {
			showHiddenFiles = show;
			refreshView();
		}
	}


	/**
	 * Sets whether "opened" files should have a special style applied
	 * to them when they are displayed in this file chooser (for example,
	 * being underlined).
	 *
	 * @param style Whether opened files should have a style applied.
	 * @see #getStyleOpenFiles()
	 * @see #setOpenFilesStyle(int)
	 */
	public void setStyleOpenFiles(boolean style) {
		if (style!=styleOpenFiles) {
			styleOpenFiles = style;
			refreshView();
		}
	}


	/**
	 * Sets the view mode.
	 *
	 * @param mode One of {@link #LIST_MODE}, {@link #DETAILS_MODE}, or
	 *        {@link #ICONS_MODE}.
	 * @see #getViewMode()
	 */
	public void setViewMode(int mode) {
		if (this.mode!=mode) {
			setViewModeImpl(mode);
		}
	}


	/**
	 * Does the dirty-work of setting the view mode.  This method does not
	 * check whether the mode to which you're setting the file chooser is
	 * already the "current mode."  It is used by internal methods to
	 * force a view mode change.
	 *
	 * @param mode The new view mode.
	 */
	protected void setViewModeImpl(int mode) {

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Remove any listeners the view has added to itself.
		if (view!=null)
			view.removeAllListeners();

		try {

			this.mode = mode;

			int horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
			int verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;

			switch (mode) {
				default: // Invalid mode specified
				case LIST_MODE:
					view = new ListView(this);
					verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER;
					break;
				case DETAILS_MODE:
					view = new DetailsView(this, nameString,
							sizeString, typeString, statusString,
							lastModifiedString);
					horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
					break;
				case ICONS_MODE:
					view = new IconsView(this);
					horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
			}

			// Add the view to a scroll pane.
			if (viewScrollPane==null) {
				viewScrollPane = new RScrollPane((JComponent)view);
				ComponentOrientation o = ComponentOrientation.
										getOrientation(getLocale());
				viewScrollPane.applyComponentOrientation(o);
			}
			else {
				viewScrollPane.setViewportView((JComponent)view);
			}
			viewScrollPane.setHorizontalScrollBarPolicy(horizontalScrollBarPolicy);
			viewScrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy);

			view.setMultiSelectionEnabled(multiSelectionEnabled); // Just pass already-used value.

			// Fill in the view with files, etc.
			if (dialog!=null && dialog.isVisible()) {
				refreshView();
			}

		// Ensure that, even if an Exception is thrown, we get our regular
		// cursor back.
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		if (fileNameTextField!=null) { // null the first time through
			fileNameTextField.requestFocusInWindow();
		}

	}


	/**
	 * Does all of the dirty-work for <code>showOpenDialog</code> and
	 * <code>showSaveDialog</code> since much of what they do is the same.
	 *
	 * @param parent The parent of this open/save dialog.
	 * @param dialogType Either {@link #OPEN_DIALOG} or {@link #SAVE_DIALOG}.
	 */
	protected int showDialogImpl(Window parent, int dialogType) {

		initializeGUIComponents(); // Creates GUI, if necessary.

		ensureCurrentDirectoryExists();

		// If the client code called "setFileFilter()" but the GUI wasn't
		// initialized yet, we remembered it via filterToSelect.  Now that
		// the GUI is initialized, select this filter.
		if (filterToSelect!=null) {
			setFileFilter(filterToSelect);
			filterToSelect = null;
		}

		// If there are files to have selected when the dialog is displayed,
		// set the current directory properly (if necessary), and select them.
		if (filesToSelect!=null) {

			int num = filesToSelect.length;
			if(num>0) {
				// Set the current directory properly to get the "selected"
				// files.
				File file = filesToSelect[0];
				if (file.isAbsolute() &&
						!fileSystemView.isParent(currentDirectory, file))
				{
					setCurrentDirectory(file.getParentFile());
				}
				view.setSelectedFiles(filesToSelect);
			}
			filesToSelect = null;
		}

		// If there are no files to select, just set the current directory.
		else {
			setCurrentDirectory(currentDirectory);
		}

		File oldCurrentDirectory = currentDirectory;

		// Create a dialog to wrap this file chooser component in.
		dialog = createDialog(parent);

		// Set up buttons/text, etc. to be appropriate to opening...
		// FIXME:  Update me to set ALL strings!
		if (dialogType==SAVE_DIALOG) {
			dialog.setTitle(customTitle!=null ? customTitle : saveDialogTitleText);
			acceptButton.setText(saveButtonText);
			acceptButton.setMnemonic(saveButtonMnemonic);
			acceptButton.setToolTipText(saveButtonToolTipText);
			lastType = dialogType;
		}
		else { // OPEN_DIALOG
			dialog.setTitle(customTitle!=null ? customTitle : openDialogTitleText);
			acceptButton.setText(openButtonText);
			acceptButton.setMnemonic(openButtonMnemonic);
			acceptButton.setToolTipText(openButtonToolTipText);
			lastType = OPEN_DIALOG;
		}

		// Display the dialog.
		dialog.pack();
		if (lastSize!=null)
			dialog.setSize(lastSize);// Keep dialog same size as last time.
		else
			adjustSizeIfNecessary(); // If there was no last time, make sure it isn't too big.
		dialog.setLocationRelativeTo(parent);
		lookInBreadcrumbBar.setMode(BreadcrumbBar.BREADCRUMB_MODE);
		fileNameTextField.requestFocusInWindow();

		try {
			dialog.setVisible(true);
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(this,
				"Exception occurred in RTextFileChooser:\n" + t +
				"\nPlease report this at http://sourceforge.net/projects/rtext,\n" +
				"\nalso noting the number of files that were in the directory you were\n" +
				"\nin/changing to.",
				errorDialogTitle, JOptionPane.ERROR_MESSAGE);
			retVal = ERROR_OPTION;
		}

		lastSize = dialog.getSize();
		dialog.dispose();
		dialog = null;

		// Only remember the most recent directory if the user clicked the
		// Approve button.
		if (retVal!=RTextFileChooser.APPROVE_OPTION)
			setCurrentDirectory(oldCurrentDirectory);

		// And return whether or not everything went okay.
		return retVal;

	}


	/**
	 * Displays an "Open" dialog.
	 *
	 * @param parent The window or dialog that owns this file chooser dialog.
	 * @return One of <code>APPROVE_OPTION</code>, <code>CANCEL_OPTION</code>,
	 *         or <code>ERROR_OPTION</code>.
	 */
	public int showOpenDialog(Window parent) {
		return showDialogImpl(parent, OPEN_DIALOG);
	}


	/**
	 * Displays a "Save As" dialog.
	 *
	 * @param parent The window or dialog that owns this file chooser dialog.
	 * @return One of <code>APPROVE_OPTION</code>, <code>CANCEL_OPTION</code>,
	 *         or <code>ERROR_OPTION</code>.
	 */
	public int showSaveDialog(Window parent) {
		return showDialogImpl(parent, SAVE_DIALOG);
	}


	/**
	 * Populates the "File: " combo box with the files selected in the
	 * view of the specified file chooser.
	 */
	void synchronizeTextFieldWithView() {

		// Don't do anything if we're in the process of changing directories.
		if (isChangingDirectories) 
			return;

		isChangingDirectories = true;

		File[] files = view.getSelectedFiles();
		int numSelected = files.length;

		// If only one item is selected, change the File field.
		fileNameTextField.setFileSystemAware(false); // So it doesn't try to steal focus.
		if (numSelected==1) {
			fileNameTextField.setText(files[0].getName());
		}

		// If multiple items are selected, add all of them to the File field.
		else if (numSelected>1) {

			String temp = "";
			for (int i=0; i<numSelected; i++) {
				temp += "\"" + files[i].getName() + "\" ";
			}

			// We'd like to clear the text from the "Look in" field
			// but if numSelected==0 and we try to call setText(),
			// we get an exception for mutating the document while
			// in an insertUpdate/removeUpdate call; so we get around
			// this by first checking that the text field doesn't have
			// focus (i.e., this method wasn't called in response to a
			// document change...).
			if (!fileNameTextField.hasFocus()) {
				fileNameTextField.setText(temp);
			}

		}

		fileNameTextField.setFileSystemAware(true);

		isChangingDirectories = false;

	}


	/**
	 * Updates the encoding displayed in the "Encoding" combo box to reflect
	 * the selected file.  If multiple files/directories or a directory is
	 * selected, the default encoding is selected, but if a single file is
	 * selected, then that file's encoding is selected.
	 */
	void updateSelectedEncoding() {

		File[] files = view.getSelectedFiles();
		if (files!=null) {
			if (files.length==1 && files[0].isFile()) {
				try {
					setEncoding(getEncodingOf(files[0]));
				} catch (IOException e) {
					// Don't print stack trace; file could have been
					// deleted out from under us, and no problem has
					// actually occurred.
					//e.printStackTrace();
					setEncoding(getDefaultEncoding());
					return; // What can you do?
				}
			}
			else {
				// Set the default encoding for 0 or >1 files selected.
				setEncoding(getDefaultEncoding());
			}
		}

	}


	/**
	 * Updates any contained components when a Look and Feel change occurs.
	 * Note that this is NOT overriding a superclass's <code>updateUI</code>
	 * method, as <code>JDialog</code> doesn't have one.  Rather, this method
	 * should be explicitly called whenever a LnF change occurs to ensure that
	 * the popup menu gets its UI updated.
	 */
	@Override
	public void updateUI() {

		if (!guiInitialized) {
			return;
		}

		super.updateUI();

		if (popupMenu!=null) {
			SwingUtilities.updateComponentTreeUI(popupMenu);
		}

		Border empty3Border = BorderFactory.createEmptyBorder(3,3,3,3);
		upOneLevelButton.setBorder(empty3Border);
		newFolderButton.setBorder(empty3Border);
		viewButton.setBorder(empty3Border);
		favoritesButton.setBorder(empty3Border);

		// Do some special stuff if we're in details mode.
		// NOTE:  This is a bad hack to get around the fact that
		// FileExplorerTableModel doesn't do too well with LnF changes... I
		// can't quite figure out how any JTable works correctly with a LnF
		// changes (the JTableHeader doesn't seem to know how to change its
		// renderer back to a Basic one after being in the Windows LnF, but
		// somehow it does).  Anyway, for the case of using a
		// FileExplorerTableModel, it doesn't work, so we simply create a new
		// JTable to get around it.
		// FIXME:  Find out why this doesn't work...
		if (mode==DETAILS_MODE) {
			view.removeAllListeners();
			mode = LIST_MODE;
			setViewMode(DETAILS_MODE);
		}

	}


	static {
		// Some actions only work with Java 6+.
		String ver = System.getProperty("java.specification.version");
		IS_JAVA_6_PLUS = !ver.startsWith("1.4") && !ver.startsWith("1.5");
	}


	/**
	 * Populates the "Favorites" popup menu when the "Favorites" menu
	 * button is clicked.
	 */
	private class FavoritesPopupListener implements PopupMenuListener {

		public void popupMenuCanceled(PopupMenuEvent e) {
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		}

		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

			// Remove items added when popup previously became visible.
			int count = favoritesButton.getItemCount();
			while (count>2) { // Remove all but last 2.
				Component old = favoritesButton.removeItem(0);
				if (old instanceof JMenuItem) {
					JMenuItem oldItem = (JMenuItem)old;
					oldItem.removeActionListener(RTextFileChooser.this);
				}
				count--;
			}

			// Add items, 1 per favorite.
			if (favoriteList!=null && !favoriteList.isEmpty()) {
				for (String dir : favoriteList) {
					JMenuItem item = new JMenuItem(dir);
					item.setActionCommand("SetDir");
					item.addActionListener(RTextFileChooser.this);
					favoritesButton.insertMenuItem(item,
								favoritesButton.getItemCount()-2);
				}
			}
			else {
				JMenuItem item = new JMenuItem(noFavoritesDefinedString);
				item.setEnabled(false);
				favoritesButton.insertMenuItem(item, 0);
			}

			// We unfortunately have to reset the component orientation
			// so all the newly-added JMenuItems are correct in RTL.
			ComponentOrientation o = ComponentOrientation.
									getOrientation(getLocale());
			favoritesButton.applyComponentOrientation(o);


		}

	}


// NOTE: Too slow to use.  Especially noticeable when files being rendered
// are on a network mounted drive.
//	/**
//	 * Renderer for the dropdown of the "File Name" FSATextField.
//	 */
//	private class FileNameFieldListCellRenderer
//				extends DefaultListCellRenderer {
//
//		public Component getListCellRendererComponent(JList list,
//			Object value, int index, boolean selected, boolean hasFocus) {
//			super.getListCellRendererComponent(list, value, index, selected,
//										hasFocus);
//			if (!selected) {
//				File file = new File((String)value);
//				Color fg = null;
//				FileTypeInfo fti = getFileTypeInfoFor(file);
//				if (getShowHiddenFiles() && file.isHidden()) {
//					fg = getHiddenFileColor();
//				}
//				else {
//					fg = fti.labelTextColor;
//				}
//				if (fg==null) { // Special care for this case.
//					fg = UIManager.getColor("List.foreground");
//				}
//				setForeground(fg);
//			}
//			return this;
//		}
//
//	}


	/**
	 * The item listener for all combo boxes on the file chooser.
	 */
	private class RTextFileChooserItemListener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {

			// Don't do anything if we're in the process of changing directories.
			if (isChangingDirectories)
				return;

			isChangingDirectories = true;

			Object source = e.getSource();

			// If they selected a new file filter...
			if (source==filterCombo && e.getStateChange()==ItemEvent.SELECTED) {
				currentFileFilter = (FileFilter)e.getItem();
				refreshView();
			}

			else if (source==encodingCombo && e.getStateChange()==ItemEvent.SELECTED) {
				setEncoding((String)e.getItem());
			}

			isChangingDirectories = false;

		}

	}


	/**
	 * Listens for typing in the file name combo box.  This class ensures that
	 * the combo box doesn't "eat" its first Enter press, as well as other
	 * stuff.
	 */
	private class TextFieldListener extends FocusAdapter
									implements DocumentListener {

		public void changedUpdate(DocumentEvent e) {
		}

		private void handleDocumentChange(DocumentEvent e) {
			if (!isChangingDirectories) {
				view.clearSelection();
			}
			int charCount = fileNameTextField.getDocument().getLength();
			acceptButton.setEnabled(charCount>0);
		}

		public void insertUpdate(DocumentEvent e) {
			handleDocumentChange(e);
		}

		public void removeUpdate(DocumentEvent e) {
			handleDocumentChange(e);
		}

		@Override
		public void focusGained(FocusEvent e) {
			fileNameTextField.selectAll();
		}

	}


}