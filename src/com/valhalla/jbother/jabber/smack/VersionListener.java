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

package com.valhalla.jbother.jabber.smack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.*;

import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.JBother;

/**
 * Listens for and responds to jabber:iq:version requests
 *
 * @author Adam Olsen
 * @version 1.0
 * @see <code>com.valhalla.jbother.jabber.smack.Version</code>
 */
public class VersionListener implements PacketListener {
    /**
     * Event listener called when a Version packet is received
     *
     * @param message
     *            the version packet received
     */
    public void processPacket(Packet message) {
        if (!(message instanceof Version)
                || ((IQ) message).getType() != IQ.Type.GET)
            return;
        Version version = (Version) message;

        String from = version.getFrom();
        String to = version.getTo();

        com.valhalla.Logger.debug("Version request received from " + from);

        String jbVersion = JBother.JBOTHER_VERSION;

        if (jbVersion.toLowerCase().indexOf("cvs") >= 0) {
            ResourceBundle bundle = ResourceBundle.getBundle("buildid");
            jbVersion += " [BID:" + bundle.getString("build.number") + "]";
        }

        // set up the version response
        version.setType(IQ.Type.RESULT);
        version.setFrom(to);
        version.setTo(from);
        version.setName("JBother");
        version.setVersion(jbVersion + " / Java "
                + System.getProperty("java.version"));

        // if the machine is a linux machine, see if we can find out what distro
        String osName = System.getProperty("os.name");
        if (osName.equals("Linux")) {
            osName = getDistro();
            if (osName.equals("Linux"))
                osName += " " + System.getProperty("os.version");
        } else
            osName += " " + System.getProperty("os.version");

        version.setOs(osName + " [" + System.getProperty("os.arch") + "]");

        // send the response
        if (BuddyList.getInstance().checkConnection())
            BuddyList.getInstance().getConnection().sendPacket(version);
    }

    /**
     * Attempts to determine the Linux distrobution
     */
    private String getDistro() {
        File etc = new File("/etc");
        if (!etc.isDirectory())
            return "Linux";

        String[] files = etc.list();
        File distroFile = null;
        Pattern p = Pattern.compile("^([^_-]*)([-_])(version|release)$");

        // finds the name of every file in /etc - if it matches the above
        // regex, read it in and we call that the distro
        for (int i = 0; i < files.length; i++) {
            String name = files[i];
            Matcher m = p.matcher(name);
            if (m.matches()) {
                distroFile = new File(etc, m.group(1) + m.group(2) + m.group(3));
            }
        }

        if (distroFile == null)
            return "Linux";
        String line = "Linux";

        try {
            BufferedReader in = new BufferedReader(new FileReader(distroFile));

            line = in.readLine();

            in.close();
        } catch (IOException e) {
            return "Linux";
        }

        return line;
    }
}
