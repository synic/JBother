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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.*;

import org.jivesoftware.smack.packet.Presence;

import com.valhalla.gui.Standard;
import com.valhalla.misc.GnuPG;
import com.valhalla.misc.MiscUtils;
import com.valhalla.misc.SimpleXOR;
import com.valhalla.settings.Arguments;
import com.valhalla.settings.Settings;

/**
 * Shows a graphical chooser for different JBother profiles
 *
 * @author Adam Olsen
 * @created Oct 28, 2004
 * @version 1.0
 */
public class ProfileManager extends JFrame {
    private static ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JList profileList = new JList();

    private JButton newButton = new JButton(resources.getString("newButton"));

    private JButton editButton = new JButton(resources.getString("editButton"));

    private JButton deleteButton = new JButton(resources
            .getString("deleteButton"));

    private JButton openButton = new JButton(resources.getString("openButton"));

    private JButton cancelButton = new JButton(resources
            .getString("cancelButton"));

    private JPanel main = null;

    private String defaultString = "     <-";

    private static File profDir = new File(JBother.settingsDir, "profiles");

    private ProfileListModel model = null;

    private boolean exitOnClose = false;

    private static String currentProfile = "default";

    private static boolean isShowing = false;
    private static Object selected = null;

    /**
     * Default constructor
     */
    public ProfileManager() {
        super("JBother");

        setIconImage(Standard.getImage("frameicon.png"));
        profileList.setCellRenderer(new ListRenderer());

        loadProfileList();

        main = (JPanel) getContentPane();
        main.setBorder(BorderFactory.createTitledBorder(resources
                .getString("profileManager")));
        main.setLayout(new BorderLayout(5, 5));

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        newButton.setMaximumSize(new Dimension(100, 100));
        editButton.setMaximumSize(new Dimension(100, 100));
        deleteButton.setMaximumSize(new Dimension(100, 100));
        rightPanel.add(newButton);
        rightPanel.add(editButton);
        rightPanel.add(deleteButton);

        rightPanel.add(Box.createVerticalGlue());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(cancelButton);
        bottomPanel.add(openButton);
        main.add(new JScrollPane(profileList), BorderLayout.CENTER);
        main.add(rightPanel, BorderLayout.WEST);
        main.add(bottomPanel, BorderLayout.SOUTH);

        addListeners();
        pack();
        setSize(350, 200);
        setLocationRelativeTo(null);
        isShowing = true;
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cancelHandler();
            }
        });
    }

    class MouseClickListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
                openHandler();
            }
        }
    }

    public static boolean isCurrentlyShowing() {
        return isShowing;
    }

    public static String getCurrentProfile() {
        return currentProfile;
    }

    public static void setCurrentProfile(String profile) {
        currentProfile = profile;
    }

    /**
     * @param exitOnClose
     *            set to true to have this dialog close the app on close
     */
    public void setExitOnClose(boolean exitOnClose) {
        this.exitOnClose = exitOnClose;
    }

    /**
     * cancels this dialog, and if exitOnClose is set, the application quits
     */
    private void cancelHandler() {
        if (exitOnClose) {
            System.exit(0);
        } else {
            isShowing = false;
            dispose();
            BuddyList.getInstance().getContainerFrame().setVisible(true);
        }
    }

    /**
     * Adds event listeners
     */
    private void addListeners() {
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelHandler();
            }
        });

        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String string = (String) profileList.getSelectedValue();
                
                selected = string;
                if (string != null && string.endsWith(defaultString)) {
                    int index = string.indexOf(defaultString);
                    string = string.substring(0, index);
                }

                new ProfileEditorDialog(ProfileManager.this,
                    ProfileManager.this, string).setVisible(true);
            }
        });

        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ProfileEditorDialog(ProfileManager.this,
                    ProfileManager.this, null).setVisible(true);
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String string = (String) profileList.getSelectedValue();
                if (string != null && string.endsWith(defaultString)) {
                    int index = string.indexOf(defaultString);
                    string = string.substring(0, index);
                }

                int result = JOptionPane.showConfirmDialog(null, resources
                        .getString("deleteProfile"), "JBother",
                        JOptionPane.YES_NO_OPTION);

                if (result == 0) {
                    try {
                        MiscUtils.recursivelyDeleteDirectory(profDir.getPath()
                                + File.separatorChar + string);
                    } catch (Exception ex) {
                        Standard.warningMessage(ProfileManager.this, "JBother",
                                resources.getString("errorDeletingProfile"));
                        com.valhalla.Logger.logException(ex);
                        return;
                    }

                    loadProfileList();
                }
            }
        });

        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openHandler();
            }
        });

        profileList.addMouseListener(new MouseClickListener());
    }

    public void openHandler() {
        String string = (String) profileList.getSelectedValue();
        if (string != null && string.endsWith(defaultString)) {
            int index = string.indexOf(defaultString);
            string = string.substring(0, index);
        }

        loadProfile(string);
        isShowing = false;
        dispose();
    }

    /**
     * Loads a profile
     *
     * @param profile
     *            the profile to load
     */
    public static void loadProfile(String profile) {
        Settings.loadSettings(profDir.getPath() + File.separatorChar + profile,
                "settings.properties");
        if (JBother.kiosk_mode
                && Arguments.getInstance().getProperty("kiosk_roomservice") != null) {
            Settings.createKioskRoom();
        }
        JBother.profileDir = JBother.settingsDir + File.separatorChar
                + "profiles" + File.separatorChar + profile;

        GnuPG gnupg = new GnuPG();
        JBotherLoader.setGPGEnabled(gnupg.listKeys(""));

        String fontString = Settings.getInstance().getProperty(
                "applicationFont");
        if (fontString == null) {
            fontString = "Default-PLAIN-12";
        }

        Font newFont = Font.decode(fontString);
        com.valhalla.jbother.preferences.AppearancePreferencesPanel
                .updateApplicationFonts(newFont, null);
        ConversationFormatter.getInstance().switchTheme(
                Settings.getInstance().getProperty("emoticonTheme"));
        StatusIconCache.clearStatusIconCache();

        BuddyList.getInstance().loadSettings();
        JBotherLoader.loadSettings();

        if (JBotherLoader.isGPGEnabled()
                && Settings.getInstance().getBoolean("gnupgSavePassphrase")
                && Settings.getInstance().getProperty("gnupgSecretKeyID") != null) {
            String pass = Settings.getInstance().getProperty("gnupgPassPhrase");
            if (pass == null)
                pass = "";
            pass = SimpleXOR.decrypt(pass, "86753099672539");

            gnupg = new GnuPG();

            String gnupgSecretKeyID = Settings.getInstance().getProperty(
                    "gnupgSecretKeyID");

            if (gnupg.sign("1", gnupgSecretKeyID, pass)) {
                BuddyList.getInstance().setGnuPGPassword(pass);
            } else {
                BuddyList.getInstance().setGnuPGPassword(null);
                Standard.warningMessage(null, "GnuPG", resources
                        .getString("gnupgBadSavedPassword"));
            }
        }

        if (Settings.getInstance().getBoolean("autoLogin")) {
            ConnectorThread.getInstance().setCancelled(false);
            ConnectorThread.getInstance().init(Presence.Mode.AVAILABLE, "Available", false).start();
        }

        if (Settings.getInstance().getBoolean("useProxy"))
        {
            Properties sysProperties = System.getProperties();
            sysProperties.setProperty("proxySet", "true");
            sysProperties.setProperty("proxyHost", Settings.getInstance().getProperty("proxyHost"));
            sysProperties.setProperty("proxyPort", Settings.getInstance().getProperty("proxyPort"));
        }

        currentProfile = profile;
    }

    /**
     * Loads a list of profiles
     */
    protected void loadProfileList() {
        if (!profDir.isDirectory() && !profDir.mkdirs()) {
            com.valhalla.Logger
                    .debug("Could not create profile directory!  Please check permissions on ~/.jbother");
            System.exit(-1);
        }

        model = new ProfileListModel();

        String list[] = profDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (new File(dir, name).isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        for (int i = 0; i < list.length; i++) {
            model.addElement(list[i]);
        }

        profileList.setModel(model);

        selectDefault();
    }

    /**
     * Selects the default profile and labels it (default)
     */
    private void selectDefault() {
        String defaultProfile = getDefaultProfile();
        if (defaultProfile == null) {
            setDefaultProfile(defaultProfile);
            return;
        }
        
        boolean def = true;
        
        if(selected != null && model.indexOf(selected) != -1) 
            defaultProfile = (String)selected;
            
                

        int index = model.indexOf(defaultProfile);
        if (index != -1) {
            profileList.setSelectedIndex(index);
        } else {
            profileList.setSelectedIndex(0);
        }
    }

    /**
     * Gets the current default profile, or the first profile in the profiles
     * directory
     *
     * @return The default profile
     */
    public static String getDefaultProfile() {
        File file = new File(profDir, "default.properties");
        if (!file.exists()) {
            return getOnlyProfile();
        }

        Properties def = new Properties();
        try {
            InputStream stream = new FileInputStream(file);

            def.load(stream);
            stream.close();
        } catch (IOException e) {
            com.valhalla.Logger.logException(e);
            return getOnlyProfile();
        }

        return def.getProperty("defaultProfile");
    }

    /**
     * Gets the first profile in the profile directory
     *
     * @return The first profile in the profile directory, or <tt>null</tt> if
     *         there are no profiles
     */
    public static String getOnlyProfile() {
        if (JBother.kiosk_mode)
            return Arguments.getInstance().getProperty("kiosk_user");

        String[] list = profDir.list();
        if (list != null && list.length > 0) {
            return list[0];
        } else {
            return null;
        }
    }

    /**
     * Sets the default profile
     *
     * @param profile
     *            The profile to set
     */
    public static void setDefaultProfile(String profile) {
        File file = new File(profDir, "default.properties");

        try {
            OutputStream stream = new FileOutputStream(file);
            Properties def = new Properties();
            def.setProperty("defaultProfile", profile);
            def.store(stream, "default profile setting");
            stream.close();
        } catch (Exception e) {
            com.valhalla.Logger.logException(e);
        }
    }
    
    class ListRenderer extends JLabel implements ListCellRenderer
    {
        public ListRenderer()
        {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {
            String def = getDefaultProfile();
            setSelected(isSelected);
            String val = (String)value;
            if(def.equals(val)) val += defaultString;
            setText(val);
            

            return this;
        }
        
        public void setSelected(boolean selected)
        {
            if(selected) setBackground(profileList.getSelectionBackground());
            else setBackground(Color.WHITE);
        }
    }

    /**
     * The JList model for the profiles list
     *
     * @author synic
     * @created November 30, 2004
     */
    class ProfileListModel extends DefaultListModel {
        /**
         * Sets the valueAt attribute of the ProfileListModel object
         *
         * @param index
         *            The new valueAt value
         * @param value
         *            The new valueAt value
         */
        public void setValueAt(int index, String value) {
            model.removeElementAt(index);
            model.insertElementAt(value, index);
            fireContentsChanged(model, index, index + 1);
        }
    }
}

