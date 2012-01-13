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

package com.valhalla.jbother.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.jivesoftware.smack.packet.Presence;

import com.valhalla.jbother.BuddyList;

public class BuddyListTransportMenu extends BuddyListPopupMenu {
    private JMenuItem signOnItem = new JMenuItem(resources.getString("signOn"));

    private JMenuItem signOffItem = new JMenuItem(resources
            .getString("signOff"));

    public BuddyListTransportMenu() {
        //insert(new JSeparator(), 6);
        insert(signOnItem, 6);
        insert(signOffItem, 7);

        TransportListener l = new TransportListener();
        signOnItem.addActionListener(l);
        signOffItem.addActionListener(l);
    }

    class TransportListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == signOnItem) {
                Presence p = new Presence(Presence.Type.AVAILABLE);
                p.setTo(buddy.getUser());
                BuddyList.getInstance().getConnection().sendPacket(p);
            } else if (e.getSource() == signOffItem) {
                Presence p = new Presence(Presence.Type.UNAVAILABLE);
                p.setTo(buddy.getUser());
                BuddyList.getInstance().getConnection().sendPacket(p);
            }
        }
    }
}