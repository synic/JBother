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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.valhalla.gui.MJTextArea;

/**
 * Plugin Panel for the PluginManager
 * 
 * @author Adam Olsen
 * @version 1.0
 */
class PluginManagerPanel extends JPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "PluginManager", Locale.getDefault());

    private PluginManager manager;

    private PluginTableModel model = null;

    private JTable table = null;

    private JPanel buttonPanel = new JPanel();

    private JButton pluginButton = new JButton(resources
            .getString("installPlugins"));

    private MJTextArea descArea = new MJTextArea();

    /**
     * Sets up the panel
     * 
     * @param manager
     *            the PluginManager enclosing this panel
     */
    public PluginManagerPanel(PluginManager manager, boolean managePanel) {
        this.manager = manager;

        model = new PluginTableModel(this);
        if (managePanel)
            model.setManageModel();
        table = new JTable(model);
        model.setTable(table);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane);
        add(Box.createRigidArea(new Dimension(0, 5)));

        JScrollPane pane = new JScrollPane(descArea);
        add(pane);

        Dimension dim = descArea.getSize();
        dim.setSize((int) dim.getWidth(), 130);
        descArea.setPreferredSize(dim);

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(pluginButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        add(buttonPanel);
        table.addMouseListener(new MouseListener());
        descArea.setEditable(false);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        pluginButton.setEnabled(false);
    }

    public JTable getTable() {
        return table;
    }

    /**
     * Gets the button
     * 
     * @return the panel's button
     */
    public JButton getButton() {
        return pluginButton;
    }

    /**
     * Gets the button panel
     * 
     * @return this panel's button panel
     */
    public JPanel getButtonPanel() {
        return buttonPanel;
    }

    /**
     * Sets the plugin buttons text
     * 
     * @param text
     *            the plugin buttons text
     */
    public void setButtonText(String text) {
        pluginButton.setText(text);
    }

    /**
     * Listens for mouse events
     * 
     * @author Adam Olsen
     * @version 1.0
     */
    class MouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            String desc = model.getDescription(row);

            descArea.setText(desc);
            descArea.setCaretPosition(0);
        }
    }

    /**
     * Sets the list of plugins in the Table
     * 
     * @param list
     *            the list of plugins
     */
    public void setPlugins(ArrayList list) {
        model.setPlugins(list);
        table.repaint();
        table.validate();
        descArea.setText("");
    }
}