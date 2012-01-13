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

import org.jivesoftware.smack.packet.*;

import java.util.*;

/**
 * IQ packet for representing jabber:iq:last
 *
 * @author Andrey Zakirov
 * @version 1.0
*/
public class LastActivity extends IQ
{
	private String seconds = null;
	private String reason = null;

	/**
	 * Default constructor - sets up the packet with no
	 * information
	*/
    public LastActivity()
	{
        this( "", "" );
    }

	/**
	 * Sets up the packet with information
	*/
    public LastActivity( String seconds, String reason)
	{
	   this.seconds = seconds;
      this.reason = reason;
    }

    public String showTime() {
        String show = "";
        if( seconds == null ) return "N/A";
        if( seconds.equals( "0s" ) ) return "0s";

        long l = 0;
        try {
            l = Long.parseLong (seconds);
        }
        catch(NumberFormatException e)
        {
        }

        if( l == 0 ) return "0s";

        long seconds = (long)(l % 60);
        long minutes = (long)(l / 60 % 60);
        long hours = (long)(l / 3600 % 60);
        long days = (long)(l / 3600 / 24);

        if( days > 0 )
        {
            show += days + "d ";
        }
        if( hours > 0 )
        {
            show += hours + "h ";
        }
        if( minutes > 0 )
        {
            show += minutes + "m ";
        }


        show += seconds + "s";

        return show;

    }
    public String getSeconds() { return seconds; }
    public String getReason() { return reason; }
    public void setSeconds( String seconds ) { this.seconds = seconds; }
    public void setReason( String reason ) { this.reason = reason; }
    public String getChildElementXML()
	{
        StringBuffer buf = new StringBuffer();
        buf.append("<query xmlns=\'jabber:iq:last\'");
	if( seconds!= null && getType() != IQ.Type.GET )
	{
		buf.append( " seconds=\'" ).append(this.seconds).append("\'");
		if( reason != null )
		{
			buf.append(">");
			buf.append(reason);
			buf.append("</query>");
		}
		else
		{
			buf.append("/>");

		}
	}
	else
	{
		buf.append("/>");
	}
        return buf.toString();
    }
}
