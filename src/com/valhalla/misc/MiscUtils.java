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
package com.valhalla.misc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;

/**
 * Miscellaneous tools good for any application
 * 
 * @author synic
 * @created November 30, 2004
 */
public class MiscUtils {
    /**
     * Deletes a directory, and all the files in it
     * 
     * @param dir
     *            the directory to delete
     * @exception Exception
     *                thrown if there is an error deleting the dir
     */
    public static void recursivelyDeleteDirectory(String dir) throws Exception {
        File file = new File(dir);
        if (!file.isDirectory() || !file.exists()) {
            throw new Exception(dir
                    + " was not a directory, could not recursively delete it");
        }

        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                recursivelyDeleteDirectory(files[i].getPath());
            } else {
                if (!files[i].delete()) {
                    throw new Exception("Could not delete " + files[i] + ".");
                }
            }
        }

        if (!file.delete()) {
            throw new Exception("Could not delete " + file + ".");
        }
    }

    /**
     * Gets stream encoding
     * 
     * @return stream encoding.
     */
    public static String streamEncoding() {
        OutputStreamWriter out = new OutputStreamWriter(
                new ByteArrayOutputStream());
        return out.getEncoding();
    }
}

