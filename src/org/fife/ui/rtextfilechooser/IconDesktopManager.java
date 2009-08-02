/*
 * 07/17/2004
 *
 * IconDesktopManager.java - Desktop pane manager that allows the user to
 *    select multiple internal frames simultaneously (when used with a
 *    JDesktopPane that also allows this, such as IconDesktopPane).
 * Copyright (C) 2004 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,1
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.fife.ui.rtextfilechooser;

import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import javax.swing.*;


/**
 * This is an implementation of the <code>DesktopManager</code> designed to
 * allow the user to select multiple internal frames at once.  This manager
 * should be used with a <code>JDesktopPane</code> subclass that also knows that
 * the user can select multiple internal frames,
 * like <code>IconDesktopPane</code>.
 *
 * @author Robert Futrell
 * @version 0.1
 */
class IconDesktopManager extends DefaultDesktopManager implements Serializable {

	private static final long serialVersionUID = 1L;


	/**
	 * Removes the frame, and, if necessary, the
	 * <code>desktopIcon</code>, from its parent.  This method is overridden so
	 * that the "next internal frame" isn't selected after this one is closed.
	 * @param f the <code>JInternalFrame</code> to be removed
	 */
	public void closeFrame(JInternalFrame f) {
		Container c = f.getParent();
		if (f.isSelected())
			try { f.setSelected(false); } catch (PropertyVetoException e2) { }
		if(c != null) {
			c.remove(f);
			c.repaint(f.getX(), f.getY(), f.getWidth(), f.getHeight());
		}
		removeIconFor(f);
		if(f.getNormalBounds() != null)
			f.setNormalBounds(null);
		if(wasIcon(f))
			setWasIcon(f, null);
	}


	/**
	 * This will activate <b>f</b> moving it to the front.
      * @param f the <code>JInternalFrame</code> to be activated
      */
	public void activateFrame(JInternalFrame f) {
		Container p = f.getParent();
		IconDesktopPane d = (IconDesktopPane)f.getDesktopPane();
		// fix for bug: 4162443
		if(p == null)
			return;
		if (d != null)
			d.addSelectedFrame(f);
		f.moveToFront();
	}

    
	// implements javax.swing.DesktopManager
	public void deactivateFrame(JInternalFrame f) {
		IconDesktopPane d = (IconDesktopPane)f.getDesktopPane();
		d.removeSelectedFrame(f);
	}


}