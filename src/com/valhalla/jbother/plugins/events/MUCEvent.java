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

import java.util.Date;

import com.valhalla.pluginmanager.PluginEvent;

/**
 * Generated when someone clicks on the close button
 * 
 * @author Adam Olsen
 */
public class MUCEvent extends PluginEvent {
    public static final int EVENT_PARTICIPANT_JOINED = 0;

    public static final int EVENT_PARTICIPANT_PARTED = 1;

    public static final int EVENT_MESSAGE_RECEIVED = 2;

    public static final int EVENT_SUBJECT_CHANGED = 3;

    private int type = 0;

    private String message;

    private Date date;

    public MUCEvent(Object source, int type, String message, Date date) {
        super(source);
        this.type = type;
        this.message = message;
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public String getFrom() {
        return (String) source;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

}