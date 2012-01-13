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
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.event.*;

import com.valhalla.gui.CopyPasteContextMenu;
import com.valhalla.gui.MJTextArea;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.smack.Blank;
import com.valhalla.jbother.menus.ConversationPopupMenu;
import com.valhalla.settings.Settings;

/**
 * Handles XML packet exchange between client and server.
 *
 * @author Andrey Zakirov
 * @created April 10, 2005
 * @version 0.1
 */

public class EventPanel extends ConversationPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JSplitPane container;

    private JScrollPane conversationScroll;

    private JPanel buttonPanel = new JPanel();

    private JPanel scrollPanel = new JPanel(new GridLayout(1, 0));

    private boolean divSetUp = false;

    private ConversationPopupMenu popMenu = new ConversationPopupMenu(this,
            conversationArea);


    /**
     * Sets up the ConsolePanel - creates all visual components and adds event
     * listeners
     *
     * @param buddy
     *            the buddy to associate with
     */
    public EventPanel(BuddyStatus buddy) {
        super(buddy);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textEntryArea.setLineWrap(true);
        textEntryArea.setWrapStyleWord(true);


        scrollPanel.add(conversationArea);

        container = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPanel,
                new JScrollPane(textEntryArea)
                                    );
        container.setResizeWeight(1);

        JPanel containerPanel = new JPanel();
        containerPanel
                .setLayout(new BoxLayout(containerPanel, BoxLayout.X_AXIS));
        containerPanel.add(container);

        conversationArea.getTextPane().addMouseListener(new RightClickListener(popMenu));
        CopyPasteContextMenu.registerComponent(conversationArea.getTextPane());
        popMenu.disableBlock();

//        add(containerPanel);
        add(scrollPanel);

        addListeners();
    }

    public void messageEvent(String text) {

            final String text2 = text;

            SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                receiveMessage();
                conversationArea.append(getDate(null) + " **** " + text2, ConversationArea.BLACK, true);
            }
        });


       }


    private String getMessageTemplate() {
        return "<message id=\"\" to=\"\" type=\"\"><body></body></message>";
    }

    /**
     * @return the input area of this panel
     */

    /**
     * Adds the various event listeners for the components that are a part of
     * this frame
     */
    private void addListeners() {


        Action checkCloseAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                checkCloseHandler();
            }
        };

        Action closeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeHandler();
            }
        };


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

    }

    public String getPanelName() {
        return resources.getString("eventConsole");
    }

    /**
     * Displays a "disconnected" message"
     */

    /**
     * Recieves a message
     *
     * @param sbj
     *            Description of the Parameter
     * @param message
     *            Description of the Parameter
     * @param resource
     *            Description of the Parameter
     * @param date
     *            Description of the Parameter
     */

    /**
     * Sends the message in the TextEntryArea
     */
    /**
     * Creates the containing frame
     */
    public void createFrame() {
        frame = new JFrame();
        frame.setContentPane(this);
        frame.pack();

        frame.setIconImage(Standard.getImage("frameicon.png"));

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeHandler();
            }
        });

        frame.setTitle(resources.getString("eventConsole"));
        frame.pack();

        String stringWidth = Settings.getInstance().getProperty(
                "EventconversationWindowWidth");
        String stringHeight = Settings.getInstance().getProperty(
                "EventconversationWindowHeight");

        if (stringWidth == null) {
            stringWidth = "400";
        }
        if (stringHeight == null) {
            stringHeight = "340";
        }

        frame.setSize(new Dimension(Integer.parseInt(stringWidth), Integer
                .parseInt(stringHeight)));

        // add a resize window listener
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Dimension dim = frame.getSize();
                Settings.getInstance().setProperty(
                        "EventconversationWindowWidth",
                        new Integer((int) dim.getWidth()).toString());
                Settings.getInstance().setProperty(
                        "EventconversationWindowHeight",
                        new Integer((int) dim.getHeight()).toString());
            }
        });
        Standard.cascadePlacement(frame);

        frame.setVisible(true);
    }

    /**
     * Description of the Method
     */
    public void checkCloseHandler() {
        closeHandler();
    }

}



