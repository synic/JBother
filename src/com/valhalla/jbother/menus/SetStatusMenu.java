/*
 *  Copyright (C) 2003 Adam Olsen
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.valhalla.jbother.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;

import org.jivesoftware.smack.packet.Presence;

import com.valhalla.jbother.*;
import com.valhalla.gui.*;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.SelfStatus;
import com.valhalla.jbother.jabber.SelfStatuses;
import com.valhalla.settings.Settings;

/**
 * Allows the user to change his/her status
 *
 * @author Adam Olsen
 * @author Yury Soldak (tail)
 * @autho Andrey Zakirov
 * @created April 10, 2005
 * @version 1.6
 */
public class SetStatusMenu extends JPopupMenu {
	private BuddyList blist;

	private ResourceBundle resources = ResourceBundle.getBundle(
			"JBotherBundle", Locale.getDefault());

	private SelfStatuses statuses = com.valhalla.jbother.jabber.SelfStatuses
			.getInstance();

	private ImageIcon current = null;

	private javax.swing.Timer blinkTimer = null;

	private boolean useIcon = false;

	private JButton button = new JButton();

	/**
	 * Sets up the SetStatusMenu
	 *
	 * @param blist
	 *            the BuddyList that this menu is attached to
	 */
	public SetStatusMenu(BuddyList blist, boolean useIcon, JButton button) {
		this.button = button;
		this.blist = blist;
		this.useIcon = useIcon;
		Iterator statusIterator = statuses.getContent().iterator();
		SelfStatus curStatus;
		while (statusIterator.hasNext()) {
			curStatus = (SelfStatus) statusIterator.next();
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(curStatus.getTitle());
			if (curStatus.getMode() == blist.getCurrentPresenceMode()) {
				item.setState(true);
			}
			add(item);
		}


		if (useIcon) {
			button.setIcon(StatusIconCache.getStatusIcon(Presence.Mode.AVAILABLE));
		}

        button.setText ( blist.getCurrentStatusString ());

		if (System.getProperty("mrj.version") != null) {
			button.setText(resources.getString("status"));
		}

		if (Settings.getInstance().getProperty("statusTheme") == null) {
			Settings.getInstance().setProperty("statusTheme", "default");
		}

		reloadStatusIcons();

		setUpListeners();
	}

    public SetStatusMenu(BuddyList blist, boolean useIcon)
    {
        this(blist,useIcon,BuddyList.getInstance().getStatusButton());
    }

	/**
	 * starts the blink timer
	 */
	public void startBlinkTimer() {
		current = (ImageIcon) button.getIcon();
		blinkTimer = new javax.swing.Timer(400, new BlinkHandler());
		blinkTimer.start();
	}

	/**
	 * @return true if the blink timer is still running
	 */
	public boolean blinkTimerIsRunning() {
		if (blinkTimer != null && blinkTimer.isRunning())
			return true;
		else
			return false;
	}

	/**
	 * stops the blink timer
	 */
	public void stopBlinkTimer() {
		if (blinkTimer != null)
			blinkTimer.stop();
		blinkTimer = null;
		if (useIcon)
			button.setIcon(current);
	}

	class BlinkHandler implements ActionListener {
		private ImageIcon off = StatusIconCache.getStatusIcon(null);

		private ImageIcon on = StatusIconCache
				.getStatusIcon(Presence.Mode.AVAILABLE);

		private ImageIcon current = on;

		public void actionPerformed(ActionEvent e) {
			if (current == on)
				current = off;
			else if (current == off)
				current = on;

			if (useIcon)
				button.setIcon(current);
		}
	}

	/**
	 * Loads self statuses (information about the current online user) and
	 * creates a tooltip on the SetStatusMenu with this information
	 */
	public void loadSelfStatuses() {
		if (!blist.checkConnection())
			return;

		if (useIcon) {
			button.setIcon(StatusIconCache.getStatusIcon(BuddyList.getInstance()
					.getCurrentPresenceMode()));
		}

		String me = blist.getConnection().getUser().replaceAll("/.*", "");

		BuddyStatus buddy = blist.getBuddyStatus(me);
		String user = buddy.getUser();
		String server = buddy.getUser();
		if (user.indexOf('@') > -1) {
			String parts[] = new String[2];
			parts = buddy.getUser().split("@");
			user = parts[0];
			server = parts[0];
			if (parts[1] != null) {
				server = parts[1];
			}
		}

		String resources = "";
		Iterator i = buddy.keySet().iterator();
		int resourceCount = 0;

		while (i.hasNext()) {
			String key = (String) i.next();

			if (!key.equals("N/A")) {
				boolean add = false;

				if (key.equals(buddy.getHighestResource())) {
					add = true;
				} else {
					resources += "  ";
				}

				resources += key + " (" + buddy.get(key) + ")";
				if (add) {
					resources += " <b>*</b>";
				}
				if (i.hasNext()) {
					resources += "<br>";
				}
				resourceCount++;
			}
		}

		String tooltip = "<html><table border='0'><tr><td colspan='2'><b><font size='+1'>"
				+ user
				+ "</font></b><table border='0' cellpadding='2' cellspacing='2'><tr><td nowrap><b>"
				+ this.resources.getString("server")
				+ ":</b></td><td nowrap>"
				+ server + "</td></tr>";

		if (resourceCount > 0) {
			tooltip += "<tr><td nowrap valign=\"top\"><b>"
					+ this.resources.getString("pluralResources")
					+ ":</b></td><td nowrap>" + resources + "</td></tr>";
		}

		String statusMessage = blist.getCurrentStatusString();
		if (statusMessage != null && !statusMessage.equals("")) {
			tooltip += "<tr><td nowrap><b>"
					+ this.resources.getString("currentStatusMessage")
					+ ":</b></td><td nowrap>" + statusMessage
					+ "</td></tr></table></td></tr></table></html>";
		}

		button.setToolTipText(tooltip);
	}

	/**
	 * Reloads the status icons (in case the theme changes, etc)
	 */
	public void reloadStatusIcons() {
		Iterator statusIterator = statuses.getContent().iterator();
		Presence.Mode mode;
		SelfStatus current;
		int i = 0;
		while (statusIterator.hasNext()) {
			current = (SelfStatus) statusIterator.next();
			mode = current.getMode();

			((JMenuItem) this.getComponent (i)) .setIcon(StatusIconCache.getStatusIcon(mode));
			if (blist != null && mode == blist.getCurrentPresenceMode()) {
				((JMenuItem) this.getComponent (i)) .setSelected(true);
				if (useIcon)
					button.setIcon(StatusIconCache.getStatusIcon(mode));
			} else {
				((JMenuItem) this.getComponent (i)) .setSelected(false);
			}

			i++;
		}
	}

	/**
	 * Sets the checked item to the mode represented
	 *
	 * @param mode
	 *            the mode to check
	 */
	public void setModeChecked(Presence.Mode mode) {
		Iterator statusIterator = statuses.getContent().iterator();
		int i = 0;
		while (statusIterator.hasNext()) {
			SelfStatus current = (SelfStatus) statusIterator.next();
			Presence.Mode m = current.getMode();

			if (m == mode) {
				((JMenuItem) this.getComponent (i)) .setSelected(true);

				if (useIcon)
					button.setIcon(StatusIconCache.getStatusIcon(mode));
			} else {
				((JMenuItem) this.getComponent (i)) .setSelected(false);
			}

			i++;
		}

        if(mode==null)button.setText( resources.getString("offline"));
   		else button.setText ( resources.getString ( mode.toString ()) );
        button.repaint();
		repaint();
	}

	/**
	 * Sets this menus icon
	 *
	 * @param mode
	 *            the mode that the icon represents
	 */
	public void setIcon(Presence.Mode mode) {
//        if (useIcon)
//            super.setIcon(StatusIconCache.getStatusIcon(mode));
	}

	/**
	 * Sets up the various event listeners in the menu
	 */
	private void setUpListeners() {
		MenuListener listener = new MenuListener();
		for (int i = 0; i < getComponentCount (); i++) {

			((JMenuItem) this.getComponent (i)) .addActionListener(listener);
		}
	}

	/**
	 * Unchecks all the items in this menu except the one currently being used
	 *
	 * @param item
	 *            Description of the Parameter
	 */
	private void uncheckAll(JCheckBoxMenuItem item) {
		JCheckBoxMenuItem curItem;

		com.valhalla.Logger.debug("Unckecking all but " + item);
		for (int i = 0; i < getComponentCount (); i++) {
			curItem = (JCheckBoxMenuItem) ((JMenuItem) this.getComponent (i)) ;
			if (curItem != item) {
				if (curItem.getState()) {
					curItem.setState(false);
				}
			} else {
				if (!curItem.getState()) {
					curItem.setState(true);
				}
			}
		}
	}

	/**
	 * Sets the current status
	 *
	 * @param item
	 *            which item was clicked
	 * @param mode
	 *            the mode to change to
	 * @param defaultMessage
	 *            the default message to pick
	 * @param getMessage
	 *            set to true if the user should specify a message
	 */
	private void setStatus(JCheckBoxMenuItem item, Presence.Mode mode,
			String defaultMessage, boolean getMessage) {
		blist.setStatus(mode, defaultMessage, getMessage);
	}

	protected void signOffHandler() {

		button.setText ( resources.getString ( "offline" ));
		setModeChecked(null);
		if (blinkTimerIsRunning())
			stopBlinkTimer();

		if (BuddyList.getInstance().checkConnection()) {
			ConnectorThread.getInstance().setCancelled(true);
			BuddyList.getInstance().signOff();
		}
	}

	/**
	 * Listens for items in the menu to be clicked
	 *
	 * @author Adam Olsen
	 * @created November 11, 2004
	 * @version 1.0
	 */
	class MenuListener implements ActionListener {
		/**
		 * Description of the Method
		 *
		 * @param e
		 *            Description of the Parameter
		 */
		public void actionPerformed(ActionEvent e) {
            if( ProfileManager.isCurrentlyShowing() )
            {
                setModeChecked(null);
                Standard.warningMessage( null, resources.getString("profileManager"), resources.getString("mustChooseProfile"));
                return;
            }


			JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
			SelfStatus status = statuses.getStatus(item.getText());

			if (status.getMode() == null) {
				ConnectorThread.getInstance().setCancelled(true);
				signOffHandler();
			} else {
				ConnectorThread.getInstance().setCancelled(false);
				setStatus(item, status.getMode(), status.getTitle(), true);
			}
		}
	}

	public void showMenu( Component tree, int x, int y)
	{
	   show( tree, x, y );
	}



}

