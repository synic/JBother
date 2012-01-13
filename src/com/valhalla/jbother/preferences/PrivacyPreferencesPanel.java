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

package com.valhalla.jbother.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import com.valhalla.gui.Standard;
import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.JBother;
import com.valhalla.settings.TempSettings;

/**
 * Allows the user to manage the User IDs mantained in the blocked user
 * Hashtable. If any user in this list sends us a message, it will be ignored
 * 
 * @author Adam Olsen
 * @version 1.0
 */
class PrivacyPreferencesPanel extends JPanel implements PreferencesPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JList blockedList = new JList();

    private PreferencesDialog prefs;

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    private JLabel label = new JLabel(resources.getString("blockedUsers") + " ");

    private JButton addButton = new JButton(resources.getString("addButton")),
            removeButton = new JButton(resources.getString("deleteButton"));

    // we use a clone of the blocked users so that no changes are written or
    // used
    // unless the user clicks OK or Apply in the preferences dialog.
    private Hashtable users = (Hashtable) BuddyList.getInstance()
            .getBlockedUsers().clone();

    /**
     * Sets up the panel
     * 
     * @param dialog
     *            the enclosing PreferencesDialog
     */
    public PrivacyPreferencesPanel(PreferencesDialog dialog) {
        this.prefs = dialog;
        setBorder(BorderFactory.createTitledBorder(resources
                .getString("privacySettings")));

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;

        blockedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        blockedList.setBorder(BorderFactory.createEtchedBorder());

        setLayout(grid);
        getBlocked();

        grid.setConstraints(label, c);
        add(label);

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridy++;

        grid.setConstraints(blockedList, c);

        add(blockedList);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createHorizontalGlue());

        c.gridy++;
        c.weightx = 0;
        c.weighty = 0;
        grid.setConstraints(buttonPanel, c);
        add(buttonPanel);
        addListeners();
    }

    /**
     * Adds listeners to different events in the panel
     */
    private void addListeners() {
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String item = (String) blockedList.getSelectedValue();
                if (item != null)
                    users.remove(item);
                getBlocked();
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // grab a user id to add to the list
                String result = (String) JOptionPane.showInputDialog(prefs,
                        resources.getString("enterIdToBlock"), resources
                                .getString("blockUser"),
                        JOptionPane.QUESTION_MESSAGE, null, null, null);

                if (result != null && !result.equals("")) {
                    users.put(result, "blocked");
                    getBlocked();
                }
            }
        });
    }

    /**
     * Set up the blocked users list
     */
    private void getBlocked() {
        ArrayList list = new ArrayList();
        Iterator i = users.keySet().iterator();
        while (i.hasNext())
            list.add(i.next());

        blockedList.setListData(list.toArray());
        blockedList.validate();
    }

    /**
     * This PreferencesPanel is different than the others in that it doesn't
     * actually change anything in Settings. Here we just return the same
     * Settings that were passed in, and write the new blocked users to the
     * blocked file
     */
    public TempSettings getSettings() {
        File blockedFile = new File(JBother.profileDir + File.separator
                + "blocked");

        try {
            FileWriter fw = new FileWriter(blockedFile);
            PrintWriter out = new PrintWriter(fw);

            Iterator i = users.keySet().iterator();
            while (i.hasNext()) {
                // write each user id on a new line
                out.println((String) i.next());
            }

            fw.close();
        } catch (IOException ex) {
            Standard.warningMessage(prefs, resources.getString("blockUser"),
                    resources.getString("problemWritingBlockedFile"));
        }
        BuddyList.getInstance().setBlockedUsers(users);

        return new TempSettings();
    }
}