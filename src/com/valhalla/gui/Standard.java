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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Some common utility functions for UI development
 *
 * @author Adam Olsen
 * @created November 30, 2004
 * @version 1.0
 */
public class Standard {
    private static Standard instance;

    private int currentX = 20;

    private int currentY = 40;

    private ResourceBundle bundle = null;

    /**
     * Constructor is private. Only the static methods should be used.
     */
    private Standard() {
    }

    /**
     * Aplies a font to a container and all of it's child components
     *
     * @param component
     *            the component to apply the font to
     * @param font
     *            the font to apply
     */
    public static void recursivelyApplyFont(Component component, Font font) {
        component.setFont(font);
        if (!(component instanceof Container)) {
            return;
        }

        Component components[] = ((Container) component).getComponents();
        for (int i = 0; i < components.length; i++) {
            recursivelyApplyFont(components[i], font);
        }
    }

    /**
     * Displays a warning dialog
     *
     * @param parent
     *            this dialog's parent
     * @param title
     *            the dialog title
     * @param message
     *            the message
     * @return Description of the Return Value
     */
    public static boolean warningMessage(Container parent, String title,
            String message) {
        JOptionPane.showMessageDialog(parent, message, title,
                JOptionPane.WARNING_MESSAGE);
        return false;
    }

    /**
     * Displays a notice dialog
     *
     * @param parent
     *            this dialog's parent
     * @param title
     *            the dialog title
     * @param message
     *            the message
     */
    public static void noticeMessage(Container parent, String title,
            String message) {
        JOptionPane.showMessageDialog(parent, message, title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     */
    public static URL getURL(String resource) {
        if (instance == null) {
            instance = new Standard();
        }
        URL resourceUrl = instance.getClass().getClassLoader().getResource(
                resource);
        if (resourceUrl == null) {
            return null;
        }

        return resourceUrl;
    }

    /**
     * Gets an Image from the resources (usually the jar or current directory
     * that the app is running from)
     *
     * @param icon
     *            the name of the image to get
     * @return the requested image, or <tt>null</tt> if it could not be found
     */
    public static Image getImage(String icon) {
        if (instance == null) {
            instance = new Standard();
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        URL imageUrl = instance.getClass().getClassLoader().getResource(
                "images/" + icon);
        if (imageUrl == null) {
            return null;
        }

        return toolkit.createImage(imageUrl);
    }

    /**
     * Gets an Icon from the resources (usually the jar or current directory
     * that the app is running from)
     *
     * @param icon
     *            the name of the image to get
     * @return the requested icon, or <tt>null</tt> if it could not be found
     */
    public static ImageIcon getIcon(String icon) {
        if (instance == null) {
            instance = new Standard();
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        URL iconUrl = instance.getClass().getClassLoader().getResource(icon);

        if (iconUrl == null) {
            com.valhalla.Logger.debug("Could not find an image for " + icon);
            return null;
        }

        return new ImageIcon(iconUrl);
    }

    /**
     * Places a Frame on window in a cascade fashion. Tracks the last location
     * of the last window to produce the cascade effect
     *
     * @param container
     *            the container to cascade
     */
    public static void cascadePlacement(Container container) {
        if (instance == null) {
            instance = new Standard();
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension sSize = toolkit.getScreenSize();
        Dimension cSize = container.getSize();

        if (instance.currentX + cSize.width > sSize.width - 50) {
            instance.currentX = 20;
        }
        if (instance.currentY + cSize.height > sSize.height - 50) {
            instance.currentY = 40;
        }

        container.setLocation(instance.currentX, instance.currentY);
        instance.currentX += 100;
        instance.currentY += 80;
    }

    /**
     * Sets the default resource bundle to be used with the Standard lib
     *
     * @param bundle
     *            The new bundle value
     */
    public static void setBundle(ResourceBundle bundle) {
        if (instance == null) {
            instance = new Standard();
        }

        instance.bundle = bundle;
    }

    /**
     * Throws an error if a field is blank
     *
     * @param field
     *            the value of the field
     * @param name
     *            the name of the field
     * @exception Exception
     *                to be caught if the field was blank
     */
    public static void assure(String field, String name) throws Exception {
        if (field == null || field.equals("")) {
            String message = MessageFormat.format(instance.bundle
                    .getString("fieldBlank"), new Object[] { name });
            warningMessage(null, "Error", message);
            throw new Exception(message);
        }
    }
}

