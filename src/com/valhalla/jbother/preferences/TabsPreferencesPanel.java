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

package com.valhalla.jbother.preferences;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.*;

import com.valhalla.gui.MJTextField;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.ConversationPanel;
import com.valhalla.jbother.MessageDelegator;
import com.valhalla.settings.Settings;
import com.valhalla.settings.TempSettings;
import net.infonode.tabbedpanel.*;
import net.infonode.tabbedpanel.titledtab.*;
import net.infonode.util.*;

/**
 * Allows the user to change Tabs Preferences
 *
 * @author Adam Olsen
 * @version 1.0
 */
class TabsPreferencesPanel extends JPanel implements PreferencesPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private PreferencesDialog prefs;

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    private JCheckBox nonICQ = new JCheckBox(resources.getString("useNonICQ"));

    private JCheckBox dock = new JCheckBox(resources.getString("dockBuddyList"));
    private JLabel orientation = new JLabel( resources.getString("tabOrientation") + ": ");
    private JLabel dockWhereLabel = new JLabel( resources.getString("dockWhere") + ": " );
    private String[] orientations = new String[] {
        resources.getString( "bottom" ),
        resources.getString( "top" ),
        resources.getString( "left" ),
        resources.getString( "right" )
    };
    
    private String[] dockOptions = new String[] {
        resources.getString("left"),
        resources.getString("right")
    };

    private JCheckBox onAll = new JCheckBox(resources.getString("showCloseOnAll"));

    private JComboBox orient = new JComboBox( orientations );
    private JComboBox dockBox = new JComboBox( dockOptions );

    /**
     * Creates the PreferencesPanel
     *
     * @param dialog
     *            the enclosing PreferencesDialog
     */
    public TabsPreferencesPanel(PreferencesDialog dialog) {
        this.prefs = dialog;
        setBorder(BorderFactory.createTitledBorder(resources
                .getString("generalPreferences")));
        setLayout(grid);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0;
        c.gridwidth = 3;

        c.gridy++;
        grid.setConstraints(nonICQ, c);
        add(nonICQ);

        nonICQ.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dock.setEnabled(nonICQ.isSelected());
            }
        });

        c.gridy++;
        grid.setConstraints(dock, c);
        add(dock);

        c.gridy++;
        grid.setConstraints(onAll,c);
        add(onAll);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        grid.setConstraints(orientation, c);
        add(orientation);
        c.fill = GridBagConstraints.NONE;

        c.gridx++;
        grid.setConstraints(orient,c);
        add(orient);

//        c.gridy++;
//        c.gridx = 0;
//        c.gridwidth = 1;

//        grid.setConstraints(dockWhereLabel, c);
//        add(dockWhereLabel);

//        c.gridx++;
//        grid.setConstraints(dockBox, c);
//        add(dockBox);

        //this is the space taker
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel blankLabel = new JLabel("");
        c.weighty = .9;
        c.weightx = .9;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        grid.setConstraints(blankLabel, c);
        add(blankLabel);

        loadSettings();
    }

    /**
     * Loads settings from the settings file and fills out the form for defaults
     */
    private void loadSettings() {
        nonICQ.setSelected(Settings.getInstance().getBoolean(
                        "useTabbedWindow"));
        dock.setEnabled(nonICQ.isSelected());
        dock.setSelected(Settings.getInstance().getBoolean("dockBuddyList"));

        String orientation = Settings.getInstance().getProperty( "tabOrientation", "Down" );
        if( orientation.equals( "Up" ) ) orient.setSelectedIndex( 1 );
        else if( orientation.equals( "Down" ) ) orient.setSelectedIndex( 0 );
        else if( orientation.equals( "Left" ) ) orient.setSelectedIndex( 2 );
        else orient.setSelectedIndex( 3 );
        
        String dockOption = Settings.getInstance().getProperty( "dockOption", "Left" );
        if( dockOption.equals( "Right" ) ) dockBox.setSelectedIndex( 1 );

        onAll.setSelected(Settings.getInstance().getBoolean( "closeButtonOnAll") );
    }

    /**
     * Switches the application to a non-ICQ style interface
     */
    public void switchToNonICQInterface() {
        com.valhalla.Logger.debug("Switching to non-ICQ style interface");

        Vector panels = MessageDelegator.getInstance().getPanels();
        Iterator i = panels.iterator();
        while (i.hasNext()) {
            ConversationPanel panel = (ConversationPanel) i.next();

            JFrame frame = panel.getContainingFrame();

            if (frame == null)
                continue;
            boolean visible = frame.isVisible();
            frame.dispose();

            panel.setContainingFrame(null);
            if (visible)
                BuddyList.getInstance().addTabPanel(panel);
        }
    }

    /**
     * Switches the application to an ICQ style interface
     */
    public void switchToICQInterface() {
        com.valhalla.Logger.debug("Switching to ICQ style interface...");

        Vector panels = MessageDelegator.getInstance().getPanels();
        Iterator i = panels.iterator();
        while (i.hasNext()) {
            ConversationPanel panel = (ConversationPanel) i.next();
            if (BuddyList.getInstance().getTabFrame() != null
                    && BuddyList.getInstance().getTabFrame().contains(panel)) {
                BuddyList.getInstance().removeTabPanel(panel);

                com.valhalla.Logger.debug("creating frame for " + panel);
                panel.createFrame();
                panel.getContainingFrame().setVisible(true);
            }
        }
    }

    /**
     * Returns the currently chosen settings
     */
    public TempSettings getSettings() {
        TempSettings mySettings = new TempSettings();


        mySettings.setBoolean("useTabbedWindow", nonICQ.isSelected());
        mySettings.setBoolean("dockBuddyList", dock.isSelected());

        if (!Settings.getInstance().getBoolean("useTabbedWindow")
                && mySettings.getBoolean("useTabbedWindow")) {
            switchToNonICQInterface();
        }

        if (Settings.getInstance().getBoolean("useTabbedWindow")
                && !mySettings.getBoolean("useTabbedWindow")) {
            switchToICQInterface();
        }

        if (nonICQ.isSelected() && dock.isSelected()) {
            if (!BuddyList.getInstance().isDocked())
                BuddyList.getInstance().dockYourself();
        } else if (BuddyList.getInstance().isDocked())
            BuddyList.getInstance().undock();
        prefs.toFront();

        int index = orient.getSelectedIndex();
        Direction d = Direction.DOWN;
        if( index == 1 ) d = Direction.UP;
        else if( index == 2 ) d = Direction.LEFT;
        else if( index == 3 ) d = Direction.RIGHT;

        mySettings.setProperty("tabOrientation", d.getName());
        
        index = dockBox.getSelectedIndex();
        Direction d2 = Direction.LEFT;
        if( index == 1 ) d2 = Direction.RIGHT;
        mySettings.setProperty("dockOption", d2.getName());

        if( BuddyList.getInstance().getTabFrame() != null )
        {
            TabbedPanel pane = BuddyList.getInstance().getTabFrame().getTabPane();
            if( pane != null )
            {
                pane.getProperties().setTabAreaOrientation(d);
                BuddyList.getInstance().getTabFrame().resetCloseButtons(onAll.isSelected());
                pane.validate();
                pane.repaint();

            }
        }

        mySettings.setBoolean("closeButtonOnAll", onAll.isSelected());
        return mySettings;
    }
}
