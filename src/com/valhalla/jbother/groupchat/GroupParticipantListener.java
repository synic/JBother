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
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.MUCUser;

import com.valhalla.jbother.ChatPanel;
import com.valhalla.jbother.ConversationPanel;
import com.valhalla.jbother.jabber.MUCBuddyStatus;
import com.valhalla.jbother.plugins.events.MUCEvent;
import com.valhalla.pluginmanager.PluginChain;

/**
 * Listens for presence packets when you are in a groupchat, and will update the
 * nicklist in a groupchat room. Also, if you are in a private conversation with
 * someone and they sign off, it will let you know.
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class GroupParticipantListener implements PacketListener {
    private ChatRoomPanel window;

    private MUCBuddyStatus nickChange = null;

    /**
     * sets up the packet listener
     */
    public GroupParticipantListener(ChatRoomPanel window) {
        this.window = window;
    }

    public void nickChange(MUCBuddyStatus user) {
        nickChange = user;
    }

    /**
     * Processes incoming presence packets (from group chats)
     */
    public void processPacket(Packet packet) {
        final Presence presence = (Presence) packet;
        final String from = packet.getFrom();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                boolean redraw = false;
                //right now we find out if we have already received a packet
                // from them,
                //and if not, we set up an information "account" in the system
                MUCBuddyStatus buddy = window.getBuddyStatus(from);

                window.removeBuddy(buddy.getUser());

                // get the role and affiliation information
                MUCUser user = (MUCUser) presence.getExtension("x",
                        "http://jabber.org/protocol/muc#user");
                if (user != null) {
                    buddy.setMUCUser(user);
                    MUCUser.Item item = user.getItem();
                    if (item != null) {
                        if (item.getAffiliation() != null)
                            buddy.setAffiliation(item.getAffiliation());
                        if (item.getRole() != null)
                            buddy.setRole(item.getRole());

                        if(item.getAffiliation()!=null && item.getRole()!=null)
                        {
                            if(item.getAffiliation().equals("none") && item.getRole().equals("none"))
                            {
                                presence.setType(Presence.Type.UNAVAILABLE);
                            }
                        }
                    }
                }

                // update the relavent presence information
                if (presence.getType() == Presence.Type.UNAVAILABLE ) {
                    ConversationPanel conv = buddy.getConversation();
                    if ((conv != null) && (conv instanceof ChatPanel))
                        ((ChatPanel) conv).signedOff();

                } else {
                    buddy.addResource("_no resource_", 5, presence.getMode(),
                            presence.getStatus());
                }

                //if we need to, reload the nicklist.
                if (presence.getType() == Presence.Type.AVAILABLE) {
                    window.addBuddy(buddy.getUser());
                    String name = buddy.getName();
                    if (name == null)
                        return;
                    if (nickChange == null && !buddy.getIsInRoom()) {

                        String message = buddy.getName();
                        if( buddy.getJid() != null )
                        {
                            message += " (" + buddy.getJid() + ") ";
                        }

                        message += " has entered the room";

                        window.serverNoticeMessage(message);
                        PluginChain.fireEvent(new MUCEvent(buddy.getUser(),
                                MUCEvent.EVENT_PARTICIPANT_JOINED, "",
                                new Date()));
                    } else if (nickChange != null) {
                        nickChange.setIsInRoom(false);
                        window.serverNoticeMessage(nickChange.getName()
                                + " is now known as " + buddy.getName());
                        window.removeBuddy(nickChange.getUser());
                    }

                    buddy.setIsInRoom(true);

                    nickChange = null;
                } else if (presence.getType() == Presence.Type.UNAVAILABLE) {
                    buddy.setIsInRoom(false);
                    window.getBuddyStatuses().remove(buddy);
                    String leaveMessage = buddy.getName()
                            + " has left the room";
                    PluginChain.fireEvent(new MUCEvent(buddy.getUser(),
                            MUCEvent.EVENT_PARTICIPANT_PARTED, "", new Date()));

                    if (presence.getStatus() != null
                            && !presence.getStatus().equals("")) {
                        leaveMessage += ": " + presence.getStatus();
                    }

                    if (nickChange == null)
                        window.serverNoticeMessage(leaveMessage);
                    else
                        nickChange = buddy;
                }

                window.getNickList().repaint();
            }
        });
    }
}
