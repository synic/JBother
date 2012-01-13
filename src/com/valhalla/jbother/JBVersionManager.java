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
package com.valhalla.jbother;

import java.io.File;

/**
 * Assures that version changes go smoothly
 * 
 * @author Adam Olsen
 * @created October 26, 2004
 */
class JBVersionManager {
    /**
     * runs the JBVersionManager
     */
    static void check() {
        checkOldSettings();
    }

    /**
     * Converts the old settings format to the new profiled format
     */
    static void checkOldSettings() {
        File sFile = new File(JBother.settingsDir, "settings");
        File pFile = new File(JBother.settingsDir, ".passwd");
        if (!sFile.exists() && !pFile.exists())
            return;

        File gFile = new File(JBother.settingsDir, "gcbookmarks");
        File lFile = new File(JBother.settingsDir, "logs");
        File bFile = new File(JBother.settingsDir, "blocked");

        File profiles = new File(JBother.profileDir);
        if (!profiles.isDirectory() && !profiles.mkdirs()) {
            com.valhalla.Logger.debug("Could not create profile directory.");
            System.exit(-1);
        }

        sFile.renameTo(new File(JBother.profileDir, "settings.properties"));
        pFile.renameTo(new File(JBother.profileDir, ".passwd"));
        gFile.renameTo(new File(JBother.profileDir, "gcbookmarks"));
        lFile.renameTo(new File(JBother.profileDir, "logs"));
        bFile.renameTo(new File(JBother.profileDir, "blocked"));

        com.valhalla.Logger
                .debug("Successfully made old settings structure conversion.");
    }
}

