/*
 *  Copyright (C) 2003 Adam Olsen
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.valhalla.jbother;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.lang.reflect.Array;

import javax.swing.*;
import com.valhalla.jbother.actions.*;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.*;

import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.groupchat.ChatRoomPanel;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.ParsedBuddyInfo;
import com.valhalla.jbother.jabber.smack.SecureExtension;
import com.valhalla.jbother.menus.BuddyListBuddiesMenu;
import com.valhalla.jbother.menus.SetStatusMenu;
import com.valhalla.jbother.plugins.events.ConnectEvent;
import com.valhalla.jbother.plugins.events.StatusChangedEvent;
import com.valhalla.misc.GnuPG;
import com.valhalla.pluginmanager.PluginChain;
import com.valhalla.settings.*;
import net.infonode.tabbedpanel.*;
import net.infonode.tabbedpanel.titledtab.*;
import net.infonode.util.*;

/**
 *  BuddyList is the main controller for the buddy list, as as the buddy list is
 *  the main component of the IM application it performs most of the work once
 *  it's been initialized.
 *
 *@author     Adam Olsen
 *@author     Andrey Zakirov
 *@created    October 26, 2004
 *@update     April 10, 2005
 *@version    1.0
 */
public class BuddyList extends JPanel
{
    private static BuddyList singleton = null;

    private static JFrame frame;

    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault() );

    private XMPPConnection connection;

    //the connection to the jabber server
    private BuddyListTree buddyListTree;

    private boolean signoff = false;

    private TabFrame tabFrame;

    private Hashtable buddyStatuses = null;

    private Presence.Mode currentMode = null;

    private String currentStatusString = resources.getString( "available" );

    private AwayHandler awayHandler = new AwayHandler();

    private javax.swing.Timer awayTimer = null;

   // private javax.swing.Timer pingTimer = new javax.swing.Timer( 300000,
    //    new PingHandler() );

    private boolean idleAway = false;

    private Hashtable blockedUsers = new Hashtable();

    private static BuddyList buddyList = null;

    private boolean docked = false;

    private String gnupgPassword = null;

    private boolean encrypting = false;

    private Hashtable notDeliveredHash = new Hashtable();

    private Hashtable notDisplayedHash = new Hashtable();

    private Hashtable notOfflineHash = new Hashtable();

    private JButton jbButton = new JButton();

    private JButton statusButton = new JButton();

    private BuddyListBuddiesMenu jbMenu = new BuddyListBuddiesMenu( this );

    private SetStatusMenu statusMenu = new SetStatusMenu( this, true, statusButton );

    private long date = new Date().getTime();
    private JToggleButton showOffline = new JToggleButton( StatusIconCache.getStatusIcon( (Presence.Mode)null ) );
    private JToggleButton showAway = new JToggleButton( StatusIconCache.getStatusIcon( Presence.Mode.AWAY ) );
    private JToggleButton sound = new JToggleButton(Standard.getIcon("images/buttons/speaker.png"));
    private boolean added = false;


    /**
     *  Constructor for the buddy list. BuddyList is a singleton, so it's
     *  constructor is private
     */
    private BuddyList()
    {
        initComponents();
    }


    /**
     *  Gets the containerFrame attribute of the BuddyList object
     *
     *@return    The containerFrame value
     */
    public JFrame getContainerFrame()
    {
        return frame;
    }


    /**
     *  Gets the BuddyList singleton
     *
     *@return        the BuddyList singleton
     *@deprecated    Use getInstance() instead
     */
    public static BuddyList getSingleton()
    {
        return getInstance();
    }


    /**
     *  Gets the BuddyList singleton
     *
     *@return    the BuddyList singleton
     */
    public static BuddyList getInstance()
    {
        if( singleton == null )
        {
            singleton = new BuddyList();
        }
        return singleton;
    }

    /*class PingHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            if( !checkConnection() ) return;

            if( idleAway )
            {
                BuddyList.getInstance().setStatus(Presence.Mode.AWAY,
                        resources.getString("autoAway"), false);
            }
            else {
                BuddyList.getInstance().setStatus(currentMode,currentStatusString,false);
            }
        }
    }*/

    public void updateButtons( String mode )
    {
        ImageIcon icon = Standard.getIcon("imagethemes/statusicons/"
                    + mode + "/offline.png");

        showOffline.setIcon( icon );

        icon = Standard.getIcon("imagethemes/statusicons/"
                    + mode + "/away.png");

        showAway.setIcon( icon );
        showOffline.validate();
        showAway.validate();
    }

    public void resetAwayTimer()
    {
        int aTime = 0;

        try {
            aTime = Integer.parseInt(Settings.getInstance().getProperty("autoAwayMinutes", "15"));
        }
        catch( Exception e ) { }

        resetAwayTimer( aTime );
    }

    /**
     *  Starts the away timer
     */
    public void resetAwayTimer( int aTime )
    {
        com.valhalla.Logger.debug( "Away timer is set for " + ( aTime * 60 * 1000 ) );
        if( aTime > 0 )
        {
            if( awayTimer != null ) awayTimer.stop();
            awayTimer = new javax.swing.Timer(aTime * 60 * 1000, awayHandler);
            awayTimer.start();
            com.valhalla.Logger.debug( "Starting away timer " + aTime);
        }
        else if( awayTimer != null )
        {
            awayTimer.stop();
            awayTimer = null;
        }

        if( !added )
        {
            added = true;
            AWTEventListener listener = new MyAWTEventListener();
            Toolkit.getDefaultToolkit().addAWTEventListener( listener,
                    AWTEvent.KEY_EVENT_MASK );
        }
    }


    /**
     *  Loads the blocked users information
     */
    protected void loadBlockedUsers()
    {
        File file = new File( JBother.profileDir + File.separatorChar
                 + "blocked" );
        String line = "";

        try
        {
            FileReader fr = new FileReader( file );
            BufferedReader in = new BufferedReader( fr );

            while( ( line = in.readLine() ) != null )
            {
                blockedUsers.put( line, "blocked" );
                com.valhalla.Logger.debug( "Blocked> " + line );
            }

            fr.close();
        }
        catch( IOException e )
        {
            com.valhalla.Logger.debug( "Blocked users file was not found or could not be read." );
            return;
        }
    }


    /**
     *  Description of the Method
     */
    private void loadNotDeliveredMessages()
    {
        final String notDeliveredMessages = Settings.getInstance().getProperty( "notDeliveredMessages" );
        String[] notDeliveredMessage;
        String[] notDeliveredMessageLast;
        String[] info = new String[2];
        if( notDeliveredMessages != null )
        {
            notDeliveredMessage = notDeliveredMessages.split( ";" );
            if( notDeliveredMessage.length > 0 )
            {
                for( int i = 0; i < notDeliveredMessage.length; i++ )
                {
                    notDeliveredMessageLast = notDeliveredMessage[i].split( "," );
                    if( notDeliveredMessageLast.length == 3 )
                    {
                        info[0] = notDeliveredMessageLast[1];
                        info[1] = notDeliveredMessageLast[2];
                        notDeliveredHash.put( notDeliveredMessageLast[0], info );
                    }
                }
            }
        }
    }


    /**
     *  Gets the eventMessage attribute of the BuddyList object
     *
     *@param  messageId  Description of the Parameter
     *@param  type       Description of the Parameter
     *@return            The eventMessage value
     */
    public String[] getEventMessage( String messageId, int type )
    {
        Hashtable tempHash;
        switch ( type )
        {
            case 1:
                tempHash = notDeliveredHash;
                break;
            case 2:
                tempHash = notDisplayedHash;
                break;
            case 3:
                tempHash = notOfflineHash;
                break;
            default:
                return null;
        }
        String[] eventMessage = (String[])tempHash.get( messageId );
        tempHash.remove( messageId );
        return eventMessage;
    }


    /**
     *  Description of the Method
     *
     *@param  messageId  Description of the Parameter
     *@param  userJID    Description of the Parameter
     *@param  timeStamp  Description of the Parameter
     *@param  type       Description of the Parameter
     *@return            Description of the Return Value
     */
    public boolean putEventMessage( String messageId, String userJID, String timeStamp, int type )
    {
        Hashtable tempHash;
        switch ( type )
        {
            case 1:
                tempHash = notDeliveredHash;
                break;
            case 2:
                tempHash = notDisplayedHash;
                break;
            case 3:
                tempHash = notOfflineHash;
                break;
            default:
                return false;
        }
        if( ( messageId != null ) && ( userJID != null ) && ( timeStamp != null ) )
        {
            String[] info = new String[2];
            info[0] = userJID;
            info[1] = timeStamp;
            tempHash.put( messageId, info );
            return true;
        }
        return false;
    }


    /**
     *  Gets the statusButton attribute of the BuddyList object
     *
     *@return    The statusButton value
     */
    public JButton getStatusButton()
    {
        return statusButton;
    }


    /**
     *  Returns the away timer
     *
     *@return    the away timer
     */
    public javax.swing.Timer getAwayTimer()
    {
        return awayTimer;
    }


    /**
     *  Sets the idleAway attribute of the BuddyList object
     *
     *@param  idleAway  The new idleAway value
     */
    public void setIdleAway( boolean idleAway )
    {
        this.idleAway = idleAway;
    }


    /**
     *  Gets the idleAway attribute of the BuddyList object
     *
     *@return    The idleAway value
     */
    public boolean getIdleAway()
    {
        return idleAway;
    }


    /**
     *  Gets the awayHandler attribute of the BuddyList object
     *
     *@return    The awayHandler value
     */
    public AwayHandler getAwayHandler()
    {
        return awayHandler;
    }


    /**
     *  Sets up the current connection
     *
     *@param  connection  the current connection
     */
    public void init( XMPPConnection connection )
    {
        this.connection = connection;
        if( connection == null )
        {
            return;
        }
        if( buddyStatuses == null )
        {
            buddyStatuses = new Hashtable();
        }

        ConnectEvent event = new ConnectEvent( connection );
        PluginChain.fireEvent( event );

        buddyListTree.setConnection( connection );
        statusMenu.setIcon( Presence.Mode.AVAILABLE );
        //pingTimer.start();
    }


    /**
     *  Clears the buddy tree
     */
    public void clearTree()
    {
        buddyListTree.clearBuddies();
    }


    /**
     *  initializes the buddy tree by loading the offline buddies
     */
    public void initBuddies()
    {
        buddyListTree.loadOfflineBuddies();
    }


    /**
     *  Sets the current presence mode
     *
     *@param  mode  the mode to set it to
     */
    public void setCurrentPresenceMode( Presence.Mode mode )
    {
        this.currentMode = mode;
    }


    /**
     *  Sets the current status string
     *
     *@param  string  the string to use
     */
    public void setCurrentStatusString( String string )
    {
        this.currentStatusString = string;
    }


    /**
     *  Gets the current presence mode
     *
     *@return    the current presence mode
     */
    public Presence.Mode getCurrentPresenceMode()
    {
        return this.currentMode;
    }


    /**
     *  Returns the current status string
     *
     *@return    the current status string
     */
    public String getCurrentStatusString()
    {
        return this.currentStatusString;
    }


    /**
     *  Returns the group chat frame
     *
     *@return    the group chat frame
     */
    public TabFrame getTabFrame()
    {
        return tabFrame;
    }


    /**
     *  Starts the group chat frame
     */
    public void startTabFrame()
    {
        if( tabFrame == null )
        {
            tabFrame = new TabFrame();
        }
    }


    /**
     *  checks to see if there are no more chat room windows in the
     *  groupchatframe if there are no more, the groupchatframe is destroyed
     */
    public void stopTabFrame()
    {
        if( tabFrame == null )
        {
            return;
        }

        if( tabFrame.tabsLeft() <= 0 )
        {
            tabFrame.saveStates();

            if( !docked )
            {
                tabFrame.removeTabListeners();
                DialogTracker.removeDialog( tabFrame );
                tabFrame = null;
                com.valhalla.Logger.debug( "Removing chat frame" );
            }
        }
    }


    /**
     *  Removes a ChatPanel
     *
     *@param  panel  the chat panel to remove
     */
    public void removeTabPanel( TabFramePanel panel )
    {
        if( tabFrame == null )
        {
            return;
        }

        tabFrame.removePanel( panel );
        stopTabFrame();
    }


    /**
     *  Adds a chat room window to the groupchat frame. If there is not
     *  groupchat frame one is created
     *
     *@param  panel  The feature to be added to the TabPanel attribute
     */
    public void addTabPanel( TabFramePanel panel )
    {
        if( tabFrame == null )
        {
            tabFrame = new TabFrame();
        }
        tabFrame.addPanel( panel );

        if( !tabFrame.isVisible() )
        {
            tabFrame.setVisible( true );
            tabFrame.toFront();
        }
    }


    /**
     *  Gets the buddy status
     *
     *@param  userId  the user id of the BuddyStatus
     *@return         The buddyStatus value
     */
    public BuddyStatus getBuddyStatus( String userId )
    {
        if( userId == null )
        {
            return null;
        }
        if( buddyStatuses == null )
        {
            buddyStatuses = new Hashtable();
        }
        BuddyStatus buddy = new BuddyStatus( userId );

        if( buddyStatuses.get( userId.toLowerCase() ) != null )
        {
            buddy = (BuddyStatus)buddyStatuses.get( userId.toLowerCase() );
        }
        else
        {
            buddy.newPubKey();
            buddyStatuses.put( userId.toLowerCase(), buddy );
        }

        return buddy;
    }


    /**
     *  Returns all the buddy status that are available
     *
     *@return    the Hashtable containing all the buddy statuses
     */
    public Hashtable getBuddyStatuses()
    {
        return this.buddyStatuses;
    }


    /**
     *  Returns the current connection
     *
     *@return    the current connection
     */
    public XMPPConnection getConnection()
    {
        return this.connection;
    }


    /**
     *  Checks to see if a connection is active
     *
     *@return    true if the connection is active (connected)
     */
    public boolean checkConnection()
    {
        if( connection == null || !connection.isConnected() )
        {
            return false;
        }
        else
        {
            return true;
        }
    }


    /**
     *  Displays a generic connection error
     */
    public void connectionError()
    {
        Standard.warningMessage( null, resources.getString( "connectionError" ),
                resources.getString( "notConnected" ) );
    }


    /**
     *  Gets the docked attribute of the BuddyList object
     *
     *@return    The docked value
     */
    public boolean isDocked()
    {
        return docked;
    }


    /**
     *  Description of the Method
     */
    public void dockYourself()
    {
        if( frame != null )
        {
            frame.remove( this );
            frame.dispose();
        }

        if( tabFrame == null )
        {
            tabFrame = new TabFrame();
        }
        tabFrame.dockBuddyList( this );
//        tabFrame.setJMenuBar(topMenu);
        frame = tabFrame;
        frame.setVisible( true );
        docked = true;
    }


    /**
     *  Sets the tabFrame attribute of the BuddyList object
     *
     *@param  frame  The new tabFrame value
     */
    public void setTabFrame( TabFrame frame )
    {
        this.tabFrame = frame;
    }


    /**
     *  Description of the Method
     */
    public void undock()
    {
        docked = false;
        if( tabFrame != null )
        {
            tabFrame.undock();
            stopTabFrame();
        }

        if( tabFrame != null )
        {
            tabFrame.setVisible( true );
        }

        BuddyListContainerFrame f = new BuddyListContainerFrame( this );
        frame = f;
        frame.setVisible( true );
    }


    /**
     *  Loads the settings for this profile
     */
    public void loadSettings()
    {
        ConnectorThread.getInstance().resetCredentials();
        buddyStatuses = new Hashtable();
        loadBlockedUsers();
        loadNotDeliveredMessages();
        gnupgPassword = null;

        Vector panels = MessageDelegator.getInstance().getPanels();
        int size = panels.size();
        for( int i = 0; i < size; i++ )
        {
            ConversationPanel panel = (ConversationPanel)panels.get( 0 );
            panel.closeHandler();
        }

        panels.removeAllElements();

        if( tabFrame != null )
        {
            tabFrame.dispose();
            tabFrame = null;
        }

        if( Settings.getInstance().getBoolean( "useTabbedWindow" )
                 && Settings.getInstance().getBoolean( "dockBuddyList" ) )
        {
            dockYourself();
        }
        else if( docked )
        {
            docked = false;
        }

        if( !docked )
        {
            BuddyListContainerFrame f = new BuddyListContainerFrame( this );
            frame = f;
        }

        frame.setVisible( true );

        statusMenu.reloadStatusIcons();
        statusMenu.setModeChecked( null );

        if( Arguments.getInstance().getBoolean( "xmlconsole" ) )
        {

            ParsedBuddyInfo info = new ParsedBuddyInfo( resources.getString( "xmlConsole" ) );
            String userId = info.getUserId().toLowerCase();
            final BuddyStatus buddyStatus = BuddyList.getInstance()
                    .getBuddyStatus( userId );
            buddyStatus.setName( resources.getString( "xmlConsole" ) );
            if( buddyStatus.getConversation() == null )
            {
                buddyStatus.setConversation( ConsolePanel.getInstance( buddyStatus ) );
                MessageDelegator.getInstance().showPanel(
                        buddyStatus.getConversation() );
                MessageDelegator.getInstance().frontFrame(
                        buddyStatus.getConversation() );
            }
        }

        sound.setSelected(!Settings.getInstance().getBoolean("noSound"));
        showOffline.setSelected( Settings.getInstance().getBoolean(
            "showOfflineBuddies") );
        showAway.setSelected(!Settings.getInstance().getBoolean("dontShowAwayBuddies"));
    }


    /**
     *  Saves the current settings - like the height and width of the buddy list
     */
    public void saveSettings()
    {
        if( frame.isVisible() )
        {
            if( !docked )
            {
                //save the current buddy list size and position
                Dimension size = new Dimension( frame.getSize() );
                Point location = new Point( frame.getLocationOnScreen() );

                Settings.getInstance().setProperty( "buddyListX",
                        new Double( location.getX() ).toString() );
                Settings.getInstance().setProperty( "buddyListY",
                        new Double( location.getY() ).toString() );
                Settings.getInstance().setProperty( "buddyListWidth",
                        new Integer( size.width ).toString() );
                Settings.getInstance().setProperty( "buddyListHeight",
                        new Integer( size.height ).toString() );


            }

        }

        Settings.writeSettings();
    }


    /**
     *  Returns the buddy list tree
     *
     *@return    the buddy list tree
     */
    public BuddyListTree getBuddyListTree()
    {
        return this.buddyListTree;
    }


    /**
     *  Init components just does some initial setup, sets the size and
     *  preferred location of the buddy list. Sets up an new initial tree.
     */
    private void initComponents()
    {
        JPanel container = this;

        container.setLayout( new BorderLayout() );
        container.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ) );

        showOffline.setPreferredSize( new Dimension( 24, 24 ) );
        ShowOfflineAction.addItem( showOffline );
        showOffline.setToolTipText(resources.getString("showOfflineBuddies"));

        showAway.setPreferredSize(new Dimension(24,24));
        showAway.setToolTipText(resources.getString("showAwayBuddies"));
        ShowAwayAction.addItem(showAway);
        sound.setPreferredSize(new Dimension(24,24));
        sound.setToolTipText(resources.getString("soundToggle"));
        sound.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                Settings.getInstance().setBoolean("noSound", !sound.isSelected());
		com.valhalla.Logger.debug( Settings.getInstance().getBoolean( "noSound" ) + "" );
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.X_AXIS ) );
        buttonPanel.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ) );
        buttonPanel.add( showOffline );
        buttonPanel.add( Box.createRigidArea(new Dimension(2,0)));
        buttonPanel.add(showAway);
        buttonPanel.add( Box.createRigidArea(new Dimension(2,0)));

        buttonPanel.add(sound);
        buttonPanel.add( Box.createHorizontalGlue());
        container.add( buttonPanel, BorderLayout.NORTH );

        jbButton.addMouseListener( new ButtonListener() );
        jbButton.setToolTipText(resources.getString("jbotherMenu"));
        jbButton.setIcon( Standard.getIcon( "images/frameicon.png" ) );
        statusButton.addMouseListener( new Button2Listener() );

        statusButton.setText( resources.getString( "offline" ) );
        buddyListTree = new BuddyListTree();

        // so the menus get the correct size
        JFrame temp = new JFrame();
        temp.getContentPane().setLayout(new FlowLayout());
        temp.getContentPane().add(jbMenu);
        temp.getContentPane().add(statusMenu);
        temp.pack();
        temp.dispose();

        container.add( buddyListTree, BorderLayout.CENTER );

        JPanel status = new JPanel();

        GridBagLayout gbl = new GridBagLayout();
        status.setLayout( gbl );
        status.setMaximumSize( new Dimension( 1000, jbButton.getHeight() ) );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets( 1, 1, 1, 1 );
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbl.addLayoutComponent( statusButton, gbc );
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets( 1, 1, 1, 1 );
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbl.addLayoutComponent( jbButton, gbc );

        status.add( jbButton );
        status.add( statusButton );

        container.add( status, BorderLayout.SOUTH );

    }


    /**
     *  Description of the Class
     *
     *@author     synic
     *@created    April 12, 2005
     */
    class ButtonListener extends MouseAdapter
    {
        public void mousePressed( MouseEvent e )
        {
            jbMenu.showMenu( e.getComponent(), jbButton.getX() - 1, jbButton.getY() - jbMenu.getHeight() - 1 );
        }
    }


    /**
     *  Description of the Class
     *
     *@author     synic
     *@created    April 12, 2005
     */
    class Button2Listener extends MouseAdapter
    {
        public void mousePressed( MouseEvent e )
        {
            statusMenu.showMenu( e.getComponent(), jbButton.getX() - 1, statusButton.getY() - statusMenu.getHeight() - 1 );
        }

    }

    /**
     *  Sets whether or not we have signed off
     *
     *@param  value  true if we have signed off
     */
    public void setSignoff( boolean value )
    {
        this.signoff = value;
    }


    /**
     *  Gets whether or not we have signed off
     *
     *@return    true if we have signed off
     */
    public boolean getSignoff()
    {
        return signoff;
    }


    /**
     *  Gets the displayed name, or "me"
     *
     *@return    the myDisplayedName setting
     */
    public String getMyName()
    {
        if( Settings.getInstance().getProperty( "myDisplayedName" ) != null )
        {
            return Settings.getInstance().getProperty( "myDisplayedName" );
        }
        return connection.getUser();
    }


    /**
     *  Listens for user input events. If JBother is set to go "Away" on idle,
     *  this will restart the idle timer.
     *
     *@author     Adam Olsen
     *@created    October 26, 2004
     *@version    1.0
     */
    public class MyAWTEventListener implements AWTEventListener
    {
        /**
         *  Called by the event listener
         *
         *@param  evt  the event
         */
        public void eventDispatched( AWTEvent evt )
        {
            setLastActive();
            if( awayTimer != null && awayTimer.isRunning() )
            {
                awayTimer.restart();
            }
            else if( idleAway )
            {
                setStatus( Presence.Mode.AVAILABLE, getCurrentStatusString(),
                        false );
                if( awayTimer != null ) awayTimer.start();
                idleAway = false;
            }

        }
    }


    /**
     *  Closes the application
     */
    public void quitHandler()
    {
        saveSettings();
        if( connection != null && connection.isConnected() )
        {
            connection.close();
        }
        //and finally close the connection

        com.valhalla.Logger.closeLog();
        System.exit( 0 );
    }


    /**
     *  signs off, clears the buddy list
     */
    public void signOff()
    {
        //pingTimer.stop();
        signoff = false;
        buddyListTree.clearBuddies();
        if( awayTimer != null ) awayTimer.stop();
        currentMode = null;
        sendStatusChangedEvent();

        if( connection != null )
        {
            if( !statusMenu.blinkTimerIsRunning() )
            {
                statusMenu.startBlinkTimer();
            }
            Thread thread = new Thread( new ConnectionCloseThread() );
            thread.start();
        }

        if( buddyStatuses != null )
        {
            Iterator iterator = buddyStatuses.keySet().iterator();
            while( iterator.hasNext() )
            {
                String user = (String)iterator.next();
                BuddyStatus buddy = (BuddyStatus)buddyStatuses.get( user );
                buddy.resetBuddy();
                if( buddy.getConversation() != null
                         && buddy.getConversation() instanceof ChatPanel )
                {
                    ( (ChatPanel)buddy.getConversation() ).disconnected();
                }
            }
        }

        if( tabFrame != null )
        {
            ArrayList remove = new ArrayList();

            TabbedPanel pane = tabFrame.getTabPane();
            for( int i = 0; i < pane.getTabCount(); i++ )
            {
                TabFramePanel panel = (TabFramePanel)pane.getTabAt(i).getContentComponent();
                if( panel instanceof ChatRoomPanel )
                {
                    remove.add( panel );
                }
            }

            for( int i = 0; i < remove.size(); i++ )
            {
                TabFramePanel panel = (TabFramePanel)remove.get( i );
                removeTabPanel( panel );
                ( (ChatRoomPanel)panel ).serverNoticeMessage( "You have been disconnected" );
                ( (ChatRoomPanel)panel ).disconnect();
            }
        }
    }


    /**
     *  Description of the Class
     *
     *@author     synic
     *@created    April 12, 2005
     */
    class ConnectionCloseThread implements Runnable
    {
        /**
         *  Main processing method for the ConnectionCloseThread object
         */
        public void run()
        {
            connection.close();
            SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        statusMenu.stopBlinkTimer();
                        statusMenu.setIcon( (Presence.Mode)null );
                    }
                } );

            connection = null;
        }
    }


    /**
     *  Close down the buddy list. Also closes down any other windows that might
     *  be open
     */
    public void kill()
    {
        if( tabFrame != null )
        {
            tabFrame.leaveAll();
        }

        saveSettings();

        setVisible( false );

        currentMode = Presence.Mode.AVAILABLE;
        currentStatusString = resources.getString( "available" );
        connection = null;

        awayTimer.stop();

        Vector panels = MessageDelegator.getInstance().getPanels();
        int size = panels.size();
        for( int i = 0; i < size; i++ )
        {
            ConversationPanel panel = (ConversationPanel)panels.get( 0 );
            panel.closeHandler();
        }

        panels.removeAllElements();

        DialogTracker.kill();
        signoff = false;
    }


    /**
     *  Gets a list of blocked users
     *
     *@return    the list of blocked users
     */
    public Hashtable getBlockedUsers()
    {
        return blockedUsers;
    }


    /**
     *  Sets the list of blocked users
     *
     *@param  users  the list of blocked users
     */
    public void setBlockedUsers( Hashtable users )
    {
        this.blockedUsers = users;
    }


    /**
     *  Gets the GnuPG password
     *
     *@return    GnuPG password
     */
    public String getGnuPGPassword()
    {
        return gnupgPassword;
    }


    /**
     *  Sets the GnuPG password
     *
     *@param  gnupgPassword  GnuPG password
     */
    public void setGnuPGPassword( String gnupgPassword )
    {
        this.gnupgPassword = gnupgPassword;
    }


    /**
     *  Gets the encryption status
     *
     *@return    encryption status
     */
    public boolean isEncrypting()
    {
        return encrypting;
    }


    /**
     *  Sets the encryption status
     *
     *@param  variant  encryption status
     */

    public void isEncrypting( boolean variant )
    {
        this.encrypting = variant;
    }




    /**
     *  Updates all the dialogs window icons
     */
    public void updateIcons()
    {
        StatusIconCache.clearStatusIconCache();
        ImageIcon icon = StatusIconCache.getStatusIcon( org.jivesoftware.smack.packet.Presence.Mode.AVAILABLE );
        if( icon != null )
        {
//            statusMenu.setIcon(icon);
        }
        statusMenu.reloadStatusIcons();
    }


    /**
     *  send StatusChanged event to plugins (and mb elsewhere too)
     */
    private void sendStatusChangedEvent()
    {
        StatusChangedEvent event = new StatusChangedEvent( getInstance() );
        PluginChain.fireEvent( event );
    }


    /**
     *  Set the current status by sending a Jabber packet
     *
     *@param  mode            the mode to set it to
     *@param  defaultMessage  the status message to set it to
     *@param  getMessage      whether or not to get a new message
     *@return                 true if the status packet was sent successfully
     */
    public boolean setStatus( Presence.Mode mode, String defaultMessage,
            boolean getMessage )
    {
        String result;
        if( getMessage )
        {
            StatusDialog statusDlg = new StatusDialog( mode );
            return false;
        }
        else
        {
            result = defaultMessage;
        }
        if( result == null || result.equals( "" ) )
        {
            return false;
        }
        else
        {
            if( mode != Presence.Mode.AWAY ) idleAway = false;
            if( awayTimer != null )
            {
                if( mode == Presence.Mode.AVAILABLE )
                {
                    awayTimer.start();
                }
                else
                {
                    awayTimer.stop();
                }
            }

            int priority = 5;
            try
            {
                priority = Integer.parseInt( Settings.getInstance().getProperty(
                        "priority", "5" ) );
            }
            catch( NumberFormatException nfe )
            {
            }

            if( !checkConnection() )
            {
                ConnectorThread.getInstance().init( mode, result, idleAway ).start();
                return true;
            }

            Presence presence = new Presence( Presence.Type.AVAILABLE, result,
                    priority, mode );

            if( mode == Presence.Mode.INVISIBLE )
            {
                final String r = result;
                final int p = priority;
                presence = new Presence( Presence.Type.AVAILABLE, result,
                    priority, mode )
                {
                    public String toXML()
                    {
                        return "<presence type='invisible'>\n" +
                            "<status>" + r + "</status>\n" +
                            "<priority>" + p + " </priority>\n" +
                            "</presence>\n";

                    }
                };
            }

            GnuPG gnupg = new GnuPG();
            String signedData = null;
            SecureExtension signedExtension = new SecureExtension( "signed" );
            String gnupgSecretKey = Settings.getInstance().getProperty(
                    "gnupgSecretKeyID" );

            if( JBotherLoader.isGPGEnabled()
                     && Settings.getInstance().getBoolean( "gnupgSignPresence" )
                     && gnupgSecretKey != null )
            {
                signedData = gnupg.signExtension( result, gnupgSecretKey );
                if( signedData != null )
                {
                    signedData = signedData.replaceAll( "(\n)+$", "" );
                    signedExtension.setData( signedData );
                    presence.addExtension( signedExtension );
                }

            }
            connection.sendPacket( presence );

            setCurrentPresenceMode( mode );
            if( !idleAway )
            {
                setCurrentStatusString( result );
            }

            // if the group chat window is open, set the status there too
            if( getTabFrame() != null )
            {
                getTabFrame().setStatus( mode, result );
            }

            ParsedBuddyInfo info = new ParsedBuddyInfo( connection.getUser() );
            BuddyStatus buddy = BuddyList.getInstance().getBuddyStatus(
                    info.getUserId() );
            buddy.addResource( info.getResource(), priority, mode, result );
            statusMenu.loadSelfStatuses();

            updateIcons();
            sendStatusChangedEvent();
            statusMenu.setModeChecked( mode );
            statusButton.setText( resources.getString( mode.toString() ) );
            statusButton.repaint();

        }
        return true;
    }

    /**
     *  Gets the statusMenu attribute of the BuddyList object
     *
     *@return    The statusMenu value
     */
    public SetStatusMenu getStatusMenu()
    {
        return statusMenu;
    }


    /**
     *  Gets the buddiesMenu attribute of the BuddyList object
     *
     *@return    The buddiesMenu value
     */
    public BuddyListBuddiesMenu getBuddiesMenu()
    {
        return jbMenu;
    }


    /**
     *  Sets the lastActive attribute of the BuddyList object
     */
    public void setLastActive()
    {
        this.date = new Date().getTime();

    }


    /**
     *  Gets the lastActive attribute of the BuddyList object
     *
     *@return    The lastActive value
     */
    public long getLastActive()
    {
        return this.date/1000;
    }

}

