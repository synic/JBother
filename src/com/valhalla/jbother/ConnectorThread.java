/*
 Copyright (C) 2003 Adam Olsen
 This program is free software; you can redistribute it and/or modify
 it under the terms of the
 GNU General Public License as published by
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
package com.valhalla.jbother;

import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.*;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.packet.*;
import org.jivesoftware.smackx.filetransfer.*;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.Time;
import org.jivesoftware.smackx.provider.*;

import com.valhalla.gui.Standard;
import com.valhalla.jbother.*;
import com.valhalla.jbother.jabber.smack.*;
import com.valhalla.jbother.jabber.*;
import com.valhalla.jbother.preferences.*;
import com.valhalla.jbother.jabber.smack.provider.*;
import com.valhalla.misc.GnuPG;
import com.valhalla.misc.SimpleXOR;
import com.valhalla.settings.Settings;

/**
 * Attempts to connect to the server. If the connection is made successfully, it
 * sets up the various packet listeners and displays the BuddyList
 *
 * @author Adam Olsen
 * @author Andrey Zakirov
 * @created April 10, 2005
 * @version 1.0
 */
public class ConnectorThread implements Runnable {
    private static ConnectorThread instance = null;
    private Thread thread = null;

    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private String server, username, resource;

    private String password = null;

    private boolean ssl;
    private boolean gmail;
    private boolean proxy;

    private int port = 0;
    private String proxyhost = "";
    private int proxyport = 0;

    private String errorMessage;

    private XMPPConnection connection = null;

    private boolean hasHadError = false;

    private com.valhalla.jbother.jabber.smack.ConnectionListener conListener = new com.valhalla.jbother.jabber.smack.ConnectionListener();

    private com.valhalla.jbother.jabber.smack.RosterListener
     rosterListener =
      new com.valhalla.jbother.jabber.smack.RosterListener();
    private MessagePacketListener messageListener = new MessagePacketListener();

    private Presence.Mode connectMode = Presence.Mode.AVAILABLE;

    private String statusString = null;

    private boolean persistent = false;

    private boolean cancelled = false;

    private boolean away = false;

    private MessageEventManager eventManager;

    private String gnupgSecretKey = Settings.getInstance().getProperty(
            "gnupgSecretKeyID");

    private String gnupgTempPass = null;

    private int connectCount = 0;
    private Roster roster = null;
    private RosterExchangeManager exchangeManager;
    private FileTransferManager ftmanager = null;

    /**
     * Sets up the connector thread
     *
     * @param connectMode
     *            Description of the Parameter
     * @param statusString
     *            Description of the Parameter
     * @param away
     *            Description of the Parameter
     */
    private ConnectorThread() {

        ProviderManager.addIQProvider("query", "jabber:iq:last",
                new LastActivityProvider());

        ProviderManager.addExtensionProvider("x", "jabber:x:encrypted",
                new EncryptedProvider());
        ProviderManager.addExtensionProvider("x", "jabber:x:signed",
                new SignedProvider());

        ProviderManager.addIQProvider("query", "jabber:iq:search",
            new com.valhalla.jbother.jabber.smack.provider.SearchProvider());

        ProviderManager.addIQProvider("vCard", "vcard-temp",
            new VCardProvider());

        PrivateDataManager.addPrivateDataProvider("storage", "storage:bookmarks", new BookmarkProvider());
    }

    public XMPPConnection getConnection() {
        return connection;
    }
    
    public FileTransferManager getFileTransferManager() {
        return ftmanager;
    }

    public boolean isAlive()
    {
        if( thread == null ) return false;
        else return thread.isAlive();
    }

    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }

    public RosterExchangeManager getExchangeManager() { return exchangeManager; }

    public static ConnectorThread getInstance()
    {
        if( instance == null ) instance = new ConnectorThread();
        return instance;
    }

    public Roster getRoster() { return roster; }

    public ConnectorThread init( Presence.Mode connectMode, String statusString,
            boolean away )
    {
        this.server = Settings.getInstance().getProperty("defaultServer");
        if( server != null ) server = server.toLowerCase();
        this.username = Settings.getInstance().getProperty("username");
        this.resource = Settings.getInstance().getProperty("resource");
        this.ssl = Settings.getInstance().getBoolean("useSSL");
        this.gmail = Settings.getInstance().getBoolean("gmailBox");
        this.proxy = Settings.getInstance().getBoolean("useProxy");

        String p = Settings.getInstance().getProperty("port");
        if (p != null) {
            try{ port = Integer.parseInt(p); }
            catch (NumberFormatException ignore){ }
        }

        if(this.proxy){
            this.proxyhost = Settings.getInstance().getProperty("proxyHost");
            String t = Settings.getInstance().getProperty("proxyPort");
            try{ proxyport = Integer.parseInt(t); }
            catch(NumberFormatException ignore){ }
        }

        hasHadError = false;
        this.connectMode = connectMode;
        this.statusString = statusString;
        this.away = away;
        connectCount = 0;

        return instance;
    }

    public void resetCredentials() {
        gnupgSecretKey = Settings.getInstance().getProperty("gnupgSecretKeyID");
        password = Settings.getInstance().getProperty("password");
        if (password != null)
            password = SimpleXOR.decrypt(password, "JBother rules!");
        gnupgTempPass = null;
    }

    /**
     * Gets the messageEventManager attribute of the ConnectorThread class
     *
     * @return The messageEventManager value
     */
    public MessageEventManager getMessageEventManager() {
        return eventManager;
    }

    /**
     * Sets the cancelled attribute of the ConnectorThread class
     *
     * @param c
     *            The new cancelled value
     */
    public void setCancelled(boolean c) {
        cancelled = c;
        connectCount = 0;
    }

    /**
     * Sets whether or not this thread should try to reconnect if there is a
     * connection error
     *
     * @param persistent
     *            set to <tt>true</tt> if you want the thread to continue to
     *            try and connect even if there's an error
     */
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
        connectCount = 0;
    }

    /**
     * Returns the ConnectionListener
     *
     * @return the connection listener
     */
    public com.valhalla.jbother.jabber.smack.ConnectionListener getConnectionListener() {
        return conListener;
    }

    /**
     * Sets whether or not a connection error has already been thrown for this
     * connection
     *
     * @param has
     *            true if an error has already been thrown
     */
    public void setHasHadError(boolean has) {
        hasHadError = has;
    }

    /**
     * Called when the Threads .start() method is called
     */
    public void run() {
        errorMessage = null;
        resetCredentials();
        com.valhalla.Logger.debug("Connector thread starting...");
        if (!BuddyList.getInstance().getStatusMenu()
                .blinkTimerIsRunning()) {
            BuddyList.getInstance().getStatusMenu()
                    .startBlinkTimer();
        }

        if( connectCount >= 15 )
        {
            BuddyList.getInstance().getStatusMenu().stopBlinkTimer();

            SwingUtilities.invokeLater( new Runnable()
                {
                    public void run()
                    {
                        BuddyList.getInstance().getStatusMenu()
                                .setModeChecked(null);
                    }
                } );
            int result = JOptionPane.showConfirmDialog(
                            null,
                            resources.getString( "connectCountTooHigh" ),
                            "JBother", JOptionPane.YES_NO_OPTION);

            connectCount = 0;

            if( result == JOptionPane.YES_OPTION )
            {
                run();
                return;
            }

            cancelled = true;
            return;
        }

        if (password == null) {
            PasswordDialog dialog = new PasswordDialog(BuddyList.getInstance().getContainerFrame(),resources
                    .getString("jabberPassword"));
            password = dialog.getText();
        }

        if (gnupgSecretKey != null && !JBotherLoader.isGPGEnabled()) {
            int result = JOptionPane
                    .showConfirmDialog(
                            null,
                            "Warning: There is a GnuPG secrety key ID in your profile,\nbut it appears as though GnuPG is not installed on this system.\nWould you still like to connect to the server?",
                            "GnuPG", JOptionPane.YES_NO_OPTION);

            if (result != JOptionPane.YES_OPTION) {
                BuddyList.getInstance().getStatusMenu()
                        .stopBlinkTimer();
                BuddyList.getInstance().init(null);
                return;
            }
        }

        else if ((gnupgSecretKey != null && JBotherLoader.isGPGEnabled())
                && (BuddyList.getInstance().getGnuPGPassword() == null)) {
            GnuPG gnupg = new GnuPG();
            while (true) {
                PasswordDialog dialog = new PasswordDialog(BuddyList.getInstance().getContainerFrame(),resources
                        .getString("gnupgKeyPassword"));
                gnupgTempPass = dialog.getText();
                if ((gnupgTempPass != null)
                        && (gnupg.sign("1", gnupgSecretKey, gnupgTempPass))) {
                    BuddyList.getInstance().setGnuPGPassword(gnupgTempPass);
                    break;
                } else {
                    BuddyList.getInstance().getStatusMenu()
                            .stopBlinkTimer();
                    Standard
                            .warningMessage(null, "GnuPG Error",
                                    "Wrong GnuPG passphrase! Please, try connecting again.");
                    BuddyList.getInstance().init(null);
                    BuddyList.getInstance().setGnuPGPassword(null);
                    return;
                }
            }
        }

        if (cancelled) {
            BuddyList.getInstance().getStatusMenu()
                    .stopBlinkTimer();
            cancelled = false;
            connectCount = 0;
            return;
        }

        try {
            XMPPConnection.DEBUG_ENABLED = true;
            if (com.valhalla.settings.Arguments.getInstance().getProperty("smackdebug") != null) {
            }else {
                System.setProperty("smack.debuggerClass", "com.valhalla.jbother.jabber.Debugger");
            }

            int port = this.port;
            if (port == 0 && ssl) {
                port = 5223;
            } else if (port == 0 && !ssl) {
                port = 5222;
            }

            connection = null;

            //smart user@service with different servername
            //talk.google.com users use username@gmail.com and talk.google.com for servername
            int b = 0;
            String service = null;
            if( ( b = username.indexOf("@") ) != -1){
                service = username.substring(b+1);
                username = username.substring(0, b);
            }   
            
            if(proxy){
                if ( service == null){
                    connection = new XMPPConnection(server, port, server, new ProxySocketFactory(proxyhost, proxyport));
                } else connection = new XMPPConnection(server, port, service, new ProxySocketFactory(proxyhost, proxyport));  
            }else if (ssl){
                if ( service == null){
                    connection = new SSLXMPPConnection(server, port);
                } else connection = new SSLXMPPConnection(server, port, service);
            }else{
                if ( service == null){
                    connection = new XMPPConnection(server, port);
                } else connection = new XMPPConnection(server, port, service);
            }

            BuddyList.getInstance().init(connection);
            BuddyList.getInstance().clearTree();
        }catch(Exception ex){
            errorMessage = ex.getMessage();
        }

        if(cancelled){
            BuddyList.getInstance().getStatusMenu().stopBlinkTimer();
            cancelled = false;

            return;
        }

        // get the resource from the login box
        String tmp = resource;
        if (tmp == null || tmp.equals("")) {
            tmp = "JBother";
        }
        final String resource = tmp;

        if (errorMessage == null && connection != null) {
            PacketFilter anyFilter = new PacketFilter() {
                public boolean accept(Packet packet) {
                    return true;
                }
            };

            // sets up the various packet listeners
            PacketFilter filter = new PacketTypeFilter(Presence.class);

            connection.addPacketListener(new PresencePacketListener(), filter);
            filter = new PacketTypeFilter(Message.class);
            connection.addPacketListener(messageListener, filter);
            connection.addConnectionListener(conListener);
            filter = new PacketTypeFilter(Version.class);
            connection.addPacketListener(new VersionListener(), filter);
            filter = new PacketTypeFilter(com.valhalla.jbother.jabber.smack.LastActivity.class);
            connection.addPacketListener(new LastActivityListener(), filter);
            filter = new PacketTypeFilter(Time.class);
            connection.addPacketListener(new TimeListener(), filter);
            connection.addPacketListener(new IQPacketListener(), filter);
            ftmanager = new FileTransferManager(connection);
            ftmanager.addFileTransferListener(new FTReceiveListener());

            exchangeManager = new RosterExchangeManager(connection);
            exchangeManager.addRosterListener(new ExchangeListener());

            // this filter will listen to three types of messages:
            // <si>, <streamhost> and <streamhost-used>
            /*filter = new OrFilter(new PacketTypeFilter(Streamhost.class),
                    new PacketTypeFilter(StreamhostUsed.class));
            OrFilter filter2 = new OrFilter(new PacketTypeFilter(
                    StreamInitiation.class), filter);
            connection.addPacketListener( new StreamInitiationListener(),
             filter2 );*/

            // attempts to connect
            try {
                connection.login(username, password, resource);

                //SmackConfiguration.setPacketReplyTimeout(5000);

                roster = connection.getRoster();

                roster.setSubscriptionMode(
                        Roster.SUBSCRIPTION_MANUAL);
                roster.addRosterListener( rosterListener );

                eventManager = new MessageEventManager(connection);

                eventManager
                        .addMessageEventNotificationListener(new EventNotificationListener());
                eventManager
                        .addMessageEventRequestListener(new EventRequestListener());
                MultiUserChat.addInvitationListener(connection,
                        new InvitationPacketListener());
            } catch (XMPPException e) {
                errorMessage = e.getMessage();
                if (e.getXMPPError() != null) {
                    errorMessage = resources.getString("xmppError"
                            + e.getXMPPError().getCode());
                }
            }
        }

        // if there was an error, display it, and then redisplay a LoginDialog
        if (errorMessage != null || connection == null) {
            resetCredentials();

            SwingUtilities.invokeLater( new Runnable()
                {
                    public void run()
                    {
                        BuddyList.getInstance().getStatusMenu()
                                .setModeChecked(null);
                    }
                } );

            if (connection != null) {
                connection.removeConnectionListener(conListener);
            }

            if (persistent) {
                try {
                    Thread.sleep(connectCount*5000);
                } catch (InterruptedException ex) {
                    com.valhalla.Logger.logException(ex);
                }
                com.valhalla.Logger.debug("Connection error was: "
                        + errorMessage);
                errorMessage = null;

                if (cancelled) {
                    cancelled = false;

                    BuddyList.getInstance().getStatusMenu()
                            .stopBlinkTimer();
                    connectCount = 0;
                    return;
                }

                messageListener.resetQueue();
                com.valhalla.Logger.debug( "Retrying, attempt #" + connectCount );
                connectCount++;

                run();
                return;
            }

            password = Settings.getInstance().getProperty("password");

            BuddyList.getInstance().getStatusMenu()
                    .stopBlinkTimer();

            if (errorMessage == null) {
                errorMessage = resources.getString("connectionError");
            }
            if (errorMessage.equals("Unauthorized")) {
                errorMessage = new String(resources
                        .getString("invalidPassword"));
            }

            Standard.warningMessage(null, resources
                    .getString("couldNotConnect"), errorMessage);
            BuddyList.getInstance().init(null);

            return;
        }

        SwingUtilities.invokeLater( new Runnable() {
                public void run()
                {
                    BuddyList.getInstance().getStatusMenu().stopBlinkTimer();
                    BuddyList.getInstance().getBuddiesMenu().logOn();

                    // otherwise, set up and display the buddy list
                    com.valhalla.Logger.debug("Connected");

                    BuddyList.getInstance().resetAwayTimer();

                    BuddyList.getInstance().getStatusMenu().setModeChecked(
                            connectMode);
                    BuddyList.getInstance().initBuddies();

                    // display the buddies
                    BuddyList.getInstance().setStatus(connectMode, statusString, false);
                    messageListener.startTimer();

                    if (away) {
                        BuddyList.getInstance().getAwayHandler().actionPerformed(
                                new ActionEvent(BuddyList.getInstance(), 1, "away"));

                    }
                }
        } );

        return;
    }
}

