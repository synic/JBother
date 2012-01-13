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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import org.jivesoftware.smack.SmackConfiguration;

import com.valhalla.gui.*;
import com.valhalla.jbother.plugins.events.InitEvent;
import com.valhalla.misc.GnuPG;
import com.valhalla.pluginmanager.PluginChain;
import com.valhalla.jbother.preferences.*;
import com.valhalla.pluginmanager.PluginLoader;
import com.valhalla.settings.Arguments;
import com.valhalla.settings.Settings;

/**
 * Sets default Settings (if this is the first run of JBother), loads command
 * line arguments and settings from the settings file, sets the L&F.
 *
 * @author Adam Olsen (arolsen@gmail.com)
 * @created November 30, 2004
 * @version 1.0
 */
public class JBotherLoader {
    private static PluginLoader loader = PluginLoader.getInstance();
    
    private String profile;
    
    private static JFrame parentFrame = new JFrame();
    
    private static Properties discoveryCache = new Properties();
    
    private static boolean gnupgEnabled = false;
    private boolean done = false;
    
    /**
     * This is the main class, it basically just provides a loading point for
     * the login screen - and also allows arguments to be passed from the
     * command line.
     *
     * @param args
     *            arguments passed via the command line
     * @see com.valhalla.settings.Arguments
     */
    public void startJBother(String args[]) {
        Toolkit.getDefaultToolkit().sync();
        AWTEventListener listener = new AWTEventListener() {
            public void eventDispatched(AWTEvent evt) {
                if (evt instanceof KeyEvent) {
                    KeyEvent e = (KeyEvent) evt;
                    if (e.getModifiers() == KeyEvent.CTRL_MASK
                            && e.getKeyCode() == KeyEvent.VK_D) {
                        com.valhalla.Logger.getDebugWindow().setVisible(true);
                    } else if(e.getModifiers() == KeyEvent.CTRL_MASK
                            && e.getKeyCode() == KeyEvent.VK_P) {
                        if (!DialogTracker.containsDialog(PreferencesDialog.class))
                            new PreferencesDialog().setVisible(true);
                    }
                    
                }
            }
        };
        
        Toolkit.getDefaultToolkit().addAWTEventListener(listener,
                AWTEvent.KEY_EVENT_MASK);
        
        Arguments.setArguments(args);
        // initialize the argument holder
        if (Arguments.getInstance().getBoolean("help")) {
            showUsage();
        }
        
        
        if (!Arguments.getInstance().getBoolean("nosplash")) {
            new SplashScreen(this);
            doLaunch();
        } else {
            doLaunch();
            afterSplash();
        }
    }
    
    protected void doLaunch() {
        //setFocusManager();
        
        String smackTimeout = Arguments.getInstance().getProperty( "smacktimeout", "90000" );
        int stimeout = 90000;
        try {
            stimeout = Integer.parseInt( smackTimeout );
        } catch( Exception ex ) { }
        
        SmackConfiguration.setPacketReplyTimeout(stimeout);
        
        if (System.getProperty("mrj.version") != null) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty(
                    "com.apple.mrj.application.apple.menu.about.name",
                    "JBother");
        }
        
        // use custom directory for kiosk mode
        if (Arguments.getInstance().getProperty("kiosk") != null) {
            JBother.kiosk_mode = true;
            JBother.settingsDir = System.getProperty("user.home")
            + File.separatorChar + ".jbother_"
                    + (String) Arguments.getInstance().getProperty("kiosk");
            profile = ProfileManager.getOnlyProfile();
        } else {
            profile = ProfileManager.getDefaultProfile();            
            
            if(Arguments.getInstance().getProperty( "settingsdir" ) != null) {
                JBother.settingsDir = System.getProperty("user.home")
                + File.separatorChar + Arguments.getInstance().getProperty( "settingsdir" );
            }
        }
        
        if (profile == null || profile.equals("")) {
            profile = "default";
        }
        
        
        com.valhalla.Logger.setLogFile(JBother.settingsDir + File.separatorChar
                + "jbother.log");
        
        JBother.profileDir = JBother.settingsDir + File.separatorChar
                + "profiles" + File.separatorChar + profile;
        
        File cache = new File(JBother.settingsDir + File.separatorChar
                + "discocache.properties");
        try {
            FileInputStream in = new FileInputStream(cache);
            discoveryCache.load(in);
            in.close();
        } catch (Exception e) {
        }
        
        Settings.loadSettings(JBother.profileDir, "settings.properties");
        loadSettings();
        
        if (Arguments.getInstance().getProperty("webstart") == null
                && Arguments.getInstance().getProperty("noplugins") == null) {
            loadPlugins();
        }
        
        checkGPG();
        
        InitEvent event = new InitEvent(null);
        PluginChain.fireEvent(event);
        done = true;
    }
    
    protected boolean done() { return done; }
    
    /**
     * Checks if GPG is enabled.
     */
    public static void checkGPG() {
        // check to make sure GnuPG is executable
        GnuPG gnupg = new GnuPG();
        gnupgEnabled = gnupg.listKeys("");
    }
    
    /**
     * Gets the discoveryCache attribute of the JBotherLoader class
     *
     * @return The discoveryCache value
     */
    public static Properties getDiscoveryCache() {
        return discoveryCache;
    }
    
    /**
     * Gets the parentFrame attribute of the JBotherLoader class
     *
     * @return The parentFrame value
     */
    public static JFrame getParentFrame() {
        return parentFrame;
    }
    
    /**
     * Gets called after the splash screen is done showing
     */
    protected void afterSplash() {
        if (Settings.getInstance().getProperty("username") == null
                || Settings.getInstance().getProperty("defaultServer") == null) {
            ProfileEditorDialog dialog = new ProfileEditorDialog(BuddyList.getInstance().getContainerFrame(),null, profile);
            dialog.setExitOnClose(true);
            dialog.getDefaultBox().setSelected(true);
            dialog.setVisible(true);
            
            return;
        }
        
        if (Arguments.getInstance().getProperty("prof") != null) {
            ProfileManager m = new ProfileManager();
            m.setExitOnClose(true);
        } else {
            launch();
        }
    }
    
    /**
     * Loads some JBother settings
     */
    public static void loadSettings() {
        UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        
        ArrayList list = new ArrayList();
        for( int i = 0; i < lafs.length; i++ ) {
            list.add( lafs[i] );
        }
        
        list.add( new UIManager.LookAndFeelInfo( "InfoNode", "net.infonode.gui.laf.InfoNodeLookAndFeel" ) );
        
        UIManager.LookAndFeelInfo[] infos = new UIManager.LookAndFeelInfo[list.size()];
        list.toArray( infos );
        
        UIManager.setInstalledLookAndFeels( infos );
        
        loadLAF();
        
        String fontString = Settings.getInstance().getProperty(
                "applicationFont");
        if (fontString == null) {
            fontString = "Default-PLAIN-12";
        }
        
        Font newFont = Font.decode(fontString);
        
        setupFont(newFont);
    }
    
    /**
     * Description of the Method
     */
    public static void loadLAF() {
        String laf = Settings.getInstance().getProperty("lookAndFeel");
        if (Arguments.getInstance().getProperty("laf") != null) {
            laf = Arguments.getInstance().getProperty(laf);
        }
        
        loadLAF(laf);
    }
    
    /**
     * Loads the Look And Feel requested in the settings
     *
     * @param laf
     *            Description of the Parameter
     */
    public static void loadLAF(String laf) {
        UIManager.put("ClassLoader", loader);
        if (laf != null
                && Arguments.getInstance().getProperty("notheme") == null) {
            try {
                Class lafClass = loader.loadClass(laf);
                UIManager.setLookAndFeel((LookAndFeel) lafClass.newInstance());
            } catch (Exception e) {
                com.valhalla.Logger
                        .debug("Could not load look and feel settings.\n"
                        + e.getMessage());
            }
        }
        
        Frame[] frames = Frame.getFrames();
        for (int f = 0; f < frames.length; f++) {
            SwingUtilities.updateComponentTreeUI(frames[f]);
            frames[f].validate();
            
            Window[] windows = frames[f].getOwnedWindows();
            for (int w = 0; w < windows.length; w++) {
                if( windows[w] instanceof SplashScreen ) { continue; }
                
                SwingUtilities.updateComponentTreeUI(windows[w]);
                windows[w].validate();
            }
        }
    }
    
    /**
     * Description of the Method
     *
     * @param con
     *            Description of the Parameter
     */
    private static void recursivelyUpdate(Container con) {
        Component[] comps = con.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof Container) {
                recursivelyUpdate((Container) comps[i]);
            }
            SwingUtilities.updateComponentTreeUI(comps[i]);
            comps[i].validate();
        }
    }
    
    /**
     * Finds the available plugins and uses the PluginLoader to load them Once a
     * plugin is loaded, it's init() method is called so it can execute initial
     * code and register for various events in JBother
     */
    public static void loadPlugins() {
        ResourceBundle resources = ResourceBundle.getBundle("JBotherBundle",
                Locale.getDefault());
        
        loader
                .findPlugins(JBother.settingsDir + File.separatorChar
                + "plugins");
        loader.loadPlugins();
        
        ArrayList invalids = loader.getInvalidPlugins();
        for (int i = 0; i < invalids.size(); i++) {
            String name = (String) invalids.get(i);
            Standard.warningMessage(null, resources.getString("pluginError"),
                    MessageFormat.format(resources
                    .getString("pluginErrorMessage"),
                    new Object[] { name }));
        }
    }
    
    /**
     * Sets the font for the entire application
     *
     * @param font
     *            the font to use
     */
    public static void setupFont(Font font) {
        LookAndFeel lf = javax.swing.UIManager.getLookAndFeel();
        UIDefaults uid = lf.getDefaults();
        Enumeration k = uid.keys();
        while (k.hasMoreElements()) {
            Object key = k.nextElement();
            Object val = javax.swing.UIManager.get(key);
            if (val instanceof FontUIResource) {
                FontUIResource fuir = (FontUIResource) val;
                javax.swing.UIManager.put(key, new FontUIResource(font));
            }
        }
    }
    
    /**
     * launches the profile defined as default
     */
    public void launch() {
        String profile = ProfileManager.getDefaultProfile();
        if (profile == null || profile.equals("")) {
            profile = "default";
        }
        ProfileManager.loadProfile(profile);
    }
    
    /**
     * Description of the Method
     *
     * @param message
     *            Description of the Parameter
     */
    private void line(String message) {
        System.out.println(message);
    }
    
    /**
     * Description of the Method
     */
    private void showUsage() {
        line("\nJBother v" + JBother.JBOTHER_VERSION + " (c)2005 Adam Olsen\n");
        line("Usage Instructions:\n");
        
        line("java -jar JBother.jar [options]");
        line("Options are:\n");
        
        line("\t--nosplash\t\tdon't show the splash screen");
        line("\t--prof\t\t\topen profile manager at start");
        line("\t--noplugins\t\tdisable plugin manager");
        line("\t--debug\t\t\tenable debug messages");
        line("\t--help\t\t\tshow this message");
        line("\n");
        
        System.exit(0);
    }
    
    /**
     * Gets the gPGEnabled attribute of the JBotherLoader class
     *
     * @return The gPGEnabled value
     */
    public static boolean isGPGEnabled() {
        return gnupgEnabled;
    }
    
    public static void setGPGEnabled(boolean enabled) {
        gnupgEnabled = enabled;
    }
    
}

