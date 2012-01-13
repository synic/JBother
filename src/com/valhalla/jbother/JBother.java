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
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

/**
 * JBother is a groovy Jabber client
 *
 * @author Adam Olsen (arolsen@gmail.com)
 * @version 1.0
 */
public class JBother {
    public static final String JBOTHER_VERSION = "0.9.0cvs";
    
    public static String settingsDir = System.getProperty("user.home")
    + File.separatorChar + ".jbother";
    
    public static String profileDir = JBother.settingsDir + File.separatorChar
            + "profiles";
    
    public static boolean kiosk_mode = false;
    
    public static boolean exceptionLock = false;
    
    /**
     * This is the main class, it basically just provides a loading point for
     * the login screen - and also allows arguments to be passed from the
     * command line.
     *
     * It checks the java version and if it's not greater than 1.4, it exits.
     *
     * @see com.valhalla.settings.Arguments
     * @param args
     *            arguments passed via the command line
     */
    public static void main(String args[]) {
        new EventProcessor();
        String version = System.getProperty("java.version");
        StringBuffer buf = new StringBuffer();
        
        // we have to check the version this way because java versions < 1.4
        // didn't have regular expressions (what a bite)
        int dots = 0;
        for (int i = 0; i < version.length(); i++) {
            if (version.charAt(i) == '.')
                dots++;
            if (dots >= 2)
                break;
            
            buf.append(version.charAt(i));
        }
        
        
        try{
            File currentdir = new File("." + File.separatorChar +  ".jbother");
            if(currentdir.exists() && currentdir.isDirectory()){
                JBother.settingsDir = currentdir.getCanonicalPath();
                JBother.profileDir = JBother.settingsDir +  File.separatorChar + "profiles";
            }
        }catch(Exception ignore){
            JBother.settingsDir = System.getProperty("user.home") +  File.separatorChar + ".jbother";
            JBother.profileDir = JBother.settingsDir + File.separatorChar +  "profiles";
        }
        
        if (Double.parseDouble(buf.toString()) < Double.parseDouble("1.4")) {
            ResourceBundle resources = ResourceBundle.getBundle(
                    "JBotherBundle", Locale.getDefault());
            
            JOptionPane
                    .showMessageDialog(null,
                    resources.getString("jdk14Needed"), resources
                    .getString("javaVersionError"),
                    JOptionPane.WARNING_MESSAGE);
            
            System.exit(1);
        } else {
            com.valhalla.Logger.debug("Java version " + version + " ok");
            try {
                new JBotherLoader().startJBother(args);
            } catch (Throwable e) {
                com.valhalla.Logger
                        .debug("An uncaught exception has occurred.  Stacktrace is below.");
                com.valhalla.Logger
                        .debug("---------------------------------------------------------");
                com.valhalla.Logger.debug(e.toString());
                StackTraceElement el[] = e.getStackTrace();
                for (int i = 0; i < el.length; i++) {
                    com.valhalla.Logger.debug(el[i].toString());
                }
                com.valhalla.Logger
                        .debug("---------------------------------------------------------");
            }
        }
    }
}

class EventProcessor extends EventQueue {
    public EventProcessor() {
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(this);
    }
    
    protected void dispatchEvent(AWTEvent es) {
        try {
            super.dispatchEvent(es);
        } catch (Throwable e) {
            if (!JBother.exceptionLock) {
                com.valhalla.Logger
                        .debug("An uncaught exception has occurred.  Stacktrace is below.");
                com.valhalla.Logger
                        .debug("---------------------------------------------------------");
                com.valhalla.Logger.debug(e.toString());
                StackTraceElement el[] = e.getStackTrace();
                for (int i = 0; i < el.length; i++) {
                    com.valhalla.Logger.debug(el[i].toString());
                }
                com.valhalla.Logger
                        .debug("---------------------------------------------------------");
                
                // Throttle the Debug Console.
                JBother.exceptionLock = true;
                Timer timer = new Timer();
                timer.schedule(new ExceptionLock(), 50000);
            }
        }
    }
}

class ExceptionLock extends TimerTask {
    
    public void run() {
        JBother.exceptionLock = false;
    }
}
