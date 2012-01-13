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
 */


/**
 * The <code>SystemTrayService</code> interface is the contract for a native 
 * SystemTray implementation.
 *
 */

package org.jdesktop.jdic.tray.internal;


import org.jdesktop.jdic.tray.TrayIcon;


public interface SystemTrayService {

    public void addNotify();
    public void addTrayIcon(TrayIcon ti, TrayIconService tis, int trayIndex);
    public void removeTrayIcon(TrayIcon ti, TrayIconService tis, int trayIndex);

}
