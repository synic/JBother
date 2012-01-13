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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.jar.JarEntry;

import com.valhalla.settings.Settings;

/**
 * This class supports the loading of custom plugins from jar files. The main
 * class in the Jar should be named after the jar filename and should be in the
 * "package" String passed to the PluginLoader package. For example, the plugin
 * LookAndFeel would be in a jar file called LookAndFeel.jar, and if the package
 * parameter was com.valhalla.jbother.plugins there would need to be a class
 * called <code>com.valhalla.jbother.plugins.LookAndFeel</code> in the jar
 * file
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class PluginLoader extends ClassLoader {
    private static PluginLoader instance = null;

    private static int pluginAPIVersion = 91214;

    private String pluginDir = null;

    private Hashtable loadedClasses = new Hashtable(); // contains information
                                                       // about what classes
                                                       // have

    // already been loaded
    private static ArrayList availablePlugins = new ArrayList(); // the list of
                                                                 // available
                                                                 // plugins

    private static ArrayList invalidPlugins = new ArrayList();

    private static ArrayList incompatiblePlugins = new ArrayList();

    private static ArrayList installedPlugins = new ArrayList();

    private static Hashtable loadedPlugins = new Hashtable();

    /**
     * The default constructor
     *
     * @param pluginDir
     *            the directory to search for plugins in .jar format
     */
    private PluginLoader() {
    }

    /**
     * Gets the singleton of this class
     *
     * @return the PluginLoader singleton
     */
    public static PluginLoader getInstance() {
        if (instance == null)
            instance = new PluginLoader();
        return instance;
    }

    public static PluginLoader getNewInstance() {
        instance = new PluginLoader();
        return instance;
    }

    /**
     * Finds which plugin contains the specified native library, extracts it to
     * a temporary location, and returns the path to that location or null if
     * the native library could not be found.
     *
     * @param the
     *            name of the library
     * @return the location of the newly created temporary file
     */
    public String findLibrary(String lib) {
        String l = super.findLibrary(lib);
        if (l != null)
            return l;
        PluginJAR jar = getPluginThatContains("native/" + lib);
        if (jar == null)
            return null;

        try {
            JarEntry entry = jar.getJarEntry("native/" + lib);

            int index = lib.lastIndexOf('.');
            String suffix = lib.substring(index, lib.length());
            lib = lib.substring(0, index);
            File temporaryDll = File.createTempFile(lib, suffix);

            InputStream inputStream = jar.getInputStream(entry);

            FileOutputStream outputStream = new FileOutputStream(temporaryDll);
            byte[] array = new byte[8192];
            for (int i = inputStream.read(array); i != -1; i = inputStream
                    .read(array)) {
                outputStream.write(array, 0, i);
            }

            outputStream.close();
            temporaryDll.deleteOnExit();
            com.valhalla.Logger.debug(temporaryDll.getPath());
            return temporaryDll.getPath();
        } catch (Exception ex) {
            com.valhalla.Logger.logException(ex);
            return null;
        }
    }

    /**
     * @return a list of currently loaded plugins
     */
    public Hashtable getLoadedPlugins() {
        return loadedPlugins;
    }

    /**
     * @return a list of available plugins
     */
    public ArrayList getAvailablePlugins() {
        return availablePlugins;
    }

    /**
     * @return a list of installed plugins
     */
    public ArrayList getInstalledPlugins() {
        return installedPlugins;
    }

    /**
     * @return a list of invalid plugins
     */
    public ArrayList getInvalidPlugins() {
        return invalidPlugins;
    }

    /**
     * @return a list of incompatible plugins
     */
    public ArrayList getIncompatiblePlugins() {
        return incompatiblePlugins;
    }

    /**
     * Returns the jar file for the specified plugin name
     *
     * @param name
     *            the plugin name
     * @return the jar file corresponding to the name
     */
    public PluginJAR getPlugin(String name) {
        PluginJAR jar = null;
        for (int i = 0; i < availablePlugins.size(); i++) {
            PluginJAR temp = (PluginJAR) availablePlugins.get(i);
            if (temp.getName().equals(name))
                jar = temp;
        }

        return jar;
    }

    /**
     * Returns the PluginJAR that contains <tt>file</tt>
     *
     * @param file
     *            the file to search for
     * @return the PluginJAR that contains <tt>file</tt>
     */
    private PluginJAR getPluginThatContains(String file) {
        PluginJAR jar = null;
        for (int i = 0; i < availablePlugins.size(); i++) {
            PluginJAR temp = (PluginJAR) availablePlugins.get(i);
            if (temp.contains(file))
                jar = temp;
        }

        return jar;
    }

    /**
     * Returns the plugin that is represented by location
     *
     * @param location
     *            the location of the plugin
     * @return the PluginJAR representing the specified location
     */
    private PluginJAR getPluginFromLocation(String location) {
        PluginJAR jar = null;

        for (int i = 0; i < availablePlugins.size(); i++) {
            PluginJAR temp = (PluginJAR) availablePlugins.get(i);
            if (temp.getLocation().equals(location))
                jar = temp;
        }

        return jar;
    }

    /**
     * Gets the current plugin API version
     *
     * @return the current plugin API version
     */
    public static int getAPIVersion() {
        return pluginAPIVersion;
    }

    /**
     * Attempts to load the available plugins
     */
    public void loadPlugins() {
        for (int i = 0; i < availablePlugins.size(); i++) {
            PluginJAR jar = (PluginJAR) availablePlugins.get(i);

            if (!jar.getLoaded()) {
                try {
                    jar.loadPlugin();
                } catch (Exception e) {
                    com.valhalla.Logger.logException(e);
                }
            } else {
                com.valhalla.Logger.debug("Plugin is already loaded.");
            }
        }
    }

    /**
     * Reads in all the available plugins and the information about them
     */
    public void findPlugins(String d) {
        this.pluginDir = d;
        String files[] = null;
        File pluginDir = null;

        installedPlugins.clear();
        ArrayList newAvailable = new ArrayList();
        invalidPlugins.clear();
        incompatiblePlugins.clear();

        try {
            // open up the plugin directory
            pluginDir = new File(this.pluginDir);
            if (!pluginDir.isDirectory())
                return;
            files = pluginDir.list();
        } catch (Exception exception) {
            return;
        }

        boolean ignoreIncompatible =
            Settings.getInstance().getBoolean("ignoreIncompatiblePlugins");

        for (int i = 0; i < files.length; i++) {
            // if it's a jar file, it might be a plugin
            if (files[i].endsWith(".jar")) {
                try {
                    PluginJAR jar = null;
                    String loc = pluginDir.getPath() + File.separatorChar
                            + files[i];
                    jar = getPluginFromLocation(loc);

                    if (jar == null || !jar.getLoaded()) {
                        jar = new PluginJAR(pluginDir.getPath()
                                + File.separatorChar + files[i]);
                    }

                    Properties props = jar.getProperties();
                    String name = props.getProperty("name");

                    if (props == null || props.getProperty("mainClass") == null
                            || name == null)
                        invalidPlugins.add(files[i]);
                    else if (ignoreIncompatible && !checkPlatform (props)) {
                        incompatiblePlugins.add(name);
                        com.valhalla.Logger.debug("Incompatible plugin found: " +
                                                  name + " (" + files[i] + ")");
                    } else {
                        int version = Integer.parseInt(props
                                .getProperty("APIVersion"));
                        if (version != pluginAPIVersion) {
                            invalidPlugins.add(name);
                        } else {
                            newAvailable.add(jar);
                        }

                        installedPlugins.add(props);
                    }
                } catch (Exception ex) {
                    com.valhalla.Logger.debug("Invalid plugin found: "
                            + files[i]);
                }
            }
        }

        availablePlugins = newAvailable;
    }

    /**
     * Gets a resource as a stream
     *
     * @param resource
     *            the resource to get
     * @return the stream
     */
    public InputStream getResourceAsStream(String resource) {
        InputStream stream = null;
        stream = this.getClass().getClassLoader().getResourceAsStream(resource);
        if (stream != null)
            return stream;

        PluginJAR jar = getPluginThatContains(resource);

        if (jar != null) {
            // find the entry in the Jar file
            JarEntry entry = jar.getJarEntry(resource);

            stream = jar.getInputStream(entry);
        } else {
            com.valhalla.Logger.debug("Stream was null for " + resource);
        }

        return stream;
    }

    protected Hashtable getLoadedClasses() {
        return loadedClasses;
    }

    /**
     * Loads the Class out of the plugin file
     *
     * @param className
     *            the class name to load
     * @param resolveIt
     *            true to resolve the class (load dependancies)
     * @return the Class if it was found
     * @throws <code>ClassNotFoundException</code> if the class could not be
     *             found in the system or any of the plugin files
     */
    public synchronized Class loadClass(String className, boolean resolveIt)
            throws ClassNotFoundException {
        Class result = null;

        String origName = className;

        // if the Class was already loaded, return it
        result = (Class) loadedClasses.get(origName);
        if (result != null)
            return result;

        // check to see if it's a system class
        try {
            result = this.getClass().getClassLoader().loadClass(className);
            //result = super.findSystemClass( className );

            return result;
        } catch (ClassNotFoundException exception) {
        }

        className = className.replaceAll("\\.", "/");
        className += ".class";

        // if the class name starts with "java", we will not load it
        // because of the security risks involved. A class in the package
        // java. or javax. will be able to access protected members of system
        // Classes, and this isn't good.
	
/*        *REMOVED* as official JBother plugins are checked by devel team 
 *        anyways...
 *        if (className.startsWith("java"))
            throw new ClassNotFoundException();
*/
        PluginJAR jar = getPluginThatContains(className);

        if (jar == null)
            throw new ClassNotFoundException();

        try {
            // find the entry in the Jar file
            JarEntry entry = jar.getJarEntry(className);
            InputStream stream = jar.getInputStream(entry);

            // read it into a class
            int len = (int) entry.getSize();
            int offset = 0;
            int success = 0;
            byte[] contents = new byte[len];

            while (success < len) {
                len -= success;
                offset += success;
                success = stream.read(contents, offset, len);
                if (success == -1)
                    throw new ClassNotFoundException();
            }

            stream.read(contents, 0, (int) entry.getSize());

            // actually define the class
            result = defineClass(origName, contents, 0, contents.length);

            if (resolveIt)
                resolveClass(result);

            // mark this class as already loaded and put it in the cache
            loadedClasses.put(origName, result);

            return result;
        } catch (Exception e) {
            throw new ClassNotFoundException(className);
        }
    }

    /**
     *  Checks whether the current platform (OS name and architecture) is
     *  compatible with the given plugin's platform or not.
     *
     *  @param pluginProps  plugin properties
     *  @return true if the plugin is compatible and false otherwise
     */
    static boolean checkPlatform (Properties pluginProps)
    {
        // @todo it may be worth to introduce 'os.match' and 'arch.match'
        // plugin properties to perform more flexible regexp match

        String os = pluginProps.getProperty("os");
        String arch = pluginProps.getProperty("arch");

        boolean ok = os == null || os.equals("all") ||
                     System.getProperty("os.name").startsWith(os);

        if (ok) {
            ok = arch == null || arch.equals("all") ||
            System.getProperty("os.arch").equals(arch);
        }

        return ok;
    }
}
