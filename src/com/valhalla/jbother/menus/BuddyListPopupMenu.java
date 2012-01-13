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
package com.valhalla.jbother.menus;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.packet.*;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.*;
import net.infonode.tabbedpanel.*;
import com.valhalla.jbother.groupchat.*;
import com.valhalla.gui.*;
import com.valhalla.jbother.*;
import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.jabber.*;
import com.valhalla.jbother.jabber.smack.*;

/**
 *  The menu that pops up when you right click on a user in your roster
 *
 *@author     Adam Olsen
 *@author     Andrey Zakirov
 *@created    June 23, 2005
 *@version    1.1
 */
public class BuddyListPopupMenu extends JPopupMenu implements UserChooserListener  {
    /**
     *  Description of the Field
     */
    protected ResourceBundle resources = ResourceBundle.getBundle("JBotherBundle", Locale.getDefault());
    /**
     *  Description of the Field
     */
    protected JMenuItem removeBuddyItem = new JMenuItem(resources.getString("removeFromRoster"));
    /**
     *  Description of the Field
     */
    protected JMenuItem modifyBuddyItem = new JMenuItem(resources.getString("modify"));
    /**
     *  Description of the Field
     */
    protected JMenu resourceMenu = new JMenu(resources.getString("resources"));
    /**
     *  Description of the Field
     */
    protected JMenu authMenu = new JMenu(resources.getString("authorization"));

    /**
     *  Description of the Field
     */
    protected JMenu infoMenu = new JMenu(resources.getString("informationMenu"));
    /**
     *  Description of the Field
     */
    protected JMenu editMenu = new JMenu(resources.getString("editMenu"));

    /**
     *  Description of the Field
     */
    protected JMenuItem requestSubscription = new JMenuItem(resources.getString("requestNotification"));
    /**
     *  Description of the Field
     */
    protected JMenuItem resendSubscription = new JMenuItem(resources.getString("resendNotification"));
    /**
     *  Description of the Field
     */
    protected JMenuItem removeSubscription = new JMenuItem(resources.getString("removeNotification"));

    /**
     *  Description of the Field
     */
    protected JMenuItem chatItem = new JMenuItem(resources.getString("openChatDialog"));
    /**
     *  Description of the Field
     */
    protected JMenuItem messageItem = new JMenuItem(resources.getString("messageWindow"));
    /**
     *  Description of the Field
     */
    protected JMenuItem logItem = new JMenuItem(resources.getString("viewLog"));
    /**
     *  Description of the Field
     */
    protected JMenuItem infoItem = new JMenuItem(resources.getString("getInfo"));
    /**
     *  Description of the Field
     */
    protected JMenuItem blockItem = new JMenuItem(resources.getString("blockUser"));

    /**
     *  Description of the Field
     */
    protected JMenuItem bindPubKeyItem = new JMenuItem(resources.getString("gnupgBindPublicKey"));
    /**
     *  Description of the Field
     */
    protected JMenuItem unbindPubKeyItem = new JMenuItem(resources.getString("gnupgUnbindPublicKey"));

    protected JMenu misc = new JMenu(resources.getString("misc"));
    protected JMenu invite = new JMenu(resources.getString("inviteToMuc"));
    protected JMenuItem noItem = new JMenuItem(resources.getString("noMucs"));
    protected JMenu rosterExchange = new JMenu(resources.getString("sendRosterItems"));
    protected JMenuItem individualItems = new JMenuItem(resources.getString("individualItems"));
    protected JMenuItem entireRoster = new JMenuItem(resources.getString("entireRoster"));

    /**
     *  Description of the Field
     */
    protected BuddyStatus buddy;
    private JTree tree;
    /**
     *  Description of the Field
     */
    protected JMenuItem sendFileItem = new JMenuItem(resources.getString("sendFile"));
    protected JMenu sendFileMenu = new JMenu(resources.getString("sendFile"));


    /**
     *  Creates the menu
     */
    public BuddyListPopupMenu() {
        MenuActionListener listener = new MenuActionListener();

        noItem.setEnabled(false);
        infoItem.addActionListener(listener);
        removeBuddyItem.addActionListener(listener);
        chatItem.addActionListener(listener);
        messageItem.addActionListener(listener);
        modifyBuddyItem.addActionListener(listener);
        requestSubscription.addActionListener(listener);
        logItem.addActionListener(listener);
        resendSubscription.addActionListener(listener);
        removeSubscription.addActionListener(listener);
        blockItem.addActionListener(listener);
        sendFileItem.addActionListener(listener);
        bindPubKeyItem.addActionListener(listener);
        unbindPubKeyItem.addActionListener(listener);
        entireRoster.addActionListener(listener);
        individualItems.addActionListener(listener);

        authMenu.add(requestSubscription);
        authMenu.add(resendSubscription);
        authMenu.add(removeSubscription);
        authMenu.add(blockItem);

        infoMenu.add(infoItem);
        infoMenu.add(logItem);

        editMenu.add(modifyBuddyItem);
        editMenu.add(removeBuddyItem);

        misc.add(invite);
        misc.add(rosterExchange);

        add(chatItem);
        add(messageItem);
        addSeparator();
        add(infoMenu);
        add(editMenu);
        addSeparator();
        add(resourceMenu);
        add(authMenu);
        add(misc);
        addSeparator();
        add(sendFileItem);
    }

    /**
     *  Gets the from attribute of the BuddyListPopupMenu object
     *
     *@return    The from value
     */
    protected String getFrom() {
        if (BuddyList.getInstance().checkConnection()) {
            return BuddyList.getInstance().getConnection().getUser();
        }

        return "";
    }


    /**
     *  Requests subscription to a user
     *
     *@param  type  the type of subscription to send
     */
    private void subscriptionHandler(Presence.Type type) {
        Presence presence = new Presence(type);

        presence.setTo(buddy.getUser());
        if (BuddyList.getInstance().checkConnection()) {
            BuddyList.getInstance().getConnection().sendPacket(presence);
        } else {
            BuddyList.getInstance().connectionError();
        }
    }


    /**
     *  Listens for different items to get clicked on in the popup menu
     *
     *@author     Adam Olsen
     *@created    June 23, 2005
     *@version    1.0
     */
    class MenuActionListener implements ActionListener {
        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == removeBuddyItem) {
                removeBuddyHandler();
            } else if (e.getSource() == chatItem) {
                BuddyList.getInstance().getBuddyListTree().initiateConversation(buddy);
            } else if (e.getSource() == messageItem) {
                MessagePanel panel = new MessagePanel();
                panel.setBuddy(buddy);

                if (!(buddy instanceof MUCBuddyStatus)) {
                    String to = buddy.getUser();
                    if (buddy.size() > 0) {
                        to += "/" + buddy.getHighestResource();
                    }
                    panel.setTo(to);
                } else {
                    panel.setTo(buddy.getUser());
                }

                panel.setFrom(getFrom());
                MessageDelegator.getInstance().showPanel(panel);
                MessageDelegator.getInstance().frontFrame(panel);
                panel.getSubjectField().grabFocus();
            } else if (e.getSource() == modifyBuddyItem) {
                modifyBuddyHandler();
            } else if (e.getSource() == requestSubscription) {
                subscriptionHandler(Presence.Type.SUBSCRIBE);
            } else if (e.getSource() == removeSubscription) {
                subscriptionHandler(Presence.Type.UNSUBSCRIBED);
            } else if (e.getSource() == resendSubscription) {
                subscriptionHandler(Presence.Type.SUBSCRIBED);
            } else if (e.getSource() == infoItem) {
                if (!(buddy instanceof MUCBuddyStatus)) {
                    new InformationViewerDialog(buddy.getAddress());
                } else {
                    new InformationViewerDialog(buddy.getUser());
                }
            } else if (e.getSource() == logItem) {
                new LogViewerDialog(buddy.getConversation(), buddy.getUser());
            } else if (e.getSource() == blockItem) {
                blockHandler();
            } else if (e.getSource() == sendFileItem) {
                sendFileHandler();
            } else if (e.getSource() == bindPubKeyItem) {
                bindPubKeyHandler();
            } else if (e.getSource() == unbindPubKeyItem) {
                unbindPubKeyHandler();
            }
            else if (e.getSource() == individualItems)
            {
                UserChooser chooser = new UserChooser(BuddyList.getInstance().getContainerFrame(),
                        resources.getString("sendRosterItems"), resources.getString("selectSendItems"), true);
                chooser.addListener(BuddyListPopupMenu.this);
                chooser.setVisible(true);
            }
            else if(e.getSource() == entireRoster)
            {
                int result = JOptionPane.showConfirmDialog(BuddyList.getInstance().getContainerFrame(),
                        resources.getString("sureSendEntireRoster"), resources.getString("sendRosterItems"),
                        JOptionPane.YES_NO_OPTION);

                if(result != JOptionPane.YES_OPTION) return;

                RosterExchangeManager manager = ConnectorThread.getInstance().getExchangeManager();
                Roster roster = BuddyList.getInstance().getConnection().getRoster();
                manager.send(roster, buddy.getUser());
            }
        }
    }

    public void usersChosen(UserChooser.Item[] items)
    {
        Message message = new Message(buddy.getUser());
        RosterExchange r = new RosterExchange();

        message.addExtension(r);

        Roster roster = BuddyList.getInstance().getConnection().getRoster();
        for(int i = 0; i < items.length; i++)
        {
            RosterEntry entry = roster.getEntry(items[i].getJID());
            r.addRosterEntry(entry);
        }

        BuddyList.getInstance().getConnection().sendPacket(message);
    }


    /**
     *  Blocks the user
     */
    protected void blockHandler() {
        // just adds the user to the blocked list in buddy list
        File blockedFile = new File(JBother.profileDir + File.separator + "blocked");
        BuddyList.getInstance().getBlockedUsers().put(buddy.getUser(), "blocked");

        // and then writes all of them to the blocked users file
        try {
            FileWriter fw = new FileWriter(blockedFile);
            PrintWriter out = new PrintWriter(fw);

            Iterator i = BuddyList.getInstance().getBlockedUsers().keySet().iterator();
            while (i.hasNext()) {
                out.println((String) i.next());
            }

            fw.close();
        } catch (IOException ex) {
            Standard.warningMessage(null, resources.getString("blockUser"),
                    resources.getString("problemWritingBlockedFile"));
            return;
        }
        BuddyList.getInstance().getBuddyListTree().removeBuddy(buddy, buddy.getGroup(), true);

        Standard.noticeMessage(null, resources.getString("blockUser"), resources.getString("userHasBeenBlocked"));
    }


    /**
     *  Opens the AddBuddyDialog to modify a buddy
     */
    protected void modifyBuddyHandler() {
        AddBuddyDialog buddyDialog = new AddBuddyDialog();
        buddyDialog.setBuddy(buddy.getRosterEntry());
        buddyDialog.setVisible(true);
    }


    /**
     *  Confirms buddy removal, and then removes the buddy
     */
    protected void removeBuddyHandler() {
        if (!BuddyList.getInstance().checkConnection()) {
            BuddyList.getInstance().connectionError();
            return;
        }

        int result = 1;

        result = JOptionPane.showConfirmDialog(null, resources.getString("sureRemoveContact"), resources.getString("removeFromRoster"),
                JOptionPane.YES_NO_OPTION);

        if (result != 0) {
            return;
        }

        RosterEntry entry = buddy.getRosterEntry();
        buddy.setRemoved(true);
        String gp = buddy.getGroup();

        if (entry != null) {
            com.valhalla.Logger.debug("Remove entry user: " + entry.getUser());
            BuddyList.getInstance().getBuddyListTree().removeBuddy(buddy, gp, true);

            try {
                BuddyList.getInstance().getConnection().getRoster().removeEntry(entry);
            }
            catch(XMPPException ex) { }

            RosterPacket packet = new RosterPacket();
            packet.setType(IQ.Type.SET);
            RosterPacket.Item item = new RosterPacket.Item(entry.getUser(), entry.getName());
            item.setItemType(RosterPacket.ItemType.REMOVE);
            packet.addRosterItem(item);

            // if this buddy is a transport, unregister from it
            if(entry.getUser().indexOf("@") == -1 )
            {
                Registration r = new Registration();
                r.setTo(entry.getUser());
                r.setFrom(BuddyList.getInstance().getConnection().getUser());
                r.setType(IQ.Type.SET);

                HashMap map = new HashMap();
                map.put("remove", "");
                r.setAttributes(map);
                BuddyList.getInstance().getConnection().sendPacket(r);
            }
            BuddyList.getInstance().getConnection().sendPacket(packet);

        } else {
            BuddyList.getInstance().getBuddyListTree().removeBuddy(buddy, gp, true);
        }
    }


    /**
     *  Updates the resources menu
     */
    private void updateResourceMenu() {
        ResourceActionListener listener = new ResourceActionListener(buddy);
        SendFileActionListener sendListener = new SendFileActionListener(buddy);
        resourceMenu.removeAll();
        sendFileMenu.removeAll();
        Set keys = buddy.keySet();

        int fileItem = getComponentIndex(sendFileItem);
        int menu = getComponentIndex(sendFileMenu);

        if(buddy.size() <= 1 && menu > -1)
        {
            remove(menu);
            insert(sendFileItem, menu);
        }
        else if(buddy.size() > 1 && fileItem > -1)
        {
            remove(fileItem);
            insert(sendFileMenu, fileItem);
        }

        Iterator i = keys.iterator();
        while (i.hasNext()) {
            boolean na = false;
            String key = (String) i.next();

            if (key.equals("N/A")) {
                na = true;
                key = "None";
            }

            JMenuItem item = new JMenuItem(key);
            JMenuItem item2 = new JMenuItem(key);

            if (!na) {
                Presence.Mode mode;
                if (buddy.size() == 0) {
                    mode = null;
                    item.setForeground(Color.GRAY);
                    item2.setForeground(Color.GRAY);
                } else {
                    mode = buddy.getPresence(key);
                }

                ImageIcon icon = StatusIconCache.getStatusIcon(mode);
                if (icon != null) {
                    item.setIcon(icon);
                    item2.setIcon(icon);
                }
                item.addActionListener(listener);
                item2.addActionListener(sendListener);
            } else {
                item.setEnabled(false);
                item2.setEnabled(false);
            }

            resourceMenu.add(item);
            if(buddy.size() > 1) sendFileMenu.add(item2);
        }
    }

    private void updateMucMenu()
    {
        MUCActionListener listener = new MUCActionListener(buddy);
        invite.removeAll();

        TabFrame frame = BuddyList.getInstance().getTabFrame();

        if(frame == null)
        {
            invite.add(noItem);
            return;
        }

        TabbedPanel pane = frame.getTabPane();
        for( int i = 0; i < pane.getTabCount(); i++ )
        {
            TabFramePanel panel = (TabFramePanel)pane.getTabAt(i).getContentComponent();
            if( panel instanceof ChatRoomPanel )
            {
                JMenuItem item = new JMenuItem( ((ChatRoomPanel)panel).getRoomName() );
                item.addActionListener(listener);
                invite.add(item);
            }
        }
    }

    class MUCActionListener implements ActionListener
    {
        BuddyStatus buddy;

        public MUCActionListener(BuddyStatus b) { this.buddy = b; }

        public void actionPerformed(ActionEvent e)
        {
            JMenuItem item = (JMenuItem)e.getSource();
            String room = item.getText();

            TabFrame frame = BuddyList.getInstance().getTabFrame();
            if(frame == null) return;

            ChatRoomPanel window = frame.getChatPanel(room);
            if(window == null) return;

            window.usersChosen(new String[] { buddy.getUser() });
        }
    }

    /**
     *  lets user choose the file to send and send it
     */
    private void sendFileHandler() {
        new FileSendDialog(buddy.getAddress());
    }


    /**
     *  binds Public Key to buddy
     */
    private void bindPubKeyHandler() {
        KeySelectDialog dialog = new KeySelectDialog("pub");
        dialog.showDialog();
        if (dialog.getID() != null) {
            buddy.setPubKey(dialog.getID());
        }
    }


    /**
     *  unbinds Public Key from buddy
     */
    private void unbindPubKeyHandler() {
        buddy.setPubKey(null);
    }


    /**
     *  Shows the popup menu
     *
     *@param  tree   the tree to show the menu on
     *@param  x      the x coordinate
     *@param  y      the y coordinate
     *@param  buddy  the buddy that this menu should show up on
     */
    public void showMenu(Component tree, int x, int y, BuddyStatus buddy) {
        this.buddy = buddy;
        this.tree = (JTree) tree;

        updateResourceMenu();
        updateMucMenu();
        updateRosterMenu();

        validate();

        if (JBotherLoader.isGPGEnabled()) {
            remove(unbindPubKeyItem);
            remove(bindPubKeyItem);

            if (buddy.getPubKey() != null) {
                add(unbindPubKeyItem);
            } else {
                add(bindPubKeyItem);
            }
        }

        show(tree, x, y);
    }

    private void updateRosterMenu()
    {
        MyExchangeListener listener = new MyExchangeListener();
        rosterExchange.removeAll();
        rosterExchange.add(entireRoster);
        rosterExchange.add(individualItems);
        rosterExchange.addSeparator();
        Roster roster = BuddyList.getInstance().getConnection().getRoster();
        Iterator i = roster.getGroups();
        while(i.hasNext())
        {
            RosterGroup group = (RosterGroup)i.next();
            JMenuItem item = new JMenuItem(resources.getString("sendGroup") + ": " + group.getName());
            item.addActionListener(listener);
            rosterExchange.add(item);
        }
    }

    class MyExchangeListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            Roster roster = BuddyList.getInstance().getConnection().getRoster();
            JMenuItem item = (JMenuItem)e.getSource();
            RosterExchangeManager manager = ConnectorThread.getInstance().getExchangeManager();
            manager.send(roster.getGroup(item.getText().replaceAll("^" + resources.getString("sendGroup") + ": ", "")), buddy.getUser());
        }
    }

    /**
     *  Listens for a resource to get clicked in the ResourceMenu
     *
     *@author     synic
     *@created    June 23, 2005
     */
    class SendFileActionListener implements ActionListener {
        private BuddyStatus buddy;


        /**
         *  Constructor for the ResourceActionListener object
         *
         *@param  buddy  Description of the Parameter
         */
        public SendFileActionListener(BuddyStatus buddy) {
            this.buddy = buddy;
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            JMenuItem item = (JMenuItem) e.getSource();
            new FileSendDialog(buddy.getUser() + "/" + item.getText());
        }
    }

    /**
     *  Listens for a resource to get clicked in the ResourceMenu
     *
     *@author     synic
     *@created    June 23, 2005
     */
    class ResourceActionListener implements ActionListener {
        private BuddyStatus buddy;


        /**
         *  Constructor for the ResourceActionListener object
         *
         *@param  buddy  Description of the Parameter
         */
        public ResourceActionListener(BuddyStatus buddy) {
            this.buddy = buddy;
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            JMenuItem item = (JMenuItem) e.getSource();
            BuddyList.getInstance().getBuddyListTree().initiateConversation(buddy);
            ChatPanel window = (ChatPanel) buddy.getConversation();
            window.getResourceBox().setSelectedItem(item.getText());
            window.getResourceBox().validate();
        }
    }
}

