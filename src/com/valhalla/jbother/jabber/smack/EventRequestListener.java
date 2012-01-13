/*
 * Copyright (C) 2003 Adam Olsen
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 1, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

package com.valhalla.jbother.jabber.smack;

import org.jivesoftware.smackx.DefaultMessageEventRequestListener;
import org.jivesoftware.smackx.MessageEventManager;

import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.ConnectorThread;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.ParsedBuddyInfo;

public class EventRequestListener extends DefaultMessageEventRequestListener {
    public void composingNotificationRequested(String from, String packetID,
            MessageEventManager messageEventManager) {
        super.composingNotificationRequested(from, packetID,
                messageEventManager);
        final ParsedBuddyInfo info = new ParsedBuddyInfo(from);
        final String userId = info.getUserId().toLowerCase();
        final BuddyStatus buddy = BuddyList.getInstance()
                .getBuddyStatus(userId);
        buddy.setComposingID(packetID);
    }

    public void deliveredNotificationRequested(String from, String packetID,
            MessageEventManager messageEventManager) {
        super.deliveredNotificationRequested(from, packetID,
                messageEventManager);
        ConnectorThread.getInstance().getMessageEventManager().sendDeliveredNotification(from, packetID);
    }

    public void displayedNotificationRequested(String from, String packetID,
            MessageEventManager messageEventManager) {
        super.displayedNotificationRequested(from, packetID,
                messageEventManager);
        final ParsedBuddyInfo info = new ParsedBuddyInfo(from);
        final String userId = info.getUserId().toLowerCase();
        final BuddyStatus buddy = BuddyList.getInstance()
                .getBuddyStatus(userId);
          buddy.addNotDisplayedID(packetID, buddy.getUser ());
    }
}




