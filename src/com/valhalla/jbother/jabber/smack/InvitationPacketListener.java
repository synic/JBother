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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.valhalla.gui.NMOptionDialog;
import com.valhalla.gui.NMOptionListener;
import com.valhalla.jbother.groupchat.GroupChatBookmarks;

/**
 * @author Adam Olsen
 * @version 1.0
 */

public class InvitationPacketListener implements InvitationListener {
    private ResourceBundle resources = ResourceBundle
            .getBundle("JBotherBundle");

    public void invitationReceived(final XMPPConnection connection,
            final String room, final String inviter, final String reason,
            final String password) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String p = password;
                if (p == null || p.equals(""))
                    p = "none";

                String message = MessageFormat.format(resources
                        .getString("invitation"), new Object[] { room, inviter,
                        reason, p });
                NMOptionDialog dialog = new NMOptionDialog((JFrame) null,
                        resources.getString("invitationReceived"), message);

                dialog.addButton("Yes", 1);
                dialog.addButton("No", 2);
                dialog.addOptionListener(new NMOptionListener() {
                    public void buttonClicked(int num) {
                        inviteHandler(num, connection, room, inviter, reason,
                                password);
                    }
                });

                dialog.setVisible(true);
            }
        });
    }

    public void invitationReceived(XMPPConnection con, String room,
            String inviter, String reason, String password, Message message) {
        invitationReceived(con, room, inviter, reason, password);
    }

    private void inviteHandler(int num, final XMPPConnection connection,
            final String room, final String inviter, final String reason,
            final String password) {
        if (num == 2) {
            String result = (String) JOptionPane.showInputDialog(null,
                    resources.getString("reasonDecline"), resources
                            .getString("invitationReceived"),
                    JOptionPane.QUESTION_MESSAGE, null, null, "Not interested");
            if (result == null || result.equals("")) {
                invitationReceived(connection, room, inviter, reason, password);
                return;
            }

            MultiUserChat.decline(connection, room, inviter, result);
        } else {
            GroupChatBookmarks.showDialog(room, connection.getUser(), password);
        }
    }
}