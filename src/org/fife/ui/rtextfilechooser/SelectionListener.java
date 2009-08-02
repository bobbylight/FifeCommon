package org.fife.ui.rtextfilechooser;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Listens for the user selecting files in both the list mode and details mode.
 * This class can be used by any subclass of <code>RTextFileChooserView</code>
 * as its list selection listener, if it implements a list.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SelectionListener implements ListSelectionListener {

	private RTextFileChooser chooser;


	/**
	 * Constructor.
	 *
	 * @param chooser The file chooser.
	 */
	public SelectionListener(RTextFileChooser chooser) {
		this.chooser = chooser;
	}


	/**
	 * Called when the list's selection changes.  This method udpates the
	 * file name and encoding text fields to be in-synch with the view's
	 * selection.
	 *
	 * @param e The selection event.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			chooser.synchronizeTextFieldWithView();
			chooser.updateSelectedEncoding();
		}
	}


}