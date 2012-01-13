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

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;

import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.MJTextField;
import com.valhalla.gui.NMOptionDialog;
import com.valhalla.gui.Standard;
import com.valhalla.gui.WaitDialog;
import com.valhalla.jbother.jabber.BuddyStatus;

/**
 *  Displays a dialog allowing to you add or modify a buddy. It displays their
 *  JID, Alias, and group
 *
 *@author     Adam Olsen
 *@created    September 26, 2005
 *@version    1.0
 */
public class AddBuddyDialog extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JComboBox buddyGroups;

    private MJTextField buddyIDBox = new MJTextField(20);

    private MJTextField buddyAliasBox = new MJTextField(20);

    private MJTextField newGroupBox;

    private JPanel container = new JPanel();

    private RosterEntry entry;

    private String currentGroup = "";

    //the buttons
    private JButton okButton = new JButton(resources.getString("okButton"));

    private JButton cancelButton = new JButton(resources.getString("cancelButton"));

    //this part is for laying out the rows for the dialog
    private int row = 0;

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    private boolean modify = false;


    /**
     *  The add buddy constructor
     */
    public AddBuddyDialog() {
        super(BuddyList.getInstance().getContainerFrame(), "Add/Modify Buddy", false);
        setTitle(resources.getString("addBuddyDialogTitle"));

        this.initComponents();
        container.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 25));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JLabel newBuddyLabel = new JLabel(resources.getString("addBuddyDialogTitle"));
        newBuddyLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        newBuddyLabel.setAlignmentX(Container.CENTER_ALIGNMENT);
        container.add(newBuddyLabel);

        JPanel newBuddyPanel = new JPanel();
        newBuddyPanel.setLayout(grid);

        createInputBox(newBuddyPanel, grid, resources.getString("buddyId")
                 + ":", buddyIDBox);
        createInputBox(newBuddyPanel, grid, resources.getString("alias") + ":",
                buddyAliasBox);
        createInputBox(newBuddyPanel, grid, resources.getString("buddyGroup")
                 + ":", buddyGroups);
        createInputBox(newBuddyPanel, grid, resources.getString("newGroup")
                 + ":", newGroupBox);

        container.add(newBuddyPanel);

        //add the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        container.add(buttonPanel);

        DialogTracker.addDialog(this, true, true);
        setResizable(false);

        pack();
        setLocationRelativeTo(null);

    }


    /**
     *  Destroys the dialog
     */
    public void delete() {
        DialogTracker.removeDialog(this);
    }


    /**
     *  The modify buddy constructor
     *
     *@param  entry  the entry to modify
     */
    public void setBuddy(RosterEntry entry) {
        this.entry = entry;
        this.buddyIDBox.setText(entry.getUser());
        this.buddyIDBox.setEnabled(false);
        this.modify = true;
        this.buddyAliasBox.setText(entry.getName());

        String currentGroup = "";

        Iterator iterator = entry.getGroups();
        while (iterator.hasNext()) {
            RosterGroup group = (RosterGroup) iterator.next();
            currentGroup = group.getName();
        }

        if (!currentGroup.equals("")) {
            this.currentGroup = currentGroup;
        }

        //buddyGroups = new JComboBox( getRosterGroups() );
        buddyGroups.setModel(new DefaultComboBoxModel(getRosterGroups()));

        validate();
    }


    /**
     *  Called by the cancel button - destroys the dialog
     */
    private void cancelButtonHandler() {
        DialogTracker.removeDialog(this);
    }


    /**
     *  Sets the JID in the dialog
     *
     *@param  id  the JID
     */
    public void setBuddyId(String id) {
        buddyIDBox.setText(id);
        validate();
    }


    /**
     *  Sets up visual components
     */
    private void initComponents() {
        setContentPane(container);
        buddyGroups = new JComboBox(getRosterGroups());
        newGroupBox = new MJTextField(15);
        newGroupBox.setText(resources.getString("newGroup"));
        newGroupBox.setEnabled(false);

        //add the handlers
        cancelButton.addActionListener(new ActionHandler());
        okButton.addActionListener(new ActionHandler());
        newGroupBox.addActionListener(new ActionHandler());
        buddyAliasBox.addActionListener(new ActionHandler());
        buddyGroups.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    String item = (String) e.getItem();
                    if (item.equals(resources.getString("newGroup"))) {
                        newGroupBox.setEnabled(true);
                        newGroupBox.setText("");
                        newGroupBox.grabFocus();
                    } else {
                        newGroupBox.setEnabled(false);
                        if (newGroupBox.getText().equals("")) {
                            newGroupBox.setText(resources.getString("newGroup"));
                        }
                    }
                }
            });

        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    cancelButtonHandler();
                }
            });
    }


    /**
     *  assures that all required information has been filled out in the dialog
     *
     *@return    Description of the Return Value
     */
    private boolean checkInformation() {
        if (buddyIDBox.getText().equals("")) {
            return Standard.warningMessage(this, resources.getString("addBuddyDialogTitle"), resources.getString("noIdError"));
        }

        if (buddyAliasBox.getText().equals("")) {
            return Standard.warningMessage(this, resources.getString("addBuddyDialogTitle"), resources.getString("noAliasError"));
        }

        if (buddyGroups.getSelectedItem().equals(
                resources.getString("newGroup"))) {
            if (newGroupBox.getText().equals("")
                     || newGroupBox.getText().equals(
                    resources.getString("newGroup"))) {
                return Standard.warningMessage(this, resources.getString("addBuddyDialogTitle"), resources.getString("newGroupError"));
            }
        }

        return true;
    }


    /**
     *  Called by OK button - checks information and adds the buddy
     */
    private void okButtonHandler() {
        if (checkInformation()) {
            String buddyGroup = (String) buddyGroups.getSelectedItem();
            if (buddyGroup.equals(resources.getString("newGroup"))) {
                buddyGroup = newGroupBox.getText();
            }
            if (buddyGroup.equals(resources.getString("none"))) {
                buddyGroup = null;
            }

            addBuddy(buddyGroup, buddyAliasBox.getText(), buddyIDBox.getText());
        }
    }


    /**
     *  Handles all button events
     *
     *@author     Adam Olsen
     *@created    September 26, 2005
     *@version    1.0
     */
    class ActionHandler implements ActionListener {
        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() != cancelButton) {
                okButtonHandler();
            } else {
                cancelButtonHandler();
            }
        }
    }


    /**
     *  Creates an input box with a corresponding label
     *
     *@param  container  the container to add the input box to
     *@param  grid       the GridBagLayout to use
     *@param  label      the label to use
     *@param  box        the input box to use
     */
    private void createInputBox(Container container, GridBagLayout grid,
            String label, Container box) {
        JLabel labelBox = new JLabel(label + "    ");

        c.gridy = row++;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        grid.setConstraints(labelBox, c);
        container.add(labelBox);

        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        grid.setConstraints(box, c);
        container.add(box);
    }


    /**
     *  Gets the different available RosterGroups
     *
     *@return    an array of strings representing the RosterGroups
     */
    private String[] getRosterGroups() {
        Roster roster = ConnectorThread.getInstance().getRoster();
        String rosterGroups[] = new String[roster.getGroupCount() + 2];

        int i = 0;

        if ((!currentGroup.equals(""))) {
            rosterGroups[i] = currentGroup;
            i++;
        }

        rosterGroups[i++] = resources.getString("none");
        rosterGroups[i++] = resources.getString("newGroup");

        Iterator iterator = roster.getGroups();
        while (iterator.hasNext()) {
            RosterGroup rosterGroup = (RosterGroup) iterator.next();
            if ((currentGroup.equals(""))
                     || (!rosterGroup.getName().equals(currentGroup))) {
                rosterGroups[i] = rosterGroup.getName();
                i++;
            }
        }

        return rosterGroups;
    }


    /**
     *  Runs the add buddy thread and adds or modifies a buddy in the Roster
     *
     *@param  groupName   the group to put the buddy in
     *@param  buddyAlias  the alias of the buddy
     *@param  buddyId     the buddy's JID
     */
    private void addBuddy(String groupName, String buddyAlias, String buddyId) {
        Roster buddyGroups = ConnectorThread.getInstance()
                .getRoster();

        WaitDialog wait = new WaitDialog(this, null, resources.getString("pleaseWait"));
        wait.setVisible(true);
        setVisible(false);

        Thread thread = new Thread(new AddBuddyThread(wait, groupName,
                buddyAlias, buddyId, this));
        thread.start();
    }


    /**
     *  Actually adds the buddy to the Roster
     *
     *@author     Adam Olsen
     *@created    September 26, 2005
     *@version    1.0
     */
    class AddBuddyThread implements Runnable {
        private String errorMessage;

        private String groupName;

        private String buddyAlias;

        private String buddyId;

        private AddBuddyDialog dialog;

        private WaitDialog wait;


        /**
         *  Default constructor
         *
         *@param  wait        the wait dialog
         *@param  groupName   the group to use
         *@param  buddyAlias  the buddy's alias
         *@param  buddyId     the buddy's JID
         *@param  dialog      the AddBuddyDialog that called this thread
         */
        public AddBuddyThread(WaitDialog wait, String groupName,
                String buddyAlias, String buddyId, AddBuddyDialog dialog) {
            this.wait = wait;
            this.groupName = groupName;
            this.buddyAlias = buddyAlias;
            this.buddyId = buddyId.trim();
            this.dialog = dialog;
        }


        /**
         *  Called by the enclosing Thread - will attempt to add the buddy to
         *  the Roster, and will display an error if it wasn't successfull
         */
        public void run() {
            final Roster roster = BuddyList.getInstance().getConnection().getRoster();
            final BuddyStatus buddy = BuddyList.getInstance().getBuddyStatus(
                    buddyId);
            if (modify) {
                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            BuddyList.getInstance().getBuddyListTree().removeBuddy(
                                    buddy, buddy.getGroup(), true);
                        }
                    });
            }

            try {


                if (modify) {
                    com.valhalla.Logger.debug("modifying roster item");
                    RosterEntry entry = buddy.getRosterEntry();
                    entry.setName(buddyAlias);
                    int c = 0;
                    Iterator groups = entry.getGroups();
                    while (groups.hasNext()) {
                        RosterGroup g = (RosterGroup) groups.next();
                        if(!g.contains(entry)) continue;
                        g.removeEntry(entry);
                        c++;
                    }

                    buddy.setTempGroup(groupName);
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            dialog.setVisible(false);
                            buddy.setRemoved(false);

                            NMOptionDialog.createMessageDialog(null, resources.getString("addBuddyDialogTitle"), resources.getString("buddyAdded"));

                            BuddyList.getInstance().getBuddyListTree().addBuddy(
                                    buddy);
                        }
                    });

                    if (groupName != null && !groupName.equals("")) {
                        RosterGroup newGroup = null;
                        newGroup = roster.getGroup(groupName);
                        if (newGroup == null) {
                            com.valhalla.Logger.debug("had to create new group" + groupName);
                            newGroup = roster.createGroup(groupName);
                        } else {
                            com.valhalla.Logger.debug("found group " + newGroup.getName());
                        }

                        if (c != 0) {
                            com.valhalla.Logger.debug("Moving buddy to " + newGroup.getName());
                            newGroup.addEntry(entry);
                        } else {
                            roster.createEntry(buddyId, buddyAlias,
                                    new String[]{groupName});

                        }
                    }
                }
                /*
                 *  if it's a new entry
                 */else {

                    if (groupName == null) {
                        roster.createEntry(buddyId, buddyAlias, null);
                    } else {
                        roster.createEntry(buddyId, buddyAlias,
                                new String[]{groupName});
                        buddy.setTempGroup(groupName);
                    }

                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            dialog.setVisible(false);
                            buddy.setRemoved(false);

                            NMOptionDialog.createMessageDialog(null, resources.getString("addBuddyDialogTitle"), resources.getString("buddyAdded"));

                            BuddyList.getInstance().getBuddyListTree().addBuddy(
                                    buddy);
                        }
                    });
                }
            } catch (XMPPException e) {
                if (e.getXMPPError() == null) {
                    errorMessage = e.getMessage();
                } else {
                    errorMessage = resources.getString("xmppError"
                             + e.getXMPPError().getCode());
                }
            }

            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        wait.dispose();

                        if (errorMessage != null) {
                            Standard.warningMessage(dialog, resources.getString("addBuddyDialogTitle"),
                                    errorMessage);
                            dialog.setVisible(true);
                        } else {
                            DialogTracker.removeDialog(dialog);

                        }
                    }

                });
        }
    }
}

