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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Presence;

import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.ConversationPanel;
import com.valhalla.jbother.ConnectorThread;
import com.valhalla.settings.Settings;

import javax.swing.*;

/**
 * Tracks a users different presences and resources
 *
 * @author Adam Olsen
 * @author Andrey Zakirov
 * @version 1.1
 */
public class BuddyStatus extends Hashtable {
    protected ResourceBundle sources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    protected String userId = "";

    protected ConversationPanel conversation = null;

    protected String name = null;

    protected boolean removed = false;

    protected boolean hasSignedOn = false;

    protected String resources = "";

    protected Hashtable presences = null;

    protected Properties statusMessages = null;

    protected String tempGroup = null;

    protected String versionInfo = null;

    protected String composingId = "";

    protected boolean encrypting = false;

    protected boolean askForDelivered = false;

    protected boolean askForDisplayed = false;

    protected boolean askForOffline = false;

    protected Hashtable notDisplayedMessages = new Hashtable();

    protected String pubKeyId = null;
    protected RosterEntry entry = null;

    /**
     * Sets up the buddy container
     *
     * @param buddyId
     *            the buddy/user to track
     */
    public BuddyStatus(String buddyId) {
        this.userId = buddyId;
    }

    public void setComposingID(String id) {
        this.composingId = id;
    }

    public String getComposingID() {
        return composingId;
    }

    public void addNotDisplayedID(String packetID, String from) {
        notDisplayedMessages.put ( packetID, from);
    }


    public void sendNotDisplayedID() {
                String packetID = null;
                String from = null;
                while (notDisplayedMessages.keys ().hasMoreElements ())
                {
                    from = (String) notDisplayedMessages.keys ().nextElement ();
                    packetID = (String) notDisplayedMessages.get ( from );
                    ConnectorThread.getInstance().getMessageEventManager().sendDisplayedNotification ( packetID, from);
                    if (from !=null)
                    {
                        notDisplayedMessages.remove ( from);
                    }
                }
    }

    /**
     * Gets the resource with the higest priority
     *
     * @return the resource with the highest priority, or null if the user is
     *         offline
     */
    public String getHighestResource() {
        String resource = null;

        Set keys = keySet();
        Iterator iterator = keys.iterator();

        int highest = -2;

        while (iterator.hasNext()) {
            Object key = iterator.next();
            Integer value = (Integer) get(key);

            if (value.intValue() > highest) {
                resource = (String) key;
                highest = value.intValue();
            }
        }

        return resource;
    }

    public String getAddress()
    {
        String to = getUser();
        if(size() > 0 && !getHighestResource().equals("N/A")) to += "/" + getHighestResource();
        return to;
    }

    /**
     * Resets the buddy so that it appears as though they never signed on
     */
    public void resetBuddy() {
        clear();
        presences = null;
        statusMessages = null;
    }

    /**
     * Adds a resource to the tracker
     *
     * @param resource
     *            the resource name
     * @param priority
     *            the priority level of this resource
     * @param mode
     *            the current presence mode
     * @param statusMessage
     *            the status message if there is one
     */
    public void addResource(String resource, int priority, Presence.Mode mode,
            String statusMessage) {
        if (presences == null)
            presences = new Hashtable();
        if (statusMessages == null)
            statusMessages = new Properties();
        presences.put(resource, mode);

        if (statusMessage == null)
            statusMessage = "";
        statusMessages.setProperty(resource, statusMessage);
        put(resource, new Integer(priority));
    }

    /**
     * Gets the status message of the highest resource
     *
     * @return the status message of the highest resource, or an empty string if
     *         there is no status message
     */
    public String getStatusMessage(String resource) {
        if (resource == null)
            return "";
        if (statusMessages == null)
            statusMessages = new Properties();
        String message = statusMessages.getProperty(resource);
        if (message == null)
            message = "";
        return message;
    }

    /**
     * Stops tracking a resource
     *
     * @param resource
     *            the resource to remove
     */
    public void removeResource(String resource) {
        if (presences != null)
            presences.remove(resource);
        if (statusMessages != null)
            statusMessages.remove(resource);
        remove(resource);
    }

    /**
     * Gets the presence mode of the highest priority
     *
     * @return the presence mode of the highest priority or null if the user is
     *         offline
     */
    public Presence.Mode getPresence(String resource) {
        if (presences == null)
            presences = new Hashtable();
        if (resource == null)
            return null;
        return (Presence.Mode) presences.get(resource);
    }

    /**
     * Sets a temporary group name (if the user is displayed before the group
     * actually changes on the server)
     *
     * @param group
     *            the temporary group to use
     */
    public void setTempGroup(String group) {
        this.tempGroup = group;
    }

    /**
     * Returns the temporary group
     *
     * @return the temp group
     */
    public String getTempGroup() {
        String temp = tempGroup;
        tempGroup = null;

        return temp;
    }

    /**
     * Gets the group the user is in in the Roster
     *
     * @return the group the user is in
     */
    public String getGroup() {
        RosterEntry entry = getRosterEntry();

        if (entry == null)
            return sources.getString("notInRoster");
        Iterator groups = entry.getGroups();

        String group = sources.getString("contactsGroup");
        if (groups.hasNext())
            group = ((RosterGroup) groups.next()).getName();
        if (userId.indexOf("@") < 0)
            group = sources.getString("transportsGroup");
        return group;
    }

    /**
     * Gets the roster entry for this user
     *
     * @return the RosterEntry for this user
     */
    public RosterEntry getRosterEntry() {
        if (!BuddyList.getInstance().checkConnection())
            return null;

        if( entry != null ) return entry;
        Roster roster = ConnectorThread.getInstance().getRoster();
        if( roster == null ) return entry;
        entry = roster.getEntry( userId );
        return entry;
    }

    /**
     * Sets the users jabber:iq:version information
     *
     * @param info
     *            the users jabber:iq:version information
     */
    public void setVersionInfo(String info) {
        this.versionInfo = info;
    }

    /**
     * @return the users jabber:iq:version information
     */
    public String getVersionInfo() {
        return versionInfo;
    }

    /**
     * Whether or not the user has signed on
     *
     * @param on
     *            set to true if the user has signed on
     */
    public void setHasSignedOn(boolean on) {
        this.hasSignedOn = on;
    }

    /**
     * @return true if the user has signed on
     */
    public boolean getHasSignedOn() {
        return this.hasSignedOn;
    }

    /**
     * @param removed
     *            set to true if this user has been removed from the roster
     */
    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    /**
     * @return true if the user has been removed from the roster
     */
    public boolean getRemoved() {
        return this.removed;
    }

    /**
     * @return the users alias
     */
    public String getName() {
        if (name != null)
            return name;
        RosterEntry entry = getRosterEntry();
        if (entry != null && entry.getName()!=null)
            return entry.getName();
        else if (userId == null)
            return "unknown";
        else
            return userId;
    }

    /**
     * @param name
     *            the buddy's alias
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param buddyId
     *            the userId
     */
    public void setUser(String buddyId) {
        this.userId = buddyId.toLowerCase();
    }

    /**
     * @return the JID for this Buddy
     */
    public String getUser() {
        return this.userId;
    }

    /**
     * Sets the ConversationPanel for this buddy
     *
     * @param window
     *            the conversation for this buddy
     */
    public void setConversation(ConversationPanel window) {
        this.conversation = window;
    }

    /**
     * Gets the conversation window for this buddy
     *
     * @return the conversation for this buddy
     */
    public ConversationPanel getConversation() {
        return this.conversation;
    }

    /**
     * Gets the encryption status
     *
     * @return encryption status
     */
    public boolean isEncrypting() {
        return encrypting;
    }

    /**
     * Sets the encryption status
     *
     * @param variant
     *            encryption status
     */

    public void isEncrypting(boolean variant) {
        this.encrypting = variant;
    }


    public boolean isAskForDelivered() {
         return askForDelivered;
     }

     public void isAskForDelivered(boolean variant) {
         this.askForDelivered = variant;
     }

     public boolean isAskForDisplayed() {
         return askForDisplayed;
     }

     public void isAskForDisplayed(boolean variant) {
         this.askForDisplayed = variant;
     }

     public boolean isAskForOffline () {
         return askForOffline;
     }

     public void isAskForOffline(boolean variant) {
         this.askForOffline = variant;
     }


    /**
     * Gets Public Key ID
     *
     * @return public key ID
     */
    public String getPubKey() {
        return this.pubKeyId;
    }

    /**
     * Sets Public Key ID
     *
     * @param ID
     *            public key ID
     */
    public void setPubKey(String Id) {
        if (Id == null) {
            Settings.getInstance()
                    .remove("gnupg." + this.userId + ".PublicKey");
        } else {
            Settings.getInstance().setProperty(
                    "gnupg." + this.userId + ".PublicKey", Id);
        }
        this.pubKeyId = Id;
    }

    /**
     * Creates Public Key ID
     *
     */

    public void newPubKey() {

        String pubKey = Settings.getInstance().getProperty(
                "gnupg." + this.userId + ".PublicKey");
        if (pubKey != null) {
            this.pubKeyId = pubKey;
        }

    }
}