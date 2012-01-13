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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.dotuseful.ui.tree.AutomatedTreeNode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import com.valhalla.gui.Standard;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.jbother.jabber.SelfStatuses;

/**
 * The renderer for the buddy list
 *
 * @author Adam Olsen
 * @created March 9, 2005
 * @version 1.0
 */
class BuddyListRenderer extends DefaultTreeCellRenderer {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    URL light = getClass().getClassLoader().getResource("images/lightbulb.png");

    /**
     * Description of the Method
     */
    public void updateUI() {
        super.updateUI();
        setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
        setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
        setBackgroundSelectionColor(UIManager
                .getColor("Tree.selectionBackground"));
        setBackgroundNonSelectionColor(UIManager
                .getColor("Tree.textBackground"));
        setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));
    }

    /**
     * Gets an individual cell
     *
     * @param tree
     *            Description of the Parameter
     * @param value
     *            Description of the Parameter
     * @param isSelected
     *            Description of the Parameter
     * @param expanded
     *            Description of the Parameter
     * @param leaf
     *            Description of the Parameter
     * @param row
     *            Description of the Parameter
     * @param hasFocus
     *            Description of the Parameter
     * @return The treeCellRendererComponent value
     * @see <code>javax.swing.DefaultTreeCellRenderer</code>
     */
    public Component getTreeCellRendererComponent(JTree tree,
            Object value, boolean isSelected, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {
        AutomatedTreeNode node = (AutomatedTreeNode) value;
        String stringVal = node.getUserObject().toString();

        UIDefaults ui = UIManager.getDefaults();

        if (node.getUserObject() instanceof BuddyStatus) {
            // sets up the buddy information
            BuddyStatus buddy = (BuddyStatus) node.getUserObject();

            value = buddy.getUser();
            if (buddy.getName() != null) {
                value = buddy.getName();
            }
            stringVal = value.toString();

            String user = buddy.getUser();
            String server = buddy.getUser();
            int index = user.indexOf("@");
            if (index > -1) {
                server = user.substring(index + 1);
                user = user.substring(0, index);
            }

            // check for connection error
            if (!BuddyList.getInstance().checkConnection()) {
                Exception ex = new Exception("Disconnected from server");
                ConnectorThread.getInstance().getConnectionListener()
                        .connectionClosedOnError(ex);
            }

            // gets all the resources
            int resourceCount = 0;
            StringBuffer resources = new StringBuffer();

            Iterator i = buddy.keySet().iterator();

            while (i.hasNext()) {
                String key = (String) i.next();

                if (!key.equals("N/A")) {
                    boolean add = false;

                    if (key.equals(buddy.getHighestResource())) {
                        add = true;
                    } else {
                        resources.append("  ");
                    }

                    resources.append(key).append(" (").append(buddy.get(key))
                            .append(")");
                    if (add) {
                        resources.append(" <b>*</b>");
                    }
                    if (i.hasNext()) {
                        resources.append("<br>");
                    }
                    resourceCount++;
                }
            }

            // draw the tooltip
            StringBuffer tooltip = new StringBuffer();
            tooltip
                    .append(
                            "<html><table><tr><td valign='middle' width='2%'><img src='")
                    .append(light.toString())
                    .append("'></td><td valign='top'>")
                    .append(
                            "<table border='0'><tr><td colspan='2'><b><font size='+1'>")
                    .append(user.replaceAll("\\%", "@"))
                    .append(
                            "</font></b><table border='0' cellpadding='2' cellspacing='2'><tr><td nowrap><b>")
                    .append(this.resources.getString("server")).append(
                            ":</b></td><td nowrap>").append(server).append(
                            "</td></tr>");

            RosterEntry entry = buddy.getRosterEntry();
            if (entry != null) {
                RosterPacket.ItemType subType = entry.getType();
                if (subType != null) {
                    tooltip.append("<tr><td nowrap><b>").append(
                            this.resources.getString("subscription")).append(
                            ":</b></td><td nowrap>").append(subType.toString())
                            .append("</td></tr>");
                }
            }

            if (resourceCount > 0) {
                tooltip.append("<tr><td valign=\"top\" nowrap><b>").append(
                        this.resources.getString("pluralResources")).append(
                        ":</b></td><td nowrap>").append(resources).append(
                        "</td></tr>");
            }

            if (buddy.getPubKey() != null) {
                tooltip.append("<tr><td valign=\"top\" nowrap><b>").append(
                        this.resources.getString("gnupgKey")).append(
                        ":</b></td><td nowrap>").append(buddy.getPubKey())
                        .append("</td></tr>");
            }

            String statusMessage = buddy.getStatusMessage(buddy
                    .getHighestResource());
            if (statusMessage != null && !statusMessage.equals("")) {
                statusMessage = statusMessage.replaceAll("\n", "<br>");
                tooltip.append("<tr><td nowrap><b>").append(
                        this.resources.getString("status")).append(
                        ":</b></td><td nowrap>").append(statusMessage).append(
                        "</td></tr>");
            }

            String using = buddy.getVersionInfo();
            if (using != null) {
                tooltip.append("<tr><td nowrap><b>").append(
                        this.resources.getString("using")).append(
                        ":</b></td><td nowrap>").append(using).append(
                        "</td></tr>");
            }

            tooltip
                    .append("</table></td></tr></table></td></tr></table></html>");
            setToolTipText(tooltip.toString());
            setFont((Font) ui.get("Label.font"));

            super.getTreeCellRendererComponent(tree, stringVal, isSelected,
                    expanded, leaf, row, hasFocus);

            // setting up status icon
            Presence.Mode mode = null;
            if (buddy.size() == 0) {
                setForeground(Color.GRAY);
            } else {
                mode = buddy.getPresence(buddy.getHighestResource());
            }

            ImageIcon statusIcon = null;

            // name based guess
            if (buddy.getUser().indexOf("@") == -1) {
                Pattern p = Pattern
                        .compile("^(aim|msn|yahoo|icq|gadu-gadu)[-_.].*");
                Matcher m = p.matcher(buddy.getUser());
                if (m.matches() && m.groupCount() >= 1) {
                    String type = m.group(1);
                    if (type != null) {
                        statusIcon = Standard
                                .getIcon("imagethemes/statusicons/"
                                        + type
                                        + "/"
                                        + SelfStatuses.getInstance().getStatus(
                                                mode).getShortcut() + ".png");
                    }
                }
            }

            // discovery information based guess
            Properties cache = JBotherLoader.getDiscoveryCache();
            String discoInfo = cache.getProperty(buddy.getUser().replaceAll(
                    "/.*", ""));
            if (discoInfo != null) {
                String items[] = discoInfo.split(" ");
                if (items.length == 2) {
                    String type = items[1];
                    if (type.equals("aim") || type.equals("icq")
                            || type.equals("msn") || type.equals("yahoo")
                            || type.equals("gadu-gadu")) {
                        statusIcon = Standard
                                .getIcon("imagethemes/statusicons/"
                                        + type
                                        + "/"
                                        + SelfStatuses.getInstance().getStatus(
                                                mode).getShortcut() + ".png");
                    }
                }
            }

            if (statusIcon == null) {
                statusIcon = StatusIconCache.getStatusIcon(mode);
            }

            if (statusIcon != null) {
                setIcon(statusIcon);
            }
            // /setting up status icon

        }

        else {
            Font newFont = (Font) ui.get("Label.font");
            setFont(new Font(newFont.getName(), Font.BOLD, newFont.getSize()));

            super.getTreeCellRendererComponent(tree, stringVal, isSelected,
                    expanded, leaf, row, hasFocus);

            setToolTipText(null);
            setIcon(null);
        }

        return this;
    }
}

