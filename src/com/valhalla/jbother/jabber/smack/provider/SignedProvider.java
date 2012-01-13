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
package com.valhalla.jbother.jabber.smack.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

import com.valhalla.jbother.jabber.smack.SecureExtension;

/**
 * Parses jabber:x:signed extensions
 * 
 * @author Andrey Zakirov
 * @version 0.1
 */
public class SignedProvider implements PacketExtensionProvider {
    /**
     * Parses extension XML
     * 
     * @param parser
     *            the xml parser
     * @return extension
     * @exception Exception
     *                if an error occurs while parsing the XML
     */
    public PacketExtension parseExtension(XmlPullParser parser)
            throws Exception {
        SecureExtension t = new SecureExtension("signed");
        t.setData(parser.nextText());
        return t;
    }
}

