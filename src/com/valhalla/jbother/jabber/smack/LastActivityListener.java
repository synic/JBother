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

import java.util.*;
import java.util.regex.*;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;

import com.valhalla.gui.*;
import com.valhalla.jbother.*;
import com.valhalla.settings.*;

import java.io.*;

/**
 * Listens for and responds to jabber:iq:last requests
 *
 * @author Andrey Zakirov
 * @version 1.0
 */
public class LastActivityListener implements PacketListener {
    /**
     * Event listener called when a Last Activity packet is received
     * @param message the version packet received
     */
    public void processPacket( Packet message ) {
        if( !( message instanceof LastActivity ) || ((IQ)message).getType() != IQ.Type.GET ) return;
        if( !Settings.getInstance().getBoolean("reportIdleTime")) return;
        
        LastActivity last = (LastActivity)message;
        String from = last.getFrom();
        String to = last.getTo();
        
        last.setType(IQ.Type.RESULT);
        last.setFrom( to );
        last.setTo( from );
        long time = (new Date().getTime()/1000) - BuddyList.getInstance().getLastActive();
        last.setSeconds(  Long.toString( time ));
        
        com.valhalla.Logger.debug( "Last Activity request received from " + from + ".  I've been idle for " + time + " seconds." );
        
        if( BuddyList.getInstance().checkConnection() )
            BuddyList.getInstance().getConnection().sendPacket( last );
    }
    
}
