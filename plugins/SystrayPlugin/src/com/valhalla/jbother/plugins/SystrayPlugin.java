/**
 *  Copyright (C) 2005 Adam Olsen, Yury Soldak This program is free software;
 *  you can redistribute it and/or modify it under the terms of the GNU General
 *  Public License as published by the Free Software Foundation; either version
 *  1, or (at your option) any later version. This program is distributed in the
 *  hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 *  the GNU General Public License for more details. You should have received a
 *  copy of the GNU General Public License along with this program; if not,
 *  write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA
 *  02139, USA.
 *
 *@author    Yury Soldak
 */

package com.valhalla.jbother.plugins;

import com.valhalla.gui.DialogTracker;
import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.ProfileManager;
import com.valhalla.jbother.StatusIconCache;
import com.valhalla.jbother.menus.SetStatusMenu;
import com.valhalla.jbother.plugins.events.*;
import com.valhalla.jbother.preferences.PreferencesDialog;
import com.valhalla.pluginmanager.Plugin;
import com.valhalla.pluginmanager.PluginChain;
import com.valhalla.pluginmanager.PluginEvent;
import com.valhalla.pluginmanager.PluginEventListener;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 *  Description of the Class
 *
 *@author     synic
 *@created    April 12, 2005
 */
public class SystrayPlugin implements Plugin, PluginEventListener
{
    /**
     *  Description of the Field
     */
    protected SystemTray tray = SystemTray.getDefaultSystemTray();
    /**
     *  Description of the Field
     */
    protected TrayIcon ti;
    private SysTrayPluginConnectionListener connectionListener = new SysTrayPluginConnectionListener( this );
    private XMPPConnection connection = null;
    private boolean listenerAdded = false;
    private JMenuItem prefsItem = new JMenuItem( "Preferences" );
    private JMenuItem quitItem = new JMenuItem( "Quit" );
    private SetStatusMenu statusMenu = null;
    private boolean iconShowing = false;


    /**
     *  Initializes plugin
     *
     *@return    true on success
     */
    public boolean init()
    {
        PluginChain.addListener( this );
        initComponents();
        com.valhalla.Logger.debug( "Systray plugin initiated" );
        return true;
    }


    /**
     *  Unloads plugin
     */
    public void unload()
    {
        tray.removeTrayIcon( ti );
        PluginChain.removeListener( this );
        if( connection != null )
        {
            connection.removeConnectionListener( connectionListener );
        }
        com.valhalla.Logger.debug( "Systray plugin unloaded" );
    }


    /**
     *  Handles StatusChangedEvent
     *
     *@param  event  One of plugin events
     */
    public void handleEvent( PluginEvent event )
    {
        if( event instanceof StatusChangedEvent )
        {
            com.valhalla.Logger.debug( "Systray: status changed to '" + BuddyList.getInstance().getCurrentPresenceMode() + "'" );
            if( ti != null )
            {
                ti.setIcon( StatusIconCache.getStatusIcon( BuddyList.getInstance().getCurrentPresenceMode() ) );
            }
            statusMenu.loadSelfStatuses();
            statusMenu.setModeChecked( BuddyList.getInstance().getCurrentPresenceMode() );
        }
        else if( event instanceof ConnectEvent )
        {
            connectionHandler( (ConnectEvent)event );
        }
    }


    /**
     *  Description of the Method
     *
     *@param  event  Description of the Parameter
     */
    private void connectionHandler( PluginEvent event )
    {
        connection = (XMPPConnection)event.getSource();
        if( !listenerAdded )
        {
            connection.addConnectionListener( connectionListener );
            listenerAdded = true;
        }

        ti.setIcon( StatusIconCache.getStatusIcon( BuddyList.getInstance().getCurrentPresenceMode() ) );
    }


    /**
     *  Description of the Class
     *
     *@author     synic
     *@created    April 12, 2005
     */
    private class MenuActionListener implements ActionListener
    {
        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void actionPerformed( ActionEvent e )
        {
            if( e.getSource() == prefsItem )
            {
                if( !DialogTracker.containsDialog( PreferencesDialog.class ) )
                {
                    new PreferencesDialog().setVisible(true);
                }
            }
            else if( e.getSource() == quitItem )
            {
                com.valhalla.Logger.debug( "Quitting..." );
                BuddyList.getInstance().quitHandler();
            }
        }
    }


    /**
     *  Initializes components of which systray consists
     */
    private void initComponents()
    {
        if( iconShowing )
        {
            return;
        }
        iconShowing = true;

        MenuActionListener listener = new MenuActionListener();
        connection = BuddyList.getInstance().getConnection();
        prefsItem.addActionListener( listener );
        quitItem.addActionListener( listener );

        statusMenu = new SetStatusMenu( BuddyList.getInstance(), false);
        statusMenu.setModeChecked( BuddyList.getInstance().getCurrentPresenceMode() );
        statusMenu.addSeparator();
        statusMenu.add( prefsItem );
        statusMenu.add( quitItem );

        ImageIcon i = StatusIconCache.getStatusIcon( BuddyList.getInstance().getCurrentPresenceMode() );

		Toolkit.getDefaultToolkit().sync();
		try {
			Thread.sleep( 500 );
		}
		catch( Exception ex ) { }

        ti = new TrayIcon( i, "TrayIcon", statusMenu );
        ti.setToolTip( "JBother" );
        ti.setCaption( "JBother" );
        ti.addActionListener(
            new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    if( !ProfileManager.isCurrentlyShowing() )
                    {
                        BuddyList.getInstance().getContainerFrame().setVisible( !BuddyList.getInstance().getContainerFrame().isVisible() );
                        if( BuddyList.getInstance().getTabFrame() != null )
                        {
                            BuddyList.getInstance().getTabFrame().setVisible( BuddyList.getInstance().getContainerFrame().isVisible() );
                        }

                        if( BuddyList.getInstance().getContainerFrame().isVisible() )
                        {
                            BuddyList.getInstance().getContainerFrame().setExtendedState( JFrame.NORMAL );
                            BuddyList.getInstance().getContainerFrame().toFront();

                            if( BuddyList.getInstance().getTabFrame() != null )
                            {
                                BuddyList.getInstance().getTabFrame().setExtendedState( JFrame.NORMAL );
                                BuddyList.getInstance().getTabFrame().toFront();
                            }
                        }
                    }
                }
            } );
        tray.addTrayIcon( ti );
    }

}

/**
 *  Listens to the connection, watches for drops, etc
 *
 *@author     Adam Olsen
 *@created    April 12, 2005
 *@version    1.0
 */
class SysTrayPluginConnectionListener implements ConnectionListener
{


    private SystrayPlugin plugin = null;


    /**
     *  Constructor for the SysTrayPluginConnectionListener object
     *
     *@param  plugin  Description of the Parameter
     */
    public SysTrayPluginConnectionListener( SystrayPlugin plugin )
    {
        this.plugin = plugin;
    }


    /**
     *  Called if the connection is lost
     *
     *@param  e  an Exception containing the reason for the connection loss
     */
    public void connectionClosedOnError( Exception e )
    {
        connectionClosed();
    }


    /**
     *  Called if the connection is closed
     */
    public void connectionClosed()
    {
        plugin.ti.setIcon( StatusIconCache.getStatusIcon( null ) );
    }

}

