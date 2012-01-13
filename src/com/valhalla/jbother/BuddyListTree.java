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
package com.valhalla.jbother;

//includes
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.dotuseful.ui.tree.AutomatedTreeModel;
import org.dotuseful.ui.tree.AutomatedTreeNode;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import com.valhalla.jbother.jabber.BuddyGroup;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.menus.BuddyListPopupMenu;
import com.valhalla.jbother.menus.BuddyListTransportMenu;
import com.valhalla.jbother.menus.GroupPopupMenu;
import com.valhalla.settings.Settings;

/**
 * BuddyListTree is the part of the buddy list dialog that draws the buddies and
 * their groups from your Jabber roster. It also displays different pictures for
 * different statuses.
 *
 * @author Adam Olsen
 * @created March 2, 2005
 * @version 1.0
 */
public class BuddyListTree extends JPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private XMPPConnection connection;

    private Roster roster;

    private AutomatedTreeNode root = new AutomatedTreeNode("Buddies");

    private AutomatedTreeModel model = new AutomatedTreeModel(root);

    private JTree tree = new JTree(model);

    private JScrollPane scrollPane = new JScrollPane(tree);

    private BuddyListPopupMenu buddyPopupMenu = new BuddyListPopupMenu();

    private GroupPopupMenu groupPopupMenu = new GroupPopupMenu();

    private BuddyListTransportMenu buddyTransportMenu = new BuddyListTransportMenu();

    private boolean showOfflineBuddies = Settings.getInstance().getBoolean(
            "showOfflineBuddies");

    private boolean showUnfiledBuddies = Settings.getInstance().getBoolean(
            "showUnfiledBuddies");

    private boolean showAgentBuddies = Settings.getInstance().getBoolean(
            "showAgentBuddies");

    private boolean sortByStatus = Settings.getInstance().getBoolean(
            "sortByStatus");

    private boolean showAwayBuddies = !Settings.getInstance().getBoolean("dontShowAwayBuddies");

    private BuddyListRenderer renderer = new BuddyListRenderer();

    protected Hashtable totalEntriesPerGroup = new Hashtable();

    protected Hashtable onlineEntriesPerGroup = new Hashtable();

    protected Hashtable groups = new Hashtable();

    private BuddyListExpansionListener expandListener = new BuddyListExpansionListener();

    private TreeMap buddyGroups = new TreeMap();

    // to sort the buddy groups

    /**
     * Sets up the tree
     */
    public BuddyListTree() {
        setLayout(new GridLayout(0, 1));
        setBackground(Color.WHITE);

        tree.setCellRenderer(renderer);
        tree.setRootVisible(false);
        tree.setRowHeight(0);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addMouseListener(new PopupMouseListener());
        tree.addTreeExpansionListener(expandListener);

        ToolTipManager.sharedInstance().registerComponent(tree);
        add(scrollPane);
    }

    public void updateUI() {
        super.updateUI();
        if (buddyPopupMenu != null) {
            buddyPopupMenu = new BuddyListPopupMenu();
            groupPopupMenu = new GroupPopupMenu();
        }
    }

    /**
     * Gets the popMenu attribute of the BuddyListTree object
     *
     * @return The popMenu value
     */
    public BuddyListPopupMenu getPopMenu() {
        return buddyPopupMenu;
    }

    /**
     * Gets the groupPopMenu attribute of the BuddyListTree object
     *
     * @return The popMenu value
     */
    public GroupPopupMenu getGroupMenu() {
        return groupPopupMenu;
    }

    /**
     * Returns the JTree
     *
     * @return the actual JTree swing component
     */
    public JTree getTree() {
        return this.tree;
    }

    /**
     * Sets the JTree's XMPPConnection
     *
     * @param connection
     *            the current connection
     */
    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
        if (connection == null) {
            return;
        }
        this.roster = ConnectorThread.getInstance().getRoster();
    }

    /**
     * Sets whether or not to show the offline buddies
     *
     * @param show
     *            true to show the offline buddies
     */
    public void setShowOfflineBuddies(boolean show) {
        this.showOfflineBuddies = show;
        Settings.getInstance().setBoolean("showOfflineBuddies", show);

        reloadBuddies();
    }

    public void setShowAwayBuddies(boolean show) {
        this.showAwayBuddies = show;
        Settings.getInstance().setBoolean("doneShowAwayBuddies", !show);
        reloadBuddies();
    }

    /**
     * Sets whether or not to show the unfiled buddies
     *
     * @param show
     *            true to show unfiled buddies
     */
    public void setShowUnfiledBuddies(boolean show) {
        this.showUnfiledBuddies = show;
        Settings.getInstance().setBoolean("showUnfiledBuddies", show);
        reloadBuddies();
    }

    /**
     * Sets whether or not to show agents/transports
     *
     * @param show
     *            true to show agents and transports
     */
    public void setShowAgentBuddies(boolean show) {
        this.showAgentBuddies = show;
        Settings.getInstance().setBoolean("showAgentBuddies", show);
        reloadBuddies();
    }

    /**
     * set to true if you want to sort your buddies by status
     *
     * @param show
     *            true to sort by status
     */
    public void setSortByStatus(boolean show) {
        this.sortByStatus = show;
        Settings.getInstance().setBoolean("sortByStatus", show);
        reloadBuddies();
    }

    /**
     * @return true if sort by status is set
     */
    public boolean getSortByStatus() {
        return this.sortByStatus;
    }

    /**
     * To listen to when a group gets expanded. Saves it in the settings for
     * session restoration
     *
     * @author Adam Olsen
     * @created March 2, 2005
     * @version 1.0
     */
    static class BuddyListExpansionListener implements TreeExpansionListener {
        /**
         * Listens for a tree collapse event
         *
         * @param e
         *            Description of the Parameter
         */
        public void treeCollapsed(TreeExpansionEvent e) {
            TreePath path = e.getPath();
            AutomatedTreeNode node = (AutomatedTreeNode) path
                    .getLastPathComponent();

            if (node.getUserObject() instanceof BuddyGroup) {
                Settings.getInstance().setProperty(
                        "groupExpandStatus_"
                                + ((BuddyGroup) node.getUserObject())
                                        .getGroupName(), "collapsed");
            }
        }

        /**
         * Listens for a tree expand event
         *
         * @param e
         *            Description of the Parameter
         */
        public void treeExpanded(TreeExpansionEvent e) {
            TreePath path = e.getPath();
            AutomatedTreeNode node = (AutomatedTreeNode) path
                    .getLastPathComponent();

            if (node.getUserObject() instanceof BuddyGroup) {
                Settings.getInstance().remove(
                        "groupExpandStatus_"
                                + ((BuddyGroup) node.getUserObject())
                                        .getGroupName());
            }
        }
    }

    /**
     * Redraws the JTree
     */
    public void reloadBuddies() {
        reloadBuddies(false);
    }

    /**
     * Shows all the current offline buddies
     */
    public void loadOfflineBuddies() {
        reloadBuddies(true);
    }

    /**
     * Redraws the JTree
     *
     * @param loadOffline
     *            whether or not to just load the offline buddies
     */
    public void reloadBuddies(final boolean loadOffline) {
        showOfflineBuddies = Settings.getInstance().getBoolean(
                "showOfflineBuddies");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (connection == null) {
                    return;
                }
                if (roster == null) {
                    roster = ConnectorThread.getInstance().getRoster();
                    roster.reload();
                }

                if (!loadOffline) {
                    clearBuddies();
                }

                // loop through all the RosterEntries and see if they need to be
                // added to the
                // BuddyList tree
                Iterator it = roster.getEntries();
                while (it.hasNext()) {
                    RosterEntry entry = (RosterEntry) it.next();
                    BuddyStatus buddy = BuddyList.getInstance().getBuddyStatus(
                            entry.getUser());

                    checkAddEntry(buddy);

                }

                tree.repaint();
                tree.validate();
            }
        });
    }

    /**
     * Clears all the buddies from the JTree
     */
    public void clearBuddies() {
        com.valhalla.Logger.debug("Clearing Buddies");
        // clear the JTree and the BuddyGroups TreeMap
        root.removeAllChildren();
        buddyGroups.clear();
        totalEntriesPerGroup.clear();
    }

    /**
     * We store all of the Group names in a TreeMap so that we can get the sort
     * index when inserting the TreeNode for this group into the root node
     *
     * @param group
     *            the name of the group
     * @return The groupIndex value
     */
    private int getGroupIndex(String group) {
        //synchronized (buddyGroups) {
            if (buddyGroups.containsKey(group)) {
                return -1;
            }
            // we want General Contacts and Agents/Transports to always
            // sort at the bottom
            if (group.equals(resources.getString("contactsGroup"))) {
                group = "zzz Contacts";
            } else if (group.equals(resources.getString("transportsGroup"))) {
                group = "zzzz Agents/Transports";
            }

            buddyGroups.put(group, new TreeMap());

            int count = 0;

            // find the index of the newly sorted group
            Iterator i = buddyGroups.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                if (key.equals(group)) {
                    break;
                }
                count++;
            }

            return count;
        //}
    }

    /**
     * Finds out the sort index of this particular buddy
     *
     * @param group
     *            the group the buddy is in
     * @param buddy
     *            the buddy to find the index of
     * @return The buddyIndex value
     */
    private int getBuddyIndex(String group, BuddyStatus buddy) {
        //synchronized (buddyGroups) {
            if (group.equals(resources.getString("contactsGroup"))) {
                group = "zzz Contacts";
            } else if (group.equals(resources.getString("transportsGroup"))) {
                group = "zzzz Agents/Transports";
            } else if (group.equals(resources.getString("notInRoster"))) {
                group = "zzzz Not In Roster";
            }

            String name = buddy.getName();
            if (name == null) {
                name = buddy.getUser();
            }
            name = name.toLowerCase() + buddy.getUser().toLowerCase();

            if (sortByStatus) {
                // sort by status
                Presence.Mode mode = buddy.getPresence(buddy
                        .getHighestResource());
                if (mode == null) {
                    name = "zzz9555 " + name;
                } else if (mode == Presence.Mode.AVAILABLE) {
                    name = "aa " + name;
                } else if (mode == Presence.Mode.CHAT) {
                    name = "zzz9111 " + name;
                } else if (mode == Presence.Mode.AWAY) {
                    name = "zzz9222 " + name;
                } else if (mode == Presence.Mode.EXTENDED_AWAY) {
                    name = "zzz9333 " + name;
                } else if (mode == Presence.Mode.DO_NOT_DISTURB) {
                    name = "zzz9444 " + name;
                }

                if (buddy.size() <= 0) {
                    name = "zzz9555 " + name;
                }
            }

            if (buddyGroups.get(group) == null) {
                buddyGroups.put(group, new TreeMap());
            }

            ((TreeMap) buddyGroups.get(group)).put(name, buddy.getUser());

            int count = 0;

            Iterator i = ((TreeMap) buddyGroups.get(group)).keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                if (key.equals(name)) {
                    break;
                }
                count++;
            }
            return count;
        //}
    }

    /**
     * Checks to see if the group is already in the tree, and returns the index
     * of it
     *
     * @param group
     *            the group to check
     * @return Description of the Return Value
     */
    public AutomatedTreeNode checkGroup(BuddyGroup group) {
        boolean check = false;
        AutomatedTreeNode node = new AutomatedTreeNode(group);

        Enumeration children = root.children();

        // find out if the group alread exists
        whileLoop: while (children.hasMoreElements()) {
            AutomatedTreeNode theNode = (AutomatedTreeNode) children
                    .nextElement();
            BuddyGroup g = (BuddyGroup) theNode.getUserObject();
            if (g.getGroupName().equals(group.getGroupName())) {
                node = theNode;
                check = true;
                break whileLoop;
            }
        }

        if (root.isNodeChild(node)) {
            return node;
        }

        final String tempGroup = group.getGroupName();
        final AutomatedTreeNode tempNode = node;

        if (!check) {
            int num = getGroupIndex(tempGroup);
            if (num >= 0) {
                root.insert(tempNode, num);
            }
        }

        group.setNode(node);

        return node;

    }

    private BuddyGroup getGroupObject(String name) {
        String temp = name.replaceAll(" ([^)]*)$", "");

        if (!showUnfiledBuddies
                && temp.equals(resources.getString("contactsGroup"))) {
            return null;
        }
        if (!showAgentBuddies
                && temp.equals(resources.getString("transportsGroup"))) {
            return null;
        }
        BuddyGroup group = (BuddyGroup) groups.get(name);
        if (group == null)
            group = new BuddyGroup(name);
        groups.put(name, group);
        return group;
    }

    /**
     * finds out if the buddy should be displayed in the BuddyListTree. If so
     * the buddy is added to the tree
     *
     * @param buddy
     *            the buddy to add
     */
    public void checkAddEntry(final BuddyStatus buddy) {
        if (buddy == null) {
            return;
        }

        boolean add = false;

        // if we are set to show the offline buddies then add the buddy to the
        // tree
        if (showOfflineBuddies) {
            add = true;
        } else {
            // otherwise we have to find out if the buddy is online before we
            // add it
            if (buddy.size() > 0) {
                add = true;
            }
        }

        if(!showAwayBuddies)
        {
            Presence.Mode mode = buddy.getPresence(buddy.getHighestResource());
            if( mode != null && mode != Presence.Mode.AVAILABLE ) add = false;
        }

        if (buddy.getRemoved()) {
            add = false;
        }

        String tempGroup = buddy.getTempGroup();
        if (tempGroup == null) {
            tempGroup = buddy.getGroup();
        }

        final String group = tempGroup;

        if( buddy.getRosterEntry() == null ) add = false;

        BuddyGroup gObj = getGroupObject(group);
        if (gObj == null)
            return;

        gObj.addBuddy(buddy);

        if (add) {
            final AutomatedTreeNode node = checkGroup(gObj);
            node.setUserObject(gObj);

            // find the group that the buddy belongs to
            int index = getBuddyIndex(gObj.getGroupName(), buddy);

            if (!isInTree(buddy)) {
                node.insert(new AutomatedTreeNode(buddy), index);
            }

            TreePath parent = new TreePath(root);
            tree.expandPath(parent);

            // find out if we need to expand this group
            String property = Settings.getInstance().getProperty(
                    "groupExpandStatus_" + group);
            if (property == null || !property.equals("collapsed")) {
                tree.expandPath(parent.pathByAddingChild(node));
            }
        }

    }

    /**
     * Returns whether or not the buddy is in the tree
     *
     * @param buddy
     *            the buddy to check
     * @return true if the buddy is in the tree
     */
    public boolean isInTree(BuddyStatus buddy) {
        String group = buddy.getGroup();

        // loop through all the groups until we find the group that this
        // buddy belongs to
        Enumeration children = root.children();
        while (children.hasMoreElements()) {
            AutomatedTreeNode node = (AutomatedTreeNode) children.nextElement();

            // once we find it's group, loop through all the buddies in that
            // group
            if (((BuddyGroup) node.getUserObject()).getGroupName()
                    .equals(group)) {
                Enumeration leafs = node.children();
                while (leafs.hasMoreElements()) {
                    AutomatedTreeNode leaf = (AutomatedTreeNode) leafs
                            .nextElement();
                    if (leaf.getUserObject() == buddy) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Adds a buddy to the tree - if it's not already in the tree
     *
     * @param buddy
     *            the buddy to add
     */
    public void addBuddy(final BuddyStatus buddy) {
        checkAddEntry(buddy);

        validate();
        repaint();
    }

    /**
     * Removes the buddy from the tree
     *
     * @param buddy
     *            the buddy to remove
     * @param group
     *            the group the buddy is in
     */
    public void removeBuddy(final BuddyStatus buddy, final String group,
            final boolean removeFromGroup) {
        // loop through all the groups until we find the group that this
        // buddy belongs to
        Enumeration children = root.children();
        while (children.hasMoreElements()) {
            AutomatedTreeNode node = (AutomatedTreeNode) children.nextElement();

            // once we find it's group, loop through all the buddies in that
            // group
            if (((BuddyGroup) node.getUserObject()).getGroupName()
                    .equals(group)) {
                Enumeration leafs = node.children();
                while (leafs.hasMoreElements()) {
                    AutomatedTreeNode leaf = (AutomatedTreeNode) leafs
                            .nextElement();
                    Object check = leaf.getUserObject();
                    if (check instanceof BuddyStatus) {
                        BuddyStatus temp = (BuddyStatus) check;

                        if (temp.getUser().equals(buddy.getUser())) {
                            // once we find the buddy, remove it
                            removeBuddyNode(node, leaf);
                            if (removeFromGroup) {
                                BuddyGroup g = getGroupObject(group);
                                g.removeBuddy(buddy);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * removes this buddy from the JTree. If this was the last buddy in the
     * group then remove the group from being displayed
     *
     * @param node
     *            the group node
     * @param leaf
     *            the leaf node
     */
    private void removeBuddyNode(final AutomatedTreeNode node,
            final AutomatedTreeNode leaf) {
        String group = ((BuddyGroup) node.getUserObject()).getGroupName();

        if (group == null || group.equals(resources.getString("contactsGroup"))) {
            group = "zzz Contacts";
        } else if (group.equals(resources.getString("transportsGroup"))) {
            group = "zzzz Agents/Transports";
        }

        BuddyStatus buddy = (BuddyStatus) leaf.getUserObject();

        String name = buddy.getName();
        if (name == null) {
            name = buddy.getUser();
        }
        String jid = buddy.getUser();

       // synchronized (buddyGroups) {
            TreeMap buddies = ((TreeMap) buddyGroups.get(group));
            Iterator i = buddies.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                if (buddies.get(key).equals(jid)) {
                    ((TreeMap) buddyGroups.get(group)).remove(key);
                    break;
                }
            }

       // }

        node.remove(leaf);
        if (node.getChildCount() <= 0) {
            buddyGroups.remove(group);
            root.remove(node);
        }

        tree.repaint();
    }

    /**
     * Starts a conversation if someone double double clicks on a buddy
     */
    public void initiateConversation(BuddyStatus buddy) {
        if (buddy == null) {
            if (tree.getSelectionPath() == null) {
                return;
            }

            TreePath path = tree.getSelectionPath();
            AutomatedTreeNode node = (AutomatedTreeNode) path
                    .getLastPathComponent();
            if (model.isLeaf(node)) {
                try {
                    buddy = (BuddyStatus) node.getUserObject();
                } catch (ClassCastException ex) {
                    return;
                }
            }
        }

        if (buddy == null)
            return;

        if (buddy.getConversation() == null) {
            ChatPanel conver = new ChatPanel(buddy);
            buddy.setConversation(conver);

            MessageDelegator.getInstance().showPanel(buddy.getConversation());
            MessageDelegator.getInstance().frontFrame(buddy.getConversation());
        } else {
            MessageDelegator.getInstance().showPanel(buddy.getConversation());
            MessageDelegator.getInstance().frontFrame(buddy.getConversation());
        }

        buddy.getConversation().stopTimer();
    }

    /**
     * Listens for mouse events in the tree
     *
     * @author Adam Olsen
     * @created March 2, 2005
     * @version 1.0
     */
    class PopupMouseListener extends MouseAdapter {
        /**
         * Description of the Method
         *
         * @param e
         *            Description of the Parameter
         */
        public void mousePressed(MouseEvent e) {
            checkPop(e);
        }

        /**
         * Description of the Method
         *
         * @param e
         *            Description of the Parameter
         */
        public void mouseReleased(MouseEvent e) {
            checkPop(e);
        }

        /**
         * Description of the Method
         *
         * @param e
         *            Description of the Parameter
         */
        public void mouseClicked(MouseEvent e) {
            checkPop(e);
            try {
                JTree tree = (JTree) e.getComponent();
            } catch (ClassCastException ex) {
                return;
            }

            if (e.getClickCount() >= 2) {
                e.consume();
                initiateConversation(null);
            }
        }

        /**
         * Checks if we need to display the buddy list popup menu Shows it if
         * needs be
         *
         * @param e
         *            Description of the Parameter
         */
        public void checkPop(MouseEvent e) {
            BuddyStatus buddy = null;

            if (e.isPopupTrigger()) {
                try {

                    JTree tree = (JTree) e.getComponent();

                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) {
                        throw new ClassCastException();
                    }

                    tree.setSelectionPath(path);

                    AutomatedTreeNode node = (AutomatedTreeNode) path
                            .getLastPathComponent();
                    Object selectedUserObject = node.getUserObject();
                    if (selectedUserObject.getClass().equals(BuddyStatus.class)) {
                        buddy = (BuddyStatus) node.getUserObject();
                        buddyPopupMenu.showMenu(e.getComponent(), e.getX(), e
                                .getY(), buddy);
                    } else if (selectedUserObject.getClass().equals(
                            BuddyGroup.class)) {
                        // buddies that are not in any group are put in
                        // "Contacts" group by JBother
                        // if we need to send a message to them, we need to
                        // extract them from the tree
                        // because there is no way to do it from the roster
                        Enumeration iChildren = node.children();
                        String usersList = new String();
                        while (iChildren.hasMoreElements())
                        {
                            AutomatedTreeNode o = (AutomatedTreeNode) iChildren
                                    .nextElement();
                            BuddyStatus buddyStatus = (BuddyStatus) o
                                    .getUserObject();
                            usersList += buddyStatus.getUser() + MessagePanel.RECIPIENTS_DELIMITER + " ";
                        }
                        // get rid of 'unneeded delimiter + space' string at the end
                        usersList = usersList.substring(0, usersList.length() -
                          (MessagePanel.RECIPIENTS_DELIMITER.length() + 1));
                        groupPopupMenu.showMenu(e.getComponent(), e.getX(), e
                                .getY(), usersList,
                                (BuddyGroup) selectedUserObject);
                    }
                    buddy = (BuddyStatus) node.getUserObject();

                    if (buddy.getUser().indexOf("@") == -1) {
                        buddyTransportMenu.showMenu(e.getComponent(), e.getX(),
                                e.getY(), buddy);
                    } else {
                        buddyPopupMenu.showMenu(e.getComponent(), e.getX(), e
                                .getY(), buddy);
                    }
                } catch (ClassCastException ex) {
                    /*
                     * is not a buddy, so don't display the menu
                     */
                }
            }
        }
    }
}