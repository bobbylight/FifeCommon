package org.fife.ctags;


/**
 * This class contains information about a tag found in a ctag file.  This
 * particular class is public domain.
 *
 * @author Robert Futrell
 * @version 0.01
 */
public class TagFileInfo {

	public int format; /* format of tag file (1 = original, 2 = extended) */
	public int sort; /* how is the tag file sorted? */

	/* information about the program which created this tag file */
	public String author;
	public String name;
	public String url;
	public String version;


	/**
	 * Creates a new <code>TagFileInfo</code>.
	 */
	public TagFileInfo() {
	}


}
