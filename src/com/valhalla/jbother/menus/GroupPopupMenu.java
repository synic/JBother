/*
 author: Lukasz Wiechec
 */
package com.valhalla.jbother.menus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import org.jivesoftware.smack.RosterGroup;

import com.valhalla.gui.Standard;
import com.valhalla.jbother.*;
import com.valhalla.jbother.jabber.BuddyGroup;

/**
 * The menu that pops up when you right click on a group in roster
 *
 * @author Lukasz Wiechec
 * @created March 2, 2005
 * @version 1.0
 */
public class GroupPopupMenu extends JPopupMenu {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JMenuItem sendMessageToGroupItem = new JMenuItem(resources
            .getString("sendMessageToGroup"));

    private JMenuItem rename = new JMenuItem(resources.getString("renameGroup"));

    private String userList;

    private JTree tree;

    private BuddyGroup group;

    /**
     * Creates the menu
     */
    public GroupPopupMenu() {
        MenuActionListener listener = new MenuActionListener();
        sendMessageToGroupItem.addActionListener(listener);
        rename.addActionListener(listener);

        add(sendMessageToGroupItem);
        add(rename);
    }

    /**
     * Requests sending message to a group
     */
    private void sendMessageToGroupHandler() {
        // question: how to handle users that not belong to any group?
        // they are displayed in BuddyListTree in group "Contacts"
        // 1) get it from the tree!
        com.valhalla.Logger.debug("message to group handler called!");

        MessagePanel groupMessageWindow = new MessagePanel();
        groupMessageWindow.setTo(userList);
        // trim the last "; "
        groupMessageWindow.setVisible(true);
        MessageDelegator.getInstance().showPanel(groupMessageWindow);
        MessageDelegator.getInstance().frontFrame(groupMessageWindow);
        groupMessageWindow.getSubjectField().grabFocus();

    }

    private void renameHandler() {
        RosterGroup g = ConnectorThread.getInstance().getRoster()
                .getGroup(group.getGroupName());
        if (g == null) {
            Standard.warningMessage(
                    BuddyList.getInstance().getContainerFrame(), resources
                            .getString("renameGroup"), resources
                            .getString("cannotRenameGroup"));
            return;
        }

        final String result = (String) JOptionPane.showInputDialog(BuddyList
                .getInstance().getContainerFrame(), resources
                .getString("newGroupName"), resources.getString("renameGroup"),
                JOptionPane.QUESTION_MESSAGE, null, null, "");

        if (result == null || result.equals(""))
            return;

        new Thread(new RenameThread(result)).start();
    }

    public String getUserList()
    {
        return userList;
    }

    class RenameThread implements Runnable {
        private String newName;

        public RenameThread(String name) {
            this.newName = name;
        }

        public void run() {
            if (!BuddyList.getInstance().checkConnection())
                return;
            RosterGroup g = ConnectorThread.getInstance().getRoster()
                    .getGroup(group.getGroupName());
            if (g == null)
                return;

            String error = null;
            try {
                g.setName(newName);
            } catch (Exception ex) {
                error = ex.getMessage();
            }

            final String er = error;

            try {
                Thread.sleep(1000);
            } catch (Exception yo) {
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (er != null) {
                        Standard.warningMessage(BuddyList.getInstance()
                                .getContainerFrame(), resources
                                .getString("renameGroup"), er);
                    }

                    BuddyList.getInstance().getBuddyListTree().reloadBuddies();
                }
            });
        }
    }

    /**
     * Listens for different items to get clicked on in the popup menu
     *
     * @author Adam Olsen
     * @created March 2, 2005
     * @version 1.0
     */
    class MenuActionListener implements ActionListener {
        /**
         * Description of the Method
         *
         * @param e
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == sendMessageToGroupItem) {
                sendMessageToGroupHandler();
            } else if (e.getSource() == rename) {
                renameHandler();
            }
        }
    }

    /**
     * Shows the popup menu
     *
     * @param tree
     *            the tree to show the menu on
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param aUserList
     *            the list of users (separated by '; ') to send the message to
     */
    public void showMenu(Component tree, int x, int y, String aUserList,
            BuddyGroup group) {
        this.userList = aUserList;
        this.tree = (JTree) tree;
        this.group = group;

        validate();

        show(tree, x, y);
    }
}

