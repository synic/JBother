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

import com.valhalla.jbother.jabber.MUCBuddyStatus;

/**
 * Listens for subject changes in a room
 * 
 * @author Adam Olsen
 * @version 1.0
 */
public class UserStatusListener implements
        org.jivesoftware.smackx.muc.UserStatusListener {
    private ChatRoomPanel window;

    /**
     * Sets up the group chat subject listener
     * 
     * @param window
     *            the window that this litener belongs to
     */
    public UserStatusListener(ChatRoomPanel window) {
        this.window = window;
    }

    private void setRole(String role) {
        MUCBuddyStatus buddy = window.getBuddyStatus(window.getRoomName() + "/"
                + window.getChat().getNickname());
	window.removeMe();
        buddy.setRole(role);
    }

    private void setAffiliation(String aff) {
        MUCBuddyStatus buddy = window.getBuddyStatus(window.getRoomName() + "/"
                + window.getChat().getNickname());
	window.removeMe();
        buddy.setAffiliation(aff);
    }

    private MUCBuddyStatus getBuddy() {
        MUCBuddyStatus buddy = window.getBuddyStatus(window.getRoomName() + "/"
                + window.getChat().getNickname());
        return buddy;
    }

    public void adminGranted() {
    	window.removeMe();
        setAffiliation("admin");
        window.serverNoticeMessage("You have been granted admin");

    }

    public void adminRevoked() {
	window.removeMe();
        setAffiliation("member");
        window.serverNoticeMessage("Admin status has been revoked");

    }

    public void banned(String user, String reason) {
        window.serverErrorMessage("You were banned by " + user + ": " + reason);
        window.disconnect();
    }

    public void kicked(String user, String reason) {
        window.serverErrorMessage("You were kicked by " + user + ": " + reason);
        window.disconnect();

    }

    public void membershipGranted() {
	window.removeMe();
        setAffiliation("member");
        window.serverNoticeMessage("You have been granted membership");
    }

    public void membershipRevoked() {
	window.removeMe();
        setAffiliation("none");
        window.serverNoticeMessage("Membership status has been revoked");
    }

    public void moderatorGranted() {
	window.removeMe();
        setRole("moderator");
        window.serverNoticeMessage("You have been granted moderator");
    }

    public void moderatorRevoked() {
	window.removeMe();
        setRole("participant");
        window.serverNoticeMessage("Moderator status has been revoked");

    }

    public void ownershipGranted() {
	window.removeMe();

        setAffiliation("owner");
        window.serverNoticeMessage("You have been granted ownership");

    }

    public void ownershipRevoked() {
	window.removeMe();

        setAffiliation("admin");
        window.serverNoticeMessage("Ownership has been revoked");
    }

    public void voiceGranted() {
	window.removeMe();

        setRole("participant");
        window.serverNoticeMessage("You have been granted voice");
    }

    public void voiceRevoked() {
	window.removeMe();

        setRole("visitor");
        window.serverNoticeMessage("Voice has been revoked");

    }
}