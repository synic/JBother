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

package com.valhalla.jbother.sound;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.valhalla.jbother.BuddyList;
import com.valhalla.jbother.JBother;
import com.valhalla.settings.Settings;

/**
 * Plays sounds with different methods. Available methods are: with Java Sound
 * System, a system command, or a pc speaker beep
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class SoundPlayer {
    private Thread thread;

    private static SoundPlayer instance;

    private static boolean running = false;
    protected static javax.swing.Timer timer = new javax.swing.Timer( 500, new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                timer.stop();
            }
        } );

    /**
     * Constructor is private, because this is a Singleton
     */
    private SoundPlayer() {
    }

    /**
     * Plays a sound using the method in the settings
     *
     * @param settingName
     *            the setting name to play
     */
    public static void play(String settingName) {
        if( Settings.getInstance().getBoolean("noSound")) return;
        if( BuddyList.getInstance().getCurrentPresenceMode() == org.jivesoftware.smack.packet.Presence.Mode.DO_NOT_DISTURB ) return;

        if (!Settings.getInstance().getBoolean(settingName + "Play"))
            return;


        if(timer.isRunning()) {
            return;
        }
        else timer.start();

        String method = Settings.getInstance().getProperty("soundMethod");
        if (method == null)
            method = "";

        if (method.equals("Console Beep")) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        String strFilename = Settings.getInstance().getProperty(settingName);

        if (strFilename == null || strFilename.equals("")) {
            com.valhalla.Logger.debug("no file to play");
            return;
        }

        if (strFilename.equals("(default)")) {
            strFilename = loadDefault(settingName);
            if (strFilename == null)
                return;
        }

        if (method.equals("Command")) {
		if( running ) return;
		else running = true;
            String c = Settings.getInstance().getProperty("soundCommand");
            if (c.indexOf("%s") > -1)
                c = c.replaceAll("%s", strFilename);
            else
                c = c + " " + strFilename;

            try {
                Runtime.getRuntime().exec(c);
            } catch (java.io.IOException e) {
            }
	    running = false;

            return;
        }

        if (instance == null)
            instance = new SoundPlayer();
        if (instance.running)
            return;

        instance.running = true;
        instance.thread = new Thread(new SoundPlayerThread(instance,
                strFilename));
        try {
            instance.thread.start();
        } catch (Exception ex) {
            instance.running = false;
        }
    }

    public static boolean playSoundFile(String file, String method,
            String soundCommand) {

        if(timer.isRunning()) return true;
        else timer.start();

        if (method.equals("Console Beep")) {
            Toolkit.getDefaultToolkit().beep();
            return true;
        }

        if (method.equals("Command")) {
            String c = soundCommand;
            if (c.indexOf("%s") > -1)
                c = c.replaceAll("%s", file);
            else
                c = c + " " + file;

            try {
                Runtime.getRuntime().exec(c);
            } catch (java.io.IOException e) {
            }
            return true;
        }

        File f = new File(file);
        if (!f.exists())
            return false;
        if (instance == null)
            instance = new SoundPlayer();
        if (instance.running)
            return true;

        instance.running = true;
        instance.thread = new Thread(new SoundPlayerThread(instance, file));
        try {
            instance.thread.start();
        } catch (Exception ex) {
            instance.running = false;
            return false;
        }
        return true;
    }

    /**
     * Set the "running" state to false
     */
    protected void nullIt() {
        running = false;
    }

    /**
     * Loads a default sound from the running jar file and puts it into a cache
     *
     * @param settingName
     *            the setting to load
     */
    public static String loadDefault(String settingName) {
        String defaultDir = Settings.getInstance().getProperty( "defaultSoundSet", "default" );

        try {
            File cacheDir = new File(JBother.settingsDir + File.separatorChar
                    + "soundcache" + File.separatorChar + defaultDir );
            if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
                com.valhalla.Logger
                        .debug("Could not create sound cache directory.");
                return null;
            }

            File outPutFile = new File(cacheDir.getPath() + File.separatorChar
                    + settingName + ".wav");
            if (outPutFile.exists())
                return outPutFile.getPath();

            InputStream file = BuddyList.getInstance().getClass()
                    .getClassLoader().getResourceAsStream(
                            "sounds/" + defaultDir + "/" + settingName + ".wav");
            if (file == null) {
                com.valhalla.Logger
                        .debug("Could not find default sound file in resources for "
                                + settingName);
                return null;
            }

            FileOutputStream out = new FileOutputStream(outPutFile);

            byte data[] = new byte[1024];
            while (file.available() > 0) {
                int size = file.read(data);
                out.write(data, 0, size);
            }

            file.close();
            out.close();

            return outPutFile.getPath();
        } catch (IOException ex) {
            com.valhalla.Logger.debug(ex.getMessage());
        }

        return null;
    }

    /*public static void clearCache()
    {
        try {
            File cacheDir = new File(JBother.profileDir + File.separatorChar
                + "soundcache");
            if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
                com.valhalla.Logger
                        .debug("Could not create sound cache directory.");
                return;
            }

            File files[] = cacheDir.listFiles();
            for( int i = 0; i < files.length; i++ )
            {
                File file = files[i];
                if( file.getName().endsWith( ".wav" ) )
                {
                    file.delete();
                }
            }
        }
        catch( Exception e ) { com.valhalla.Logger.logException( e ); }
    }*/
}

/**
 * Plays a sound using the Java Sound System
 *
 * @author jresources.org
 * @version ?
 */

class SoundPlayerThread implements Runnable {
    private static final int EXTERNAL_BUFFER_SIZE = 128000;

    private String strFilename;

    private SoundPlayer player;

    /**
     * Sets up the thread with a specified sound
     *
     * @param player
     *            the calling player
     * @param file
     *            the .wav file to play
     */
    public SoundPlayerThread(SoundPlayer player, String file) {
        this.player = player;
        this.strFilename = file;
    }

    public void run() {
        File soundFile = new File(strFilename);

        //this code taken from jseresources.org. Thanks!

        /*
         * We have to read in the sound file.
         */
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (Exception e) {
            /*
             * In case of an exception, we dump the exception including the
             * stack trace to the console output. Then, we exit the program.
             */
            com.valhalla.Logger.logException(e);
        }

        /*
         * From the AudioInputStream, i.e. from the sound file, we fetch
         * information about the format of the audio data. These information
         * include the sampling frequency, the number of channels and the size
         * of the samples. These information are needed to ask Java Sound for a
         * suitable output line for this audio file.
         */
        AudioFormat audioFormat = audioInputStream.getFormat();

        /*
         * Asking for a line is a rather tricky thing. We have to construct an
         * Info object that specifies the desired properties for the line.
         * First, we have to say which kind of line we want. The possibilities
         * are: SourceDataLine (for playback), Clip (for repeated playback) and
         * TargetDataLine (for recording). Here, we want to do normal playback,
         * so we ask for a SourceDataLine. Then, we have to pass an AudioFormat
         * object, so that the Line knows which format the data passed to it
         * will have. Furthermore, we can give Java Sound a hint about how big
         * the internal buffer for the line should be. This isn't used here,
         * signaling that we don't care about the exact size. Java Sound will
         * use some default value for the buffer size.
         */
        SourceDataLine line = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                audioFormat);
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);

            /*
             * The line is there, but it is not yet ready to receive audio data.
             * We have to open the line.
             */
            line.open(audioFormat);
        } catch (LineUnavailableException e) {
            com.valhalla.Logger.logException(e);
        } catch (Exception e) {
            com.valhalla.Logger.logException(e);
        }

        if (line == null) {
            player.nullIt();

            return;
        }

        /*
         * Still not enough. The line now can receive data, but will not pass
         * them on to the audio output device (which means to your sound card).
         * This has to be activated.
         */
        line.start();

        /*
         * Ok, finally the line is prepared. Now comes the real job: we have to
         * write data to the line. We do this in a loop. First, we read data
         * from the AudioInputStream to a buffer. Then, we write from this
         * buffer to the Line. This is done until the end of the file is
         * reached, which is detected by a return value of -1 from the read
         * method of the AudioInputStream.
         */
        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
        while (nBytesRead != -1) {
            try {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                com.valhalla.Logger.logException(e);
            }
            if (nBytesRead >= 0) {
                int nBytesWritten = line.write(abData, 0, nBytesRead);
            }
        }

        /*
         * Wait until all data are played. This is only necessary because of the
         * bug noted below. (If we do not wait, we would interrupt the playback
         * by prematurely closing the line and exiting the VM.)
         *
         * Thanks to Margie Fitch for bringing me on the right path to this
         * solution.
         */
        line.drain();

        /*
         * All data are played. We can close the shop.
         */
        line.close();

        player.nullIt();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }

        return;
    }
}
