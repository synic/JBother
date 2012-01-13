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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Creates dialog for entering masked password
 *
 * @author synic
 * @created March 9, 2005
 */

public class PasswordDialog extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JPanel main;

    private JButton okButton = new JButton(resources.getString("okButton"));

    private String text = null;

    private JPasswordField field = new JPasswordField();
    private String title;

    /**
     * Constructor for the PasswordDialog object
     */
    public PasswordDialog(String title) {
        setModal(true);
        this.title = title;

        initComponents();
    }

    private void initComponents()
    {
        main = (JPanel) getContentPane();
        main.setBorder(BorderFactory.createTitledBorder(this.title));
        field.setColumns(25);

        GridBagLayout grid = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        main.setLayout(grid);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;

        JPanel fieldPanel = new JPanel();
        fieldPanel.add(field);
        grid.setConstraints(fieldPanel, c);

        main.add(fieldPanel);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        field.setFont(okButton.getFont());

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(okButton);
        buttons.add(Box.createHorizontalGlue());
        buttons.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        c.gridy++;
        grid.setConstraints(buttons, c);
        main.add(buttons);

        addListeners();
        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                okHandler();
            }
        });

        //Dimension dim = getSize();
        //setSize( 300, (int)dim.getHeight() );
        setResizable(false);

        this.setVisible(true);
    }

    public PasswordDialog(JFrame parent,String title)
    {
        super(parent,title,true);
        this.title = title;
        initComponents();
    }

    public PasswordDialog(JDialog parent,String title)
    {
        super(parent,title,true);
        this.title = title;
        initComponents();
    }

    /**
     * Adds the event listeners to the buttons
     */
    private void addListeners() {
        PEDialogListener listener = new PEDialogListener();
        okButton.addActionListener(listener);
        field.addActionListener(listener);
    }

    /**
     * Handles events
     *
     * @author synic
     * @created November 30, 2004
     */
    class PEDialogListener implements ActionListener {
        /**
         * Description of the Method
         *
         * @param e
         *            Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            okHandler();
        }
    }

    /**
     * Cancels the dialog, and quits if exitOnClose is set to true
     */
    private void okHandler() {
        this.text = new String(field.getPassword());
        dispose();
    }

    /**
     * Gets string entered by user
     *
     * @return The text value
     */
    public String getText() {
        return this.text;
    }
}

