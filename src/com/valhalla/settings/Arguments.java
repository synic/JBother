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

package com.valhalla.settings;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a singleton class. setArguments can only be called once. It can only
 * have one reference to it, which is created inside the class.
 * 
 * @author Adam Olsen
 * @version 1.0
 */
public class Arguments extends Properties {
    private static Arguments instance;

    /**
     * Initializes the arguments class with the CLI arguments
     * 
     * @param args
     *            to be passed in from main( String args )
     */
    public static void setArguments(String args[]) {
        if (instance != null) {
            com.valhalla.Logger
                    .debug("WARNING: Arguments was already initiated, and an attempt to initiate it again was just made.  This attempt was ignored.");
        } else {
            instance = new Arguments(args);
        }
    }

    /**
     * Gets the Arguments instance
     * 
     * @return the Arguments instance
     */
    public static Arguments getInstance() {
        return instance;
    }

    public boolean getBoolean(String key) {
        String value = getProperty(key);
        if (value == null || value.equals("false"))
            return false;
        else
            return true;
    }

    /**
     * Parses the arguments
     * 
     * @params args passed in from setArguments
     */
    private Arguments(String args[]) {
        instance = this;
        String nameValue[] = new String[2];
        String keyValue;
        Matcher matcher;

        for (int i = 0; i < args.length; i++) {
            // parse the arguments into name value pairs
            nameValue = args[i].split("=");
            if (nameValue.length == 1)
                nameValue = new String[] { nameValue[0], "true" };
            matcher = Pattern.compile("-").matcher(nameValue[0]);
            keyValue = matcher.replaceAll("");
            com.valhalla.Logger.debug("Argument> " + keyValue + "="
                    + nameValue[1]);
            setProperty(keyValue, nameValue[1]);
        }
    }
}