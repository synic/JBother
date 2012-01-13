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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.packet.*;
import org.jivesoftware.smackx.packet.*;
import com.valhalla.jbother.jabber.smack.*;

import com.valhalla.gui.*;


import com.valhalla.jbother.jabber.smack.LastActivity;

/**
 * A dialog that collects and shows information about a Jabber user Shows a
 * dialog with several fields and starts an information collecting field for
 * each one. As each piece of information is found it fills out the fields
 *
 * @author Adam Olsen
 * @author Andrey Zakirov
 * @version 1.0
 */
public class InformationViewerDialog extends JDialog implements WaitDialogListener {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JButton okButton = new JButton(resources.getString("okButton"));
    private JButton retrieve = new JButton(resources.getString("retrieve"));
    private JButton save = new JButton(resources.getString("saveButton"));
    private String user;
    private JPanel mainPanel;
    private JTabbedPane pane = new JTabbedPane();
    private Vector fields = new Vector();
    private GridBagConstraints c = new GridBagConstraints();
    private MJTextField name = new MJTextField();
    private MJTextField last = new MJTextField();
    private MJTextField birthday = new MJTextField();
    private MJTextField nickname = new MJTextField();
    private MJTextField email = new MJTextField();
    private MJTextField homepage = new MJTextField();
    private MJTextField phone = new MJTextField();

    // location
    private MJTextField street1 = new MJTextField();
    private MJTextField street2 = new MJTextField();
    private MJTextField city = new MJTextField();
    private MJTextField state = new MJTextField();
    private MJTextField zip = new MJTextField();
    private MJTextField country = new MJTextField();

    // work
    private MJTextField company = new MJTextField();
    private MJTextField department = new MJTextField();
    private MJTextField position = new MJTextField();
    private MJTextField role = new MJTextField();
    private MJTextArea about = new MJTextArea();
    private WaitDialog wait = new WaitDialog(this, this, resources
            .getString("pleaseWait"));
    private MJTextField clientField = new MJTextField();
    private MJTextField timeField = new MJTextField();
    private MJTextField lastField = new MJTextField();
    private boolean personal;
    protected boolean cancelled = false;

    /**
     * Default constructor
     *
     * @param user
     *            the user you want to collect information about
     */
    public InformationViewerDialog(String user, boolean personal) {
        super(BuddyList.getInstance().getContainerFrame());
        this.personal = personal;
        this.user = user;
        if (!personal)
            setTitle(resources.getString("information") + " " + user);
        else
            setTitle(resources.getString("editInformation"));

        if (!BuddyList.getInstance().checkConnection()) {
            BuddyList.getInstance().connectionError();
            return;
        }

        about.setWrapStyleWord(true);
        mainPanel = (JPanel) getContentPane();

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(pane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(retrieve);
        if (personal)
            buttonPanel.add(save);
        buttonPanel.add(okButton);
        //buttonPanel.add( Box.createHorizontalGlue() );
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        addGeneralItems();
        addLocationItems();
        addWorkItems();
        addAboutItems();

        if (!personal) {
            addClientItems();
        }

        pack();
        Dimension dim = getSize();
        setSize(new Dimension(450, (int) dim.getHeight()));

        setLocationRelativeTo(null);
        DialogTracker.addDialog(this, false, true);
        collectInformation();

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DialogTracker.removeDialog(InformationViewerDialog.this);
            }
        });

        retrieve.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                collectInformation();
            }
        });

        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                wait.setVisible(true);
                cancelled = false;
                disableAll();
                new Thread(new SaveVCard()).start();
            }
        });
    }

    public void cancel()
    {
        cancelled = true;
    }

    private void collectInformation() {
        wait.setVisible(true);
        disableAll();
        new Thread(new VCardCollector()).start();

        if (!personal) {
            new Thread(new VersionCollector(clientField, this)).start();
            new Thread(new TimeCollector(timeField, this)).start();
            new Thread(new LastCollector(lastField, this)).start();
        }
    }

    public InformationViewerDialog(String user) {
        this(user, false);
    }

    /**
     * Saves a the VCard
     */
    private class SaveVCard implements Runnable {
        public void run() {
            if( cancelled ) return;
            VCard card = new VCard();
            card.setFirstName(name.getText());
            card.setLastName(last.getText());
            card.setNickName(nickname.getText());

            card.setEmailHome(email.getText());
            card.setPhoneHome("VOICE", phone.getText());
            card.setField("BDAY", birthday.getText());

            card.setAddressFieldHome("STREET", street1.getText());
            card.setAddressFieldHome("LOCALITY", city.getText());
            card.setAddressFieldHome("REGION", state.getText());
            card.setAddressFieldHome("PCODE", zip.getText());
            card.setAddressFieldHome("CTRY", country.getText());

            card.setOrganization(company.getText());
            card.setOrganizationUnit(department.getText());
            card.setField("TITLE", position.getText());
            card.setField("ROLE", role.getText());
            card.setField("DESC", about.getText());

            try {
                card.save(BuddyList.getInstance().getConnection());
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            catch( XMPPException ex ) { }

            if( cancelled ) return;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    wait.setVisible(false);
                    enableAll();
                }
            });
        }
    }

    /**
     * Collects a VCard for a user and fills in the InformationViewer form
     * fields
     */
    private class VCardCollector implements Runnable {
        /**
         * Cancels this thread
         */
        public void cancel()
        {
            cancelled = true;
        }

        /**
         * Called by Thread.start()
         */
        public void run() {
            VCard temp = new VCard();
            if( cancelled ) return;

            try {
                if (!personal) {
                    temp.load(BuddyList.getInstance().getConnection(),
                            user);
                } else {
                    temp.load(BuddyList.getInstance().getConnection());
                }
            } catch (NullPointerException npe) {
            } catch (XMPPException ex) {
            }

            final VCard card = temp;
            if( cancelled ) return;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (card != null) {
                        name.setText(card.getFirstName());
                        last.setText(card.getLastName());
                        nickname.setText(card.getNickName());
                        birthday.setText(card.getField("BDAY"));
                        email.setText(card.getEmailHome());
                        phone.setText(card.getPhoneHome("VOICE"));

                        street1.setText(card.getAddressFieldHome("STREET"));
                        city.setText(card.getAddressFieldHome("LOCALITY"));
                        state.setText(card.getAddressFieldHome("REGION"));
                        zip.setText(card.getAddressFieldHome("PCODE"));
                        country.setText(card.getAddressFieldHome("CTRY"));

                        company.setText(card.getOrganization());
                        department.setText(card.getOrganizationUnit());
                        position.setText(card.getField("TITLE"));
                        role.setText(card.getField("ROLE"));
                        about.setText(card.getField("DESC"));
                        about.setCaretPosition(0);
                    }

                    if (personal)
                        enableAll();
                    wait.setVisible(false);
                    validate();
                    setVisible(true);
                }
            });
        }
    }

    /**
     * Creates the "About" tab
     */
    private void addAboutItems() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(new JScrollPane(about), BorderLayout.CENTER);
        fields.add(about);

        pane.add(panel, resources.getString("aboutUser"));
    }

    /**
     * Creates the "work" tab
     */
    private void addWorkItems() {
        GridBagLayout grid = new GridBagLayout();
        JPanel work = createNewPanel(grid);

        addItem(work, resources.getString("company"), company, grid);
        addItem(work, resources.getString("department"), department, grid);
        addItem(work, resources.getString("position"), position, grid);
        addItem(work, resources.getString("role"), role, grid);

        pane.add(work, resources.getString("work"));
        padEnd(work, grid);
    }

    /**
     * Creates the "general" tab
     */
    private void addGeneralItems() {
        GridBagLayout grid = new GridBagLayout();
        JPanel general = createNewPanel(grid);

        addItem(general, resources.getString("name"), name, grid);
        addItem(general, resources.getString("last"), last, grid);
        addItem(general, resources.getString("nickname"), nickname, grid);
        addItem(general, resources.getString("birthday"), birthday, grid);
        addItem(general, resources.getString("email"), email, grid);
        addItem(general, resources.getString("phone"), phone, grid);

        pane.add(general, resources.getString("general"));
        padEnd(general, grid);
    }

    /**
     * Adds the "location" tab items
     */
    private void addLocationItems() {
        GridBagLayout grid = new GridBagLayout();
        JPanel location = createNewPanel(grid);

        addItem(location, resources.getString("street"), street1, grid);
        addItem(location, resources.getString("city"), city, grid);
        addItem(location, resources.getString("state"), state, grid);
        addItem(location, resources.getString("zip"), zip, grid);
        addItem(location, resources.getString("country"), country, grid);

        pane.add(location, resources.getString("location"));
        padEnd(location, grid);
    }

    /**
     * Adds the different fields to the displayed form
     */
    private void addClientItems() {
        GridBagLayout grid = new GridBagLayout();
        JPanel client = createNewPanel(grid);

        /* gets jabber:iq:version information */
        addItem(client, resources.getString("clientInformation"), clientField,
                grid);
        addItem(client, resources.getString("timeInformation"), timeField, grid);
        addItem(client, "Idle for", lastField, grid);
        pane.add(client, resources.getString("misc"));
        padEnd(client, grid);
    }

    /**
     * Creates a new panel suitable for the Information Viewer
     * @param grid  The gridbag to use
     * @return a new JPanel with appropriate constraints
     */
    private JPanel createNewPanel(GridBagLayout grid) {
        JPanel panel = new JPanel(grid);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        return panel;
    }

    /**
     * Disables all the fields in the dialog
     * Appropriate for when you are viewing another users 
     * information (that you can't change)
     */
    private void disableAll() {
        for (int i = 0; i < fields.size(); i++) {
            JTextComponent field = (JTextComponent) fields.get(i);
            field.setEditable(false);
        }
        validate();
    }

    /**
     * Enables all the fields in the dialog
     * Used for when you are changing your own information.
     */
    private void enableAll() {
        for (int i = 0; i < fields.size(); i++) {
            JTextComponent field = (JTextComponent) fields.get(i);
            field.setEditable(true);
        }
        validate();
    }

    /**
     * Adds a blank panel that takes all excess space on the panel
     * @param panel The panel to add this spacer to
     * @param grid The grid that's associated with this panel
     */
    private void padEnd(JPanel panel, GridBagLayout grid) {
        JLabel blank = new JLabel("");
        c.gridwidth = 2;
        c.weighty = 1;
        grid.setConstraints(blank, c);
        panel.add(blank);
    }

    /**
     * Adds a field and starts the thread that will collect it's information
     *
     * @param key the name of the field
     * @param informationThread the thread that will collect the information
     */
    private void addItem(JPanel panel, String l, MJTextField field,
            GridBagLayout grid) {
        c.fill = GridBagConstraints.HORIZONTAL;

        if (!l.equals(""))
            l = l + ": ";
        JLabel label = new JLabel(l);
        label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));
        c.ipadx = 2;
        c.ipady = 2;
        c.weightx = .1;
        c.gridx = 0;
        grid.setConstraints(label, c);
        panel.add(label);
        c.gridx++;
        c.weightx = 1;
        grid.setConstraints(field, c);
        panel.add(field);
        fields.add(field);
        c.gridy++;
    }

    public String getUser() {
        return user;
    }
}

/**
 * An <code>InformationCollector</code> that collects jabbber:iq:version
 * information
 *
 * @author Adam Olsen
 * @version 1.0
 */

class VersionCollector implements Runnable {
    InformationViewerDialog dialog;

    JTextField field;

    /**
     * The default constructor
     *
     * @param dialog
     *            the InformationViewerDialog that called this thread
     * @param id
     *            the field id
     */
    public VersionCollector(MJTextField field, InformationViewerDialog dialog) {
        this.field = field;
        this.dialog = dialog;
    }

    /**
     * Called by the enclosing <code>java.util.Thread</code> The run() method
     * is responsible for collecting the requested information and setting the
     * field in the InformationViewerDialog
     */
    public void run() {
        XMPPConnection con = BuddyList.getInstance().getConnection();
        if( dialog.cancelled ) return;

        Version request = new Version();
        request.setType(IQ.Type.GET);
        request.setTo(dialog.getUser());

        // Create a packet collector to listen for a response.
        PacketCollector collector = con
                .createPacketCollector(new PacketIDFilter(request.getPacketID()));

        con.sendPacket(request);

        // Wait up to 5 seconds for a result.
        IQ result = (IQ) collector.nextResult(SmackConfiguration
                .getPacketReplyTimeout());
        if( dialog.cancelled ) return;

        if (result != null && result.getType() == IQ.Type.RESULT) {
            Version v = (Version) result;

            field.setText(v.getName() + " " + v.getVersion() + " / "
                    + v.getOs());
        } else
            field.setText("N/A");
        field.validate();
    }
}

/**
 * @author Adam Olsen
 * @version 1.0
 */

class TimeCollector implements Runnable {
    InformationViewerDialog dialog;

    JTextField field;

    /**
     * The default constructor
     *
     * @param dialog
     *            the InformationViewerDialog that called this thread
     * @param id
     *            the field id
     */
    public TimeCollector(MJTextField field, InformationViewerDialog dialog) {
        this.field = field;
        this.dialog = dialog;
    }

    /**
     * Called by the enclosing <code>java.util.Thread</code> The run() method
     * is responsible for collecting the requested information and setting the
     * field in the InformationViewerDialog
     */
    public void run() {
        if( dialog.cancelled ) return;

        XMPPConnection con = BuddyList.getInstance().getConnection();

        Time request = new Time();
        request.setType(IQ.Type.GET);
        request.setTo(dialog.getUser());

        // Create a packet collector to listen for a response.
        PacketCollector collector = con
                .createPacketCollector(new PacketIDFilter(request.getPacketID()));

        con.sendPacket(request);


        // Wait up to 5 seconds for a result.
        IQ result = (IQ) collector.nextResult(SmackConfiguration
                .getPacketReplyTimeout());
        collector.cancel();
        if( dialog.cancelled ) return;

        if (result != null && result.getType() == IQ.Type.RESULT) {
            Time t = (Time) result;

            field.setText(t.getDisplay());
        } else
            field.setText("N/A");
        field.validate();
    }
}

/**
 * Collects information in order to show the idle time of a user
 */
class LastCollector implements Runnable {
    InformationViewerDialog dialog;

    JTextField field;

    /**
     * The default constructor
     *
     * @param dialog
     *            the InformationViewerDialog that called this thread
     * @param id
     *            the field id
     */
    public LastCollector(MJTextField field, InformationViewerDialog dialog) {
        this.field = field;
        this.dialog = dialog;
    }

    /**
     * Called by the enclosing <code>java.util.Thread</code> The run() method
     * is responsible for collecting the requested information and setting the
     * field in the InformationViewerDialog
     */
    public void run() {
        if( dialog.cancelled ) return;

        XMPPConnection con = BuddyList.getInstance().getConnection();

        LastActivity request = new LastActivity();
        request.setType(IQ.Type.GET);
        request.setTo(dialog.getUser());

        // Create a packet collector to listen for a response.
        PacketCollector collector = con
                .createPacketCollector(new PacketIDFilter(request.getPacketID()));

        con.sendPacket(request);

        // Wait up to 5 seconds for a result.
        IQ result = (IQ) collector.nextResult(SmackConfiguration
                .getPacketReplyTimeout());
        if( dialog.cancelled ) return;

        if (result != null && result.getType() == IQ.Type.RESULT) {
            LastActivity t = (LastActivity) result;

            field.setText(t.showTime());
        } else
            field.setText("N/A");
        field.validate();
    }
}
