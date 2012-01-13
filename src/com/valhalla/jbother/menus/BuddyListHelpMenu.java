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
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.valhalla.gui.DialogTracker;
import com.valhalla.jbother.AboutDialog;

/**
 * The JBother help menu
 */
class BuddyListHelpMenu extends JMenu {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JMenuItem aboutItem = new JMenuItem(resources.getString("about"));

    /**
     * Creates the Help Menu
     */
    public BuddyListHelpMenu() {
        super("Help");
        setText(resources.getString("help"));
        setMnemonic(KeyEvent.VK_H);
        aboutItem.setMnemonic(KeyEvent.VK_A);

        aboutItem.addActionListener(new MenuActionListener());
        add(aboutItem);
    }

    /**
     * Waits for an item to be clicked in the menu
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class MenuActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == aboutItem)
                if (!DialogTracker.containsDialog(AboutDialog.class))
                    new AboutDialog().setVisible(true);
        }
    }
}