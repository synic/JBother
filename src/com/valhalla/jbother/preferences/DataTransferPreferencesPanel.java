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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.*;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import javax.swing.*;

import com.valhalla.gui.MJTextField;
import com.valhalla.settings.Settings;
import com.valhalla.settings.TempSettings;


/**
 * preferences panel for file transfer-related settings.
 *
 * @author Lukasz Wiechec
 * @created Feb 2, 2005 5:36:43 PM
 * @version 1.0
 */

public class DataTransferPreferencesPanel extends JPanel
  implements PreferencesPanel
{
    private static String[] PROXIES = new String[] { "proxy.netlab.cz:7777", "proxy65.jabber.autocom.pl:7777", "proxy.jabber.org:7777" };
    private static String fId = "$Id$";

    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();
    
    private JLabel proxy65Label = new JLabel(resources.getString("preferredProxy65") + ": ");
    private JLabel proxy65Note = new JLabel(resources.getString("proxy65Note"));
    private JComboBox proxy65Host = new JComboBox(getProxies());
    private JCheckBox preferIBB = new JCheckBox(resources.getString("preferIBB"));

    public DataTransferPreferencesPanel(PreferencesDialog dialog)
    {
        setBorder(BorderFactory.createTitledBorder(resources.getString("dataTransfer")));
        setLayout(grid);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        proxy65Host.setEditable(true);

        // port
        c.weightx = 0.0;
        grid.setConstraints(proxy65Label, c);
        add(proxy65Label);

        c.gridx++;
        grid.setConstraints(proxy65Host, c);
        add(proxy65Host);
        
        c.gridy++;
        grid.setConstraints(proxy65Note, c);
        add(proxy65Note);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        grid.setConstraints(preferIBB, c);
        add(preferIBB);

        c.gridy++;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        JLabel emptyLabel = new JLabel("");
        grid.setConstraints(emptyLabel, c);
        add(emptyLabel);

        loadSettings();
    }

    public TempSettings getSettings()
    {
        TempSettings mySettings = new TempSettings();
        mySettings.setProperty("proxy65Host", (String)proxy65Host.getSelectedItem());
        mySettings.setBoolean("preferIBB", preferIBB.isSelected());

        return mySettings;
    }

    private void loadSettings()
    {
        preferIBB.setSelected(Settings.getInstance().getBoolean("preferIBB"));
    }

    public static String[] getProxies()
    {
        String proxy = Settings.getInstance().getProperty("proxy65Host");
        
        ArrayList proxies = new ArrayList();
        if(proxy != null) proxies.add(proxy);
        
        for(int i = 0; i < PROXIES.length; i++)
        {
            if(proxy == null || !proxy.equals(PROXIES[i]))
            {
                proxies.add(PROXIES[i]);
            }
        }
        
        return (String[])proxies.toArray(new String[proxies.size()]);
    }
}
