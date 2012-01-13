/*
 Copyright (C) 2003 Adam Olsen

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 1, or (at your option)
 any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.valhalla.jbother.jabber.smack;

import java.util.Locale;
import java.util.ResourceBundle;

import org.jivesoftware.smack.packet.Presence;

import com.valhalla.gui.Standard;
import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.ConnectorThread;
import com.valhalla.settings.Settings;

/**
 * Listens to the connection, watches for drops, etc
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class ConnectionListener implements
        org.jivesoftware.smack.ConnectionListener {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private Presence.Mode lastMode = null;

    private String lastStatus = null;

    /**
     * Called if the connection is lost
     *
     * @param e
     *            an Exception containing the reason for the connection loss
     */
    public void connectionClosedOnError(Exception e) {
        if (BuddyList.getInstance().getConnection() == null) {
            com.valhalla.Logger
                    .debug("Connection closed error received, but the connection was null!");
            return;
        }

        lastMode = BuddyList.getInstance().getCurrentPresenceMode();
        lastStatus = BuddyList.getInstance().getCurrentStatusString();

        com.valhalla.jbother.ConnectorThread.getInstance().setHasHadError(true);
        String errorMessage = resources.getString("connectionLost");

        if (e.getMessage() != null)
            errorMessage = e.getMessage();
        com.valhalla.Logger.debug(errorMessage);

        if (!Settings.getInstance().getBoolean("reconnectOnDisconnect"))
            Standard.warningMessage(null, resources
                    .getString("connectionError"), errorMessage);

        BuddyList.getInstance().setSignoff(true);
        connectionClosed();
    }

    /**
     * Called if the connection is closed
     */
    public void connectionClosed() {
        BuddyList.getInstance().saveSettings();
        BuddyList.getInstance().getBuddiesMenu().signOff();
        BuddyList.getInstance().getStatusMenu().setModeChecked(null);
        if (BuddyList.getInstance().getSignoff()) {
            BuddyList.getInstance().signOff();

            if (Settings.getInstance().getBoolean("reconnectOnDisconnect")) {
                boolean away = BuddyList.getInstance().getIdleAway();

                if( ConnectorThread.getInstance().isAlive() )
                {
                    com.valhalla.Logger.debug( "Connector thread is already running." );
                    return;
                }

                ConnectorThread.getInstance().init( lastMode, lastStatus, away );
                ConnectorThread.getInstance().setPersistent(true);
                ConnectorThread.getInstance().start();
            }
        }
    }
}