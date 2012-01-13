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
package com.valhalla.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * This class allows for the creation of a non-modal option dialog
 *
 * @author Adam Olsen
 * @created October 22, 2004
 * @version 1.0
 */
public class NMOptionDialog extends JDialog {
    private JPanel mainPanel;

    private ArrayList listeners = new ArrayList();

    private JPanel buttonPanel = new JPanel();

    private ButtonListener listener = new ButtonListener();

    private Hashtable buttons = new Hashtable();

    /**
     * Information type
     */
    public static final int INFORMATION = 1;

    /**
     * warning type
     */
    public static final int WARNING = 2;

    /**
     * question type
     */
    public static final int QUESTION = 3;

    /**
     * error type
     */
    public static final int ERROR = 4;

    /**
     * Creates the non-modal option dialog
     *
     * @param parent
     *            the dialog's parent
     * @param title
     *            the text to use in the title bar of the dialog
     * @param message
     *            the message to display in the dialog's JLabel
     * @param icon
     *            the icon to display in the icon section
     */
    public NMOptionDialog(JFrame parent, String title, String message, int icon) {
        super(parent, title);
        mainPanel = (JPanel) getContentPane();
        mainPanel.setLayout(new BorderLayout());

        message = message.replaceAll("\n", "<br>");
        JLabel messageLabel = new JLabel("<html>" + message + "</html>",
                SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        mainPanel.add(messageLabel, BorderLayout.CENTER);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(buttonPanel);
        bottomPanel.add(Box.createHorizontalGlue());

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        JLabel iconLabel = new JLabel(getIconFromNum(icon));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        mainPanel.add(iconLabel, BorderLayout.WEST);

        pack();
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            }
        });
    }

    /**
     * Constructor that creates an NMOptionDialog with an Information icon
     *
     * @param parent
     *            the dialog's parent
     * @param title
     *            the text to use in the title bar of the dialog
     * @param message
     *            the message to display in the dialog's JLabel
     */
    public NMOptionDialog(JFrame parent, String title, String message) {
        this(parent, title, message, INFORMATION);
    }

    /**
     * Returns the icon corresponding to the number value
     *
     * @param num
     *            the number of the icon to get
     * @return the icon corresponding to the specified number
     */
    public Icon getIconFromNum(int num) {
        String name = "OptionPane.";
        if (num == ERROR) {
            name += "error";
        } else if (num == INFORMATION) {
            name += "information";
        } else if (num == WARNING) {
            name += "warning";
        } else if (num == QUESTION) {
            name += "question";
        }

        name += "Icon";

        return UIManager.getIcon(name);
    }

    /**
     * Creates a message dialog with only an "OK" button
     *
     * @param parent
     *            the dialog' parent
     * @param title
     *            the text to use in the title bar of the dialog
     * @param message
     *            the message to display in the dialog's label
     * @return a NMOptionDialog with only an OK button
     */
    public static NMOptionDialog createMessageDialog(JFrame parent,
            String title, String message) {
        NMOptionDialog d = new NMOptionDialog(parent, title, message,
                INFORMATION);
        d.addButton("OK", 1);
        d.setVisible(true);
        return d;
    }

    /**
     * Adds a button to the dialog
     *
     * @param text
     *            the text to put on the button
     * @param num
     *            the number of the button
     */
    public void addButton(String text, int num) {
        JButton button = new JButton(text);
        buttonPanel.add(button);
        button.addActionListener(listener);
        pack();
        validate();
        repaint();

        buttons.put(text, new Integer(num));
    }

    /**
     * Adds a listener to this dialog
     *
     * @param l
     *            The feature to be added to the OptionListener attribute
     */
    public void addOptionListener(NMOptionListener l) {
        listeners.add(l);
    }

    /**
     * Called by the NMOption panel buttons
     *
     * @author synic
     * @created October 22, 2004
     */
    class ButtonListener implements ActionListener {
        /**
         * Called by the NMOption panel buttons
         *
         * @param e
         *            the event
         */
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            String name = button.getText();
            Integer num = (Integer) buttons.get(name);

            for (int i = 0; i < listeners.size(); i++) {
                NMOptionListener l = (NMOptionListener) listeners.get(i);
                l.buttonClicked(num.intValue());
            }

            dispose();
        }
    }
}

