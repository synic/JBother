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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.packet.DelayInformation;

import com.valhalla.jbother.*;
import com.valhalla.jbother.groupchat.ChatRoomPanel;
import com.valhalla.jbother.groupchat.GroupChatBookmarks;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.MUCBuddyStatus;
import com.valhalla.jbother.jabber.ParsedBuddyInfo;
import com.valhalla.misc.GnuPG;
import com.valhalla.settings.Arguments;
import com.valhalla.settings.Settings;
import net.infonode.tabbedpanel.*;
import net.infonode.tabbedpanel.titledtab.*;
import net.infonode.util.*;

/**
 * Listens for a message packet, and sends it to the appropriate buddies
 * conversation window. If the window does not exist a new window will be
 * created
 *
 * @author Adam Olsen
 * @author Andrey Zakirov
 * @version 1.0
 */

public class MessagePacketListener implements PacketListener {
    private Hashtable buddyes; //all the available buddy statuses

    private Vector messageQueue = new Vector();

    private boolean started = false;

    private javax.swing.Timer timer = new javax.swing.Timer(3000,
            new QueueListener());

    private boolean verifiedFlag = false;

    private boolean decryptedFlag = false;

    private String gnupgSecretKey = Settings.getInstance().getProperty(
            "gnupgSecretKeyID");

    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    /**
     * Constructor for the packet listener
     */
    public MessagePacketListener() {
    }

    public void startTimer() {
        timer.start();
    }

    public void resetQueue() {
        started = false;
    }

    private class QueueListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            com.valhalla.Logger.debug("Emptying message queue - contains "
                    + messageQueue.size() + " items");

            started = true;
            timer.stop();

            while (messageQueue.size() > 0) {
                Packet packet = (Packet) messageQueue.firstElement();
                processPacket(packet);
                messageQueue.remove(packet);
            }

            if (!BuddyList.getInstance().checkConnection())
                return;
            if (Arguments.getInstance().getBoolean("nomucautojoin"))
                return;
            GroupChatBookmarks gcb = new GroupChatBookmarks(BuddyList
                    .getInstance());
            gcb.autoJoin();
        }
    }

    /**
     * Processes the message packet
     */
    public void processPacket(Packet message) {
        final Message packet = (Message) message;

        verifiedFlag = false;
        decryptedFlag = false;
        if (packet.getType() != Message.Type.CHAT
                && packet.getType() != Message.Type.HEADLINE
                && packet.getType() != Message.Type.NORMAL)
            return;

        if (!started) {
            com.valhalla.Logger.debug("Message received, adding to queue.");
            messageQueue.add(message);
            return;
        }

        if (packet.getType() == Message.Type.NORMAL) {
            if (packet.getBody() == null)
                return;
            PacketExtension ex = packet.getExtension("x",
                    "http://jabber.org/protocol/muc#user");
            if (ex != null)
                return;

            ex = packet.getExtension("x", "jabber:x:conference");
            if (ex != null)
                return;

            ParsedBuddyInfo info = new ParsedBuddyInfo(packet.getFrom());
            String userId = info.getUserId().toLowerCase();
            if (!Settings.getInstance().getBoolean("showAgentMessages")
                    && userId.indexOf("@") == -1)
                return;

            if (BuddyList.getInstance().getBlockedUsers().containsKey(userId)) {
                com.valhalla.Logger.debug("Blocking user: " + userId);
                return;
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MessagePanel window = new MessagePanel();
                    window.receiveMessage(packet);
                }
            });

            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                String from = packet.getFrom();

                //check to see if it's a private message
                if (BuddyList.getInstance().getTabFrame() != null
                        && BuddyList.getInstance().getTabFrame().isRoomOpen(
                                from.replaceAll("\\/.*", ""))) {
                    ChatRoomPanel window = BuddyList.getInstance()
                            .getTabFrame().getChatPanel(
                                    from.replaceAll("\\/.*", ""));
                    if (window != null)
                        initiatePMSession(window, packet);
                    return;
                }

                if (from != null) {
                    ParsedBuddyInfo info = new ParsedBuddyInfo(from);
                    String userId = info.getUserId().toLowerCase();
                    String resource = info.getResource();
                    String server = info.getServer();
                    from = info.getBareAddress();

                    BuddyStatus buddy = BuddyList.getInstance().getBuddyStatus(
                            userId);

                    if (BuddyList.getInstance().getBlockedUsers().containsKey(
                            userId)) {
                        com.valhalla.Logger.debug("Blocking user: " + userId);
                        return;
                    }

                    String messageSbj = packet.getSubject();
                    String messageBody = packet.getBody();
                    GnuPG gnupg = new GnuPG();
                    decryptedFlag = false;
                    verifiedFlag = false;
                    final SecureExtension xEncryptedExtension = (SecureExtension) packet
                            .getExtension("x", "jabber:x:encrypted");
                    if (xEncryptedExtension != null
                            && Settings.getInstance().getProperty(
                                    "gnupgSecretKeyID") != null) {
                        String decryptedMessageBody = gnupg
                                .decryptExtension(xEncryptedExtension.getData());
                        if (decryptedMessageBody != null) {
                            messageBody = decryptedMessageBody;
                            decryptedFlag = true;
                        } else {
                            messageBody = "[ "
                                    + resources
                                            .getString("gnupgErrorDecrypting")
                                    + ". "
                                    + resources.getString("reason")
                                    + ":\n "
                                    + gnupg.getErrorString().replaceAll("\n",
                                            " ") + " ]";
                        }
                    }

                    final SecureExtension xSignedExtension = (SecureExtension) packet
                            .getExtension("x", "jabber:x:signed");
                    if (xSignedExtension != null) {
                        String verifiedMessageId = gnupg.verifyExtension(
                                xSignedExtension.getData(), messageBody);

                        if (verifiedMessageId != null) {
                            verifiedMessageId = verifiedMessageId.replaceAll(
                                    "\n$", "");
                        }
                        if ((verifiedMessageId != null)
                                && (buddy.getPubKey() != null)
                                && (buddy.getPubKey()
                                        .endsWith(verifiedMessageId))) {
                            verifiedFlag = true;
                        }
                    }

                    if (!BuddyList.getInstance().checkConnection())
                        return;

                    // we don't want null messages to be displayed.
                    if ((messageBody == null))
                        return;

                    if (!Settings.getInstance().getBoolean("showAgentMessages")
                            && userId.indexOf("@") == -1)
                        return;

                    RosterEntry entry = ConnectorThread.getInstance()
                            .getRoster().getEntry(from);
                    if (entry != null)
                        userId = entry.getName();

                    if (buddy.getName() != null)
                        userId = buddy.getName();

                    if (buddy.getConversation() == null) {
                        if (packet.getType() == Message.Type.HEADLINE) {
                            buddy.setConversation(new HeadlinesPanel(buddy));
                        } else {
                            buddy.setConversation(new ChatPanel(buddy));
                        }
                    }

                    Date date = null;
                    String append = "";
                    DelayInformation inf = (DelayInformation) packet
                            .getExtension("x", "jabber:x:delay");
                    if (inf != null
                            && buddy.getConversation() instanceof ChatPanel) {

                            append = " (" + resources.getString( "offline" ).toLowerCase();
                            if( inf.getReason() != null && !inf.getReason().equals( "" )) append += ": " + inf.getReason();

                            append += ")";

                         if (inf.getStamp() != null)
                            date = inf.getStamp();
                    }

                    buddy.getConversation().receiveMessage(messageSbj,
                            append, messageBody, resource, date, decryptedFlag,
                            verifiedFlag);
                    if (buddy.getConversation() instanceof ChatPanel) {
                        ((ChatPanel) buddy.getConversation())
                                .setLastReceivedMessage(packet);
                    }

                    /*if (Settings.getInstance().getBoolean("useTabbedWindow")
                            && BuddyList.getInstance().getTabFrame() != null) {
                        TabbedPanel pane = BuddyList.getInstance()
                                .getTabFrame().getTabPane();
                        final TabFramePanel tempPanel = (TabFramePanel) pane
                                .getSelectedComponent();
                        if (tempPanel == null)
                            return;
                    }*/
                }
            }
        });
    }

    /**
     * If it's a group chat packet listener, process the packet
     *
     * @param window
     *            the chat room window
     * @param packet
     *            the message
     */
    private void initiatePMSession(final ChatRoomPanel window,
            final Message packet) {
        final MUCBuddyStatus buddy = window.getBuddyStatus(packet.getFrom());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                if (buddy.getConversation() == null) {
                    ChatPanel conver = new ChatPanel(buddy);
                    conver.setVisible(true);
                    buddy.setConversation(conver);
                }

                String messageBody = packet.getBody();
                if (messageBody == null)
                    return;

                Date date = new Date();

                DelayInformation inf = (DelayInformation) packet.getExtension(
                        "x", "jabber:x:delay");
                if (inf != null && inf.getStamp() != null)
                    date = inf.getStamp();

                buddy.getConversation().receiveMessage("", "", messageBody, null,
                        date, false, false);
            }
        });
    }
}