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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.MUCUser;

import com.valhalla.jbother.JBotherLoader;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.MUCBuddyStatus;
import com.valhalla.jbother.menus.BuddyListPopupMenu;

/**
 * The menu that pops up if someone right clicks on a user in a groupchat
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class NickListPopupMenu extends BuddyListPopupMenu {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private ChatRoomPanel panel;

    private JMenuItem grantAdmin = new JMenuItem(resources
            .getString("grantAdmin")), grantMembership = new JMenuItem(
            resources.getString("grantMembership")),
            grantModerator = new JMenuItem(resources
                    .getString("grantModerator")),
            grantOwnership = new JMenuItem(resources
                    .getString("grantOwnership")), grantVoice = new JMenuItem(
                    resources.getString("grantVoice")),

            revokeAdmin = new JMenuItem(resources.getString("revokeAdmin")),
            revokeMembership = new JMenuItem(resources
                    .getString("revokeMembership")),
            revokeModerator = new JMenuItem(resources
                    .getString("revokeModerator")),
            revokeOwnership = new JMenuItem(resources
                    .getString("revokeOwnership")),
            revokeVoice = new JMenuItem(resources.getString("revokeVoice")),

            kick = new JMenuItem(resources.getString("kickParticipant")),
            ban = new JMenuItem(resources.getString("banParticipant"));

    private JMenu adminMenu = new JMenu(resources.getString("administration"));

    /**
     * Sets up the popup menu
     */
    public NickListPopupMenu(ChatRoomPanel panel) {
        removeAll();
        MenuActionListener listener = new MenuActionListener();
        this.panel = panel;

        grantAdmin.addActionListener(listener);
        grantMembership.addActionListener(listener);
        grantModerator.addActionListener(listener);
        grantOwnership.addActionListener(listener);
        grantVoice.addActionListener(listener);

        revokeAdmin.addActionListener(listener);
        revokeMembership.addActionListener(listener);
        revokeModerator.addActionListener(listener);
        revokeOwnership.addActionListener(listener);
        revokeVoice.addActionListener(listener);

        kick.addActionListener(listener);
        ban.addActionListener(listener);

        add(chatItem);
        add(messageItem);
        add(infoItem);
        add(logItem);
        addSeparator();
        add(adminMenu);
        if (JBotherLoader.isGPGEnabled())
            addSeparator();

        grantAdmin.setActionCommand("grantAdmin");
        grantMembership.setActionCommand("grantMembership");
        grantModerator.setActionCommand("grantModerator");
        grantOwnership.setActionCommand("grantOwnership");
        grantVoice.setActionCommand("grantVoice");

        revokeAdmin.setActionCommand("revokeAdmin");
        revokeMembership.setActionCommand("revokeMembership");
        revokeModerator.setActionCommand("revokeModerator");
        revokeOwnership.setActionCommand("revokeOwnership");
        revokeVoice.setActionCommand("revokeVoice");

        adminMenu.add(grantAdmin);
        adminMenu.add(grantMembership);
        adminMenu.add(grantModerator);
        adminMenu.add(grantOwnership);
        adminMenu.add(grantVoice);
        adminMenu.addSeparator();

        adminMenu.add(revokeAdmin);
        adminMenu.add(revokeMembership);
        adminMenu.add(revokeModerator);
        adminMenu.add(revokeOwnership);
        adminMenu.add(revokeVoice);

        adminMenu.addSeparator();

        adminMenu.add(kick);
        adminMenu.add(ban);
    }

    protected String getFrom() {
        return panel.getUser();
    }

    /**
     * Listens for a double mouse click, or a right click
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class MenuActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == kick) {
                String result = (String) JOptionPane.showInputDialog(null,
                        resources.getString("enterReason"), resources
                                .getString("kickParticipant"),
                        JOptionPane.QUESTION_MESSAGE, null, null, "No reason specified");

                if (result == null)
                    return;
                Thread thread = new Thread(new KickThread(result));
                thread.start();
            } else if (e.getSource() == ban) {
                String result = (String) JOptionPane.showInputDialog(null,
                        resources.getString("enterReason"), resources
                                .getString("banParticipant"),
                        JOptionPane.QUESTION_MESSAGE, null, null, "No reason specified");

                if (result == null)
                    return;
                Thread thread = new Thread(new BanThread(result));
                thread.start();
            }

            // MUC stuff
            else {
                JMenuItem item = (JMenuItem) e.getSource();
                panel.doAction(item.getActionCommand(), (MUCBuddyStatus) buddy);
            }
        }
    }

    class KickThread implements Runnable {
        String reason;

        public KickThread(String reason) {
            this.reason = reason;
        }

        public void run() {
            com.valhalla.Logger.debug("kicking " + buddy.getUser());

            try {
                panel.getChat().kickParticipant(buddy.getName(), reason);
            } catch (final XMPPException ex) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String message = "";
                        if (ex.getXMPPError() != null) {
                            message = resources.getString("xmppError"
                                    + ex.getXMPPError().getCode());
                        } else
                            message = ex.getMessage();
                        panel.serverNoticeMessage(message);
                    }
                });
            }
        }
    }

    class BanThread implements Runnable {
        String reason;

        public BanThread(String reason) {
            this.reason = reason;
        }

        public void run() {
            MUCUser user = ((MUCBuddyStatus) buddy).getMUCUser();
            if (user == null)
                return;

            MUCUser.Item item = user.getItem();
            if (item == null)
                return;

            String jid = item.getJid();
            if (jid == null)
                return;

            try {
                panel.getChat().banUser(jid, reason);
            } catch (final XMPPException ex) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String message = "";
                        if (ex.getXMPPError() != null) {
                            message = resources.getString("xmppError"
                                    + ex.getXMPPError().getCode());
                        } else
                            message = ex.getMessage();
                        panel.serverNoticeMessage(message);
                    }
                });
            }
        }
    }

    /**
     * Displays the popup menu
     *
     * @param comp
     *            the component to pop the menu up on
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate of the menu
     * @param buddy
     *            the BuddyStatus that was clicked on
     */
    public void showMenu(Component comp, int x, int y, BuddyStatus buddy) {
        this.buddy = buddy;

        if (JBotherLoader.isGPGEnabled()) {
            remove(unbindPubKeyItem);
            remove(bindPubKeyItem);

            if (buddy.getPubKey() != null
                    && !(this instanceof NickListPopupMenu)) {
                add(unbindPubKeyItem);
            } else {
                add(bindPubKeyItem);
            }
        }

        validate();
        show(comp, x, y);
    }
}