/*
 * Created on 20/6/2004
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

/**
 * @author dkrukovsky
 *
 */
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * AutomatedTreeModel extends DefaultTreeModel and uses AutomatedTreeNodes as
 * its nodes.
 */
public class AutomatedTreeModel extends DefaultTreeModel implements
        TreeModelListener {

    /**
     * Creates an AutomatedTreeModel in which any node can have children.
     * 
     * @param root
     *            an AutomatedTreeNode object that is the root of the tree
     * @see #AutomatedTreeModel(AutomatedTreeNode, boolean)
     */
    public AutomatedTreeModel(AutomatedTreeNode root) {
        this(root, false);
    }

    /**
     * Creates an AutomatedTreeModel specifying whether any node can have
     * children, or whether only certain nodes can have children.
     * 
     * @param root
     *            an AutomatedTreeNode object that is the root of the tree
     * @param asksAllowsChildren
     *            a boolean, false if any node can have children, true if each
     *            node is asked to see if it can have children
     * @see #asksAllowsChildren
     */
    public AutomatedTreeModel(AutomatedTreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
        if (root != null) {
            root.addTreeModelListener(this);
        }
    }

    /**
     * Sets the root to <code>root</code>. A null <code>root</code> implies
     * the tree is to display nothing, and is legal.
     */
    public void setRoot(TreeNode root) {
        AutomatedTreeNode oldRoot = (AutomatedTreeNode) getRoot();
        if (oldRoot != null) {
            oldRoot.removeTreeModelListener(this);
        }
        super.setRoot(root);
        if (root != null) {
            ((AutomatedTreeNode) root).addTreeModelListener(this);
        }
    }

    /**
     * <p>
     * Invoked after a node (or a set of siblings) has changed in some way. The
     * node(s) have not changed locations in the tree or altered their children
     * arrays, but other attributes have changed and may affect presentation.
     * Example: the name of a file has changed, but it is in the same location
     * in the file system.
     * </p>
     */
    public void treeNodesChanged(TreeModelEvent e) {
        fireTreeNodesChanged(e.getSource(), e.getPath(), e.getChildIndices(), e
                .getChildren());
    }

    /**
     * <p>
     * Invoked after nodes have been inserted into the tree.
     * </p>
     */
    public void treeNodesInserted(TreeModelEvent e) {
        fireTreeNodesInserted(e.getSource(), e.getPath(), e.getChildIndices(),
                e.getChildren());
    }

    /**
     * <p>
     * Invoked after nodes have been removed from the tree. Note that if a
     * subtree is removed from the tree, this method may only be invoked once
     * for the root of the removed subtree, not once for each individual set of
     * siblings removed.
     * </p>
     */
    public void treeNodesRemoved(TreeModelEvent e) {
        fireTreeNodesRemoved(e.getSource(), e.getPath(), e.getChildIndices(), e
                .getChildren());
    }

    /**
     * <p>
     * Invoked after the tree has drastically changed structure from a given
     * node down. If the path returned by e.getPath() is of length one and the
     * first element does not identify the current root node the first element
     * should become the new root of the tree.
     * <p>
     */
    public void treeStructureChanged(TreeModelEvent e) {
        fireTreeStructureChanged(e.getSource(), e.getPath(), e
                .getChildIndices(), e.getChildren());
    }
}