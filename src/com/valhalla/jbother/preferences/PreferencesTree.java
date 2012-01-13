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
package com.valhalla.jbother.preferences;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Displays the different preference panels available in a JTree
 * 
 * @author Adam Olsen
 * @created March 3, 2005
 * @version 1.0
 */
public class PreferencesTree extends JPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private PreferencesDialog prefsDialog;

    private JTree tree;

    private DefaultMutableTreeNode root, topNode, pluginsNode;

    private JScrollPane scroll = new JScrollPane();

    /**
     * Sets up the preferences tree
     * 
     * @param prefsDialog
     *            the enclosing preferences dialog
     */
    public PreferencesTree(PreferencesDialog prefsDialog) {
        this.prefsDialog = prefsDialog;

        setupTree();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(scroll);
        setPreferredSize(new Dimension(150, 350));
    }

    /**
     * Description of the Method
     */
    public void updateUI() {
        super.updateUI();

        if (resources != null)
            setupTree();
    }

    /**
     * Sets the tree up
     */
    private void setupTree() {
        root = new DefaultMutableTreeNode(resources.getString("preferences"));
        topNode = new DefaultMutableTreeNode(resources.getString("preferences"));
        root.add(topNode);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree,
                    Object value, boolean sel, boolean expanded, boolean leaf,
                    int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded,
                        leaf, row, hasFocus);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                setText(((String) node.getUserObject()).replaceAll("^a\\d+ ",
                        ""));

                return this;
            }
        };
        renderer.setLeafIcon(null);

        Iterator i = prefsDialog.getPanels().keySet().iterator();

        while (i.hasNext()) {
            topNode.add(new DefaultMutableTreeNode((String) i.next()));
        }

        TreeMap plugins = PreferencesDialog.getPluginPanels();
        if (plugins.size() > 0) {
            pluginsNode = new DefaultMutableTreeNode(resources
                    .getString("pluginPreferences"));
            i = plugins.keySet().iterator();
            while (i.hasNext()) {
                pluginsNode.add(new DefaultMutableTreeNode((String) i.next()));
            }
            root.add(pluginsNode);
        }

        tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        for (int a = 0; a < tree.getRowCount(); a++) {
            tree.expandRow(a);
        }

        tree.setSelectionRow(1);
        tree.setCellRenderer(renderer);
        tree.addMouseListener(new DoubleClickListener());
        scroll.setViewportView(tree);
        validate();
    }

    /**
     * Gets the index of the selected item
     * 
     * @return the selected index
     */
    public int getSelectedIndex() {
        TreePath path = tree.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
                .getLastPathComponent();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        return model.getIndexOfChild(root, node);
    }

    /**
     * Gets the number of rows
     * 
     * @return the row count
     */
    public int getRowCount() {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        return model.getChildCount(root);
    }

    /**
     * Listens for a click on one of the tree items
     * 
     * @author synic
     * @created March 3, 2005
     */
    class DoubleClickListener extends MouseAdapter {
        /**
         * Description of the Method
         * 
         * @param e
         *            Description of the Parameter
         */
        public void mouseClicked(MouseEvent e) {
            JTree tree = (JTree) e.getComponent();
            TreePath path = tree.getSelectionPath();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
                    .getLastPathComponent();
            String string = (String) node.getUserObject();
            if (string != null) {
                prefsDialog.switchPanel(string);
            }
        }
    }
}

