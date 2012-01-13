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
 You should have recieved a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.valhalla.jbother;

import com.valhalla.jbother.jabber.smack.*;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.filetransfer.*;
import org.jivesoftware.smack.*;

import org.jivesoftware.smackx.packet.*;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.PacketListener;
import java.io.*;
import java.net.*;

import javax.swing.*;
import java.awt.event.KeyEvent;
import com.valhalla.settings.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * Dialog for receiving files. It's called each time <code><streamhost></code> packet
 * arrives. User can accept or reject incoming file by pressing "Accept" and "Reject" buttons.
 *
 *@author     Lukasz Wiechec
 *@created    Jan 19, 2005 4:25:58 PM
 */

public class FileReceiveDialog extends FileDialog {
    private FileTransferManager ft;
    private static JFileChooser fileChooser = new JFileChooser();
    private FileTransferRequest recieve = null;
    private FileTransfer rcv = null;
    private JLabel sizeLabel = null;
    private JTextField sizeField = new JTextField();

    java.text.NumberFormat nf = java.text.NumberFormat.getInstance();

    private ResourceBundle resources = ResourceBundle.getBundle( "JBotherBundle", Locale.getDefault() );

    public FileReceiveDialog(FileTransferRequest recieve) {
        this.recieve = recieve;
        this.ft = ft;
        this.recieve = recieve;
        setTitle( "Receive file" );
        fromToLabel.setText( "From:" );
        fromToTF.setEditable(false);
        fileTF.setEditable(false);

        ayeButton.setText( "Accept" );
        ayeButton.setEnabled(true);
        ayeButton.setMnemonic( KeyEvent.VK_A );
        nayButton.setText( "Reject" );
        nayButton.setMnemonic( KeyEvent.VK_R );
        descriptionArea.setEditable( false );

        sizeLabel = new JLabel(resources.getString("fileSize"));
        sizeField.setEditable(false);

        addComponent(sizeLabel, sizeField);

        sizeField.setText(nf.format((recieve.getFileSize()) / 1024) + "k");
        descriptionArea.setText(""); //recieve.getDescription());
        fromToTF.setText(recieve.getRequestor());
        fileTF.setText(recieve.getFileName());
        descriptionArea.setText(recieve.getDescription());
    }


    /** "Accept" button pressed */
    protected void doAye() {
        // try really hard to make sure that file will not be overwritten
        // and can be written to

        boolean done = false;
        while( !done ) {

            fileChooser.setSelectedFile(new File(recieve.getFileName()));
            int retval = fileChooser.showSaveDialog(this);
            if(retval == JFileChooser.APPROVE_OPTION) {
                if(fileChooser.getSelectedFile().exists()) {
                    int choice = JOptionPane.showConfirmDialog(this,
                    "File exists - overwrite?","Receive File",JOptionPane.YES_NO_CANCEL_OPTION);
                    if(choice == JOptionPane.CANCEL_OPTION) {
                        doNay();
                        return;
                    } else if(choice == JOptionPane.NO_OPTION) {
                        done = false;
                    } else if(choice == JOptionPane.YES_OPTION) {
                        done = true;
                        if( ! fileChooser.getSelectedFile().canWrite() ) {
                            JOptionPane.showMessageDialog(this,
                            resources.getString( "writingToFileNotPermitted" ),
                            resources.getString( "recieveFileError" ),
                            JOptionPane.ERROR_MESSAGE
                            );
                            done = false;
                        }
                    }
                } else {

                    done = true;
                }
            } else if(retval == JFileChooser.CANCEL_OPTION) {
                doNay();
                return;
            }
        }
        // send confirmation message indicating that we want to recieve the file

        statusLabel.setText("Waiting for sender...");
        disableAll();

        IncomingFileTransfer rcv = recieve.accept();

        FileProgressDialog.getInstance().addFile(rcv, rcv.getPeer(), fileChooser.getSelectedFile().getName(),
            0, rcv.getFileSize());
        try {
            rcv.recieveFile(fileChooser.getSelectedFile());
        }
        catch(XMPPException ex ) { }
        dispose();
    }

    /** "Reject" button pressed */
    protected void doNay() {
        // we don't want to accept the file so we need to send the IQ error message
        // to the Initiator
        if( rcv != null ) rcv.cancel();
        dispose();
    }

    /**
     * helper method for making sure that packet will be sent
     * @param packet packet to send
     */
    private void sendPacket(Packet packet) {
        if(BuddyList.getInstance().checkConnection()) {
            BuddyList.getInstance().getConnection().sendPacket( packet );
        }
    }
}
