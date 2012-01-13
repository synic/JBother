/*
 *  Copyright (C) 2003 Adam Olsen
 *
 *  This program is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 1, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 *  Ave, Cambridge, MA 02139, USA.
 */
package com.valhalla.jbother;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import com.valhalla.jbother.groupchat.*;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;

import com.valhalla.settings.Settings;

/**
 *  Description of the Class
 *
 *@author     synic
 *@created    May 18, 2005
 */
public class NotificationPopup extends JWindow {
    private Timer timer = new Timer(3000, new DestroyListener());

    private JLabel messageLabel = new JLabel();

    private static NotificationPopup instance;

    private Window focusWindow;
    private Container focusComponent;


    /**
     *  Constructor for the NotificationPopup object
     */
    private NotificationPopup() {
        super((JFrame) null);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();

        JPanel panel = (JPanel) getContentPane();
        panel.setLayout(new BorderLayout());

        messageLabel.addMouseListener(
            new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (focusWindow != null) {
                        focusWindow.setVisible(false);
                        focusWindow.setVisible(true);
                        if(focusWindow instanceof JFrame) ((JFrame)focusWindow).setExtendedState(JFrame.NORMAL);

                        focusWindow.toFront();
                    }

                    if( focusComponent instanceof ChatRoomPanel )
                    {
                        BuddyList.getInstance().getTabFrame().getTabPane().setSelectedTab(((ChatRoomPanel)focusComponent).getTab());
                    }

                    setVisible(false);
                }
            });

        setFocusableWindowState(false);
        panel.add(messageLabel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setBackground(new Color(247, 255, 117));
        pack();
        setSize(new Dimension(200, 60));
    }


    /**
     *  Description of the Method
     *
     *@param  focusWindow     Description of the Parameter
     *@param  title           Description of the Parameter
     *@param  message         Description of the Parameter
     *@param  focusComponent  Description of the Parameter
     */
    public static void showSingleton(Window focusWindow, String title,
            String message, Container focusComponent) {
        if(!Settings.getInstance().getBoolean("usePopup")) return;
        if (instance == null) {
            instance = new NotificationPopup();
        }

        if (focusComponent != null && checkFocus(focusComponent)) {
            return;
        }
        if (BuddyList.getInstance().getCurrentPresenceMode() == org.jivesoftware.smack.packet.Presence.Mode.DO_NOT_DISTURB) {
            return;
        }

        instance.setLocation();
        instance.focusWindow = focusWindow;
        instance.focusComponent = focusComponent;

        StringBuffer mess = new StringBuffer();

        URL light = instance.getClass().getClassLoader().getResource(
                "images/lightbulb.png");

        mess.append("<html><table><tr><td valign='top' width='2%'>").append(
                "<img src='").append(light.toString()).append("'></td>")
                .append("<td valign='top'><b>").append(title)
                .append("</b><br>").append(message).append(
                "</td></tr></table></html>");

        instance.messageLabel.setText(mess.toString());
        instance.setVisible(true);
        if (instance.timer.isRunning()) {
            instance.timer.restart();
        } else {
            instance.timer.start();
        }
    }


    /**
     *  Description of the Method
     *
     *@param  container  Description of the Parameter
     *@return            Description of the Return Value
     */
    private static boolean checkFocus(Container container) {
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof Container) {
                if (checkFocus((Container) components[i])) {
                    return true;
                }
                if (components[i].hasFocus()) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     *  Sets the location attribute of the NotificationPopup object
     */
    private void setLocation() {
        int npopupx = 100;
        int npopupy = 100;
        int npopupw = 200;
        int npopuph = 60;

        try {
            npopupx = Integer.parseInt(Settings.getInstance().getProperty(
                    "NPopupX"));
            npopupy = Integer.parseInt(Settings.getInstance().getProperty(
                    "NPopupY"));
            npopupw = Integer.parseInt(Settings.getInstance().getProperty(
                    "NPopupW"));
            npopuph = Integer.parseInt(Settings.getInstance().getProperty(
                    "NPopupH")) - 20;
        } catch (Exception e) {
        }

        setLocation(npopupx, npopupy + 20);
        setSize(npopupw, npopuph);
    }


    /**
     *  Description of the Class
     *
     *@author     synic
     *@created    May 18, 2005
     */
    private class DestroyListener implements ActionListener {
        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            focusComponent = null;
            dispose();
        }
    }
}
