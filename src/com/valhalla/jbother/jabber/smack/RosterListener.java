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
import com.valhalla.jbother.*;
import com.valhalla.jbother.jabber.*;
import javax.swing.*;
import org.jivesoftware.smack.*;
import java.util.*;

/**
 * Listens for changes in the Roster
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class RosterListener implements org.jivesoftware.smack.RosterListener {
    boolean ran = true;
    /**
     * Called when a roster entry's presence changes
     */
    public void presenceChanged(String address) {

        if( ran ) return;
        ran = true;

            Roster roster = ConnectorThread.getInstance().getRoster();
            Iterator i = roster.getEntries();
            while( i.hasNext() )
            {
                RosterEntry entry = (RosterEntry)i.next();


                final BuddyStatus buddy = BuddyList.getInstance().getBuddyStatus(
                entry.getUser() );

                SwingUtilities.invokeLater( new Runnable() { public void run() {
                        BuddyList.getInstance().getBuddyListTree().addBuddy( buddy ); } } );
            }
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run() { BuddyList.getInstance().getBuddyListTree().reloadBuddies(); } } );

    }

    /**
     * Not used by this class
     */
    public void rosterModified() {
    }
    
    public void entriesDeleted(Collection col)
    {
    }
    
    public void entriesUpdated(Collection col)
    {
    }
    
    public void entriesAdded(Collection col)
    {
    }
}
