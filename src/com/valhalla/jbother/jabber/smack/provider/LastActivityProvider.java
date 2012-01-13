/*
 *  Copyright (C) 2003 Adam Olsen
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.valhalla.jbother.jabber.smack.provider;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.packet.*;
import com.valhalla.jbother.jabber.smack.*;
import org.xmlpull.v1.XmlPullParser;

/**
 *  Parses Last Activity packets
 *
 *@author     Andrey Zakirov
 *@created    April 10, 2005
 */
public class LastActivityProvider implements IQProvider {
    /**
     *  parses the packet
     *
     *@param  parser  the xml parser
     *@return         the parsed IQ object
     */
    public IQ parseIQ(XmlPullParser parser) {
        LastActivity a = new LastActivity();
        a.setSeconds(parser.getAttributeValue("", "seconds"));

        try {
            boolean done =false;
            while (!done) {
                int eventType = parser.next();
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("reason")) {
                        a.setReason(parser.nextText());
                    }
                }

                else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("query")) {
                        done = true;
                    }
                }
            }
        }
        catch( Exception ex )
        {
            com.valhalla.Logger.logException(ex);
        }

        return a;
    }
}

