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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * Displays a splash screen for a second
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class SplashScreen extends JWindow {
    private javax.swing.Timer splashTimer = new javax.swing.Timer(1000,
            new SplashHandler());

    private JBotherLoader loader;

    private BufferedImage splash = null;

    public SplashScreen(JBotherLoader loader) {
        this.loader = loader;
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getClassLoader()
                    .getResourceAsStream("images/splashimage.png"));
        } catch (IOException ex) {
            loader.afterSplash();
            return;
        }

        createShadowPicture(image);

        setLocationRelativeTo(null);
        setVisible(true);
        toFront();
        splashTimer.start();
    }

    public void paint(Graphics g) {
        if (splash != null) {
            g.drawImage(splash, 0, 0, null);
        }
    }

    private void createShadowPicture(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int extra = 14;

        setSize(new Dimension(width + extra, height + extra));
        setLocationRelativeTo(null);
        Rectangle windowRect = getBounds();

        splash = new BufferedImage(width + extra, height + extra,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) splash.getGraphics();

        try {
            Robot robot = new Robot(getGraphicsConfiguration().getDevice());
            BufferedImage capture = robot.createScreenCapture(new Rectangle(
                    windowRect.x, windowRect.y, windowRect.width + extra,
                    windowRect.height + extra));
            g2.drawImage(capture, null, 0, 0);
        } catch (AWTException e) {
        }

        BufferedImage shadow = new BufferedImage(width + extra, height + extra,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = shadow.getGraphics();
        g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.3f));
        g.fillRoundRect(6, 6, width, height, 12, 12);

        g2.drawImage(shadow, getBlurOp(7), 0, 0);
        g2.drawImage(image, 0, 0, this);
    }

    private ConvolveOp getBlurOp(int size) {
        float[] data = new float[size * size];
        float value = 1 / (float) (size * size);
        for (int i = 0; i < data.length; i++) {
            data[i] = value;
        }
        return new ConvolveOp(new Kernel(size, size, data));
    }

    /**
     * Closes the SplashScreen Closes the SplashScreen after the time has
     * expired
     *
     * @author Adam Olsen
     * @version 1.0
     */
    class SplashHandler implements ActionListener {
        /**
         * Called by the <code>javax.swing.Timer</code>
         */
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if( !loader.done() )
                    {
                        com.valhalla.Logger.debug( "Loader is not done" );
                        splashTimer.restart();
                        return;
                    }
                    splashTimer.stop();
                    setVisible(false);
                    loader.afterSplash();
                    dispose();
                }
            });
        }
    }
}