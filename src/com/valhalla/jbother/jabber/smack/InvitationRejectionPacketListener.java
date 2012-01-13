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

import javax.swing.SwingUtilities;

import org.jivesoftware.smackx.muc.InvitationRejectionListener;

import com.valhalla.gui.Standard;

/**
 * @author Adam Olsen
 * @version 1.0
 */

public class InvitationRejectionPacketListener implements
        InvitationRejectionListener {
    private ResourceBundle resources = ResourceBundle
            .getBundle("JBotherBundle");

    public void invitationDeclined(final String invitee, final String reason) {
        if(invitee == null) return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String message = MessageFormat.format(resources
                        .getString("invitationDeclined"), new Object[] {
                        invitee, reason });
                Standard.warningMessage(null, resources
                        .getString("invitationRejected"), message);
            }
        });
    }
}