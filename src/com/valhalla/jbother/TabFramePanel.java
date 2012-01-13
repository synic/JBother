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

import java.awt.Font;

import javax.swing.JComponent;
import net.infonode.tabbedpanel.*;
import net.infonode.tabbedpanel.titledtab.*;
import net.infonode.util.*;


/**
 * This interface describes the panels that are used in the TabFrame
 *
 * @author Adam Olsen
 * @version 1.0
 * @see com.valhalla.jbother.TabFrame
 */
public interface TabFramePanel {
    public String getWindowTitle();
    public String getTooltip();
    public void updateStyle(Font font);
    public String getPanelName();
    public String getPanelToolTip();
    public boolean listenersAdded();
    public JComponent getInputComponent();
    public void setListenersAdded(boolean added);
    public void setTab( TitledTab tab );
    public TitledTab getTab();
}
