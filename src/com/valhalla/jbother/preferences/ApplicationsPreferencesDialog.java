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
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.valhalla.gui.MJTextField;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.BuddyList;
import com.valhalla.misc.GnuPG;
import com.valhalla.settings.Settings;
import com.valhalla.settings.TempSettings;

/**
 * Allows the user to change default applications for hyperlinks in
 * ConversationArea
 * 
 * @author Adam Olsen
 * @version 1.0
 */
class ApplicationsPreferencesPanel extends JPanel implements PreferencesPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private PreferencesDialog prefs;

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    private MJTextField browserBox = new MJTextField(20);

    private JLabel browserLabel = new JLabel(resources
            .getString("internetBrowser")
            + ": ");

    private JButton browserChooser = new JButton(resources.getString("browse"));

    private MJTextField emailBox = new MJTextField(20);

    private JLabel emailLabel = new JLabel(resources.getString("emailClient")
            + ": ");

    private JButton emailChooser = new JButton(resources.getString("browse"));

    private MJTextField gpgBox = new MJTextField(20);

    private JLabel gpgLabel = new JLabel(resources.getString("gpgClient")
            + ": ");

    private JButton gpgChooser = new JButton(resources.getString("browse"));

    private JFileChooser fc = new JFileChooser();

    private boolean autoChecked = false;

    /**
     * Sets up the ApplicationPreferencesPanel
     * 
     * @param dialog
     *            the enclosing PreferencesDialog
     */
    public ApplicationsPreferencesPanel(PreferencesDialog dialog) {
        this.prefs = dialog;
        setBorder(BorderFactory.createTitledBorder(resources
                .getString("applicationPreferences")));
        setLayout(grid);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;

        //display name stuff
        browserLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        c.weightx = -.1;
        c.fill = GridBagConstraints.NONE;
        grid.setConstraints(browserLabel, c);
        add(browserLabel);

        c.gridx = 1;

        grid.setConstraints(browserBox, c);
        add(browserBox);

        c.gridx = 2;
        grid.setConstraints(browserChooser, c);
        add(browserChooser);

        c.gridy++;
        c.gridx = 0;
        //display name stuff
        emailLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        grid.setConstraints(emailLabel, c);
        add(emailLabel);

        c.gridx = 1;

        grid.setConstraints(emailBox, c);
        add(emailBox);

        c.gridx = 2;
        grid.setConstraints(emailChooser, c);
        add(emailChooser);

        c.gridy++;
        c.gridx = 0;
        //display name stuff
        gpgLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        grid.setConstraints(gpgLabel, c);
        add(gpgLabel);

        c.gridx = 1;

        grid.setConstraints(gpgBox, c);
        add(gpgBox);

        c.gridx = 2;
        grid.setConstraints(gpgChooser, c);
        add(gpgChooser);

        c.gridy++;
        c.gridx = 1;
        c.gridwidth = 2;
        JLabel hintLabel = new JLabel(resources.getString("applicationHint"));
        grid.setConstraints(hintLabel, c);
        add(hintLabel);

        //this is the space taker
        JLabel blankLabel = new JLabel("");
        c.weighty = 1;
        c.weightx = 1;
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy++;
        grid.setConstraints(blankLabel, c);
        add(blankLabel);

        loadSettings();
        initComponents();
    }

    /**
     * Adds listeners to the different events in the panel
     */
    private void initComponents() {
        ChooserActionListener listener = new ChooserActionListener();
        browserChooser.addActionListener(listener);
        emailChooser.addActionListener(listener);
        gpgChooser.addActionListener(listener);
    }

    /**
     * Listens for button events
     */
    class ChooserActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int result = fc.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (e.getSource() == browserChooser)
                    browserBox.setText(file.getPath());

                if (e.getSource() == emailChooser)
                    emailBox.setText(file.getPath());

                if (e.getSource() == gpgChooser)
                    gpgBox.setText(file.getPath());
            }
        }
    }

    /**
     * Loads settings from the settings file
     */
    private void loadSettings() {
        if (Settings.getInstance().getProperty("browserApplication") != null)
            browserBox.setText(Settings.getInstance().getProperty(
                    "browserApplication"));
        if (Settings.getInstance().getProperty("emailApplication") != null)
            emailBox.setText(Settings.getInstance().getProperty(
                    "emailApplication"));
        if (Settings.getInstance().getProperty("gpgApplication") != null)
            gpgBox
                    .setText(Settings.getInstance().getProperty(
                            "gpgApplication"));
    }

    /**
     * Returns currently selected settings
     * 
     * @return currently selected settings
     */
    public TempSettings getSettings() {
        TempSettings tempSettings = new TempSettings();
        tempSettings.setProperty("browserApplication", browserBox.getText());
        tempSettings.setProperty("emailApplication", emailBox.getText());
        tempSettings.setProperty("gpgApplication", gpgBox.getText());

        if (browserBox.getText().equals(""))
            tempSettings.setProperty("browserApplication", "!!EMPTY!!");
        if (emailBox.getText().equals(""))
            tempSettings.setProperty("emailApplication", "!!EMPTY!!");
        if (gpgBox.getText().equals(""))
            tempSettings.setProperty("gpgApplication", "!!EMPTY!!");

        if( Settings.getInstance().getProperty("gpgApplication") == null) Settings.getInstance().setProperty("gpgApplication", "");
        
        if ((Settings.getInstance().getProperty("gpgApplication").equals("") && !tempSettings
                .getProperty("gpgApplication").equals("!!EMPTY!!"))
                && (!Settings.getInstance().getProperty("gpgApplication")
                        .equals(tempSettings.getProperty("gpgApplication")))) {
            String command = tempSettings.getProperty("gpgApplication");
            if (command.equals("!!EMPTY!!"))
                command = "gpg";
            // check to make sure GnuPG is executable
            GnuPG gnupg = new GnuPG(command);
            boolean enabled = gnupg.listKeys("");
            if (!enabled) {
                Standard.warningMessage(prefs, "GnuPG Error",
                        "Invalid GPG path, GPG settings not applied.");
                tempSettings.setProperty("gpgApplication", Settings
                        .getInstance().getProperty("gpgApplication"));
                gpgBox.setText(tempSettings.getProperty("gpgApplication"));
                gpgBox.repaint();
            } else {
                if (BuddyList.getInstance().checkConnection()) {
                    Standard.noticeMessage(prefs, resources
                            .getString("applicationPreferences"), resources
                            .getString("reconnectToEnableGPG"));
                }
            }
        }

        return tempSettings;
    }
}