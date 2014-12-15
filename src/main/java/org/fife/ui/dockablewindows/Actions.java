package org.fife.ui.dockablewindows;

import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


/**
 * Actions related to dockable windows.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Actions {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private Actions() {
	}


	/**
	 * Creates and returns a popup menu that can be used to change the dock
	 * location of a dockable window.
	 *
	 * @param dwindPanel The parent dockable window panel.
	 * @return The popup menu.
	 */
	public static JPopupMenu createRedockPopupMenu(
									DockableWindowPanel dwindPanel) {
		JPopupMenu popup = new JPopupMenu();
		JMenu dockedLocMenu = new JMenu(DockableWindow.getString("Dock.Location"));
		dockedLocMenu.add(new JMenuItem(new Actions.RedockAction(
				dwindPanel, DockableWindow.LEFT, "Dock.Left")));
		dockedLocMenu.add(new JMenuItem(new Actions.RedockAction(
				dwindPanel, DockableWindow.RIGHT, "Dock.Right")));
		dockedLocMenu.add(new JMenuItem(new Actions.RedockAction(
				dwindPanel, DockableWindow.BOTTOM, "Dock.Bottom")));
		dockedLocMenu.add(new JMenuItem(new Actions.RedockAction(
				dwindPanel, DockableWindow.TOP, "Dock.Top")));
		dockedLocMenu.add(new JMenuItem(new Actions.RedockAction(
				dwindPanel, DockableWindow.FLOATING, "Dock.Floating")));
		popup.add(dockedLocMenu);
		popup.addSeparator();
		popup.add(new JMenuItem(new Actions.CloseAction(dwindPanel)));
		return popup;
	}


	/**
	 * Closes the selected docked window.  Expected to be used from a
	 * popup menu.
	 */
	public static class CloseAction extends DockableActionBase {


		public CloseAction(DockableWindowPanel dwindPanel) {
			super(dwindPanel);
			putValue(NAME, DockableWindow.getString("PopupMenu.Close"));
		}


		public void actionPerformed(ActionEvent e) {
			Container source = (Container)e.getSource();
			JComponent invoker = getOriginalInvoker(source);
			DockableWindow dwind = (DockableWindow)invoker.
							getClientProperty("DockableWindow");
			dwindPanel.removeDockableWindow(dwind);
			dwind.setActive(false);
		}


	}


	/**
	 * Base class for all dockable window-related actions.
	 */
	private abstract static class DockableActionBase extends AbstractAction {

		protected DockableWindowPanel dwindPanel;


		public DockableActionBase(DockableWindowPanel dwindPanel) {
			this.dwindPanel = dwindPanel;
		}


		/**
		 * Returns the original invoking component, taking into account
		 * nested popup menus.
		 *
		 * @param source The source component (a JMenuItem).
		 * @return The original invoking component of the 
		 */
		protected JComponent getOriginalInvoker(Container source) {
			JPopupMenu popup = (JPopupMenu)source.getParent();
			JComponent invoker = (JComponent)popup.getInvoker();
			while (invoker instanceof JMenu) { // Just a nested menu
				popup = (JPopupMenu)((JMenu)invoker).getParent();
				invoker = (JComponent)popup.getInvoker();
			}
			return invoker;
		}

	}


	/**
	 * Moves the selected dockable window to a new location in the parent
	 * <code>DockableWindowPanel</code>.  Expected to be used from a popup
	 * menu.
	 */
	public static class RedockAction extends DockableActionBase {

		private int location;


		public RedockAction(DockableWindowPanel dwindPanel, int location,
							String nameKey) {
			super(dwindPanel);
			putValue(NAME, DockableWindow.getString(nameKey));
			this.location = location;
		}


		public void actionPerformed(ActionEvent e) {
			Container source = (Container)e.getSource();
			JComponent invoker = getOriginalInvoker(source);
			DockableWindow dwind = (DockableWindow)invoker.
							getClientProperty("DockableWindow");
//			dwindPanel.removeDockableWindow(dwind);
//			dwind.setActive(false);
			dwind.setPosition(location);
		}


	}


	/*
	private class RestoreAction extends AbstractAction {

		public RestoreAction() {
			putValue(SHORT_DESCRIPTION, // Tool tip
				DockableWindow.getString("Button.Restore"));
			Icon icon = new ImageIcon(getClass().getResource("restore.png"));
			putValue(SMALL_ICON, icon);
		}

		public void actionPerformed(ActionEvent e) {
			setCollapsed(false);
		}

	}
	*/


}