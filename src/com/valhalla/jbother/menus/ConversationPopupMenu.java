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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;

import com.valhalla.gui.Standard;
import com.valhalla.jbother.*;
import org.jivesoftware.smack.Roster;

/**
 * The menu that pops up when you right click on a ChatPanel
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class ConversationPopupMenu extends JPopupMenu {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private ConversationPanel window;

    private ConversationArea area;

    private JMenuItem log = new JMenuItem(resources.getString("viewLog"));

    private JMenuItem clear = new JMenuItem(resources.getString("clearWindow"));

    private JMenuItem block = new JMenuItem(resources.getString("blockUser"));

    private JMenuItem addperson = new JMenuItem(resources.getString("addBuddy"));

    private JMenuItem events = new JMenuItem(resources.getString("messageEvents"));

    protected JMenuItem sendFileItem = new JMenuItem(resources.getString("sendFile"));
    protected JMenu sendFileMenu = new JMenu(resources.getString("sendFile"));

    /**
     * Sets up the menu
     *
     * @param window
     *            the window to attach this menu to
     * @param area
     *            the conversation area to attach this menu to
     */
    public ConversationPopupMenu(final ConversationPanel window,
            final ConversationArea area) {
        this.window = window;
        this.area = area;

        initComponents();
    }

    public void disableBlock() {
        block.setEnabled(false);
    }

    /**
     * Sets up the visuals and event listeners
     */
    private void initComponents() {
        add(log);
        add(clear);
        add(block);
        add(events);
	add(sendFileItem);

        final String userId = window.getBuddy().getUser();

        try {
            Roster are = BuddyList.getInstance().getConnection().getRoster();
            if ( !( window instanceof ConsolePanel ) && !are.contains( userId ) ) {
                add(addperson);
            }
        }
        catch( NullPointerException ex ){ }

        log.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.openLogWindow();
            }
        });

        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                area.setText("");
            }
        });

        addperson.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Create AddBuddyDialog window.
                AddBuddyDialog addUser = new AddBuddyDialog();
                addUser.setVisible(true);

                // Set text of user in buddyIDBox.
                addUser.setBuddyId( userId );
            }
        });

        events.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFrame f = window.getFrame();
                    if (f == null) {
                        f = BuddyList.getInstance().getTabFrame();
                    }
                    Events events = new Events();
                    events.setEvents(f, window.getBuddy());
                }
            });

        block.addActionListener(new BlockActionListener());

	// Stellaris did this with help from synic
	sendFileItem.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e) {
	    new FileSendDialog(window.getBuddy().getAddress());
	  }
	});
    }


    /**
     * Adds a user to the blocked users list
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class BlockActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // just adds the user to the blocked list in buddy list
            File blockedFile = new File(JBother.profileDir + File.separator
                    + "blocked");
            BuddyList.getInstance().getBlockedUsers().put(
                    window.getBuddy().getUser(), "blocked");

            // and then writes all of them to the blocked users file
            try {
                FileWriter fw = new FileWriter(blockedFile);
                PrintWriter out = new PrintWriter(fw);

                Iterator i = BuddyList.getInstance().getBlockedUsers().keySet()
                        .iterator();
                while (i.hasNext()) {
                    out.println((String) i.next());
                }

                fw.close();
            } catch (IOException ex) {
                Standard.warningMessage(window, resources
                        .getString("blockUser"), resources
                        .getString("problemWritingBlockedFile"));
                return;
            }

            BuddyList.getInstance().getBuddyListTree().removeBuddy(
                    window.getBuddy(), window.getBuddy().getGroup(), true);

            Standard.noticeMessage(window, resources.getString("blockUser"),
                    resources.getString("userHasBeenBlocked"));
        }
    }
}