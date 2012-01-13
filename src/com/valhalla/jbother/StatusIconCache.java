/*
 * Copyright (C) 2003 Adam Olsen
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 1, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

package com.valhalla.jbother;

import java.io.File;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import org.jivesoftware.smack.packet.Presence;

import com.valhalla.gui.Standard;
import com.valhalla.jbother.jabber.SelfStatuses;
import com.valhalla.settings.Settings;

public class StatusIconCache {
    private static Hashtable statusIconCache = new Hashtable();

    public static ImageIcon getStatusIcon(Presence.Mode mode) {
        String statusShortcut = (mode != null) ? SelfStatuses.getInstance()
                .getStatus(mode).getShortcut() : "offline";
        if (!statusIconCache.containsKey(statusShortcut))
            statusIconCache
                    .put(statusShortcut, fetchStatusIcon(statusShortcut));
        return (ImageIcon) statusIconCache.get(statusShortcut);
    }

    public static ImageIcon fetchStatusIcon(String statusShortcut) {
        if (Settings.getInstance().getProperty("statusTheme") == null)
            Settings.getInstance().setProperty("statusTheme", "default");
        char slash = File.separatorChar;

        String secondPathPart = "statusicons" + slash
                + Settings.getInstance().getProperty("statusTheme") + slash
                + statusShortcut + ".png";
        String userPath = JBother.settingsDir + slash + "themes" + slash
                + secondPathPart;
        ImageIcon result = null;
        if (new File(userPath).exists()) {
            result = new ImageIcon(userPath);
        } else {
            // this was changed because items in JAR files are ALWAYS found with
            // a /, not separatorChar
            result = Standard.getIcon("imagethemes/statusicons/"
                    + Settings.getInstance().getProperty("statusTheme") + '/'
                    + statusShortcut + ".png");
        }
        return result;
    }

    public static void clearStatusIconCache() {
        statusIconCache.clear();
    }
}