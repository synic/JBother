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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Represents a plugin in a Jar File
 * 
 * @author Adam Olsen
 * @created October 31, 2004
 * @version 1.0
 */
public class PluginJAR {
    private Properties props = new Properties();

    private ArrayList contents = new ArrayList();

    private JarFile jar;

    private boolean loaded = false;

    private String location;

    private Plugin plugin = null;

    /**
     * Constructs the Plugin Jar
     * 
     * @param location
     *            The location of the plugin
     * @exception IOException
     *                if there is an exception while opening the plugin
     */
    public PluginJAR(String location) throws IOException {
        this.location = location;

        jar = new JarFile(location);

        loadContents();
    }

    /**
     * Gets the jarEntry with a specific name
     * 
     * @param name
     *            the name of the file you want to get the entry for
     * @return The jarEntry value
     */
    public JarEntry getJarEntry(String name) {
        JarEntry entry = null;
        try {
            entry = jar.getJarEntry(name);
        } catch (IllegalStateException ex) {
            try {
                jar = new JarFile(location);
                entry = jar.getJarEntry(name);
            } catch (IllegalStateException e) {
            } catch (IOException e) {
            }
        }

        return entry;
    }

    /**
     * Closes the JarFile
     */
    public void close() {
        try {
            jar.close();
        } catch (Exception e) {
        }
    }

    /**
     * Gets the InputStream from an entry
     * 
     * @param entry
     *            which entry to get the input stream for
     * @return The InputStream
     */
    public InputStream getInputStream(JarEntry entry) {
        InputStream stream = null;
        try {
            stream = jar.getInputStream(entry);
        } catch (IOException ex) {
        }

        return stream;
    }

    /**
     * @return the location of this Jar
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets whether or not the jar has been loaded
     * 
     * @param loaded
     *            true if boolean
     */
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * @return true if this jar is loaded
     */
    public boolean getLoaded() {
        return loaded;
    }

    /**
     * Loads the contents of the Jar into the Properties
     * 
     * @exception IOException
     *                if there is an error reading the jar file
     */
    public void loadContents() throws IOException {
        contents.removeAll(contents);
        Enumeration e = jar.entries();
        while (e.hasMoreElements()) {
            JarEntry entry = (JarEntry) e.nextElement();

            // if it contains the file "plugin.properties", it's a plugin
            // so read in the properties file
            if (entry.getName().equals("plugin.properties")) {
                InputStream stream = jar.getInputStream(entry);
                props.load(stream);

                File file = new File(location);

                props.setProperty("size", "" + file.length());
                props.setProperty("fileName", file.getPath());
                stream.close();
            }

            contents.add(entry.getName());
        }
    }

    /**
     * Loads the specified plugin
     * 
     * @return the Plugin
     */
    public Plugin loadPlugin() {
        PluginLoader loader = PluginLoader.getInstance();

        try {
            Class c = loader.loadClass(props.getProperty("mainClass"));
            if (c == null)
                return null;

            plugin = (Plugin) c.newInstance();
            this.loaded = plugin.init();
        } catch (Exception ex) {
            System.out.println(ex.getCause().getMessage());
            com.valhalla.Logger
                    .debug("Could not load the main class from the jar file.");
        }

        if (loaded)
            com.valhalla.Logger.debug(getName() + " Plugin Loaded");
        else
            com.valhalla.Logger.debug("Error loading " + getName());

        return plugin;
    }

    /**
     * Unloads the specified plugin
     */
    public void unloadPlugin() {
        com.valhalla.Logger.debug("Unloading Plugin " + getName());
        plugin.unload();
        this.loaded = false;
    }

    /**
     * Returns the jar information(
     * 
     * @return a Properties with information about the jar
     */
    public Properties getProperties() {
        return props;
    }

    /**
     * @return the name of this plugin
     */
    public String getName() {
        return props.getProperty("name");
    }

    /**
     * @param file
     *            the file to check
     * @return true if the jar contains a file
     */
    public boolean contains(String file) {
        for (int i = 0; i < contents.size(); i++) {
            String name = (String) contents.get(i);
            if (file.equals(name)) {
                return true;
            }
        }

        return false;
    }
}

