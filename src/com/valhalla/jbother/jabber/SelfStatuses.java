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
package com.valhalla.jbother.jabber;

import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import org.jivesoftware.smack.packet.Presence;

/**
 * Vector of user statuses (SelfStatuses)
 * 
 * @author Yury Soldak (tail)
 * @created November 11, 2004
 * @see com.valhalla.jbother.jabber.SelfStatus
 */
public class SelfStatuses {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private static SelfStatuses singleton = null;

    private Vector content = new Vector();

    /**
     * Gets the SelfStatuses singleton
     * 
     * @return the SelfStatuses singleton
     */
    public static SelfStatuses getInstance() {
        if (singleton == null) {
            singleton = new SelfStatuses();
        }
        return singleton;
    }

    /**
     * Creates the SelfStatus Constructor is private, this is a singleton. See
     * the <code>getSingleton</code> method
     */
    private SelfStatuses() {

        SelfStatus offline = new SelfStatus(resources.getString("offline"),
                "offline", null);
        SelfStatus available = new SelfStatus(resources.getString("available"),
                "online", Presence.Mode.AVAILABLE);
        SelfStatus away = new SelfStatus(resources.getString("away"), "away",
                Presence.Mode.AWAY);
        SelfStatus chat = new SelfStatus(resources.getString("chat"), "ffc",
                Presence.Mode.CHAT);
        SelfStatus dnd = new SelfStatus(resources.getString("dnd"), "dnd",
                Presence.Mode.DO_NOT_DISTURB);
        SelfStatus xa = new SelfStatus(resources.getString("xa"), "xa",
                Presence.Mode.EXTENDED_AWAY);
        SelfStatus invisible = new SelfStatus(resources.getString("invisible"),
                "invisible", Presence.Mode.INVISIBLE);

        content.add(available);
        content.add(away);
        content.add(chat);
        content.add(dnd);
        content.add(xa);
        content.add(invisible);
        content.add(offline);
    }

    /**
     * Gets a self status for the specified string
     * 
     * @param title
     *            the title of the status to get
     * @return the SelfStatus
     */
    public SelfStatus getStatus(String title) {
        SelfStatus result = null;

        Iterator statusIterator = content.iterator();
        while (statusIterator.hasNext()) {
            result = (SelfStatus) statusIterator.next();
            if (result.getTitle().equals(title)) {
                break;
            }
        }

        return result;
    }

    /**
     * Gets a status for a mode
     * 
     * @param mode
     *            the mode to get the self status for
     * @return the requested SelfStatus
     */
    public SelfStatus getStatus(Presence.Mode mode) {
        SelfStatus result = null;

        Iterator statusIterator = content.iterator();
        while (statusIterator.hasNext()) {
            result = (SelfStatus) statusIterator.next();
            if (result.getMode() != null && result.getMode().equals(mode)) {
                break;
            }
        }

        return result;
    }

    /**
     * Gets all the self statuses
     * 
     * @return The content value
     */
    public Vector getContent() {
        return content;
    }

}

