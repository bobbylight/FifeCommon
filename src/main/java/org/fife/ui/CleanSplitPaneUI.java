/*
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.Graphics;
import java.io.Serial;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;


/**
 * UI for a <code>JSplitPane</code> that draws no nasty bevels around
 * components or the divider.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class CleanSplitPaneUI extends BasicSplitPaneUI {


	@Override
	public BasicSplitPaneDivider createDefaultDivider() {
		return new EmptySplitPaneDivider(this);
	}


	@Override
	protected void installDefaults() {
		super.installDefaults();
		splitPane.setBorder(null);
		divider.setDividerSize(4);
	}


	/**
	 * A divider that renders nothing but its background color.
	 */
	static class EmptySplitPaneDivider extends BasicSplitPaneDivider {

		@Serial
		private static final long serialVersionUID = 1L;

		EmptySplitPaneDivider(BasicSplitPaneUI ui) {
			super(ui);
		}

		@Override
		public void paint(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}

	}


}
