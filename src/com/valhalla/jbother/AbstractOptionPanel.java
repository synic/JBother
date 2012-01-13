/*
 * Copyright (C) 2003 Adam Olsen This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 1, or
 * (at your option) any later version. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program; if not, write to the
 * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.valhalla.jbother;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel that provides a form in the following format:
 *        label:     field
 *        label:     field
 * and pads the end so that when the panel is resized, the form stays at the
 * top left of the dialog
 */
public class AbstractOptionPanel extends JPanel {
    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    /**
     * Sets up the panel and the GridBagLayout
     */
    public AbstractOptionPanel() {
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(1, 1, 1, 1);
        c.ipadx = 1;
        c.ipady = 1;
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(grid);
    }

    /**
     * Adds a component to the form and sets up the layout manager for the
     * next component
     * @param text The text for the label
     * @param component the component field (JTextField, etc)
     */
    public void addComponent(String text, JComponent component) {
        c.anchor = GridBagConstraints.WEST;
        JLabel label = new JLabel(text + ": ");

        c.weightx = 0;
        grid.setConstraints(label, c);
        add(label);
        c.gridx++;
        grid.setConstraints(component, c);
        add(component);
        c.gridy++;
        c.gridx = 0;
    }

    /**
     * Returns the GridBagLayout that this form is using
     */
    public GridBagConstraints getConstraints() {
        return c;
    }

    /**
     * Adds a component, and allows you to specify where in the grid to put it
     * @param component the component to add
     * @param gridx the column to place the component in
     * @param anchor which side of the column cell to place the component in
     */
    public void addComponent(JComponent component, int gridx, int anchor) {
        c.anchor = anchor;
        c.gridx = gridx;
        c.fill = GridBagConstraints.VERTICAL;
        grid.setConstraints(component, c);
        add(component);
        c.gridx = 0;
        c.gridy++;
    }

    /**
     * Call to add a spacer to the end of the form
     */
    public void end() {
        c.weightx = .9;
        c.weighty = .9;
        c.gridwidth = 2;
        JLabel label = new JLabel();
        grid.setConstraints(label, c);
        add(label);
    }
}
