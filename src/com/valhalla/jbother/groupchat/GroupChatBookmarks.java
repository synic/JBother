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

package com.valhalla.jbother.groupchat;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;

import java.util.*;

import javax.swing.*;
import java.io.*;
import com.valhalla.jbother.jabber.smack.*;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.*;
import com.valhalla.gui.*;
import com.valhalla.settings.*;
import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.JBother;

/**
 * Allows the user to create bookmarks for each of their favorite group chat
 * rooms
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class GroupChatBookmarks extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JPanel container = new JPanel();

    private JList bookMarkList;

    private JPanel rightPanel = new JPanel();

    private JPanel leftPanel = new JPanel();

    private JPanel buttonPanel = new JPanel();

    private JPanel inputPanel = new JPanel();

    private JButton saveButton = new JButton(resources.getString("saveButton")),
            openButton = new JButton(resources.getString("openButton")),
            cancelButton = new JButton(resources.getString("cancelButton"));

    private MJTextField roomBox = new MJTextField(15),
            serverBox = new MJTextField(20), nickBox = new MJTextField(15);

    private JPasswordField passBox = new JPasswordField(15);

    private int row = 0;

    private GridBagLayout grid = new GridBagLayout();

    private GridBagConstraints c = new GridBagConstraints();

    private JPopupMenu deleteMenu = new JPopupMenu();
    private Vector bookmarks = new Vector();

    private JMenuItem deleteItem = new JMenuItem(resources
            .getString("deleteButton"));

    private JCheckBox auto = new JCheckBox(resources.getString("autoJoinRoom"));

    private WaitDialog wait = new WaitDialog(this,null,resources.getString("pleaseWait"));
    private Bookmark bookmark;
    private PrivateDataManager pDataManager;
    private boolean pdBookmark = false;
    private File bookmarksDir = new File(JBother.profileDir + File.separator + "gcbookmarks");

    /**
     * Sets up the visual components of the bookmark dialog
     */
    public GroupChatBookmarks(Component parent) {
        super(BuddyList.getInstance().getContainerFrame(), "Group Chat Bookmarks");
        setTitle(resources.getString("groupChatBookmarksDialogTitle"));

        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        if( !bookmarksDir.isDirectory() )
        {
            bookmarksDir.mkdirs();
        }

        bookMarkList = new JList();
        bookMarkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookMarkList.setCellRenderer(new ListRenderer());

        leftPanel.setBackground(Color.WHITE);
        leftPanel.setLayout(new GridLayout(0, 1));
        setContentPane(container);

        deleteMenu.add(deleteItem);

        leftPanel.add(new JScrollPane(bookMarkList));
        leftPanel.setPreferredSize(new Dimension(120, 200));

        inputPanel.setBorder(BorderFactory.createTitledBorder(resources
                .getString("groupChatBookmarksDialogTitle")));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        inputPanel.setLayout(grid);
        c.anchor = GridBagConstraints.WEST;

        passBox.setFont(nickBox.getFont());

        createInputBox(resources.getString("room") + ":", roomBox);
        createInputBox(resources.getString("server") + ":", serverBox);
        createInputBox(resources.getString("nickname") + ":", nickBox);
        createInputBox(resources.getString("password") + ":", passBox);
        c.gridx = 1;
        c.gridy++;
        grid.setConstraints(auto, c);
        inputPanel.add(auto);

        container.add(leftPanel);
        container.add(Box.createRigidArea(new Dimension(5, 0)));

        //this is the space taker
        JLabel blankLabel = new JLabel("");
        c.weighty = 1;
        c.weightx = 1;
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy++;
        grid.setConstraints(blankLabel, c);
        inputPanel.add(blankLabel);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(saveButton);
        buttonPanel.add(openButton);
        buttonPanel.add(cancelButton);

        rightPanel.add(inputPanel);
        rightPanel.add(buttonPanel);
        container.add(rightPanel);

        setListeners();

        setSize(400, 200);
        pack();
        setLocationRelativeTo(parent);
        DialogTracker.addDialog(this, true, true);
    }

    public static void showDialog(String room, String user, String password) {
        int index = room.indexOf("@");
        String server = room.substring(index + 1);
        room = room.substring(0, index);
        index = user.indexOf("@");
        String nick = user.substring(0, index);

        GroupChatBookmarks gc = new GroupChatBookmarks(null);
        gc.load();
        gc.roomBox.setText(room);
        gc.serverBox.setText(server);
        gc.nickBox.setText(nick);
        gc.passBox.setText(password);
        gc.setVisible(true);

    }

    public void load()
    {
         new Thread( new CollectBookmarks(false) ).start();
    }

    /**
     * Adds event listeners to components in the bookmark window
     */
    private void setListeners() {
        addWindowListener(new WindowAdapter()
        {
            public void windowClosed(WindowEvent e )
            {
                bookmarks.clear();
                //dispose();
            }
        } );

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bookmarks.clear();
                dispose();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveBookmark();
            }
        });

        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openHandler();
            }
        });

        bookMarkList.addMouseListener(new RightClickListener());
        deleteItem.addActionListener(new DeleteListener());
    }

    /**
     * Writes a bookmark to the bookmarks file on disk
     */
    private void saveBookmark() {
        if (!checkData())
            return;

        String defaultString = roomBox.getText();

        String result = (String) JOptionPane.showInputDialog(this, resources
                .getString("enterBookmarkName"), resources
                .getString("saveBookmark"), JOptionPane.QUESTION_MESSAGE, null,
                null, defaultString);

        if (result == null || result.equals(""))
            return;

        if( pdBookmark )
        {
            for( int i = 0; i < bookmarks.size(); i++ )
            {
                Bookmark.Conference c = (Bookmark.Conference)bookmarks.get(i);
                if( c.getName().toLowerCase().equals(result.toLowerCase()))
                {
                    bookmark.removeConference(c);
                }
            }

            bookmark.addConference(result,roomBox.getText() + "@" + serverBox.getText(),
                nickBox.getText(), new String(passBox.getPassword()),auto.isSelected());

            new Thread(new SaveBookmark()).start();
        }
        else {

            
            try {
                File bm = new File( bookmarksDir, result + ".gcb" );
                FileWriter fw = new FileWriter(bm);
                PrintWriter out = new PrintWriter(fw);
                out.println(roomBox.getText());
                out.println(serverBox.getText());
                out.println(nickBox.getText());
                out.println(new String(passBox.getPassword()));
                out.println(auto.isSelected());
                fw.close();
            }
            catch( IOException ex ) { }
        }

        loadBookmarks();
    }

    class SaveBookmark implements Runnable
    {
        public void run()
        {
            try {
                pDataManager.setPrivateData(bookmark);
            }
            catch( Exception ex ) { com.valhalla.Logger.logException(ex);}
        }
    }

    /**
     * Makes sure all of the required information to save a bookmark is filled
     * in
     *
     * @return true if the information is completely filled out
     */
    private boolean checkData() {
        if (roomBox.getText().equals("") || serverBox.getText().equals("")
                || nickBox.getText().equals("")) {
            Standard.warningMessage(this, resources
                    .getString("groupChatBookmarksDialogTitle"), resources
                    .getString("enterAllFields"));
            return false;
        }

        return true;
    }

    /**
     * Deletes a bookmark from disk
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class DeleteListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(GroupChatBookmarks.this, resources
                    .getString("sureDelete"), resources
                    .getString("deleteBookmark"), JOptionPane.YES_NO_OPTION);

            if (result != 0)
                return;

            ListModel model = bookMarkList.getModel();

            try {
                Bookmark.Conference c = (Bookmark.Conference)model.getElementAt(bookMarkList
                        .getSelectedIndex());
                bookmark.removeConference(c);
            }
            catch(ClassCastException ex) {
                String f = (String)model.getElementAt(bookMarkList.getSelectedIndex()); 
                File bm = new File(bookmarksDir, f + ".gcb");
                bm.delete();
            }

            loadBookmarks();
            validate();

            new Thread(new SaveBookmark()).start();
        }
    }

    /**
     * Shows a menu on the bookmark items allowing you to delete one of them
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class RightClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            checkPop(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkPop(e);
        }

        public void mouseClicked(MouseEvent e) {
            checkPop(e);
        }

        public void checkPop(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int index = bookMarkList.locationToIndex(new Point(e.getX(), e
                        .getY()));
                bookMarkList.setSelectedIndex(index);
                deleteMenu.show(e.getComponent(), e.getX(), e.getY());
            } else {
                loadBookmark();
                if (e.getClickCount() >= 2)
                    openHandler();
            }
        }
    }

    /**
     * Opens a bookmark and fills out the fields with it's information
     */
    private void loadBookmark() {
        ListModel model = bookMarkList.getModel();
        try {
            Bookmark.Conference c = (Bookmark.Conference)model.getElementAt(bookMarkList.getSelectedIndex());

            String jid = c.getJid();
            int b = jid.indexOf("@");
            roomBox.setText(jid.substring(0,b));
            serverBox.setText(jid.substring(b+1));
            nickBox.setText(c.getNick());
            passBox.setText(c.getPassword());
            auto.setSelected(c.getAutojoin());
        }
        catch( ClassCastException ex ) {
            // it's a local bookmark
            String sel = (String)model.getElementAt(bookMarkList.getSelectedIndex());
            File bm = new File(bookmarksDir, sel + ".gcb");
            try {
                BufferedReader in = new BufferedReader(new FileReader(bm));
                roomBox.setText(in.readLine());
                serverBox.setText(in.readLine());
                nickBox.setText(in.readLine());
                passBox.setText(in.readLine());
                String a = in.readLine();
                if( a.equals("true") )
                {
                    auto.setSelected(true);
                }
                else {
                    auto.setSelected(false);
                }
                in.close();
            }
            catch( IOException iox ) { }
        }
    }

    /**
     * Opens a connection to a groupchat room
     */
    private void openHandler() {
        if (!checkData())
            return;

        if (BuddyList.getInstance().getTabFrame() != null
                && BuddyList.getInstance().getTabFrame().isRoomOpen(
                        roomBox.getText() + "@" + serverBox.getText())) {
            Standard.warningMessage(this, resources.getString("openBookmark"),
                    resources.getString("alreadyInRoom"));
            return;
        }

        ChatRoomPanel window = new ChatRoomPanel(roomBox.getText() + "@"
                + serverBox.getText(), nickBox.getText(), new String(passBox
                .getPassword()));
        window.startChat();
        dispose();
    }

    /**
     * Loads all the bookmarks and puts them in the list of bookmarks
     */
    private void loadBookmarks() {

        wait.setVisible(false);
        bookmarks.clear();
        
        if( pdBookmark )
        {
            if( bookmark != null )
            {
            Iterator i = bookmark.getConferences();

                while( i.hasNext() )
                {
                    bookmarks.add(i.next());
                }
            }
        }
        String marks[] = bookmarksDir.list();
        for( int i = 0; i < marks.length; i++ )
        {
            bookmarks.add(marks[i].replace(".gcb", ""));
        }

        bookMarkList.setListData(bookmarks.toArray());
        bookMarkList.validate();
    }

    class CollectBookmarks implements Runnable
    {
        private boolean autojoin = false;

        public CollectBookmarks( boolean autojoin )
        {
            this.autojoin = autojoin;
            if( !autojoin ) wait.setVisible(true);
        }

        public void run()
        {
            try {
                pDataManager = new PrivateDataManager(BuddyList.getInstance().getConnection());
                bookmark = (Bookmark)pDataManager.getPrivateData("storage", "storage:bookmarks");
                pdBookmark = true;

                if( !Settings.getInstance().getBoolean( "jbotherMucAdded" ) )
                {
                    // we save this bookmark locally, so as not to get anyone
                    // distrought about JBother doing things to their global
                    // private data storage without asking
                    String user = BuddyList.getInstance().getConnection().getUser();
                    int b = user.indexOf("@");
                    user = user.substring( 0, b );
                    File bm = new File(bookmarksDir, "jbother.gcb");
                    PrintWriter out = new PrintWriter(new FileWriter(bm));
                    out.println("jbother");
                    out.println("muc.jbother.org");
                    out.println(user);
                    out.println("");
                    out.println("true");
                    out.close();
                    Settings.getInstance().setBoolean( "jbotherMucAdded", true );
                }
            }
            catch( XMPPException ex ) {
                // do nothing, private data is not implemented
                com.valhalla.Logger.debug("Private Data is not supported, using local storage");
            }
            catch( Exception ex ) { 
                com.valhalla.Logger.logException(ex); 
            }

            if( !autojoin )
            {
                loadBookmarks();
            }
            else performAutoJoin();
        }
    }

    private void performAutoJoin()
    {
        if( bookmark != null ) {
            Iterator i = bookmark.getConferences();
            while(i.hasNext())
            {
                final Bookmark.Conference c=(Bookmark.Conference)i.next();

                if (c.getAutojoin()) {

                SwingUtilities.invokeLater( new Runnable()
                {
                    public void run()
                    {
                        ChatRoomPanel window = new ChatRoomPanel(c.getJid(), c.getNick(), c.getPassword());
                        window.startChat();
                    }
                    } );
                }

                try {
                    Thread.sleep(5);
                }
                catch( Exception ex ) { }
            }
        }

        String list[] = bookmarksDir.list();
        for( int i = 0; i < list.length; i ++ )
        {
            try {
                File bm = new File(bookmarksDir, list[i]);
                BufferedReader in = new BufferedReader(new FileReader(bm));
                final String room = in.readLine();
                final String server = in.readLine();
                final String nick = in.readLine();
                final String pass = in.readLine();
                boolean a = false;
                if( in.readLine().equals("true") )
                {
                    a = true;
                }

                final boolean tempAuto = a;
                in.close();

                if( a )
                {
                    SwingUtilities.invokeLater ( new Runnable()
                    {
                        public void run()
                        {
                            ChatRoomPanel window = new ChatRoomPanel(room + "@" + server,
                                nick, pass);
                            window.startChat();
                        }
                    } );

                    try {
                        Thread.sleep(5);
                    }
                    catch( Exception ex ) { }
                }
            }
            catch( IOException iox ) 
            { 
                com.valhalla.Logger.logException(iox);
            }
        }
    }

    public void autoJoin() {
        new Thread(new CollectBookmarks(true)).start();
    }

    /**
     * Creates a MJTextField with a label
     *
     * @param label
     *            the name of the the label
     * @param box
     *            the MJTextField
     */
    private void createInputBox(String label, Container box) {
        JLabel labelBox = new JLabel(label + "    ");

        c.gridy = row++;
        c.gridx = 0;
        grid.setConstraints(labelBox, c);
        inputPanel.add(labelBox);

        c.gridx = 1;
        grid.setConstraints(box, c);
        inputPanel.add(box);
    }
}

/**
 * Displays the different group chat bookmarks
 *
 * @author Adam Olsen
 * @version 1.0
 */

class ListRenderer extends JLabel implements ListCellRenderer {
    public ListRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        try {
            Bookmark.Conference c = (Bookmark.Conference)value;
            setText(c.getName());
        }
        catch( ClassCastException ex )
        {
            setText((String)value);
        }

        setBackground(isSelected ? list.getSelectionBackground() : list
                .getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list
                .getForeground());
        list.validate();

        return this;
    }
}
