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

package com.valhalla.settings;

import java.util.Properties;

/**
 * A temporary storage container for Settings - used in
 * <code>com.valhalla.jbother.plugins.PluginDialog.java</code>
 * 
 * @author Adam Olsen
 * @version 1.0
 */
public class TempSettings extends Properties {
    /**
     * Gets a boolean from a value
     * 
     * @param key
     *            the key to get
     * @return true or false
     */
    public boolean getBoolean(String key) {
        if (getProperty(key) != null && getProperty(key).equals("!!REMOVED!!"))
            return false;
        return (getProperty(key) != null);
    }

    /**
     * Sets a boolean
     * 
     * @param key
     *            the key to set
     * @param value
     *            true or false
     */
    public void setBoolean(String key, boolean value) {
        if (value)
            setProperty(key, "true");
        else
            setProperty(key, "!!REMOVED!!");
    }
}