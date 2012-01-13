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

import javax.swing.SwingUtilities;

import org.jivesoftware.smackx.muc.SubjectUpdatedListener;

/**
 * Listens for subject changes in a room
 * 
 * @author Adam Olsen
 * @version 1.0
 */
public class SubjectListener implements SubjectUpdatedListener {
    private ChatRoomPanel window;

    /**
     * Sets up the group chat subject listener
     * 
     * @param window
     *            the window that this litener belongs to
     */
    public SubjectListener(ChatRoomPanel window) {
        this.window = window;
    }

    /**
     * Processes the packet
     */
    public void subjectUpdated(final String subject, final String from) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                window.setSubject(subject);
                window.serverNoticeMessage("Subject is \"" + subject + "\"");

                window.serverNoticeMessage("Subject set by "
                        + window.getNickname(from));
            }
        });
    }
}