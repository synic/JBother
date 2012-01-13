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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.valhalla.gui.MJTextField;
import com.valhalla.gui.Standard;
import com.valhalla.settings.Settings;
import com.valhalla.settings.TempSettings;

/**
 * Allows you to change the sounds preferences
 *
 * @author Adam Olsen
 * @version 1.0
 */
class SoundsPreferencesPanel extends JPanel implements PreferencesPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private PreferencesDialog prefs;

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    private JLabel soundMethod = new JLabel(resources.getString("method")
            + ": ");

    private JLabel command = new JLabel(resources.getString("command") + ": ");

    private JLabel soundLabel = new JLabel(resources.getString("soundHint"));

    private JComboBox methodBox;

    private MJTextField commandBox = new MJTextField(15);

    private JLabel soundSet = new JLabel( resources.getString( "defaultSoundSet" ) + ": " );

    private int numberOfBoxes = 5;

    private int current = 0;

    //message received
    private String settingNames[] = new String[numberOfBoxes];

    private JLabel soundLabels[] = new JLabel[numberOfBoxes];

    private JCheckBox soundPlay[] = new JCheckBox[numberOfBoxes];

    private MJTextField soundBoxes[] = new MJTextField[numberOfBoxes];

    private JButton soundButtons[] = new JButton[numberOfBoxes];

    private JButton testButtons[] = new JButton[numberOfBoxes];

    private JButton defaultsButton = new JButton(resources
            .getString("defaults"));

    private JFileChooser fc = new JFileChooser();

    private boolean autoChecked = false;
    private String[] sets = new String[] { "default", "micro", "synthetic" };
    private JComboBox soundSetBox = new JComboBox( sets );
    private String currentDefault = "default";

    /**
     * Sets up the SoundsPreferencesPanel
     *
     * @param dialog
     *            the PreferencesDialog that contains this panel
     */
    public SoundsPreferencesPanel(PreferencesDialog dialog) {
        this.prefs = dialog;
        setBorder(BorderFactory.createTitledBorder(resources
                .getString("soundsPreferences")));
        setLayout(grid);

        String methods[] = new String[] { "Java Sound System", "Command",
                "Console Beep" };
        methodBox = new JComboBox(methods);

        c.gridx = 0;
        c.gridy = 0;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = -.1;
        c.gridwidth = 1;

        grid.setConstraints(soundMethod, c);
        add(soundMethod);
        c.gridx++;
        c.gridwidth = 2;
        grid.setConstraints(methodBox, c);
        add(methodBox);

        c.gridy++;
        c.gridwidth = 1;
        c.gridx = 0;
        grid.setConstraints(command, c);
        add(command);
        c.gridwidth = 2;
        c.gridx++;
        grid.setConstraints(commandBox, c);
        add(commandBox);

        c.gridx = 1;
        c.gridy++;
        c.gridwidth = 2;
        grid.setConstraints(soundLabel, c);
        add(soundLabel);

        c.gridwidth = 1;

        c.gridx = 0;
        c.gridy++;

        grid.setConstraints(soundSet, c );
        add(soundSet);
        c.gridx++;
        c.gridwidth = 2;
        grid.setConstraints(soundSetBox, c);
        add(soundSetBox);

        c.gridx = 0;
        c.gridy += 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;

        // display name stuff
        addSoundBox("receivedSound", resources.getString("messageReceived")
                + ":");
        addSoundBox("signedOnSound", resources.getString("buddySignedOn") + ":");
        addSoundBox("signedOffSound", resources.getString("buddySignedOff")
                + ":");
        addSoundBox("groupReceivedSound", resources
                .getString("groupMessageReceived")
                + ":");
        addSoundBox("groupHighlightedSound", resources
                .getString("groupHighlighted")
                + ":");

        //this is the space taker
        JLabel blankLabel = new JLabel("");
        c.weighty = 1;
        c.weightx = 1;
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy++;
        grid.setConstraints(blankLabel, c);
        add(blankLabel);

        c.gridy++;
        c.gridx = 2;
        c.weightx = 1;
        c.weighty = .1;
        c.gridwidth = 4;
        c.anchor = GridBagConstraints.EAST;

        grid.setConstraints(defaultsButton, c);
        add(defaultsButton);

        loadSettings();
        initComponents();
    }

    /**
     * Adds a box with a label and an input box
     *
     * @param settingName
     *            the setting name that this preference corresponds to
     * @param labelName
     *            the text on the label to be displayed
     */
    private void addSoundBox(String settingName, String labelName) {
        c.anchor = GridBagConstraints.WEST;
        settingNames[current] = settingName;
        soundLabels[current] = new JLabel(labelName + " ");
        soundPlay[current] = new JCheckBox();
        soundBoxes[current] = new MJTextField(12);
        soundButtons[current] = new JButton(resources.getString("browse"));
        testButtons[current] = new JButton(Standard
                .getIcon("images/buttons/Play24.gif"));
        testButtons[current].setPreferredSize(new Dimension(24, 24));

        c.gridx = 0;
        c.weightx = .1;
        grid.setConstraints(soundLabels[current], c);
        add(soundLabels[current]);

        c.gridx++;
        grid.setConstraints(soundPlay[current], c);
        add(soundPlay[current]);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.gridx++;
        grid.setConstraints(soundBoxes[current], c);
        add(soundBoxes[current]);

        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;

        c.weightx = .2;

        c.gridx++;
        grid.setConstraints(soundButtons[current], c);
        add(soundButtons[current]);

        c.weightx = .0;
        c.gridx++;
        grid.setConstraints(testButtons[current], c);
        add(testButtons[current]);

        current++;
        c.gridy++;
    }

    /**
     * Sets up the visual components
     */
    private void initComponents() {
        ChooserActionListener listener = new ChooserActionListener();
        PlayActionListener playListener = new PlayActionListener();

        fc.addChoosableFileFilter(new SoundFilter());

        for (int i = 0; i < numberOfBoxes; i++) {
            soundButtons[i].addActionListener(listener);
            testButtons[i].addActionListener(playListener);
        }

        methodBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                String item = (String) e.getItem();
                if (item == "Command") {
                    commandBox.setEnabled(true);
                    commandBox.grabFocus();
                } else {
                    commandBox.setEnabled(false);
                }
            }
        });

        defaultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                defaultsHandler();
            }
        });
    }

    /**
     * Called if the user clicks the "Use Defaults" button
     */
    private void defaultsHandler() {
        int result = JOptionPane.showConfirmDialog(prefs, resources
                .getString("confirmDefaults"), resources
                .getString("preferences"), JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION)
            return;

        for (int i = 0; i < numberOfBoxes; i++) {
            soundBoxes[i].setText("(default)");
            soundPlay[i].setSelected(true);
        }
    }

    class PlayActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String file = "";
            for (int i = 0; i < numberOfBoxes; i++) {
                if (e.getSource() == testButtons[i]) {
                    file = soundBoxes[i].getText();
                    if (file.equals("(default)")) {
                        file = com.valhalla.jbother.sound.SoundPlayer
                                .loadDefault(settingNames[i]);
                        if (file == null)
                            return;
                    }
                }
            }

            int selected = methodBox.getSelectedIndex();
            String soundMethod = "Java Sound System";
            if (selected == 1)
                soundMethod = "Command";
            if (selected == 2)
                soundMethod = "Console Beep";

            if (!com.valhalla.jbother.sound.SoundPlayer.playSoundFile(file,
                    soundMethod, commandBox.getText())) {
                Standard.warningMessage(prefs, resources
                        .getString("soundPreferences"), resources
                        .getString("couldNotPlaySound"));
            }
        }
    }

    /**
     * Listens for users to click the "Browse" button
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class ChooserActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int result = fc.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                for (int i = 0; i < numberOfBoxes; i++) {
                    if (e.getSource() == soundButtons[i])
                        soundBoxes[i].setText(file.getPath());
                }
            }
        }
    }

    /**
     * Loads the settings from the settings file for sounds
     */
    private void loadSettings() {
        String method = Settings.getInstance().getProperty("soundMethod");
        if (method == null)
            method = "";

        if (method.equals("Command")) {
            commandBox.setEnabled(true);
            methodBox.setSelectedIndex(1);
        } else if (method.equals("Console Beep")) {
            commandBox.setEnabled(false);
            methodBox.setSelectedIndex(2);
        } else
            commandBox.setEnabled(false);

        commandBox.setText(Settings.getInstance().getProperty("soundCommand"));

        for (int i = 0; i < numberOfBoxes; i++) {
            boolean play = Settings.getInstance().getBoolean(
                    settingNames[i] + "Play");
            if (play)
                soundPlay[i].setSelected(true);
            String sound = Settings.getInstance().getProperty(settingNames[i]);
            if (sound != null && !sound.equals(""))
                soundBoxes[i].setText(sound);
        }

        String defaults = Settings.getInstance().getProperty( "defaultSoundSet", "default" );
        currentDefault = defaults;
        for( int i = 0; i < soundSetBox.getItemCount(); i++ )
        {
            if( ((String)soundSetBox.getItemAt(i)).equals(defaults) )
            {
                soundSetBox.setSelectedIndex(i);
                soundSetBox.validate();
            }
        }
    }

    /**
     * Returns temporary settings
     */
    public TempSettings getSettings() {
        TempSettings mySettings = new TempSettings();

        int selected = methodBox.getSelectedIndex();
        String soundMethod = "Java Sound System";
        if (selected == 1)
            soundMethod = "Command";
        if (selected == 2)
            soundMethod = "Console Beep";

        mySettings.setProperty("soundMethod", soundMethod);
        if (!commandBox.getText().equals(""))
            mySettings.setProperty("soundCommand", commandBox.getText());

        for (int i = 0; i < numberOfBoxes; i++) {
            String setting = settingNames[i];
            if (soundBoxes[i].getText().equals(""))
                mySettings.remove(setting);
            else
                mySettings.setProperty(setting, soundBoxes[i].getText());
            mySettings.setBoolean(setting + "Play", soundPlay[i].isSelected());
        }

        String newDefault = (String)soundSetBox.getSelectedItem();
        /*if( !newDefault.equals(currentDefault))
        {
            com.valhalla.jbother.sound.SoundPlayer.clearCache();
            currentDefault = newDefault;
        }*/

        mySettings.setProperty("defaultSoundSet", newDefault);

        return mySettings;
    }
}

/**
 * Makes sure that the sound picked is a .wav file
 */

class SoundFilter extends javax.swing.filechooser.FileFilter {

    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    public boolean accept(File f) {
        if (f.isDirectory())
            return true;

        String extension = Utils.getExtension(f);

        if (extension != null) {
            if (extension.equals(Utils.wav)) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return resources.getString("soundFiles");
    }
}

/**
 * Gets the .wav extension
 */

class Utils {
    public final static String wav = "wav";

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}