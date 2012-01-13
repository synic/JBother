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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.JBotherLoader;
import com.valhalla.settings.Settings;
import com.valhalla.settings.TempSettings;

/**
 * This is the preferences dialog. It is basically a container for the different
 * PreferencesPanels, which are swapped in and out as the user selects items
 * from the preferences tree.
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class PreferencesDialog extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private PreferencesTree tree;

    private JPanel container = new JPanel();

    private JPanel rightSide = new JPanel();

    private JPanel buttonPanel = new JPanel();

    private JPanel prefsPanel = new JPanel(new GridLayout(0, 1));

    private JButton cancelButton = new JButton(resources
            .getString("cancelButton")), okButton = new JButton(resources
            .getString("okButton")), applyButton = new JButton(resources
            .getString("applyButton"));

    private boolean ok = true;

    private TreeMap panels = new TreeMap();

    private JPanel current;

    private static TreeMap pluginPanels = new TreeMap();

    /**
     * Creates the PreferencesDialog
     */
    public PreferencesDialog() {
        super(JBotherLoader.getParentFrame(), "Preferences", false);
        setTitle(resources.getString("preferences"));

        // add the panels
        current = new GeneralPreferencesPanel(this);
        panels.put("a01 " + resources.getString("general"), current);
        panels.put("a02 " + resources.getString("eventPrefs"), new NotificationPreferencesPanel(this));
        panels.put("a03 " + resources.getString("applications"),
                new ApplicationsPreferencesPanel(this));
        panels.put("a04 " + resources.getString("sounds"),
                new SoundsPreferencesPanel(this));
        panels.put("a05 " + resources.getString("appearance"),
                new AppearancePreferencesPanel(this));
        panels.put("a06 " + resources.getString("tabs"),
                new TabsPreferencesPanel(this));
        panels.put("a07 " + resources.getString("privacy"),
                new PrivacyPreferencesPanel(this));
        // this is commented because I've upgraded JBother to use smack's
        // built in file transfer, and I can't see any way of setting a proxy
        // or anything
        //panels.put("a08 " + resources.getString("dataTransfer"),
        //        new DataTransferPreferencesPanel( this ) );
        panels.put("a09 " + resources.getString("pluginsMirrorSettings"),
                new PluginDownloaderPreferencesPanel( this ));
        initComponents();

        container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pack();
        setLocationRelativeTo(null);
        DialogTracker.addDialog(this, false, true);
    }

    public static void registerPluginPanel(String name, JPanel panel) {
        pluginPanels.put(name, panel);
    }

    public static TreeMap getPluginPanels() {
        return pluginPanels;
    }

    public TreeMap getPanels() {
        return panels;
    }

    public static void removePluginPanel(String name) {
        pluginPanels.remove(name);
    }

    public JPanel getTree() {
        return tree;
    }

    /**
     * Sets up the visual components
     */
    private void initComponents() {
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));

        tree = new PreferencesTree(this);
        container.add(tree);

        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        container.add(rightSide);

        // set the general preferences panel as the default selected panel
        prefsPanel.add(current);
        rightSide.add(prefsPanel);

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(okButton);

        rightSide.add(Box.createRigidArea(new Dimension(0, 5)));
        rightSide.add(buttonPanel);
        rightSide.setPreferredSize(new Dimension(520, 400));

        //set up the handlers
        PrefsActionHandler handler = new PrefsActionHandler();
        cancelButton.addActionListener(handler);
        okButton.addActionListener(handler);
        applyButton.addActionListener(handler);

        setContentPane(container);
    }

    /**
     * Listens for events on the different buttons
     */
    class PrefsActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == cancelButton) {
                DialogTracker.removeDialog(PreferencesDialog.this);
            }

            if (e.getSource() == okButton)
                writeSettings();
            if (e.getSource() == applyButton)
                applyHandler();
        }
    }

    /**
     * Applies the settings
     */
    private void applyHandler() {
        PreferencesPanel panel = (PreferencesPanel) current;
        applySettings(panel.getSettings());
        Standard.noticeMessage(this, resources.getString("applySettings"),
                resources.getString("settingsHaveBeenApplied"));
    }

    /**
     * Applies the specified Settings
     *
     * @param settings
     *            the settings to apply
     */
    private void applySettings(TempSettings temp) {
        Iterator iterator = temp.keySet().iterator();

        while (iterator.hasNext()) {
            String key = (String) iterator.next();

            if (temp.getProperty(key).equals("!!REMOVED!!"))
                Settings.getInstance().setBoolean(key, false);
            else if (temp.getProperty(key).equals("!!EMPTY!!"))
                Settings.getInstance().remove(key);
            else
                Settings.getInstance().setProperty(key, temp.getProperty(key));

        }

        BuddyList.getInstance().updateIcons();
    }

    /**
     * Switches the currently displayed preferences panel to the specified one
     *
     * @param string
     *            the panel to switch to
     */
    protected void switchPanel(String string) {
        com.valhalla.Logger.debug("Switching to panel " + string);
        prefsPanel.remove(current);

        JPanel panel = (JPanel) panels.get(string);
        if (panel == null) {
            switchToPluginPanel(string);
            return;
        }

        prefsPanel.add(panel);
        current = panel;
        validate();
        repaint();
    }

    private void switchToPluginPanel(String string) {
        com.valhalla.Logger.debug("Switching to plugin panel " + string);
        JPanel panel = (JPanel) pluginPanels.get(string);
        if (panel == null)
            return;
        prefsPanel.add(panel);
        current = panel;
        validate();
        repaint();
    }

    /**
     * Writes the settings to the settings file
     */
    private void writeSettings() {
        Iterator i = panels.keySet().iterator();

        while (i.hasNext()) {
            JPanel panel = (JPanel) panels.get(i.next());
            if (panel != null) {
                PreferencesPanel prefsPanel = (PreferencesPanel) panel;
                applySettings(prefsPanel.getSettings());
            }
        }
        JBotherLoader.checkGPG();

        if (pluginPanels.size() > 0) {
            i = pluginPanels.keySet().iterator();

            while (i.hasNext()) {
                JPanel panel = (JPanel) pluginPanels.get(i.next());
                if (panel != null) {
                    PreferencesPanel prefsPanel = (PreferencesPanel) panel;
                    applySettings(prefsPanel.getSettings());
                }
            }
        }

        DialogTracker.removeDialog(this);
    }

}
