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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * Represents a list of plugins in a table
 * 
 * @author Adam Olsen
 * @version 1.0
 */
class PluginTableModel extends AbstractTableModel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "PluginManager", Locale.getDefault());

    private PluginManagerPanel panel;

    private String[] columns = new String[] { " ", resources.getString("Name"),
            resources.getString("Version"), resources.getString("Size") };

    private JTable table;

    private ArrayList list = new ArrayList();

    private boolean managePanel = false;

    /**
     * Sets up the list model
     * 
     * @param panel
     *            the PluginManagerListPanel that encloses this model's table
     */
    PluginTableModel(PluginManagerPanel panel) {
        this.panel = panel;
    }

    /**
     * Sets up the list model as a manage panel
     */
    public void setManageModel() {
        columns = new String[] { resources.getString("Name"),
                resources.getString("Version"), resources.getString("Status") };
        managePanel = true;
    }

    /**
     * Finds out whether or not a cell is editable
     * 
     * @param row
     *            the row to check
     * @param column
     *            the column to check
     * @return true if the cell is editable
     */
    public boolean isCellEditable(int row, int column) {
        if (column == 0 && !managePanel)
            return true;
        else
            return false;
    }

    /**
     * Fired when the tables data is changed
     * 
     * @param value
     *            the new value of the data
     * @param row
     *            the row of the item
     * @param col
     *            the column of the item
     */
    public void setValueAt(Object value, int row, int col) {
        boolean b = ((Boolean) value).booleanValue();

        Properties props = (Properties) list.get(row);
        if (b)
            props.setProperty("selected", "true");
        else
            props.remove("selected");

        JButton button = panel.getButton();
        boolean selected = false;
        for (int i = 0; i < list.size(); i++) {
            Properties p = (Properties) list.get(i);
            if (p.getProperty("selected") != null)
                selected = true;
        }
        button.setEnabled(selected);

        fireTableCellUpdated(row, col);
    }

    /**
     * Gets a description for a plugin
     * 
     * @param index
     *            the index of plugin to get the description of
     * @return a description of the plugin
     */
    public String getDescription(int index) {
        Properties props = (Properties) list.get(index);

        String author = props.getProperty("author");
        if (author == null)
            author = "Unknown";
        String releaseDate = props.getProperty("releaseDate");
        if (releaseDate == null)
            releaseDate = "Unknown";

        String desc = "Author: " + author + "\n";
        desc += "Release Date: " + releaseDate + "\n";
        desc += props.getProperty("description");

        return desc;
    }

    /**
     * Sets the list of plugins in the Table
     * 
     * @param list
     *            the list of plugins
     */
    public void setPlugins(ArrayList list) {
        this.list = list;
        table.tableChanged(new TableModelEvent(this));
    }

    /**
     * Sets the table value for this model
     * 
     * @param table
     *            the table that this model represents
     */
    public void setTable(JTable table) {
        this.table = table;

        TableColumn column = null;

        int versionWidth = 270;

        // set the default column widths
        for (int i = 0; i < getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                if (!managePanel)
                    column.setPreferredWidth(1);
                else {
                    column.setPreferredWidth(310);
                    versionWidth = 65;
                }
            } else if (i == 1)
                column.setPreferredWidth(versionWidth);
            else
                column.setPreferredWidth(65);
        }

        table.validate();
    }

    /**
     * Returns the column name for a specified index
     * 
     * @param index
     *            the index you want
     * @return the column name
     */
    public String getColumnName(int index) {
        return columns[index];
    }

    /**
     * Gets the column class
     * 
     * @param index
     *            the column index to return
     * @return the Class that represents the column
     */
    public Class getColumnClass(int index) {
        if (managePanel)
            index++;
        if (index == 0)
            return Boolean.class;
        else if (index == 1)
            return Object.class;
        else
            return Number.class;
    }

    /**
     * @return the number of columns in the table
     */
    public int getColumnCount() {
        return columns.length;
    }

    /**
     * @return the number of rows in the table
     */
    public int getRowCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    /**
     * @param props
     *            the properties of the plugin to get
     * @return the status of the plugin specified in props
     */
    private String getLoaded(Properties props) {
        PluginLoader loader = PluginLoader.getInstance();
        PluginJAR jar = loader.getPlugin(props.getProperty("name"));

        if (jar == null || !jar.getLoaded())
            return "Not Loaded";
        else
            return "Loaded";
    }

    /**
     * Get the Object for a specific coordinate in the table
     * 
     * @param row
     *            the row of the item
     * @param column
     *            the column of the item
     * @return the Object at the specified coordinates
     */
    public Object getValueAt(int row, int column) {
        Properties props = (Properties) list.get(row);

        boolean selected = false;
        if (props.getProperty("selected") != null)
            selected = true;
        if (managePanel)
            column++;

        if (column == 0)
            return new Boolean(selected);
        else if (column == 1)
            return props.getProperty("name");
        else if (column == 2)
            return props.getProperty("version");
        else if (column == 3) {
            if (managePanel)
                return getLoaded(props);

            String num = "k";

            try {
                DecimalFormat nf = new DecimalFormat(
                        "###,###,###,##0.0;(#,##0.0)");

                double i = Double.parseDouble(props.getProperty("size"));
                i = i / 1024.0;

                num = nf.format(i) + num;
            } catch (NumberFormatException e) {
            }

            return num;
        }

        return new String("");
    }
}