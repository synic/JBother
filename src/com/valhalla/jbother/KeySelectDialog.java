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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.valhalla.gui.Standard;
import com.valhalla.misc.GnuPG;

/**
 * Allows a user to edit a profile
 *
 * @author Andrey Zakirov
 * @created February, 2004
 */
public class KeySelectDialog extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JPanel main;

    private JButton okButton = new JButton(resources.getString("okButton"));

    private JButton cancelButton = new JButton(resources
            .getString("cancelButton"));

    private String name = null;

    private String id = null;

    private String type;

    private String[] entries;

    private ArrayList entries2 = new ArrayList();

    private Hashtable keys = new Hashtable();

    private JList sampleJList = new JList();

    /**
     * Contructs the ProfileEditorDialog
     *
     * @param dialog
     *            the ProfileManager dialog that's calling this editor, or <tt>
     *      null</tt>
     *            if nothing is calling it
     * @param type
     *            Description of the Parameter
     */
    public KeySelectDialog(JDialog dialog, String type) {
        super(dialog, "", true);
        this.type = type;
    }

    /**
     * Constructor for the KeySelectDialog object
     *
     * @param type
     *            Description of the Parameter
     */
    public KeySelectDialog(String type) {
        super((JFrame) null, "", true);
        this.type = type;
    }

    /**
     * Description of the Method
     */
    public void showDialog() {
        setTitle(resources.getString("gnupgKeySelector"));
        GnuPG gnupg = new GnuPG();
        boolean res = false;
        if (type.equals("sec")) {
            res = gnupg.listSecretKeys("");
        } else {
            res = gnupg.listKeys("");
        }

        if (!res) {
            Standard.warningMessage(null, "Error running GnuPG",
                    "Error running GnuPG: " + gnupg.getErrorString());
            return;
        }

        entries = gnupg.getResult().split("\n");
        for (int i = 0; i < entries.length; i++) {
            id = entries[i]
                    .replaceAll(
                            "^"
                                    + type
                                    + ":[^:]*:[^:]*:[^:]*:([^:]*):[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*$",
                            "$1");
            name = entries[i]
                    .replaceAll(
                            "^"
                                    + type
                                    + ":[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:[^:]*:([^:]*):[^:]*:[^:]*:[^:]*$",
                            "$1");
            if ((!name.equals(entries[i])) && (!id.equals(entries[i]))) {
                entries2.add(name);
                keys.put(name, id);
            }
        }
        name = null;
        id = null;
        sampleJList.setListData(entries2.toArray());
        sampleJList.setSelectedIndex(0);
        sampleJList.setVisibleRowCount(4);
        sampleJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sampleJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    okHandler();
                }
            }
        });

        JScrollPane listPane = new JScrollPane(sampleJList);
        main = (JPanel) getContentPane();

        JPanel listPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        listPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.add(Box.createHorizontalGlue());
        listPanel.add(listPane);
        listPanel.add(buttonPanel);
        main.add(listPanel);
        addListeners();
        pack();
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                okHandler();
            }
        });
        this.setVisible(true);

    }

    /**
     * Adds the event listeners to the buttons
     */
    private void addListeners() {
        PEDialogListener listener = new PEDialogListener();
        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
    }

    /**
     * Handles events
     *
     * h *@author synic
     *
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
            if (e.getSource() == cancelButton) {
                cancelHandler();
            } else {
                okHandler();
            }

        }
    }

    /**
     * Cancels the dialog, and quits if exitOnClose is set to true
     */
    private void okHandler() {
        if (sampleJList.getSelectedValue() != null) {
            this.name = sampleJList.getSelectedValue().toString();
            this.id = keys.get(sampleJList.getSelectedValue()).toString();
        }
        dispose();
    }

    /**
     * Description of the Method
     */
    private void cancelHandler() {
        dispose();
    }

    /**
     * Gets the name attribute of the KeySelectDialog object
     *
     * @return The name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the iD attribute of the KeySelectDialog object
     *
     * @return The iD value
     */
    public String getID() {
        return this.id;
    }

}

