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

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;

/**
 * A JTextArea with a copy/paste context menu.  Also includes an
 * optional history manager - IE, pressing the up and down arrows
 * will bring up different history items
 *
 * @author Adam Olsen
**/
public class MJTextArea extends JTextArea
{
    private Vector history = new Vector();
    private int currentLine = 0;

    boolean h = false;
    public MJTextArea(boolean history, int rows, int cols)
    {
        super(rows, cols);
        this.h = history;
	if(h) addKeyListener(new ArrowListener());
        CopyPasteContextMenu.registerComponent(this);
        initializeKeyboardBindings();
    }

    public MJTextArea( boolean history )
    {
        this(history, 0, 0);
    }

    public MJTextArea()
    {
        this(false, 0,0);
    }

    public MJTextArea(int rows, int cols)
    {
        this(false, rows, cols);
    }

    void clearText()
    {
        super.setText("");
    }

    public void setText(String text)
    {
        if( h )
        {
            checkHistoryAdd(getText());
        }
        super.setText(text);
    }

    /**
     * Sets the text without affecting the history
    **/
    public void setNewText(String text)
    {
        super.setText(text);
    }

    /**
     * adds keyboard action to this MJTextArea
     * @param aKeyEventAction to add
     */
    public void addKeyboardBindingAction(KeyBindingAction aKeyEventAction)
    {
        getInputMap().put(KeyStroke.getKeyStroke(aKeyEventAction.getKeyEvent(),
            aKeyEventAction.getKeyModifier()), aKeyEventAction.getName());
        getActionMap().put(aKeyEventAction.getName(), aKeyEventAction);
    }

    /**
     * initialization of "default" keybindings. As default JTextArea that MJTextArea inherits
     * from does not contain all handy shortcuts, they can be added here
     */
    private void initializeKeyboardBindings()
    {
        // add Ctrl-Backspace binding to delete the (rest) of current word
        addKeyboardBindingAction(new KeyBindingAction("ctrlBackspace",
            java.awt.event.KeyEvent.VK_BACK_SPACE,
            java.awt.Event.CTRL_MASK)
        {
            public void actionPerformed(ActionEvent e)
            {
                int caretPosition = getCaretPosition();
                String contents = getText();
                if(contents.length() == 0) return;
                String stringBeforeCaret = contents.substring(0,caretPosition - 1);
                String stringAfterCaret = contents.substring(caretPosition,contents.length());

                int lastSpaceIndex = stringBeforeCaret.lastIndexOf(' ');
                int lastNLIndex = stringBeforeCaret.lastIndexOf('\n');

                int lastIndex = (lastSpaceIndex > lastNLIndex ? lastSpaceIndex : lastNLIndex);
                String modifiedStringBeforeCaret = stringBeforeCaret.substring(0, lastIndex + 1 );
                int newCaretPos = modifiedStringBeforeCaret.length();
                setText(modifiedStringBeforeCaret + stringAfterCaret);
                setCaretPosition(newCaretPos);
                repaint();
            }
        });
    }

    /**
     * simple abstract class to encapsulate key bindings and their actions. The reason for it is
     * to have all the information on key binding: key event, modifier and the action in one place
     */
    abstract class KeyBindingAction extends AbstractAction
    {
        private int keyEvent;
        private int keyModifier;

        public KeyBindingAction(String name, int aKeyEvent, int aKeyModifier)
        {
            super(name);
            keyEvent = aKeyEvent;
            keyModifier = aKeyModifier;
        }

        public int getKeyEvent()
        {
            return keyEvent;
        }

        public int getKeyModifier()
        {
            return keyModifier;
        }

        public String getName()
        {
            return (String) getValue(Action.NAME);
        }
    }

    boolean checkHistoryAdd(String text)
    {
        if(text.trim().equals(""))
        {
            return false;
        }
        currentLine = history.size();
        if( history.size() != 0 )
        {
            String last = (String)history.get(history.size() - 1);
            if(last.equals(text)) return false;
        }

        history.add(text);
        currentLine = history.size();

	return true;
    }

    /**
     *  Description of the Class
     *
     *@author     synic
     *@created    September 9, 2005
     */
    class ArrowListener extends KeyAdapter {
        /**
         *  Description of the Method
         *
         *@param  e  Description of the Parameter
         */
        public void keyPressed(KeyEvent e) {
            if ( (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP )
                && e.isControlDown()) {

                if(e.getKeyCode() == KeyEvent.VK_UP && currentLine >= history.size())
                {
                    if(checkHistoryAdd(getText())) currentLine--;
                }

                if(e.getKeyCode() == KeyEvent.VK_DOWN && currentLine >= history.size())
                {
                    checkHistoryAdd(getText());
                }

                e.consume();
                int add = 0;
                if(e.getKeyCode() == KeyEvent.VK_DOWN) add++;
                else add--;

                currentLine += add;
                if( currentLine >= history.size())
                {
                    clearText();
                    currentLine = history.size();
                    return;
                }

                if( currentLine < 0 )
                {
                    currentLine = 0;
                }

                String t = (String)history.get(currentLine);
                setNewText(t.replaceAll("\n$", ""));
            }
        }
    }
}
