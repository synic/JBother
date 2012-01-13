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

import org.jivesoftware.smack.packet.Presence;

/**
 * Class for storing relationship between: 1) presence mode, 2) it title, 3) it
 * shortcut
 * 
 * @author Yury Soldak (tail)
 * @see com.valhalla.jbother.menus.SetStatusMenu
 */
public class SelfStatus {
    private String title;

    private String shortcut;

    private Presence.Mode mode;

    /**
     * @param title
     * @param shortcut
     * @param mode
     */
    public SelfStatus(String title, String shortcut, Presence.Mode mode) {
        this.title = title;
        this.shortcut = shortcut;
        this.mode = mode;
    }

    /**
     * @return Returns the mode.
     */
    public Presence.Mode getMode() {
        return mode;
    }

    /**
     * @param mode
     *            The mode to set.
     */
    public void setMode(Presence.Mode mode) {
        this.mode = mode;
    }

    /**
     * @return Returns the shortcut.
     */
    public String getShortcut() {
        return shortcut;
    }

    /**
     * @param shortcut
     *            The shortcut to set.
     */
    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }
}