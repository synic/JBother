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

import com.valhalla.jbother.jabber.smack.*;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ;
import com.valhalla.jbother.preferences.*;
import org.jivesoftware.smackx.filetransfer.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.net.*;
import java.io.File;

import com.valhalla.settings.Settings;

import javax.swing.*;

/**
 * dialog for sending files. It is also a packet listener
 *
 *@author     Lukasz Wiechec
 *@created    Feb 2, 2005 4:52:15 PM
 */

public class FileSendDialog extends FileDialog {
    private ResourceBundle resources = ResourceBundle.getBundle( "JBotherBundle", Locale.getDefault() );
    private static JFileChooser fileChooser = new JFileChooser();
    private String jid;

    public FileSendDialog(String jid) throws HeadlessException {
        setTitle( "Send file" );
        fromToLabel.setText( "To:" );
        this.jid = jid;
        fromToTF.setText(jid);
        fromToTF.setEditable(false);
        ayeButton.setText( "Send" );
        ayeButton.setMnemonic( KeyEvent.VK_S );
        nayButton.setText( "Cancel" );
        nayButton.setMnemonic( KeyEvent.VK_C );
        descriptionArea.setEditable( true );

        selectFile();

    }

    class SendFileThread extends Thread
    {
        OutgoingFileTransfer send = null;
        public SendFileThread(OutgoingFileTransfer send)
        {
            this.send = send;
        }

        public void run()
        {
            try {
                send.sendFile(fileChooser.getSelectedFile().getPath(),
                    fileChooser.getSelectedFile().length(), descriptionArea.getText());
            }
            catch(Exception ex) { }
        }
    }
            

    /**
     * "Send" button pressed
     * note: sending initial StreamInitiation message is done in separate thread, in order to prevent
     * locking up JBother while waiting for the reply
     */
    protected void doAye() {
        FileTransferManager ft = ConnectorThread.getInstance().getFileTransferManager();
        OutgoingFileTransfer send = ft.createOutgoingFileTransfer(jid);
        FileProgressDialog.getInstance().addFile(send, jid, 
            fileChooser.getSelectedFile().getName(), 0, fileChooser.getSelectedFile().length());
        dispose();
        new SendFileThread(send).start();
    }

    /**
     * "Cancel" button pressed
     */
    protected void doNay() {
        setVisible(false);
        dispose();
    }

    /**
     * displays file chooser window and lets user select file to send
     */
    private void selectFile() {
        int retval = fileChooser.showOpenDialog(BuddyList.getInstance().getContainerFrame());

        if( retval == JFileChooser.APPROVE_OPTION ) {
            File selectedFile = fileChooser.getSelectedFile();
            if( ! selectedFile.exists() ) {
                JOptionPane.showMessageDialog(this,
                resources.getString("fileNotFound"),
                resources.getString("sendFileError"),
                JOptionPane.ERROR_MESSAGE);
                return;
            }

            setVisible(true);

            fileTF.setText(selectedFile.getPath());
            fileTF.setEditable(false);

        } else if( retval == JFileChooser.CANCEL_OPTION ) {
            doNay();
            return;
        }
    }
}
