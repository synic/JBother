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
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import com.valhalla.gui.MJTextField;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.ConversationPanel;
import com.valhalla.jbother.MessageDelegator;
import com.valhalla.settings.Settings;
import com.valhalla.settings.TempSettings;

/**
 * Allows the user to change General Preferences
 *
 * @author Adam Olsen
 * @version 1.0
 */
class NotificationPreferencesPanel extends JPanel implements PreferencesPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private PreferencesDialog prefs;

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    private JCheckBox focusWindow = new JCheckBox(resources
            .getString("focusConversations"));

    private JCheckBox usePopup = new JCheckBox(resources.getString("usePopup"));
    private JCheckBox popupForSignOn = new JCheckBox(resources.getString("popupForSignOn"));
    private JCheckBox popupForSignOff = new JCheckBox(resources.getString("popupForSignOff"));
    private JCheckBox popupForGroupMessage = new JCheckBox(resources.getString("popupForGroupMessage"));

    private int npopupx = 100;

    private int npopupy = 100;
    private int npopupw = 200;
    private int npopuph = 82;

    private NotificationWindow nw = null;

    /**
     * Creates the PreferencesPanel
     *
     * @param dialog
     *            the enclosing PreferencesDialog
     */
    public NotificationPreferencesPanel(PreferencesDialog dialog) {
        this.prefs = dialog;
        setBorder(BorderFactory.createTitledBorder(resources
                .getString("generalPreferences")));
        setLayout(grid);

        usePopup.addActionListener(new NPMouseListener());

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = .5;
        c.gridwidth = 2;
        grid.setConstraints(focusWindow, c);
        add(focusWindow);

        c.gridy++;
        grid.setConstraints(usePopup, c);
        add(usePopup);

        c.gridy++;
        grid.setConstraints(popupForSignOn, c);
        add(popupForSignOn);

        c.gridy++;
        grid.setConstraints(popupForSignOff, c);
        add(popupForSignOff);

        c.gridy++;
        grid.setConstraints(popupForGroupMessage, c);
        add(popupForGroupMessage);

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

    private void enableDisable(boolean selected)
    {
        popupForSignOn.setEnabled(selected);
        popupForSignOff.setEnabled(selected);
        popupForGroupMessage.setEnabled(selected);
    }

    private class NPMouseListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (usePopup.isSelected() && nw == null) {
                Standard.warningMessage(prefs, resources
                        .getString("preferences"), resources
                        .getString("usePopupDescription"));
                nw = new NotificationWindow();
            } else if (!usePopup.isSelected() && nw != null) {
                nw.dispose();
                nw = null;
            }

            enableDisable(usePopup.isSelected());
        }
    }

    /**
     * Loads settings from the settings file and fills out the form for defaults
     */
    private void loadSettings() {
        focusWindow.setSelected(Settings.getInstance()
                .getBoolean("focusWindow"));
        usePopup.setSelected(Settings.getInstance().getBoolean("usePopup"));

        try {
            npopupx = Integer.parseInt(Settings.getInstance().getProperty(
                    "NPopupX"));
            npopupy = Integer.parseInt(Settings.getInstance().getProperty(
                    "NPopupY"));
            npopupw = Integer.parseInt(Settings.getInstance().getProperty("NPopupW"));
            npopuph = Integer.parseInt(Settings.getInstance().getProperty("NPopupH"));
        } catch (Exception e) {
        }

        popupForSignOn.setSelected(Settings.getInstance().getBoolean("popupForSignOn"));
        popupForSignOff.setSelected(Settings.getInstance().getBoolean("popupForSignOff"));
        popupForGroupMessage.setSelected(Settings.getInstance().getBoolean("popupForGroupMessage"));
        enableDisable(usePopup.isSelected());
    }

    /**
     * Returns the currently chosen settings
     */
    public TempSettings getSettings() {
        TempSettings mySettings = new TempSettings();

        mySettings.setBoolean("focusWindow", focusWindow.isSelected());
        mySettings.setBoolean("usePopup", usePopup.isSelected());

        mySettings.setBoolean("popupForSignOn", popupForSignOn.isSelected());
        mySettings.setBoolean("popupForSignOff", popupForSignOff.isSelected());
        mySettings.setBoolean("popupForGroupMessage", popupForGroupMessage.isSelected());

        if (nw != null) {
            nw.saveLocation();
            nw.dispose();
        }

        mySettings.setProperty("NPopupX", "" + npopupx);
        mySettings.setProperty("NPopupY", "" + npopupy);
        mySettings.setProperty("NPopupW", "" + npopupw);
        mySettings.setProperty("NPopupH", "" + npopuph);

        return mySettings;
    }

    class NotificationWindow extends JFrame {
        public NotificationWindow() {
            super("Drag & Close");

            JPanel panel = (JPanel) getContentPane();
            panel.setLayout(new BorderLayout());

            URL light = getClass().getClassLoader().getResource(
                    "images/lightbulb.png");

            StringBuffer mess = new StringBuffer();
            mess.append("<html><table><tr><td valign='top' width='2%'>")
                    .append("<img src='").append(light.toString()).append(
                            "'></td>").append(
                            "<td valign='top'><b>Notification").append(
                            "</b><br>Drag & Close").append(
                            "</td></tr></table></html>");

            panel.add(new JLabel(mess.toString()), BorderLayout.CENTER);
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            panel.setBackground(new Color(247, 255, 117));
            pack();
            setSize(new Dimension(npopupw, npopuph));

            setLocation(npopupx, npopupy);

            setVisible(true);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent ex) {
                    nw = null;
                    saveLocation();
                }
            });
            toFront();

        }

        public void saveLocation() {
            Point p = getLocation();
            npopupx = (int) p.getX();
            npopupy = (int) p.getY();
            Dimension dim = getSize();
            npopupw = (int)dim.getWidth();
            npopuph = (int)dim.getHeight();
        }

    }
}