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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.text.*;

import javax.swing.*;

import com.valhalla.gui.CopyPasteContextMenu;
import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.Standard;
import com.valhalla.settings.Settings;

/**
 *  Allows the user to view the log of any contact Displays a dialog that allows
 *  you to view the log of any Jabber user you have contacted. You can view the
 *  log by date and time
 *
 *@author     Adam Olsen
 *@created    April 20, 2005
 *@version    1.0
 */
public class LogViewerDialog extends JFrame {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private String logDirectory;

    private JList logList = new JList();

    private LogListModel model = new LogListModel();

    private JPanel container = new JPanel();

    private JPanel rightPanel = new JPanel();

    private ConversationArea logView;

    private File logDirectoryFile;

    private JButton closeButton = new JButton(resources.getString("closeButton")), clearButton = new JButton(resources.getString("clearButton"));

    private JSplitPane splitPane;

    private File fileList[];

    private LogViewerCaller caller;

    private JPopupMenu popMenu = new JPopupMenu();

    private JMenuItem delete = new JMenuItem(resources.getString("deleteButton"));

    private JScrollPane scrollPane;

    private JButton searchButton = new JButton(resources.getString("search"));

    private JTextField searchField = new JTextField();


    /**
     *  Default constructor Can be passed a ChatRoomPanel, HeadlineWindow, and a
     *  ChatPanel as the caller
     *
     *@param  caller     the object that called this dialog
     *@param  entryName  the user who's log is to be viewed
     */
    public LogViewerDialog(LogViewerCaller caller, String entryName) {
        super("Log Viewer (" + entryName + ")");
        setTitle(resources.getString("logViewer") + " (" + entryName + ")");

        setIconImage(Standard.getImage("frameicon.png"));
        logList.setModel(model);

        if (caller != null) {
            this.caller = caller;
        }

        logDirectory = JBother.profileDir + File.separatorChar + "logs"
                 + File.separatorChar
                 + entryName.replaceAll("@", "_at_").replaceAll("\\/", "-");

        logDirectoryFile = new File(logDirectory);

        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        setContentPane(container);

        logView = new ConversationArea();
        logView.setEmoticons(false);
        popMenu.add(delete);

        logView.setPreferredSize(new Dimension(500, 350));
        CopyPasteContextMenu.registerComponent(logView.getTextPane());

        rightPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout(2, 2));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);
        rightPanel.add(topPanel, BorderLayout.NORTH);


        rightPanel.add(logView, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        SearchActionListener searchListener = new SearchActionListener();
        searchField.addActionListener(searchListener);
        searchButton.addActionListener(searchListener);

        loadLogFiles();
        scrollPane = new JScrollPane(logList);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane,
                rightPanel);
        splitPane.setDividerLocation(150);
        splitPane.setOneTouchExpandable(true);

        container.add(splitPane);
        container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setUpListeners();

        pack();
        Standard.cascadePlacement(this);
        DialogTracker.addDialog(this, false, true);

        if (logList.getModel().getSize() >= 1) {
            setVisible(true);
        } else {
            Standard.warningMessage(null, resources.getString("logViewer"),
                    resources.getString("noLogData"));
            DialogTracker.removeDialog(this);
        }
    }

    /**
     *  Returns a formatted date string that is used in many places in JBother
     *
     *@return    the formatted date and time
     */
    public static String getDateName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String date = formatter.format(new Date());
        return date;
    }


    /**
     *  Sets up the listeners for the various events
     */
    private void setUpListeners() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        closeButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    DialogTracker.removeDialog(LogViewerDialog.this);
                }
            });

        clearButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    clearHandler();
                }
            });

        delete.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int result = JOptionPane.showConfirmDialog(null, resources.getString("sureDeleteItem"), resources.getString("logViewer"), JOptionPane.YES_NO_OPTION);

                    if (result == JOptionPane.YES_OPTION) {
                        File file = (File) logList.getSelectedValue();
                        if (file != null) {
                            com.valhalla.Logger.debug("deleting " + file.getPath());
                            if (file.delete()) {
                                model.removeLog(file);
                            }
                        }
                    }
                }
            });

        logList.addMouseListener(new LogListMouseListener());

        logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logList.setCellRenderer(new LogListRenderer());
    }


    /**
     *  Listens for someone to click on an entry date and populates the textArea
     *  with the log data
     *
     *@author     Adam Olsen
     *@created    April 20, 2005
     *@version    1.0
     */
    private class LogListMouseListener extends MouseAdapter {
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
            File file = (File) logList.getSelectedValue();
            logView.setText("");
            logView.append(getFileContents(file));

            new java.util.Timer().schedule(new Scroller(logView.getTextPane()), 100);

            validate();
            checkPop(e);

        }

        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        private void checkPop(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int index = logList.locationToIndex(new Point(e.getX(), e.getY()));
                logList.setSelectedIndex(index);

                if (logList.getSelectedValue() != null) {
                    popMenu.show(logList, e.getX(), e.getY());
                }
            }
        }
    }

    class Scroller extends TimerTask
    {
        JTextPane pane;
        public Scroller(JTextPane pane) { this.pane = pane; }
        public void run()
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    pane.setCaretPosition(0);
                }
            });

        }
    }


    /**
     *  Clears all log data (deletes all log entries)
     */
    private void clearHandler() {
        int result = JOptionPane.showConfirmDialog(null, resources.getString("sureClearLog"), resources.getString("clearLogs"),
                JOptionPane.YES_NO_OPTION);

        if (result == 0) {
            if (caller != null) {
                caller.closeLog();
                caller.startLog();
            }

            for (int i = 0; i < fileList.length; i++) {
                fileList[i].delete();
            }
            DialogTracker.removeDialog(this);
        }
    }


    /**
     *  Returns a list of all available log entries
     *
     */
    private void loadLogFiles() {
        if (!logDirectoryFile.isDirectory()) {
            return;
        }

        // opens the directory and lists the files
        fileList = logDirectoryFile.listFiles(new LogFileFilter());

        logView.setText("");
        Arrays.sort(fileList, Collections.reverseOrder());

        if (!searchField.getText().equals("")) {
            ArrayList list = new ArrayList();
            for (int i = 0; i < fileList.length; i++) {
                if (fileContainsText(fileList[i], searchField.getText())) {
                    list.add(fileList[i]);
                }
            }

            fileList = (File[]) list.toArray(new File[list.size()]);
        }

        if (fileList.length == 0) {
            logView.setText("No logs found");
            model.setLogs(fileList);
            validate();
            return;
        }

        logView.append(getFileContents(fileList[0]));

        new java.util.Timer().schedule(new Scroller(logView.getTextPane()), 100);

        logList.setSelectedIndex(0);
        model.setLogs(fileList);
        validate();
    }


    /**
     *  Description of the Class
     *
     *@author     synic
     *@created    April 20, 2005
     */
    class SearchActionListener implements ActionListener {
        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            loadLogFiles();
        }
    }

    private String getFileContents(File file)
    {
        String html = "";
        try {
            FileInputStream in = new FileInputStream(file);
            byte[] contents = new byte[in.available()];
            in.read(contents);
            in.close();
            String logEnc =
                    Settings.getInstance().getProperty("keepLogsEncoding");
            try {
                if (logEnc != null) {
                    html = new String(contents, logEnc);
                } else {
                    html = new String(contents);
                }
            } catch (UnsupportedEncodingException ex) {
            } catch (NullPointerException ex) {
            }

        } catch (FileNotFoundException fnfe) {
        } catch (IOException ioe) {
        }

        return html;
    }

    /**
     *  Description of the Method
     *
     *@param  file  Description of the Parameter
     *@param  text  Description of the Parameter
     *@return       Description of the Return Value
     */
    private boolean fileContainsText(File file, String text) {
        text = text.toLowerCase();
        String html = getFileContents(file);
        html = html.toLowerCase();
        if (html.indexOf(text) > -1) {
            return true;
        } else {
            return false;
        }
    }


    /**
     *  The model that represents the list of buddies in the room
     *
     *@author     Adam Olsen
     *@created    April 20, 2005
     *@version    1.0
     */
    class LogListModel extends AbstractListModel {
        private Vector logs = new Vector();


        /**
         *  Sets the logs attribute of the LogListModel object
         *
         *@param  list  The new logs value
         */
        public void setLogs(File[] list) {
            logs.clear();
            for (int i = 0; i < list.length; i++) {
                logs.add(list[i]);
            }

            fireChanged();
        }


        /**
         *@return    the number of elements in the list
         */
        public int getSize() {
            return logs.size();
        }


        /**
         *@param  row  the element you want to get
         *@return      the Object at <tt>row</tt>
         */
        public Object getElementAt(int row) {
            return logs.get(row);
        }


        /**
         *@param  log    The feature to be added to the Log attribute
         */
        public void addLog(File log) {
            logs.add(log);
            fireChanged();
        }


        /**
         *  Removes a buddy from the list
         *
         *@param  log  Description of the Parameter
         */
        public void removeLog(File log) {
            logs.remove(log);
            fireChanged();
        }



        /**
         *  Fires a change of the list
         */
        private synchronized void fireChanged() {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        fireContentsChanged(LogListModel.this, 0, logs.size());
                        logList.validate();
                    }
                });
        }
    }
}

/**
 *  Displays each log entry as a date and time
 *
 *@author     Adam Olsen
 *@created    April 20, 2005
 *@version    1.0
 */

class LogListRenderer extends JLabel implements ListCellRenderer {


    /**
     *  Sets the background to opaque
     */
    public LogListRenderer() {
        setOpaque(true);
    }


    /**
     *  Renders each entry
     *
     *@param  list          Description of the Parameter
     *@param  value         Description of the Parameter
     *@param  index         Description of the Parameter
     *@param  isSelected    Description of the Parameter
     *@param  cellHasFocus  Description of the Parameter
     *@return               The listCellRendererComponent value
     */
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        File file = (File) value;

        String dateString[] = file.toString().replaceAll(
                ".*\\" + File.separatorChar, "")
                .replaceAll("\\.log", "").split("\\-");

        try {
            setText(dateString[1] + "-" + dateString[2] + "-" + dateString[0]
                     + " " + dateString[3] + ":" + dateString[4] + ":"
                     + dateString[5]);
        } catch (Exception e) {
            setText("Invalid Date");
        }

        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        list.validate();

        return this;
    }
}

/**
 *  A filter to only accept html files
 *
 *@author     Adam Olsen
 *@created    April 20, 2005
 *@version    1.0
 */

class LogFileFilter implements FileFilter {

    // accept html files

    /**
     *  Description of the Method
     *
     *@param  f  Description of the Parameter
     *@return    Description of the Return Value
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return false;
        }

        if (f.length() <= 0.0) {
            return false;
        }

        String extension = Utils.getExtension(f);

        if (extension != null) {
            if (extension.equals(Utils.log)) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }
}
