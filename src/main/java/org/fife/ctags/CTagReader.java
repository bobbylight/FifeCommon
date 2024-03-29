package org.fife.ctags;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * A class capable of reading a ctags file and searching for identifiers in it.
 * CTags are a useful little tool for things like programmers' text editors.
 * See http://ctags.sourceforge.net for more information.<p>
 *
 * This file is simply a translation of readtags.c from the Exuberant Ctags'
 * source distribution into Java.  This particular class is public domain.
 *
 * @author Robert Futrell
 * @version 0.01
 */
public class CTagReader {

	/* Options for tagsSetSortType() */
	public static final byte TAG_UNSORTED		= 1;
	public static final byte TAG_SORTED		= 2;
	public static final byte TAG_FOLDSORTED		= 3;

	/* Options for tagsFind() */
	public static final int TAG_FULLMATCH		= 0x00;
	public static final int TAG_PARTIALMATCH	= 0x01;

	public static final int TAG_OBSERVECASE		= 0x00;
	public static final int TAG_IGNORECASE		= 0x02;

	// Formerly tagResult values.
	public static final int TAGRESULT_FAILURE	= 0x00;
	public static final int TAGRESULT_SUCCESS	= 0x01;

	// Other #defines found in the code.
	private static final int JUMP_BACK			= 512;

	// Random constants.
	public static final String EmptyString = "";
	public static final String PseudoTagPrefix = "!_";


	private boolean initialized;		/* has the file been opened and this structure initialized? */
	private int format;				/* format of tag file */
	private int sortMethod;			/* how is the tag file sorted? */
	private RandomAccessFile fp;		/* pointer to file structure */
	private long pos;				/* file position of first character of 'line' */
	private long size;				/* size of tag file in seekable positions */
	private String line;			/* last line read */
	private String name;			/* name of tag in last line read */

	// Was "search" struct.
//	private long searchPos;			/* file position of last match for tag */
	private String searchName;		/* name of tag last searched for */
	private boolean searchPartial;	/* performing partial match */
	private boolean searchIgnoreCase;	/* ignoring case */

	// Was "program" struct.
	private String programAuthor;		/* name of program author */
	private String programName;		/* name of program */
	private String programUrl;		/* URL of distribution */
	private String programVersion;	/* program version */



	/**
	 * Creates a new CTagReader.  After this you should call
	 * <code>tagsOpen</code>.
	 */
	public CTagReader() {
	}


	/**
	 * Searches for an indentifier <code>name</code> in the current ctag file.
	 *
	 * @param entry The found identifier in the ctag file if success, ???
	 *        or <code>null</code> if it wasn't found ???
	 * @param name The identifier for which you are searching.
	 * @param options A bit flag; checks for <code>TAG_PARTIALMATCH</code>
	 *        and <code>TAG_IGNORECASE</code>.
	 * @return <code>true</code> iff the identifier was found.
	 */
	private boolean find(TagEntry entry, String name, int options) throws IOException {

		boolean result;
		searchName = name;
		searchPartial = (options & TAG_PARTIALMATCH) != 0;
		searchIgnoreCase = (options & TAG_IGNORECASE) != 0;
		//size = fp.length();
		fp.seek(0);	// Start over at the beginning for the searches below.
		if ((sortMethod == TAG_SORTED && !searchIgnoreCase) || (sortMethod == TAG_FOLDSORTED && searchIgnoreCase))
			result = findBinary();
		else
			result = findSequential();

		if (result && entry!=null) {
			entry.parseTagLine(line);
		}

		return result;

	}


	private boolean findBinary() throws IOException {

		boolean result = false;
		long lower_limit = 0;
		long upper_limit = size;
		long last_pos = 0;
		long pos = upper_limit / 2;

		while (!result) {

			if (!readTagLineSeek(pos)) {
				/* in case we fell off end of file */
				result = findFirstMatchBefore();
				break;
			}
			else if (pos == last_pos) {
				/* prevent infinite loop if we backed up to beginning of file */
				break;
			}
			else {

				int comp = nameComparison();
				last_pos = pos;
				if (comp < 0) {
					upper_limit = pos;
					pos = lower_limit + ((upper_limit - lower_limit) / 2);
				}
				else if (comp > 0) {
					lower_limit = pos;
					pos = lower_limit + ((upper_limit - lower_limit) / 2);
				}
				else if (pos == 0)
					result = true;
				else
					result = findFirstMatchBefore();
			}

		}

		return result;

	}


	private boolean findFirstMatchBefore() throws IOException {

		boolean result = false;
		boolean more_lines;
		long start = pos;
		findFirstNonMatchBefore();
		do {
			more_lines = readTagLine();
			if (nameComparison() == 0)
				result = true;
		} while (more_lines && !result && pos<start);

		return result;

	}


	private void findFirstNonMatchBefore() {

		boolean more_lines;
		int comp;
		long start = pos;
		long pos = start;

		do {
			if (pos < JUMP_BACK)
				pos = 0;
			else
				pos = pos - JUMP_BACK;
			more_lines = readTagLineSeek(pos);
			comp = nameComparison();
		} while (more_lines && comp==0 && pos>0 && pos<start);

	}


	private boolean findNext(TagEntry entry) throws IOException {

		boolean result;
		if ((sortMethod == TAG_SORTED && !searchIgnoreCase) ||
			(sortMethod == TAG_FOLDSORTED  &&  searchIgnoreCase))
		{
			result = tagsNext(entry);
			if (result && nameComparison() != 0)
				result = false;
		}
		else {
			result = findSequential();
			if (result &&  entry != null)
				entry.parseTagLine(line);
		}

		return result;

	}

	private boolean findSequential() throws IOException {

		boolean result = false;
		if (initialized) {
			while (!result &&  readTagLine()) {
				if (nameComparison() == 0)
					result = true;
			}
		}

		return result;

	}


	private int nameComparison() {

		int result;

		if (searchIgnoreCase) {
			if (searchPartial)
				//result = strnuppercmp (search.name, name,
				result = searchName.compareToIgnoreCase(name.substring(0,searchName.length()));
			else
				result = searchName.compareToIgnoreCase(name);
		}
		else {
			if (searchPartial)
				//result = strncmp (search.name, name, search.nameLength);
				result = searchName.compareTo(name.substring(0,searchName.length()));
			else
				result = searchName.compareTo(name);
		}

		return result;

	}


	/**
	 * Retrieves the value associated with the extension field for a given
	 * key.  It is passed a pointer to a structure already populated with
	 * values by a previous call to <code>tagsNext()</code>,
	 * <code>tagsFind()</code>, or <code>tagsFindNext()</code>, and a string
	 * containing the key of the desired extension field. If no such field
	 * of the specified key exists, the function will return null.
	 */
	private static String readFieldValue(TagEntry entry, String key) {

		if (key.equals("kind"))
			return entry.kind;
		else if (key.equals("file"))
			return EmptyString;

		String result = null;
		int size = entry.fieldList.size();
		for (int i=0; i<size && result==null; i++) {
			if (key.equals(entry.fieldList.get(i).key)) {
				result = entry.fieldList.get(i).value;
			}
		}
		return result;

	}


	private boolean readNext(TagEntry entry) throws IOException {

		boolean result;
		if (!initialized)
			result = false;
		else if (!readTagLine())
			result = false;
		else {
			if (entry!=null)
				entry.parseTagLine(line);
			result = true;
		}

		return result;

	}


	/**
	 * Reads all the "pseudo-tags" from the top of the ctag file (author, version,
	 * etc.).
	 * <p>
	 * param file The tag file from which to get the pseudotag information.
	 * @param info The structure in which to place the information.
	 */
	private void readPseudoTags(TagFileInfo info) throws IOException {

		long startOfLine;
		final int prefixLength = PseudoTagPrefix.length();

		if (info==null)
			return;

		// Initialize the info structure.
		info.format	= 1;
		info.sort		= TAG_UNSORTED;
		info.author	= null;
		info.name		= null;
		info.url		= null;
		info.version	= null;

		// Keep reading in lines until a non-pesudotag line is read.
		while (true) {

			startOfLine = fp.getFilePointer();

			if (!readTagLine())
				break;
			if (!line.startsWith(PseudoTagPrefix))
				break;

			TagEntry entry = new TagEntry();
			String key, value;
			entry.parseTagLine(line);	// Fills in entry.
			key = entry.name.substring(prefixLength);
			value = entry.file;
			switch (key) {
				case "TAG_FILE_SORTED" -> sortMethod = Integer.parseInt(value);
				case "TAG_FILE_FORMAT" -> format = Integer.parseInt(value);
				case "TAG_PROGRAM_AUTHOR" -> programAuthor = value;
				case "TAG_PROGRAM_NAME" -> programName = value;
				case "TAG_PROGRAM_URL" -> programUrl = value;
				case "TAG_PROGRAM_VERSION" -> programVersion = value;
			}

			info.format = format;
			info.sort = sortMethod;
			info.author = programAuthor;
			info.name = programName;
			info.url = programUrl;
			info.version = programVersion;

		} // End of while (true).

		// Returns file pointer to where we were initially.
		fp.seek(startOfLine);

	}


	/**
	 * Reads the next line from the ctag file.  This populates the
	 * <code>line</code> field so that it can later be passed to a
	 * <code>TagEntry</code> instance using <code>parseTagLine</code>.
	 *
	 * @return <code>true</code> if the next line is successfully read, or
	 *         <code>false</code> if EOF is encountered.
	 * @throws IOException If an I/O exception occurs.
	 */
	private boolean readTagLine() throws IOException {

		line = fp.readLine();

		// Copy the name from the line into the main name field.
		if (line!=null) {
			// There should be a tab char after the name.  But get whatever
			// whitespace comes after the name so we know its length.
			int index = line.indexOf('\t');
			if (index==-1) {
				index = line.indexOf('\n');
				if (index==-1)
					index = line.indexOf('\r');
			}
			if (index!=-1)
				name = line.substring(0, index);
			else
				name = line; // If we didn't find any whitespace, assume name is the entire line.
		}

		return line!=null;

	}


	private boolean readTagLineSeek(long pos) {

		boolean result = false;

		try {

			fp.seek(fp.getFilePointer()+pos); // May throw IOException if fails.

			result = readTagLine();		/* Read probable partial line. */
			if (pos>0 && result)
				result = readTagLine();	/* Read complete line. */

		} catch (Exception ignored) {}

		return result;

	}


/* *********************** EXTERNAL INTERFACE *********************************/
	/**
	 * Sets the ctag file to be read by this reader.
	 *
	 * @param filePath The ctag file from which to read.
	 * @param info Will be populated with information about the ctag file.
	 * @throws FileNotFoundException If the specified file does not exist or
	 *                               cannot be opened for some reason.
	 * @throws IOException If an I/O error occurs.
	 */
	public void tagsOpen(String filePath, TagFileInfo info) throws FileNotFoundException,
														IOException {
		fp = new RandomAccessFile(filePath, "r");
		size = fp.length();
		readPseudoTags(info);	// Read in author, version, etc. tags.
		initialized = true;		// And initialized.
	}


	/**
	 * Returns the first ctag from the ctag file.
	 *
	 * @param entry Will contain the tag read, ??? or <code>null</code> if
	 *        the read fails ???
	 * @return Whether the read was successful.
	 * @throws IOException If an IO error occurs.
	 */
	public boolean tagsFirst(TagEntry entry) throws IOException {
		boolean result = false;
		if (initialized) {

			// Move to the beginning of the first line containing a
			// non-pseudo ctag.
			long startOfLine;
			fp.seek(0);		// Start at the beginning.
			// Go until you find the first non-pseudo tag line.
			while (true) {
				startOfLine = fp.getFilePointer();
				if (!readTagLine())
					break;
				if (!line.startsWith(PseudoTagPrefix))
					break;
			}
			// Look at the beginning of the line (readTagLine moved it).
			fp.seek(startOfLine);

			result = readNext(entry);

		}
		return result;
	}


	/**
	 * Returns the next ctag from the given ctag file.
	 *
	 * @param entry Will be the next ctag in the file, ??? or <code>null</code>
	 *        if the read fails???
	 * @return <code>true</code> if a tag was read, or <code>false</code> if
	 *         EOF was reached.
	 * @throws IOException If an IO error occurs.
	 */
	public boolean tagsNext(TagEntry entry) throws IOException {
		boolean result = false;
		if (initialized)
			result = readNext(entry);
		return result;
	}


	/**
	 *  Retrieve the value associated with the extension field for a specified key.
	 *  It is passed a pointer to a structure already populated with values by a
	 *  previous call to tagsNext(), tagsFind(), or tagsFindNext(), and a string
	 *  containing the key of the desired extension field. If no such field of the
	 *  specified key exists, the function will return null.
	 *
	 * @param entry The tag entry gotten from a call such as <code>tagsNext</code>.
	 * @param key The key to search for.
	 * @return The value associated with <code>key</code>, or <code>null</code>
	 *         if none exists.
	 */
	public String tagsField(final TagEntry entry, final String key) {
		String result = null;
		if (entry!=null)
			result = readFieldValue(entry, key);
		return result;
	}


	/**
	 *  Find the first tag matching `name'. The structure pointed to by `entry'
	 *  will be populated with information about the tag file entry. If a tag file
	 *  is sorted using the C locale, a binary search algorithm is used to search
	 *  the tag file, resulting in very fast tag lookups, even in huge tag files.
	 *  Various options controlling the matches can be combined by bit-wise or-ing
	 *  certain values together. The available values are:
	 *
	 * <ul>
	 *   <li>TAG_PARTIALMATCH - Tags whose leading characters match `name'
	 *       will qualify.</li>
	 *
	 *   <li>TAG_FULLMATCH - Only tags whose full lengths match `name' will
	 *       qualify.</li>
	 *
	 *   <li>TAG_IGNORECASE - Matching will be performed in a case-insensitive
	 *       manner. Note that this disables binary searches of the tag file.</li>
	 *
	 *   <li>TAG_OBSERVECASE - Matching will be performed in a case-sensitive
	 *       manner. Note that this enables binary searches of the tag file.</li>
	 * </ul>
	 *
	 * @param entry Will be the ctag entry found, ??? or <code>null</code> if it
	 *        isn't found or an error occurs???
	 * @param name ???
	 * @param options ???
	 * @return <code>true</code> if the tag was found, or <code>false</code>
	 *         if it wasn't or <code>file</code> wasn't initialized.
	 * @throws IOException If an IO error occurs.
	 */
	public boolean tagsFind(TagEntry entry, String name, int options) throws IOException {
		boolean result = false;
		if (initialized)
			result = find(entry, name, options);
		return result;
	}


	/**
	 * Find the next tag matching the name and options supplied to the most
	 * recent call to <code>tagsFind()</code> for the same tag file. The
	 * structure pointed to by 'entry' will be populated with information
	 * about the tag file entry.
	 *
	 * @param entry The tag entry.
	 * @return whether another tag matching the name is found.
	 * @throws IOException If an IO error occurs.
	 */
	public boolean tagsFindNext(TagEntry entry) throws IOException {
		boolean result = false;
		if (initialized)
			result = findNext(entry);
		return result;
	}


	/**
	 * "Closes" the current ctag file.  This reader can still be reused
	 * by calling the <code>tagsOpen</code> method again.
	 *
	 * @return <code>true</code> if the file was closed, <code>false</code>
	 *         if this reader wasn't even initialized so there was no reason
	 *         to call close.
	 * @throws IOException If an IO error occurs.
	 */
	public boolean tagsClose() throws IOException {
		if (initialized) {
			initialized = false;
			fp.close();
			pos = size = 0;
			line = null;
			name = null;
			programAuthor = null;
			programName = null;
			programUrl = null;
			programVersion = null;
			return true;
		}
		return false;
	}


}
