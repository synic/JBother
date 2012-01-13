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

package com.valhalla.jbother.jabber.smack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.Time;

import com.valhalla.jbother.BuddyList;

/**
 * Listens for and responds to jabber:iq:time requests
 * 
 * @author Adam Olsen
 * @version 1.0
 */
public class TimeListener implements PacketListener {
    /**
     * Event listener called when a Time packet is received
     * 
     * @param message
     *            the version packet received
     */
    public void processPacket(Packet message) {
        if (!(message instanceof Time)
                || ((IQ) message).getType() != IQ.Type.GET)
            return;

        Time time = (Time) message;

        String from = message.getFrom();
        String to = message.getTo();

        com.valhalla.Logger.debug("Time request received from " + from);

        Calendar cal = Calendar.getInstance();

        SimpleDateFormat utcFormat = new SimpleDateFormat("yyyyMMdd'T'hh:mm:ss");
        DateFormat displayFormat = DateFormat.getDateTimeInstance();

        TimeZone timeZone = cal.getTimeZone();
        time.setTz(cal.getTimeZone().getID());
        time.setDisplay(displayFormat.format(cal.getTime()));
        // Convert local time to the UTC time.
        time.setUtc(utcFormat.format(new Date(cal.getTimeInMillis()
                - timeZone.getOffset(cal.getTimeInMillis()))));
        time.setTo(from);
        time.setFrom(to);
        time.setType(IQ.Type.RESULT);

        // send the response
        if (BuddyList.getInstance().checkConnection())
            BuddyList.getInstance().getConnection().sendPacket(time);
    }
}