/*
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package org.fife.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This class allows you to move a Component by using a mouse. The Component
 * moved can be a high level Window (ie. Window, Frame, Dialog) in which case
 * the Window is moved within the desktop. Or the Component can belong to a
 * Container in which case the Component is moved within the Container.
 * <p>
 * When moving a Window, the listener can be added to a child Component of
 * the Window. In this case attempting to move the child will result in the
 * Window moving. For example, you might create a custom "Title Bar" for an
 * undecorated Window and moving of the Window is accomplished by moving the
 * title bar only. Multiple components can be registered as "window movers".
 * <p>
 * Components can be registered when the class is created. Additional
 * components can be added at any time using the registerComponent() method.
 * <p>
 *
 * Example usage:
 * <code>
 *     ComponentMover mover = new ComponentMover(frame, frame.getJMenuBar());
 *     mover.setChangeCursor(false);
 * </code>
 *
 * This class was originally written by Rob Camick and is based off of this blog post:
 * https://tips4java.wordpress.com/2009/06/14/moving-windows/
 *
 * @author Rob Camick
 * @author Robert Futrell
 */
public class ComponentMover extends MouseAdapter {

	private Insets dragInsets = new Insets(0, 0, 0, 0);
	private Insets edgeInsets = new Insets(0, 0, 0, 0);
	private boolean changeCursor = true;
	private boolean autoLayout;

	private Class<?> destinationClass;
	private Component destinationComponent;
	private Component destination;
	private Component source;

	private Point pressed;
	private Point location;

	private Cursor originalCursor;
	private boolean autoScrolls;
	private boolean potentialDrag;


	/**
	 * Constructor for moving individual components. The components must be
	 * registered using the registerComponent() method.
	 */
	public ComponentMover() {
	}

	/**
	 * Constructor to specify a Class of Component that will be moved when
	 * drag events are generated on a registered child component. The events
	 * will be passed to the first ancestor of this specified class.
	 *
	 * @param destinationClass The Class of the ancestor component.
	 * @param components       The Components to be registered for forwarding
	 *                         drag events to the ancestor Component.
	 */
	public ComponentMover(Class<?> destinationClass, Component... components) {
		this.destinationClass = destinationClass;
		registerComponent(components);
	}

	/**
	 * Constructor to specify a parent component that will be moved when drag
	 * events are generated on a registered child component.
	 *
	 * @param destinationComponent the component drag events should be forwarded to.
	 * @param components           the Components to be registered for forwarding drag
	 *                             events to the parent component to be moved.
	 */
	public ComponentMover(Component destinationComponent, Component... components) {
		this.destinationComponent = destinationComponent;
		registerComponent(components);
	}

	/**
	 * Get the auto layout property.
	 *
	 * @return the auto layout property.
	 */
	public boolean isAutoLayout() {
		return autoLayout;
	}

	/**
	 * Set the auto layout property.
	 *
	 * @param autoLayout when true layout will be invoked on the parent container.
	 */
	public void setAutoLayout(boolean autoLayout) {
		this.autoLayout = autoLayout;
	}

	/**
	 * Get the change cursor property.
	 *
	 * @return the change cursor property.
	 */
	public boolean isChangeCursor() {
		return changeCursor;
	}

	/**
	 * Set the change cursor property.
	 *
	 * @param changeCursor when {@code true} the cursor will be changed to the {@code Cursor.MOVE_CURSOR}
	 *        while the mouse is pressed
	 */
	public void setChangeCursor(boolean changeCursor) {
		this.changeCursor = changeCursor;
	}

	/**
	 * Get the drag insets.
	 *
	 * @return the drag insets.
	 */
	public Insets getDragInsets() {
		return dragInsets;
	}

	/**
	 * Set the drag insets. The insets specify an area where mouseDragged
	 * events should be ignored and therefore the component will not be moved.
	 * This will prevent these events from being confused with a
	 * MouseMotionListener that supports component resizing.
	 *
	 * @param dragInsets The new insets.
	 */
	public void setDragInsets(Insets dragInsets) {
		this.dragInsets = dragInsets;
	}

	/**
	 * Get the bounds insets.
	 *
	 * @return the bounds insets.
	 */
	public Insets getEdgeInsets() {
		return edgeInsets;
	}

	/**
	 * Set the edge insets. The insets specify how close to each edge of the parent
	 * component that the child component can be moved. Positive values means the
	 * component must be contained within the parent. Negative values means the
	 * component can be moved outside the parent.
	 *
	 * @param edgeInsets The new insets.
	 */
	public void setEdgeInsets(Insets edgeInsets) {
		this.edgeInsets = edgeInsets;
	}

	/**
	 * Remove listeners from the specified component.
	 *
	 * @param components The components the listeners are removed from.
	 */
	public void deregisterComponent(Component... components) {
		for (Component component : components) {
			component.removeMouseListener(this);
		}
	}

	/**
	 * Add the required listeners to the specified component.
	 *
	 * @param components The components the listeners are added to.
	 */
	public void registerComponent(Component... components) {
		for (Component component : components) {
			component.addMouseListener(this);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		if (e.getClickCount() == 2) {
			Window window = SwingUtilities.getWindowAncestor(e.getComponent());
			if (window instanceof Frame) {
				Frame frame = (Frame)window;
				boolean extended = frame.getExtendedState() == Frame.MAXIMIZED_BOTH;
				frame.setExtendedState(extended ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
			}
		}
	}

	/**
	 * Setup the variables used to control the moving of the component:
	 * <p>
	 * source - the source component of the mouse event
	 * destination - the component that will ultimately be moved
	 * pressed - the Point where the mouse was pressed in the destination
	 * component coordinates.
	 */
	@Override
	public void mousePressed(MouseEvent e) {

		source = e.getComponent();
		int width = source.getSize().width - dragInsets.left - dragInsets.right;
		int height = source.getSize().height - dragInsets.top - dragInsets.bottom;
		Rectangle r = new Rectangle(dragInsets.left, dragInsets.top, width, height);

		if (r.contains(e.getPoint())) {
			setupForDragging(e);
		}
	}

	private void setupForDragging(MouseEvent e) {

		source.addMouseMotionListener(this);
		potentialDrag = true;

		//  Determine the component that will ultimately be moved

		if (destinationComponent != null) {
			destination = destinationComponent;
		}
		else if (destinationClass == null) {
			destination = source;
		}
		else { //  forward events to destination component
			destination = SwingUtilities.getAncestorOfClass(destinationClass, source);
		}

		pressed = e.getLocationOnScreen();
		location = destination.getLocation();

		if (changeCursor) {
			originalCursor = source.getCursor();
			source.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}

		// Making sure autoScrolls is false will allow for smoother dragging of
		// individual components

		if (destination instanceof JComponent) {
			JComponent jc = (JComponent)destination;
			autoScrolls = jc.getAutoscrolls();
			jc.setAutoscrolls(false);
		}
	}

	/**
	 * Move the component to its new location. The dragged Point must be in
	 * the destination coordinates.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {

		Point dragged = e.getLocationOnScreen();
		int dragX = getDragDistance(dragged.x, pressed.x);
		int dragY = getDragDistance(dragged.y, pressed.y);

		int locationX = location.x + dragX;
		int locationY = location.y + dragY;

		destination.setLocation(locationX, locationY);
	}

	/*
	 *  Determine how far the mouse has moved from where dragging started
	 *  (Assume drag direction is down and right for positive drag distance)
	 */
	private static int getDragDistance(int larger, int smaller) {
		return larger - smaller;
	}

	/**
	 * Restore the original state of the Component.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {

		if (!potentialDrag) {
			return;
		}

		source.removeMouseMotionListener(this);
		potentialDrag = false;

		if (changeCursor)
			source.setCursor(originalCursor);

		if (destination instanceof JComponent) {
			((JComponent)destination).setAutoscrolls(autoScrolls);
		}

		//  Layout the components on the parent container
		if (autoLayout) {
			destination.revalidate();
		}
	}
}
