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

package com.valhalla.jbother.groupchat;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.MUCUser;

import com.valhalla.gui.*;
import com.valhalla.jbother.*;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.MUCBuddyStatus;
import com.valhalla.jbother.jabber.smack.InvitationRejectionPacketListener;
import com.valhalla.jbother.plugins.events.MUCEvent;
import com.valhalla.pluginmanager.PluginChain;
import com.valhalla.settings.Settings;
import net.infonode.tabbedpanel.*;
import net.infonode.tabbedpanel.titledtab.*;
import net.infonode.util.*;

/**
 * This is the panel that contains a groupchat conversation. It is placed in a
 * JTabbedPane in GroupChat frame.
 *
 * @author Adam Olsen
 */
public class ChatRoomPanel extends JPanel implements LogViewerCaller,
        TabFramePanel, UserChooserListener {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private MJTextArea textEntryArea = new MJTextArea(true,2, 0);

    private StringBuffer conversationText = new StringBuffer();

    private ConversationArea conversationArea = new ConversationArea();

    private JMenuItem logItem = new JMenuItem(resources.getString("viewLog")),
            newItem = new JMenuItem(resources.getString("joinRoom")),
            leaveItem = new JMenuItem(resources.getString("leaveRoom")),
            nickItem = new JMenuItem(resources.getString("changeNickname")),
            registerItem = new JMenuItem(resources.getString("registerForRoom")),
            viewAdmins = new JMenuItem(resources.getString("viewAdmins")),
            viewModerators = new JMenuItem(resources
                    .getString("viewModerators")), viewMembers = new JMenuItem(
                    resources.getString("viewMembers")),
            viewParticipants = new JMenuItem(resources
                    .getString("viewParticipants")),
            viewOwners = new JMenuItem(resources.getString("viewOwners")),
            viewOutcasts = new JMenuItem(resources.getString("viewOutcasts")),
            destroyRoom = new JMenuItem(resources.getString("destroyRoom")),
            invite = new JMenuItem(resources.getString("inviteUser"));

    private JPopupMenu popMenu = new JPopupMenu();
    private TitledTab tab;

    private int oldMaximum = 0;

    private JPanel scrollPanel = new JPanel(new GridLayout(1, 0));

    private JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private MultiUserChat chat;

    private String chatroom, nickname, pass;

    private Hashtable buddyStatuses = new Hashtable();

    private GroupChatNickList nickList;

    private String subject = resources.getString("noSubject");

    private MJTextField subjectField = new MJTextField();

    private boolean listenersAdded = false;

    private GroupParticipantListener participantListener = new GroupParticipantListener(this);

    private InvitationRejectionPacketListener invitationRejectionPacketListener = new InvitationRejectionPacketListener();

    private GroupChatMessagePacketListener messageListener = new GroupChatMessagePacketListener(
            this);

    private SubjectListener subjectListener = new SubjectListener(this);

    private StatusListener statusListener = new StatusListener(this);

    private UserStatusListener userStatusListener = new UserStatusListener(this);

    private boolean messageToMe = false;
    private boolean removed = false;
    private int joins = 0;

    /**
     * This sets up the appearance of the chatroom window
     *
     * @param chatroom
     *            the chatroom address
     * @param nickname
     *            the nickname to use when joining
     */
    public ChatRoomPanel(String chatroom, String nickname, String pass) {
        this.chatroom = chatroom;
        this.nickname = nickname;
        this.pass = pass;
        chat = new MultiUserChat(BuddyList.getInstance().getConnection(),
                chatroom);

        BuddyList.getInstance().startTabFrame();

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        subjectField.setText(resources.getString("noSubject"));

        JPanel subjectPanel = new JPanel();
        subjectPanel.setLayout(new BorderLayout());
        subjectPanel.add(new JLabel("<html><b>"
                + resources.getString("subject") + ":&nbsp;&nbsp;</b></html>"),
                BorderLayout.WEST);
        subjectPanel.add(subjectField, BorderLayout.CENTER);

        add(subjectPanel, BorderLayout.NORTH);

        add(mainPanel);

        nickList = new GroupChatNickList(this);

        String divLocString = Settings.getInstance().getProperty(
                "chatWindowDividerLocation");
        int divLoc = 0;
        Dimension dimension = BuddyList.getInstance().getTabFrame().getSize();

        if (divLocString != null) {
            divLoc = Integer.parseInt(divLocString);
        } else {
            divLoc = (int) dimension.getWidth() - 127;
        }

        if (divLoc == 0)
            divLoc = (int) dimension.getWidth() - 127;

        mainPanel.setDividerLocation(divLoc);
        mainPanel.setOneTouchExpandable(true);
        mainPanel.setResizeWeight(1);
        mainPanel.addPropertyChangeListener("lastDividerLocation",
                new DividerListener("chatWindowDividerLocation"));

        conversationArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setUpPopMenu();

        scrollPanel.add(conversationArea);

        JSplitPane containerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scrollPanel, new JScrollPane(textEntryArea));
        containerPanel.setResizeWeight(1);

        textEntryArea.setLineWrap(true);
        textEntryArea.setWrapStyleWord(true);

        divLocString = Settings.getInstance().getProperty(
                "chatRoomPanelDividerLocation");
        divLoc = 0;

        try {
            if (divLocString != null) {
                divLoc = Integer.parseInt(divLocString);
            } else {
                divLoc = (int) dimension.getWidth() - 100;
                Settings.getInstance().setProperty(
                        "chatRoomPanelDividerLocation", divLoc + "" );
            }
        } catch (NumberFormatException ex) {
        }

        if (divLoc == 0)
            divLoc = 290;
        containerPanel.setDividerLocation(divLoc);
        containerPanel.addPropertyChangeListener("lastDividerLocation",
                new DividerListener("chatRoomPanelDividerLocation"));
        containerPanel.repaint();

        mainPanel.add(containerPanel);
        mainPanel.add(nickList);

        textEntryArea.grabFocus();
        textEntryArea.setFocusTraversalKeysEnabled(false); //for disable focus
                                                           // traversal with TAB
        setSubject(subject);

        addListeners();
    }

    public void removed() { removed = true; }

    public void updateStyle(Font font){}

    public void setTab( TitledTab tab ) { this.tab = tab; }
    public TitledTab getTab() { return tab; }

    public ConversationArea getConversationArea() {
        return conversationArea;
    }

    public GroupChatNickList getGroupChatNickList() {
        return nickList;
    }

    public void removeMe()
    {
	nickList.removeBuddy(chatroom + "/" + nickname);
    }

    public void disconnect() {
        if(nickList != null) nickList.clear();
    }

    public void doAction(String command, MUCBuddyStatus buddy) {
        MUCUser user = buddy.getMUCUser();
        if (user == null) {
            serverErrorMessage(resources.getString("jidNotFound"));
            return;
        }

        MUCUser.Item item = user.getItem();

        if (item == null) {
            serverErrorMessage(resources.getString("jidNotFound"));
            return;
        }

        if (item.getJid() == null) {
            serverErrorMessage(resources.getString("jidNotFound"));
            return;
        }

        com.valhalla.Logger.debug("Running " + command + " on "
                + buddy.getUser());
        Thread thread = new Thread(new RunTaskThread(resources
                .getString("error")
                + ": ", command, item.getJid()));
        thread.start();
    }

    public String getUser() {
        return chat.getRoom() + "/" + chat.getNickname();
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
        return textEntryArea;
    }

    /**
     * @return the JList representing the nicklist
     */
    public JList getNickList() {
        return nickList.getList();
    }

    /**
     * @return the text entry area
     */
    public JTextComponent getTextEntryArea() {
        return textEntryArea;
    }

    /**
     * Listens for a change in the divider location, and saves it for later
     * retreival
     *
     * @author Adam Olsen
     * @version 1.0
     */
    private class DividerListener implements PropertyChangeListener {
        String prop;

        public DividerListener(String prop) {
            this.prop = prop;
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (e.getOldValue().toString().equals("-1"))
                return;
            Settings.getInstance()
                    .setProperty(prop, e.getOldValue().toString());
            BuddyList.getInstance().getTabFrame().saveStates();
        }
    }


    /**
     * Look for a right click, and show a pop up menu
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class RightClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            checkPop(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkPop(e);
        }

        public void mouseClicked(MouseEvent e) {
            checkPop(e);
        }

        public void checkPop(MouseEvent e) {
            // look for the popup trigger.. usually a right click
            if (e.isPopupTrigger()) {
                if (conversationArea.getSelectedText() == null) {
                    popMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }

    /**
     * Add the various menu items to the popup menu
     */
    private void setUpPopMenu() {
        MenuItemListener listener = new MenuItemListener();

        conversationArea.getTextPane().addMouseListener(new RightClickListener());
        CopyPasteContextMenu.registerComponent(conversationArea.getTextPane());

        popMenu.add(nickItem);
        popMenu.add(newItem);
        popMenu.add(logItem);

        popMenu.addSeparator();

        popMenu.add(viewAdmins);
        popMenu.add(viewModerators);
        popMenu.add(viewMembers);
        popMenu.add(viewParticipants);
        popMenu.add(viewOwners);
        popMenu.add(viewOutcasts);
        popMenu.add(invite);
        popMenu.add(registerItem);
        popMenu.add(destroyRoom);

        popMenu.addSeparator();
        popMenu.add(leaveItem);

        logItem.addActionListener(listener);
        newItem.addActionListener(listener);
        leaveItem.addActionListener(listener);
        nickItem.addActionListener(listener);
        registerItem.addActionListener(listener);
        viewAdmins.addActionListener(listener);
        viewOutcasts.addActionListener(listener);
        viewMembers.addActionListener(listener);
        invite.addActionListener(listener);
        viewParticipants.addActionListener(listener);
        viewOwners.addActionListener(listener);
        viewModerators.addActionListener(listener);
        destroyRoom.addActionListener(listener);
    }

    /**
     * Listens for items to be selected in the menu
     *
     * @author Adam Olsen
     * @version 1.0
     */
    private class MenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == nickItem)
                changeNickHandler();
            else if (e.getSource() == leaveItem) {
                BuddyList.getInstance().getTabFrame().removePanel(ChatRoomPanel.this);
                BuddyList.getInstance().stopTabFrame();
            } else if (e.getSource() == newItem) {
                GroupChatBookmarks gc = new GroupChatBookmarks(BuddyList
                        .getInstance().getTabFrame());
                gc.load();
                gc.setVisible(true);
                gc.toFront();
            } else if (e.getSource() == logItem)
                new LogViewerDialog(ChatRoomPanel.this, getRoomName());
            else if (e.getSource() == registerItem)
                configurationHandler("registerFor");
            else if (e.getSource() == viewAdmins)
                new ListViewDialog(ChatRoomPanel.this, ListViewDialog.TYPE_ADMIN);
            else if (e.getSource() == viewOutcasts)
                new ListViewDialog(ChatRoomPanel.this, ListViewDialog.TYPE_OUTCASTS);
            else if (e.getSource() == viewMembers)
                new ListViewDialog(ChatRoomPanel.this, ListViewDialog.TYPE_MEMBERS);
            else if (e.getSource() == viewParticipants)
                new ListViewDialog(ChatRoomPanel.this,
                        ListViewDialog.TYPE_PARTICIPANTS);
            else if (e.getSource() == viewOwners)
                new ListViewDialog(ChatRoomPanel.this, ListViewDialog.TYPE_OWNERS);
            else if (e.getSource() == invite)
            {
                UserChooser chooser = new UserChooser(BuddyList.getInstance().getTabFrame(), resources.getString("inviteUser"));
                chooser.addListener(ChatRoomPanel.this);
                chooser.setVisible(true);
            }

            else if (e.getSource() == viewModerators)
                new ListViewDialog(ChatRoomPanel.this, ListViewDialog.TYPE_MODERATORS);
            else if (e.getSource() == destroyRoom)
                destroyHandler();
        }
    }

    public void usersChosen(UserChooser.Item[] items)
    {
        usersChosen((Object[])items);
    }

    /**
     *  Description of the Method
     *
     * @param  array  Description of the Parameter
     */
    public void usersChosen(Object[] us) {
        String result = (String) JOptionPane.showInputDialog(BuddyList.getInstance().getTabFrame(), resources
                .getString("enterReasonForInvite"), resources
                .getString("inviteUser"), JOptionPane.QUESTION_MESSAGE, null,
                null, "Come join us!");


         if(result == null || result.equals("")) return;

         if(us[0] instanceof String)
         {
            inviteUsers(new String[] {(String)us[0]}, result);
            return;
         }

         ArrayList u = new ArrayList();
         for(int i = 0; i < us.length; i++)
         {
             UserChooser.Item item = (UserChooser.Item)us[i];
             u.add(item.getJID());
         }
         inviteUsers((String[])u.toArray(new String[u.size()]), result);
    }


    protected void inviteUsers(final String[] users, final String reason) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for(int i = 0; i < users.length; i++)
                {
                    chat.invite(users[i], reason);
                }
            }
        });

        thread.start();

    }

    private void destroyHandler() {
        final int r = JOptionPane.showConfirmDialog(BuddyList.getInstance()
                .getTabFrame(), resources.getString("sureDestroyRoom"),
                resources.getString("destroyRoom"), JOptionPane.YES_NO_OPTION);

        if (r == JOptionPane.YES_OPTION) {
            final String reason = (String) JOptionPane.showInputDialog(
                    BuddyList.getInstance().getTabFrame(), resources
                            .getString("enterReasonForDestroy"), resources
                            .getString("destroyRoom"),
                    JOptionPane.QUESTION_MESSAGE, null, null,
                    "Room has been moved");

            if (reason == null)
                return;

            final String result = (String) JOptionPane.showInputDialog(
                    BuddyList.getInstance().getTabFrame(), resources
                            .getString("enterAlternate"), resources
                            .getString("destroyRoom"),
                    JOptionPane.QUESTION_MESSAGE, null, null, "");

            if (result == null)
                return;

            new Thread(new DestroyThread(reason, result)).start();
        }
    }

    class DestroyThread implements Runnable {
        String reason, result;

        public DestroyThread(String reason, String result) {
            this.reason = reason;
            this.result = result;
        }

        public void run() {
            String error = null;
            try {
                chat.destroy(reason, result);
            } catch (XMPPException ex) {
                error = ex.getMessage();
            }

            final String e = error;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (e != null) {
                        serverErrorMessage(e);
                    } else {
                        serverErrorMessage("Room has been destroyed");
                    }
                }
            });
        }
    }

    /**
     * Collects a Data Form to be filled out
     *
     * @param type
     *            the type of form. Either "configure" or "registerFor"
     */
    public void configurationHandler(String type) {
        Thread thread = new Thread(new ConfigThread(type));
        thread.start();
    }

    /**
     * Collects the data form and displays it.
     *
     * @author Adam Olsen
     */
    private class ConfigThread implements Runnable {
        private String type = "configure";

        /**
         * @param type
         *            the type of form to collect.
         */
        public ConfigThread(String type) {
            this.type = type;
        }

        /**
         * Called by the enclosing thread
         */
        public void run() {
            try {
                Form temp;

                // get the form
                if (type.equals("configure")) {
                    temp = chat.getConfigurationForm();
                } else {
                    temp = chat.getRegistrationForm();
                }

                if (temp == null) {
                    serverErrorMessage(resources
                            .getString("couldNotCollectForm"));
                    return;
                }

                final Form form = temp;
                final JBDataForm f = new JBDataForm(BuddyList.getInstance().getTabFrame(), form);
                f.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // if the cancel button is pressed, close the form
                        if (e.getActionCommand().equals("cancel")) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    f.dispose();
                                }
                            });
                        }

                        // else submit the form
                        else if (e.getActionCommand().equals("ok")) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (submitConfigurationForm(f, type)) {
                                        f.setVisible(false);
                                    }
                                }
                            });
                        }
                    }
                });

                // show the form
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        f.setVisible(true);
                    }
                });
            }

            // if there was an error collecting the form
            catch (XMPPException ex) {
                String message = ex.getMessage();
                if (ex.getXMPPError() != null) {
                    message = resources.getString("xmppError"
                            + ex.getXMPPError().getCode());
                }

                serverErrorMessage(resources.getString(type + "Room") + ": "
                        + message);
            }
        }
    }

    /**
     * Submits the form
     *
     * @param form
     *            the form to submit
     * @param type
     *            the type of form, either "configure" or "registerFor"
     * @return false if the required fields have not been filled out
     */
    private boolean submitConfigurationForm(JBDataForm form, final String type) {
        final Form answer = form.getAnswerForm();
        if (answer == null)
            return false;

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    if (type.equals("configure")) {
                        chat.sendConfigurationForm(answer);
                    } else {
                        chat.sendRegistrationForm(answer);
                    }
                } catch (XMPPException ex) {
                }

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (type.equals("configure")) {
                            serverNoticeMessage(resources
                                    .getString("configureSubmitted"));
                        } else {
                            serverNoticeMessage(resources
                                    .getString("registerSubmitted"));
                        }
                    }
                });
            }
        });
        thread.start();

        return true;
    }

    /**
     * Opens the log window for this chat room
     */
    public void openLogWindow() {
        new LogViewerDialog(this, getRoomName());
    }

    /**
     * Adds a buddy to the nickname list
     *
     * @param buddy
     *            the buddy to add
     */
    public void addBuddy(String buddy) {
        if( nickList == null || buddy == null ) return;
        nickList.addBuddy(buddy);
    }

    /**
     * Removes a buddy from the nick list
     *
     * @param buddy
     *            the buddy to remove
     */
    public void removeBuddy(String buddy) {
        if( nickList == null || buddy == null ) return;

        nickList.removeBuddy(buddy);
    }

    /**
     * Opens the log file
     */
    public void startLog() {
        // for loggingg
        if (Settings.getInstance().getBoolean("keepLogs")) {
            String logFileName = LogViewerDialog.getDateName() + ".log";
            String logFileDir = JBother.profileDir
                    + File.separatorChar
                    + "logs"
                    + File.separatorChar
                    + getRoomName().replaceAll("@", "_at_").replaceAll("\\/",
                            "-");

            File logDir = new File(logFileDir);

            if (!logDir.isDirectory() && !logDir.mkdirs())
                Standard.warningMessage(this, resources.getString("log"),
                        resources.getString("couldNotCreateLogDir"));

            String logEnc =
                Settings.getInstance().getProperty("keepLogsEncoding");
            conversationArea.setLogFile(new File(logDir, logFileName), logEnc);
        }
    }

    /**
     * Gets the BuddyStatus represending a user in the room
     *
     * @param user the BuddyStatus to get
     * @return the requested BuddyStatus
     */
    public MUCBuddyStatus getBuddyStatus(String user) {
        if (!buddyStatuses.containsKey(user)) {
            MUCBuddyStatus buddy = new MUCBuddyStatus(user);
            buddy.setMUC(chat);
            buddy.setName(user.substring(user.indexOf("/") + 1, user.length()));
            buddyStatuses.put(user, buddy);
        }

        return (MUCBuddyStatus) buddyStatuses.get(user);
    }

    /**
     * @return the tooltip for this panel (when hovering over the tab in the
     * tab frame
     */
    public String getPanelToolTip()
    {
        return getWindowTitle();
    }

    /**
     * Returns the tab name for the TabFramePanel
     *
     * @return the panel name
     */
    public String getPanelName() {
        String n = getShortRoomName().replaceAll( "%.*", "" );
        if (n.length() >= 10 )
        {
            n = n.substring(0, 7) + "...";
        }
        return n;
    }

    /**
     * Returns the tooltip for the tab in the TabFrame
     *
     * @return the tooltip for this tab in the tab frame
     */
    public String getTooltip() {
        return getRoomName();
    }

    /**
     * Returns the window title
     *
     * @return the window title for the TabFrame when this tab is selected
     */
    public String getWindowTitle() {
        return resources.getString("groupChat") + ": " + getRoomName();
    }

    /**
     * Gets the short room name - for example, if you are talking in
     * jdev@conference.jabber.org, it would return "jdev"
     *
     * @return short room name
     */
    public String getShortRoomName() {
        return chatroom.replaceAll("\\@.*", "");
    }

    /**
     * Gets the entire room name, server included
     *
     * @return gets the room address
     */
    public String getRoomName() {
        return chatroom;
    }

    /**
     * Returns the nickname of a user in a group chat.
     *
     * @param id
     *            The full id of someone in a room, ie:
     *            jdev@conference.jabber.org/synic
     * @return the nickname of the person in the room
     */
    public String getNickname(String id) {
        int index = id.indexOf("/");
        if (index == -1)
            return id;
        return id.substring(index + 1);
    }

    public MultiUserChat getChat() {
        return chat;
    }

    public GroupParticipantListener getParticipantListener() {
        return participantListener;
    }

    /**
     * Starts the groupchat. Sets up a thread to connect, and start that thread
     */
    public void startChat() {
        if (!BuddyList.getInstance().checkConnection())
            return;

        serverNoticeMessage("Connecting to " + getRoomName() + " ... ");
        BuddyList.getInstance().addTabPanel(this);

        JoinChatThread t = new JoinChatThread();
        t.start();

        startLog();
    }

    private void leaveChat() {
        if (chat == null)
            return;

        buddyStatuses.clear();
        com.valhalla.Logger.debug("Leaving " + chat.getRoom());
        Presence p = new Presence(Presence.Type.UNAVAILABLE);
        p.setTo(chat.getRoom());
        if (BuddyList.getInstance().checkConnection())
            BuddyList.getInstance().getConnection().sendPacket(p);
        chat.removeMessageListener(messageListener);
        chat.removeParticipantListener(participantListener);
        chat.removeSubjectUpdatedListener(subjectListener);
        chat.removeParticipantStatusListener(statusListener);
        chat.removeUserStatusListener(userStatusListener);
        chat.removeInvitationRejectionListener(invitationRejectionPacketListener);

        chat = null;
        System.gc();
    }

    /**
     * Asks for a new topic, then sets the new topic
     */
    private void topicHandler(final String subject) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    chat.changeSubject(subject);
                } catch (final XMPPException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            String message = e.getMessage();
                            serverErrorMessage(resources
                                    .getString("errorSettingSubject")
                                    + ": " + message);
                        }
                    });
                }

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        subjectField.setEnabled(true);
                    }
                });

            }
        });

        thread.start();
    }

    private class RunTaskThread implements Runnable {
        private String err, method;

        private Object param;

        public RunTaskThread(String err, String method, Object param) {
            this.err = err;
            this.method = method;
            this.param = param;
        }

        public void run() {
            java.lang.reflect.Method m;
            try {
                m = chat.getClass().getMethod(this.method,
                        new Class[] { param.getClass() });
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            try {
                m.invoke(chat, new Object[] { param });
            } catch (final java.lang.reflect.InvocationTargetException ex) {
                final XMPPException xmpp = (XMPPException) ex.getCause();
                if (xmpp == null)
                    return;

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        serverErrorMessage(err
                                + ": "
                                + resources.getString("xmppError"
                                        + xmpp.getXMPPError().getCode()));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Asks for a new nickname, and sends a nickname change request
     */
    private void changeNickHandler() {
        String result = (String) JOptionPane.showInputDialog(null, resources
                .getString("enterNickname"),
                resources.getString("setNickname"),
                JOptionPane.QUESTION_MESSAGE, null, null, chat.getNickname());

        if (result != null && !result.equals("")) {
            Thread thread = new Thread(new RunTaskThread(resources
                    .getString("couldNotChangeNick"), "changeNickname", result));
            thread.start();
        }
    }

    /**
     * Adds the event listeners for the various components in this chatwindows
     */
    public void addListeners() {
        //set up the window so you can press enter in the text box and
        //that will send the message.
        Action SendMessageAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                sendHandler();
            }
        };

        Action nickCompletionAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                nickCompletionHandler();
            }
        };

        //set it up so that if they drag in the conversation window, it grabs
        // the focus
        conversationArea.getTextPane().addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                //conversationArea.grabFocus();
            }
        });

        //set it up so that if there isn't any selected text in the
        // conversation area
        //the textentryarea grabs the focus.
        conversationArea.getTextPane().addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (conversationArea.getSelectedText() == null) {
                    textEntryArea.requestFocus();
                }
            }
        });

        Action closeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                BuddyList.getInstance().getTabFrame().removePanel(ChatRoomPanel.this);
                BuddyList.getInstance().stopTabFrame();
            }
        };

        subjectField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                subjectField.setEnabled(false);
                topicHandler(subjectField.getText());
                setSubject(subject);
            }
        });

        KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        textEntryArea.getInputMap().put(enterStroke, SendMessageAction);

        KeyStroke tabStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        textEntryArea.getInputMap().put(tabStroke, nickCompletionAction);

        textEntryArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit
                        .getDefaultToolkit().getMenuShortcutKeyMask()),
                closeAction);

    }

    /**
     * Leaves this room and removes it from the groupchat frame
     */
    public void leave() {
        closeLog();
        leaveChat();
    } //leave the chatroom

    /**
     * Gets the nickname currently being used in the chat room
     *
     * @return the nickname being used in the chatroom
     */
    public String getNickname() {
        if (chat == null || chat.getNickname() == null)
            return nickname;
        return chat.getNickname();
    }

    /**
     * Displays a server notice message
     *
     * @param message
     *            the message to display
     */
    public void serverNoticeMessage(String message) {
        conversationArea.append(getDate(null));
        conversationArea.append(" -> " + message + "\n", ConversationArea.SERVER);
    }

    public void serverErrorMessage(String message) {
        conversationArea.append(getDate(null));
        conversationArea.append(" -> " + message + "\n", ConversationArea.SENDER);
    }

    /**
     * Receives a message
     *
     * @param from
     *            who it's from
     * @param message
     *            the message
     */
    public void receiveMessage(String from, String message,
            Date date) {

        String curNick = nickname;
        if( chat != null && chat.getNickname() != null) curNick = chat.getNickname();
        if (from.equals("")
                || from.toLowerCase().equals(chat.getRoom().toLowerCase())) {
            //server message
            serverNoticeMessage(message);
            return;
        } else {

            boolean highLightedSound = false;

            if (message.startsWith("/me ")) {
                message = message.replaceAll("^\\/me ", "");
                conversationArea.append(getDate(date));
                conversationArea.append(" *" + from+ " ", ConversationArea.SENDER, true);
                conversationArea.append(message + "\n", ConversationArea.BLACK);
            } else if (message.toLowerCase().replaceAll("<[^>]*>", "").matches(
                    ".*(^|\\W)" + curNick.toLowerCase() + "\\W.*") &&
                    !from.toLowerCase().equals(curNick.toLowerCase())) {
                TabbedPanel tabPane = BuddyList.getInstance().getTabFrame()
                        .getTabPane();

                if (tabPane.getSelectedTab().getContentComponent() != this)
                    messageToMe = true;
                conversationArea.append(getDate(date), Color.BLACK, false, ConversationArea.HL);
                conversationArea.append(" " +from + ": ", ConversationArea.RECEIVER, true, ConversationArea.HL);
                conversationArea.append(message + "\n", ConversationArea.BLACK, false, ConversationArea.HL);

                com.valhalla.jbother.sound.SoundPlayer
                        .play("groupHighlightedSound");

                if (!BuddyList.getInstance().getTabFrame().isFocused()) {
                    NotificationPopup.showSingleton(BuddyList.getInstance()
                            .getTabFrame(), resources
                            .getString("messageReceived"), "<b>"
                            + resources.getString("from") + ":</b>&nbsp;&nbsp;"
                            + from,this);
                }

                highLightedSound = true;
            } else {
                conversationArea.append(getDate(date));
                conversationArea.append(" " + from +": ", ConversationArea.RECEIVER, true);
                conversationArea.append(message + "\n", ConversationArea.BLACK);
            }

            if (!highLightedSound)
            {
                com.valhalla.jbother.sound.SoundPlayer
                        .play("groupReceivedSound");

                if( Settings.getInstance().getBoolean("usePopup") &&
                    Settings.getInstance().getBoolean("popupForGroupMessage" ))
                {
                   NotificationPopup.showSingleton(BuddyList.getInstance().getTabFrame(), resources
                            .getString("groupMessageReceived"), from,this);
                }
            }
        }

        // fire MUCEvent for message received
        PluginChain.fireEvent(new MUCEvent(from,
                MUCEvent.EVENT_MESSAGE_RECEIVED, message, date));

        BuddyList.getInstance().getTabFrame().markTab(this, messageToMe);
    }

    public void resetMessageToMe() {
        messageToMe = false;
    }

    /**
     * Closes the log file
     */
    public void closeLog() {
        conversationArea.closeLog();
    }

    /**
     * @return a String representing the current time the format:
     *         [Hour:Minute:Second]
     */
    public String getDate(Date d) {
        return ConversationPanel.getDate(d);
    }

    public MJTextField getSubjectField() {
        return subjectField;
    }

    /**
     * Sets the subject of the room
     *
     * @param subject
     *            the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
        if (BuddyList.getInstance().getTabFrame() != null)
            BuddyList.getInstance().getTabFrame().setSubject(this);

        subjectField.setText(subject);
        subjectField.setCaretPosition(0);
        subjectField.setToolTipText(subject);
    }

    /**
     * Returns the current room subject
     *
     * @return the current room subject
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Gets all the buddy statuses in the room
     *
     * @return all BuddyStatuses
     */
    public Hashtable getBuddyStatuses() {
        return this.buddyStatuses;
    }

    /**
     * Sends the message currently in the textentryarea
     */
    private void sendHandler() {
        String text = textEntryArea.getText();

        Message message = chat.createMessage();
        message.setBody(text);

        if (!textEntryArea.getText().equals("")) {
            try {
                chat.sendMessage(message);
            } catch (XMPPException e) {
                com.valhalla.Logger.debug("Could not send message.");
            } catch (IllegalStateException ex) {
                serverErrorMessage(resources.getString("notConnected"));
            }

            textEntryArea.setText("");
        }
    }

    /**
     * Implementation of Tab nick completion in the textEntryArea
     */
    private void nickCompletionHandler() {
        String text = textEntryArea.getText();

        /* if we have nothing => do nothing */
        if (!text.equals("")) {
            int caretPosition = textEntryArea.getCaretPosition();
            int startPosition = text.lastIndexOf(" ", caretPosition - 1) + 1;
            String nickPart = text.substring(startPosition, caretPosition);
            Vector matches = new Vector();

            java.util.List keys = new ArrayList(buddyStatuses.keySet());
            Iterator iterator = keys.iterator();

            while (iterator.hasNext()) {
                BuddyStatus buddy = (BuddyStatus) buddyStatuses.get(iterator
                        .next());
                if (!nickList.contains(buddy))
                    continue;
                try {
                    String nick = buddy.getUser().substring(
                            buddy.getUser().lastIndexOf("/") + 1);
                    if (nick.toLowerCase().startsWith(nickPart.toLowerCase())) {
                        matches.add(nick);
                    }
                } catch (java.lang.NullPointerException e) {
                }
            }

            if (matches.size() > 0) {
                String append = "";

                if (matches.size() > 1) {
                    String nickPartNew = (String) matches.firstElement();
                    String nick = "";
                    String hint = nickPartNew + ", ";
                    int nickPartLen = nickPart.length();
                    for (int i = 1; i < matches.size(); i++) {
                        nick = (String) matches.get(i);
                        hint += nick + ", ";
                        for (int j = 1; j <= nick.length() - nickPartLen; j++) {
                            if (!nickPartNew.regionMatches(true, nickPartLen,
                                    nick, nickPartLen, j)) {
                                nickPartNew = nickPartNew.substring(0,
                                        nickPartLen + j - 1);
                                break;
                            }
                        }
                    }
                    if (nickPart.length() != nickPartNew.length()) {
                        nickPart = nickPartNew;
                    }
                    // emphasize differense in matches by bold and append hint
                    // to the conversationArea
                    // hint = hint.replaceAll() can't be used here because of
                    // its case sensitive nature
                    //Pattern pattern = Pattern.compile("(" + nickPart
                    //        + ")([^,]+), ", Pattern.CASE_INSENSITIVE);
                    //hint = pattern.matcher(hint).replaceAll("$1<b>$2</b>, ");
                    conversationArea.append(hint.substring(0, hint.length() - 2) + "\n", ConversationArea.RECEIVER, true);
                } else {
                    nickPart = (String) matches.firstElement();
                    if (startPosition == 0)
                        append = ": ";
                    else
                        append = " ";
                }

                String newText = text.substring(0, startPosition);
                newText += nickPart + append;
                newText += text.substring(caretPosition);
                textEntryArea.setText(newText);
                /* Set caret to the appropriate position */
                textEntryArea.setCaretPosition(startPosition
                        + nickPart.length() + append.length());
            }

        } /* end of the lazy "if" */
    }

    /**
     * Joins the chatroom and adds this chatroomwindow to the TabFrame
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class JoinChatThread extends Thread {
        private String errorMessage;

        private boolean cancelled = false;


        public void cancel() {
            interrupt();
            cancelled = true;
        }

        public void run() {
            chat.addMessageListener(messageListener);
            chat.addParticipantListener(participantListener);
            chat.addSubjectUpdatedListener(subjectListener);
            chat.addParticipantStatusListener(statusListener);
            chat.addUserStatusListener(userStatusListener);
            chat.addInvitationRejectionListener(invitationRejectionPacketListener);

            int errorCode = 0;

            try {
                chat.join(nickname, pass, new DiscussionHistory(),
                        SmackConfiguration.getPacketReplyTimeout());
            } catch (XMPPException e) {
                if (!cancelled) {
                    if (e.getXMPPError() == null)
		    {
                        errorMessage = e.getMessage();

		    }
                    else {
                        errorMessage = resources.getString("xmppError"
                                + e.getXMPPError().getCode());
			errorCode = e.getXMPPError().getCode();
		    }


                }
            }

            final int tempError = errorCode;
            if (!cancelled) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (errorMessage != null) {
                            if(tempError == 409 && joins++ < 1 && !removed)
                            {
                                nickname += " ";
                                leaveChat();
                                ChatRoomPanel window = new ChatRoomPanel(chatroom, nickname, pass);
                                BuddyList.getInstance().removeTabPanel(ChatRoomPanel.this);
                                window.startChat();


                                return;
                            }

                            serverErrorMessage(errorMessage);
                        } else {
                            if (cancelled) {
                                errorMessage = "error";
                            } else {
                                //set up a packet to be sent to my user in
                                // every groupchat
                                Presence presence = new Presence(
                                        Presence.Type.AVAILABLE, BuddyList
                                                .getInstance()
                                                .getCurrentStatusString(), 0,
                                        BuddyList.getInstance()
                                                .getCurrentPresenceMode());
                                presence.setTo(getRoomName() + '/'
                                        + getNickname());

                                BuddyList.getInstance().getConnection()
                                        .sendPacket(presence);
                            }
                        }
                    }
                });
            } else {
                errorMessage = "cancelled";
            }

            if (errorMessage != null) {
                try {
                    Thread.sleep(1000);
                    leaveChat();
                } catch (Exception neverCaught) {
                }
            }
        }
    }
}
