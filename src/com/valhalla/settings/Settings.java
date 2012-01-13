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

package com.valhalla.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import com.valhalla.jbother.JBother;

/**
 * A singleton Properties class to access and save settings
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class Settings extends Properties {
    private static Settings instance;

    private File settingsDir;

    private File settingsFile;

    private static boolean saving = false;

    /**
     * Reads the settings file and sets up the singleton
     *
     * @param dir
     *            the settings directory
     * @param settingsFile
     *            the settings file
     */
    public static void loadSettings(String dir, String settingsFile) {
        instance = new Settings(dir, settingsFile);
    }

    /**
     * Gets the Settings instance
     *
     * @return the Settings singleton
     */
    public static Settings getInstance() {
        return instance;
    }

    /**
     * Gets the settings directory
     *
     * @return the settings directory
     */
    public File getSettingsDir() {
        return settingsDir;
    }

    /**
     * Gets the settings file
     *
     * @return the settings file
     */
    public File getSettingsFile() {
        return settingsFile;
    }

    /**
     * Private constructor
     *
     * @param dir
     *            the settings directory
     * @param settingsFile
     *            the settings file
     */
    private Settings(String dir, String settingsFile) {
        instance = this;
        settingsDir = new File(dir);
        this.settingsFile = new File(dir, settingsFile);
        saving = true;

        // make sure the settings directory and file is there
        if (!settingsDir.isDirectory() || !this.settingsFile.isFile()
                || JBother.kiosk_mode)
            createDefaultSettings();

        try {
            // load the file into the properties
            InputStream stream = new FileInputStream(this.settingsFile);
            load(stream);
            stream.close();
        } catch (Exception e) {
            com.valhalla.Logger.debug("Could not load settings file");
            com.valhalla.Logger.debug(e.getMessage());
        }
        saving = false;
    }

    /**
     * Gets a boolean value
     *
     * @param key
     *            the key to get a boolean for
     * @return a boolean based on the key
     */
    public boolean getBoolean(String key) {
        return (getProperty(key) != null);
    }

    /**
     * Sets a boolean
     *
     * @param key
     *            the key to set
     * @param value
     *            the value to set the key to
     */
    public void setBoolean(String key, boolean value) {
        if (value)
            setProperty(key, "true");
        else
            remove(key);
        writeSettings();
    }

    /**
     * Sets the settings
     *
     * @param settings
     *            the settings to set it to
     */
    public static void setSettings(Properties settings) {
        if (instance != null)
            instance = (Settings) settings;
        writeSettings();
    }

    /**
     * Writes the settings to the settings file
     */
    public static void writeSettings() {
        if (saving)
            return;
        saving = true;
        if (instance == null) {
            com.valhalla.Logger
                    .debug("Fatal Error: Settings has not been initiated, and an attempt to access a value was made. Potential NullPointerException.");
            System.exit(1);
        }

        try {
            // create the output stream
            FileOutputStream stream = new FileOutputStream(
                    instance.settingsFile);
            StringBuffer buf = new StringBuffer();

            // store the properties file
            instance.store(stream, "JBother Settings File");
            stream.close();
        } catch (IOException e) {
            com.valhalla.Logger
                    .debug("There was an error saving your settings.");
            com.valhalla.Logger.debug(e.toString());
        }
        saving = false;
    }

    /**
     * If a settings file is not found, a default settings file is copied in to
     * place.
     */
    private void createDefaultSettings() {
        saving = true;
        if (!settingsDir.isDirectory() && !settingsDir.mkdirs()) {
            //creating the settings directory failed....
            com.valhalla.Logger
                    .debug("Fatal Error: Could not create the settings directory ("
                            + settingsDir.getName() + ").");
        }

        try {

            // copy the default file in to place
            InputStream stream = null;
            if (JBother.kiosk_mode)
                stream = instance.getClass().getClassLoader()
                        .getResourceAsStream("kiosksettings.properties");
            else
                stream = instance.getClass().getClassLoader()
                    .getResourceAsStream("defaultsettings.properties");
            if (stream == null)
                throw new IOException();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(stream));

            PrintWriter out = new PrintWriter(new FileWriter(settingsFile));
            String line = null;

            while ((line = in.readLine()) != null) {
                int i = line.indexOf("%username%");
                if (i > 0) {
                    line = line.substring(0, i)
                            + Arguments.getInstance().getProperty("kiosk_user")
                            + line.substring(i + 10);
                }
                i = line.indexOf("%password%");
                if (i > 0) {
                    line = line.substring(0, i)
                            + Arguments.getInstance().getProperty("kiosk_pass")
                            + line.substring(i + 10);
                }
                out.println(line);
            }

            in.close();
            out.close();

        } catch (IOException ex) {
            //creating an initial settings file failed...
            com.valhalla.Logger
                    .debug("Fatal Error: Could not create the settings file ("
                            + settingsFile.getPath() + ")\n" + ex.getMessage());
            System.exit(1);
        }
        saving = false;
    }

    public static void createKioskRoom() {
        File bookMarksDirectory = new File(JBother.profileDir
                + File.separatorChar + "gcbookmarks");
        if (!bookMarksDirectory.isDirectory() && !bookMarksDirectory.mkdir()) {
            com.valhalla.Logger
                    .debug("Could not create kiosk bookmarks directory.");
            return;
        }

        String settingsFile = JBother.profileDir + File.separatorChar
                + "gcbookmarks" + File.separatorChar
                + Arguments.getInstance().getProperty("kiosk") + ".gcb";

        try {
            PrintWriter out = new PrintWriter(new FileWriter(settingsFile));

            out.println(Arguments.getInstance().getProperty("kiosk"));
            out.println(Arguments.getInstance()
                    .getProperty("kiosk_roomservice"));
            out.println(Arguments.getInstance().getProperty("kiosk_nick"));
            out.println(Arguments.getInstance().getProperty("kiosk_roompass"));
            out.println("true");

            out.close();
        } catch (IOException ioe) {
            com.valhalla.Logger
                    .debug("I/O exception writing kiosk room properties.");
            com.valhalla.Logger.debug(ioe.getMessage());
            // System.exit(-1);
        }
    }
}