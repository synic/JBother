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

package com.valhalla.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class SettingsProperties extends Properties {
    /**
     * Sets a boolean
     * 
     * @param key
     *            the key to set
     * @param value
     *            the value to set the key to
     */
    public void setBoolean(String key, boolean value) {
        if (value)
            setProperty(key, "true");
        else
            remove(key);
    }

    /**
     * Gets a boolean value
     * 
     * @param key
     *            the key to get a boolean for
     * @return a boolean based on the key
     */
    public boolean getBoolean(String key) {
        return (getProperty(key) != null);
    }

    public void loadSettings(String file) throws FileNotFoundException,
            IOException {
        File f = new File(file);
        InputStream stream = new FileInputStream(f);
        load(stream);
        stream.close();
    }

    public void saveSettings(String file, String comments) throws IOException {
        File f = new File(file);
        OutputStream stream = new FileOutputStream(f);
        store(stream, comments);
        stream.close();
    }
}