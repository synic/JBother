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

import java.awt.Component;
import java.awt.event.*;


import javax.swing.*;

import com.valhalla.jbother.jabber.BuddyStatus;


/**
 * @author Andrey Zakirov
 * @since April 10, 2005
 */

public class Events {

    private BuddyStatus buddy;


    private JCheckBox checkDelivered = new JCheckBox();
    private JCheckBox checkDisplayed = new JCheckBox();
    private JCheckBox checkOffline = new JCheckBox();

    public Events()
    {
    }

    public boolean setEvents(JFrame window, BuddyStatus buddy ) {
        this.buddy  = buddy;
        JDialog dialog = new JDialog(window);
        JPanel panel = (JPanel) dialog.getContentPane();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel ("Notify when message is:");

        checkDelivered.setText("delivered");
        checkDelivered.setSelected(buddy.isAskForDelivered());
        checkDisplayed.setText("displayed");
        checkDisplayed.setSelected(buddy.isAskForDisplayed());
        checkOffline.setText("offline");
        checkOffline.setSelected(buddy.isAskForOffline());
        dialog.getContentPane().add(label);
        dialog.getContentPane().add(checkDelivered);
        dialog.getContentPane().add(checkDisplayed);
        dialog.getContentPane().add(checkOffline);
        dialog.pack();
        dialog.setLocationRelativeTo(window);
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeHandler();
            }
        });
        return true;
    }




    public void checkCloseHandler() {
        closeHandler();
    }

    public void closeHandler() {
      buddy.isAskForDelivered(checkDelivered.isSelected());
      buddy.isAskForDisplayed(checkDisplayed.isSelected());
      buddy.isAskForOffline(checkOffline.isSelected());
    }


}

