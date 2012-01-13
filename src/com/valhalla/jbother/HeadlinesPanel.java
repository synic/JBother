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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.valhalla.gui.CopyPasteContextMenu;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.settings.Settings;
import com.valhalla.jbother.menus.*;

/**
 * Handles Headline messages (server announces, RSS, etc.)
 *
 * @author Yury Soldak (tail)
 * @version 1.0
 */
public class HeadlinesPanel extends ConversationPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());
    private ConversationPopupMenu popMenu = new ConversationPopupMenu(this,
            conversationArea);

    /**
     * Default constructor
     *
     * @param buddy
     *            the Buddy that this window corresponds to
     * @param userId
     *            the user id of the buddy
     * @param buddyName
     *            the buddy's alias
     */
    public HeadlinesPanel(BuddyStatus buddy) {
        super(buddy);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        conversationArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(conversationArea);
        addListeners();
    }

    /**
     **/
    public void createFrame() {
        stopTimer();

        frame = new JFrame();
        frame.setContentPane(this);
        frame.pack();
        frame.setSize(new Dimension(400, 420));
        frame.setIconImage(Standard.getImage("frameicon.png"));

        Standard.cascadePlacement(frame);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (Settings.getInstance().getProperty("preserveMessages") == null) {
                    closeHandler();
                } else {
                    startTimer();
                    frame.setVisible(false);
                }
            }
        });

        frame.setTitle(buddy.getName() + " (" + buddy.getUser() + ")");
        validate();
    }

    /**
     * Adds the various event listeners for the components that are a part of
     * this frame
     */
    private void addListeners() {
        conversationArea.getTextPane().addKeyListener(new CTRLWHandler());
        conversationArea.getTextPane().addMouseListener(new RightClickListener(popMenu));
        CopyPasteContextMenu.registerComponent(conversationArea.getTextPane());
    }

    /**
     * Closes the Window if CTRL+W is pressed
     */
    class CTRLWHandler extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_W
                    && e.getModifiers() == Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()) {
                checkCloseHandler();
            }

            if (e.getKeyCode() == KeyEvent.VK_K
                    && e.getModifiers() == Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()) {
                closeHandler();
            }
        }
    }

    /**
     * Receives a message
     *
     * @param sbj
     *            the subject
     * @param body
     *            the message
     * @param resource
     *            the resource of the buddy
     */
    public void receiveMessage(String sbj, String delayInfo, String body, String resource,
            Date date, boolean flag, boolean flag2) {

        conversationArea.append(sbj + "\n\n", java.awt.Color.BLACK, true);
        conversationArea.append(body + "\n\n");

        super.receiveMessage();

    }

}