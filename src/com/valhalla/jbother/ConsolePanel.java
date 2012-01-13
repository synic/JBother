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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.event.*;

import com.valhalla.gui.CopyPasteContextMenu;
import com.valhalla.gui.MJTextArea;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.smack.Blank;
import com.valhalla.jbother.menus.ConversationPopupMenu;
import com.valhalla.settings.Settings;

/**
 * Handles XML packet exchange between client and server.
 *
 * @author Andrey Zakirov
 * @created March 2, 2005
 * @version 0.1
 */

public class ConsolePanel extends ConversationPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JSplitPane container;

    private JPanel buttonPanel = new JPanel();

    private JPanel scrollPanel = new JPanel(new GridLayout(1, 0));

    private boolean divSetUp = false;

    private ConversationPopupMenu popMenu = new ConversationPopupMenu(this,
            conversationArea);

    private DividerListener dividerListener = new DividerListener();
    private static ConsolePanel instance = null;

    /**
     * Sets up the ConsolePanel - creates all visual components and adds event
     * listeners
     *
     * @param buddy
     *            the buddy to associate with
     */
    private ConsolePanel(BuddyStatus buddy) {
        super(buddy);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textEntryArea.setLineWrap(true);
        textEntryArea.setWrapStyleWord(true);

        scrollPanel.add(conversationArea);

        container = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPanel,
                new JScrollPane(textEntryArea));
        container.setResizeWeight(1);

        JPanel containerPanel = new JPanel();
        containerPanel
                .setLayout(new BoxLayout(containerPanel, BoxLayout.X_AXIS));
        containerPanel.add(container);

        conversationArea.getTextPane().addMouseListener(new RightClickListener(popMenu));
        conversationArea.setEmoticons(false);
        CopyPasteContextMenu.registerComponent(conversationArea.getTextPane());
        popMenu.disableBlock();

        add(containerPanel);

        textEntryArea.grabFocus();

        addListeners();
    }

    /**
     * Destroys the dialog, disposes the containing frame if there is one and
     * removes the panel from the TabFrame if required.
     */
    public void closeHandler() {
        closeLog();
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }

        if (buddy != null) {
            com.valhalla.Logger.debug("Closing ConversationPanel for "
                    + buddy.getUser());
            buddy.setConversation(null);
        }

        if (Settings.getInstance().getBoolean("useTabbedWindow")) {
            BuddyList.getInstance().removeTabPanel(this);
        }

        MessageDelegator.getInstance().removePanel(this);
        instance = null;
    }


    public static ConsolePanel getInstance(BuddyStatus buddy)
    {
        if( instance == null ) instance = new ConsolePanel(buddy);
        return instance;
    }

    public static ConsolePanel getInstance()
    {
        return instance;
    }

    private String getMessageTemplate() {
        return "<message id=\"\" to=\"\" type=\"\"><body></body></message>";
    }

    /**
     * @return the input area of this panel
     */
    public JComponent getInputComponent() {
        return textEntryArea;
    }

    /**
     * Sets up the Divider
     */
    public void setUpDivider() {
        String modifier = "";
        if (Settings.getInstance().getBoolean("useTabbedWindow")) {
            modifier = "tabbed_";
        }

        String stringHeight = Settings.getInstance().getProperty(
                modifier + "XMLconversationWindowHeight", "100");

        // set up the divider location from settings
        String divLocString = Settings.getInstance().getProperty(
                modifier + "XMLconversationWindowDividerLocation", "50");
        int divLoc = 50;

        //com.valhalla.Logger.debug( "divlocstring is " + divLocString );

        try {
            if (divLocString != null) {
                divLoc = Integer.parseInt(divLocString);
            } else {
                Settings
                        .getInstance()
                        .setProperty(
                        modifier
                        + "XMLconversationWindowDividerLocation",
                        "30");
            }
        } catch (NumberFormatException ex) {
            com.valhalla.Logger.logException( ex );
        }

        //com.valhalla.Logger.debug( "getting divider to " + divLoc );
        container.setDividerLocation(divLoc);

        if (!divSetUp) {
            container.addPropertyChangeListener("lastDividerLocation",
                    dividerListener);
            divSetUp = true;
        }
    }

    /**
     * Listens for the user to move the divider, and saves it's location
     *
     * @author Adam Olsen
     * @version 1.0
     */
    private class DividerListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String modifier = "";
            if (Settings.getInstance().getBoolean("useTabbedWindow")) {
                modifier = "tabbed_";
            }

            if (e.getOldValue().toString().equals("-1")) {
                return;
            }

            //com.valhalla.Logger.debug( "Setting divider to " + e.getOldValue() );
            Settings.getInstance().setProperty(
                    modifier + "XMLconversationWindowDividerLocation",
                    e.getOldValue().toString());
        }
    }

    public void removeDividerListener() {
        container.removePropertyChangeListener(dividerListener);
    }

    /**
     * @return the ConsolePanel's JSPlitPane
     */
    public JSplitPane getSplitPane() {
        return container;
    }

    /**
     * Adds the various event listeners for the components that are a part of
     * this frame
     */
    private void addListeners() {

        //if the press enter, send the message
        Action sendMessageAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                sendHandler();
            }
        };

        Action shiftEnterAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int pos = textEntryArea.getCaretPosition();
                if (pos < 0) {
                    pos = 0;
                }

                textEntryArea.setText(textEntryArea.getText() + "\n");

                try {
                    textEntryArea.setCaretPosition(pos + 1);
                } catch (IllegalArgumentException ex) {
                }
            }
        };

        Action checkCloseAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeHandler();
            }
        };

        Action closeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeHandler();
            }
        };

         //set it up so that if there isn't any selected text in the
        // conversation area
        //the textentryarea grabs the focus.
        conversationArea.getTextPane().addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (conversationArea.getSelectedText() == null) {
                    textEntryArea.requestFocus();
                }
            }
        });


        textEntryArea.getInputMap()
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                sendMessageAction);
        textEntryArea.getInputMap()
        .put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                java.awt.event.InputEvent.SHIFT_MASK),
                shiftEnterAction);
        textEntryArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()),
                shiftEnterAction);
        textEntryArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()),
                checkCloseAction);
        textEntryArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit
                .getDefaultToolkit().getMenuShortcutKeyMask()),
                closeAction);
    }

    public String getPanelName() {
        return resources.getString("console");
    }

    /**
     * Recieves a message
     *
     * @param sbj
     *            Description of the Parameter
     * @param message
     *            Description of the Parameter
     * @param resource
     *            Description of the Parameter
     * @param date
     *            Description of the Parameter
     */
    public void append(final String message, boolean to) {
        final Color color;

        if(!to) color = ConversationArea.SENDER;
        else color = ConversationArea.RECEIVER;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                conversationArea.append(message + "\n\n", color );
            }
        });
    }

    /**
     * Sends the message in the TextEntryArea
     */
    private void sendHandler() {
        if (!textEntryArea.getText().equals("")) {
            if (!BuddyList.getInstance().checkConnection()) {
                BuddyList.getInstance().connectionError();
                return;
            }
            sendBuddyMessage(textEntryArea.getText());

            textEntryArea.setText("");
            //com.valhalla.jbother.sound.SoundPlayer.play( "sentSound" );
        }

    }

    /**
     * Sends the message
     *
     * @param text
     *            the message to send
     */
    public void sendBuddyMessage(String text) {
        Blank message = new Blank(text);
        if (BuddyList.getInstance().checkConnection()) {
            BuddyList.getInstance().getConnection().sendPacket(message);
        } else {
            BuddyList.getInstance().connectionError();
        }
    }

    /**
     * Creates the containing frame
     */
    public void createFrame() {
        frame = new JFrame();
        frame.setContentPane(this);
        frame.pack();

        frame.setIconImage(Standard.getImage("frameicon.png"));

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeHandler();
            }
        });

        frame.setTitle(resources.getString("xmlConsole"));
        frame.pack();

        String stringWidth = Settings.getInstance().getProperty(
                "XMLconversationWindowWidth");
        String stringHeight = Settings.getInstance().getProperty(
                "XMLconversationWindowHeight");

        if (stringWidth == null) {
            stringWidth = "400";
        }
        if (stringHeight == null) {
            stringHeight = "340";
        }

        frame.setSize(new Dimension(Integer.parseInt(stringWidth), Integer
                .parseInt(stringHeight)));

        // add a resize window listener
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Dimension dim = frame.getSize();
                Settings.getInstance().setProperty(
                        "XMLconversationWindowWidth",
                        new Integer((int) dim.getWidth()).toString());
                Settings.getInstance().setProperty(
                        "XMLconversationWindowHeight",
                        new Integer((int) dim.getHeight()).toString());
            }
        });
        Standard.cascadePlacement(frame);

        setUpDivider();
        frame.setVisible(true);
    }

    /**
     * Description of the Method
     */
    public void checkCloseHandler() {
        closeHandler();
    }

}

