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
package com.valhalla.jbother.groupchat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.*;

import com.valhalla.gui.Standard;
import com.valhalla.jbother.*;
import com.valhalla.jbother.ConversationFormatter;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.MUCBuddyStatus;

/**
 * The JPanel that contains the nickname list
 *
 * @author     Adam Olsen
 * @created    October 28, 2005
 * @version    1.0
 */
public class GroupChatNickList extends JPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private NickListModel nickListModel = new NickListModel();

    private JList nickList = new JList(nickListModel);

    private JScrollPane scrollPane = new JScrollPane(nickList);

    private JButton clear = new JButton(Standard.getIcon("images/buttons/New24.gif"));

    private JButton emoticons = new JButton(Standard.getIcon("images/buttons/smiley.gif"));

    private JButton configure = new JButton(Standard.getIcon("images/buttons/preferences.gif"));

    private ChatRoomPanel window;

    private JLabel countLabel = new JLabel("0 users");
    private int userCount = 0;
    private int adminCount = 0;

    private NickListPopupMenu popMenu;


    /**
     * Sets up the panel
     *
     * @param  window              the chatroom window that this nicklist is a part of
     */
    public GroupChatNickList(final ChatRoomPanel window) {
        super();

        popMenu = new NickListPopupMenu(window);
        this.window = window;
        setLayout(new BorderLayout());

        add(countLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        emoticons.setPreferredSize(new Dimension(26, 26));
        clear.setPreferredSize(new Dimension(26, 26));
        configure.setPreferredSize(new Dimension(26, 26));

        buttons.add(emoticons);
        buttons.add(clear);
        buttons.add(configure);
        countLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JScrollPane scroll = new JScrollPane(buttons);
        scroll.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        bottomPanel.add(scroll, BorderLayout.WEST);

        clear.setToolTipText(resources.getString("clear"));
        configure.setToolTipText(resources.getString("configureRoom"));
        add(bottomPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(120, 400));
        NickListRenderer renderer = new NickListRenderer(window);
        nickList.setCellRenderer(renderer);
        nickList.addMouseListener(new DoubleClickListener());

        emoticons.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ConversationFormatter.getInstance().displayEmoticonChooser(
                            BuddyList.getInstance().getTabFrame(), emoticons,
                            window.getTextEntryArea());
                }
            });

        clear.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    window.getConversationArea().setText("");
                }
            });

        configure.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    window.configurationHandler("configure");
                }
            });

    }


    /**
     *  Description of the Method
     */
    public void clear() {
        nickListModel.clear();
    }


    /**
     * Gets the JList
     *
     * @return    the JList
     */
    public JList getList() {
        return nickList;
    }


    /**
     * Adds a buddy to the JList (when they sign on)
     *
     * @param  buddy              the buddy to add
     */
    public void addBuddy(String buddy) {
        if (nickListModel.contains(buddy)) {
            return;
        }
        nickListModel.addBuddy(buddy);
    }


    /**
     *  Description of the Method
     *
     * @param  buddy  Description of the Parameter
     * @return        Description of the Return Value
     */
    public boolean contains(String buddy) {
        return nickListModel.contains(buddy);
    }


    /**
     *  Description of the Method
     */
    public void redraw() {
        adminCount = 0;
        userCount = 0;
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    nickListModel.clear();
                    Hashtable table = window.getBuddyStatuses();
                    ArrayList removers = new ArrayList();
                    for (Iterator i = table.keySet().iterator(); i.hasNext(); ) {
                        MUCBuddyStatus buddy = (MUCBuddyStatus) table.get(i.next());
                        if (buddy.size() <= 0 || !buddy.getIsInRoom()) {
                            removers.add(buddy);
                            continue;
                        }
                        addBuddy(buddy.getUser());
                    }
                    repaint();

                    for (int i = 0; i < removers.size(); i++) {
                        window.getBuddyStatuses().remove(removers.get(i));
                    }
                }

            });
    }


    /**
     * Removes a buddy from the JList
     *
     * @param  buddy              the buddy to remove
     */
    public void removeBuddy(String buddy) {
        if (!window.getBuddyStatuses().containsKey(buddy)) {
            return;
        }
        if (!nickListModel.contains(buddy)) {
            return;
        }
        nickListModel.removeBuddy(buddy);
    }


    /**
     *  Description of the Method
     *
     * @param  buddy  Description of the Parameter
     * @return        Description of the Return Value
     */
    public boolean contains(BuddyStatus buddy) {
        return nickListModel.contains(buddy.getUser());
    }


    /**
     * The model that represents the list of buddies in the room
     *
     * @author     Adam Olsen
     * @created    October 28, 2005
     * @version    1.0
     */
    class NickListModel extends AbstractListModel {
        private ArrayList buddies = new ArrayList();

        private Object[] buddyNames = null;


        /**
         *  Description of the Method
         */
        public void clear() {
            buddies.clear();
            fireChanged();
        }


        /**
         *  Description of the Method
         *
         * @param  buddy  Description of the Parameter
         * @return        Description of the Return Value
         */
        public boolean contains(String buddy) {

            MUCBuddyStatus b = window.getBuddyStatus(buddy);
            String a = b.getAffiliation();
            if (a != null) {
                if (a.equals("owner")) {
                    buddy = "aa_aa1111 " + buddy;
                }
                if (a.equals("admin")) {
                    buddy = "aa_aa1222 " + buddy;
                }
            }
            return buddies.contains(buddy);
        }


        /**
         * @return    the number of elements in the list
         */
        public int getSize() {
            if (buddyNames == null) {
                return 0;
            }

            return buddyNames.length;
        }


        /**
         * @param  row              the element you want to get
         * @return      the Object at <tt>row</tt>
         */
        public Object getElementAt(int row) {
            return buddyNames[row];
        }


        /**
         * @param  buddy              the buddy to add
         */
        public void addBuddy(String buddy) {

            MUCBuddyStatus b = window.getBuddyStatus(buddy);
            String a = b.getAffiliation();
            boolean admin = false;
            if (a != null) {
                if (a.equals("owner")) {
                    buddy = "aa_aa1111 " + buddy;
                    admin = true;
                } else if (a.equals("admin")) {
                    buddy = "aa_aa1222 " + buddy;
                    admin = true;
                }
            }

            buddies.add(buddy);

            if (!admin) {
                userCount++;
            } else {
                adminCount++;
            }

            String text = "";
            String append = "";

            if (adminCount > 1 || adminCount == 0) {
                append = "s";
            }
            text = adminCount + " admin" + append;

            text += ", " + (userCount + adminCount) + " total";

            countLabel.setText(text);

            fireChanged();
        }


        /**
         * Removes a buddy from the list
         *
         * @param  buddy  Description of the Parameter
         */
        public void removeBuddy(String buddy) {
            int row = 0;
            boolean found = false;

            MUCBuddyStatus b = window.getBuddyStatus(buddy);
            String a = b.getAffiliation();
            boolean admin = false;
            if (a != null) {
                if (a.equals("owner")) {
                    buddy = "aa_aa1111 " + buddy;
                    admin = true;
                } else if (a.equals("admin")) {
                    buddy = "aa_aa1222 " + buddy;
                    admin = true;
                }
            }

            for (int i = 0; i < buddies.size(); i++) {
                String item = (String) buddies.get(i);
                if (item.equals(buddy)) {
                    found = true;
                    row = i;
                }
            }

            if (!admin) {
                userCount--;
            } else {
                adminCount--;
            }

            String text = "";
            String append = "";

            if (adminCount > 1 || adminCount == 0) {
                append = "s";
            }
            text = adminCount + " admin" + append;
            text += ", " + (userCount + adminCount) + " total";

            if (found) {

                buddies.remove(row);

                countLabel.setText(text);
                fireChanged();
            }
        }


        /**
         * Fires a change of the list
         */
        private void fireChanged() {
            buddyNames = buddies.toArray();

            Arrays.sort(buddyNames,
                new Comparator() {
                    public int compare(Object string1, Object string2) {
                        String s1 = ((String) string1).toLowerCase();
                        String s2 = ((String) string2).toLowerCase();
                        return s1.compareTo(s2);
                    }


                    public boolean equals(Object o) {
                        return false;
                    }
                });

            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        fireContentsChanged(NickListModel.this, 0, buddyNames.length);
                        nickList.repaint();
                        nickList.validate();
                    }
                });
        }
    }


    /**
     * Listens for mouse events in the JList
     *
     * @author     Adam Olsen
     * @created    October 28, 2005
     * @version    1.0
     */
    class DoubleClickListener extends MouseAdapter {
        MUCBuddyStatus buddy = null;


        /**
         * Description of the Method
         *
         * @param  e              Description of the Parameter
         */
        public void mousePressed(MouseEvent e) {
            checkPop(e);
        }


        /**
         * Description of the Method
         *
         * @param  e              Description of the Parameter
         */
        public void mouseReleased(MouseEvent e) {
            checkPop(e);
        }


        /**
         * Description of the Method
         *
         * @param  e              Description of the Parameter
         */
        public void mouseClicked(MouseEvent e) {
            checkPop(e);
            if (e.getClickCount() >= 2) {
                JList list = (JList) e.getComponent();

                MUCBuddyStatus buddy = window.getBuddyStatus(((String) list.getSelectedValue()).replaceAll("^aa_aa\\d{4} ", ""));
                BuddyList.getInstance().getBuddyListTree()
                        .initiateConversation(buddy);
            }
        }


        /**
         * Shows the popup menu
         *
         * @param  e  Description of the Parameter
         */
        public void checkPop(MouseEvent e) {
            if (e.isPopupTrigger()) {
                try {

                    JList list = (JList) e.getComponent();
                    int index = list.locationToIndex(e.getPoint());
                    list.setSelectedIndex(index);

                    String user = ((String) list.getSelectedValue()).replaceAll("^aa_aa\\d{4} ", "");
                    buddy = window.getBuddyStatus(user);
                    popMenu.showMenu(e.getComponent(), e.getX(), e.getY(),
                            buddy);
                } catch (ClassCastException ex) {
                    /*
                     *  is not a buddy, so don't
                     *  display the menu
                     */
                }
            }
        }
    }
}
