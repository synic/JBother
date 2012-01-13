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

package com.valhalla.jbother.plugins;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ConnectionEstablishedListener;
import org.jivesoftware.smack.XMPPConnection;

import com.valhalla.gui.*;
import com.valhalla.jbother.*;
import java.util.*;

import snoozesoft.systray4j.*;

/**
 * Listens to the connection, watches for drops, etc
 * @author Adam Olsen
 * @version 1.0
*/
public class SysTrayConnectionListener implements ConnectionListener
{
	private Win32Plugin plugin = null;

	public SysTrayConnectionListener( Win32Plugin plugin ) { this.plugin = plugin; }
	/**
	 * Called if the connection is lost
	 * @param e an Exception containing the reason for the connection loss
	*/
	public void connectionClosedOnError( Exception e )
	{
	    plugin.menu.setIcon(plugin.icons[0]);
	}

	/**
	 * Called if the connection is closed
	*/
	public void connectionClosed()
	{
	    plugin.menu.setIcon(plugin.icons[0]);
	}

}
