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

import org.jivesoftware.smack.packet.Packet;

/**
 * Blank packet for creating user-defined packets
 * 
 * @author Andrey Zakirov
 * @version 1.0
 */
public class Blank extends Packet {
    private String text = null;

    /**
     * Default constructor - sets up the packet with no information
     */
    public Blank() {
        this.text = "";
    }

    /**
     * Sets up the packet with information
     * 
     * @param text
     *            user-defined packet text
     */

    public Blank(String text) {
        this.text = text;
    }

    /**
     * Builds the packet
     * 
     * @return the XML version of the packet
     */
    public String toXML() {
        return text;
    }
}