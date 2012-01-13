/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 * Customized for Systray JBother plugin by Yury Soldak
 * Jan 07 2005
 *
 */
 
package org.jdesktop.jdic.tray.internal;


import org.jdesktop.jdic.tray.internal.impl.*;


/**
 * The <code>ServiceManager</code> class provides static fields to refer to 
 * the available services, and static methods to get the approprate service 
 * objects with the given service name. This class is abstract and final and 
 * cannot be instantiated.
 */
public class ServiceManager {
  
    /**
     * Constant name for looking up the SystemTray service object.
     */
    public static final String SYSTEM_TRAY_SERVICE = "SystemTrayService";

    /**
     * Constant name for looking up the TrayIcon service object.
     */
    public static final String TRAY_ICON_SERVICE = "TrayIconService";

    /**
     * Constant name for looking up the TrayApplet service object.
     */
    public static final String TRAY_APPLET_SERVICE = "TrayAppletService";
    
    /**
     * Suppress default constructor for noninstantiability.
     */
    private ServiceManager() {}
  
    /**
     * Gets a service object with the given name. The given service name should be one 
     * of the pre-defined service names.
     * 
     * @param  serviceName the given service name.
     * @return the appropriate service object.
     * @throws NullPointerException if the given service name is null.
     */
    public static Object getService(String serviceName) 
            throws NullPointerException {
        if (serviceName == null) { 
            throw new NullPointerException("Service name is null.");
        }
	    Object service = null;

	    if( System.getProperty( "os.name" ).startsWith( "Windows" ) ) {

		    if      ( serviceName == SYSTEM_TRAY_SERVICE )
	        service = new WinSystemTrayService();
		    else if ( serviceName == TRAY_ICON_SERVICE )
			    service = new WinTrayIconService();

	    } else if ( System.getProperty( "os.name" ).startsWith( "Linux" ) ) {

		    if      ( serviceName == SYSTEM_TRAY_SERVICE )
	        service = new GnomeSystemTrayService();
		    else if ( serviceName == TRAY_ICON_SERVICE )
			    service = new GnomeTrayIconService();
		    else if ( serviceName == TRAY_APPLET_SERVICE )
			    service = new GnomeTrayAppletService();

	    } else {
		    System.out.println("SystemTray: Your OS " + System.getProperty( "os.name" ) + " is not supported.");
	    }
        return service;
    }
}
