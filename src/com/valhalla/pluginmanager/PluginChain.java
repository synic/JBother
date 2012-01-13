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
package com.valhalla.pluginmanager;

import java.util.Iterator;
import java.util.Vector;

/**
 * The PluginChain keeps track of PluginEventListeners. Distributes events to
 * the different PluginEventListeners
 * 
 * @author Adam Olsen
 * @created October 31, 2004
 */
public class PluginChain {
    private static Vector pluginListeners = new Vector();

    /**
     * Adds a PluginEventListener to the chain
     * 
     * @param listener
     *            The listener to addd
     */
    public static void addListener(PluginEventListener listener) {
        pluginListeners.add(listener);
    }

    /**
     * Removes a listener from the chain
     * 
     * @param listener
     *            The listener to remove
     */
    public static void removeListener(PluginEventListener listener) {
        pluginListeners.remove(listener);
    }

    /**
     * Notifies all the EventListeners of an event
     * 
     * @param e
     *            the event to fire
     */
    public static void fireEvent(PluginEvent e) {
        Iterator i = pluginListeners.iterator();

        while (i.hasNext()) {
            PluginEventListener listener = (PluginEventListener) i.next();
            listener.handleEvent(e);
        }
    }
}

