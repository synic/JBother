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
package com.valhalla.jbother;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;

import org.jivesoftware.smack.packet.Presence;

/**
 * Sets the user to away after a specific amount of idle time
 *
 * @author Adam Olsen
 * @created October 26, 2004
 * @version 1.0
 */
class AwayHandler implements ActionListener {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    /**
     * Called by the enclosing event listener
     *
     * @param e
     *            the event
     */
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                BuddyList.getInstance().setIdleAway(true);
                BuddyList.getInstance().setStatus(Presence.Mode.AWAY,
                        resources.getString("autoAway"), false);
                BuddyList.getInstance().getAwayTimer().stop();
                BuddyList.getInstance().getStatusMenu().loadSelfStatuses();

                NotificationPopup.showSingleton(BuddyList.getInstance().getContainerFrame(), resources.getString("autoAway"), resources.getString("setToAway"), null);

            }
        } );
    }
}