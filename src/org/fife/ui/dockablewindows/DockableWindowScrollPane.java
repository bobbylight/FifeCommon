package org.fife.ui.dockablewindows;

import java.awt.Component;
import javax.swing.border.Border;

import org.fife.ui.RScrollPane;


/**
 * A scroll pane to go into a dockable window.  This scroll pane never draws
 * any borders, to avoid the ugly compound/bevel border effect of nesting
 * components.<p>
 *
 * Components going into a {@link DockableWindow} should put themselves inside
 * instances of this class.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class DockableWindowScrollPane extends RScrollPane {


	/**
	 * Constructor.
	 */
	public DockableWindowScrollPane() {
	}


	/**
	 * Constructor.
	 *
	 * @param view The view for this scroll pane.
	 */
	public DockableWindowScrollPane(Component view) {
		super(view);
	}


	/**
	 * Overridden so the border is never set.
	 *
	 * @param b This parameter is ignored.
	 */
	public void setBorder(Border b) {
		super.setBorder(null);
	}


}