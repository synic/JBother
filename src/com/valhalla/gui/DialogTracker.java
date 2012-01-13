/*
 *  Copyright (C) 2003 Adam Olsen
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.valhalla.gui;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Tracks the different dialogs in JBother. Keeps track of dialogs that should
 * only be opened one at a time or dialogs that should be killed when the
 * connection is lost
 * 
 * @author Adam Olsen
 * @created October 22, 2004
 * @version 1.0
 */
public class DialogTracker extends ArrayList {
    private static DialogTracker instance = null;

    private HashMap kDialogs = null;

    /**
     * Default constructor - this is a singleton, so it's private
     */
    private DialogTracker() {
        kDialogs = new HashMap();
    }

    /**
     * Make sure the singleton is active
     */
    private static void checkInstance() {
        if (DialogTracker.instance == null) {
            DialogTracker.instance = new DialogTracker();
        }
    }

    /**
     * Returns the dialog tracker's instance
     * 
     * @return The instance value
     */
    public static DialogTracker getInstance() {
        checkInstance();
        return instance;
    }

    /**
     * Checks to see if the tracker is tracking a specific dialog
     * 
     * @param dialog
     *            the dialog class to check
     * @return true if the dialog tracker is tracking the dialog
     */
    public static boolean containsDialog(Class dialog) {
        checkInstance();
        for (int i = 0; i < DialogTracker.instance.size(); i++) {
            Window check = (Window) DialogTracker.instance.get(i);
            if (check.getClass().getName().equals(dialog.getName())) {
                check.toFront();
                return true;
            }
        }

        return false;
    }

    /**
     * Kills all the dialogs that are supposed to be killed when the connection
     * is lost
     */
    public static void kill() {
        checkInstance();
        for (int i = 0; i < DialogTracker.instance.size(); i++) {
            Window check = (Window) DialogTracker.instance.get(i);
            if (instance.kDialogs.get(check) != null) {
                check.setVisible(false);
                DialogTracker.removeDialog(check);
                i--;
                check.dispose();
            }
        }
    }

    /**
     * Removes a dialog from the tracker, and calls it's dispose() method
     * 
     * @param dialog
     *            the dialog to remove
     */
    public static void removeDialog(Window dialog) {
        checkInstance();

        for (int i = 0; i < DialogTracker.instance.size(); i++) {
            Window check = (Window) DialogTracker.instance.get(i);
            if (check == dialog) {
                DialogTracker.instance.remove(i);
                instance.kDialogs.remove(check);
                check.setVisible(false);
                check.dispose();
                i--;
            }
        }
    }

    /**
     * Adds a dialog to the tracker
     * 
     * @param dialog
     *            the dialog to add
     * @param signOffKill
     *            set to true if you want the dialog to be destroyed when the
     *            connection is lost
     * @param addCloseHandler
     *            set to true if you want a default close handler that will
     *            remove the dialog to be added
     */
    public static void addDialog(final Window dialog, boolean signOffKill,
            boolean addCloseHandler) {
        checkInstance();
        if (addCloseHandler) {
            dialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    DialogTracker.removeDialog(dialog);
                }
            });
        }

        if (signOffKill) {
            instance.kDialogs.put(dialog, "true");
        }

        addDialog(dialog);
    }

    /**
     * Adds a dialog
     * 
     * @param dialog
     *            the dialog to add
     */
    public static void addDialog(Window dialog) {
        checkInstance();
        DialogTracker.instance.add(dialog);
    }
}

