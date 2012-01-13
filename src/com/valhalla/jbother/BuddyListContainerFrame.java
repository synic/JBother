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
package com.valhalla.jbother;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.valhalla.gui.Standard;
import com.valhalla.jbother.plugins.events.ExitingEvent;
import com.valhalla.pluginmanager.PluginChain;
import com.valhalla.settings.Settings;

/**
 * Provides a containing frame for the BuddyList panel when in ICQ mode
 * 
 * @author Adam Olsen
 */
public class BuddyListContainerFrame extends JFrame {
    private BuddyList buddyList;

    /**
     * The default constructor
     * 
     * @param buddyList
     *            the buddy list panel that should be contained in this fram
     */
    public BuddyListContainerFrame(BuddyList buddyList) {
        super("JBother");
        this.buddyList = buddyList;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setIconImage(Standard.getImage("frameicon.png"));
        initComponents();
        getContentPane().add(buddyList);
        pack();

        setPreferredDimensions();
        setPreferredLocation();
        addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                BuddyList.getInstance().saveSettings();
            }
        });
    }

    /**
     * Sets up the components for this container
     */
    private void initComponents() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ExitingEvent event = new ExitingEvent(buddyList);
                PluginChain.fireEvent(event);
                if (event.getExit()) {
                    buddyList.signOff();
                    buddyList.quitHandler();
                }
            }

        });
    }

    /**
     * Load saved settings from the last session and set the buddy list to the
     * sizes that were saved.
     */
    protected void setPreferredDimensions() {
        String width = Settings.getInstance().getProperty("buddyListWidth");
        String height = Settings.getInstance().getProperty("buddyListHeight");

        Dimension dim = new Dimension(100, 100);

        try {
            dim.setSize(Integer.parseInt(width), Integer.parseInt(height));
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }

        setSize(dim);
    }

    /**
     * Loads the saved settings from any previous settings
     */
    protected void setPreferredLocation() {
        //load the settings from the settings file
        String xString = Settings.getInstance().getProperty("buddyListX");
        String yString = Settings.getInstance().getProperty("buddyListY");

        if (xString == null)
            xString = "100";
        if (yString == null)
            yString = "100";

        double x = 100;
        double y = 100;

        try {
            x = Double.parseDouble(xString);
            y = Double.parseDouble(yString);
        } catch (NumberFormatException e) {
        } catch (NullPointerException e) {
        }

        if (x < -50.0)
            x = 100.0;
        if (y < -50.0)
            y = 100.0;

        setLocation((int) x, (int) y);
    }

}