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

package com.valhalla.jbother.groupchat;

import java.util.Date;

import javax.swing.SwingUtilities;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.DelayInformation;

import com.valhalla.jbother.BuddyList;

/**
 * Listens for Group Chat Messages
 * 
 * @author Adam Olsen
 * @version 1.0
 */
public class GroupChatMessagePacketListener implements PacketListener {
    private ChatRoomPanel window;

    /**
     * Sets up the group chat message listener
     * 
     * @param window
     *            the window that this litener belongs to
     */
    public GroupChatMessagePacketListener(ChatRoomPanel window) {
        this.window = window;
    }

    /**
     * Processes the packet
     */
    public void processPacket(Packet message) {
        final Message packet = (Message) message;

        final String from = packet.getFrom();
        final String to = packet.getTo();
        final String longUser = BuddyList.getInstance().getConnection()
                .getUser();
        final String shortUser = longUser.substring(0, longUser.indexOf("/"));

        if (packet.getType() != Message.Type.GROUP_CHAT)
            return;

        if (from != null) {
            if (!(to.equals(longUser) || to.equals(shortUser)))
                return;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Date date = null;

                    DelayInformation inf = (DelayInformation) packet
                            .getExtension("x", "jabber:x:delay");
                    if (inf != null && inf.getStamp() != null)
                        date = inf.getStamp();

                    final String messageBody = packet.getBody();

                    String tempString = from;
                    int index = from.indexOf("/");
                    if (index > -1)
                        tempString = from.substring(index + 1);
                    else
                        tempString = from;
                    if (messageBody != null)
                    {
                        window.receiveMessage(tempString, messageBody, date);
                    }
                }
            });
        }
    }
}