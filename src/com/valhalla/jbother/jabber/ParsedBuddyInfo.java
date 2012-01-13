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

/**
 * Parses a JID into its various parts
 * 
 * @author Adam Olsen
 * @version 1.0
 */
public class ParsedBuddyInfo {
    private String from, userId, server, resource;

    /**
     * Creates the ParsedBuddyInfo and parses the information
     * 
     * @param from
     *            the address to parse
     */
    public ParsedBuddyInfo(String from) {
        this.from = from;

        parseInfo();
    }

    /**
     * Parses the address
     */
    private void parseInfo() {
        if (from == null)
            return;
        if (from.indexOf("@") > -1) {
            if (from.indexOf("/") > -1)
                resource = from.substring(from.indexOf("/") + 1);

            userId = from.replaceAll("/.*", "");
            server = userId.substring(userId.indexOf("@") + 1);
        } else {
            userId = from;
            server = from;
            resource = "";
        }

        if (resource == null || resource.equals(""))
            resource = "N/A";
        from = from.replaceAll("/.*", "");
    }

    /**
     * Gets the address without the resource
     * 
     * @return the bare XMPPP address without the resource
     */
    public String getBareAddress() {
        return from;
    }

    /**
     * Gets the user ID - without resource if this is not a transport or agent
     * 
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the server that the user is on
     * 
     * @return the server the user is on
     */
    public String getServer() {
        return server;
    }

    /**
     * Gets the resource
     * 
     * @return the resource, or an empty string if there isn't one or the user
     *         is an agent/transport
     */
    public String getResource() {
        return resource;
    }
}