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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.XMPPException;

import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.Standard;
import com.valhalla.gui.WaitDialog;
import com.valhalla.misc.SimpleXOR;
import com.valhalla.settings.Settings;

/**
 * Displays a Dialog allowing the user to change his passowrd on the Jabber
 * server
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class ChangePasswordDialog extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JPanel mainPanel;

    private JButton okButton = new JButton(resources.getString("okButton"));

    private JButton cancelButton = new JButton(resources
            .getString("cancelButton"));

    private JPasswordField passwordField = new JPasswordField(16);

    private JPasswordField verifyPasswordField = new JPasswordField(16);

    private WaitDialog wait = new WaitDialog(this, null, resources
            .getString("pleaseWait"));

    /**
     * Sets up the dialog
     */
    public ChangePasswordDialog() {
        super(BuddyList.getInstance().getContainerFrame());
        setTitle(resources.getString("changePassword"));
        initComponents();
        DialogTracker.addDialog(this, true, true);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Sets up the various visual components
     */
    private void initComponents() {
        mainPanel = (JPanel) getContentPane();

        mainPanel.setBorder(BorderFactory.createTitledBorder(resources
                .getString("changePassword")));
        mainPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel innerPanel = new JPanel();
        GridBagLayout grid = new GridBagLayout();
        innerPanel.setLayout(grid);
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;

        JLabel pLabel = new JLabel(resources.getString("newPassword") + ": ");
        grid.setConstraints(pLabel, c);
        innerPanel.add(pLabel);
        c.gridx++;
        grid.setConstraints(passwordField, c);
        innerPanel.add(passwordField);

        JLabel vLabel = new JLabel(resources.getString("verifyPassword") + ": ");
        c.gridy++;
        c.gridx = 0;
        grid.setConstraints(vLabel, c);
        innerPanel.add(vLabel);
        c.gridx++;
        grid.setConstraints(verifyPasswordField, c);
        innerPanel.add(verifyPasswordField);

        // we have to set the JPasswordField fonts manually for some reason
        passwordField.setFont(okButton.getFont());
        verifyPasswordField.setFont(okButton.getFont());

        passwordField.grabFocus();

        mainPanel.add(innerPanel, BorderLayout.CENTER);
        addListeners();
    }

    /**
     * Adds listeners to the dialogs buttons
     */
    private void addListeners() {
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DialogTracker.removeDialog(ChangePasswordDialog.this);
            }
        });

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okHandler();
            }
        });

    }

    /**
     * Checks the information and runs the PasswordChangeThread
     */
    private void okHandler() {
        String pass = new String(passwordField.getPassword());
        String verify = new String(verifyPasswordField.getPassword());

        if (pass.equals("")) {
            Standard.warningMessage(this,
                    resources.getString("changePassword"), resources
                            .getString("passwordRequired"));
            return;
        }

        if (!verify.equals(pass)) {
            Standard.warningMessage(this,
                    resources.getString("changePassword"), resources
                            .getString("verificationMatch"));
            return;

        }

        wait.setVisible(true);
        setVisible(false);
        Thread thread = new Thread(new PasswordChangeThread(pass));
        thread.start();
    }

    /**
     * Sends the new password to the server and gets the response
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class PasswordChangeThread implements Runnable {
        private String newPass;

        public PasswordChangeThread(String p) {
            newPass = p;
        }

        public void run() {
            String errorMessage = null;

            if (BuddyList.getInstance().checkConnection()) {
                AccountManager manager = BuddyList.getInstance()
                        .getConnection().getAccountManager();
                try {
                    manager.changePassword(newPass);
                } catch (XMPPException e) {
                    if (e.getXMPPError() == null)
                        errorMessage = e.getMessage();
                    else
                        errorMessage = resources.getString("xmppError"
                                + e.getXMPPError().getCode());
                }
            } else
                errorMessage = resources.getString("notConnected");

            wait.dispose();

            if (errorMessage == null) {
                if (Settings.getInstance().getProperty("password") != null)
                    Settings.getInstance().setProperty("password",
                            SimpleXOR.encrypt(newPass, "JBother rules!"));
                Standard.noticeMessage(ChangePasswordDialog.this, resources
                        .getString("changePassword"), resources
                        .getString("passwordChanged"));
            } else {
                Standard.warningMessage(ChangePasswordDialog.this, resources
                        .getString("changePassword"), errorMessage);
            }

            DialogTracker.removeDialog(ChangePasswordDialog.this);
        }
    }
}