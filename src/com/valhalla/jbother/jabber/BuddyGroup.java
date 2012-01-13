/*
 * Copyright (C) 2003 Adam Olsen This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 1, or
 * (at your option) any later version. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program; if not, write to the
 * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.valhalla.jbother.jabber;

import java.util.Vector;

import org.dotuseful.ui.tree.AutomatedTreeNode;

import com.valhalla.settings.Settings;

public class BuddyGroup {

    private String groupName;

    private Vector buddies = new Vector();

    private AutomatedTreeNode node = null;

    public BuddyGroup(final String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return Returns the groupName.
     */
    public String getGroupName() {
        return groupName;
    }

    public int getOnlineCount() {
        int count = 0;
        //synchronized (buddies) {
            for (int i = 0; i < buddies.size(); i++) {
                BuddyStatus currentBuddy = (BuddyStatus) buddies.get(i);
                if (currentBuddy.size() > 0)
                    count++;
            }
        //}

        return count;
    }

    public void addBuddy(BuddyStatus buddy) {
        if (buddy == null)
            return;
        //synchronized (buddies) {
            for (int i = 0; i < buddies.size(); i++) {
                BuddyStatus currentBuddy = (BuddyStatus) buddies.get(i);
                if (currentBuddy != null
                        && buddy.getUser().equals(currentBuddy.getUser())) {
                    return;
                }
            }
            buddies.add(buddy);
        //}

        if (node != null)
            node.setUserObject(this); //node.nodeChanged();
    }

    public void removeBuddy(BuddyStatus buddy) {
        if (buddy == null)
            return;
        //synchronized (buddies) {
            for (int i = 0; i < buddies.size(); i++) {
                BuddyStatus currentBuddy = (BuddyStatus) buddies.get(i);
                if (currentBuddy != null
                        && buddy.getUser().equals(currentBuddy.getUser())) {
                    buddies.remove(i);
                }
            }
        //}
        if (node != null)
            node.setUserObject(this); //node.nodeChanged();

    }

    public void setNode(AutomatedTreeNode node) {
        this.node = node;
    }

    public String toString() {
        if (!Settings.getInstance().getBoolean("showNumbersInGroups"))
            return groupName;
        int totalUsers = 0;
        int onlineUsers = 0;

       // synchronized (buddies) {
            for (int i = 0; i < buddies.size(); i++) {
                BuddyStatus buddy = (BuddyStatus) buddies.get(i);
                if (buddy.size() > 0) {
                    onlineUsers++;
                }
                totalUsers++;
            }
        //}
        return groupName + " (" + onlineUsers + "/" + totalUsers + ")";
    }
}