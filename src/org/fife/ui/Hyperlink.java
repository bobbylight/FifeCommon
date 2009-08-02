package org.fife.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;

import org.fife.ui.UIUtil;


/**
 * A clickable hyperlink.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Hyperlink extends JLabel {

	private static final long serialVersionUID = 1L;

	private String text;
	private String address;


	/**
	 * Constructor.
	 *
	 * @param address The address the link points to.
	 */
	public Hyperlink(String address) {
		this(address, address);
	}


	/**
	 * Constructor.
	 *
	 * @param text The text to display as the hyperlink.
	 * @param address The address the link points to.
	 */
	public Hyperlink(String text, String address) {
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		setAddress(address);
		setText(text);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setFocusable(true);
	}


	/**
	 * Returns the web address this hyperlink points to.
	 *
	 * @return The web address.
	 * @see #setAddress(String)
	 */
	public String getAddress() {
		return address;
	}


	/**
	 * Returns the text displayed by this hyperlink.
	 *
	 * @return The text displayed.
	 * @see #setText(String)
	 */
	public String getDisplayedText() {
		return this.text;
	}

/*
protected void paintBorder(java.awt.Graphics g) {
	if (isFocusOwner()) {
		Rectangle bounds = getBounds();
		g.setColor(Color.RED);
		javax.swing.plaf.basic.BasicGraphicsUtils.drawDashedRect(g, 0,0, bounds.width,bounds.height);
	}
}
*/
	protected void processMouseEvent(MouseEvent e) {
		if (e.getButton()==MouseEvent.BUTTON1) {
			switch (e.getID()) {
				case MouseEvent.MOUSE_CLICKED:
					if (!UIUtil.browse(getAddress())) {
						UIManager.getLookAndFeel().
									provideErrorFeedback(this);
					}
					break;
/*				case MouseEvent.MOUSE_PRESSED:
					requestFocusInWindow();
					break;
*/			}
		}
		super.processMouseEvent(e);
	}


	/**
	 * Sets the address this hyperlink points to.
	 *
	 * @param address The new web address.
	 * @see #getAddress()
	 */
	public void setAddress(String address) {
		this.address = address;
	}


	/**
	 * Sets the text for this hyperlink.
	 *
	 * @param text The new text.
	 * @see #getText()
	 */
	public void setText(String text) {
		this.text = text;
		text = "<html><body><a href=\"" + address + "\">" + text +
				"</a></body></html>";
		super.setText(text);
		setToolTipText(address);
	}


}