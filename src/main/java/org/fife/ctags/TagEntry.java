package org.fife.ctags;

import java.util.ArrayList;
import java.util.List;


/**
 * Simple class that contains information about a specific tag.  This class
 * is public domain.
 *
 * @author Robert Futrell
 * @version 0.1
 */
public class TagEntry {

	/**
	 * The name of this tag entry.
	 */
	public String name;

	/**
	 * Path of source file containing the definition of this tag.
	 */
	public String file;

	/**
	 * Pattern for locating source line (may be null if not present).
	 *
	 * @see #getPlainTextPattern()
	 */
	public String pattern;

	/**
	 * Line number in source file of tag definition (may be zero if not known).
	 */
	public long lineNumber;

	/**
	 * Kind of tag (may be name, character, or null if not known).
	 */
	public String kind;

	/**
	 * Whether this tag is of file-limited scope.
	 */
	public boolean fileScope;

	/**
	 * List of key-value pairs.
	 */
	public List<TagExtensionField> fieldList;


	/**
	 * Creates a new <code>TagEntry</code>.
	 */
	public TagEntry() {
		// Initialize a small list, because we have fields dedicated to a
		// pattern and line number, which are the common extension fields.
		this.fieldList = new ArrayList<TagExtensionField>(1);
	}


	/**
	 * Creates a new <code>TagEntry</code> based on the passed-in ctag
	 * file line.
	 *
	 * @param line A line from a ctag file on which to create this tag
	 *        entry.
	 */
	public TagEntry(String line) {
		this.fieldList = new ArrayList<TagExtensionField>(1);
		parseTagLine(line);
	}


	/**
	 * Fixes the escaped characters found in the tag pattern.  See
	 * http://ctags.sourceforge.net/FORMAT for more information.
	 *
	 * @param pattern The pattern read from the ctags file.
	 * @return The pattern, with escaped characters un-escaped.
	 */
	private static final String fixEscapes(String pattern) {

		// TODO: Replace with StringBuilder and use better append methods
		// in 1.5.
		StringBuilder sb = new StringBuilder();

		int old = 0;
		int pos = 0;
		int len = pattern.length();
		while (old<len && (pos=pattern.indexOf('\\', old))>-1) {
			sb.append(pattern.substring(old, pos)); // Replace in 1.5!
			if (pos<len-1) { // Should always be true
				char ch = pattern.charAt(++pos);
				switch (ch) {
					case '\\':
						sb.append('\\');
						break;
					case 'r':
						sb.append('\r');
						break;
					case 'n':
						sb.append('\n');
						break;
					case 't':
						sb.append('\t');
						break;
					default: // Should never happen
						sb.append(ch);
						break;
				}
			}
			else {
				sb.append('\\');
			}
			old = ++pos;
		}
		if (old<len) {
			sb.append(pattern.substring(old)); // Replace in 1.5!
		}

		return sb.toString();

	}


	/**
	 * Returns a "plain text" version of {@link #pattern} - that is, with
	 * escaped characters un-escaped, and without a leading "/^" or trailing
	 * "$/".
	 *
	 * @return <code>pattern</code> as plain text, or <code>null</code> if
	 *         there is no pattern (in which case, use {@link #lineNumber}).
	 */
	public String getPlainTextPattern() {
		return pattern==null ? null :
				fixEscapes(pattern.substring(2,pattern.length()-2));
	}


	/**
	 * Parses the part of a ctag line that was added as an "extension."
	 *
	 * @param string The extension fields part of a ctag line (i.e.,
	 *        everything after ';"').
	 */
	private void parseExtensionFields(String string) {

		if (string==null)
			return;

		string = string.trim();	// Cut off leading/trailing whitespace.

		String[] keyValuePairs = string.split("\t");
		int numKeyValuePairs = keyValuePairs.length;

		for (int i=0; i<numKeyValuePairs; i++) {

			int colonIndex = keyValuePairs[i].indexOf(':');

			// If there was no ':', then this MUST be a 'kind' field (which
			// is a single char always).
			if (colonIndex==-1) {
				kind = keyValuePairs[i];
			}

			// MUST be of the form "<key>:<value>".
			else {
				String key = keyValuePairs[i].substring(0,colonIndex);
				String value = keyValuePairs[i].substring(colonIndex+1);
				if (key.equals("kind"))
					kind = value;
				else if (key.equals("file"))
					fileScope = true;
				else if (key.equals("line"))
					lineNumber = Integer.parseInt(value);
				else {
					fieldList.add(new TagExtensionField(key, value));
				}
			}

		} // End of for (int i=0; i<numKeyValuePairs; i++).

	}


	/**
	 * Parses a line from a ctag file and populates this tag entry with
	 * appropriate values.
	 *
	 * @param line A line from a ctag file.
	 */
	public void parseTagLine(String line) {

		kind = null;
		fileScope = false;

		// Get the index of the first tab.  Everything before it is the
		// entry's name.
		int tabIndex = line.indexOf('\t');
		if (tabIndex==-1) {
			name = line; // Shouldn't really happen? ...
		}
		else { // tabIndex!=null.
			name = line.substring(0,tabIndex);
			// Get the next tab after this one.  Everything between these
			// tabs is the entry's file name.
			int tabIndex2 = line.indexOf('\t', tabIndex+1);
			if (tabIndex2==-1) {
				file = line.substring(tabIndex+1); // Shouldn't happen...
			}
			else {
				file = line.substring(tabIndex+1, tabIndex2);

				// Next comes either the pattern or the line number
				// (usually a pattern, but C #defines have line numbers).
				String temp = line.substring(tabIndex2+1);
				char tempChar = temp.charAt(0);
				if (tempChar=='/' || tempChar=='?') {
					/* parse pattern. */
					lineNumber = 0;
					//entry.pattern = temp;
					int curPos = 0, foo=0;
					do {
						foo = temp.indexOf(tempChar, curPos+1);
						if (foo>-1)
							curPos = foo;
					} while (foo!=-1 && temp.charAt(foo-1)=='\\');
					if (foo==-1) {
						/* Invalid pattern. */
					}
					else  {
						pattern = temp.substring(0, foo+1);
						temp = temp.substring(foo+1);
					}
				}

				else if (Character.isDigit(tempChar)) {
					/* Parse line number. */
					pattern = null;
					int i = 0;
					while (Character.isDigit(tempChar))
						tempChar = temp.charAt(++i);
					lineNumber = Integer.parseInt(temp.substring(0,i));
					temp = temp.substring(i);
				}

				else {
					/* Invalid pattern. */
				}

				boolean fieldsPresent = temp.startsWith(";\"");
				if (fieldsPresent)
					parseExtensionFields(temp.substring(2));

			} // End of else.

		} // End of else.

		fieldList.clear();
		fieldList.addAll(fieldList);

	}


	/**
	 * Writes this tag entry as a string.
	 *
	 * @return A string representation of this <code>TagEntry</code>.
	 */
	@Override
	public String toString() {
		return name;
	}


}