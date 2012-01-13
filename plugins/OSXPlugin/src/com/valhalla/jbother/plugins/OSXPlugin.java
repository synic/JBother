package com.valhalla.jbother.plugins;

import com.valhalla.pluginmanager.*;
import com.valhalla.jbother.plugins.events.*;
import com.valhalla.gui.*;
import com.valhalla.jbother.*;
import com.valhalla.jbother.preferences.*;
import com.apple.cocoa.application.NSApplication;
import java.util.*;
import com.apple.eawt.*;

/**
 * OS X specific notification class.
 * This class will make the dock icon bounce until the user makes
 * the application active.  On other platforms, it does nothing.
 *
 * @author     Adam Olsen
 * @created    October 31, 2004
 * @version    1.0
 */
public class OSXPlugin extends ApplicationAdapter implements Plugin,
	PluginEventListener
{
	private int task;
	private NSApplication app;
	private Application a = new Application();


	/**
	 * Initializes the plugin
	 */
	public boolean init()
	{
		if( System.getProperty( "mrj.version" ) == null )
		{
			Standard.warningMessage( null, "OS X Plugin",
				"The OS X plugin only works on OS X." );
			return false;
		}

		BuddyList.getInstance().getTopMenu().setOSX( true );

		a.addApplicationListener( this );
		a.addPreferencesMenuItem();
		a.setEnabledPreferencesMenu( true );

		PluginChain.addListener( this );

		return true;
	}

	public void handleAbout( ApplicationEvent event )
	{
		if( !DialogTracker.containsDialog( AboutDialog.class )  ) new AboutDialog().setVisible(true);
		event.setHandled( true );
	}

	public void handlePreferences( ApplicationEvent event )
	{
		if( !DialogTracker.containsDialog( PreferencesDialog.class )  ) new PreferencesDialog().setVisible(true);
		event.setHandled( true );
	}

	public void handleQuit( ApplicationEvent event )
	{
		BuddyList.getInstance().quitHandler();
		event.setHandled( true );
	}


	/**
	 * Unloads the plugin
	 */
	public void unload()
	{
		PluginChain.removeListener( this );
		a.removeApplicationListener( this );
		BuddyList.getInstance().getTopMenu().setOSX( false );
	}


	/**
	 * Listens for MessageReceivedEvent events
	 *
	 * @param  event  the event
	 */
	public void handleEvent( PluginEvent event )
	{
		if( event instanceof MessageReceivedEvent )
		{
			markNotify();
		}
	}


	/**
	 * the method to call that will make the dock icon bounce
	 */
	public void markNotify()
	{
		app = NSApplication.sharedApplication();
		task = app.requestUserAttention( NSApplication.UserAttentionRequestInformational );

		// because of a bug in Apple's Java API, we have to schedule a time to stop
		// this user attention request
		//new java.util.Timer().schedule( new AttentionTask(), 1500 );
	}


	/**
	 * Stops the dock icon from bouncing
	 *
	 * @author     Adam Olsen
	 * @created    October 31, 2004
	 * @version    1.0
	 */
	class AttentionTask extends TimerTask
	{
		/**
		 * Called by the <code>java.util.Timer</code>
		 */
		public void run()
		{
			// cancels the notifications
			app.cancelUserAttentionRequest( task );
		}
	}
}

