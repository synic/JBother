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

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * A tree control which transmits mouse events to its nodes. Its nodes must
 * implement MouseListener interface.
 * 
 * To work with this tree, you <br>- provide it with TreeModel which contains
 * tree nodes which implement MouseListener, or <br>- provide it with TreeNode
 * root which and all its descendants are implement MouseListener.
 * 
 * @author Denis Krukovsky
 *  
 */
public class MouseAdaptedTree extends JTree {
    /**
     * Returns an instance of <code>MouseAdaptedTree</code> which displays the
     * root node -- the tree is created using the specified data model.
     * 
     * @param newModel
     *            the <code>TreeModel</code> to use as the data model
     */
    public MouseAdaptedTree(TreeModel newModel) {
        super(newModel);
        addMouseHandler();
    }

    /**
     * Returns a <code>MouseAdaptedTree</code> with the specified
     * <code>TreeNode</code> as its root, which displays the root node. By
     * default, the tree defines a leaf node as any node without children.
     * 
     * @param root
     *            a <code>TreeNode</code> object
     * @see DefaultTreeModel#asksAllowsChildren
     */
    public MouseAdaptedTree(TreeNode root) {
        super(root);
        addMouseHandler();
    }

    /**
     * Returns a <code>MouseAdaptedTree</code> with the specified
     * <code>TreeNode</code> as its root, which displays the root node and
     * which decides whether a node is a leaf node in the specified manner.
     * 
     * @param root
     *            a <code>TreeNode</code> object
     * @param asksAllowsChildren
     *            if false, any node without children is a leaf node; if true,
     *            only nodes that do not allow children are leaf nodes
     * @see DefaultTreeModel#asksAllowsChildren
     */
    public MouseAdaptedTree(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
        addMouseHandler();
    }

    /**
     * Adds MouseAdaptedTreeMouseHandler mouse listener to transmit mouse events
     * to corresponding nodes.
     */
    protected void addMouseHandler() {
        addMouseListener(new MouseAdaptedTreeMouseHandler());
    }
}