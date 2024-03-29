/*
 * 04/13/2004
 *
 * OptionsDialogPanel.java - Base class for option panels that
 * go into an OptionsDialog.
 * Copyright (C) 2003 Robert Futrell
 * https://bobbylight.github.io/RText/
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;


/**
 * Base class for panels that go into an instance of <code>OptionsDialog</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public abstract class OptionsDialogPanel extends JPanel {

	/**
	 * The event fired when an option panel marks itself dirty via
	 * {@link #setDirty(boolean)}. The parent options dialog
	 * listens to this property to know where there are unsaved changes.
	 */
	static final String PROPERTY_DIRTY = "optionPanel.dirty";

	/**
	 * The amount of space to add between components in an option panel.
	 */
	protected static final int COMPONENT_VERTICAL_SPACING = 3;

	/**
	 * The amount of space to add between sections in an option panel.
	 */
	protected static final int SECTION_VERTICAL_SPACING = 5;

	/**
	 * The name of this options panel to be used in the the options dialog's
	 * selection tree.
	 */
	private String name;

	/**
	 * An identifier for this option panel.  Non-localized so it can be used
	 * in code.
	 */
	private String id;

	/**
	 * The icon to use for this options panel in the options dialog, if any.
	 */
	private Icon icon;

	/**
	 * whether this panel has any unsaved changes.  The Options panel
	 * should set this whenever an Option value is changed by the user, but
	 * it doesn't have to clear it (this is done by the parent
	 * <code>OptionsDialog</code>).
	 */
	private boolean dirty;

	/**
	 * A collection of "child" option panels in the options dialog tree.
	 */
	private List<OptionsDialogPanel> childPanels;

	/**
	 * Parent panel.  null if no parent panel.
	 */
	private OptionsDialogPanel parent;


	/**
	 * Constructor.
	 */
	public OptionsDialogPanel() {
		this("<Unnamed>");
	}


	/**
	 * Constructor.
	 *
	 * @param name The name of this options panel to be used in the options
	 *        dialog's selection tree.
	 */
	public OptionsDialogPanel(String name) {
		this.name = name;
		this.dirty = false;
		childPanels = new ArrayList<>(0);
	}


	/**
	 * Adds a "child" option panel to this one.
	 *
	 * @param child The option panel to add as a child.
	 * @see #getChildPanelCount
	 * @see #getChildPanel
	 */
	public void addChildPanel(final OptionsDialogPanel child) {
		childPanels.add(child);
		child.parent = this;
	}


	/**
	 * Adds a child component to a container, ensuring it is left-aligned.
	 *
	 * @param parent The parent container.  This should have a vertical
	 *        BoxLayout.
	 * @param toAdd The component to add.
	 * @see #addLeftAligned(Container, Component, int)
	 * @see #addLeftAligned(Container, Component, int, int)
	 */
	protected void addLeftAligned(Container parent, Component toAdd) {
		addLeftAligned(parent, toAdd, 0);
	}


	/**
	 * Adds a child component to a container, ensuring it is left-aligned.
	 *
	 * @param parent The parent container.  This should have a vertical
	 *        BoxLayout.
	 * @param toAdd The component to add.
	 * @param spacer The amount of vertical space to add after the component.
	 *        This may be zero.
	 * @see #addLeftAligned(Container, Component)
	 * @see #addLeftAligned(Container, Component, int, int)
	 */
	protected void addLeftAligned(Container parent, Component toAdd,
									int spacer) {
		addLeftAligned(parent, toAdd, spacer, 0);
	}


	/**
	 * Adds a child component to a container, ensuring it is left-aligned.
	 *
	 * @param parent The parent container.  This should have a vertical
	 *        BoxLayout.
	 * @param toAdd The component to add.
	 * @param spacer The amount of vertical space to add after the component.
	 *        This may be zero.
	 * @param indent An amount to indent <code>toAdd</code> by.  This may be
	 *        zero.
	 * @see #addLeftAligned(Container, Component)
	 * @see #addLeftAligned(Container, Component, int)
	 */
	protected void addLeftAligned(Container parent, Component toAdd,
									int spacer, int indent) {

		indent = Math.max(indent, 0);

		if (indent>0) {
			Box box = createHorizontalBox();
			box.add(Box.createHorizontalStrut(indent));
			box.add(toAdd);
			box.add(Box.createHorizontalGlue());
			toAdd = box;
		}

		// Always wrap everything inside BorderLayout.  Even a horizontal
		// BoxLayout will slightly resize with a window.
		JPanel temp = new JPanel(new BorderLayout());
		temp.add(toAdd, BorderLayout.LINE_START);

		parent.add(temp);
		if (spacer>0) {
			parent.add(Box.createVerticalStrut(spacer));
		}

	}


	/**
	 * Returns a horizontal box that respects component orientation, which
	 * <code>Box.createHorizontalBox()</code> does not, for backward
	 * compatibility reasons (!).
	 *
	 * @return The horizontal box.
	 */
	protected Box createHorizontalBox() {
		return new Box(BoxLayout.LINE_AXIS);
	}


	/**
	 * Applies the settings entered into this panel on the specified
	 * application.  Child panels are also handled.
	 *
	 * @param owner The application.
	 * @see #setValues(Frame)
	 */
	public final void doApply(Frame owner) {
		if (isDirty()) {
			doApplyImpl(owner);
		}
		for (int i=0; i<getChildPanelCount(); i++) {
			getChildPanel(i).doApply(owner);
		}
	}


	/**
	 * Applies the settings entered into this panel on the specified
	 * application.  Child panels are not handled.
	 *
	 * @param owner The application.
	 * @see #doApply(Frame)
	 */
	protected abstract void doApplyImpl(Frame owner);


	/**
	 * Checks whether all input the user specified on this panel, and
	 * any child panels, is valid.
	 *
	 * @return <code>null</code> if the panel has all valid inputs, or an
	 *         <code>OptionsPanelCheckResult</code> if an input was invalid.
	 *         This component is the one that had the error and will be
	 *         given focus, and the string is an error message that will be
	 *         displayed.
	 */
	public final OptionsPanelCheckResult ensureValidInputs() {
		OptionsPanelCheckResult res = ensureValidInputsImpl();
		if (res==null) {
			for (int i=0; i<getChildPanelCount(); i++) {
				res = getChildPanel(i).ensureValidInputs();
				if (res!=null) {
					break;
				}
			}
		}
		return res;
	}


	/**
	 * Checks whether all input the user specified on this panel is
	 * valid.  This should be overridden to check, for example, whether
	 * text fields have valid values, etc.  This method will be called
	 * whenever the user clicks "OK" or "Apply" on the options dialog to
	 * ensure all input is valid.  If it isn't, the component with invalid
	 * data will be given focus and the user will be prompted to fix it.
	 *
	 * @return <code>null</code> if the panel has all valid inputs, or an
	 *         <code>OptionsPanelCheckResult</code> if an input was invalid.
	 *         This component is the one that had the error and will be
	 *         given focus, and the string is an error message that will be
	 *         displayed.
	 */
	protected abstract OptionsPanelCheckResult ensureValidInputsImpl();


	/**
	 * Returns the specified child option panel.
	 *
	 * @param index The index of the child option panel to return.
	 * @return The child option panel.
	 * @see #addChildPanel
	 * @see #getChildPanelCount()
	 * @see #getChildPanels()
	 */
	public OptionsDialogPanel getChildPanel(int index) {
		return childPanels.get(index);
	}


	/**
	 * Gets the number of "child" option panels.
	 *
	 * @return The child option panel count.
	 * @see #addChildPanel(OptionsDialogPanel)
	 * @see #getChildPanel(int)
	 * @see #getChildPanels()
	 */
	public int getChildPanelCount() {
		return childPanels.size();
	}


	/**
	 * Returns all child panels of this options panel.
	 *
	 * @return All child panels.  This may be empty, but will never be
	 *         <code>null</code>.  This list is a copy and can be modified.
	 * @see #getChildPanel(int)
	 * @see #getChildPanelCount()
	 */
	public List<OptionsDialogPanel> getChildPanels() {
		return new ArrayList<>(childPanels);
	}


	/**
	 * Returns the icon to display for this options panel, if any.
	 *
	 * @return The icon for this options dialog panel, or <code>null</code>
	 *         if there isn't one.
	 * @see #setIcon(Icon)
	 */
	public Icon getIcon() {
		return icon;
	}


	/**
	 * Returns the ID of this option panel.
	 *
	 * @return The ID of this option panel, or <code>null</code> for none.
	 * @see #setId(String)
	 */
	public String getId() {
		return id;
	}


	/**
	 * Returns the string used to describe this panel in the left-hand
	 * tree pane of the options dialog.
	 *
	 * @return This option panel's name.
	 */
	@Override
	public String getName() {
		return name;
	}


	/**
	 * Returns the Options dialog that contains this options panel.
	 *
	 * @return The options dialog, or <code>null</code> if this panel
	 *         hasn't been added to an Options dialog yet.
	 */
	public OptionsDialog getOptionsDialog() {
		Container parent = getParent();
		while (parent!=null && !(parent instanceof OptionsDialog))
			parent = parent.getParent();
		return (OptionsDialog)parent;
	}


	/**
	 * Returns the parent options dialog panel.  This value may be
	 * <code>null</code> if there is no parent panel (e.g. the parent
	 * of this panel is the options dialog itself).
	 *
	 * @return The parent options dialog panel.
	 */
	protected OptionsDialogPanel getParentPanel() {
		return parent;
	}


	/**
	 * Returns the <code>JComponent</code> at the "top" of this Options
	 * panel.  This is the component that will receive focus if the user
	 * switches to this Options panel in the Options dialog.  As an added
	 * bonus, if this component is a <code>JTextComponent</code>, its
	 * text is selected for easy changing.
	 *
	 * @return The top component.
	 */
	public abstract JComponent getTopJComponent();


	/**
	 * Returns whether this Options panel has unsaved changes.  Note
	 * that these changes may or may not be invalid.
	 *
	 * @return Whether this panel has any unsaved changes.
	 * @see #setDirty(boolean)
	 */
	public boolean isDirty() {
		return dirty;
	}


	/**
	 * Returns whether this panel is in the process of setting all values
	 * displayed in it to reflect the state of the application (i.e.,
	 * the parent dialog is initializing).
	 *
	 * @return Whether this panel is initializing.
	 */
	protected boolean isInitializing() {
		// The parent dialog might be null during the very first initialization
		OptionsDialog dialog = getOptionsDialog();
		return dialog == null || dialog.isInitializing();
	}


	/**
	 * Called when an options-related event is broadcasted to all panels in
	 * the parent options dialog.<p>
	 * The default implementation simply forwards the event to all of its child
	 * panels, if any.  Subclasses should be sure to call the super
	 * implementation to continue this behavior.
	 *
	 * @param event The event.
	 */
	public void optionsEvent(String event) {
		for (OptionsDialogPanel panel : childPanels) {
			panel.optionsEvent(event);
		}
	}


	/**
	 * Sets the icon to use for this option panel in the dialog.
	 *
	 * @param icon The icon to use, or <code>null</code> for none.
	 * @see #getIcon()
	 */
	public void setIcon(Icon icon) {
		this.icon = icon;
	}


	/**
	 * Sets the ID of this option panel.
	 *
	 * @param id The ID of this option panel.
	 * @see #getId()
	 */
	public void setId(String id) {
		this.id = id;
	}


	/**
	 * Sets the name of this options panel.
	 *
	 * @param name The name to use for this options panel.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Sets whether the dirty flag for this Options panel
	 * is set.  You should call this method with a parameter set to
	 * <code>false</code> before displaying an Options dialog.
	 *
	 * @param dirty whether the flag should be set.
	 * @see #isDirty()
	 */
	public void setDirty(boolean dirty) {
		if (!isInitializing() && dirty != this.dirty) {
			this.dirty = dirty;
			firePropertyChange(PROPERTY_DIRTY, !dirty, dirty);
		}
	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are also handled.
	 *
	 * @param owner The parent application.
	 * @see #doApply(Frame)
	 */
	public final void setValues(Frame owner) {
		// Don't check isDirty(), since the first-time through it'll be false
		setValuesImpl(owner);
		dirty = false;
		getChildPanels().forEach(panel -> panel.setValues(owner));
	}


	/**
	 * Sets the values displayed by this panel to reflect those in the
	 * application.  Child panels are not handled.
	 *
	 * @param owner The parent application.
	 * @see #setValues(Frame)
	 */
	protected abstract void setValuesImpl(Frame owner);


	/**
	 * Returns the name of this options panel, since this is the value
	 * that is displayed in the Options dialog's JList.
	 */
	@Override
	public final String toString() {
		return name;
	}


	/**
	 * The class that is returned from <code>ensureValidInputs</code>; it
	 * contains a <code>JComponent</code> that had invalid input, and a
	 * <code>String</code> to display as the error message.
	 */
	@SuppressWarnings("checkstyle:VisibilityModifier")
	public static class OptionsPanelCheckResult {

		public OptionsDialogPanel panel;
		public JComponent component;
		public String errorMessage;

		public OptionsPanelCheckResult(OptionsDialogPanel panel) {
			this.panel = panel;
		}

		public OptionsPanelCheckResult(OptionsDialogPanel panel,
						JComponent component, String errorMessage) {
			this.panel = panel;
			this.component = component;
			this.errorMessage = errorMessage;
		}

	}


	/**
	 * A border useful for dividing sections of an Options panel.
	 */
	public static class OptionPanelBorder implements Border {

		private String title;
		private Insets insets;
		private static final int INSETS_HEIGHT = 30;

		/**
		 * Constructor.
		 *
		 * @param title The title of the border.
		 */
		public OptionPanelBorder(String title) {
			this.title = title;
			insets = new Insets(INSETS_HEIGHT,8,8,8);
		}

		/**
		 * Returns the insets of the border.
		 *
		 * @param c Not used.
		 */
		@Override
		public Insets getBorderInsets(Component c) {
			// Dynamically calculate so this is updated on LaF change.
			// Reusing the same Insets instance is fine since nobody
			// mutates it.
			Font font = OptionPanelBorder.getFont();
			FontMetrics fm = c.getFontMetrics(font);
			insets.top = fm.getHeight() + 5;
			return insets;
		}

		/**
		 * Returns whether the border is opaque.
		 *
		 * @return This method always returns <code>true</code>.
		 */
		@Override
		public boolean isBorderOpaque() {
			return true;
		}

		private static Font getFont() {
			Font font = UIManager.getFont("Label.font");
			if (font == null) {
				// This is quite expensive for a repaint, but not sure of any
				// other way to do this.  All Swing's "standard" LAFs define
				// the Label.font property, as do all Substance looks, so
				// usually we won't hit this condition.
				// TODO: Find a more performant way to do this, but note that
				// LAF changes at runtime cause changes if you cache anything...
				font = new JLabel().getFont();
			}
			return font;
		}

		private Color getForeground(Component c) {
			Color fg = null;
			if (!c.isEnabled()) {
				fg = UIManager.getColor("Label.disabledForeground");
			}
			if (fg == null) {
				fg = UIUtil.getHyperlinkForeground();
			}
			return fg;
		}

		/**
		 * Paints the border for the specified component with the specified
		 * position and size.
		 *
		 * @param c The component that has this border.
		 * @param g The graphics context with which to paint.
		 * @param x The x-coordinate of the border.
		 * @param y The y-coordinate of the border.
		 * @param width The width of the component.
		 * @param height The height of the component.
		 */
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y,
							int width, int height) {

			Graphics2D g2d = (Graphics2D)g;

			// Try to use the rendering hint set that is "native".
			RenderingHints old = UIUtil.setNativeRenderingHints(g2d);

			g.setColor(getForeground(c));
			Font font = OptionPanelBorder.getFont();
			FontMetrics fm = c.getFontMetrics(font);
			int titleWidth = fm.stringWidth(title);
			int titleY = y + fm.getLeading() + fm.getAscent();
			int lineY = titleY - fm.getAscent() / 2;

			ComponentOrientation orientation = c.getComponentOrientation();
			boolean isDarkLaf = UIUtil.isLightForeground(c.getForeground());
			Color lineColor = isDarkLaf ? c.getBackground().brighter() :
				c.getBackground().darker();
			if (orientation.isLeftToRight()) {
				g.drawString(title, x,titleY);
				g.setColor(lineColor);
				g.drawLine(x+titleWidth+5, lineY, x+width, lineY);
			}
			else {
				int titleX = x+width-titleWidth-1;
				g.drawString(title, titleX,titleY);
				g.setColor(lineColor);
				g.drawLine(x, lineY, titleX-5, lineY);
			}

			if (old!=null) {
				g2d.addRenderingHints(old);
			}

		}

	}


}
