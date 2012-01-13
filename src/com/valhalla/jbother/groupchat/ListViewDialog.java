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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.*;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.*;

/**
 * Allows for viewing the different lists of JIDs in MUC
 *
 * @author Adam Olsen
 */
class ListViewDialog extends JDialog {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    public static final int TYPE_ADMIN = 1;

    public static final int TYPE_MEMBERS = 3;

    public static final int TYPE_MODERATORS = 2;

    public static final int TYPE_OUTCASTS = 4;

    public static final int TYPE_OWNERS = 5;

    public static final int TYPE_PARTICIPANTS = 6;

    private ChatRoomPanel panel;

    private JPanel container;

    private ListViewModel model = new ListViewModel();

    private JTable table = new JTable(model);

    private JPanel buttonPanel = new JPanel();

    private JButton okButton = new JButton(resources.getString("okButton"));

    private JButton addButton = new JButton(resources.getString("addButton"));

    private JButton banButton = new JButton(resources
            .getString("unbanSelected"));

    private Vector listeners = new Vector();

    private int type;

    private JScrollPane pane = new JScrollPane(table);

    private String title = "";

    public ListViewDialog(ChatRoomPanel panel, final int type) {
        super(BuddyList.getInstance().getTabFrame());
        this.panel = panel;
        this.type = type;

        if (type == TYPE_ADMIN)
            title = resources.getString("viewAdmins");
        else if (type == TYPE_MEMBERS)
            title = resources.getString("viewMembers");
        else if (type == TYPE_MODERATORS)
            title = resources.getString("viewModerators");
        else if (type == TYPE_OUTCASTS)
            title = resources.getString("viewOutcasts");
        else if (type == TYPE_OWNERS)
            title = resources.getString("viewOwners");
        else if (type == TYPE_PARTICIPANTS)
            title = resources.getString("viewParticipants");

        setTitle(title);

        container = (JPanel) getContentPane();
        container.setBorder(BorderFactory.createTitledBorder(title));
        container.setLayout(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        p.add(pane, BorderLayout.CENTER);
        pane.getViewport().setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        container.add(p, BorderLayout.CENTER);
        pane.setBorder(BorderFactory.createEtchedBorder());

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.add(Box.createHorizontalGlue());

        if (type != TYPE_OUTCASTS)
            banButton.setText(resources.getString("revokeSelected"));
        buttonPanel.add(banButton);
        if (type != TYPE_MODERATORS && type != TYPE_PARTICIPANTS)
            buttonPanel.add(addButton);
        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalGlue());
        container.add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        banButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String err = resources.getString("couldNotUnban");
                if (type != TYPE_OUTCASTS)
                    err = resources.getString("couldNotRevoke");
                table.setEnabled(false);
                banButton.setEnabled(false);
                Thread thread = new Thread(new UnbanThread(type, err));
                thread.start();
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addHandler();
            }
        });

        model.setWidths();
        pack();
        setSize(new Dimension(460, 400));
        setLocationRelativeTo(panel);

        Thread thread = new Thread(new GetListThread());
        thread.start();
    }

    private class UnbanThread implements Runnable {
        private String err;

        private int type;

        public UnbanThread(int type, String err) {
            this.type = type;
            this.err = err;
        }

        public void run() {
            String message = null;
            try {
                MultiUserChat chat = panel.getChat();
                int rows[] = table.getSelectedRows();
                ArrayList users = new ArrayList();
                for (int i = 0; i < rows.length; i++) {
                    String jid = (String) model.getValueAt(rows[i], 0);

                    if (type == TYPE_MODERATORS || type == TYPE_PARTICIPANTS) {
                        jid = (String) model.getValueAt(rows[i], 1);
                        if (jid == null)
                            continue;
                    }

                    users.add(jid);
                }

                if (type == TYPE_OUTCASTS)
                    chat.grantMembership(users);
                else if (type == TYPE_ADMIN)
                    chat.revokeAdmin(users);
                else if (type == TYPE_MEMBERS)
                    chat.revokeMembership(users);
                else if (type == TYPE_OWNERS)
                    chat.revokeOwnership(users);
                else if (type == TYPE_PARTICIPANTS)
                    chat.revokeVoice(users);
                else if (type == TYPE_MODERATORS)
                    chat.revokeModerator(users);

            } catch (XMPPException ex) {
                message = ex.getMessage();
                if (ex.getXMPPError() != null) {
                    message = resources.getString("xmppError"
                            + ex.getXMPPError().getCode());
                }

                Standard.warningMessage(ListViewDialog.this, resources
                        .getString("viewOutcasts"), err + ": " + message);
            }

            if (message == null) {
                Thread thread = new Thread(new GetListThread());
                thread.start();
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    banButton.setEnabled(true);
                    table.setEnabled(true);
                }
            });
        }
    }

    private void addHandler() {
        String p = resources.getString("pleaseEnterJid");

        String result = (String) JOptionPane.showInputDialog(this, p, title,
                JOptionPane.QUESTION_MESSAGE, null, null, "");

        if (result.equals("") || result == null)
            return;
        Thread thread = new Thread(new AddThread(result));
        thread.start();
    }

    private class AddThread implements Runnable {
        String res;

        public AddThread(String jid) {
            this.res = jid;
        }

        public void run() {
            String message = null;
            String title = "";
            if (type == TYPE_ADMIN)
                title = resources.getString("grantAdmin");
            else if (type == TYPE_MEMBERS)
                title = resources.getString("grantMembership");
            else if (type == TYPE_OWNERS)
                title = resources.getString("grantOwnership");
            else if (type == TYPE_PARTICIPANTS)
                title = resources.getString("grantVoice");

            try {
                MultiUserChat chat = panel.getChat();
                if (type == TYPE_OUTCASTS)
                    chat.banUser(res, "None");
                else if (type == TYPE_ADMIN)
                    chat.grantAdmin(res);
                else if (type == TYPE_MEMBERS)
                    chat.grantMembership(res);
                else if (type == TYPE_OWNERS)
                    chat.grantOwnership(res);
            } catch (XMPPException ex) {
                message = ex.getMessage();
                if (ex.getXMPPError() != null) {
                    message = resources.getString("xmppError"
                            + ex.getXMPPError().getCode());
                }

                Standard.warningMessage(ListViewDialog.this, title, title + ": "
                        + message);
            }

            if (message == null) {
                Thread thread = new Thread(new GetListThread());
                thread.start();
            }
        }
    }

    private class GetListThread implements Runnable {
        public void run() {
            MultiUserChat chat = panel.getChat();
            Collection col = new Vector();
            try {
                if (type == TYPE_ADMIN)
                    col = chat.getAdmins();
                else if (type == TYPE_MEMBERS)
                    col = chat.getMembers();
                else if (type == TYPE_MODERATORS)
                    col = chat.getModerators();
                else if (type == TYPE_OUTCASTS)
                    col = chat.getOutcasts();
                else if (type == TYPE_OWNERS)
                    col = chat.getOwners();
                else if (type == TYPE_PARTICIPANTS)
                    col = chat.getParticipants();
            } catch (XMPPException ex) {
                String error = ex.getMessage();
                if (ex.getXMPPError() != null)
                    error = resources.getString("couldNotFetchList")
                            + ": "
                            + resources.getString("xmppError"
                                    + ex.getXMPPError().getCode());
                panel.serverErrorMessage(error);
                dispose();
                return;
            }

            final Collection temp = col;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateList(temp);
                    setVisible(true);
                }
            });
        }
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public JPanel getButtonPanel() {
        return buttonPanel;
    }

    public void updateList(Collection col) {
        model.clear();
        model.setItems(col);
    }

    private class ListViewModel extends AbstractTableModel {
        private Collection items = new Vector();

        private String names[] = new String[] { "JID", "Nick", "Role",
                "Affiliation" };

        public ListViewModel() {
        }

        public void setWidths() {
            TableColumn column = null;

            for (int i = 0; i < getColumnCount(); i++) {
                column = table.getColumnModel().getColumn(i);
                if (i == 0) {
                    column.setPreferredWidth(300);
                } else if (i == 3)
                    column.setPreferredWidth(150);
                else
                    column.setPreferredWidth(100);
            }

            table.validate();
        }

        public void clear() {
            items.clear();
            table.tableChanged(new TableModelEvent(this));
        }

        public void setItems(Collection col) {
            this.items = col;
            table.tableChanged(new TableModelEvent(this));
        }

        public int getSize() {
            return items.size();
        }

        public String getColumnName(int index) {
            return names[index];
        }

        public int getColumnCount() {
            return names.length;
        }

        public int getRowCount() {
            return items.size();
        }

        public Object getValueAt(int row, int column) {
            Object[] array = items.toArray();
            Object item = array[row];

            if (item instanceof Affiliate) {
                Affiliate aff = (Affiliate) item;
                if (column == 0)
                    return aff.getJid();
                else if (column == 1) {
                    if (aff.getNick() == null)
                        return "n/a";
                    else
                        return aff.getNick();
                } else if (column == 2) {
                    if (aff.getRole() == null)
                        return "none";
                    else
                        return aff.getRole();
                } else if (column == 3) {
                    if (aff.getAffiliation() == null)
                        return "none";
                    else
                        return aff.getAffiliation();
                }
            } else {
                Occupant aff = (Occupant) item;
                if (column == 0)
                    return aff.getJid();
                else if (column == 1) {
                    if (aff.getNick() == null)
                        return "n/a";
                    else
                        return aff.getNick();
                } else if (column == 2) {
                    if (aff.getRole() == null)
                        return "none";
                    else
                        return aff.getRole();
                } else if (column == 3) {
                    if (aff.getAffiliation() == null)
                        return "none";
                    else
                        return aff.getAffiliation();
                }
            }

            return "none";
        }
    }
}