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

import org.jivesoftware.smackx.muc.ParticipantStatusListener;

/**
 * Listens for subject changes in a room
 * 
 * @author Adam Olsen
 * @version 1.0
 */
public class StatusListener implements ParticipantStatusListener {
    private ChatRoomPanel window;

    /**
     * Sets up the group chat subject listener
     * 
     * @param window
     *            the window that this litener belongs to
     */
    public StatusListener(ChatRoomPanel window) {
        this.window = window;
    }

    public void adminGranted(String user) {
        window.serverNoticeMessage("admin granted to "
                + window.getNickname(user));
	
	window.getGroupChatNickList().removeBuddy(user);
	window.getBuddyStatus(user).setAffiliation("admin");
    }

    public void adminRevoked(String user) {
        window.serverNoticeMessage("admin taken from "
                + window.getNickname(user));
	window.getGroupChatNickList().removeBuddy(user);
        window.getBuddyStatus(user).setAffiliation("none");
    }

    public void left(String user) {
    }

    public void joined(String user) {
    }

    public void banned(String user, String actor, String reason) {
	window.getGroupChatNickList().removeBuddy(user);
        window.serverNoticeMessage(window.getNickname(user) + " was banned ("+actor+": " + reason +")");
    }

    public void kicked(String user, String actor, String reason) {
	window.getGroupChatNickList().removeBuddy(user);
        window.serverNoticeMessage(window.getNickname(user) + " was kicked ("+actor+": " + reason +")");
    }

    public void membershipGranted(String user) {
        window.serverNoticeMessage("membership granted to "
                + window.getNickname(user));
	window.getGroupChatNickList().removeBuddy(user);
        window.getBuddyStatus(user).setAffiliation("member");
    }

    public void membershipRevoked(String user) {
        window.serverNoticeMessage("membership taken from "
                + window.getNickname(user));
	window.getGroupChatNickList().removeBuddy(user);
        window.getBuddyStatus(user).setAffiliation("none");
    }

    public void moderatorGranted(String user) {
        window.serverNoticeMessage("moderator granted to "
                + window.getNickname(user));
	window.getGroupChatNickList().removeBuddy(user);
        window.getBuddyStatus(user).setRole("moderator");
    }

    public void moderatorRevoked(String user) {
        window.serverNoticeMessage("moderator revoked from "
                + window.getNickname(user));
	window.getGroupChatNickList().removeBuddy(user);
        window.getBuddyStatus(user).setRole("none");
    }

    public void nicknameChanged(String user, String dummy) {
        window.getParticipantListener().nickChange(window.getBuddyStatus(window.getRoomName() + "/" + user));
        window.getGroupChatNickList().redraw();
    }

    public void ownershipGranted(String user) {
        window.serverNoticeMessage("ownership granted to "
                + window.getNickname(user));
	window.getGroupChatNickList().removeBuddy(user);
        window.getBuddyStatus(user).setAffiliation("owner");
    }

    public void ownershipRevoked(String user) {
        window.serverNoticeMessage("ownership taken from "
                + window.getNickname(user));
	window.getGroupChatNickList().removeBuddy(user);
        window.getBuddyStatus(user).setAffiliation("none");
    }

    public void voiceGranted(String user) {
        window.serverNoticeMessage("voice granted to "
                + window.getNickname(user));
	window.getGroupChatNickList().removeBuddy(user);
        window.getBuddyStatus(user).setRole("participant");
    }

    public void voiceRevoked(String user) {
        window.serverNoticeMessage("voice taken from "
                + window.getNickname(user));
	window.getGroupChatNickList().removeBuddy(user);
        window.getBuddyStatus(user).setRole("visitor");

    }
}