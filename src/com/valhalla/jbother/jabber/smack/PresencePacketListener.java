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
package com.valhalla.jbother.jabber.smack;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import com.valhalla.gui.NMOptionDialog;
import com.valhalla.gui.NMOptionListener;
import com.valhalla.jbother.*;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.ParsedBuddyInfo;
import com.valhalla.settings.Settings;

/**
 *  This class listens to all presence packets, and delivers them according to
 *  their type, etc...
 *
 *@author     Adam Olsen
 *@created    June 10, 2005
 *@version    1.0
 */

public class PresencePacketListener implements PacketListener {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private Properties currentlyRequesting = new Properties();


    /**
     *  Called when a Presence packet is received
     *
     *@param  packet  Description of the Parameter
     */
    public void processPacket(Packet packet) {
        if (!BuddyList.getInstance().checkConnection()) {
            return;
        }

        final Presence presence = (Presence) packet;
        String from = presence.getFrom();

        ParsedBuddyInfo info = new ParsedBuddyInfo(from);
        final String userId = info.getUserId();
        final String resource = info.getResource();
        final String server = info.getServer();

        // if the user is blocked, ignore this packet
        if (BuddyList.getInstance().getBlockedUsers().containsKey(userId)) {
            return;
        }

        // if this is a groupchat packet, then we don't care about it in this
        // class
        if (BuddyList.getInstance().getTabFrame() != null
                 && (BuddyList.getInstance().getTabFrame().getChatPanel(userId) != null || BuddyList.getInstance().getTabFrame().getChatPanel(from) != null)) {
            return;
        }

        // if they are trying to subscribe, as if we want to let them subscribe
        if (presence.getType() == Presence.Type.SUBSCRIBE) {
            requestSubscription(userId);
            return;
        }

        // beyond this point, we don't care about anything but online and
        // offline packets
        if (presence.getType() != Presence.Type.AVAILABLE
                 && presence.getType() != Presence.Type.UNAVAILABLE) {
            return;
        }

        final BuddyStatus buddy = BuddyList.getInstance()
                .getBuddyStatus(userId);

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    boolean isSelf = false;
                    if (buddy.getUser().equals(
                            BuddyList.getInstance().getConnection().getUser()
                            .replaceAll("/.*", ""))) {
                        isSelf = true;
                    }

                    // if it's unavailable, check to see if they have any resources
                    // still online
                    // if they do, set the packet to available, and minus one
                    // resource
                    if (presence.getType() == Presence.Type.UNAVAILABLE) {
                        buddy.removeResource(resource);
                        if (buddy.size() > 0) {
                            presence.setType(Presence.Type.AVAILABLE);
                        }
                    } else {
                        int priority = presence.getPriority();
                        if (priority < 0) {
                            priority = 0;
                        }

                        buddy.addResource(resource, priority, presence.getMode(),
                                presence.getStatus());
                    }

                    performPresenceTasks(buddy, presence.getType());

                    if (!BuddyList.getInstance().checkConnection()) {
                        return;
                    }

                    if (isSelf) {
                        BuddyList.getInstance().getStatusMenu()
                                .loadSelfStatuses();
                    }

                    BuddyList.getInstance().getBuddyListTree().removeBuddy(buddy,
                            buddy.getGroup(), false);

                    Roster roster = ConnectorThread.getInstance().getRoster();
                    if (roster == null) {
                        return;
                    }
                    if (!isSelf
                             && (roster.getEntry(userId) != null || roster.getEntry(userId + "/" + resource) != null)) {
                        if (presence.getType() == Presence.Type.AVAILABLE
                                 || Settings.getInstance().getBoolean(
                                "showOfflineBuddies")) {
                            BuddyList.getInstance().getBuddyListTree().addBuddy(
                                    buddy);
                        }
                    }

                    ConversationPanel conv = buddy.getConversation();
                    if ((conv != null) && (conv instanceof ChatPanel)) {
                        ((ChatPanel) conv).updateResources();
                    }
                }
            });
    }


    /**
     *  Performs presence tasks, like playing sounds when a buddy signs on or
     *  off or displaying that they have done so in their conversation window if
     *  it exists
     *
     *@param  buddy  the buddy to process
     *@param  type   the presence type
     */
    public void performPresenceTasks(final BuddyStatus buddy,
            final Presence.Type type) {
        ConversationPanel conv = buddy.getConversation();

        if (type == Presence.Type.AVAILABLE && !buddy.getHasSignedOn()) {
            buddy.setHasSignedOn(true);
            if ((conv != null) && (conv instanceof ChatPanel)) {
                ((ChatPanel) conv).signedOn();
            }
            com.valhalla.jbother.sound.SoundPlayer.play("signedOnSound");

            if (Settings.getInstance().getBoolean("usePopup") &&
                    Settings.getInstance().getBoolean("popupForSignOn")) {
                NotificationPopup.showSingleton(BuddyList.getInstance().getContainerFrame(), resources.getString("buddySignedOn"), buddy.getName(), null);
            }
        }

        if (type == Presence.Type.UNAVAILABLE && buddy.size() <= 0
                 && buddy.getHasSignedOn()) {
            if ((conv != null) && (conv instanceof ChatPanel)) {
                ((ChatPanel) conv).signedOff();
            }
            buddy.setHasSignedOn(false);
            com.valhalla.jbother.sound.SoundPlayer.play("signedOffSound");

            if (Settings.getInstance().getBoolean("usePopup") &&
                    Settings.getInstance().getBoolean("popupForSignOff")) {
                NotificationPopup.showSingleton(BuddyList.getInstance().getContainerFrame(), resources.getString("buddySignedOff"), buddy.getName(), null);
            }
        }
    }


    /**
     *  If someone sends a subscription request packet, the user will be asked
     *  if they want to accept the request
     *
     *@param  userId  the requesting user
     */
    public void requestSubscription(final String userId) {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    if (currentlyRequesting.getProperty(userId.toLowerCase()) != null) {
                        return;
                    }
                    currentlyRequesting.setProperty(userId.toLowerCase(), "true");

                    NMOptionDialog d = new NMOptionDialog((JFrame) null, resources.getString("subscriptionRequest"), MessageFormat.format(resources.getString("requestedPresence"),
                            new Object[]{userId}),
                            NMOptionDialog.QUESTION);

                    d.addButton("Yes", 1);
                    d.addButton("No", 2);
                    d.setVisible(true);

                    d.addOptionListener(
                        new NMOptionListener() {
                            public void buttonClicked(int num) {
                                currentlyRequesting.remove(userId);
                                if (num == 1) {
                                    Presence packet = new Presence(
                                            Presence.Type.SUBSCRIBED);
                                    packet.setTo(userId);

                                    BuddyList.getInstance().getConnection().sendPacket(
                                            packet);
                                    BuddyStatus buddy = BuddyList.getInstance()
                                            .getBuddyStatus(userId);
                                    buddy.setRemoved(false);
                                }

                                boolean add = true;
                                // find out if they are already in the roster
                                if (ConnectorThread.getInstance().getRoster()
                                        .contains(userId.toLowerCase())) {
                                    add = false;
                                }
                                if (userId.indexOf("@") < 0) {
                                    add = false;
                                }

                                if (add) {
                                    NMOptionDialog di = new NMOptionDialog(
                                            (JFrame) null, resources.getString("addButton"),
                                            MessageFormat.format(resources.getString("doAddBuddy"),
                                            new Object[]{userId}),
                                            NMOptionDialog.QUESTION);

                                    di.addButton("Yes", 1);
                                    di.addButton("No", 2);
                                    di.setVisible(true);

                                    di.addOptionListener(
                                        new NMOptionListener() {
                                            public void buttonClicked(int num) {
                                                if (num == 1) {
                                                    AddBuddyDialog dialog = new AddBuddyDialog();
                                                    dialog.setBuddyId(userId);
                                                    dialog.setVisible(true);
                                                }
                                            }
                                        });
                                }
                            }

                        });
                }
            });
    }
}
