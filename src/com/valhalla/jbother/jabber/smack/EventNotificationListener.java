/*
 * Copyright (C) 2003 Adam Olsen
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 1, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

package com.valhalla.jbother.jabber.smack;

import javax.swing.SwingUtilities;

import org.jivesoftware.smackx.MessageEventNotificationListener;

import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.ChatPanel;
import com.valhalla.jbother.EventPanel;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.ParsedBuddyInfo;
import java.util.Locale;
import java.util.ResourceBundle;



public class EventNotificationListener implements
        MessageEventNotificationListener {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());


    public void deliveredNotification(String from, String packetID) {
        receiveNotificationHandler(from, BuddyList.getInstance ().getEventMessage ( packetID, 1), "delivered") ;
    }

    public void displayedNotification(String from, String packetID) {
        receiveNotificationHandler(from, BuddyList.getInstance ().getEventMessage ( packetID, 2), "displayed") ;
    }

    public void offlineNotification(String from, String packetID) {
        receiveNotificationHandler(from, BuddyList.getInstance ().getEventMessage ( packetID, 3), "stored on server") ;
    }

    public void composingNotification(String from, String packetID) {
        final ParsedBuddyInfo info = new ParsedBuddyInfo(from);
        final String userId = info.getUserId().toLowerCase();
        final BuddyStatus buddy = BuddyList.getInstance()
                .getBuddyStatus(userId);
        if (buddy.getConversation() != null
                && buddy.getConversation() instanceof ChatPanel) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ((ChatPanel) buddy.getConversation()).setIsTyping(true);
                }
            });
        }
    }

    public void cancelledNotification(String from, String packetID) {
        final ParsedBuddyInfo info = new ParsedBuddyInfo(from);
        final String userId = info.getUserId().toLowerCase();
        final BuddyStatus buddy = BuddyList.getInstance()
                .getBuddyStatus(userId);
        if (buddy.getConversation() != null
                && buddy.getConversation() instanceof ChatPanel) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ((ChatPanel) buddy.getConversation()).setIsTyping(false);
                }
            });
        }
    }

    private void receiveNotificationHandler (String from,  final String [] doneMessage, final String eventType )
    {

        from=resources.getString("eventConsole");
               final BuddyStatus buddy =  BuddyList.getInstance().getBuddyStatus(new ParsedBuddyInfo(from).getUserId().toLowerCase());
        buddy.setName ( resources.getString("eventConsole"));

        if (buddy.getConversation() == null) {
                    buddy.setConversation( new EventPanel(buddy));
                }


        if ( doneMessage !=null &&
                buddy.getConversation() != null &&
                buddy.getConversation() instanceof EventPanel) {
                final String[] statusMessage = doneMessage;
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run() {
                        ((EventPanel) buddy.getConversation()).messageEvent( "<font color='red'>Message sent to " + BuddyList.getInstance().getBuddyStatus(new ParsedBuddyInfo(statusMessage[0]).getUserId().toLowerCase()).getName () + " at " +  statusMessage[1]  +  " was " + eventType + ".");
                    }
                });

            }
    }
}