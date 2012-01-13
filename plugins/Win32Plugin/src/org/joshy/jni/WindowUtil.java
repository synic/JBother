package org.joshy.jni;

import java.awt.Component;
import java.awt.Canvas;

import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import com.valhalla.pluginmanager.*;

/**
 * Class to utilize the WindowUtil, created by Joshua Marinacci
 * http://today.java.net/pub/a/today/2003/12/08/swing.html
 * Causes taskbar to notify user of new messages on win32 platform.
 */
public class WindowUtil extends Canvas {

	/**
	 * Static boolean to determine if the library was successfully loaded.
	 */
	static final boolean isWindows = System.getProperty( "os.name" ).startsWith( "Windows" );
	private static boolean libraryLoaded = false;

	static {
		try {
			System.loadLibrary("WindowUtil.dll");
			libraryLoaded = true;
			// TODO: Fix exception handling (this is just a catch all for now)
		} catch (Exception e) {
			e.printStackTrace();
			// On exception assume library failed loading
			libraryLoaded = false;
		} catch (Error er) {
			er.printStackTrace();
			libraryLoaded = false;
		}

	}

	// the actual native method
	/**
	 * Private native stub for the flash method, implemented in WindowUtil.dll
	 *
	 * @param c Component (usually JFrame) to notify
	 * @param bool
	 */
	private static native void flash(Component c, boolean bool);

	/**
	 * Public static method to cause frame to flash (checks if library was loaded successfully)
	 *
	 * @param c Component (usually JFrame) to notify
	 * @param bool
	 */
	public static void doFlash(Component c, boolean bool) {
		if (libraryLoaded) {
			flash(c, bool);
		}
	}

}
