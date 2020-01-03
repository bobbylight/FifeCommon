/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui.rtextfilechooser;

import java.io.File;
import java.util.Comparator;

/**
 * Compares files by  name, case-insensitively.  This unfortunately has to be
 * used on OS X, where Java sorts file names case-sensitively, which is
 * incorrect for that platform.<p>
 *
 * It is assumed that the two file objects being compared are either both
 * files or both directories, so that comparison doesn't have to be made.
 */
class CaseInsensitiveFileComparator implements Comparator<File> {

    @Override
    public int compare(File f1, File f2) {
        return f1.getName().compareToIgnoreCase(f2.getName());
    }
}
