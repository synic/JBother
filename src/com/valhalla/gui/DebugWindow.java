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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class DebugWindow extends JFrame {
    private JButton ok = new JButton("OK");

    private JButton clear = new JButton("Clear");

    private MJTextArea log = new MJTextArea();

    private JScrollPane pane = new JScrollPane(log);

    public DebugWindow() {
        super("Debug Window");

        JPanel main = (JPanel) getContentPane();
        main.setBorder(BorderFactory.createTitledBorder("Debug Window"));
        main.setLayout(new BorderLayout());
        log.setEditable(false);

        main.add(pane, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(clear);
        buttons.add(ok);
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        log.setFont(new Font("Default", Font.PLAIN, 9));

        main.add(buttons, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.setText("");
            }
        });

        pack();
        setSize(400, 300);
        setLocationRelativeTo(null);
    }

    public void append(String text) {
        JScrollBar bar = pane.getVerticalScrollBar();

        boolean end = bar.getValue()
                - (bar.getMaximum() - bar.getModel().getExtent()) >= -16;
        Point p = pane.getViewport().getViewPosition();

        boolean scrFlag = bar.isVisible();
        p.y += 50; // just so it's not the first line (might scroll a bit)
        int pos = log.viewToModel(p);

        if (pos < 0)
            pos = 0;

        try {

            log.setText(log.getText() + text + "\n");

            if (!end && scrFlag)
                log.setCaretPosition(pos);
            else
                log.setCaretPosition(log.getText().length());
            pane.repaint();
        } catch (Exception e) {
            //com.valhalla.Logger.debug( "ShowHandler.class Exception: " +
            // e.toString() );
        }
    }
}