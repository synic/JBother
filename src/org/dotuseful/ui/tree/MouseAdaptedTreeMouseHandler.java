/*
 * Created on 21/7/2004
 * 
 * Copyright (C) 2004 Denis Krukovsky. All rights reserved.
 * ====================================================================
 * The Software License (based on Apache Software License, Version 1.1)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Denis Krukovsky (dkrukovsky at yahoo.com)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "dot useful" and "Denis Krukovsky" must not be used to
 *    endorse or promote products derived from this software without
 *    prior written permission. For written permission, please
 *    contact dkrukovsky at yahoo.com.
 *
 * 5. Products derived from this software may not be called "useful",
 *    nor may "useful" appear in their name, without prior written
 *    permission of Denis Krukovsky.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL JIVE SOFTWARE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

package org.dotuseful.ui.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * A mouse handler for MouseAdaptedTree which transmits mouse events to
 * corresponding tree nodes. Tree nodes must implement MouseListener interface.
 * <br>
 * Unfortunately you can't transmit mouseEntered and mouseExited events in easy
 * way because these events are fired for JTree component at all.
 * 
 * @author Denis Krukovsky
 */
public class MouseAdaptedTreeMouseHandler extends MouseAdapter {
    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
        MouseListener node = getNode(e);
        if (node != null) {
            node.mouseClicked(e);
        }
    }

    public void mousePressed(MouseEvent e) {
        MouseListener node = getNode(e);
        if (node != null) {
            node.mousePressed(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        MouseListener node = getNode(e);
        if (node != null) {
            node.mouseReleased(e);
        }
    }

    /**
     * Returns a tree node which is mouse event on.
     * 
     * @param e
     *            a mouse event to calculate the node to
     * @return a tree node which is mouse event on, or null if there is no node.
     */
    protected MouseListener getNode(MouseEvent e) {
        JTree tree = getTree(e);
        int x = e.getX();
        int y = e.getY();
        TreePath path = tree.getPathForLocation(x, y);
        if (path != null) {
            return (MouseListener) (path.getLastPathComponent());
        } else {
            return null;
        }
    }

    protected JTree getTree(MouseEvent e) {
        return (JTree) (e.getSource());
    }
}