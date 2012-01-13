/*
 * Copyright (C) 2003 Adam Olsen
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 1, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

package com.valhalla.gui;

import javax.swing.JTextField;

/**
 * A JTextField with a copy/paste context menu
 * @author Adam Olsen
**/
public class MJTextField extends JTextField {
    public MJTextField(String text) {
        super(text);
        CopyPasteContextMenu.registerComponent(this);
    }

    public MJTextField(int cols) {
        super(cols);
        CopyPasteContextMenu.registerComponent(this);
    }

    public MJTextField() {
        CopyPasteContextMenu.registerComponent(this);
    }
}