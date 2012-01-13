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
package com.valhalla.jbother;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import com.valhalla.jbother.jabber.smack.*;
import java.util.*;

import com.valhalla.gui.*;

/**
 *  as Dialogs for sending and receiving files will be very similar, this is the
 *  basic window with 3 areas: To/From field, Date field, Filename field,
 *  Description field, size field and two buttons: Reject/Cancel & Accept/Send
 *
 *@author     Lukasz Wiechec
 *@created    Feb 2, 2005 4:27:37 PM
 */

public abstract class FileDialog extends JFrame implements ActionListener {
    protected ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());
    /**
     * The button that corresponds with accept or send, depending on the
     * transfer type
     */
    protected JButton ayeButton = new JButton();
    /**
     * The button that corresponds with deny or cancel, depending on the
     * transfer type
     */
    protected JButton nayButton = new JButton();
    
    /**
     * The from or to label
     */
    protected JLabel fromToLabel = new JLabel();
    /**
     * The from or to field
     */
    protected MJTextField fromToTF = new MJTextField();
    /**
     * The description of the file
     */
    protected MJTextArea descriptionArea = new MJTextArea();

    /**
     * The file being transferred
     */
    protected MJTextField fileTF = new MJTextField();

    /**
     * The status of the file
     */
    protected JLabel statusLabel = new JLabel("");

    /**
     * A file chooser to pick the file to send or save to
     */
    protected static JFileChooser fileChooser;

    protected JFrame parent;

    protected GridBagLayout grid = new GridBagLayout();
    protected GridBagConstraints c = new GridBagConstraints();
    protected JPanel main;
    protected JPanel topPanel = new JPanel(grid);

    /**
     * Constructs a file dialog
     */
    public FileDialog()
    {
        setIconImage(Standard.getImage("frameicon.png"));

        parent = BuddyList.getInstance().getContainerFrame();

        initialize();
    }


    /**
     *  initialize the dialog
     */
    private void initialize() {
        getRootPane().setDefaultButton(ayeButton);
        // set up the layout
        main = (JPanel)getContentPane();
        main.setLayout(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        main.add(topPanel, BorderLayout.NORTH);
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 0);

        addComponent(fromToLabel, fromToTF);
        addComponent(new JLabel("File: "), fileTF);

        main.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(nayButton);
        buttonPanel.add(ayeButton);

        main.add(buttonPanel, BorderLayout.SOUTH);

        // set the buttons
        ayeButton.setActionCommand("aye");
        ayeButton.addActionListener(this);
        nayButton.setActionCommand("nay");
        nayButton.addActionListener(this);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // add to DialogTracker so that when the connection is lost, it will be closed
        DialogTracker.addDialog(this, true, true);

        setSize(450, 300);
        setFocusable(true);
        Standard.cascadePlacement(this);

    }

    /**
     * Adds a field to the dialog form, and sets up the gridbag for the next
     * element
     * @param label the label corresponding to this field
     * @param field the text field 
     */ 
    protected void addComponent(JLabel label, JTextField field)
    {
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        c.weightx = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        grid.setConstraints(label, c);
        topPanel.add(label);
        c.weightx = 1;
        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        grid.setConstraints(field, c);
        topPanel.add(field);
        c.gridy++;
    }

    /**
     * Called when the users clicks the 'aye' or 'nay' button
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getActionCommand().equals("aye")) {
            doAye();
        } else if (e.getActionCommand().equals("nay")) {
            doNay();
        }
    }


    /**
     *  Disables all the form fields in this dialog (for fields that cannot be
     *  changed
     */
    protected void disableAll() {
        ayeButton.setEnabled(false);
        fromToTF.setEditable(false);
        descriptionArea.setEditable(false);
        fileTF.setEditable(false);
    }

    /**
     *  method called when "Aye" button is pressed to be implemented in child
     *  class
     */
    protected abstract void doAye();


    /**
     *  method called when "Nay" button is pressed to be implemented in child
     *  class
     */
    protected abstract void doNay();
}
