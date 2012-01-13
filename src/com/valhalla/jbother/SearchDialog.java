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

import com.valhalla.gui.*;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import com.valhalla.jbother.jabber.smack.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * dialog used in searching using Jabber Search (eg. Jabber User Directories)
 * exploits JEP-0055's 'jabber:iq:search' namespace
 * handles dynamic number of search fields
 *
 * @author     Lukasz Wiechec
 * @created    Apr 1, 2005 1:36:43 PM
 */

public class SearchDialog extends JDialog implements WaitDialogListener
{
    private static String fId = "$Id$";

    private String service;
    private ResourceBundle resources = ResourceBundle.getBundle("JBotherBundle", Locale.getDefault());

    // this will hold the mappings: <searchFieldName> -> textfield that contains it
    private HashMap searchTextFields = new HashMap();

    // all data for displaying the inside of this dialog is taken from here:
    private Search search;

    private JPanel container = new JPanel();
    private JPanel inputPanel = new JPanel();
    private JPanel resultsPanel = new JPanel();
    private JButton closeButton = new JButton("Close");
    private JTextArea instructionsArea = new JTextArea();
    private JButton searchButton = new JButton("Search");
    private JButton stopSearchButton = new JButton("Stop");
    private JTable resultsTable = new JTable();
    private SearchResultsTableModel resultsTableModel = new SearchResultsTableModel();
    private JButton addContactButton = new JButton("Add contact");
    private JButton userInfoButton = new JButton("User info");
    private WaitDialog wait = new WaitDialog(this,null,"Gathering search information...");
    // create panel with search fields
    private JPanel searchFieldsPanel = new JPanel();
    private GridBagLayout gridbag = new GridBagLayout();
    private GridBagConstraints c = new GridBagConstraints();


    private int selectedRow = -1;
    private boolean cancelled = false;

    /**
     *Constructor for the SearchDialog object
     *
     * @param  service                Description of the Parameter
     * @exception  HeadlessException  Description of the Exception
     */
    public SearchDialog(String service)
        throws HeadlessException
    {
        super(BuddyList.getInstance().getContainerFrame(), "Search", true);
        setModal(false);
        setTitle(resources.getString("search") + ": " + service);
        this.service = service;
        wait.setWaitListener(this);

        getRootPane().setDefaultButton(searchButton);

        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        initialize();
        DialogTracker.addDialog(this, false, true);
        collectInformation();

    }

    public void cancel() { this.cancelled = true; dispose(); }

    public void collectInformation()
    {
        wait.setVisible(true);

        new Thread(new QuerySearch()).start();
    }


    /**
     *  Description of the Class
     *
     * @author     synic
     * @created    April 14, 2005
     */
    class QuerySearch implements Runnable
    {
        /**
         *  Main processing method for the QuerySearch object
         */
        public void run()
        {
            if(querySearchService(service) == true)
            {
                if( cancelled ) { return; }
                SwingUtilities.invokeLater(
                    new Runnable()
                    {
                        public void run()
                        {
                            instructionsArea.setText(search.getInstructions());
                            Iterator iSearchFields = search.getSearchFields().keySet().iterator();
                            while(iSearchFields.hasNext())
                            {
                                String searchFieldName = (String) iSearchFields.next();
                                JLabel searchFieldNameLabel = new JLabel(capitalizeString(searchFieldName) + ": ");
                                gridbag.setConstraints(searchFieldNameLabel, c);
                                searchFieldsPanel.add(searchFieldNameLabel, c);
                                c.gridx++;
                                c.gridwidth = GridBagConstraints.REMAINDER;
                                c.weightx = 1.0;
                                c.fill = GridBagConstraints.HORIZONTAL;
                                JTextField inputSearchField = new JTextField();
                                inputSearchField.setColumns(30);
                                searchTextFields.put(searchFieldName, inputSearchField);
                                gridbag.setConstraints(inputSearchField, c);
                                searchFieldsPanel.add(inputSearchField, c);
                                c.gridx = 0;
                                c.gridwidth = 1;
                                c.weightx = 0;
                                c.fill = GridBagConstraints.NONE;
                                c.gridy++;
                            }

                            searchFieldsPanel.validate();
                            searchFieldsPanel.repaint();
                            wait.setVisible(false);
                            wait = new WaitDialog(SearchDialog.this,null,"Searching...");
                            setVisible(true);
                        }
                    });
            }
        }
    }

    /**
     * initialize the frame
     */
    private void initialize()
    {
        inputPanel = initializeInputPanel();
        resultsPanel = initializeResultsPanel();
        initializeListeners();
        container.setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, resultsPanel);
        splitPane.setDividerLocation(200);
        container.add(splitPane, BorderLayout.CENTER);
        JPanel bottomButtonPanel = new JPanel(new BorderLayout());
        bottomButtonPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 10));
        bottomButtonPanel.add(closeButton, BorderLayout.EAST);
        container.add(bottomButtonPanel, BorderLayout.SOUTH);
        addContactButton.setEnabled(false);
        userInfoButton.setEnabled(false);
        setContentPane(container);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * @return    JPanel containing instructions, search fields and start/stop searching buttons
     */
    private JPanel initializeInputPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(resources.getString("searchFields")));

        // add text area with instructions
        instructionsArea.setEditable(false);
        instructionsArea.setBorder(BorderFactory.createEtchedBorder());
        instructionsArea.setPreferredSize(new Dimension(300, 200));
        panel.add(instructionsArea);
        panel.add(Box.createVerticalStrut(5));

        searchFieldsPanel.setLayout(gridbag);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.NORTHWEST;

        panel.add(searchFieldsPanel);
        panel.add(Box.createVerticalStrut(5));

        // create a panel with search/stop search buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(searchButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(stopSearchButton);
        buttonsPanel.add(Box.createHorizontalGlue());
        panel.add(buttonsPanel);
        panel.add(Box.createVerticalGlue());

        JPanel ueberPanel = new JPanel(new BorderLayout());
        ueberPanel.add(panel, BorderLayout.NORTH);

        return ueberPanel;
    }

    /**
     * @return    panel containing table with search results
     */
    private JPanel initializeResultsPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        // table
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSL = resultsTable.getSelectionModel();
        rowSL.addListSelectionListener(
            new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    // ignora extra values
                    if(e.getValueIsAdjusting())
                    {
                        return;
                    }

                    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                    if(lsm.isSelectionEmpty())
                    {
                        // no rows selected
                        selectedRow = -1;
                        addContactButton.setEnabled(false);
                        userInfoButton.setEnabled(false);
                    }
                    else
                    {
                        addContactButton.setEnabled(true);
                        userInfoButton.setEnabled(true);
                        selectedRow = lsm.getMinSelectionIndex();
                    }
                }
            });
        resultsTable.setModel(resultsTableModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Search results"));
        panel.add(scrollPane, BorderLayout.CENTER);
        // buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(addContactButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(userInfoButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    class SearchThread implements Runnable
    {
        public void run()
        {
            doSearch();
        }
    }

    /**
     * sets up action listeners for buttons
     */
    private void initializeListeners()
    {
        closeButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    dispose();
                }
            });

        searchButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    wait.setVisible( true );
                    new Thread( new SearchThread()).start();
                }
            });

        stopSearchButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {

                }
            });
        addContactButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    AddBuddyDialog addBuddyDialog = new AddBuddyDialog();
                    addBuddyDialog.setBuddyId(
                        ((Search.Item) resultsTableModel.getItems().get(selectedRow)).getJid()
                        );
                    addBuddyDialog.setVisible(true);
                }
            });
        userInfoButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    new InformationViewerDialog(
                        ((Search.Item) resultsTableModel.getItems().get(selectedRow)).getJid());
                }
            });

    }

    /**
     *  Gets the search attribute of the SearchDialog object
     *
     * @return    The search value
     */
    public Search getSearch()
    {
        return search;
    }

    /**
     *  Sets the search attribute of the SearchDialog object
     *
     * @param  search  The new search value
     */
    public void setSearch(Search search)
    {
        this.search = search;
    }

    /**
     * return string with first letter in uppercase
     *
     * @param  aStr  string to capitalize
     * @return       capitalized string
     */
    private String capitalizeString(String aStr)
    {
        return aStr.substring(0, 1).toUpperCase() + aStr.substring(1);
    }

    /**
     * send the empty "jabber:iq:search" query request to the service in order to receive
     * the list of search field and instructions
     * also the results table can be initialized, as now we know the columns
     *
     * @param  service  to query
     * @return          true if service provides Jabber search service
     */
    private boolean querySearchService(String service)
    {
        boolean returnVal = false;
        Search searchQuery = new Search();
        searchQuery.setTo(service);
        searchQuery.setType(IQ.Type.GET);

        sendPacket(searchQuery);

        PacketFilter filter = new AndFilter(new PacketIDFilter(searchQuery.getPacketID()),
            new PacketTypeFilter(IQ.class));

        PacketCollector packetCollector =
            BuddyList.getInstance().getConnection().createPacketCollector(filter);

        IQ reply = (IQ) packetCollector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        if(reply == null)
        {
            SwingUtilities.invokeLater( new Runnable()
            {
                public void run()
                {
                    JOptionPane.showMessageDialog(SearchDialog.this,
                        resources.getString("searchRequestTimeout"),
                        resources.getString("searchError"),
                        JOptionPane.OK_OPTION);
                }
            } );
            returnVal = false;
        }
        else
        {
            if(reply.getType() == IQ.Type.ERROR)
            {
                SwingUtilities.invokeLater( new Runnable()
                {
                    public void run()
                    {
                        JOptionPane.showMessageDialog(SearchDialog.this,
                            resources.getString("serviceDoesNotSupportJaberSearch"),
                            resources.getString("searchError"),
                            JOptionPane.OK_OPTION
                            );
                        wait.setVisible(false);
                    }
                });

                returnVal = false;
            }
            else if(reply instanceof Search)
            {
                search = (Search) reply;
                returnVal = true;
            }
        }
        packetCollector.cancel();
        return returnVal;
    }

    /**
     * "Search" button handler
     * todo: maybe it would be wise to put his code in separate thread?
     */
    private void doSearch()
    {
        if( cancelled ) { return; }
        Search srch = new Search();
        srch.setTo(service);
        srch.setType(IQ.Type.SET);
        HashMap searchCriteria = new HashMap();
        // get the input from all the fields, build Search message and send it
        Iterator iSearchFields = searchTextFields.keySet().iterator();
        while(iSearchFields.hasNext())
        {
            String searchFieldName = (String) iSearchFields.next();
            String searchFieldValue = ((JTextField) searchTextFields.get(searchFieldName)).getText();
            searchCriteria.put(searchFieldName, searchFieldValue);
        }
        srch.setSearchFields(searchCriteria);

        sendPacket(srch);

        PacketFilter filter = new AndFilter(new PacketIDFilter(srch.getPacketID()),
            new PacketTypeFilter(IQ.class));
        PacketCollector packetCollector =
            BuddyList.getInstance().getConnection().createPacketCollector(filter);

        final IQ reply = (IQ) packetCollector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        if(reply == null)
        {
            SwingUtilities.invokeLater( new Runnable()
            {
                public void run()
                {
                    wait.setVisible( false );
                    JOptionPane.showMessageDialog(SearchDialog.this,
                        resources.getString("searchRequestTimeout"),
                        resources.getString("searchError"),
                        JOptionPane.OK_OPTION);
                }
            });
        }
        else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    wait.setVisible( false );
                   if( cancelled ) { return; }

                    // update results table with new data
                    resultsTableModel.setItems(((Search) reply).getItems());
                    resultsTableModel.fireTableStructureChanged();
                    resultsTableModel.fireTableDataChanged();
                }
            });
        }
        packetCollector.cancel();
    }

    /**
     *  Description of the Method
     *
     * @param  packet  Description of the Parameter
     */
    private void sendPacket(Packet packet)
    {
        if(BuddyList.getInstance().getConnection().isConnected())
        {
            BuddyList.getInstance().getConnection().sendPacket(packet);
        }
    }

    /**
     * table model for the results of the search
     * the number of columns will be know *after* querying the JUD. This
     *
     * @author     synic
     * @created    April 14, 2005
     */
    class SearchResultsTableModel extends AbstractTableModel
    {
        // list of Search.Items
        private ArrayList items = new ArrayList();


        /**
         *  Sets the items attribute of the SearchResultsTableModel object
         *
         * @param  aItems  The new items value
         */
        private void setItems(ArrayList aItems)
        {
            items = aItems;
        }

        /**
         *  Gets the items attribute of the SearchResultsTableModel object
         *
         * @return    The items value
         */
        public ArrayList getItems()
        {
            return items;
        }

        /**
         *  Gets the rowCount attribute of the SearchResultsTableModel object
         *
         * @return    The rowCount value
         */
        public int getRowCount()
        {
            if(items == null)
            {
                return 0;
            }
            return items.size();
        }

        /**
         *  Gets the columnCount attribute of the SearchResultsTableModel object
         *
         * @return    The columnCount value
         */
        public int getColumnCount()
        {

            if(items == null || items.size() == 0)
            {
                return 0;
            }
            else
            {
                return ((Search.Item) items.get(0)).getAttributes().size() + 1;
            }
        }

        /**
         *  Gets the valueAt attribute of the SearchResultsTableModel object
         *
         * @param  rowIndex     Description of the Parameter
         * @param  columnIndex  Description of the Parameter
         * @return              The valueAt value
         */
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            String out = "";
            Search.Item item = (Search.Item) items.get(rowIndex);
            if(columnIndex == item.getAttributes().size())
            {
                out = item.getJid();
            }
            else
            {
                Object[] keys = item.getAttributes().keySet().toArray();
                out = (String) item.getAttributes().get(keys[columnIndex]);
            }
            return out;
        }

        /**
         *  Gets the columnName attribute of the SearchResultsTableModel object
         *
         * @param  col  Description of the Parameter
         * @return      The columnName value
         */
        public String getColumnName(int col)
        {
            if(col == search.getSearchFields().keySet().size())
            {
                return "JID";
            }
            else
            {
                Object[] searchFields = search.getSearchFields().keySet().toArray();
                return capitalizeString((String) searchFields[col]);
            }
        }
    }
}

