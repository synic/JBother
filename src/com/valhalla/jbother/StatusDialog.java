/*
    Copyright (C) 2003 Adam Olsen This program is free software; you can
    redistribute it and/or modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either version 1, or
    (at your option) any later version. This program is distributed in the hope
    that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
    warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    General Public License for more details. You should have received a copy of
    the GNU General Public License along with this program; if not, write to the
    Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
package com.valhalla.jbother;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import com.valhalla.settings.*;

import org.jivesoftware.smack.packet.Presence;

/**
 * @author     Anrdey Zakirov
 * @created    April 12, 2005
 * @version    0.2
 * @since      April 10, 2005
 */

public class StatusDialog extends JDialog
{
    private ResourceBundle resources = ResourceBundle.getBundle(
        "JBotherBundle", Locale.getDefault());

    private JPanel main;

    private JButton okButton = new JButton(resources.getString("okButton"));

    private JButton changeNameButton = new JButton("Rename");

    private JButton deleteButton = new JButton("Delete");

    private JButton cancelButton = new JButton(resources.getString("cancelButton"));

    private JList statusList = new JList();
    private StatusListModel model = new StatusListModel();

    private JTextArea itemText = new JTextArea(5, 10);

    private JScrollPane itemScroll = new JScrollPane(itemText);

    private Presence.Mode mode = null;

    private StatusMessageProperties statusProps;

    private Hashtable modeHash = new Hashtable();

    private String modeString;

    private String modeLocaleString;

    private String modeDescription;

    private JTextField priorityBox = new JTextField(5);

    /**
     *Constructor for the StatusDialog object
     *
     * @param  mode  Description of the Parameter
     */
    public StatusDialog(Presence.Mode mode)
    {

        super(BuddyList.getInstance().getContainerFrame(), "", true);
        statusProps = new StatusMessageProperties();

        statusList.setModel(model);

        this.mode = mode;
        main = (JPanel) getContentPane();
        main.setBorder(BorderFactory.createTitledBorder(resources.getString("enterStatusMessage")));
        String currentMessage = BuddyList.getInstance()
            .getCurrentStatusString();
        GridBagLayout grid = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        main.setLayout(grid);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.weightx = 1.0;

        grid.setConstraints(statusList, c);

        modeString = mode.toString();

        modeLocaleString = resources.getString(modeString);
		itemText.setWrapStyleWord( true );
		itemText.setLineWrap( true );

        modeDescription = resources.getString(modeString + ".description");

        this.setTitle(resources.getString(mode.toString()));

        if(!statusProps.containsKey(modeString + "." + modeLocaleString))
        {
            statusProps.put(modeString + "." + modeLocaleString, modeDescription);
        }

        StatusMessageProperties statusProps1 = (StatusMessageProperties) statusProps.clone();

        String item = null;
        String key = null;
        String key2 = null;
        boolean justChecked = false;
        while(statusProps1.keys().hasMoreElements())
        {
            item = null;
            key = (String) statusProps1.keys().nextElement();
            item = (String) statusProps1.get(key);
            if(key.startsWith(mode.toString()) == true)
            {
                key2 = key.substring(modeString.length() + 1, key.length());
                if(modeHash.containsKey(key2) == false)
                {
                    model.addItem(key2);
                    modeHash.put(key2, item);
                    if(currentMessage.matches(item))
                    {
                        justChecked = true;
                        statusList.setSelectedValue(key2, true);
                        itemText.setText(item);
                    }

                }
            }
            statusProps1.remove(key);
        }

        if(!justChecked)
        {
            statusList.setSelectedIndex(0);
            itemText.setText((String) modeHash.get((String) statusList.getSelectedValue()));
        }

        JPanel status = new JPanel();
        status.setLayout(new BorderLayout(5, 5));
        status.add(new JScrollPane(statusList), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        changeNameButton.setMaximumSize(new Dimension(100, 100));
        deleteButton.setMaximumSize(new Dimension(100, 100));

        buttonPanel.add(changeNameButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createVerticalGlue());
        status.add(buttonPanel, BorderLayout.EAST);

        status.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        c.gridy++;
        grid.setConstraints(status, c);

        main.add(status);

        JPanel items = new JPanel();
        items.setLayout(new BorderLayout());
        JLabel newLabel = new JLabel(resources.getString("createNewStatusMessage"));
        newLabel.setBorder( BorderFactory.createEmptyBorder(0,0,5,0));
        items.add(newLabel,BorderLayout.NORTH);
        items.add(itemScroll,BorderLayout.CENTER);
        items.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        c.gridy++;
        grid.setConstraints(items, c);
        main.add(items);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(okButton);
        buttons.add(cancelButton);
        buttons.add(Box.createHorizontalGlue());
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));
        getRootPane().setDefaultButton(okButton);

        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        String p = Settings.getInstance().getProperty("priority", "5");

        JPanel priority = new JPanel();
        priorityBox.setText(p);
        priority.setLayout( new BoxLayout(priority,BoxLayout.X_AXIS));
        priority.add(Box.createHorizontalGlue());
        priority.add(new JLabel(resources.getString("priority")+":   "));
        priority.add(priorityBox);
        priority.add(Box.createHorizontalGlue());
        c.gridy++;
        grid.setConstraints(priority, c);
        main.add(priority);

        c.weightx = 1;
        c.gridy++;
        grid.setConstraints(buttons, c);
        main.add(buttons);

        addListeners();
        pack();
        setLocationRelativeTo(null);

        addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    cancelHandler();
                }
            });

        Dimension dim = getSize();
        setSize(350, (int) dim.getHeight());
        setResizable(false);

        setVisible(true);
    }

    /**
     *  Adds a feature to the Listeners attribute of the StatusDialog object
     */
    private void addListeners()
    {
        statusList.addMouseListener(new MouseClickListener());
        okButton.addActionListener(new OKDialogListener());
        cancelButton.addActionListener(new CancelDialogListener());
        changeNameButton.addActionListener(new ChangeNameDialogListener());
        deleteButton.addActionListener(new DeleteDialogListener());
    }

   class MouseClickListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {

            if( e.getClickCount() >= 2 ) okHandler();
            else statusHandler();
        }
    }

     /**
     *  Description of the Class
     *
     * @author     synic
     * @created    April 12, 2005
     */
    class OKDialogListener implements ActionListener
    {
        /**
         *  Description of the Method
         *
         * @param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e)
        {
            okHandler();
        }
    }

    /**
     *  Description of the Class
     *
     * @author     synic
     * @created    April 12, 2005
     */
    class CancelDialogListener implements ActionListener
    {
        /**
         *  Description of the Method
         *
         * @param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e)
        {
            cancelHandler();
        }
    }

    /**
     *  Description of the Class
     *
     * @author     synic
     * @created    April 12, 2005
     */
    class DeleteDialogListener implements ActionListener
    {
        /**
         *  Description of the Method
         *
         * @param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e)
        {
            deleteHandler();
        }
    }

    /**
     *  Description of the Class
     *
     * @author     synic
     * @created    April 12, 2005
     */
    class ChangeNameDialogListener implements ActionListener
    {
        /**
         *  Description of the Method
         *
         * @param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e)
        {
            changeNameHandler();
        }
    }

    /**
     *  Description of the Method
     */
    private void okHandler()
    {
        String statusText = itemText.getText();
        if(!statusProps.containsValue(statusText))
        {
            if(statusProps.addMessage(statusText))
            {
                statusProps.saveToFile();
            }
        }

        Settings.getInstance().setProperty("priority", priorityBox.getText());

        BuddyList.getInstance().setStatus(mode, statusText, false);
        dispose();
    }

    /**
     *  Description of the Method
     */
    private void cancelHandler()
    {
        BuddyList.getInstance().getStatusMenu().setModeChecked(
            BuddyList.getInstance().getCurrentPresenceMode());
        dispose();
    }

    /**
     *  Description of the Method
     */
    private void deleteHandler()
    {
        if(model.getSize() < 2)
        {
            JOptionPane.showMessageDialog(null, "This is the last entry. Make another to remove it.");
            return;
        }
        int i = JOptionPane.showConfirmDialog(null, "Do you really want to remove this presence message?", "", 0, 1);
        if(i == 0)
        {
            i = statusList.getSelectedIndex();
            String temp = (String) statusList.getSelectedValue();
            statusProps.remove(mode + "." + temp);
            if(i > 0)
            {
                statusList.setSelectedIndex(i - 1);
            }
            else
            {
                statusList.setSelectedIndex(i + 1);
            }
            model.removeItem(temp);
            statusProps.saveToFile();
            statusHandler();
        }
    }

    /**
     *  Description of the Method
     */
    private void changeNameHandler()
    {
        String key;
        while(true)
        {
            key = JOptionPane.showInputDialog("Please enter this status name:");
            if(key.equals("") == true)
            {
                JOptionPane.showMessageDialog(null, "Presence name must be not blank! Please, enter name.");
            }
            else if((statusProps.containsKey(modeString + "." + key) == false))
            {
                break;
            }
            else
            {
                JOptionPane.showMessageDialog(null, "This name is already taken! Pease, choose other name.");
            }
        }

        if(key.equals(null) == false)
        {

            String selected = (String) statusList.getSelectedValue();
            String message = (String) statusProps.get(mode + "." + selected);
            statusProps.remove(mode + "." + selected);
            statusProps.put(mode + "." + key, message);
            model.removeItem(selected);
            model.addItem(key);
            statusList.setSelectedValue(key, true);
            statusProps.saveToFile();
        }

    }


    /**
     *  Description of the Method
     */
    private void statusHandler()
    {
        itemText.setText((String) statusProps.get(modeString + "." + (String) statusList.getSelectedValue()));
    }

    /**
     * The model that represents the list of buddies in the room
     *
     * @author     Adam Olsen
     * @created    April 12, 2005
     * @version    1.0
     */
    class StatusListModel extends AbstractListModel
    {
        private Vector statuses = new Vector();

        /**
         * @return    the number of elements in the list
         */
        public int getSize()
        {
            return statuses.size();
        }

        /**
         * @param  row  the element you want to get
         * @return      the Object at <tt>row</tt>
         */
        public Object getElementAt(int row)
        {
            return statuses.get(row);
        }

        /**
         * @param  status  The feature to be added to the Item attribute
         */
        public void addItem(String status)
        {
            statuses.add(status);
            fireChanged();
        }

        /**
         * Removes a buddy from the list
         *
         * @param  status  Description of the Parameter
         */
        public void removeItem(String status)
        {
            statuses.remove(status);
            fireChanged();
        }

        /**
         * Fires a change of the list
         */
        private void fireChanged()
        {
            SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        fireContentsChanged(StatusListModel.this, 0, statuses.size());
                        statusList.validate();
                    }
                });
        }
    }

    /**
     *  Description of the Class
     *
     * @author     synic
     * @created    April 12, 2005
     */
    private class StatusMessageProperties extends Properties
    {

        private File propDir;

        private File propFile;

        private int tempIndex = 0;

        /**
         *Constructor for the StatusMessageProperties object
         */
        public StatusMessageProperties()
        {
            this(JBother.profileDir, "statusmessages.properties");
        }

        /**
         *Constructor for the StatusMessageProperties object
         *
         * @param  propDir   Description of the Parameter
         * @param  propFile  Description of the Parameter
         */
        private StatusMessageProperties(String propDir, String propFile)
        {

            this.propDir = new File(propDir);
            this.propFile = new File(propDir, propFile);
            loadFromFile();

        }

        /**
         *  Description of the Method
         */
        public void loadFromFile()
        {

            if(!propDir.isDirectory())
            {

                if(!propDir.isDirectory() && !propDir.mkdirs())
                {
                    com.valhalla.Logger.debug("Could not create directory for StatusMessageProperties file! ("
                         + propDir.getName() + ")");
                }

            }

            if(propFile.isFile())
            {
                try
                {
                    InputStream is = new FileInputStream(propFile);
                    load(is);
                    is.close();
                }
                catch(Exception e)
                {
                    com.valhalla.Logger.debug("Could not load away message properties file");
                    com.valhalla.Logger.debug(e.getMessage());
                }
            }

        }

        /**
         *  Adds a feature to the Message attribute of the StatusMessageProperties object
         *
         * @param  message  The feature to be added to the Message attribute
         * @return          Description of the Return Value
         */
        public boolean addMessage(String message)
        {
            String key;
            while(true)
            {
                key = JOptionPane.showInputDialog("Please enter this status name:");
                if(key.equals("") == true)
                {
                    JOptionPane.showMessageDialog(null, "Presence name must be not blank! Please, enter name.");
                }
                else if((statusProps.containsKey(modeString + "." + key) == false))
                {
                    break;
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "This name is already taken! Pease, choose other name.");
                }
            }

            if(key.equals(null) == false)
            {
                model.addItem(key);
                itemText.setText(message);
                statusProps.put(mode + "." + key, message);
                return true;
            }
            return false;
        }

        /**
         *  Description of the Method
         */
        public void saveToFile()
        {

            StatusMessageProperties propCopy = (StatusMessageProperties) clone();
            clear();
            int i = 0;
            for(Enumeration e = propCopy.propertyNames(); e.hasMoreElements(); i++)
            {
                String propName = (String) e.nextElement();
                setProperty(propName, propCopy.getProperty(propName));
            }
            if(propDir.isDirectory())
            {
                try
                {
                    OutputStream os = new FileOutputStream(propFile);
                    store(os, "StatusMessages");
                    os.close();
                }
                catch(Exception e)
                {
                    com.valhalla.Logger.debug("Could not save away message properties file");
                    com.valhalla.Logger.debug(e.getMessage());
                }
            }

        }

    }

}

