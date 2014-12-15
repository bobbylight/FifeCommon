/*
 * 11/14/2003
 *
 * PrintPreviewDialog.java - A dialog showing the user what their Printable
 * print job will look like.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.print;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import org.fife.ui.EscapableDialog;
import org.fife.ui.UIUtil;
import org.fife.ui.RScrollPane;
import org.fife.ui.StatusBar;


/**
 * A dialog showing the user what their print job will look like.
 *
 * @author Robert Futrell
 * @version 0.7
 */
public class PrintPreviewDialog extends EscapableDialog
						implements ActionListener, ItemListener {

	private static final long serialVersionUID = 1L;

	private JPanel previewPanel;		// The panel that actually shows the preview.
	private PagePreview[] pageImage;	// Image of the page.
	private Printable masterImage;		// The "master" (full-sized) image.

	private JButton printButton;
	private JButton nextPageButton;		// Button to view the next page.
	private JButton prevPageButton;		// Button to view the previous page.
	private JButton closeButton;

	private JComboBox sizeComboBox;		// To choose the preview's size.
	private JComboBox numPagesComboBox;	// To choose the number of pages to view at a time.

	private int scale;					// Current scale of pages, in percent (i.e., 10 ==> 10%).
	private int pageWidth;
	private int pageHeight;
	private int currentPage;				// The first currently visible page.
	private int numVisiblePages;			// Number of visible pages.
	private int numPages;				// Number of pages in the document.

	private Cursor zoomInCursor;
	private Cursor zoomOutCursor;

	private Border pagePreviewBorder;	// Shared to reduce footprint.

	private PageFormat pageFormat;		// Used when generating a page.

	private static final int MAX_PAGE_COUNT		= 4;


	/**
	 * Creates a new <code>PrintPreviewDialog</code>.
	 *
	 * @param owner The application from which you are going to print.
	 * @param printable The component you are going to print.
	 */
	public PrintPreviewDialog(Frame owner, Printable printable) {

		super(owner);

		ComponentOrientation orientation = ComponentOrientation.
									getOrientation(getLocale());

		ResourceBundle msg = ResourceBundle.getBundle(
								"org.fife.print.PrintPreviewDialog");

		// Get the page format to use when generating a preview image.
		PrinterJob prnJob = PrinterJob.getPrinterJob();
		pageFormat = prnJob.defaultPage();

		createCursors();

		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new GridLayout(1,6, 5,10));
		Box topPanel = new Box(BoxLayout.LINE_AXIS);
		topPanel.add(toolBarPanel);
		topPanel.add(Box.createHorizontalGlue());
		topPanel.setBorder(BorderFactory.createEmptyBorder(1,1,2,1));

		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		contentPane.add(topPanel, BorderLayout.NORTH);
		contentPane.add(new StatusBar(""), BorderLayout.SOUTH);
		printButton = new JButton(msg.getString("Print"));
		printButton.setActionCommand("Print");
		printButton.addActionListener(this);
		toolBarPanel.add(printButton);
		prevPageButton = new JButton(msg.getString("PrevPage"));
		prevPageButton.setEnabled(false);
		prevPageButton.setActionCommand("PrevPage");
		prevPageButton.addActionListener(this);
		toolBarPanel.add(prevPageButton);
		nextPageButton = new JButton(msg.getString("NextPage"));
		nextPageButton.setActionCommand("NextPage");
		nextPageButton.addActionListener(this);
		toolBarPanel.add(nextPageButton);

		closeButton = new JButton(msg.getString("Close"));
		closeButton.setActionCommand("Close");
		closeButton.addActionListener(this);
		toolBarPanel.add(closeButton);
		sizeComboBox = new JComboBox(
			new String[] { "10%", "25%", "33%", "50%", "66%",
						"75%", "100%", "150%", "200%" }
		);
		UIUtil.fixComboOrientation(sizeComboBox);
		sizeComboBox.addItemListener(this);
		toolBarPanel.add(sizeComboBox);
		numPagesComboBox = new JComboBox(
			new String[] { "1", "2", "3", "4" } );
		UIUtil.fixComboOrientation(numPagesComboBox);
		numPagesComboBox.addItemListener(this);
		toolBarPanel.add(numPagesComboBox);
		toolBarPanel.add(Box.createHorizontalGlue());
		previewPanel = new JPanel();
		previewPanel.setBackground(Color.GRAY);
		RScrollPane scrollPane = new RScrollPane(previewPanel);
		contentPane.add(scrollPane);

		this.masterImage = printable;
		scale = 25;
		sizeComboBox.setSelectedItem("25%");
		pageImage = new PagePreview[MAX_PAGE_COUNT];
		setNumVisiblePages(1);				// Only one page is visible initially.
		setPreviewPage(0);					// Initially preview the first page.
		setModal(true);					// So the user can't switch back to RText.

		// Calculate the number of pages in the document.
		// NOTE:  There MUST be a more efficient way to do this...
		JDialog dummyDialog = new JDialog();
		dummyDialog.pack();
		Graphics h = dummyDialog.getGraphics();
		numPages = 0;
		try {
			while (printable.print(h, pageFormat, numPages) == Printable.PAGE_EXISTS)
				numPages++;
		} catch (PrinterException pe) { pe.printStackTrace(); }
		h.dispose();

		// Get ready to go!
		updateButtons();
		setTitle(msg.getString("Title"));
		applyComponentOrientation(orientation);
		pack();

	}


	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if (actionCommand.equals("Close")) {
			setVisible(false);
		}

		else if (actionCommand.equals("NextPage")) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				incrementPreviewPage();
			} finally { // To ensure GUI stays correct.
				updateButtons();
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		else if (actionCommand.equals("PrevPage")) {
			setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				decrementPreviewPage();
			} finally { // To ensure GUI stays correct.
				updateButtons();
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		// If the user elects to print the document, close the Print Preview dialog.
		// (The printing is done by the other listener).
		else if (actionCommand.equals("Print")) {

			// Try to print!
			DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
			PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
			attributeSet.add(new Copies(1));
			attributeSet.add(Sides.ONE_SIDED);
			attributeSet.add(OrientationRequested.PORTRAIT);
			PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);//attributeSet);
			PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
			//DocFlavor[] flavors = defaultService.getSupportedDocFlavors();
			PrintService chosenService = ServiceUI.printDialog(null, 200, 200, services, defaultService,
										flavor, attributeSet);
			if (chosenService != null) {
				DocPrintJob job = chosenService.createPrintJob();
				Doc myDoc = new SimpleDoc(masterImage, flavor, null);//attributeSet);
				try {
					job.print(myDoc, attributeSet);
				} catch (PrintException pe) {
					JOptionPane.showMessageDialog(this,
							"Error attempting to print: "+pe+".",
							"Printing Error", JOptionPane.ERROR_MESSAGE);
				}
			}

			// After we've printed (or not), hide the Print Preview dialog.
			setVisible(false);

		}

	}


	/**
	 * Creates the custom zoom in/zoom out cursors.
	 */
	private void createCursors() {

		// Get the zoom in/zoom out cursor images.
		BufferedImage zoomInCursor1 = null;
		BufferedImage zoomOutCursor1 = null;
		ClassLoader cl = this.getClass().getClassLoader();
		try {
			zoomInCursor1 = ImageIO.read(cl.getResource("org/fife/print/zoomin.gif"));
			zoomOutCursor1 = ImageIO.read(cl.getResource("org/fife/print/zoomout.gif"));
		} catch (Exception e) {	// IOException or MalformedURLException.
			e.printStackTrace();
			Cursor defaultCursor = Cursor.getPredefinedCursor(
							Cursor.DEFAULT_CURSOR);
			zoomInCursor = defaultCursor;
			zoomOutCursor = defaultCursor;
			return;
		}

		Point hotspot = new Point(0,0);
		zoomInCursor = getCustomCursor(zoomInCursor1, hotspot,
								"ZoomInCursor", 32,32);
		zoomOutCursor = getCustomCursor(zoomOutCursor1, hotspot,
								"ZoomOutCursor", 32,32);

	}


	/**
	 * Decrements the first preview page, if possible, and updates the
	 * screen.  This method is designed as an alternative to
	 * <code>setPreviewPage()</code> that performs better with the more
	 * preview pages visible, as only one new page ever has to be created.
	 */
	public void decrementPreviewPage() {

		if (pageImage==null || currentPage==0)
			return;
		currentPage--;

		// Move 'backward' one page in all visible pages.  This should
		// should work even if some of the pages are null (i.e., no
		// more pages to view), as we'd just be setting stuff to null.
		// We'll reuse the last page preview to keep from having to
		// create a new image, etc.
		PagePreview pp = null;
		pp = pageImage[numVisiblePages-1];
		previewPanel.remove(pp);
		for (int i=numVisiblePages-1; i>0; i--)
			pageImage[i] = pageImage[i-1];
		pageImage[0] = null; // May help GC.

		pageWidth = (int)(pageFormat.getWidth());
		pageHeight = (int)(pageFormat.getHeight());
		try {
			BufferedImage tempImage = pp.getSourceImage();
			Graphics g = tempImage.getGraphics();
			g.setColor(Color.white);
			g.fillRect(0,0, pageWidth,pageHeight);
			if (masterImage.print(g, pageFormat, currentPage) == Printable.PAGE_EXISTS) {
				int w = (int)(pageWidth*scale/100.0);
				int h = (int)(pageHeight*scale/100.0);
				pp.setScaledSize(w,h); // Recreate scaled version.
				pageImage[0] = pp;
				previewPanel.add(pp, 0);
			}
			else {
				pageImage[0] = null;
			}
			g.dispose();
		} catch (PrinterException pe) {
			pe.printStackTrace();
		}

		previewPanel.revalidate();
		previewPanel.repaint(); // Needed, but why?

	}


	/**
	 * Returns the text on the "Close" button.
	 *
	 * @return The text on the Close button.
	 * @see #setCloseButtonText
	 */
	public final String getCloseButtonText() {
		return closeButton.getText();
	}


	/**
	 * Returns the cursor to use for a print-preview page of the specified
	 * scale.
	 *
	 * @param scale The scale of the page, in percent.
	 * @return The cursor to display while over the page.
	 */
	private final Cursor getCursorForScale(int scale) {
		return scale>=100 ? zoomOutCursor : zoomInCursor;
	}


	/**
	 * Creates and returns a cursor using the specified image and
	 * properties.
	 *
	 * @param image The image to use for the cursor.
	 * @param hotspot The "hotspot" for the cursor (i.e., when you click,
	 *        this is the exact pixel whose location is "clicked").
	 * @param name A name for the cursor.
	 * @param preferredWidth The desired width of the cursor.  Note that
	 *        this is only a suggestion; the cursor returned will be whatever
	 *        width and height the system says it wants.
	 * @param preferredHeight The desired height of the cursor.  Like
	 *        <code>preferredWidth</code>, the system may want a
	 *        wider or shorter cursor than this value.
	 * @return The cursor.
	 */
	private static final Cursor getCustomCursor(Image image, Point hotspot,
						String name, int preferredWidth,
						int preferredHeight) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension bestCursorSize = toolkit.getBestCursorSize(
								preferredWidth, preferredHeight);
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		if (bestCursorSize.width==width && bestCursorSize.height==height) {
			return toolkit.createCustomCursor(image, hotspot, name);
		}
		BufferedImage cursorImage = new BufferedImage(
					bestCursorSize.width, bestCursorSize.height,
					BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = cursorImage.createGraphics();
		g2d.drawImage(image, 0,0, null);
		return toolkit.createCustomCursor(cursorImage, hotspot, name);
	}


	/**
	 * Returns the text on the "Next Page" button.
	 *
	 * @return The text on the Next Page button.
	 * @see #setNextPageButtonText
	 */
	public final String getNextPageButtonText() {
		return nextPageButton.getText();
	}


	/**
	 * Returns the border to use on page preview panels.
	 *
	 * @return The border.
	 */
	private synchronized final Border getPagePreviewBorder() {
		if (pagePreviewBorder==null)
			pagePreviewBorder = new MatteBorder(1, 1, 2, 2,
									Color.BLACK);
		return pagePreviewBorder;
	}


	/**
	 * Returns the text on the "Prev Page" button.
	 *
	 * @return The text on the Previous Page button.
	 * @see #setPrevPageButtonText
	 */
	public final String getPrevPageButtonText() {
		return prevPageButton.getText();
	}


	/**
	 * Returns the text on the "Print" button.
	 *
	 * @return The text on the Print button.
	 * @see #setPrintButtonText
	 */
	public final String getPrintButtonText() {
		return printButton.getText();
	}


	/**
	 * Increments the first preview page, if possible, and updates the
	 * screen.  This method is designed as an alternative to
	 * <code>setPreviewPage()</code> that performs better with the more
	 * preview pages visible, as only one new page ever has to be created.
	 */
	public void incrementPreviewPage() {

		if (pageImage==null || currentPage==numPages-numVisiblePages)
			return;
		currentPage++;

		// Grab the "first" page displayed.  We'll remove this one
		// from the preview panel, push any pages following it
		// "back," then reuse this one for the new page and add it
		// back at the end.
		PagePreview pp = pageImage[0];
		previewPanel.remove(pp);

		// Move 'forward' one page in all visible pages.  This should work
		// even if some of the pages are null (i.e., no more pages to view),
		// as we'd just be setting stuff to null.
		for (int i=0; i<numVisiblePages-1; i++)
			pageImage[i] = pageImage[i+1];

		pageWidth = (int)(pageFormat.getWidth());
		pageHeight = (int)(pageFormat.getHeight());
		try {
			// Reuse the preview panel since its master image size is still valid.
			BufferedImage tempImage = pp.getSourceImage();
			Graphics g = tempImage.getGraphics();
			g.setColor(Color.white);
			g.fillRect(0,0, pageWidth,pageHeight);
			if (masterImage.print(g, pageFormat, currentPage+numVisiblePages-1) == Printable.PAGE_EXISTS) {
				int w = (int)(pageWidth*scale/100.0);
				int h = (int)(pageHeight*scale/100.0);
				pp.setScaledSize(w, h); // Re-initialize the scaled (displayed) version.
				pageImage[numVisiblePages-1] = pp;
				previewPanel.add(pp); // Add back to the end.
			}
			else {
				pageImage[numVisiblePages-1] = null;
			}
			g.dispose();
			previewPanel.revalidate();
			previewPanel.repaint(); // Needed, but why?
		} catch (PrinterException pe) {
			pe.printStackTrace();
		}

	}


	// Called whenever the user changes the value in one of the comboboxes.
	public void itemStateChanged(ItemEvent e) {

		JComboBox source = (JComboBox)e.getSource();

		if (e.getStateChange()==ItemEvent.SELECTED) {

			setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			if (source.equals(sizeComboBox)) {
				String choice = (String)e.getItem();
				choice = choice.substring(0, choice.length()-1);
				scale = Integer.parseInt(choice);
				Cursor newCursor = getCursorForScale(scale);
				if (pageImage!=null && pageImage.length>0) {
					int w = (int)(pageWidth*scale/100.0);
					int h = (int)(pageHeight*scale/100.0);
					boolean setNewCursor = pageImage[0].getCursor()!=newCursor;
					for (int i=0; i<pageImage.length; i++)
						if (pageImage[i]!=null) {
							pageImage[i].setScaledSize(w,h);
							if (setNewCursor==true)
								pageImage[i].setCursor(newCursor);
						}
				}
				previewPanel.revalidate();
			}

			else if (source.equals(numPagesComboBox)) {
				int choice = Integer.parseInt((String)e.getItem());
				choice = Math.min(choice, numPages);
				setNumVisiblePages(choice);
				// We MUST reset the preview page to page 0 because if we
				// don't, the user could be previewing the last page with
				// visible pages set to 1, then up visible pages 4, but then
				// if there aren't 4 pages in the document and the user
				// then clicks "Previous", an Exception will be thrown.
				setPreviewPage(0);
			}

			updateButtons();
			setCursor( Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		}

	}


	/**
	 * Sets the text on the "Close" button.
	 *
	 * @param text The text for the Close button.
	 * @see #getCloseButtonText
	 */
	public void setCloseButtonText(String text) {
		closeButton.setText(text);
	}


	/**
	 * Sets the text on the "Next Page" button.
	 *
	 * @param text The text for the Next Page button.
	 * @see #getNextPageButtonText
	 */
	public void setNextPageButtonText(String text) {
		nextPageButton.setText(text);
	}


	/**
	 * Sets the number of pages currently being previewed.
	 *
	 * @param num The number of pages to preview.  If <code>num</code> is less
	 *        than <code>1</code>, then it is set to <code>1</code>.  If it is
	 *        greater than the number of pages in the document, it is set to
	 *        the number of pages in the document.
	 */
	private void setNumVisiblePages(int num) {
		if (numPages<=0)
			numPages = 1;
		if (num>numPages)
			num = numPages;
		numVisiblePages = num;
	}


	/**
	 * Sets the text on the "Prev Page" button.
	 *
	 * @param text The text on the Previous Page button.
	 * @see #getPrevPageButtonText
	 */
	public void setPrevPageButtonText(String text) {
		prevPageButton.setText(text);
	}


	/**
	 * Sets the text on the "Print" button.
	 *
	 * @param text The text on the Print button.
	 * @see #getPrintButtonText
	 */
	public void setPrintButtonText(String text) {
		printButton.setText(text);
	}


	/**
	 * Sets the first page being previewed.
	 *
	 * @param pageNumber The first page to preview.
	 */
	private void setPreviewPage(int pageNumber) {

		previewPanel.removeAll();	// Remove all currently-viewed pages.
		currentPage = pageNumber;

		pageWidth = (int)(pageFormat.getWidth());
		pageHeight = (int)(pageFormat.getHeight());
		int w = (int)(pageWidth*scale/100.0);
		int h = (int)(pageHeight*scale/100.0);
		try {

			Cursor cursorToUse = getCursorForScale(scale);

			for (int i=0; i<numVisiblePages; i++) {

				BufferedImage img = new BufferedImage(pageWidth,pageHeight, BufferedImage.TYPE_INT_RGB);
				Graphics g = img.getGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, pageWidth,pageHeight);
				if (masterImage.print(g, pageFormat, currentPage+i) == Printable.PAGE_EXISTS) {
					pageImage[i] = new PagePreview(w,h, img, cursorToUse);
					previewPanel.add(pageImage[i]);
				}
				else
					pageImage[i] = null;
				g.dispose();

			}

			previewPanel.revalidate();
			previewPanel.repaint(); // Needed, but why?

		} catch (PrinterException pe) {
			pe.printStackTrace();
		}

	}


	/**
	 * Sets what scale to make the preview images of the pages.
	 *
	 * @param newScale The scale to use when sizing the page
	 *        previews, in percent (i.e., 33 => 33%).
	 */
	public void setScale(int newScale) {
		sizeComboBox.setSelectedItem(Integer.toString(newScale)+"%");
	}


	/**
	 * Updates the buttons to be enabled/disabled as appropriate.
	 */
	private void updateButtons() {
		prevPageButton.setEnabled(currentPage>0);
		nextPageButton.setEnabled(currentPage<numPages-numVisiblePages);
	}


	/**
	 * A panel representing the image of a page.
	 */
	class PagePreview extends JPanel {

		private static final long serialVersionUID = 1L;

		protected int width;
		protected int height;
		protected BufferedImage sourceImage;
		protected Image drawImage;

		public PagePreview(int width, int height, BufferedImage source,
							Cursor cursorToUse) {
			sourceImage = source;
			setScaledSize(width, height);
			setBorder(getPagePreviewBorder());
			setCursor(cursorToUse);
			enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		}

		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			Insets ins = getInsets();
			return new Dimension(width+ins.left+ins.right,
							height+ins.top+ins.bottom);
		}

		public BufferedImage getSourceImage() {
			return sourceImage;
		}

		@Override
		public void paintComponent(Graphics g) {
			g.drawImage(drawImage, 0, 0, this);
		}

		@Override
		protected void processMouseEvent(MouseEvent e) {
			if (e.getID()==MouseEvent.MOUSE_PRESSED &&
					e.getClickCount()==1 &&
					e.getButton()==MouseEvent.BUTTON1) {
				setScale(getCursor()==zoomInCursor ? 100 : 25);
			}
			super.processMouseEvent(e);
		}

		public void setScaledSize(int width, int height) {
			this.width = width;
			this.height = height;
			drawImage = sourceImage.getScaledInstance(width, height,
						Image.SCALE_SMOOTH);//Image.SCALE_DEFAULT);
			repaint();
		}

	}


}