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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.text.*;

import com.valhalla.gui.Standard;
import com.valhalla.settings.Settings;

/**
 * Replaces different emote symbols with images
 *
 * @author Adam Olsen
 * @created October 30, 2004
 * @version 1.0
 */
public class ConversationFormatter {
    private String imageDir;

    private static ConversationFormatter instance;

    private Properties map;

    private String themeDir;
    Pattern url = Pattern.compile("(^|\\s)((ftp|https?)://[^\\s\"':]+?)(\\s|$)");
    Pattern email = Pattern.compile("(\\s|^)((?!(ftp|http|https)://)[^\\s\"'\\(\\)\\[\\]><:]+?@[^<>\\s\"':]+?)(\\s|\\s?$)");
    Pattern emoticon = Pattern.compile("<image_location=([^>]*)>");

    private InputStream emoticonStream;

    private boolean switched = false;

    protected ResourceBundle resources = ResourceBundle.getBundle("JBotherBundle", Locale.getDefault());
    /**
     * Default constructor - opens the emoticon theme dir and reads in the data
     * file containing the different emote definitions
     */
    private ConversationFormatter() {
    }

    /**
     *
     *
     * @return the ConversationFormatter singleton
     */
    public static ConversationFormatter getInstance() {
        if (instance == null) {
            instance = new ConversationFormatter();
        }
        if (!instance.switched) {
            instance.switchTheme(Settings.getInstance().getProperty(
                    "emoticonTheme"));
        }
        return instance;
    }

    /**
     * Switches the emoticon theme
     *
     * @param theme
     *            the theme to switch to
     */
    public void switchTheme(String theme) {
        switched = true;

        map = new Properties();

        themeDir = theme;
        if (themeDir == null) {
            themeDir = "default";
        }

        emoticonStream = getClass().getClassLoader().getResourceAsStream(
                "imagethemes/emoticons/" + themeDir + '/' + "index.dat");

        if (emoticonStream == null) {
            com.valhalla.Logger.debug("Bad Emoticon File");
            return;
        }

        InputStreamReader in = new InputStreamReader(emoticonStream);
        BufferedReader reader = new BufferedReader(in);

        String line;
        String nameValue[] = new String[2];

        try {
            map.setProperty("(-)(-)", "fairy.gif");
            map.setProperty(":birdie:", "funny_bird.gif");
            while ((line = reader.readLine()) != null) {
                nameValue = line.split(" ");
                if (nameValue[0] == null || nameValue[1] == null) {
                    break;
                }

                nameValue[0] = nameValue[0].replaceAll("<", "&lt;");
                nameValue[0] = nameValue[0].replaceAll(">", "&gt;");
                map.setProperty(nameValue[0], nameValue[1]);
            }

            in.close();
            // close the file
        } catch (IOException e) {
            com.valhalla.Logger.debug("Couldn't read emoticon file.");
        }
    }

    /**
     * Replaces the different symbols with the images defined in the emote data
     * file
     *
     * @param text
     *            the text to modify
     * @return the modified text
     */
    public void replaceIcons(String text, StyledDocument doc, SimpleAttributeSet sas, JTextPane pane, boolean emotes) {
        if( emotes ) {
            if (emoticonStream == null) {
                return;
            }

            Iterator iterator = map.keySet().iterator();
            String symbol, image, imageLocation;

            while (iterator.hasNext()) {
                symbol = (String) iterator.next();

                image = map.getProperty(symbol);
                imageLocation = "imagethemes/emoticons/" + themeDir + "/" + image;
                if (symbol.equals("(-)(-)") || symbol.equals(":birdie:")) {
                    imageLocation = "imagethemes/emoticons/" + image;
                }

                StringBuffer newText = null;
                int l1 = 0, l2 = 0;

                while ((l2 = text.indexOf(symbol, l1)) != -1) {
                    if (newText == null)
                        newText = new StringBuffer(text.length() + 40);

                    String before = text.substring(l1, l2);
                    newText.append(before);

                    int after = l2 + symbol.length();
                    if ((l1 == l2 ||
                            before.endsWith(" ")) &&
                            (after == text.length() - 1 ||
                            text.startsWith(" ", after))) {
                        newText.append("<image_location=" + imageLocation + ">");
                    } else {
                        newText.append(symbol);
                    }
                    l1 = after;
                }

                if (newText != null) {
                    if (l1 < text.length())
                        newText.append(text.substring(l1));
                    text = newText.toString();
                }
            }

            int b = -1;
            while(true) {
                Matcher m = emoticon.matcher(text);
                if( !m.find() ) break;

                String i = m.group(1);
                b = text.indexOf("<image_location=" + i + ">");
                if(b == -1) break;
                String before = text.substring(0, b);
                before = checkHyperlink(before, doc, sas);

                ImageIcon icon = Standard.getIcon(i);
                try {
                    doc.insertString(doc.getLength(), before, sas);
                    SimpleAttributeSet set = new SimpleAttributeSet();
                    StyleConstants.setIcon(set, icon);
                    doc.insertString(doc.getLength(), "o", set);

                } catch( Exception e ) { }

                text = text.substring(b + ("<image_location=" + i + ">").length());

            }

            text = checkHyperlink(text, doc, sas);
        }

        try {
            doc.insertString(doc.getLength(), text, sas);
        } catch( Exception e){}
    }

    public String checkHyperlink(String text, StyledDocument doc, SimpleAttributeSet sas) {
        if(StyleConstants.getForeground(sas) != Color.BLACK) return text;

        Color link = new Color(91,88,188);

        while(true) {
            Matcher m = url.matcher(text.replaceAll("\n", " "));
            if(!m.find()) break;

            String address = m.group(2);

            int b = text.indexOf(address);
            if(b == -1) break;
            String before = text.substring(0, b);// + m.group(1);

            SimpleAttributeSet newSas = (SimpleAttributeSet)sas.clone();
            StyleConstants.setUnderline(newSas, true);
            Color test = (Color)sas.getAttribute(StyleConstants.Foreground);
            if( test != null && test == Color.BLACK ) StyleConstants.setForeground(newSas, link);
            try {
                doc.insertString(doc.getLength(), before, sas);
                doc.insertString(doc.getLength(), address, newSas);
                //doc.insertString(doc.getLength(), m.group(4), sas);
            } catch( Exception e ) { }

            text = text.substring(b + address.length());
        }

        while(true) {
            Matcher m = email.matcher(text.replaceAll("\n", " "));
            if(!m.find()) break;

            String address = m.group(2);

            int b = text.indexOf(address);
            if(b == -1) break;
            String before = text.substring(0, b);// + m.group(1);

            SimpleAttributeSet newSas = (SimpleAttributeSet)sas.clone();
            StyleConstants.setUnderline(newSas, true);

            Color test = (Color)sas.getAttribute(StyleConstants.Foreground);
            if( test != null && test == Color.BLACK ) StyleConstants.setForeground(newSas, link);


            try {
                doc.insertString(doc.getLength(), before, sas);
                doc.insertString(doc.getLength(), address, newSas);
                //doc.insertString(doc.getLength(), m.group(4), sas);
            } catch( Exception e ) { }

            text = text.substring(b + address.length());
        }

        return text;
    }

    /**
     * Shows a small window displaying all available emoticons.
     *
     * @param window
     *            the parent window
     * @param component
     *            the Component to display this frame over
     * @param area
     *            the text area that the emoticons will be placed on when the
     *            user clicks an image
     */
    public void displayEmoticonChooser(JFrame window, Component component,
            JTextComponent area) {
        if(themeDir.equals("no emoticons"))
        {
            Standard.warningMessage(null,"No Emoticons", "No emoticons to display.");
            return;
        }
        JDialog dialog = new JDialog(window);
        JPanel panel = (JPanel) dialog.getContentPane();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        EmoteClickListener listener = new EmoteClickListener(dialog, area);

        int columns = 6;
        int current = 0;
        JPanel cPanel = null;

        Iterator i = map.keySet().iterator();

        Properties displayed = new Properties();
        while (i.hasNext()) {
            String symbol = (String) i.next();

            if (symbol.equals("(-)(-)") || symbol.equals(":birdie:")) {
                continue;
            }

            String image = map.getProperty(symbol);

            if (displayed.getProperty(image) != null)
                continue;
            displayed.setProperty(image, "True");

            String imageLocation = "imagethemes/emoticons/" + themeDir + "/"
                    + image;
            ImageIcon icon = Standard.getIcon(imageLocation);
            if (icon == null) {
                continue;
            }

            if (current > columns) {
                panel.add(cPanel);
                current = 0;
            }

            if (current == 0) {
                cPanel = new JPanel();
                cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.X_AXIS));
            }

            JLabel label = new JLabel(icon);
            label.setName(symbol);
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            label.addMouseListener(listener);
            if (icon != null) {
                cPanel.add(label);
            }
            current++;
        }

        dialog.pack();
        dialog.setLocationRelativeTo(component);
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Listens for an emoticon to get clicked
     *
     * @author synic
     * @created October 30, 2004
     */
    class EmoteClickListener extends MouseAdapter {
        JDialog dialog;

        JTextComponent area;

        /**
         * Constructor for the EmoteClickListener object
         *
         * @param dialog
         *            the emote dialog that called this listener
         * @param area
         *            the textarea to append the emote symbol to
         */
        public EmoteClickListener(JDialog dialog, JTextComponent area) {
            this.dialog = dialog;
            this.area = area;
        }

        /**
         * Called by the mouse listener
         *
         * @param e
         *            the mouse event
         */
        public void mouseReleased(MouseEvent e) {
            dialog.dispose();
            JLabel label = (JLabel) e.getSource();
            String symbol = label.getName();
            area.setText(area.getText()
            + symbol.replaceAll("&gt;", ">").replaceAll("&lt;", "<")
            + " ");
            area.grabFocus();
        }
    }
}

