package org.fife.ctags;


/**
 * This class contains information about an extension field for a tag.
 * These exist at the end of the tag in the form "key:value".  This particular
 * class is public domain.
 *
 * @author Robert Futrell
 * @version 0.01
 */
public class TagExtensionField {

	public String key;		/* the key of the extension field */
	public String value;	/* the value of the extension field (may be an empty string) */


	/**
	 * Creates a new <code>TagExtensionField</code>.
	 */
	public TagExtensionField(String key, String value) {
		this.key = key;
		this.value = value;
	}


}