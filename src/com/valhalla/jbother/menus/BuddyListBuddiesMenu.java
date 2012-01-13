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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.jivesoftware.smack.packet.Presence;

import com.valhalla.gui.Standard;
import com.valhalla.jbother.AddBuddyDialog;
import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.ConnectorThread;
import com.valhalla.jbother.InformationViewerDialog;
import com.valhalla.jbother.JBother;
import com.valhalla.jbother.MessageDelegator;
import com.valhalla.jbother.MessagePanel;
import com.valhalla.jbother.ProfileEditorDialog;
import com.valhalla.jbother.ProfileManager;
import com.valhalla.jbother.RegistrationForm;
import com.valhalla.jbother.SearchDialog;
import com.valhalla.jbother.ServiceDiscoveryDialog;
import com.valhalla.jbother.groupchat.GroupChatBookmarks;
import com.valhalla.settings.Settings;

/**
 * Buddies menu - contains all the other menus - excluding the help menu
 *
 * @author Adam Olsen
 * @author Andrey Zakirov
 * @version 1.0
 */
public class BuddyListBuddiesMenu extends JPopupMenu {
    private BuddyList blist;
    
    private ResourceBundle resources = ResourceBundle.getBundle(
    "JBotherBundle", Locale.getDefault());
    
    private JMenuItem addBuddyItem = new JMenuItem(resources
    .getString("addBuddy"));
    
    private JMenuItem signOnItem = new JMenuItem(resources.getString("signOn"));
    
    private JMenuItem quitItem = new JMenuItem(resources
    .getString("quitButton"));
    
    private JMenu servicesMenu = new JMenu(resources
    .getString("jabberServices"));
    
    private JMenuItem joinChatItem = new JMenuItem(resources
    .getString("joinGroupChat"));
    
    private JMenuItem discoItem = new JMenuItem(resources
    .getString("serviceDiscovery"));
    
    private JMenuItem blankItem = new JMenuItem(resources
    .getString("blankMessage"));
    
    private JMenuItem registerItem = new JMenuItem(resources
    .getString("registerForService"));
    
    private JMenuItem searchItem = new JMenuItem(resources.getString("userSearch"));
    
    private BuddyListOptionsMenu optionsMenu = new BuddyListOptionsMenu();
    
    private JMenuItem switchItem = new JMenuItem(resources
    .getString("switchProfile"));
    
    private JMenuItem editItem = new JMenuItem(resources
    .getString("editAccount"));
    
    private JMenuItem infoItem = new JMenuItem(resources
    .getString("editInformation"));
    
    private JMenu profileMenu = new JMenu("Profile");
    
    
    
    /**
     * Creates the buddies menu
     *
     * @param blist
     *            the buddy list to attach this menu to
     */
    public BuddyListBuddiesMenu(BuddyList blist) {
        super("JBother");
        
        if (System.getProperty("mrj.version") != null) {
//            setText(resources.getString("actions"));
//            setMnemonic(KeyEvent.VK_A);
        } else {
//            setMnemonic(KeyEvent.VK_J);
        }
        
        addBuddyItem.setMnemonic(KeyEvent.VK_A);
        quitItem.setMnemonic(KeyEvent.VK_Q);
        signOnItem.setMnemonic(KeyEvent.VK_L);
        servicesMenu.setMnemonic(KeyEvent.VK_J);
        joinChatItem.setMnemonic(KeyEvent.VK_G);
        blankItem.setMnemonic(KeyEvent.VK_B);
        switchItem.setMnemonic(KeyEvent.VK_S);
        editItem.setMnemonic(KeyEvent.VK_E);
        optionsMenu.setMnemonic(KeyEvent.VK_O);
        profileMenu.setMnemonic(KeyEvent.VK_E);
        discoItem.setMnemonic(KeyEvent.VK_S);
        registerItem.setMnemonic(KeyEvent.VK_R);
        
        this.blist = blist;
        
        initComponents();
    }
    
    public void logOn() {
        signOnItem.setText(resources.getString("signOff"));
    }
    
    public void logOff() {
        signOnItem.setText(resources.getString("signOn"));
    }
    
    /**
     * Sets up the visual components
     */
    private void initComponents() {
        MenuActionListener listener = new MenuActionListener();
        
        if (!JBother.kiosk_mode) {
            joinChatItem.addActionListener(new MenuActionListener());
            
            addBuddyItem.addActionListener(listener);
            blankItem.addActionListener(listener);
            signOnItem.addActionListener(listener);
            registerItem.addActionListener(listener);
            discoItem.addActionListener(listener);
            infoItem.addActionListener(listener);
            switchItem.addActionListener(listener);
            searchItem.addActionListener(listener);
            
            editItem.addActionListener(listener);
            
            add(addBuddyItem);
            
            addSeparator();
            
            add(blankItem);
            add(joinChatItem);
            
            addSeparator();
            
            servicesMenu.add(discoItem);
            servicesMenu.add(registerItem);
            servicesMenu.add(searchItem);
            
            add(servicesMenu);
            
            profileMenu.add(switchItem);
            profileMenu.add(editItem);
            profileMenu.add(infoItem);
            add(profileMenu);
            
            
            add(optionsMenu);
            if (System.getProperty("mrj.version") == null) {
                add(new BuddyListHelpMenu());
            }
            addSeparator();
            add(signOnItem);
        }
        
        quitItem.addActionListener(listener);
        add(quitItem);
    }
    
    /**
     * @param var
     *            true if this is OS X
     */
    public void setOSX(boolean var) {
        if (var) {
            remove(quitItem);
        } else {
//            if (!isMenuComponent(quitItem))
//                add(quitItem);
        }
        
        optionsMenu.setOSX(var);
    }
    
    /**
     * Listens for an item to be clicked
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class MenuActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == quitItem)
                blist.quitHandler();
            else if (e.getSource() == switchItem)
                switchHandler();
            else if (e.getSource() == editItem)
                editHandler();
            else if (e.getSource() == signOnItem)
                signOnHandler();
            else {
                if (!BuddyList.getInstance().checkConnection()) {
                    BuddyList.getInstance().connectionError();
                    return;
                }
                
                if (e.getSource() == addBuddyItem)
                    new AddBuddyDialog().setVisible(true);
                else if (e.getSource() == blankItem) {
                    MessagePanel panel = new MessagePanel();
                    MessageDelegator.getInstance().showPanel(panel);
                    MessageDelegator.getInstance().frontFrame(panel);
                } else if (e.getSource() == joinChatItem) {
                    GroupChatBookmarks gc = new GroupChatBookmarks(BuddyList.getInstance()
                    .getTabFrame());
                    gc.load();
                    gc.setVisible(true);
                } else if (e.getSource() == registerItem)
                    registrationHandler();
                else if (e.getSource() == discoItem)
                    new ServiceDiscoveryDialog(blist.getContainerFrame())
                    .setVisible(true);
                else if (e.getSource() == infoItem)
                    new InformationViewerDialog(BuddyList.getInstance()
                    .getConnection().getUser(), true);
                else if (e.getSource() == searchItem) {
                    searchHandler();
                }
            }
        }
    }
    
    private void signOnHandler() {
        if (!BuddyList.getInstance().checkConnection()) {
            ConnectorThread.getInstance().setCancelled(false);
            BuddyList.getInstance().setStatus(Presence.Mode.AVAILABLE,
            resources.getString("available"), false);
        } else
            signOffHandler();
    }
    
    private void signOffHandler() {
        ConnectorThread.getInstance().setCancelled(true);
        BuddyList.getInstance().getStatusMenu().signOffHandler();
    }
    
    private void editHandler() {
        ProfileEditorDialog dialog = new ProfileEditorDialog(BuddyList.getInstance().getContainerFrame(),null,
        ProfileManager.getCurrentProfile());
        dialog.setIsCurrentProfile(true);
        dialog.setVisible(true);
    }
    
    private void switchHandler() {
        if (BuddyList.getInstance().checkConnection()) {
            Standard.warningMessage(null, resources.getString("error"),
            resources.getString("stillConnected"));
        } else {
            BuddyList.getInstance().getContainerFrame().setVisible(false);
            new ProfileManager().setVisible(true);
        }
    }
    
    /**
     * Allows you to search for a user
     **/
    private void searchHandler() {
        
        String d = Settings.getInstance().getProperty("defaultSearchService", "users.jabber.org");
        
        String result = (String) JOptionPane.showInputDialog(null, resources
        .getString("pleaseEnterSearchServer"), resources
        .getString("registerForService"), JOptionPane.QUESTION_MESSAGE,
        null, null, d);  
        
        if(result != null && !result.equals(""))
        {
            Settings.getInstance().setProperty("defaultSearchService", d);
            new SearchDialog(result);
        }
    }
    
    /**
     * Registers for a server by displaying a RegistrationForm
     */
    private void registrationHandler() {
        String result = (String) JOptionPane.showInputDialog(null, resources
        .getString("pleaseEnterServer"), resources
        .getString("registerForService"), JOptionPane.QUESTION_MESSAGE,
        null, null, "");
        
        if (result != null && !result.equals("")) {
            RegistrationForm form = new RegistrationForm(BuddyList.getInstance().getContainerFrame(),result);
            form.getRegistrationInfo();
        }
    }
    
    public void showMenu( Component tree, int x, int y) {
        show( tree, x, y );
    }
    
    public void signOn() {
        logOn();
    }
    
    public void signOff() {
        logOff();
    }
    
    public JMenu getServicesMenu()
    {
        return servicesMenu;
    }
}