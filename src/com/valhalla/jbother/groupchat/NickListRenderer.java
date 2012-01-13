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

package com.valhalla.jbother.groupchat;

import java.awt.*;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.valhalla.jbother.StatusIconCache;
import com.valhalla.jbother.jabber.MUCBuddyStatus;
import com.valhalla.settings.Settings;

/**
 * Renders the JList that represents the nickname list in a groupchat
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class NickListRenderer extends JLabel implements ListCellRenderer {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private ChatRoomPanel window;

    /**
     * Sets up the renderer
     *
     * @param window
     *            the window that this nicklist belongs to
     */
    public NickListRenderer(ChatRoomPanel window) {
        this.window = window;
        setOpaque(true);
    }

    /**
     * @see ListCellRenderer
     */
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        String name = ((String)value).replaceAll( "^aa_aa\\d{4} ", "" );
        MUCBuddyStatus buddy = window.getBuddyStatus(name);

        String nick = "";
        String room = "";

        int a = buddy.getUser().indexOf("/");
        if (a > -1) {
            room = buddy.getUser().substring(0, a);
            nick = buddy.getUser().substring(a + 1);
        } else { //just in case
            nick = buddy.getUser();
            room = buddy.getUser();
        }

        if (Settings.getInstance().getProperty("statusTheme") == null)
            Settings.getInstance().setProperty("statusTheme", "default");
        ImageIcon icon = StatusIconCache.getStatusIcon(buddy.getPresence(buddy
                .getHighestResource()));
        if (icon != null)
            setIcon(icon);

        URL light = getClass().getClassLoader().getResource(
                "images/lightbulb.png");
        StringBuffer tooltip = new StringBuffer();
                tooltip.append( "<html><table border='0'><tr><td valign='top'><img src='" )
                .append( light.toString() )
                .append( "'></td><td>" )
                .append( "<b><font size='+1'>")
                .append( nick )
                .append( "</font></b>" )
                .append( "<table border='0' cellpadding='2' cellspacing='2'>" );

        // role and affiliation information
        String role = buddy.getRole();
        if (role == null || role.equals(""))
            role = "none";
        String affiliation = buddy.getAffiliation();
        if (affiliation == null || affiliation.equals(""))
            affiliation = "none";
        if (buddy.getJid() != null)
            tooltip.append( "<tr><td><b>JID:</b></td><td>" ).append( buddy.getJid() )
                    .append( "</td></tr>\n" );
        tooltip.append( "<tr><td><b>Role:</b></td><td>" ).append( role ).append( "</td></tr>\n" );
        tooltip.append( "<tr><td><b>Affiliation:</b></td><td>" ).append( affiliation )
                .append( "</td></tr>\n" );

        String statusMessage = buddy.getStatusMessage(buddy
                .getHighestResource());
        if (statusMessage != null && !statusMessage.equals("")) {
            tooltip.append( "<tr><td><b>" ).append( this.resources.getString("status") )
                    .append( ":</b></td><td>" ).append( statusMessage ).append( "</td></tr>" );
        }

        String using = buddy.getVersionInfo();
        if (using != null) {
            tooltip.append( "<tr><td nowrap><b>" ).append( this.resources.getString("using") )
                    .append( ":</b></td><td nowrap>" ).append( using ).append( "</td></tr>" );
        }

        tooltip.append( "</table></td></tr></table></html>" );

        setToolTipText(tooltip.toString());

        URL type = getClass().getClassLoader().getResource(
                "imagethemes/muc/default/" + buddy.getRole() + ".png");
        if (getClass().getClassLoader().getResource(
                "imagethemes/muc/default/" + buddy.getAffiliation() + ".png") != null) {
            type = getClass().getClassLoader().getResource(
                    "imagethemes/muc/default/" + buddy.getAffiliation()
                            + ".png");
        }

        if (type == null) {
            type = getClass().getClassLoader().getResource(
                    "imagethemes/muc/default/blank.png");
        }

        if (type != null) {
            nick = "<html><img src='" + type.toString() + "'>&nbsp;&nbsp;"
                    + nick + "</html>";
        }

        setText(nick);

        setBackground(isSelected ? list.getSelectionBackground() : Color.WHITE);
        setForeground(isSelected ? list.getSelectionForeground() : list
                .getForeground());
        list.validate();

        return this;
    }
}