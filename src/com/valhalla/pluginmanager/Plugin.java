package com.valhalla.pluginmanager;

/**
 * Defines the main plugin interface. This interface MUST be implemented by all
 * JBother plugins. The plugin MUST have a file called "plugin.properties"
 * somewhere in the jar, and this file MUST define mainClass to be a class that
 * implements this interface, or the plugin will not work
 * 
 * @author Adam Olsen
 * @version 1.0
 */
public interface Plugin {
    /**
     * This method will be called on the mainClass of the plugin at load time.
     * This method is responsible for initializing the plugin and registering
     * for different plugin events.
     * 
     * @return returns true if the plugin loaded successfully, false if not
     */
    public boolean init();

    /**
     * This method will be called on the mainClass of the plugin to unload it.
     * After this method is called the class should no longer be being used.
     */
    public void unload();
}