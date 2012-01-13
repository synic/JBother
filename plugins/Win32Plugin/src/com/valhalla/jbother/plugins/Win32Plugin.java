package com.valhalla.jbother.plugins;

import com.valhalla.pluginmanager.*;
import snoozesoft.systray4j.*;
import com.valhalla.jbother.preferences.*;
import com.valhalla.gui.*;
import com.valhalla.jbother.*;
import com.valhalla.jbother.plugins.events.*;
import javax.swing.*;
import org.joshy.jni.*;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import com.valhalla.settings.*;

public class Win32Plugin implements Plugin, PluginEventListener, SysTrayMenuListener
{
	//private Win32PluginPrefsPanel prefs = new Win32PluginPrefsPanel();
	/**
	 *  the System tray menu
	 */
	protected SysTrayMenu menu = null;
	/**
	 *  system tray icons
	 */
	protected SysTrayMenuIcon[] icons = {new SysTrayMenuIcon( PluginLoader.getInstance().getResourceAsStream( "native/systrayoffline.ico" ) ),
			new SysTrayMenuIcon(PluginLoader.getInstance().getResourceAsStream( "native/systray.ico" ) )};

	private SysTrayConnectionListener connectionListener = new SysTrayConnectionListener( this );
	private XMPPConnection connection = null;
	private boolean listenerAdded = false;

	public boolean init()
	{
		if( !System.getProperty( "os.name" ).startsWith( "Windows" ) )
		{
			Standard.warningMessage( null, "Win32Plugin", "The Win32Plugin only works on Windows." );
			return false;
		}
		PluginChain.addListener( this );
		//PreferencesDialog.registerPluginPanel( "Win32 Plugin", prefs );
		createMenu();
		return true;
	}

	public void unload()
	{
		if( connection != null ) connection.removeConnectionListener( connectionListener );
		//PreferencesDialog.removePluginPanel( "Win32 Plugin" );
		PluginChain.removeListener( this );
		if( menu != null ) menu.hideIcon();
	}


	public void handleEvent( PluginEvent event )
	{
		if( event instanceof ConnectEvent ) connectionHandler( event );
		else if( event instanceof ExitingEvent ) exitingHandler( (ExitingEvent)event );
		else if( event instanceof MessageReceivedEvent ) messageReceivedHandler( (MessageReceivedEvent)event );
	}

	private void messageReceivedHandler( MessageReceivedEvent event )
	{
		if( BuddyList.getInstance().getCurrentPresenceMode() != Presence.Mode.AVAILABLE ) return;

		ConversationPanel panel = (ConversationPanel)event.getSource();
		JFrame f = panel.getContainingFrame();
		if( Settings.getInstance().getBoolean( "useTabbedWindow" ) )
		{
			f = BuddyList.getInstance().getTabFrame();
		}

		if( f != null )
		{
			WindowUtil.doFlash( f, false );
		}
	}

	private void connectionHandler( PluginEvent event )
	{
		if( SysTrayMenu.isAvailable() )
		{
			connection = (XMPPConnection)event.getSource();
			if( !listenerAdded )
			{
				connection.addConnectionListener( connectionListener );
				listenerAdded = true;
			}
			menu.setIcon( icons[1] );
		}
	}

	private void exitingHandler( ExitingEvent event )
	{
		if( SysTrayMenu.isAvailable() )
		{
			BuddyList.getInstance().getContainerFrame().setVisible( false );
			event.setExit( false );
		}
		else {
			event.setExit( true );
		}
	}

	/**
	 *  Called when the status icon is left clicked
	 *
	 * @param  e  the event
	 */
	public void iconLeftClicked( SysTrayMenuEvent e )
	{
		if( BuddyList.getInstance().getContainerFrame().isVisible() )
		{
			BuddyList.getInstance().getContainerFrame().setVisible( false );
		}
		else
		{
			BuddyList.getInstance().getContainerFrame().setVisible( true );
			BuddyList.getInstance().getContainerFrame().setState( JFrame.NORMAL );
		}

	}

	/**
	 * left double clicked
	 *
	 * @param  e  the event
	 */
	public void iconLeftDoubleClicked( SysTrayMenuEvent e )
	{
		// Do nothing special!
	}

	/**
	 *  item in the system tray menu is selected
	 *
	 * @param  e  the event
	 */
	public void menuItemSelected( SysTrayMenuEvent e )
	{

		if( e.getActionCommand().equals( "exit" ) )
		{
			BuddyList.getInstance().quitHandler( );
		}
		else if( e.getActionCommand().equals( "prefs" ) )
		{
			if( !DialogTracker.containsDialog( PreferencesDialog.class ) )
			{
				new PreferencesDialog().setVisible(true);
			}

		}
	}

	/**
	 *  Creates the system tray menu
	 */
	private void createMenu()
	{
		if( SysTrayMenu.isAvailable() )
		{

			SysTrayMenuItem itemPrefs = new SysTrayMenuItem( "Preferences", "prefs" );
			SysTrayMenuItem itemExit = new SysTrayMenuItem( "Exit", "exit" );

			itemPrefs.addSysTrayMenuListener( this );
			itemExit.addSysTrayMenuListener( this );

			icons[0].addSysTrayMenuListener( this );
			icons[1].addSysTrayMenuListener( this );

			int which = 0;
			XMPPConnection connection = BuddyList.getInstance().getConnection();
			if( connection != null && connection.isConnected() )
			{
				which = 1;
				if( !listenerAdded )
				{
					connection.addConnectionListener( connectionListener );
					listenerAdded = true;
				}
			}

			menu = new SysTrayMenu( icons[which], "JBother" );
			menu.addItem( itemExit );
			menu.addItem( itemPrefs );

		}
		else {
			com.valhalla.Logger.debug( "Could not create menu." );
		}
	}
}
