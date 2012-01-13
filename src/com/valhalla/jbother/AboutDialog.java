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

package com.valhalla.jbother;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.Standard;
import com.valhalla.pluginmanager.PluginLoader;

/**
 * This obiously displays an about dialog with the credits for JBother
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class AboutDialog extends JDialog {
    private ResourceBundle resources = ResourceBundle
            .getBundle("JBotherBundle");

    private JButton okButton = new JButton(resources.getString("okButton"));

    private JButton creditsButton = new JButton(resources
            .getString("creditsButton"));

    private JTextPane textPane = new JTextPane();

    private JScrollPane scrollPane = new JScrollPane(textPane);

    private JPanel middlePanel = new JPanel(new BorderLayout());

    private JPanel version;

    private JLabel imageLabel = new JLabel(Standard
            .getIcon("images/splashimage.png"));

    private JPanel buttonPanel = new JPanel();

    private JPanel mainPanel;

    private JPanel container = new JPanel(new BorderLayout());

    private boolean credits = false;

    /**
     * Sets up the Visual components
     */
    public AboutDialog() {
        super(BuddyList.getInstance().getContainerFrame(), "About JBother", false);
        setTitle(resources.getString("aboutDialogTitle"));

        mainPanel = (JPanel) getContentPane();
        container.setLayout(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        mainPanel.setBorder(BorderFactory.createTitledBorder(resources
                .getString("aboutDialogTitle")));

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.add(okButton);
        buttonPanel.add(creditsButton);
        buttonPanel.add(Box.createHorizontalGlue());

        createVersionPanel();
        middlePanel.add(version, BorderLayout.NORTH);

        container.add(imageLabel, BorderLayout.NORTH);
        container.add(middlePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(container, BorderLayout.CENTER);

        imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        textPane.setEditable(false);

        // load in the application credits
        InputStream file = getClass().getClassLoader().getResourceAsStream(
                "credits.txt");
        InputStreamReader in = new InputStreamReader(file);
        BufferedReader reader = new BufferedReader(in);
        StringBuffer buffer = new StringBuffer();
        String line;

        try {

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            file.close();
        } catch (IOException e) {
            com.valhalla.Logger.debug("Couldn't fread credits file.");
        }

        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textPane.setText(buffer.toString());
        textPane.setCaretPosition(0);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DialogTracker.removeDialog(AboutDialog.this);
            }
        });

        creditsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeHandler();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setResizable(false);

        DialogTracker.addDialog(this, true, true);
     }

    /**
     * Toggles back and forth between showing the logo and version information
     * and showing the application credits
     */
    private void changeHandler() {
        if (!credits) {
            mainPanel.remove(container);
            mainPanel.add(scrollPane);

            creditsButton.setText(resources.getString("info"));
            mainPanel.setBorder(BorderFactory.createTitledBorder(resources
                    .getString("creditsButton")));

            credits = true;
        } else {
            mainPanel.remove(scrollPane);
            mainPanel.add(container);

            creditsButton.setText(resources.getString("creditsButton"));
            mainPanel.setBorder(BorderFactory.createTitledBorder(resources
                    .getString("aboutDialogTitle")));

            credits = false;
        }

        buttonPanel.repaint();
        mainPanel.repaint();
        validate();
    }

    /**
     * Creates a panel containing all of the Runtime version information
     */
    private void createVersionPanel() {
        version = new JPanel();
        ResourceBundle bundle = ResourceBundle.getBundle("buildid");

        version.setLayout(new GridLayout(0, 2));
        addItem(resources.getString("aboutJbotherVersion"),
                com.valhalla.jbother.JBother.JBOTHER_VERSION);
        addItem(resources.getString("aboutBildId"), bundle
                .getString("build.number"));
        addItem(resources.getString("aboutCreatedBy"), "Adam Olsen");
        addItem(resources.getString("aboutPluginApiVersion"), PluginLoader
                .getAPIVersion()
                + "");
        addItem(resources.getString("aboutSmackVersion"),
                org.jivesoftware.smack.SmackConfiguration.getVersion());
        addItem(resources.getString("aboutHostOperatingSystem"), System
                .getProperty("os.name")
                + " " + System.getProperty("os.version"));
        addItem(resources.getString("aboutHostSystemArchitecture"), System
                .getProperty("os.arch"));
        addItem(resources.getString("aboutJavaVersion"), System
                .getProperty("java.version"));
        addItem(resources.getString("aboutJavaVendor"), System
                .getProperty("java.vendor"));
    }

    /**
     * Adds a text label and a value
     *
     * @param name
     *            the label name
     * @param the
     *            value
     */
    private void addItem(String name, String value) {
        UIDefaults ui = UIManager.getDefaults();

        Font newFont = (Font) ui.get("Label.font");

        JLabel nameLabel = new JLabel(name + ":  ", SwingConstants.RIGHT);
        nameLabel.setFont(new Font(newFont.getName(), Font.BOLD, newFont
                .getSize()));

        JLabel valueLabel = new JLabel(value);
        version.add(nameLabel);
        version.add(valueLabel);
    }
}