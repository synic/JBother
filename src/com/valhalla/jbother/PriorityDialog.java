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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.MJTextField;
import com.valhalla.gui.Standard;
import com.valhalla.settings.Settings;

/**
 * Displays a dialog that allows you to change your priority
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class PriorityDialog extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JLabel label = new JLabel(resources.getString("priority") + ":    ");

    private MJTextField priorityBox = new MJTextField(4);

    private JButton okButton = new JButton(resources.getString("okButton")),
            cancelButton = new JButton(resources.getString("cancelButton"));

    private JPanel container = new JPanel();

    /**
     * Default constructor
     */
    public PriorityDialog() {
        super(BuddyList.getInstance().getContainerFrame(), "Set Priority", false);
        setTitle(resources.getString("setPriority"));

        String current = Settings.getInstance().getProperty("priority");
        if (current != null)
            priorityBox.setText(current);

        DialogTracker.addDialog(this, true, true);
        setContentPane(container);
        container.setBorder(BorderFactory.createEmptyBorder(10, 35, 10, 35));

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        JLabel setPriorityLabel = new JLabel(resources.getString("setPriority"));
        setPriorityLabel
                .setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        setPriorityLabel.setAlignmentX(Container.CENTER_ALIGNMENT);

        container.add(setPriorityLabel);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.add(label);
        labelPanel.add(priorityBox);
        container.add(labelPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        container.add(buttonPanel);

        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Adds the various event listeners to the various components
     */
    private void initComponents() {
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DialogTracker.removeDialog(PriorityDialog.this);
            }
        });

        PriorityListener listener = new PriorityListener(this);

        okButton.addActionListener(listener);
        priorityBox.addActionListener(listener);
    }

    /**
     * Listens for the OK button to be pressed and sends the presence packet.
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class PriorityListener implements ActionListener {
        private PriorityDialog dialog;

        public PriorityListener(PriorityDialog dialog) {
            this.dialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            // this try block makes sure that the user entered a valid number
            // greater than 0
            try {
                if (Integer.parseInt(priorityBox.getText()) < 1)
                    throw new NumberFormatException();

                Settings.getInstance().setProperty("priority",
                        priorityBox.getText());
                BuddyList.getInstance()
                        .setStatus(
                                BuddyList.getInstance()
                                        .getCurrentPresenceMode(),
                                BuddyList.getInstance()
                                        .getCurrentStatusString(), false);

                DialogTracker.removeDialog(dialog);

            } catch (NumberFormatException nfe) {
                Standard.warningMessage(null, resources
                        .getString("setPriority"), resources
                        .getString("specifyGreaterThanZero"));
            }
        }
    }
}