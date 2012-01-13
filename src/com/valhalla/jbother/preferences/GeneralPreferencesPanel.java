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

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;

import javax.swing.*;

import com.valhalla.gui.*;
import com.valhalla.jbother.*;
import com.valhalla.settings.*;

/**
 * Allows the user to change General Preferences
 *
 * @author Adam Olsen
 * @version 1.0
 */
class GeneralPreferencesPanel extends JPanel implements PreferencesPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private PreferencesDialog prefs;

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();


    private JCheckBox keepLogs = new JCheckBox(resources.getString("keepLogs"));

    private JCheckBox preserve = new JCheckBox(resources
            .getString("preserveClosed"));

    private JTextField awayTime = new JTextField(4);

    private JCheckBox reportIdleTime = new JCheckBox(resources.getString("reportIdleTime"));

    private JCheckBox showNumbers = new JCheckBox(resources
            .getString("showNumbersInGroups"));

    private JCheckBox sendTypingNotification = new JCheckBox(resources
            .getString("sendTypingNotification"));

    private MJTextField displayName = new MJTextField(15);

    private JLabel displayLabel = new JLabel(resources
            .getString("displayedName") + " ");

    /**
     * Creates the PreferencesPanel
     *
     * @param dialog
     *            the enclosing PreferencesDialog
     */
    public GeneralPreferencesPanel(PreferencesDialog dialog) {
        this.prefs = dialog;
        setBorder(BorderFactory.createTitledBorder(resources
                .getString("generalPreferences")));
        setLayout(grid);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = .5;
        c.gridwidth = 2;

        c.gridy++;
        grid.setConstraints(sendTypingNotification, c);
        add(sendTypingNotification);

        c.gridy++;
        grid.setConstraints(keepLogs, c);
        add(keepLogs);

        c.gridy++;
        grid.setConstraints(preserve, c);
        add(preserve);

        c.gridwidth = 1;

        c.gridy++;
        JLabel autoAway = new JLabel(resources.getString("setAutoAway") + ":  ");
        autoAway.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JPanel awayPanel = new JPanel();
        awayPanel.setLayout(new BoxLayout(awayPanel, BoxLayout.X_AXIS));
        awayPanel.add(autoAway);
        awayPanel.add(awayTime);
        awayPanel.add(Box.createHorizontalGlue());

        c.gridwidth = 2;
        c.weightx = .0;
        c.fill = GridBagConstraints.NONE;

        grid.setConstraints(awayPanel, c);
        add(awayPanel);
        c.gridx = 0;

        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy++;
        grid.setConstraints(reportIdleTime,c);
        add(reportIdleTime);

        c.gridy++;
        grid.setConstraints(showNumbers, c);
        add(showNumbers);

        //display name stuff
        displayLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        c.gridwidth = 1;
        c.gridy++;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        grid.setConstraints(displayLabel, c);
        add(displayLabel);
        c.gridx = 1;
        c.weightx = 1.0;

        grid.setConstraints(displayName, c);
        add(displayName);

        //this is the space taker
        JLabel blankLabel = new JLabel("");
        c.weighty = .5;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        grid.setConstraints(blankLabel, c);
        add(blankLabel);

        loadSettings();
    }

    /**
     * Loads settings from the settings file and fills out the form for defaults
     */
    private void loadSettings() {
        displayName.setText(Settings.getInstance().getProperty(
                "myDisplayedName"));
        keepLogs.setSelected(Settings.getInstance().getBoolean("keepLogs"));
        preserve.setSelected(Settings.getInstance().getBoolean(
                "preserveMessages"));
        sendTypingNotification.setSelected(Settings.getInstance().getBoolean(
                "sendTypingNotification"));
        showNumbers.setSelected(Settings.getInstance().getBoolean(
                "showNumbersInGroups"));
        reportIdleTime.setSelected(Settings.getInstance().getBoolean("reportIdleTime"));

        awayTime.setText(Settings.getInstance().getProperty("autoAwayMinutes", "15"));
    }

    /**
     * Returns the currently chosen settings
     */
    public TempSettings getSettings() {
        TempSettings mySettings = new TempSettings();

        mySettings.setBoolean("keepLogs", keepLogs.isSelected());
        mySettings.setBoolean("preserveMessages", preserve.isSelected());
        mySettings.setProperty("myDisplayedName", displayName.getText());
        mySettings.setBoolean("sendTypingNotification", sendTypingNotification
                .isSelected());
        mySettings.setBoolean("showNumbersInGroups", showNumbers.isSelected());
        mySettings.setBoolean("reportIdleTime",reportIdleTime.isSelected());

        int aTime = 0;
        try {
            aTime = Integer.parseInt(awayTime.getText());
        }
        catch( Exception e ) { }
        mySettings.setProperty("autoAwayMinutes", aTime + "" );
        BuddyList.getInstance().resetAwayTimer( aTime );

        if (Settings.getInstance().getBoolean("showNumbersInGroups") != showNumbers
                .isSelected()) {
            BuddyList.getInstance().getBuddyListTree().reloadBuddies();
        }

        return mySettings;
    }
}