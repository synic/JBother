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
package com.valhalla.jbother;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import net.infonode.tabbedpanel.*;
import net.infonode.tabbedpanel.titledtab.*;
import net.infonode.util.*;

import com.valhalla.settings.Settings;

/**
 * Displays a ConversationPanel in the TabFrame or in a containing JFrame
 * depending on whether or not the application is set to use a tabbed window
 *
 * @author Adam Olsen
 * @created Oct 25, 2004
 * @version 1.1
 */
public class MessageDelegator {
    private static MessageDelegator instance = null;

    private Vector panels = new Vector();

    private FocusTimer timer = new FocusTimer();

    private javax.swing.Timer t = new javax.swing.Timer(150, timer);

    private static ConversationPanel currentPanel;

    /**
     * Default constructor... private for singleton
     */
    private MessageDelegator() {
    }

    /**
     * @return the MessageDelegator instance
     */
    public static MessageDelegator getInstance() {
        if (instance == null) {
            instance = new MessageDelegator();
        }
        return instance;
    }

    /**
     * Shows a panel using the TabFrame or a containing frame
     *
     * @param panel
     *            the panel to show
     */
    public void showPanel(ConversationPanel panel) {
        if (Settings.getInstance().getBoolean("useTabbedWindow")) {
            BuddyList.getInstance().startTabFrame();
            if (!BuddyList.getInstance().getTabFrame().contains(panel)) {
                BuddyList.getInstance().addTabPanel(panel);

                if (panel instanceof ChatPanel) {
                    ((ChatPanel) panel).setUpDivider();
                } else if (panel instanceof ConsolePanel) {
                    ((ConsolePanel) panel).setUpDivider();
                }
            }
        } else {
            if (panel.getContainingFrame() == null) {
                panel.createFrame();
            }
        }

        if (!panels.contains(panel)) {
            panels.add(panel);
        }
    }

    /**
     * If the panel is contained in a JFrame, this method brings that frame to
     * the front of the screen
     *
     * @param panel
     *            the panel containing the frame to bring to the front
     */
    public void frontFrame(final ConversationPanel panel) {
        if (Settings.getInstance().getBoolean("useTabbedWindow")) {
            TabbedPanel pane = BuddyList.getInstance().getTabFrame()
                    .getTabPane();


            pane.setSelectedTab(((TabFramePanel)panel).getTab());
            currentPanel = panel;

            if (!t.isRunning())
                t.start();
            else
                t.restart();

            return;
        }

        JFrame frame = panel.getContainingFrame();

        if (frame != null) {
            frame.setVisible(true);
            frame.toFront();
        }

        panel.getInputComponent().requestFocus();
    }

    class FocusTimer implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            t.stop();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    currentPanel.getInputComponent().requestFocus();
                }
            });
        }
    }

    /**
     * Removes a panel from the panels Vector
     *
     * @param panel
     *            the panel to remove
     */
    public void removePanel(ConversationPanel panel) {
        if (panel instanceof ChatPanel) {
            ((ChatPanel) panel).removeDividerListener();
        } else if (panel instanceof ConsolePanel) {
            ((ConsolePanel) panel).removeDividerListener();
        }

        panels.remove(panel);
        if(currentPanel == panel) currentPanel = null;
    }

    /**
     * @return the Vector containing a list of all the available
     *         ConversationPanels
     */
    public Vector getPanels() {
        return panels;
    }
}

