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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.*;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;

import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.MJTextField;
import com.valhalla.gui.NMOptionDialog;
import com.valhalla.gui.Standard;
import com.valhalla.gui.WaitDialog;
import com.valhalla.gui.WaitDialogListener;

/**
 * Displays a dynamic registration form A registration server is contacted and
 * responds with the required fields that it needs in order for someone to
 * register for it. This form will then dynamically display the required fields.
 * Once the fields are filled out, this class will send the information back to
 * the server.
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class RegistrationForm extends JDialog {
    protected ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    protected String server;

    protected ArrayList fieldListFields = new ArrayList();

    protected ArrayList fieldListNames = new ArrayList();

    protected WaitDialog wait;

    protected JLabel instructions = new JLabel(resources
            .getString("pleaseFillIn"));

    private String regKey = "";

    private JPanel container = new JPanel();

    private JButton okButton = new JButton(resources.getString("okButton")),
            cancelButton = new JButton(resources.getString("cancelButton"));

    private JPanel buttonPanel = new JPanel();

    private JPanel inputPanel = new JPanel();

    private Registration register = new Registration();

    //this part is for laying out the rows for the dialog
    private int row = 1;

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    /**
     * Default constructor
     *
     * @param server
     *            the server to register for
     */
    public RegistrationForm(JFrame parent,String server) {
        super(parent,"Registration", false);

        setTitle(resources.getString("registration"));
        this.server = server;

        instructions.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        instructions.setAlignmentX(Container.CENTER_ALIGNMENT);
        container.add(instructions);
        container.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 25));

        inputPanel.setLayout(grid);

        setContentPane(container);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;

        container.add(inputPanel);
        //add the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        DialogTracker.addDialog(this, true, true);

        container.add(buttonPanel);
        initializeListeners();
    }

    /**
     * Sets up the different event listeners in the RegistrationForm
     */
    private void initializeListeners() {
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeHandler();
            }
        });

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                register();
            }
        });
    }

    /**
     * Closes this dialog
     */
    public void closeHandler() {
        DialogTracker.removeDialog(this);
    }

    /**
     * Causes the registration thread to begin - sending the information in the
     * form to the server
     */
    public void register() {
        setVisible(false);

        RegisterThread thread = new RegisterThread();

        wait = new WaitDialog(this, thread, resources.getString("pleaseWait"));
        wait.setVisible(true);

        thread.start();
    }

    /**
     * Contacts the server to find out which fields are needed
     */
    public void getRegistrationInfo() {

        GetRegistrationFormThread thread = new GetRegistrationFormThread();
        wait = new WaitDialog(this, thread, resources.getString("pleaseWait"));
        wait.setVisible(true);

        thread.start();
    }

    /**
     * Capitalizes the first letter of a string
     *
     * @param text
     *            the text to capitalize
     * @return the capitalized text
     */
    private String capitalize(String text) {
        text = text.substring(0, 1).toUpperCase()
                + text.substring(1, text.length());
        return text;
    }

    /**
     * Creates a <code>Label</code> and a <code>JTextField</code> next to it
     * and places it in the registration form after the last If the label param
     * is "password", it creates a <code>JPasswordField</code>
     *
     * @param label
     *            the text to put in the label
     */
    protected void createInputBox(String label, String value) {
        JLabel labelBox = new JLabel(capitalize(label) + ":    ");

        fieldListNames.add(label);

        c.gridy = row++;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        grid.setConstraints(labelBox, c);
        inputPanel.add(labelBox);

        JTextField box = new MJTextField(15);
        if (label.equals("password")) {
            box = new JPasswordField(15);
            box.setFont(labelBox.getFont());
        }

        if (value != null)
            box.setText(value);
        fieldListFields.add(box);

        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        grid.setConstraints(box, c);
        inputPanel.add(box);
    }

    /**
     * Submits the registration information to the server
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class RegisterThread extends Thread implements WaitDialogListener {
        private String errorMessage;

        private boolean stopped = false;

        public void cancel() {
            stopped = true;
            interrupt();
        }

        /**
         * is called from the <code>Thread</code> enclosing this class
         */
        public void run() {
            if (!BuddyList.getInstance().checkConnection()) {
                BuddyList.getInstance().connectionError();
                return;
            }

            register = new Registration();
            register.setType(IQ.Type.SET);
            register.setTo(server);

            Hashtable map = new Hashtable();
            map.put("key", regKey);

            // set up the various attributes to be sent to the server
            for (int i = 0; i < fieldListNames.size(); i++) {
                String name = (String) fieldListNames.get(i);
                JTextField field = (JTextField) fieldListFields.get(i);

                map.put(name, field.getText());
            }

            // send the packet
            register.setAttributes(map);
            PacketFilter filter = new AndFilter(new PacketIDFilter(register
                    .getPacketID()), new PacketTypeFilter(IQ.class));

            PacketCollector collector = BuddyList.getInstance().getConnection()
                    .createPacketCollector(filter);
            BuddyList.getInstance().getConnection().sendPacket(register);

            // collect the response
            IQ result = (IQ) collector.nextResult(SmackConfiguration
                    .getPacketReplyTimeout());
            wait.dispose();

            if (stopped)
                return;

            if (result == null) {
                errorMessage = resources.getString("unknownError");
            } else if (result.getType() == IQ.Type.ERROR) {
                errorMessage = result.getError().getMessage();
                if (errorMessage == null)
                    errorMessage = resources.getString("unknownError");
            }

            // display the error message if there was one
            // otherwise just close
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (errorMessage != null) {
                        Standard.warningMessage(null, resources
                                .getString("registration"), errorMessage);
                    } else {
                        NMOptionDialog.createMessageDialog(null, resources
                                .getString("registration"), resources
                                .getString("registrationSuccessful"));
                    }

                    DialogTracker.removeDialog(RegistrationForm.this);
                }
            });
        }
    }

    /**
     * Contacts the registration server and finds out what fields need to be
     * sent back in order to register for the server
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class GetRegistrationFormThread extends Thread implements
            WaitDialogListener {
        private String errorMessage;

        private boolean stopped = false;

        public void cancel() {
            stopped = true;
            interrupt();
        }

        /**
         * Called from the <code>Thread</code> enclosing this class
         */
        public void run() {
            if (!BuddyList.getInstance().checkConnection()) {
                BuddyList.getInstance().connectionError();
                return;
            }

            register = new Registration();
            register.setType(IQ.Type.GET);
            register.setTo(server);
            PacketFilter filter = new AndFilter(new PacketIDFilter(register
                    .getPacketID()), new PacketTypeFilter(IQ.class));

            PacketCollector collector = BuddyList.getInstance().getConnection()
                    .createPacketCollector(filter);

            // send the request
            BuddyList.getInstance().getConnection().sendPacket(register);

            // collect the response
            IQ result = (IQ) collector.nextResult(SmackConfiguration
                    .getPacketReplyTimeout());

            if (stopped)
                return;

            if (result == null) {
                errorMessage = resources.getString("noResponse");
            } else if (result.getType() == IQ.Type.ERROR) {
                errorMessage = result.getError().getMessage();
                if (errorMessage == null)
                    errorMessage = resources.getString("unknownError");
            }

            wait.setVisible(false);

            // if there was no error, create the registration form and display
            // it
            if (errorMessage == null) {
                register = (Registration) result;

                instructions
                        .setText("<html><table width='300' border='0'><tr><td align='center'> "
                                + register.getInstructions()
                                + "</td></tr></table></html>");

                Map map = register.getAttributes();
                if (map != null) {
                    Iterator iterator = map.keySet().iterator();

                    // we iterate twice to ensure the username goes first
                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();

                        // build the registration form
                        String value = (String) map.get(key);
                        if (key.equals("username"))
                            createInputBox(key, value);
                    }

                    iterator = map.keySet().iterator();

                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();

                        // build the registration form
                        String value = (String) map.get(key);
                        if (key.equals("key"))
                            regKey = value; // this field does not need to be
                                            // displayed
                        else if (!key.equals("instructions")
                                && !key.equals("username")
                                && !key.equals("registered"))
                            createInputBox(key, value);
                    }
                }

            }


            // either display an error if there was one or
            // display the registration dialog if there wasn't one
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (errorMessage != null) {
                        Standard.warningMessage(null, resources
                                .getString("registration"), errorMessage);
                        DialogTracker.removeDialog(RegistrationForm.this);
                    } else {
                        pack();

                        setLocationRelativeTo(null);
                        setVisible(true);
                    }
                }
            });
        }
    }
}