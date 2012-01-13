/*
 * Copyright (C) 2003 Adam Olsen
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 1, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

package com.valhalla.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

public class CopyPasteContextMenu extends JPopupMenu {
    private JMenuItem cut = new JMenuItem("Cut");

    private JMenuItem copy = new JMenuItem("Copy");

    private JMenuItem paste = new JMenuItem("Paste");

    private JMenuItem delete = new JMenuItem("Delete");

    private static CopyPasteContextMenu m;

    private static JTextComponent current = null;

    private CMouseListener mouseListener = new CMouseListener();

    private Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();

    private CopyPasteContextMenu() {
        add(cut);
        add(copy);
        add(paste);
        add(delete);

        ItemListener l = new ItemListener();
        cut.addActionListener(l);
        copy.addActionListener(l);
        paste.addActionListener(l);
        delete.addActionListener(l);
    }

    public static CopyPasteContextMenu newInstance() {
        m = new CopyPasteContextMenu();
        return m;
    }

    private class ItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == cut)
                current.cut();
            else if (e.getSource() == copy)
                current.copy();
            else if (e.getSource() == paste)
                current.paste();
            else if (e.getSource() == delete)
                current.replaceSelection("");
        }
    }

    private void disableEditItems() {
        cut.setEnabled(false);
        paste.setEnabled(false);
        delete.setEnabled(false);
    }

    private void enableEditItems() {
        cut.setEnabled(true);
        paste.setEnabled(true);
        delete.setEnabled(true);
    }

    public static void registerComponent(JTextComponent comp) {
        if (m == null)
            m = new CopyPasteContextMenu();
        comp.addMouseListener(m.mouseListener);
    }

    private class CMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            checkPop(e);
        }

        public void mouseClicked(MouseEvent e) {
            checkPop(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkPop(e);
        }

        private void checkPop(MouseEvent e) {
            if (e.isPopupTrigger()) {
                current = (JTextComponent) e.getSource();
                if (!current.isEditable())
                    m.disableEditItems();
                else
                    m.enableEditItems();

                if (m.board.getContents(current) == null)
                    m.paste.setEnabled(false);
                else if (current.isEditable())
                    m.paste.setEnabled(true);

                String text = current.getSelectedText();

                if (text == null) {
                    m.copy.setEnabled(false);
                    m.cut.setEnabled(false);
                } else {
                    m.copy.setEnabled(true);
                    if (current.isEditable())
                        m.cut.setEnabled(true);
                }

                if (text != null) {
                    e.consume();
                    m.show(current, e.getX(), e.getY());
                }
            }
        }
    }
}