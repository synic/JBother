/*
 * FTReceiveListener.java
 *
 * Created on October 11, 2005, 7:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.valhalla.jbother.jabber.smack;
import org.jivesoftware.smackx.filetransfer.*;
import com.valhalla.jbother.*;
/**
 *
 * @author synic
 */
public class FTReceiveListener implements FileTransferListener {
    public void fileTransferRequest(FileTransferRequest rec)
    {
        new FileReceiveDialog(rec).setVisible(true);
    }  
}
