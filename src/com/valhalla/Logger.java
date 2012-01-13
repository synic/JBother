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

package com.valhalla;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingUtilities;

import com.valhalla.gui.DebugWindow;
import com.valhalla.settings.Arguments;

/**
 * Class used for logging/debugging
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class Logger {
    private static DebugWindow d = new DebugWindow();

    private static PrintWriter out = null;

    private static FileWriter fw;

    /**
     * Logs an exceptions stack trace if the log file is open
     *
     * @param e
     *            the exception
     */
    public static void logException(Exception e) {
        debug("An uncaught exception has occurred.  Stacktrace is below.");
        debug("---------------------------------------------------------");
        debug(e.toString());
        StackTraceElement el[] = e.getStackTrace();
        for (int i = 0; i < el.length; i++) {
            debug(el[i].toString());
        }
        debug("---------------------------------------------------------");
    }

    public static void setLogFile(String file) {
        try {
            File f = new File(file);
            fw = new FileWriter(f,true);
            out = new PrintWriter(fw, true);
        } catch (Exception ex) {
            debug("Could not open debug log: " + ex.getMessage());
        }
    }

    public static void closeLog() {
        if (fw != null) {
            try {
                fw.close();
            } catch (Exception ex) {
                debug("Error closing debug log file: " + ex.getMessage());
            }
        }
    }

    /**
     * Outputs to the console only if the "debug" system property is set.
     *
     * @param message
     *            The message to output to the console
     */
    public static void debug(final String message) {
        // get the current date/time and output it prettily to the console
        SimpleDateFormat formatter = new SimpleDateFormat("[HH:mm:ss]: ");
        final String date = formatter.format(new Date());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                d.append(date + message);
                if (Arguments.getInstance() != null
                        && Arguments.getInstance().getBoolean("debug")) {
                    if (!d.isVisible())
                        d.setVisible(true);
                }
            }
        });

        if (out != null) {
            out.println(date + message);
        }

        System.out.println(date + message);
    }

    public static void debug(Object message) {
        debug(message + "");
    }

    public static void debug(int num) {
        debug(num + "");
    }

    public static DebugWindow getDebugWindow() {
        return d;
    }
}
