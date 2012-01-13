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

import org.jivesoftware.smack.packet.PacketExtension;

/**
 * Builds extension to send encrypted & signed data
 * 
 * @author Andrey Zakirov
 * @version 1.0
 */
public class SecureExtension extends Object implements PacketExtension {
    private String Type = null;

    private String Data = null;

    /**
     * Default constructor - creates extension with type "encrypted"
     */
    public SecureExtension() {
        this.Type = "encrypted";
    }

    /**
     * Creates extension with user-defined type
     * 
     * @param Type
     *            user-defined type
     */
    public SecureExtension(String Type) {
        this.Type = Type;
    }

    /**
     * Sets up user-defined type for extension
     * 
     * @param Type
     *            user-defined type
     */
    public void setType(String Type) {
        this.Type = Type;
    }

    /**
     * Gets extension type
     * 
     * @return extension type
     */
    public String getType() {
        return this.Type;
    }

    /**
     * Sets up user-defined data for extension
     * 
     * @param Type
     *            user-defined data
     */
    public void setData(String Data) {
        this.Data = Data;
    }

    /**
     * Gets extension data
     * 
     * @return extension data
     */
    public String getData() {
        return this.Data;
    }

    /**
     * Builds the packet
     * 
     * @return the XML version of the packet
     */
    public String toXML() {
        return "<x xmlns=\"jabber:x:" + this.Type + "\">" + this.Data + "</x>";
    }

    /**
     * Gets extension element name
     * 
     * @return element name
     */
    public String getElementName() {
        return "x";
    }

    /**
     * Gets extension element namespace
     * 
     * @return element namespace
     */
    public String getNamespace() {
        return "jabber:x:" + this.Type;
    }
}