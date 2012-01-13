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

package com.valhalla.jbother.jabber;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.MUCUser;

/**
 * Tracks a users different presences and resources in a mu-conference room
 * 
 * @author Adam Olsen
 * @version 1.0
 */
public class MUCBuddyStatus extends BuddyStatus {
    private String affiliation = "";

    private String role = "";

    private MUCUser u = null;

    private String jid = null;

    private boolean isInRoom = false;

    private MultiUserChat muc = null;

    /**
     * Default constructor
     * 
     * @param userId
     *            the JID of the user this object represents
     */
    public MUCBuddyStatus(String userId) {
        super(userId);
    }

    public void setMUC(MultiUserChat muc) {
        this.muc = muc;
    }

    public MultiUserChat getMUC() {
        return muc;
    }

    public void setIsInRoom(boolean is) {
        this.isInRoom = is;
    }

    public boolean getIsInRoom() {
        return isInRoom;
    }

    /**
     * Sets the affiliation of this user
     * 
     * @param affiliation
     *            the affiliation of this user
     */
    public void setAffiliation(String aff) {
        this.affiliation = aff;
    }

    /**
     * @return the users affiliation
     */
    public String getAffiliation() {
        return affiliation;
    }

    /**
     * Sets the role of this user
     * 
     * @param role
     *            the role of this user
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the role of this user
     */
    public String getRole() {
        return role;
    }

    public void setMUCUser(MUCUser u) {
        this.u = u;
    }

    public MUCUser getMUCUser() {
        return u;
    }

    public String getJid() {
        if (u == null)
            return null;
        MUCUser.Item item = u.getItem();
        if (item == null)
            return null;
        return item.getJid();
    }
}