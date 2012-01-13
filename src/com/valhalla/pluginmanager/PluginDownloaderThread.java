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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.*;

import com.valhalla.gui.ProgressDialog;

/**
 * Downloads plugins
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class PluginDownloaderThread implements Runnable {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "PluginManager", Locale.getDefault());

    private PluginManager manager = null;

    private ProgressDialog progress = null;

    private ArrayList list = null;

    private double size = 0;

    private String mirror, script, installDir;

    private boolean cancelled = false;

    /**
     * Sets up the thread
     *
     * @param manager
     *            the PluginManager that contains this thread
     * @param list
     *            the list of plugins to download
     * @param progress
     *            the dialog that tracks this threads progress
     */
    public PluginDownloaderThread(PluginManager manager, ArrayList list) {
        this.manager = manager;
        this.list = list;

        mirror = manager.getMirror();
        script = manager.getScript();
        installDir = manager.getInstallDir();

        size = calculateSize(list);

        this.progress = new ProgressDialog(manager, resources
                .getString("downloading"), 0, (int) size + 1);
        JButton button = this.progress.getButton();
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                progress.delete();
                cancelled = true;
            }
        });
    }

    /**
     * Called by the enclosing Thread
     */
    public void run() {
        if (size <= 0.0) {
            manager.throwError("selectPlugins", true);
            progress.delete();

            return;
        }

        InputStream in = null;
        BufferedReader bIn = null;

        File cacheDir = new File(installDir, "downloadcache");
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            progress.delete();
            manager.throwError("couldNotCreateCache", true);
            return;
        }

        try {
            int totalRead = 0;

            for (int i = 0; i < list.size(); i++) {
                if (cancelled)
                    return;
                Properties props = (Properties) list.get(i);
                if (props.getProperty("selected") != null) {

                    URL url = new URL("http://" + mirror + script +
                        "?command=getPlugin&apiVersion=" + PluginLoader.getAPIVersion()
                        + "&plugin=" + props.getProperty("fileName"));
                    System.out.println("accessing URL:" + url);
                    in = url.openStream();

                    File outFile = new File(cacheDir, props
                            .getProperty("fileName"));
                    int pluginSize = Integer
                            .parseInt(props.getProperty("size"));
                    FileOutputStream fileOut = new FileOutputStream(outFile);

                    int readSize = 0;
                    int totalSize = 0;
                    byte buf[] = new byte[1024];

                    while (true) {
                        if (cancelled) {
                            in.close();
                            fileOut.close();
                            return;
                        }

                        readSize = in.read(buf, 0, 1024);
                        if (readSize == -1)
                            break;

                        fileOut.write(buf, 0, readSize);
                        totalRead += readSize;
                        totalSize += readSize;

                        final int tempSize = totalRead;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                progress.setValue(tempSize);
                                progress.repaint();
                            }
                        });
                    }

                    fileOut.close();

                    if (cancelled)
                        return;

                    if (totalSize != pluginSize) {
                        com.valhalla.Logger.debug(pluginSize + " " + totalSize);
                        manager.throwError("downloadError", true);
                        progress.delete();
                        return;
                    }

                    in.close();
                }
            }

        } catch (ConnectException e) {
            JOptionPane.showMessageDialog(null,
                resources.getString("couldNotConnectThroughProxy"),
                resources.getString("connectionError"),
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            manager.throwError(e.getMessage(), false);
            progress.delete();
            return;
        }

        if (cancelled)
            return;

        progress.delete();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                manager.doneDownloadingPlugins(list);
            }
        });
    }

    /**
     * Gets the size of the selected plugins in an array
     *
     * @param list
     *            the plugin list
     * @return the size of the selected plugins
     */
    public static double calculateSize(ArrayList list) {
        double size = 0;
        for (int i = 0; i < list.size(); i++) {
            Properties p = (Properties) list.get(i);
            if (p.getProperty("selected") != null) {
                try {
                    double s = Double.parseDouble(p.getProperty("size"));
                    size += s;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return size;
    }
}