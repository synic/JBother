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
package com.valhalla.jbother.preferences;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.jfree.ui.FontChooserDialog;

import com.valhalla.gui.CopyPasteContextMenu;
import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.ConversationFormatter;
import com.valhalla.jbother.JBother;
import com.valhalla.jbother.JBotherLoader;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.settings.Settings;
import com.valhalla.settings.TempSettings;

/**
 * Allows the user to change appearance preferences
 *
 * @author Adam Olsen
 * @created March 9, 2005
 * @version 1.0
 */
public class AppearancePreferencesPanel extends JPanel implements
        PreferencesPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private PreferencesDialog prefs;

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    private JLabel lafLabel = new JLabel(resources.getString("themeLabel")
            + ": ");

    private JLabel statusLabel = new JLabel(resources.getString("statusLabel")
            + ": ");

    private JLabel emoticonLabel = new JLabel(resources
            .getString("emoticonLabel")
            + ": ");

    private JLabel messageLabel = new JLabel(resources
            .getString("messageWindowFont")
            + ": ");

    private JButton messageFontButton = new JButton("Font");

    private JLabel appFontLabel = new JLabel(resources
            .getString("appFontLabel")
            + ": ");

    private JButton appFontButton = new JButton("Font");

    private JComboBox lookAndFeel, statusTheme, emoticonTheme;

    private int current = -1;

    private UIManager.LookAndFeelInfo[] lfs;

    private String[] names;

    /**
     * Sets up the AppearancePreferences
     *
     * @param dialog
     *            the enclosing PreferencesDialog
     */
    public AppearancePreferencesPanel(PreferencesDialog dialog) {
        this.prefs = dialog;
        setBorder(BorderFactory.createTitledBorder(resources
                .getString("appearancePreferences")));
        setLayout(grid);

        lfs = UIManager.getInstalledLookAndFeels();
        String lf = UIManager.getLookAndFeel().getClass().getName();

        ArrayList list = new ArrayList();
        Properties displayed = new Properties();

        int index = 0;
        for (int i = 0; i < lfs.length; i++) {

            if( displayed.getProperty( lfs[i].getName() ) == null )
            {
                list.add(lfs[i].getName());

                if (lf.equals(lfs[i].getClassName()) && current == -1) {
                    index = i;
                    current = index;
                }
            }

            displayed.setProperty(lfs[i].getName(), "1" );
        }

        if (current == -1)
            index = 0;

        names = new String[list.size()];
        list.toArray(names);

        lookAndFeel = new JComboBox(names);
        lookAndFeel.setSelectedIndex(index);
        lookAndFeel.validate();
        lookAndFeel.repaint();

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;

        //display name stuff
        lafLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        c.weightx = -.1;
        c.fill = GridBagConstraints.NONE;
        grid.setConstraints(lafLabel, c);
        add(lafLabel);

        c.gridx = 1;

        grid.setConstraints(lookAndFeel, c);
        add(lookAndFeel);

        c.gridx = 0;
        c.gridy++;

        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        grid.setConstraints(statusLabel, c);
        add(statusLabel);

        statusTheme = getStatusThemes();
        c.gridx++;
        grid.setConstraints(statusTheme, c);
        add(statusTheme);

        c.gridx = 0;
        c.gridy++;

        emoticonLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        grid.setConstraints(emoticonLabel, c);
        add(emoticonLabel);

        emoticonTheme = getEmoticonThemes();
        c.gridx++;
        grid.setConstraints(emoticonTheme, c);
        add(emoticonTheme);

        FontChangeListener fontListener = new FontChangeListener();

        // get the message window font settings
        String messageFont = Settings.getInstance().getProperty(
                "messageWindowFont");
        if (messageFont == null) {
            messageFont = "Default-PLAIN-12";
        }

        Font font = Font.decode(messageFont);
        messageFontButton
                .setText(getEncodedFontName(font).replaceAll("-", " "));
        messageFontButton.setFont(font);

        c.gridx = 0;
        c.gridy++;
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        grid.setConstraints(messageLabel, c);
        add(messageLabel);
        c.gridx++;
        messageFontButton.addActionListener(fontListener);
        grid.setConstraints(messageFontButton, c);
        add(messageFontButton);

        // get the application font settings
        String appFont = Settings.getInstance().getProperty("applicationFont");
        if (appFont == null) {
            appFont = "Default-PLAIN-12";
        }

        font = Font.decode(appFont);
        appFontButton.setText(getEncodedFontName(font).replaceAll("-", " "));
        appFontButton.setFont(font);

        c.gridx = 0;
        c.gridy++;
        appFontLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        grid.setConstraints(appFontLabel, c);
        add(appFontLabel);
        c.gridx++;
        appFontButton.addActionListener(fontListener);
        grid.setConstraints(appFontButton, c);
        add(appFontButton);

        //this is the space taker
        JLabel blankLabel = new JLabel("");
        c.weighty = 1;
        c.weightx = 1;
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy++;
        grid.setConstraints(blankLabel, c);
        add(blankLabel);
    }

    /**
     * Listens for one of the font buttons to be clicked and displays a
     * FontChooserDialog for it
     *
     * @author Adam Olsen
     * @created March 9, 2005
     * @version 1.0
     */
    private class FontChangeListener implements ActionListener {
        /**
         * Description of the Method
         *
         * @param e
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            FontChooserDialog dialog = new FontChooserDialog(prefs, resources
                    .getString("messageWindowFont"), true, button.getFont());

            dialog.setVisible(true);

            if (!dialog.isCancelled()) {
                Font newFont = dialog.getSelectedFont();
                button.setFont(newFont);
                button
                        .setText(getEncodedFontName(newFont).replaceAll("-",
                                " "));
            }
        }
    }

    /**
     * Checks the default jar file and the user theme directory for status
     * themes
     *
     * @return The statusThemes value
     */
    private JComboBox getStatusThemes() {
        JComboBox box = new JComboBox();
        String current = Settings.getInstance().getProperty("statusTheme");
        if (current == null) {
            current = "default";
        }
        box.addItem(current);

        // user defined themes
        File path = new File(JBother.settingsDir + File.separatorChar
                + "themes" + File.separatorChar + "statusicons");

        String[] userThemes = new String[0];

        if (!path.isDirectory() && !path.mkdirs()) {
            com.valhalla.Logger
                    .debug("Could not create user defined settings directories.");
        } else {
            userThemes = path.list();
            for (int i = 0; i < userThemes.length; i++) {
                if (!current.equals(userThemes[i])) {
                    box.addItem(userThemes[i]);
                }
            }
        }

        // default themes in the jar
        InputStream stream = getClass().getClassLoader().getResourceAsStream(
                "imagethemes/statusicons/index.dat");
        if (stream == null) {
            com.valhalla.Logger.debug("Bad status theme file.");
            return box;
        }

        InputStreamReader in = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(in);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                boolean userTheme = false;
                for (int i = 0; i < userThemes.length; i++) {
                    if (line.equals(userThemes[i])) {
                        userTheme = true;
                        break;
                    }
                }
                if (!userTheme && !line.equals(current)) {
                    box.addItem(line);
                }
            }
        } catch (IOException e) {
        }

        return box;
    }

    /**
     * Gets a list of emoticon themes
     *
     * @return The emoticonThemes value
     */
    private JComboBox getEmoticonThemes() {
        JComboBox box = new JComboBox();
        String current = Settings.getInstance().getProperty("emoticonTheme");
        if (current == null) {
            current = "default";
        }
        box.addItem(current);

        // default themes in the jar
        InputStream stream = getClass().getClassLoader().getResourceAsStream(
                "imagethemes/emoticons/index.dat");
        if (stream == null) {
            com.valhalla.Logger.debug("Bad emoticon theme file.");
            return box;
        }

        InputStreamReader in = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(in);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.equals(current)) {
                    box.addItem(line);
                }
            }
        } catch (IOException e) {
        }

        return box;
    }

    /**
     * Gets the String representation of a font
     *
     * @param font
     *            Description of the Parameter
     * @return the string representation of a font
     */
    private String getEncodedFontName(Font font) {
        String fontString = font.getName() + "-";

        if (font.isPlain()) {
            fontString += "PLAIN";
        }
        if (font.isBold()) {
            fontString += "BOLD";
        }
        if (font.isItalic()) {
            fontString += "ITALIC";
        }
        fontString += "-" + font.getSize();

        return fontString;
    }

    /**
     * Gets temporary settings
     *
     * @return temporary settings
     */
    public TempSettings getSettings() {
        TempSettings tempSettings = new TempSettings();

        if (lookAndFeel.getSelectedIndex() != current) {
            //Standard.noticeMessage( prefs, resources.getString( "themeLabel"
            // ),
            //	resources.getString( "themeSettingsApplied" ) );

            String laf = lfs[lookAndFeel.getSelectedIndex()].getClassName();

            JBotherLoader.loadLAF(laf);
            tempSettings.setProperty("lookAndFeel", laf);

            current = lookAndFeel.getSelectedIndex();
        }

        tempSettings.setProperty("messageWindowFont",
                getEncodedFontName(messageFontButton.getFont()));
        // update all open conversation areas
        Hashtable buddyStatuses = BuddyList.getInstance().getBuddyStatuses();
        if (buddyStatuses != null) {
            Iterator i = buddyStatuses.keySet().iterator();

            while (i.hasNext()) {
                BuddyStatus buddy = BuddyList.getInstance().getBuddyStatus(
                        (String) i.next());
                if (buddy.getConversation() != null) {
                    buddy.getConversation().updateStyle(
                            messageFontButton.getFont());
                }
            }
        }

        if (BuddyList.getInstance().getTabFrame() != null) {
            BuddyList.getInstance().getTabFrame().updateStyles(
                    messageFontButton.getFont());
        }

        tempSettings.setProperty("applicationFont",
                getEncodedFontName(appFontButton.getFont()));
        updateApplicationFonts(appFontButton.getFont(), prefs);

        String selectedStatus = (String) statusTheme.getItemAt(statusTheme
                .getSelectedIndex());
        tempSettings.setProperty("statusTheme", selectedStatus);
        tempSettings.setProperty("emoticonTheme", (String) emoticonTheme
                .getItemAt(emoticonTheme.getSelectedIndex()));

        BuddyList.getInstance().getBuddyListTree().repaint();
        if (BuddyList.getInstance().getTabFrame() != null) {
            BuddyList.getInstance().getTabFrame().repaint();
        }
        BuddyList.getInstance().updateButtons(selectedStatus);

        ConversationFormatter.getInstance().switchTheme(
                tempSettings.getProperty("emoticonTheme"));

        return tempSettings;
    }

    /**
     * Updates all the fonts in the application
     *
     * @param font
     *            the font to update to
     * @param prefs
     *            Description of the Parameter
     */
    public static void updateApplicationFonts(Font font, PreferencesDialog prefs) {
        com.valhalla.jbother.JBotherLoader.setupFont(font);
        updateAllUIs();

        if (prefs != null) {
            updateComponentTreeUI0(prefs.getTree());
            Iterator i = prefs.getPanels().keySet().iterator();
            while (i.hasNext()) {
                updateComponentTreeUI0((Component) prefs.getPanels().get(
                        i.next()));
            }

            i = PreferencesDialog.getPluginPanels().keySet().iterator();
            while (i.hasNext()) {
                updateComponentTreeUI0((Component) prefs.getPanels().get(
                        i.next()));
            }
        }
    }

    /**
     * Method to attempt a dynamic update for any GUI accessible by this JVM. It
     * will filter through all frames and sub-components of the frames.
     */
    public static void updateAllUIs() {
        CopyPasteContextMenu.newInstance();
        Frame frames[];
        int i1;
        frames = Frame.getFrames();
        i1 = 0;

        for (int i = 0; i < frames.length; i++) {
            updateWindowUI(frames[i]);
        }
    }

    /**
     * Method to attempt a dynamic update for all components of the given
     * <code>Window</code>.
     *
     * @param window
     *            The <code>Window</code> for which the look and feel update
     *            has to be performed against.
     */
    public static void updateWindowUI(Window window) {
        try {
            updateComponentTreeUI(window);
        } catch (Exception exception) {
        }

        Window windows[] = window.getOwnedWindows();

        for (int i = 0; i < windows.length; i++) {
            updateWindowUI(windows[i]);
        }
    }

    /**
     * A simple minded look and feel change: ask each node in the tree to
     * <code>updateUI()</code>-- that is, to initialize its UI property with
     * the current look and feel. Based on the Sun
     * SwingUtilities.updateComponentTreeUI, but ensures that the update happens
     * on the components of a JToolbar before the JToolbar itself.
     *
     * @param c
     *            Description of the Parameter
     */
    public static void updateComponentTreeUI(Component c) {
        updateComponentTreeUI0(c);
        c.invalidate();
        c.validate();
        c.repaint();
    }

    /**
     * Description of the Method
     *
     * @param c
     *            Description of the Parameter
     */
    private static void updateComponentTreeUI0(Component c) {

        Component[] children = null;

        if (c instanceof JToolBar) {
            children = ((JToolBar) c).getComponents();

            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    updateComponentTreeUI0(children[i]);
                }
            }

            ((JComponent) c).updateUI();
        } else {
            if (c instanceof JComponent) {
                ((JComponent) c).updateUI();
            }

            if (c instanceof JMenu) {
                children = ((JMenu) c).getMenuComponents();
            } else if (c instanceof Container) {
                children = ((Container) c).getComponents();
            }

            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    updateComponentTreeUI0(children[i]);
                }
            }
        }
    }
}

