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

package com.valhalla.jbother.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.XMPPException;

import com.valhalla.gui.*;
import com.valhalla.jbother.*;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.ParsedBuddyInfo;
import com.valhalla.jbother.preferences.PreferencesDialog;
import com.valhalla.pluginmanager.PluginManager;
import com.valhalla.settings.Arguments;
import com.valhalla.settings.Settings;
import com.valhalla.jbother.actions.*;

/**
 * The Options menu for JBother
 *
 * @author Adam Olsen
 * @version 1.0
 */
class BuddyListOptionsMenu extends JMenu implements UserChooserListener {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JCheckBoxMenuItem offlineBuddies = new JCheckBoxMenuItem(resources
            .getString("showOfflineBuddies"));

    private JCheckBoxMenuItem unfiledBuddies = new JCheckBoxMenuItem(resources
            .getString("showUnfiledBuddies"));

    private JCheckBoxMenuItem agentBuddies = new JCheckBoxMenuItem(resources
            .getString("showAgentBuddies"));

    private JCheckBoxMenuItem agentMessages = new JCheckBoxMenuItem(resources
            .getString("receiveAgentMessages"));

    private JMenuItem priority = new JMenuItem(resources
            .getString("setPriority"));

    private JMenuItem prefsItem = new JMenuItem(resources
            .getString("preferences"));

    private JMenuItem pluginItem = new JMenuItem(resources
            .getString("pluginManager"));

    private JCheckBoxMenuItem sortItem = new JCheckBoxMenuItem(resources
            .getString("sortByStatus"));

    private JMenuItem deleteAccount = new JMenuItem(resources
            .getString("deleteAccount"));

    private JMenu managementMenu = new JMenu(resources
            .getString("accountManagement"));
    
    private JMenuItem deleteMultiple = new JMenuItem(resources.getString("deleteMultiple"));

    private JMenuItem changePassword = new JMenuItem(resources
            .getString("changePassword"));

    private JMenuItem consoleItem = new JMenuItem(resources
            .getString("xmlConsole"));
    
    private JMenuItem downloadsItem = new JMenuItem(resources.getString("downloads"));

    private JMenuItem memItem = new JMenuItem(resources.getString("memoryStats"));

    /**
     * Creates the menu, adds the various items to it
     */
    public BuddyListOptionsMenu() {
        super("Options");
        setText(resources.getString("options"));
        offlineBuddies.setMnemonic(KeyEvent.VK_H);
        unfiledBuddies.setMnemonic(KeyEvent.VK_U);
        agentBuddies.setMnemonic(KeyEvent.VK_A);
        agentMessages.setMnemonic(KeyEvent.VK_R);
        sortItem.setMnemonic(KeyEvent.VK_S);
        priority.setMnemonic(KeyEvent.VK_I);
        prefsItem.setMnemonic(KeyEvent.VK_P);
        pluginItem.setMnemonic(KeyEvent.VK_M);
        managementMenu.setMnemonic(KeyEvent.VK_E);
        changePassword.setMnemonic(KeyEvent.VK_C);

        ShowOfflineAction.addItem( offlineBuddies );

        MenuActionListener listener = new MenuActionListener();
        prefsItem.addActionListener(listener);

        unfiledBuddies.addActionListener(listener);
        agentBuddies.addActionListener(listener);
        pluginItem.addActionListener(listener);
        agentMessages.addActionListener(listener);
        priority.addActionListener(listener);
        sortItem.addActionListener(listener);
        consoleItem.addActionListener(listener);
        memItem.addActionListener(listener);
        downloadsItem.addActionListener(listener);
        deleteMultiple.addActionListener(listener);

        //get the settings for the "show" states
        offlineBuddies.setState(Settings.getInstance().getBoolean(
                "showOfflineBuddies"));
        unfiledBuddies.setState(Settings.getInstance().getBoolean(
                "showUnfiledBuddies"));
        agentBuddies.setState(Settings.getInstance().getBoolean(
                "showAgentBuddies"));
        agentMessages.setState(Settings.getInstance().getBoolean(
                "showAgentMessages"));
        sortItem.setState(Settings.getInstance().getBoolean("sortByStatus"));

        managementMenu.add(changePassword);
        managementMenu.add(deleteAccount);
        managementMenu.add(deleteMultiple);
        deleteAccount.addActionListener(listener);
        changePassword.addActionListener(listener);

        add(offlineBuddies);
        add(unfiledBuddies);
        add(agentBuddies);
        add(agentMessages);
        add(sortItem);
        add(consoleItem);
        add(downloadsItem);

        add(priority);

        if (Arguments.getInstance().getProperty("webstart") == null) {
            add(pluginItem);
        }

        add(prefsItem);
        add(managementMenu);
	add(memItem);
    }

    /**
     * @param var
     *            true if this is OS X
     */
    public void setOSX(boolean var) {
        if (var) {
            remove(prefsItem);
        } else {
            if (!isMenuComponent(prefsItem))
                insert(prefsItem, getItemCount() - 1);
        }

    }

    /**
     * Listens for an item to be clicked in the menu
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class MenuActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == unfiledBuddies)
                BuddyList.getInstance().getBuddyListTree()
                        .setShowUnfiledBuddies(unfiledBuddies.getState());
            else if (e.getSource() == agentBuddies)
                BuddyList.getInstance().getBuddyListTree().setShowAgentBuddies(
                        agentBuddies.getState());
            else if (e.getSource() == agentMessages) {
                Settings.getInstance().setBoolean("showAgentMessages",
                        agentMessages.getState());
            } else if (e.getSource() == sortItem)
                BuddyList.getInstance().getBuddyListTree().setSortByStatus(
                        sortItem.getState());
            else if (e.getSource() == prefsItem) {
                if (!DialogTracker.containsDialog(PreferencesDialog.class))
                    new PreferencesDialog().setVisible(true);
            } else if (e.getSource() == priority)
                new PriorityDialog().setVisible(true);
            else if (e.getSource() == deleteAccount)
                deleteAccountHandler();
            else if (e.getSource() == changePassword) {
                if (!DialogTracker.containsDialog(ChangePasswordDialog.class))
                    new ChangePasswordDialog().setVisible(true);
            } else if (e.getSource() == pluginItem) {
                if (DialogTracker.containsDialog(PluginManager.class))
                    return;
                PluginManager manager = new PluginManager(
                  Settings.getInstance().getProperty("pluginsDownloadMirror"),
                  Settings.getInstance().getProperty("pluginsDownloadScript"),
                  JBother.settingsDir);
                manager.setVisible(true);
            } else if (e.getSource() == consoleItem) {
                String from = resources.getString("xmlConsole");;
                ParsedBuddyInfo info = new ParsedBuddyInfo(from);
                String userId = info.getUserId().toLowerCase();
                final BuddyStatus buddyStatus = BuddyList.getInstance()
                        .getBuddyStatus(userId);
                buddyStatus.setName ( resources.getString ( "xmlConsole"));
                if (buddyStatus.getConversation() == null) {
                    buddyStatus.setConversation(ConsolePanel.getInstance(buddyStatus));
                    MessageDelegator.getInstance().showPanel(
                            buddyStatus.getConversation());
                    MessageDelegator.getInstance().frontFrame(
                            buddyStatus.getConversation());
                }
            }
            else if(e.getSource() == memItem)
		{
			MemoryDialog.getInstance(BuddyList.getInstance().getContainerFrame());
		}
            else if(e.getSource() == downloadsItem)
            {
                FileProgressDialog.getInstance().setVisible(true);
            }
            else if(e.getSource() == deleteMultiple)
            {
                UserChooser chooser = new UserChooser(BuddyList.getInstance().getContainerFrame(), resources.getString("deleteMultiple"), "", true);
                chooser.addListener(BuddyListOptionsMenu.this);
                chooser.setVisible(true);
            }
        }
    }
    
    public void usersChosen(UserChooser.Item[] items)
    {
        int result = JOptionPane.showConfirmDialog(BuddyList.getInstance().getContainerFrame(), 
                resources.getString("sureDeleteBuddies"), resources.getString("deleteMultiple"), JOptionPane.YES_NO_OPTION);
        if(result != JOptionPane.YES_OPTION) return;
        new Thread(new DeleteThread(items)).start();
    }
    
    class DeleteThread implements Runnable
    {
        UserChooser.Item[] items;
        public DeleteThread(UserChooser.Item[] items)
        {
            this.items = items;
        }
        
        public void run()
        {
            for(int i = 0; i < items.length; i++)
            {
                final BuddyStatus buddy = BuddyList.getInstance().getBuddyStatus(items[i].getJID());
                buddy.setRemoved(true);
                final String gp = buddy.getGroup();
                
                try {
                    BuddyList.getInstance().getConnection().getRoster().removeEntry(buddy.getRosterEntry());
                }
                catch(XMPPException ex) { }
                
                javax.swing.SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        BuddyList.getInstance().getBuddyListTree().removeBuddy(buddy, gp, true); 
                    }
                });
            }
        }
    }

    /**
     * Confirms that the user really wants to delete their account, and does so
     * if they do
     */
    private void deleteAccountHandler() {
        if (!BuddyList.getInstance().checkConnection()) {
            BuddyList.getInstance().connectionError();
            return;
        }

        int result = JOptionPane.showConfirmDialog(null, resources
                .getString("sureDeleteAccount"), resources
                .getString("deleteAccount"), JOptionPane.YES_NO_OPTION);

        // makes sure one more time :)
        if (result == 0) {
            result = JOptionPane.showConfirmDialog(null, resources
                    .getString("sureDeleteAccountPart2"), resources
                    .getString("deleteAccount"), JOptionPane.YES_NO_OPTION);
        }

        if (result == 0) {
            AccountManager manager = BuddyList.getInstance().getConnection()
                    .getAccountManager();

            // go ahead and delete the account
            try {
                manager.deleteAccount();
            } catch (XMPPException e) {
                String errorMessage = "";
                errorMessage = resources.getString("xmppError"
                        + e.getXMPPError().getCode());

                Standard.warningMessage(BuddyList.getInstance(), resources
                        .getString("deleteAccount"), errorMessage);
                return;
            }

            // close everything and display a login dialog
            BuddyList.getInstance().kill();
            Standard.warningMessage(BuddyList.getInstance(), resources
                    .getString("deleteAccount"), resources
                    .getString("accountDeleted"));
        }
    }
}
