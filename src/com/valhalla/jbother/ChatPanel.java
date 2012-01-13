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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.valhalla.gui.*;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.MUCBuddyStatus;
import com.valhalla.jbother.jabber.smack.SecureExtension;
import com.valhalla.jbother.menus.ConversationPopupMenu;
import com.valhalla.jbother.plugins.events.*;
import com.valhalla.misc.GnuPG;
import com.valhalla.settings.Settings;

/**
 *  Handles conversations between two users. It is usually associated with a
 *  BuddyStatus.
 *
 *@author     Adam Olsen
 *@author     Andrey Zakirov
 *@created    September 9, 2005
 *@version    1.1
 *@see        com.valhalla.jbother.jabber.BuddyStatus
 */
public class ChatPanel extends ConversationPanel {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    //private StringBuffer conversationText = new StringBuffer();
    private boolean offlineMessage = false;

    private ConversationPopupMenu popMenu = new ConversationPopupMenu(this,
            conversationArea);

    //private HTMLDocument document =
    // (HTMLDocument)conversationArea.getDocument();
    private JSplitPane container;

    private JPanel buttonPanel = new JPanel();

    private JPanel scrollPanel = new JPanel(new GridLayout(1, 0));

    //private JCheckBox useHTML = new JCheckBox( "Use HTML" );
    // for logging
    private JComboBox resourceBox = new JComboBox();

    private JLabel typingLabel = new JLabel(Standard.getIcon("images/nottyping.png"));

    private JButton clearButton = new JButton(Standard.getIcon("images/buttons/New24.gif"));

    private JButton emoteButton = new JButton(Standard.getIcon("images/buttons/smiley.gif"));

    private boolean divSetUp = false;

    private DividerListener dividerListener = new DividerListener();

    private Hashtable chats = new Hashtable();

    private boolean isTyping = false;

    private javax.swing.Timer typingTimer = new javax.swing.Timer(13000,
            new TypingHandler());

    private JButton encryptButton = new JButton();
    private String selected = null;
    private JScrollPane scroll = new JScrollPane(textEntryArea);

    /**
     *  Sets up the ChatPanel - creates all visual components and adds event
     *  listeners
     *
     *@param  buddy      the buddy to associate with
     */
    public ChatPanel(final BuddyStatus buddy) {
        super(buddy);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // create two fields, one for where you type your message to be sent,
        // and the other where you see the conversation that has already happened.
        textEntryArea.setLineWrap(true);
        textEntryArea.setWrapStyleWord(true);

        conversationArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        container = new JSplitPane(JSplitPane.VERTICAL_SPLIT, conversationArea,
                scroll);
        container.setResizeWeight(1);

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.X_AXIS));
        containerPanel.add(container);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        if (buddy.getUser().indexOf("/") >= 0) {
            resourceBox.setEnabled(false);
        }

        resourceBox.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selected = (String) resourceBox.getSelectedItem();
                }
            });

        resourceBox.setRenderer(new PresenceComboBoxRenderer());
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JPanel resourcePanel = new JPanel();
        resourcePanel.setLayout(new BoxLayout(resourcePanel, BoxLayout.Y_AXIS));
        resourcePanel.add(Box.createVerticalGlue());
        resourcePanel.add(resourceBox);

        buttonPanel.add(resourcePanel);
        typingLabel.setPreferredSize(new Dimension(26, 26));
        buttonPanel.add(typingLabel);
        typingLabel.setToolTipText(resources.getString("notTypingReply"));

        emoteButton.setPreferredSize(new Dimension(26, 26));
        buttonPanel.add(emoteButton);

        if (!buddy.isEncrypting()) {
            encryptButton.setIcon(Standard.getIcon("images/buttons/ssl_no.png"));
        } else {
            encryptButton.setIcon(Standard.getIcon("images/buttons/ssl_yes.png"));
        }

        encryptButton.setPreferredSize(new Dimension(26, 26));
        if (JBotherLoader.isGPGEnabled()
                 && BuddyList.getInstance().getGnuPGPassword() != null) {
            buttonPanel.add(encryptButton);
        }

        String gnupgSecretKey = Settings.getInstance().getProperty(
                "gnupgSecretKeyID");

        if (gnupgSecretKey == null) {
            encryptButton.setEnabled(false);
        }

        clearButton.setPreferredSize(new Dimension(26, 26));
        buttonPanel.add(clearButton);

        bottomPanel.add(buttonPanel);

        add(containerPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(bottomPanel);

        textEntryArea.grabFocus();

        textEntryArea.addKeyListener(
            new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (lastReceived != null && !isTyping
                             && e.getKeyChar() != KeyEvent.VK_ENTER) {
                        if (Settings.getInstance().getBoolean(
                                "sendTypingNotification")
                                 && buddy.getComposingID() != null
                                 && buddy.getComposingID().equals(
                                lastReceived.getPacketID())) {
                            ConnectorThread.getInstance().getMessageEventManager()
                                    .sendComposingNotification(
                                    lastReceived.getFrom(),
                                    lastReceived.getPacketID());

                            isTyping = true;

                            if (typingTimer.isRunning()) {
                                typingTimer.restart();
                            } else {
                                typingTimer.start();
                            }
                        }
                    }
                }
            });

        addListeners();
        updateResources();
    }

    public void removeScroll() { scroll.setViewportView(null); }


    /**
     *  Description of the Method
     */
    public void enableEncrypt() {
        encryptButton.setEnabled(true);
    }


    /**
     *  Description of the Method
     */
    public void disableEncrypt() {
        encryptButton.setEnabled(true);
    }


    /**
     *  Description of the Class
     *
     *@author     synic
     *@created    September 9, 2005
     */
    private class TypingHandler implements ActionListener {
        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void actionPerformed(ActionEvent e) {
            if (lastReceived != null
                     && buddy.getComposingID() != null
                     && buddy.getComposingID()
                    .equals(lastReceived.getPacketID())) {
                ConnectorThread.getInstance().getMessageEventManager()
                        .sendCancelledNotification(lastReceived.getFrom(),
                        lastReceived.getPacketID());
            }

            typingTimer.stop();
            isTyping = false;
        }
    }


    /**
     *  Sets the isTyping attribute of the ChatPanel object
     *
     *@param  typing  The new isTyping value
     */
    public void setIsTyping(boolean typing) {
        if (buddy.size() <= 0) {
            return;
        }
        String s = "images/typing.png";
        if (!typing) {
            s = "images/nottyping.png";
        }
        typingLabel.setIcon(Standard.getIcon(s));

        if (typing) {
            typingLabel.setToolTipText(resources.getString("typingReply"));
        } else {
            typingLabel.setToolTipText(resources.getString("notTypingReply"));
        }
        typingLabel.validate();
    }


    /**
     *  Description of the Method
     */
    public void removeDividerListener() {
        container.removePropertyChangeListener(dividerListener);
    }


    /**
     *@return    the input area of this panel
     */
    public JComponent getInputComponent() {
        return textEntryArea;
    }


    /**
     *  Sets up the Divider
     */
    public void setUpDivider() {
        String modifier = "";
        if (Settings.getInstance().getBoolean("useTabbedWindow")) {
            modifier = "tabbed_";
        }

        String stringHeight = Settings.getInstance().getProperty(
                "conversationWindowHeight");

        // set up the divider location from settings
        String divLocString = Settings.getInstance().getProperty(
                modifier + "conversationWindowDividerLocation");
        int divLoc = 30;

        try {
            if (divLocString != null) {
                divLoc = Integer.parseInt(divLocString);
            } else {
                divLoc = Integer.parseInt(stringHeight) - 117;
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }

        container.setDividerLocation(divLoc);

        if (!divSetUp) {
            container.addPropertyChangeListener("lastDividerLocation",
                    dividerListener);
            divSetUp = true;
        }
    }


    /**
     *@return    the ChatPanel's JSPlitPane
     */
    public JSplitPane getSplitPane() {
        return container;
    }


    /**
     *  Listens for the user to move the divider, and saves it's location
     *
     *@author     Adam Olsen
     *@created    September 9, 2005
     *@version    1.0
     */
    private class DividerListener implements PropertyChangeListener {
        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void propertyChange(PropertyChangeEvent e) {
            String modifier = "";
            if (Settings.getInstance().getBoolean("useTabbedWindow")) {
                modifier = "tabbed_";
            }

            if (e.getOldValue().toString().equals("-1")) {
                return;
            }

            Settings.getInstance().setProperty(
                    modifier + "conversationWindowDividerLocation",
                    e.getOldValue().toString());
        }
    }

    /**
     *  Gets the ComboBox with all the buddy's resources
     *
     *@return    the ComboBox
     */
    public JComboBox getResourceBox() {
        return resourceBox;
    }


    /**
     *  Updates the JComboBox with the buddy's current resources
     */
    public void updateResources() {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    if (selected == null) {
                        selected = "";
                    }
                    com.valhalla.Logger.debug("updating resources " + buddy.getUser());
                    resourceBox.removeAllItems();
                    resourceBox.addItem(resources.getString("defaultResource"));
                    resourceBox.addItem(resources.getString("allResources"));

                    if (buddy.getUser().indexOf("/") >= 0) {
                        return;
                    }

                    Iterator i = buddy.keySet().iterator();

                    int count = 2;

                    int sel = 0;
                    while (i.hasNext()) {
                        String key = (String) i.next();
                        if (!key.equals("N/A")) {
                            resourceBox.addItem(key);
                            if (key.equals(selected)) {
                                sel = count;
                                com.valhalla.Logger.debug("sel" + selected);
                            }
                            count++;
                        }
                    }

                    if (count == 2) {
                        sel = 0;
                        resourceBox.setEnabled(false);
                    } else if (count == 3) {
                        sel = 2;
                        resourceBox.setEnabled(false);
                    } else {
                        resourceBox.setEnabled(true);
                    }

                    if (selected.equals(resources.getString("allResources"))) {
                        sel = 1;
                    }
                    if (sel > 0 && sel <= resourceBox.getItemCount()) {
                        resourceBox.setSelectedIndex(sel);
                    } else {
                        resourceBox.setSelectedIndex(0);
                    }

                    resourceBox.repaint();
                }
            });
    }


    /**
     *  Adds the various event listeners for the components that are a part of
     *  this frame
     */


    private void addListeners() {

        clearButton.setToolTipText(resources.getString("clear"));
        emoteButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFrame f = frame;
                    if (f == null) {
                        f = BuddyList.getInstance().getTabFrame();
                    }
                    ConversationFormatter.getInstance().displayEmoticonChooser(f, emoteButton,
                            textEntryArea);
                }
            });

        encryptButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    if (buddy.isEncrypting()) {
                        buddy.isEncrypting(false);
                        encryptButton.setIcon(Standard.getIcon("images/buttons/ssl_no.png"));
                    } else {
                        buddy.isEncrypting(true);
                        encryptButton.setIcon(Standard.getIcon("images/buttons/ssl_yes.png"));
                    }
                }
            });

        clearButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    conversationArea.setText("");
                }
            });

        //if the press enter, send the message
        Action sendMessageAction =
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    sendHandler(null);
                }
            };

        Action shiftEnterAction =
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    int pos = textEntryArea.getCaretPosition();
                    if (pos < 0) {
                        pos = 0;
                    }

                    textEntryArea.insert("\n", pos);

                    try {
                        textEntryArea.setCaretPosition(pos + 1);
                    } catch (IllegalArgumentException ex) {
                    }
                }
            };

        Action checkCloseAction =
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    checkCloseHandler();
                }
            };

        Action closeAction =
            new AbstractAction() {
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


        conversationArea.getTextPane().addMouseListener(new RightClickListener(popMenu));
        CopyPasteContextMenu.registerComponent(conversationArea.getTextPane());

        textEntryArea.getInputMap()
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                sendMessageAction);
        textEntryArea.getInputMap()
                .put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                java.awt.event.InputEvent.SHIFT_MASK),
                shiftEnterAction);
        textEntryArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                shiftEnterAction);
        textEntryArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                checkCloseAction);
        textEntryArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                closeAction);
    }



    /**
     *  Gets the textEntryArea attribute of the ChatPanel object
     *
     *@return    The textEntryArea value
     */
    public JTextComponent getTextEntryArea() {
        return textEntryArea;
    }


    /**
     *  Displays a message in the window when the buddy signs off
     */
    public void signedOff() {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    setIsTyping(false);
                    conversationArea.append(getDate(null) + " " + buddy.getName() + " "+
                            resources.getString("signedOff") + "\n",
                            ConversationArea.SERVER);
                }
            });
    }


    /**
     *  Displays a message in the window when a buddy signs on
     */
    public void signedOn() {

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    conversationArea.append(getDate(null) + " " + buddy.getName() + " " +
                            resources.getString("signedOn") + "\n",
                            ConversationArea.SERVER);
                }
            });
    }


    /**
     *  Displays a "disconnected" message"
     */
    public void disconnected() {
        conversationArea.append(getDate(null) + " *** " +
                resources.getString("disconnected") + "\n",
                ConversationArea.BLACK, true);

        chats = new Hashtable();
    }


    /**
     *  Description of the Method
     *
     *@param  text  Description of the Parameter
     */
    public void messageEvent(String text) {
         conversationArea.append(getDate(null) + " *** " +
                text + "\n",
                ConversationArea.BLACK, true);
    }


    /**
     *  Receives a message
     *
     *@param  sbj            the message subject
     *@param  body           the message body
     *@param  resource       the resource the message came from if there is one
     *@param  delayInfo      Description of the Parameter
     *@param  date           Description of the Parameter
     *@param  decryptedFlag  Description of the Parameter
     *@param  verifiedFlag   Description of the Parameter
     */
    public void receiveMessage(final String sbj, final String delayInfo,
            final String body, final String resource, final Date date,
            final boolean decryptedFlag, final boolean verifiedFlag) {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    String extraInfo = delayInfo;
                    setIsTyping(false);
                    receiveMessage();

                    JFrame f = frame;
                    if (f == null) {
                        f = BuddyList.getInstance().getTabFrame();
                    }

                    if (f.isFocused() == true) {
//                    SwingUtilities.invokeLater(new Runnable() {
//                         public void run() {
                        buddy.sendNotDisplayedID();
//                         }
//                    });
                    }

                    if (resource != null && buddy.getUser().indexOf("/") < 0 && !resource.equals("")) {
                        int c = 0;
                        selected = resource;
                        boolean select = false;
                        for (int i = 0; i < resourceBox.getModel().getSize(); i++) {
                            if (((String) resourceBox.getModel().getElementAt(i)).equals(resource)) {
                                select = true;
                                break;
                            }
                            c++;
                        }

                        if(select) resourceBox.setSelectedIndex(c);
                    }
                    String newBody = body;

                    String name = buddy.getName();
                    if (name == null) {
                        name = buddy.getUser();
                    }

                    ImageIcon enc = null;
                    ImageIcon sig = null;
                    if (decryptedFlag) {
                        enc = Standard.getIcon("images/encrypted.gif");

                    }
                    if (verifiedFlag) {
                        sig = Standard.getIcon("images/signed.gif");
                    }
                    if (newBody.startsWith("/me ")) {
                        newBody = newBody.replaceAll("^\\/me ", "");
                        conversationArea.append(getDate(date));
                        conversationArea.append(" *" + name, ConversationArea.BLACK, true);
                        conversationArea.append(extraInfo, ConversationArea.BLACK);
                        if( sig != null ) conversationArea.appendIcon(sig);
                        if( enc != null ) conversationArea.appendIcon(enc);
                        conversationArea.append(" " + newBody + "\n", ConversationArea.BLACK);
                    } else {
                        conversationArea.append(getDate(date), ConversationArea.SENDER);
                        conversationArea.append(" " + name, ConversationArea.SENDER, true);
                        conversationArea.append(extraInfo, ConversationArea.BLACK);
                        if( sig != null ) conversationArea.appendIcon(sig);
                        if( enc != null ) conversationArea.appendIcon(enc);
                        conversationArea.append(": " + newBody + "\n", ConversationArea.BLACK);
                    }
                }
            });

    }


    /**
     *  Sends the message in the TextEntryArea
     *
     *@param  allText  Description of the Parameter
     */
    public void sendHandler(String allText) {
        String areaTextComplete;
        final String areaText;
        if (allText != null) {
            areaTextComplete = allText;
        } else {
            areaTextComplete = textEntryArea.getText();
        }

        if (!areaTextComplete.equals("")) {
            if (!BuddyList.getInstance().checkConnection()) {
                BuddyList.getInstance().connectionError();
                return;
            }
            if ((areaTextComplete.startsWith("/all ") == true || areaTextComplete.startsWith("/ame ") == true)) {
                if (areaTextComplete.startsWith("/ame ") == true) {
                    areaTextComplete = areaTextComplete.replaceAll("^/ame ",
                            "/me ");
                } else {
                    areaTextComplete = areaTextComplete.replaceAll("^/all ", "");
                }
                if (allText == null) {
                    Hashtable buddyStatuses = BuddyList.getInstance()
                            .getBuddyStatuses();
                    if (buddyStatuses != null) {
                        Iterator iterator = buddyStatuses.keySet().iterator();
                        while (iterator.hasNext()) {
                            String user2 = (String) iterator.next();
                            BuddyStatus buddy2 = (BuddyStatus) buddyStatuses.get(user2);
                            if (buddy2.equals(buddy) == false
                                     && buddy2.getConversation() != null
                                     && buddy2.getConversation() instanceof ChatPanel) {
                                ((ChatPanel) buddy2.getConversation())
                                        .sendHandler(areaTextComplete);
                            }
                        }
                    }
                }
            }

            areaText = areaTextComplete;

            if ((buddy.isEncrypting())
                     &&
            // ( BuddyList.getInstance().isEncrypting() ) &&
                    ((buddy.getPubKey() == null) || (buddy.getPubKey() == null))) {
                KeySelectDialog dialog = new KeySelectDialog("pub");
                dialog.showDialog();
                if (dialog.getID() != null) {
                    buddy.setPubKey(dialog.getID());
                } else {
                    buddy.isEncrypting(false);
                    encryptButton.setIcon(Standard.getIcon("images/buttons/ssl_no.png"));
                }
            }

            if (!sendBuddyMessage(areaText)) {
                return;
            }
            isTyping = false;
            typingTimer.stop();

            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {

                        String text = areaText;

                        if (text.startsWith("/me ")) {
                            text = text.replaceAll("^/me ", "");
                            conversationArea.append(getDate(null), ConversationArea.RECEIVER);
                            conversationArea.append( " *" + BuddyList.getInstance().getMyName() + " ", ConversationArea.RECEIVER, true);
                            conversationArea.append(text + "\n", ConversationArea.BLACK);
                        } else {
                            conversationArea.append(getDate(null), ConversationArea.RECEIVER);
                            conversationArea.append(" " + BuddyList.getInstance().getMyName()+ ": ", ConversationArea.RECEIVER, true);
                            conversationArea.append(text + "\n", ConversationArea.BLACK);
                        }

                        textEntryArea.setText("");
                    }
                });
        }
    }


    /**
     *  Sends the message to the resource in the JComboBox
     *
     *@param  text  the message to send
     *@return       Description of the Return Value
     */
    public boolean sendBuddyMessage(String text) {
        String to = buddy.getUser();
        int sel = resourceBox.getSelectedIndex();

        SecureExtension secureExtension = new SecureExtension();
        SecureExtension signedExtension = new SecureExtension("signed");

        // if they've selected a resource, send to it
        if (sel != 0 && sel != 1 && sel != -1) {
            to += "/" + (String) resourceBox.getSelectedItem();
        }

        ArrayList send = new ArrayList();

        if (sel != 1 || resourceBox.getItemCount() <= 2) {
            send.add(to);
        }
        // if they've selected to send to all resources, send to all
        else {
            Set keys = buddy.keySet();
            Iterator i = keys.iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                if (!key.equals("N/A")) {
                    send.add(buddy.getUser() + "/" + key);
                }
            }
        }

        String gnupgSecurityVariant = Settings.getInstance().getProperty(
                "gnupgSecurityVariant");
        String gnupgSecretKey = Settings.getInstance().getProperty(
                "gnupgSecretKeyID");
        String gnupgPublicKey = buddy.getPubKey();
        if (JBotherLoader.isGPGEnabled() &&
        // BuddyList.getInstance().isEncrypting()
                buddy.isEncrypting() && (gnupgSecretKey != null)
                 && (gnupgPublicKey != null)) {
            GnuPG gnupg = new GnuPG();
            String encryptedData = null;
            String signedData = null;

            if (gnupgSecurityVariant == null) {
                gnupgSecurityVariant = "0";
                Settings.getInstance().setProperty("gnupgSecurityVariant", "0");
            }

            if ((gnupgSecurityVariant.equals("0"))
                     || (gnupgSecurityVariant.equals("1"))) {
                encryptedData = gnupg.encryptExtension(text, gnupgSecretKey,
                        gnupgPublicKey);
                if (encryptedData != null) {
                    secureExtension.setData(encryptedData);
                }
            }
            if ((gnupgSecurityVariant.equals("0"))
                     || (gnupgSecurityVariant.equals("2"))) {
                signedData = gnupg.signExtension(text, gnupgSecretKey);
                if (signedData != null) {
                    signedExtension.setData(signedData);
                }
            }

            if ((encryptedData == null) && (signedData == null)) {
                buddy.isEncrypting(false);
                encryptButton.setIcon(Standard.getIcon("images/buttons/ssl_no.png"));
                Standard.warningMessage(null,
                        resources.getString("gnupgError"), resources.getString("gnupgErrorEncrypting")
                         + ".\n\n"
                         + resources.getString("reason")
                         + ":\n\n"
                         + gnupg.getResult()
                         + gnupg.getErrorString()
                         + "\n"
                         + resources.getString("gnupgTryOrSendUnencrypted")
                         + ".");
                return false;
            }
        }

        for (int i = 0; i < send.size(); i++) {
            Chat chat = null;

            if (buddy instanceof MUCBuddyStatus) {
                MultiUserChat muc = ((MUCBuddyStatus) buddy).getMUC();
                chat = muc.createPrivateChat(buddy.getUser());
            } else {
                chat = (Chat) chats.get((String) send.get(i));
            }

            if (chat == null) {
                chat = BuddyList.getInstance().getConnection().createChat(
                        (String) send.get(i));
                chats.put((String) send.get(i), chat);
            }

            Message message = chat.createMessage();
            if (secureExtension.getData() != null) {
                message.setBody("[This message is encrypted]");
                message.addExtension(secureExtension);
            } else {
                message.setBody(text);
            }
            if (signedExtension.getData() != null) {
                message.addExtension(signedExtension);
            }


            if (buddy.isAskForDelivered()) {
                BuddyList.getInstance().putEventMessage(message.getPacketID(), message.getTo(), getDate(null), 1);
            }
            if (buddy.isAskForDisplayed()) {
                BuddyList.getInstance().putEventMessage(message.getPacketID(), message.getTo(), getDate(null), 2);
            }
            if (buddy.isAskForOffline()) {
                BuddyList.getInstance().putEventMessage(message.getPacketID(), message.getTo(), getDate(null), 3);
            }

            MessageEventManager.addNotificationsRequests(message, buddy.isAskForOffline(), buddy.isAskForDelivered(), buddy.isAskForDisplayed(), true);
            MessageSendingEvent event = new MessageSendingEvent(this);
            event.setMessage(message);
            com.valhalla.pluginmanager.PluginChain.fireEvent(event);

            try {
                if (BuddyList.getInstance().checkConnection()) {
                    chat.sendMessage(message);
                } else {
                    BuddyList.getInstance().connectionError();
                }
            } catch (XMPPException e) {
                com.valhalla.Logger.debug("Could not send message: "
                         + e.getMessage());
            }
        }

        return true;
    }

    public void closeHandler() {
        removeScroll();
        super.closeHandler();
    }


    /**
     *  Creates the containing frame
     */
    public void createFrame() {
        frame = new JFrame();
        frame.setContentPane(this);
        frame.pack();

        frame.setIconImage(Standard.getImage("frameicon.png"));

        frame.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    if (Settings.getInstance().getProperty("preserveMessages") == null) {
                        closeHandler();
                    } else {
                        startTimer();
                        frame.setVisible(false);
                    }
                }
            });
//        frame.addFocusListener(new FocusListener() {

//        JFrame f = frame;

        //      if (frame !=null)
//        {
//
        final BuddyStatus buddy2 = buddy;
        frame.addWindowFocusListener(
            new WindowFocusListener() {
                public void windowGainedFocus(WindowEvent e) {
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
                    buddy2.sendNotDisplayedID();
//                    }
//                });
                }


                public void windowLostFocus(WindowEvent e) {
                }
            });
//        }
//        else
//        {

        String title = buddy.getUser();
        if (buddy.getName() != null) {

            if ((buddy.getName().toLowerCase()).matches(buddy.getUser())) {
                title = buddy.getName();
            } else {
                title = buddy.getName() + " (" + buddy.getUser() + ")";
            }

//            title = buddy.getName()
//            + " (" + title + ")";
        }

        frame.setTitle(title);
        frame.pack();

        String stringWidth = Settings.getInstance().getProperty(
                "conversationWindowWidth");
        String stringHeight = Settings.getInstance().getProperty(
                "conversationWindowHeight");

        if (stringWidth == null) {
            stringWidth = "400";
        }
        if (stringHeight == null) {
            stringHeight = "340";
        }

        frame.setSize(new Dimension(Integer.parseInt(stringWidth), Integer.parseInt(stringHeight)));

        // add a resize window listener
        frame.addComponentListener(
            new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    Dimension dim = frame.getSize();
                    Settings.getInstance().setProperty("conversationWindowWidth",
                            new Integer((int) dim.getWidth()).toString());
                    Settings.getInstance().setProperty("conversationWindowHeight",
                            new Integer((int) dim.getHeight()).toString());
                }
            });

        Standard.cascadePlacement(frame);

        setUpDivider();
        validate();
    }


    /**
     *  This renders the resource combo box - and displays icons for the online
     *  status of each resource
     *
     *@author     Adam Olsen
     *@created    September 9, 2005
     *@version    1.0
     */
    class PresenceComboBoxRenderer extends JLabel implements ListCellRenderer {
        /**
         *  Gets the listCellRendererComponent attribute of the
         *  PresenceComboBoxRenderer object
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
            Presence.Mode mode = null;
            if (value == null) {
                value = "";
            }

            if (buddy.size() != 0) {
                mode = buddy.getPresence(buddy.getHighestResource());
            }

            if (value.toString().equals(resources.getString("allResources"))) {
                mode = Presence.Mode.AVAILABLE;
            }

            if (!value.toString()
                    .equals(resources.getString("defaultResource"))
                     && !value.toString().equals(
                    resources.getString("allResources"))) {
                mode = buddy.getPresence(value.toString());
            }

            ImageIcon icon = StatusIconCache.getStatusIcon(mode);
            if (icon != null) {
                setIcon(icon);
            }
            setText(value.toString());

            return this;
        }
    }
}

