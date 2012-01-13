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

package com.valhalla.pluginmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.Standard;

/**
 * Allows the user to download, install, and upgrade plugins all from within the
 * application that the plugins are used in
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class PluginManager extends JFrame {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "PluginManager", Locale.getDefault());

    private String mirror;

    private String script;

    private String installDir;

    private ArrayList pluginList = null;

    private ArrayList installedPlugins = null;

    private JLabel statusLabel = new JLabel("<html><b>"
            + resources.getString("status") + "</b>: ");

    private JTabbedPane pane = new JTabbedPane();

    private JButton closeButton = new JButton(resources
            .getString("closeButton"));

    private JPanel mainPanel;

    private JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

    private PluginManagerPanel listPanel = new PluginManagerPanel(this, false);

    private PluginManagerPanel managePanel = new PluginManagerPanel(this, true);

    private JButton unloadButton = new JButton(resources.getString("unload"));

    private JButton loadButton = new JButton(resources.getString("load"));

    /**
     * Constructs the Plugin Manager
     *
     * @param mirror
     *            the mirror to download the plugins from
     * @param script
     *            the script to get after connecting
     * @param installDir
     *            the location to install the plugins to
     */
    public PluginManager(String mirror, String script, String installDir) {
        setTitle(resources.getString("pluginManager"));
        this.mirror = mirror;
        this.script = script;
        this.installDir = installDir;

        initComponents();
        DialogTracker.addDialog(this, false, true);
        downloadPluginList();
    }

    /**
     * @return the download mirror
     */
    String getMirror() {
        return mirror;
    }

    /**
     * @return the download script
     */
    String getScript() {
        return script;
    }

    /**
     * @return the install dir
     */
    String getInstallDir() {
        return installDir;
    }

    /**
     * Sets up UI components
     */
    private void initComponents() {
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DialogTracker.removeDialog(PluginManager.this);
            }
        });

        mainPanel = (JPanel) getContentPane();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BorderLayout(5, 5));
        loadButton.setEnabled(false);
        unloadButton.setEnabled(false);

        ListActionListener listener = new ListActionListener();

        managePanel.getButton().addActionListener(listener);
        managePanel.getTable().getSelectionModel().addListSelectionListener(
                new HighlightListener());
        unloadButton.addActionListener(listener);
        loadButton.addActionListener(listener);

        listPanel.getButton().addActionListener(listener);

        // set up the manage panel
        managePanel.setButtonText(resources.getString("removePlugins"));
        JPanel bPanel = managePanel.getButtonPanel();
        bPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        bPanel.add(unloadButton);
        bPanel.add(loadButton);
        bPanel.validate();

        pane.add(managePanel, resources.getString("managePlugins"));
        pane.add(listPanel, resources.getString("installUpgradePlugins"));

        mainPanel.add(pane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(closeButton, BorderLayout.EAST);

        pack();
        setSize(new Dimension(520, 470));
        setLocationRelativeTo(null);
    }

    class HighlightListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            int rows[] = managePanel.getTable().getSelectedRows();

            boolean selected = (rows.length > 0);
            managePanel.getButton().setEnabled(selected);
            loadButton.setEnabled(selected);
            unloadButton.setEnabled(selected);
        }
    }

    /**
     * Listens for a button to be clicked in one of the list panels
     */
    class ListActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == managePanel.getButton())
                removeHandler();
            else if (e.getSource() == listPanel.getButton())
                installHandler(pluginList);
            else if (e.getSource() == unloadButton) {
                unloadPluginHandler(installedPlugins, true, managePanel);
                managePanel.setPlugins(installedPlugins);
            } else if (e.getSource() == loadButton) {
                loadPluginHandler(installedPlugins, managePanel);
                managePanel.setPlugins(installedPlugins);
            }
        }
    }

    private boolean checkSelectedRow(PluginManagerPanel panel, int current) {
        if (panel == null)
            return false;
        JTable table = panel.getTable();

        int rows[] = table.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            if (current == rows[i])
                return true;
        }

        return false;
    }

    /**
     * loads the checked plugins
     */
    private void loadPluginHandler(ArrayList list, PluginManagerPanel panel) {
        for (int i = 0; i < list.size(); i++) {
            Properties props = (Properties) list.get(i);
            if (props.getProperty("selected") != null
                    || checkSelectedRow(panel, i)) {
                Hashtable loadedPlugins = PluginLoader.getInstance()
                        .getLoadedPlugins();
                PluginJAR jar = PluginLoader.getInstance().getPlugin(
                        props.getProperty("name"));

                if (jar != null && !jar.getLoaded()) {
                    jar.loadPlugin();
                    loadedPlugins.put(props.getProperty("name"), jar);
                    props.remove("selected");
                }
            }
        }
    }

    /**
     * Unloads the checked plugins
     */
    private void unloadPluginHandler(ArrayList list, boolean mark,
            PluginManagerPanel panel) {
        for (int i = 0; i < list.size(); i++) {
            Properties props = (Properties) list.get(i);
            if (props.getProperty("selected") != null
                    || checkSelectedRow(panel, i)) {
                Hashtable loadedPlugins = PluginLoader.getInstance()
                        .getLoadedPlugins();

                PluginJAR jar = PluginLoader.getInstance().getPlugin(
                        props.getProperty("name"));

                if (jar != null) {
                    if (jar.getLoaded())
                        jar.unloadPlugin();
                    loadedPlugins.remove(props.getProperty("name"));

                    jar.close();

                    jar = null;
                    System.gc();
                }

                if (mark)
                    props.remove("selected");
            }
        }

    }

    /**
     * Installs the plugins selected in the install panel
     *
     * @param list
     *            the list to download
     */
    private void installHandler(ArrayList list) {
        setStatusText(resources.getString("downloading"));
        Thread thread = new Thread(new PluginDownloaderThread(this, list));
        thread.start();
    }

    /**
     * Removes the plugins that are selected in the manage panel
     */
    private void removeHandler() {
        int result = JOptionPane.showConfirmDialog(null, resources
                .getString("pluginRemoveConfirmation"), resources
                .getString("pluginManager"), JOptionPane.YES_NO_OPTION);

        if (result != 0)
            return;

        int rows[] = managePanel.getTable().getSelectedRows();
        ArrayList remove = new ArrayList();
        for (int i = 0; i < rows.length; i++) {
            Properties props = (Properties) installedPlugins.get(rows[i]);
            props.setProperty("selected", "true");

            remove.add(props);
        }

        unloadPluginHandler(remove, false, null);
        System.gc();
        remove = new ArrayList();

        for (int i = 0; i < rows.length; i++) {
            Properties props = (Properties) installedPlugins.get(rows[i]);
            Hashtable loadedPlugins = PluginLoader.getInstance()
                    .getLoadedPlugins();

            String name = props.getProperty("fileName");
            File file = new File(name);

            if (file.delete()) {
                remove.add(props);
            } else {
                throwError("Could not unload plugin!", false);
                return;
            }
        }

        for (int i = 0; i < remove.size(); i++) {
            installedPlugins.remove(remove.get(i));
        }

        managePanel.setPlugins(installedPlugins);
        downloadPluginList();
    }

    /**
     * Downloads a list of plugins
     */
    private void downloadPluginList() {
        PluginLoader loader = PluginLoader.getInstance();
        loader.findPlugins(installDir + File.separatorChar + "plugins");
        installedPlugins = loader.getInstalledPlugins();

        managePanel.setPlugins(installedPlugins);

        managePanel.getTable().setEnabled(false);
        listPanel.getTable().setEnabled(false);

        setStatusText(resources.getString("gettingPluginList"));
        Thread thread = new Thread(new DownloadListThread());
        thread.start();
    }

    /**
     * Called when the download thread is done downloading selected plugins
     */
    protected void doneDownloadingPlugins(ArrayList list) {
        File cacheDir = new File(installDir, "downloadcache");
        File pluginDir = new File(installDir, "plugins");
        if (!pluginDir.isDirectory() && !pluginDir.mkdirs()) {
            throwError("pluginDirectoryCreationError", true);
            return;
        }

        unloadPluginHandler(list, false, null);
        System.gc();

        for (int i = 0; i < list.size(); i++) {
            Properties props = (Properties) list.get(i);
            if (props.getProperty("selected") != null) {
                File file = new File(cacheDir, props.getProperty("fileName"));
                File newFile = new File(pluginDir, props
                        .getProperty("fileName"));
                if (newFile.exists() && !newFile.delete()) {
                    com.valhalla.Logger.debug("Could not delete old plugin!");
                }

                if (!file.renameTo(newFile)) {
                    com.valhalla.Logger.debug("Could not rename to new plugin");
                }
            }
        }

        PluginLoader.getNewInstance();
        System.gc();

        PluginLoader.getInstance().findPlugins(
                installDir + File.separatorChar + "plugins");
        loadPluginHandler(list, null);

        downloadPluginList();
    }

    /**
     * Called when the downloader thread is done downloading the list of
     * available plugins
     */
    protected void doneDownloadingList() {
        listPanel.setPlugins(pluginList);
        managePanel.getTable().setEnabled(true);
        listPanel.getTable().setEnabled(true);

        setStatusText(resources.getString("doneDownloading"));
    }

    /**
     * Sets the text in the status label
     *
     * @param text
     *            the text to set
     */
    private void setStatusText(String text) {
        statusLabel.setText("<html><b>" + resources.getString("status")
                + "</b>: " + text);
    }

    /**
     * Returns true if a plugin is already installed
     *
     * @return <tt>true</tt> if a plugin is already installed
     */
    private boolean pluginInstalled(Properties props) {
        boolean check = false;
        if (props.getProperty("name") == null)
            return check;
        for (int i = 0; i < installedPlugins.size(); i++) {
            Properties p = (Properties) installedPlugins.get(i);

            if (p.getProperty("name") != null
                    && p.getProperty("name").equals(props.getProperty("name")))
                check = true;
        }

        return check;
    }

    /**
     * Returns true if a plugin is upgradable
     *
     * @return <tt>true</tt> if a plugin is already upgradable
     */
    private boolean pluginUpgradable(Properties props) {
        boolean check = false;
        if (props.getProperty("name") == null)
            return check;
        for (int i = 0; i < installedPlugins.size(); i++) {
            Properties p = (Properties) installedPlugins.get(i);

            if (p.getProperty("name") != null
                    && p.getProperty("name").equals(props.getProperty("name"))) {
                try {
                    String installedVersion = p.getProperty("version");
                    String remoteVersion = props.getProperty("version");

                    int comp = remoteVersion.compareTo(installedVersion);

                    if (comp > 0)
                        check = true;

                    String installedAPI = p.getProperty("APIVersion");
                    String apiVersion = PluginLoader.getAPIVersion() + "";

                    comp = apiVersion.compareTo(installedAPI);
                    if( comp > 0 ) check = true;

                } catch (Exception e) {
                }
            }
        }

        return check;
    }

    /**
     * Collects the list of available plugins in their versions
     */
    class DownloadListThread implements Runnable {
        public void run() {
            BufferedReader in = null;
            pluginList = new ArrayList();

            try {
                URL url = new URL("http://" + mirror + script +
                    "?command=pluginList&apiVersion=" + PluginLoader.getAPIVersion());
                in = new BufferedReader(new InputStreamReader(url.openStream()));

                String line = null;
                String names[] = null;
                int lineCount = 0;

                while ((line = in.readLine()) != null) {
                    // the first line will indicate if we've connected
                    // successfully
                    if (lineCount == 0) {
                        if (!line.equals("Plugin list will follow")) {
                            throwError("invalidResponse", true);
                            break;
                        }
                    }

                    // second line gets a list of available arguments for each
                    // plugin
                    else if (lineCount == 1)
                        names = line.split("\t");

                    // all lines after are plugin information lines
                    else {
                        String de[] = line.split("\t");
                        //com.valhalla.Logger.debug( line );
                        if (de.length == names.length) {
                            Properties props = new Properties();
                            for (int i = 0; i < names.length; i++) {
                                props.setProperty(names[i], de[i]);
                            }

                            // make sure the plugin platform is compatible and
                            // the plugin api version is the same,
                            // otherwise
                            // ignore this plugin
                            if (PluginLoader.checkPlatform (props)
                                    && props.getProperty("APIVersion").equals(
                                            PluginLoader.getAPIVersion() + "")) {

                                com.valhalla.Logger.debug( props.getProperty("APIVersion"));
                                if (!pluginInstalled(props))
                                    pluginList.add(props);
                                else if (pluginUpgradable(props))
                                    pluginList.add(props);
                            }
                        } else {
                            com.valhalla.Logger.debug("Invalid plugin found.");
                        }
                    }

                    lineCount++;
                }

            } catch (Exception e) {
                throwError(e.getMessage(), false);
                e.printStackTrace();
            }

            doneDownloadingList();
        }
    }

    /**
     * Displays an error dialog
     *
     * @param message
     *            the message to display
     */
    void throwError(String message, boolean useResources) {
        if (useResources)
            message = resources.getString(message);
        final String tempMessage = message;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Standard.warningMessage(PluginManager.this, resources
                        .getString("pluginManager"), tempMessage);
                setStatusText(tempMessage);
            }
        });
    }
}