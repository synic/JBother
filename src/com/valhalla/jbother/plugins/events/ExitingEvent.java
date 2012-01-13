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

package com.valhalla.jbother.plugins.events;

import com.valhalla.pluginmanager.PluginEvent;

/**
 * Generated when someone clicks on the close button
 * 
 * @author Adam Olsen
 */
public class ExitingEvent extends PluginEvent {
    private boolean exit = true;

    public ExitingEvent(Object source) {
        super(source);
    }

    /**
     * @return true if exit should still take place
     */
    public boolean getExit() {
        return exit;
    }

    /**
     * @param exit
     *            set to false if you want to cancel the exit operation
     */
    public void setExit(boolean exit) {
        this.exit = exit;
    }
}