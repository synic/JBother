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

package com.valhalla.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * WaitDialog This is just a frame with a JLabel in it and no buttons used for
 * displaying "Please wait..." dialogs and the like
 *
 * @author Adam Olsen
 * @version 1.0
 *
 */

public class WaitDialog extends JDialog {
    private Container parent;

    private String title;

    private JButton cancel = new JButton("Cancel");

    private WaitDialogListener listener = null;

    /**
     * Default constructor
     *
     * @param title
     *            the message to be displayed in the window's titlebar
     * @param string
     *            the message to be displayed in the main window
     */
    public WaitDialog(Frame parent, WaitDialogListener listener, String title) {
        super(parent, title);
        this.parent = parent;
        this.listener = listener;
        this.title = title;
        initComponents();
    }

    public WaitDialog(Dialog parent, WaitDialogListener listener, String title) {
        super(parent, title);
        this.parent = parent;
        this.listener = listener;
        this.title = title;
        initComponents();
    }

    public WaitDialog(String title){
        setTitle(title);
        this.title = title;
        initComponents();
    }

    public void setWaitListener( WaitDialogListener listener )
    {
        this.listener = listener;
        if(listener != null) cancel.setEnabled(true);
    }

    private void initComponents() {
        JPanel panel = (JPanel) getContentPane();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JProgressBar bar = new JProgressBar();
        bar.setPreferredSize(new Dimension(200, 20));
        bar.setIndeterminate(true);
        bar.setStringPainted(true);
        bar.setString(title);

        if (listener == null)
            cancel.setEnabled(false);

        panel.add(bar, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(cancel);
        buttons.add(Box.createHorizontalGlue());
        panel.add(buttons, BorderLayout.SOUTH);
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listener.cancel();
                dispose();
            }
        });

        pack();
        setLocationRelativeTo(parent);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
}