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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.valhalla.gui.MJTextField;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.misc.GnuPG;
import com.valhalla.misc.SimpleXOR;
import com.valhalla.settings.Settings;
import com.valhalla.settings.SettingsProperties;

/**
 * Allows a user to edit a profile
 *
 * @author synic
 * @author Andrey Zakirov
 * @created April 10, 2004
 */
public class ProfileEditorDialog extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JPanel main;

    private JTabbedPane pane = new JTabbedPane();

    private MJTextField nameField = new MJTextField();

    private MJTextField usernameField = new MJTextField(20);

    private MJTextField serverField = new MJTextField(25);

    private MJTextField resourceField = new MJTextField(20);

    private MJTextField portField = new MJTextField(5);

    private JPasswordField passwordField = new JPasswordField(20);

    private JCheckBox savePassword = new JCheckBox();

    private JCheckBox sslBox = new JCheckBox();

    private SettingsProperties settings = new SettingsProperties();

    private JCheckBox autoLoginBox = new JCheckBox();

    private JCheckBox defaultBox = new JCheckBox();

    private JCheckBox reconnectBox = new JCheckBox();

    private JButton createButton = new JButton(resources
            .getString("createAccountButton"));

    private JButton saveButton = new JButton(resources.getString("saveButton"));

    private JButton cancelButton = new JButton(resources
            .getString("cancelButton"));

    private File profDir = new File(JBother.settingsDir, "profiles");

    private JPanel innerPanel = new JPanel();

    private String origProf = null;

    private ProfileManager dialog = null;

    private boolean exitOnClose = false;

    private boolean isCurrentProfile = false;

    private JButton gnupgusenoneButton = new JButton(resources
            .getString("gnupgUseNone"));

    private JButton gnupgselectButton = new JButton(resources
            .getString("gnupgSelectKey"));

    private JLabel keyinfoLabel = new JLabel(resources
            .getString("gnupgNoKeySelected"));

    private String gnupgSecretKeyID = null;

    private JCheckBox savepassphraseCheck = new JCheckBox();

    private JPasswordField gnupgpasswordField = new JPasswordField(15);

    private JCheckBox gnupgSignPresenceCheck = new JCheckBox();

    private String[] gnupgSecurityVariants = { "Sign and Encrypt",
            "Encrypt Only", "Sign Only" };

    private JComboBox gnupgSecurityVariantBox = new JComboBox(
            gnupgSecurityVariants);

    private String tempgnupgSecretKeyID = "";
    private JFrame parent;

    private JCheckBox useProxyBox = new JCheckBox(resources
            .getString("useProxy"));

    private MJTextField proxyHostBox = new MJTextField(15);

    private MJTextField proxyPortBox = new MJTextField(15);

    /**
     * Contructs the ProfileEditorDialog
     *
     * @param dialog
     *            the ProfileManager dialog that's calling this editor, or
     *            <tt>null</tt> if nothing is calling it
     * @param profile
     *            the profile to edit, or <tt>null</tt> if it's a new profile
     */
    public ProfileEditorDialog(JFrame parent,ProfileManager dialog, String profile) {
        super(parent, "", true);
        this.parent = parent;
        this.dialog = dialog;
        setModal(false);
        setTitle(resources.getString("profileEditor"));
        origProf = profile;

        main = (JPanel) getContentPane();
        main.setLayout(new BorderLayout());
        main.setBorder(BorderFactory.createTitledBorder(resources
                .getString("profileEditor")));
        main.add(pane, BorderLayout.CENTER);

        pane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (pane.getSelectedIndex() == 2) {
                    if (!JBotherLoader.isGPGEnabled()) {
                        if (Settings.getInstance().getProperty(
                                "gnupgSecretKeyID") != null) {
                            int result = JOptionPane
                                    .showConfirmDialog(
                                            ProfileEditorDialog.this,
                                            "Warning: There is a GnuPG secrety key ID in your profile,\nbut it appears as though GnuPG is not installed on this system.\nWould you like to remove the ID from your profile?",
                                            "GnuPG", JOptionPane.YES_NO_OPTION);

                            if (result == JOptionPane.YES_OPTION) {
                                Settings.getInstance().remove(
                                        "gnupgSecretKeyID");
                                Settings.getInstance()
                                        .remove("gnupgPassPhrase");
                                Settings.getInstance().remove(
                                        "gnupgSavePassphrase");
                            }
                        }

                        Standard
                                .warningMessage(ProfileEditorDialog.this, "GnuPG Error",
                                        "GnuPG is not executable or sends unknown response.  GnuPG is disabled");

                        pane.setSelectedIndex(0);
                    }
                }
            }
        });

        pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel namePanel = new JPanel();
        namePanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
        namePanel.add(new JLabel(resources.getString("profileName") + ": "));
        namePanel.add(nameField);

        topPanel.add(namePanel);

        JPanel defaultPanel = new JPanel();
        defaultPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        defaultPanel.setLayout(new BoxLayout(defaultPanel, BoxLayout.X_AXIS));
        defaultPanel
                .add(new JLabel(resources.getString("setAsDefault") + ": "));
        defaultPanel.add(defaultBox);
        defaultPanel.add(Box.createHorizontalGlue());

        topPanel.add(defaultPanel);

        main.add(topPanel, BorderLayout.NORTH);

        createAccountPanel();
        createOptionsPanel();
        createGnuPGPanel();
        createProxyPanel();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        main.add(buttonPanel, BorderLayout.SOUTH);

        addListeners();
        loadProfile(profile);

        pack();
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cancelHandler();
            }
        });
    }

    protected JFrame getDialogParent() { return parent; }

    /**
     * Sets whether or not this dialog should exit the application when it's
     * cancel button has been pressed
     *
     * @param e
     *            true to close the app
     */
    protected void setExitOnClose(boolean e) {
        this.exitOnClose = true;
    }

    /**
     * Sets the isCurrentProfile attribute of the ProfileEditorDialog object
     *
     * @param i
     *            The new isCurrentProfile value
     */
    public void setIsCurrentProfile(boolean i) {
        this.isCurrentProfile = i;
    }

    /**
     * @return the defaultBox
     */
    protected JCheckBox getDefaultBox() {
        return defaultBox;
    }

    /**
     * Adds the event listeners to the buttons
     */
    private void addListeners() {
        PEDialogListener listener = new PEDialogListener();
        createButton.addActionListener(listener);
        saveButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
        gnupgusenoneButton.addActionListener(listener);
        gnupgselectButton.addActionListener(listener);
        useProxyBox.addActionListener(listener);

    }

    /**
     * Handles events
     *
     * @author synic
     * @created November 30, 2004
     */
    class PEDialogListener implements ActionListener {
        /**
         * Description of the Method
         *
         * @param e
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == createButton) {
                createAccountHandler();
            } else if (e.getSource() == saveButton) {
                saveHandler();
            } else if (e.getSource() == cancelButton) {
                cancelHandler();
            } else if (e.getSource() == gnupgusenoneButton) {
                gnupgusenoneHandler();
            } else if (e.getSource() == gnupgselectButton) {
                gnupgselectHandler();
            } else if (e.getSource() == useProxyBox) {
                useProxyBoxHandler();
            }
        }
    }

    /**
     * Cancels the dialog, and quits if exitOnClose is set to true
     */
    private void cancelHandler() {
        dispose();
        if (exitOnClose) {
            System.exit(0);
        }
    }

    /**
     * Description of the Method
     */
    private void gnupgusenoneHandler() {
        keyinfoLabel.setText(resources.getString("gnupgNoKeySelected"));
        settings.remove("gnupgPassPhrase");
        this.gnupgSecretKeyID = null;
        savepassphraseCheck.setSelected(false);
        savepassphraseCheck.setEnabled(false);
        gnupgpasswordField.setText("");
        gnupgpasswordField.setEnabled(false);
        gnupgusenoneButton.setEnabled(false);
        gnupgSignPresenceCheck.setSelected(false);
        gnupgSignPresenceCheck.setEnabled(false);
        gnupgSecurityVariantBox.setEnabled(false);
        BuddyList.getInstance().setGnuPGPassword(null);

        if (BuddyList.getInstance().checkConnection()) {
            settings.setProperty("gnupgPassPhrase", null);
        }

    }

    /**
     * Description of the Method
     */
    private void gnupgselectHandler() {
        KeySelectDialog dialog = new KeySelectDialog((JDialog) this,
                "sec");
        dialog.showDialog();
        if ((dialog.getName() != null) && (dialog.getID() != null)) {
            keyinfoLabel.setText(dialog.getName());
            this.gnupgSecretKeyID = dialog.getID();
            savepassphraseCheck.setEnabled(true);
            gnupgpasswordField.setText("");
            gnupgpasswordField.setEnabled(savepassphraseCheck.isSelected());
            gnupgusenoneButton.setEnabled(true);
            gnupgSignPresenceCheck.setSelected(true);
            gnupgSignPresenceCheck.setEnabled(true);
            gnupgSecurityVariantBox.setEnabled(true);
        }
    }

    /**
     * Saves the currently opened profile
     */
    private void saveHandler() {
        try {
            Standard.setBundle(resources);
            Standard.assure(nameField.getText(), "Profile Name");
            Standard.assure(usernameField.getText(), "Username");

            if (savePassword.isSelected()) {
                Standard.assure(new String(passwordField.getPassword()),
                        "Password");
            }

            Standard.assure(resourceField.getText(), "Resource");
            Standard.assure(serverField.getText(), "Server");
        } catch (Exception e) {
            com.valhalla.Logger.logException(e);
            return;
        }

        settings.setProperty("username", usernameField.getText());

        if (savePassword.isSelected()) {
            settings.setProperty("password", SimpleXOR.encrypt(new String(
                    passwordField.getPassword()), "JBother rules!"));
        } else
            settings.remove("password");

        settings.setProperty("resource", resourceField.getText());
        settings.setProperty("defaultServer", serverField.getText());
        settings.setProperty("port", portField.getText());
        settings.setBoolean("useSSL", sslBox.isSelected());
        settings.setBoolean("autoLogin", autoLoginBox.isSelected());
        settings.setBoolean("reconnectOnDisconnect", reconnectBox.isSelected());
        if (gnupgSecretKeyID != null) {
            String gnupgTempPass = null;
            GnuPG gnupg = new GnuPG();
            boolean check = true;
            if (BuddyList.getInstance().checkConnection()
                    && !savepassphraseCheck.isSelected()
                    && gnupgSecretKeyID.matches(tempgnupgSecretKeyID) == false) {
                PasswordDialog d = new PasswordDialog(this,resources
                        .getString("gnupgKeyPassword"));
                gnupgTempPass = d.getText();
            } else if (savepassphraseCheck.isSelected()) {
                gnupgTempPass = new String(gnupgpasswordField.getPassword());
            } else {
                check = false;
            }

            if (!check
                    || ((gnupgTempPass != null) && (gnupg.sign("1",
                            gnupgSecretKeyID, gnupgTempPass)))) {
                BuddyList.getInstance().setGnuPGPassword(gnupgTempPass);
            } else {
                BuddyList.getInstance().setGnuPGPassword(null);
                Standard
                        .warningMessage(null, "GnuPG Error",
                                "Wrong GnuPG passphrase! Please, try entering it again again.");
                return;
            }

            Hashtable buddyStatuses = BuddyList.getInstance()
                    .getBuddyStatuses();
            if (buddyStatuses != null) {
                Iterator iterator = buddyStatuses.keySet().iterator();
                while (iterator.hasNext()) {
                    String user = (String) iterator.next();
                    BuddyStatus buddy = (BuddyStatus) buddyStatuses.get(user);
                    if (buddy.getConversation() != null
                            && buddy.getConversation() instanceof ChatPanel) {
                        ((ChatPanel) buddy.getConversation()).enableEncrypt();
                    }
                }
            }
            if (gnupgSignPresenceCheck.isSelected()) {
                settings.setBoolean("gnupgSignPresence", true);
            } else {
                settings.remove("gnupgSignPresence");
            }
            int variant = gnupgSecurityVariantBox.getSelectedIndex();
            settings.setProperty("gnupgSecurityVariant", String
                    .valueOf(variant));
            settings.setProperty("gnupgSecretKeyID", gnupgSecretKeyID);
            if (savepassphraseCheck.isSelected()) {
                String pass = new String(gnupgpasswordField.getPassword());
                if (!pass.equals("")) {
                    settings.setBoolean("gnupgSavePassphrase", true);
                    settings.setProperty("gnupgPassPhrase", SimpleXOR.encrypt(
                            pass, "86753099672539"));
                } else {
                    Standard.warningMessage(this, "GnuPG", resources
                            .getString("gnupgEnterPassphrase"));
                    return;
                }
            } else {
                settings.setBoolean("gnupgSavePassphrase", false);
                settings.remove("gnupgPassPhrase");
            }
        } else {
            settings.remove("gnupgSecretKeyID");
        }

        settings.setBoolean("useProxy", useProxyBox.isSelected());
        settings.setProperty("proxyHost", proxyHostBox.getText().trim());
        settings.setProperty("proxyPort", proxyPortBox.getText().trim());
        // this sould make sure that new settings are activated
        // when user presses 'Save' button
        Properties sysProperties = System.getProperties();
        sysProperties.put("proxySet",settings.getBoolean("useProxy") ? "true" : "false");
        sysProperties.put("proxyHost",settings.getProperty("proxyHost"));
        sysProperties.put("proxyPort",settings.getProperty("proxyPort"));

        String profile = nameField.getText();
        File profileDir = new File(profDir, profile);

        if (origProf == null) {
            // check to see if the profile already exists
            if (profileDir.exists()) {
                Standard.warningMessage(this, resources
                        .getString("profileEditor"), resources
                        .getString("profileExists"));
                return;
            }

            profileDir.mkdirs();
        } else if (!profile.equals(origProf)) {
            File origProfDir = new File(profDir, origProf);
            origProfDir.renameTo(profileDir);
        }

        try {
            settings.saveSettings(profDir.getPath() + File.separatorChar
                    + profile + File.separatorChar + "settings.properties",
                    "JBother Settings File");
        } catch (IOException ex) {
            Standard.warningMessage(this, resources.getString("profileEditor"),
                    resources.getString("errorSavingSettings"));
            return;
        }

        if (defaultBox.isSelected()) {
            ProfileManager.setDefaultProfile(profile);
        }

        if (dialog != null) {
            dialog.loadProfileList();
        }

        if (isCurrentProfile) {
            ProfileManager.setCurrentProfile(profile);
            Settings.loadSettings(profDir.getPath() + File.separatorChar
                    + profile, "settings.properties");
            ConnectorThread.getInstance().resetCredentials();
        }

        if (exitOnClose) {
            ProfileManager.loadProfile(nameField.getText());
        }

        dispose();
    }

    /**
     * Calls the NewAccoutDialog to create a new account
     */
    private void createAccountHandler() {
        if (serverField.getText().equals("")) {
            Standard.warningMessage(this, "createAccount", resources
                    .getString("enterNewAccountServer"));
            return;
        }

        int port = -1;
        boolean ssl = sslBox.isSelected();

        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
        }

        if (port == -1) {
            if (ssl) {
                port = 5223;
            } else {
                port = 5222;
            }
        }


        String server = JOptionPane.showInputDialog(resources.getString("enterNewAccountServer"));
        if( server == null || server.equals("")) return;

        NewAccountDialog dialog = new NewAccountDialog(this, server,
            usernameField.getText(), new String(passwordField
                .getPassword()), port, ssl);
        dialog.getRegistrationInfo();
    }

    /**
     * makes sure that
     */
    private void useProxyBoxHandler() {
        if(useProxyBox.isSelected())
        {
            proxyHostBox.setEditable(true);
            proxyPortBox.setEditable(true);
        } else
        {
            proxyHostBox.setEditable(false);
            proxyPortBox.setEditable(false);
        }
    }

    /**
     * Sets the username
     *
     * @param username
     *            The new username value
     */
    public void setUsername(String username) {
        usernameField.setText(username);
    }

    /**
     * Sets the password
     *
     * @param password
     *            The new password value
     */
    public void setPassword(String password) {
        passwordField.setText(password);
    }

    public void setServer(String server)
    {
        serverField.setText(server);
    }

    /**
     * Creates the Account Panel
     */
    private void createAccountPanel() {
        savePassword.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = new String(passwordField.getPassword());
                passwordField.setEnabled(savePassword.isSelected());
                if (!savePassword.isSelected()) {
                    passwordField.setText("");
                }
                else passwordField.setText(text);
            }
        });

        AbstractOptionPanel panel = new AbstractOptionPanel();
        panel.addComponent(resources.getString("username"), usernameField);
        passwordField.setFont(usernameField.getFont());
        panel.addComponent(resources.getString("savePassword"), savePassword);
        panel.addComponent(resources.getString("password"), passwordField);
        panel.addComponent(resources.getString("resource"), resourceField);
        panel.addComponent(resources.getString("server"), serverField);
        panel.addComponent(createButton, 1, GridBagConstraints.EAST);
        panel.end();

        pane.add(resources.getString("account"), panel);
    }

    /**
     * Creates the options panel
     */
    private void createOptionsPanel() {
        AbstractOptionPanel panel = new AbstractOptionPanel();
        panel.addComponent(resources.getString("useSsl"), sslBox);

        JPanel portPanel = new JPanel(new BorderLayout());
        portPanel.add(portField, BorderLayout.WEST);
        portPanel.add(new JLabel(" "
                + resources.getString("leaveBlankForDefault")),
                BorderLayout.CENTER);
        panel.addComponent(resources.getString("logInAutomatically"),
                autoLoginBox);
        panel.addComponent(resources.getString("reconnectOnDisconnect"),
                reconnectBox);
        panel.addComponent(resources.getString("connectPort"), portPanel);
        panel.end();

        pane.add(resources.getString("options"), panel);
    }

    /**
     * Creates proxy panel
     */
    private void createProxyPanel() {
        AbstractOptionPanel panel = new AbstractOptionPanel();
        panel.addComponent( resources.getString("useNetworkProxy"),
            useProxyBox );
        panel.addComponent( resources.getString("proxyHost"),
            proxyHostBox );
        panel.addComponent( resources.getString("proxyPort"),
            proxyPortBox );
        panel.end();

        pane.add(resources.getString("proxy"), panel);
    }

    /**
     * Creates the GnuPG Panel
     */

    private void createGnuPGPanel() {
        JPanel main = new JPanel(new BorderLayout());

        AbstractOptionPanel panel = new AbstractOptionPanel();
        panel.addComponent(resources.getString("gnupgSecretKey"), keyinfoLabel);
        panel.addComponent(resources.getString("gnupgSavePassphrase"),
                savepassphraseCheck);
        panel.addComponent(resources.getString("gnupgPassphrase"),
                gnupgpasswordField);
        panel.addComponent("Sign Presence", gnupgSignPresenceCheck);
        panel.addComponent("Security Variant", gnupgSecurityVariantBox);

        savepassphraseCheck.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                gnupgpasswordField.setEnabled(savepassphraseCheck.isSelected());

                if (savepassphraseCheck.isSelected()) {
                    if (savepassphraseCheck.isEnabled()) {
                        int result = JOptionPane.showConfirmDialog(null,
                                resources.getString("gnupgInsecure"), "GnuPG",
                                JOptionPane.YES_NO_OPTION);

                        if (result != JOptionPane.YES_OPTION) {
                            savepassphraseCheck.setSelected(false);
                        }
                    } else {
                        gnupgpasswordField.setText("");
                    }
                    gnupgpasswordField.setEnabled(savepassphraseCheck
                            .isSelected());
                } else {
                    gnupgpasswordField.setText("");
                }
            }
        });

        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(gnupgselectButton);
        buttons.add(gnupgusenoneButton);
        panel.end();

        main.add(panel, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.SOUTH);

        pane.add("GnuPG", main);

    }

    /**
     * Loads a certain profile
     *
     * @param profile
     *            the profile to load, or null to create a new one
     */
    private void loadProfile(String profile) {
        if (profile != null) {
            nameField.setText(profile);

            try {
                settings.loadSettings(profDir.getPath() + File.separatorChar
                        + profile + File.separatorChar + "settings.properties");
            } catch (Exception ex) {
                return;
            }
        } else {
            // copy the default file in to place
            InputStream stream = getClass().getClassLoader()
                    .getResourceAsStream("defaultsettings.properties");
            try {
                settings.load(stream);
            } catch (Exception ex) {
                return;
            }
        }

        usernameField.setText(settings.getProperty("username"));

        String pass = settings.getProperty("password");

        savePassword.setSelected(pass != null);
        passwordField.setEnabled(savePassword.isSelected());
        if (savePassword.isSelected()) {
            passwordField.setText(SimpleXOR.decrypt(settings
                    .getProperty("password"), "JBother rules!"));
        }

        resourceField.setText(settings.getProperty("resource"));
        serverField.setText(settings.getProperty("defaultServer"));
        portField.setText(settings.getProperty("port"));

        sslBox.setSelected(settings.getBoolean("useSSL"));

        autoLoginBox.setSelected(settings.getBoolean("autoLogin"));
        reconnectBox.setSelected(settings.getBoolean("reconnectOnDisconnect"));

        // proxy
        useProxyBox.setSelected(settings.getBoolean("useProxy"));
        proxyHostBox.setEditable(useProxyBox.isSelected());
        proxyPortBox.setEditable(useProxyBox.isSelected());
        proxyHostBox.setText(settings.getProperty("proxyHost"));
        proxyPortBox.setText(settings.getProperty("proxyPort"));


        String[] entries;
        String name;
        String id;
        GnuPG gnupg = new GnuPG();
        id = settings.getProperty("gnupgSecretKeyID");
        if (id != null) {
            if (gnupg.listSecretKeys(id)) {
                entries = gnupg.getResult().split("\n");
                for (int i = 0; i < entries.length; i++) {
                    name = entries[i]
                            .replaceAll(
                                    "^sec:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:([^:]*):[^:]*:[^:]*:[^:]*$",
                                    "$1");
                    if (!name.equals(entries[i])) {
                        keyinfoLabel.setText(name);
                        gnupgSecretKeyID = id;
                        if (id != null) {
                            tempgnupgSecretKeyID = id;
                        }
                    }
                }
            }

            if (settings.getProperty("gnupgPassPhrase") != null) {
                gnupgpasswordField.setText(SimpleXOR.decrypt(settings
                        .getProperty("gnupgPassPhrase"), "86753099672539"));
                savepassphraseCheck.setSelected(true);
            }

            gnupgSignPresenceCheck.setSelected(settings
                    .getBoolean("gnupgSignPresence"));
            gnupgSignPresenceCheck.setEnabled(true);

            if (settings.getProperty("gnupgSecurityVariant") != null) {
                int variant;
                String vs = settings.getProperty("gnupgSecurityVariant");
                if (vs != null) {
                    if (vs.matches("0") == false && vs.matches("1") == false
                            && vs.matches("2") == false) {
                        variant = 0;
                    } else {
                        variant = Integer.parseInt(vs);
                    }
                } else {
                    variant = 0;
                }
                gnupgSecurityVariantBox.setSelectedIndex(variant);
                gnupgSecurityVariantBox.setEnabled(true);
            }
        } else {
            gnupgSecurityVariantBox.setEnabled(false);
            gnupgpasswordField.setText("");
            gnupgpasswordField.setEnabled(false);
            savepassphraseCheck.setSelected(false);
            savepassphraseCheck.setEnabled(false);
            gnupgSignPresenceCheck.setEnabled(false);
        }

        String defaultProf = ProfileManager.getDefaultProfile();
        com.valhalla.Logger.debug(defaultProf);

        if (defaultProf != null && defaultProf.equals(profile)) {
            defaultBox.setSelected(true);
        }
    }
}

