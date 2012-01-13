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

import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.swing.*;

import com.valhalla.gui.*;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.plugins.events.MessageReceivedEvent;
import com.valhalla.settings.Settings;
import net.infonode.tabbedpanel.*;
import net.infonode.tabbedpanel.titledtab.*;
import net.infonode.util.*;
import org.jivesoftware.smack.packet.*;


/**
 * Provides common tools for conversation windows (such as logging). Must be
 * extended.
 *
 * @author Adam Olsen
 * @author Yury Soldak (tail)
 * @author Andrey Zakirov
 * @created April 10, 2005
 * @version 1.1
 * @see com.valhalla.jbother.jabber.BuddyStatus
 */
public abstract class ConversationPanel extends JPanel implements
        LogViewerCaller, TabFramePanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    protected BuddyStatus buddy;
    private TitledTab tab;
    protected Message lastReceived = null;
    MJTextArea textEntryArea = new MJTextArea(true);

    public void ConversationPanel(BuddyStatus buddy) {this.buddy = buddy;}

    /**
     * the conversation area
     */
    protected ConversationArea conversationArea = new ConversationArea();

    private int oldMaximum = 0;

    /**
     * the close timer
     */
    protected javax.swing.Timer timer = new javax.swing.Timer(600000,
            new CloseListener());

    public JFrame getFrame() { return frame; }

    /**
     * the containing frame
     */
    protected JFrame frame = null;

    /**
     * if the listeners have been added
     */
    protected boolean listenersAdded = false;
    public void setTab( TitledTab tab ) { this.tab = tab; }
    public TitledTab getTab() { return tab; }

    /**
     * Sets up the defaults in the ConversationPanel
     *
     * @param buddy
     *            the buddy that this window corresponds to
     */
    public ConversationPanel(BuddyStatus buddy) {
        this.buddy = buddy;
        startLog();
    }



    /**
     * Listens for a right click - and displays a menu if it's caught
     *
     * @author Adam Olsen
     * @created October 26, 2004
     * @version 1.0
     */
    class RightClickListener extends MouseAdapter {
        private JPopupMenu menu;

        private JMenuItem item = new JMenuItem(resources
                .getString("closeConversation"));

        private boolean containsCloseItem = false;

        /**
         * Constructor for the RightClickListener object
         *
         * @param menu
         *            the menu
         */
        public RightClickListener(JPopupMenu menu) {
            this.menu = menu;
        }

        /**
         * Description of the Method
         *
         * @param e
         *            the event
         */
        public void mousePressed(MouseEvent e) {
            checkPop(e);
        }

        /**
         * @param e
         *            the mouse event
         */
        public void mouseReleased(MouseEvent e) {
            checkPop(e);
        }

        /**
         * @param e
         *            the mouse event
         */
        public void mouseClicked(MouseEvent e) {
            checkPop(e);
        }

        /**
         * @param e
         *            the mouse event
         */
        public void checkPop(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (conversationArea.getSelectedText() != null)
                    return;
                if (Settings.getInstance().getBoolean("useTabbedWindow")
                        && !containsCloseItem) {
                    containsCloseItem = true;
                    menu.add(item);
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            checkCloseHandler();
                        }
                    });
                } else if (!Settings.getInstance()
                        .getBoolean("useTabbedWindow")
                        && containsCloseItem) {
                    containsCloseItem = false;
                    menu.remove(item);
                }

                // if a right click is detected, show the popup menu
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /**
     * @return true if the TabFrame panel listeners have already been added to
     *         this panel
     */
    public boolean listenersAdded() {
        return listenersAdded;
    }

    /**
     * Sets whether or not the TabFrame panel listeners have been added
     *
     * @param added
     *            true if they have been added
     */
    public void setListenersAdded(boolean added) {
        this.listenersAdded = added;
    }

    /**
     * @return the input area of this panel
     */
    public JComponent getInputComponent() {
        return conversationArea;
    }
   
    /**
     * @return A string containing the tooltip that should be displayed for
     * this tab
     */
    public String getPanelToolTip()
    {
        return getWindowTitle();
    }

    /**
     * @return the name of the tab in the TabFrame
     */
    public String getPanelName() {
        if (buddy != null) {
            String n = buddy.getName();
            if (n == null)
                n = buddy.getUser();
            int i = n.indexOf("@");
            if (i > -1)
                n = n.substring(0, i);
            n = n.replaceAll("%.*", "");
            if (n.length() >= 10 )
            {
                n = n.substring(0, 7) + "...";
            }
            return n;
        } else {
            return "blank";
        }
    }

    /**
     * @return the title of the window in the TabFrame
     */
    public String getWindowTitle() {
        if (buddy != null) {
            if( buddy.getName() == null )
            {
                return buddy.getUser();
            }

            if ((buddy.getName().toLowerCase ()).matches(buddy.getUser()))
            {
                return buddy.getName();
            }
            else
            {
                return buddy.getName() +  " (" + buddy.getUser() + ")";
            }
        }
        else {
            return "Blank Message";
        }
    }

    /**
     * @return the tooltip for this tab in the TabFrame
     */
    public String getTooltip() {
        if (buddy != null) {
            return buddy.getUser();
        } else {
            return "Blank Message";
        }
    }

    /**
     * @return the containing frame for this panel
     */
    public JFrame getContainingFrame() {
        return frame;
    }

    /**
     * @param frame
     *            the new containing frame
     */
    public void setContainingFrame(JFrame frame) {
        this.frame = frame;
    }

    /**
     * Returns the buddy status for this dialog
     *
     * @return the buddy passed in to the constructor
     */
    public BuddyStatus getBuddy() {
        return buddy;
    }

    /**
     * Listens for a close event, and either makes the dialog hidden or removes
     * it from the dialog tracker
     *
     * @author Adam Olsen
     * @created October 26, 2004
     * @version 1.0
     */
    class CloseListener implements ActionListener {
        /**
         * @param e
         *            the event
         */
        public void actionPerformed(ActionEvent e) {
            timer.stop();
            closeHandler();
            timer = null;
        }
    }

    /**
     * Checks to see if we are preserving messages. If so, it starts the timer,
     * otherwise it just closes the panel
     */
    public void checkCloseHandler() {
        if (Settings.getInstance().getProperty("preserveMessages") == null) {
            closeHandler();
        } else {
            startTimer();
            if (!Settings.getInstance().getBoolean("useTabbedWindow")) {
                frame.setVisible(false);
            } else {
                BuddyList.getInstance().removeTabPanel(this);
            }
        }
    }

    /**
     * Destroys the dialog, disposes the containing frame if there is one and
     * removes the panel from the TabFrame if required.
     */
    public void closeHandler() {
        if(this instanceof ChatPanel) ((ChatPanel)this).removeScroll();
        textEntryArea.getInputMap().clear();
        textEntryArea = null;
        closeLog();
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }

        if (buddy != null) {
            com.valhalla.Logger.debug("Closing ConversationPanel for "
                    + buddy.getUser());
            buddy.setConversation(null);
        }

        if (Settings.getInstance().getBoolean("useTabbedWindow")) {
            BuddyList.getInstance().removeTabPanel(this);
        }

        timer.stop();
        com.valhalla.Logger.debug("timer is now null");
        timer = null;

        MessageDelegator.getInstance().removePanel(this);
    }

    /**
     * Opens a <code>com.valhalla.jbother.LogViewerDialog</code>
     */
    public void openLogWindow() {
        new LogViewerDialog(this, buddy.getUser());
    }

    /**
     * Opens a log and starts it.
     */
    public void startLog() {
        if (buddy == null)
            return;
        // for loggingg
        if (Settings.getInstance().getBoolean("keepLogs")) {
            String logFileName = LogViewerDialog.getDateName() + ".log";
            String logFileDir = JBother.profileDir
                    + File.separatorChar
                    + "logs"
                    + File.separatorChar
                    + this.buddy.getUser().replaceAll("@", "_at_").replaceAll(
                            "\\/", "-");
            File logDir = new File(logFileDir);

            if (!logDir.isDirectory() && !logDir.mkdirs()) {
                Standard.warningMessage(this, resources.getString("log"),
                        resources.getString("couldNotCreateLogDir"));
            }

            String logEnc =
                Settings.getInstance().getProperty("keepLogsEncoding");
            conversationArea.setLogFile(new File(logDir, logFileName), logEnc);

        }
    }

    /**
     * Sets a message to offline (displays "this message is offline")
     */
    public void setOfflineMessage() {
    }

    /**
     * @return a String representing the current time the format:
     *         [Hour:Minute:Second]
     * @param d
     *            the date stamp to use
     */
    public static String getDate(Date d) {
        if (d != null) {
            // calculation of the offset is no longer needed
            // perhaps a smack change?
            //d = new Date(d.getTime()
              //      + TimeZone.getDefault().getOffset(d.getTime()));
        } else {
            d = new Date();
        }

        Calendar mtime = Calendar.getInstance();
        mtime.setTime(d);

        Calendar today = Calendar.getInstance();
        today.setTime(new Date());

        SimpleDateFormat formatter = new SimpleDateFormat("[HH:mm:ss]");

        if (mtime.get(Calendar.MONTH) != today.get(Calendar.MONTH)
                || mtime.get(Calendar.DAY_OF_MONTH) != today
                        .get(Calendar.DAY_OF_MONTH)
                || mtime.get(Calendar.YEAR) != today.get(Calendar.YEAR)) {
            formatter = new SimpleDateFormat("[MM/dd@HH:mm]");
        }

        String date = formatter.format(d);
        return date;
    }

    /**
     * Receives a message
     *
     * @param sbj
     *            the subject of the message
     * @param body
     *            the message body
     * @param resource
     *            the message resource
     * @param date
     *            the timestamp when the message was received
     */
    public void receiveMessage(String sbj, String delayInfo, String body, String resource,
            Date date, boolean decryptedFlag, boolean verifiedFlag) {
        receiveMessage();
    }

    /**
     * Calls the received message events
     */
    public void receiveMessage() {
        MessageDelegator.getInstance().showPanel(this);


        stopTimer();

        JFrame f = frame;
        boolean isFocused = true;
        if (Settings.getInstance().getBoolean("useTabbedWindow")) {
            f = BuddyList.getInstance().getTabFrame();
            BuddyList.getInstance().getTabFrame().markTab(this, false);
            isFocused = f.isFocused();
        } else {
            if (!frame.isVisible()) {
                frame.setVisible(true);
            } else if (Settings.getInstance().getBoolean("focusWindow")) {
                isFocused = frame.isFocused();
                frame.toFront();
            } else
                isFocused = frame.isFocused();
        }


        if (Settings.getInstance().getBoolean("usePopup")
            // && !isFocused
                && f != null) {
            if (buddy != null && buddy.getName() != null) {
                NotificationPopup.showSingleton(f, resources
                        .getString("messageReceived"), "<b>"
                        + resources.getString("from") + ":</b>&nbsp;&nbsp;"
                        + buddy.getName(),this);
            } else {
                NotificationPopup.showSingleton(f, resources
                        .getString("messageReceived"), "Message Received",this);
            }
        }

        com.valhalla.jbother.sound.SoundPlayer.play("receivedSound");

    }

    public void setLastReceivedMessage(Message message) {
        this.lastReceived = message;

        MessageReceivedEvent event = new MessageReceivedEvent(this);

        Message newMessage = new Message( message.getTo(), message.getType() );
        newMessage.setSubject(message.getSubject());
        newMessage.setBody(message.getBody());
        newMessage.setThread(message.getThread());
        event.setMessage(newMessage);
        com.valhalla.pluginmanager.PluginChain.fireEvent(event);
    }

    /**
     * Abstract createFrame - creates the containing frame of this panel
     */
    public abstract void createFrame();

    /**
     * Stops the close timer
     */
    public void stopTimer() {
        timer.stop();
    }

    /**
     * Starts the closet imer
     */
    public void startTimer() {
        timer.start();
    }

    /**
     * Displays a "disconnected" message"
     */
    public void disconnected() {
        conversationArea.append(getDate(null) + " **** " + resources.getString("disconnected"), ConversationArea.BLACK, true);
    }


    public void updateStyle(Font font){}

    /**
     * Closes the log file
     */
    public void closeLog() {
        conversationArea.closeLog();
    }

    public void finailize()
    {
        String user = "";
        if(buddy != null) user = buddy.getUser();
        com.valhalla.Logger.debug("Finalize called for conversationpanel " + user);
    }

}

