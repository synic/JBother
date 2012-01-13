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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverItems;

import com.valhalla.gui.*;
import com.valhalla.jbother.groupchat.GroupChatBookmarks;

/**
 *  For browsing Jabber services Displays a dialog for browsing services on
 *  Jabber entities such as servers according to JEP-0030
 *
 *@author     Adam olsen
 *@created    May 25, 2005
 *@version    1.0
 */
public class ServiceDiscoveryDialog extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private String host;

    private JButton closeButton = new JButton(resources.getString("closeButton"));

    private JButton searchButton = new JButton(resources.getString("search"));

    private ServiceTableModel tableModel = new ServiceTableModel(this);

    private JTable table = new JTable(tableModel);

    private JLabel title = new JLabel(resources.getString("serviceDiscoveryManager"));

    private MJTextField serverField = new MJTextField();

    private JLabel status = new JLabel(resources.getString("status") + ": ");

    private JPanel bottomPanel = new JPanel();

    private JPanel middlePanel = new JPanel(new BorderLayout(5, 5));

    private JPanel topPanel = new JPanel();

    private JLabel hostLabel = new JLabel(resources.getString("host") + ": ");

    private ServiceDiscoveryThread currentThread = null;

    private TablePopupMenu popupMenu = new TablePopupMenu(this, table);

    private Vector history = new Vector();

    private int current = -1;

    private JButton forward = new JButton(Standard.getIcon("images/buttons/Forward24.gif"));

    private JButton back = new JButton(Standard.getIcon("images/buttons/Back24.gif"));

    private Properties cache = JBotherLoader.getDiscoveryCache();

    private boolean writing = false;


    /**
     *  Creates a ServiceDiscoveryDialog with parent as it's parent
     *
     *@param  parent  the JFrame that owns this dialog
     */
    public ServiceDiscoveryDialog(JFrame parent) {
        super(parent);
        setTitle(resources.getString("serviceDiscovery"));

        initComponents();
    }


    /**
     *  Sets up the Dialog layout
     */
    private void initComponents() {
        tableModel.setTable(table);
        JPanel panel = (JPanel) getContentPane();

        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);

        topPanel.setLayout(new BorderLayout(5, 5));
        topPanel.add(hostLabel, BorderLayout.WEST);
        topPanel.add(serverField, BorderLayout.CENTER);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.X_AXIS));
        forward.setPreferredSize(new Dimension(26, 26));
        back.setPreferredSize(new Dimension(26, 26));
        navPanel.add(back);
        navPanel.add(forward);

        topPanel.add(navPanel, BorderLayout.EAST);

        middlePanel.add(topPanel, BorderLayout.NORTH);

        middlePanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(title, BorderLayout.NORTH);
        table.setBorder(BorderFactory.createEtchedBorder());
        panel.add(middlePanel, BorderLayout.CENTER);

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(status);

        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(searchButton);
        bottomPanel.add(closeButton);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        pack();
        setSize(new Dimension(600, 300));

        if (BuddyList.getInstance().checkConnection()) {
            serverField.setText(BuddyList.getInstance().getConnection()
                    .getHost());
        }

        ActionListener listener =
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    runServiceDiscovery(null);
                    addHistoryItem(serverField.getText());
                }
            };

        addHistoryItem(serverField.getText());

        serverField.addActionListener(listener);
        searchButton.addActionListener(listener);

        Standard.cascadePlacement(this);

        closeButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    writeServiceDiscoveryCache();
                    dispose();
                }
            });

        table.addMouseListener(new PopupMouseListener());

        forward.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    forwardHandler();
                }
            });

        back.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    backHandler();
                }
            });

        back.setEnabled(false);
        forward.setEnabled(false);
        runServiceDiscovery(null);
    }


    /**
     *  Description of the Method
     */
    private void writeServiceDiscoveryCache() {
        if (writing) {
            return;
        }
        writing = true;

        File file = new File(JBother.settingsDir + File.separatorChar
                 + "discocache.properties");

        try {
            FileOutputStream stream = new FileOutputStream(file);
            cache.store(stream, "");
            stream.close();
        } catch (Exception ex) {

        }

        writing = false;
    }


    /**
     *  Description of the Method
     */
    private void backHandler() {
        current--;
        String id = (String) history.get(current);
        if (id == null) {
            current++;
            return;
        }
        forward.setEnabled(true);
        if (current == 0) {
            back.setEnabled(false);
        }
        runServiceDiscovery(id);
    }


    /**
     *  Description of the Method
     */
    private void forwardHandler() {
        current++;
        String id = (String) history.get(current);
        if (id == null) {
            current--;
            return;
        }

        back.setEnabled(true);
        if (current == history.size() - 1) {
            forward.setEnabled(false);
        }
        runServiceDiscovery(id);
    }


    /**
     *  Adds a feature to the HistoryItem attribute of the
     *  ServiceDiscoveryDialog object
     *
     *@param  id  The feature to be added to the HistoryItem attribute
     */
    protected void addHistoryItem(String id) {
        for (int i = 0; i > current + 1; i--) {
            history.remove(i);
        }

        history.add(id);
        current++;
        forward.setEnabled(false);
        back.setEnabled(true);
    }


    /**
     *  Listens for mouse events
     *
     *@author     Adam Olsen
     *@created    May 25, 2005
     *@version    1.0
     */
    class PopupMouseListener extends MouseAdapter {
        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void mousePressed(MouseEvent e) {
            checkPop(e);
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void mouseReleased(MouseEvent e) {
            checkPop(e);
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
                int row = table.getSelectedRow();
                if (row < 0) {
                    return;
                }

                String id = (String) tableModel.getValueAt(row, 1);
                runServiceDiscovery(id);
                addHistoryItem(id);
            } else {
                checkPop(e);
            }
        }


        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void checkPop(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupMenu.popup(e);
            }
        }
    }


    /**
     *  Starts the service discovery thread on the specified server
     *
     *@param  server  the server to run discovery on
     */
    protected void runServiceDiscovery(String server) {
        if (!BuddyList.getInstance().checkConnection()) {
            BuddyList.getInstance().connectionError();
            return;
        }

        if (server != null) {
            serverField.setText(server);
        }

        if (serverField.getText().equals("")) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        status.setText(resources.getString("status") + ": "
                 + resources.getString("collecting") + " ...");
        tableModel.clear();

        if (currentThread != null) {
            currentThread.abortDiscovery();
        }
        currentThread = new ServiceDiscoveryThread();
        new Thread(currentThread).start();
    }


    /**
     *  The thread that actually collects disco information about the server
     *
     *@author     Adam Olsen
     *@created    May 25, 2005
     *@version    1.0
     */
    class ServiceDiscoveryThread implements Runnable {
        private boolean stopped = false;

        private ArrayList discoItems = new ArrayList();


        /**
         *  Main processing method for the ServiceDiscoveryThread object
         */
        public void run() {
            if (!BuddyList.getInstance().checkConnection()) {
                BuddyList.getInstance().connectionError();
                return;
            }

            ServiceDiscoveryManager manager = new ServiceDiscoveryManager(
                    BuddyList.getInstance().getConnection());

            // get the discover items for the server
            try {
                DiscoverItems items = manager.discoverItems(serverField.getText());
                Iterator i = items.getItems();

                String top[] = new String[]{serverField.getText(),
                        serverField.getText(), "", "", ""};
                discoItems.add(top);
                tableModel.addItem(top);

                while (i.hasNext()) {
                    DiscoverItems.Item item = (DiscoverItems.Item) i.next();
                    if (stopped) {
                        return;
                    }

                    final String[] entry = new String[]{item.getName(),
                            item.getEntityID(), "", ""};
                    discoItems.add(entry);

                    SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                tableModel.addItem(entry);
                            }
                        });
                }

                for (int icount = 0; icount < discoItems.size(); icount++) {
                    String[] entry = (String[]) discoItems.get(icount);
                    final String id = entry[1];

                    status.setText(resources.getString("status") + ": "
                             + resources.getString("gettingFeatures") + " ("
                             + id + ") ...");

                    // get the discover info about each item
                    DiscoverInfo info = null;

                    try {
                        info = manager.discoverInfo(id);
                    } catch (XMPPException e) {
                    }

                    // if the service discovery has been aborted, bail out
                    if (stopped) {
                        return;
                    }
                    tableModel.setDisco(icount, info);

                    if (info != null) {
                        final DiscoverInfo tempInfo = info;
                        final int index = icount;
                        SwingUtilities.invokeLater(
                            new Runnable() {
                                public void run() {
                                    Iterator identities = tempInfo.getIdentities();
                                    while (identities.hasNext()) {
                                        DiscoverInfo.Identity identity = (DiscoverInfo.Identity) identities.next();
                                        if (stopped) {
                                            return;
                                        }
                                        // set the table information
                                        tableModel.setItemInfo(index, identity.getName(), identity.getCategory(),
                                                identity.getType());

                                        if (identity.getCategory()
                                                .equals("gateway")) {
                                            cache.setProperty(id, identity.getCategory()
                                                     + " " + identity.getType());
                                        }
                                    }
                                }
                            });
                    }
                }

            } catch (XMPPException e) {
                String message = e.getMessage();
                if (e.getXMPPError() != null) {
                    message = resources.getString("xmppError"
                             + e.getXMPPError().getCode());
                }
                status.setText(resources.getString("error") + ": " + message);
                return;
            }

            writeServiceDiscoveryCache();

            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        status.setText(resources.getString("status") + ": "
                                 + resources.getString("completed") + ".");
                    }
                });
        }


        /**
         *  Aborts the service discovery
         */
        public void abortDiscovery() {
            this.stopped = true;
        }
    }
}

/**
 *  The popup menu for each of the disco items
 *
 *@author     Adam Olsen
 *@created    May 25, 2005
 *@version    1.0
 */

class TablePopupMenu extends JPopupMenu {


    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JTable table;

    private ServiceTableModel model;

    private ServiceDiscoveryDialog dialog;

    private JMenuItem browseItem = new JMenuItem(resources.getString("browse"));

    private JMenuItem registerItem = new JMenuItem(resources.getString("register"));

    private JMenuItem joinItem = new JMenuItem(resources.getString("join"));

    private JMenuItem addItem = new JMenuItem(resources.getString("addToRoster"));

    private JMenuItem searchItem = new JMenuItem(resources.getString("search"));


    /**
     *  Default constructor
     *
     *@param  dialog  The ServiceDiscoveryDialog to connect this popup menu to
     *@param  table   the table to connect this menu to
     */
    public TablePopupMenu(ServiceDiscoveryDialog dialog, JTable table) {
        this.dialog = dialog;
        this.table = table;
        model = (ServiceTableModel) table.getModel();

        add(addItem);
        add(browseItem);
        add(registerItem);
        add(joinItem);
        add(searchItem);

        // show the add buddy dialog
        addItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    AddBuddyDialog dialog = new AddBuddyDialog();
                    String id = getId();
                    dialog.setBuddyId(id);
                    dialog.setVisible(true);
                }
            });

        // the browse item runs service discovery on that item
        browseItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String id = getId();
                    TablePopupMenu.this.dialog.runServiceDiscovery(id);
                    TablePopupMenu.this.dialog.addHistoryItem(id);
                }
            });

        joinItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String id = getId();
                    GroupChatBookmarks.showDialog(id, BuddyList.getInstance()
                            .getConnection().getUser(), "");
                }
            });

        // the register item forms a RegistrationForm for that item
        registerItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String id = getId();
                    new RegistrationForm(BuddyList.getInstance().getContainerFrame(), id).getRegistrationInfo();
                }
            });

        // search dialog
        searchItem.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String id = getId();
                    SearchDialog searchDialog = new SearchDialog(id);
                    searchDialog.setSize(700, 500);
                }
            });
    }


    /**
     *  Returns the ID of the selected row
     *
     *@return    the id of the row
     */
    private String getId() {
        int row = this.table.getSelectedRow();
        if (row < 0) {
            return "";
        }

        String id = (String) model.getValueAt(row, 1);
        return id;
    }


    /**
     *  Shows the popup menu
     *
     *@param  e  the mouse event
     */
    public void popup(MouseEvent e) {
        int selectedRow = table.rowAtPoint(e.getPoint());
        if (selectedRow < 0) {
            return;
        }
        table.setRowSelectionInterval(selectedRow, selectedRow);

        String features = model.getFeatures(selectedRow);
        if (features.indexOf("r") > -1) {
            registerItem.setEnabled(true);
        } else {
            registerItem.setEnabled(false);
        }

        if (features.indexOf("b") > -1) {
            browseItem.setEnabled(true);
        } else {
            browseItem.setEnabled(false);
        }

        if (features.indexOf("j") > -1) {
            joinItem.setEnabled(true);
        } else {
            joinItem.setEnabled(false);
        }

        if (features.indexOf("s") > -1) {
            searchItem.setEnabled(true);
        } else {
            searchItem.setEnabled(false);
        }

        validate();

        show(table, e.getX(), e.getY());
    }
}

/**
 *  The table model for the ServiceDiscoveryDialog table
 *
 *@author     Adam Olsen
 *@created    May 25, 2005
 *@version    1.0
 */

class ServiceTableModel extends AbstractTableModel {


    private ServiceDiscoveryDialog dialog;

    private JTable table;

    private ArrayList items = new ArrayList();

    private String[] columns = new String[]{"Name", "JID", "Category", "Type"};

    private ArrayList infos = new ArrayList();


    /**
     *  Sets the table value for this model
     *
     *@param  table  the table that this model represents
     */
    public void setTable(JTable table) {
        this.table = table;

        TableColumn column = null;

        // set the default column widths
        for (int i = 0; i < getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            if (i < 2) {
                column.setPreferredWidth(150);
            }
            // sport column is bigger
            else {
                column.setPreferredWidth(50);
            }
        }
    }


    /**
     *  Default Constructor
     *
     *@param  dialog  the ServiceDiscoveryDialog that contains this table
     */
    public ServiceTableModel(ServiceDiscoveryDialog dialog) {
        this.dialog = dialog;
    }


    /**
     *  gets the number of columns in this table
     *
     *@return    the number of columns
     */
    public int getColumnCount() {
        return columns.length;
    }


    /**
     *  Gets the number of rows in the table
     *
     *@return    the number of rows in the table
     */
    public int getRowCount() {
        return items.size();
    }


    /**
     *  Returns the name of a specific column
     *
     *@param  column  the column who's name is wanted
     *@return         the name of the column
     */
    public String getColumnName(int column) {
        return columns[column];
    }


    /**
     *  Get the Object for a specific coordinate in the table
     *
     *@param  row     the row of the item
     *@param  column  the column of the item
     *@return         the Object at the specified coordinates
     */
    public Object getValueAt(int row, int column) {
        synchronized (items) {
            String[] item = (String[]) items.get(row);
            return item[column];
        }
    }


    /**
     *  gets the features of a specific item (row)
     *
     *@param  row  the index of the row you want information for
     *@return      a string containing all of the features the row supports
     */
    public String getFeatures(int row) {
        synchronized (infos) {
            String info = "";

            try {
                info = (String) infos.get(row);
            } catch (Exception e) {
            }

            return info;
        }
    }


    /**
     *  Sets the disco information once it's found
     *
     *@param  index  the index of the row you want to set the information about
     *@param  disco  the information about the row
     */
    public void setDisco(int index, DiscoverInfo disco) {
        synchronized (infos) {
            String info = "";
            if (disco != null) {
                if (disco.containsFeature("jabber:iq:register")) {
                    info += "r";
                }
                if (disco.containsFeature("jabber:iq:browse")
                         || disco.containsFeature("http://jabber.org/protocol/disco")) {
                    info += "b";
                }
                if (disco.containsFeature("muc_public")) {
                    info += "j";
                }

                if (disco.containsFeature("jabber:iq:search")) {
                    info += "s";
                }
            }

            infos.add(index, info);
        }
    }


    /**
     *  Adds a row to the table
     *
     *@param  item  the array containing the item to add
     */
    public void addItem(String[] item) {
        synchronized (items) {
            items.add(item);
            fireTableRowsInserted(items.size(), items.size());
        }
    }


    /**
     *  Sets information about a specific row
     *
     *@param  row       the row to set
     *@param  name      the new name
     *@param  category  the category of the row
     *@param  type      The new itemInfo value
     */
    public void setItemInfo(int row, String name, String category, String type) {
        synchronized (items) {
            String[] item = (String[]) items.get(row);
            item[0] = name;
            item[2] = category;
            item[3] = type;

            fireTableRowsUpdated(row, row);
        }
    }


    /**
     *  Clears the table
     */
    public void clear() {
        items.clear();
        infos.clear();
        table.tableChanged(new TableModelEvent(this));
    }
}

