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

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * AutomatedTreeNode extends DefaultMutableTreeNode adding support for automatic
 * notification of node changes. AutomatedTreeNodes are used with
 * AutomatedTreeModel. In this model each node considered as a little tree which
 * can fire TreeModel events about its changes. Each parent node registers
 * itself as a listener of its child nodes events and transfers events to its
 * own listeners (which is its parent) up to the root node. You can use
 * AutomatedTreeModel which automatically handles events and fires them as usual
 * TreeModel.
 * <p>
 * A sample of code that uses DefaultTreeModel and DefaultTreeNodes <code>
 * setUserObject( event.getObject() );
 * //getting a tree model from somewhere
 * DefaultTreeModel model = ( DefaultTreeModel ) titleTree.getModel();
 * model.nodeChanged( node );
 * </code>
 * <p>
 * A sample of code that uses AutomatedTreeModel and AutomatedTreeNodes <code>
 * setUserObject( event.getObject() );
 * //Everything else automated
 * </code>
 * <b>This is not a thread safe class. </b> If you intend to use an
 * AutomatedTreeNode (or a tree of TreeNodes) in more than one thread, you need
 * to do your own synchronizing. A good convention to adopt is synchronizing on
 * the root node of a tree.
 * <p>
 * 
 * @author dkrukovsky
 */
public class AutomatedTreeNode extends DefaultMutableTreeNode implements
        TreeModelListener {
    /** Listeners. */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Creates an AutomatedTreeNode that has no parent and no children, but
     * which allows children.
     */
    public AutomatedTreeNode() {
    }

    /**
     * Creates an AutomatedTreeNode node with no parent, no children, but which
     * allows children, and initializes it with the specified user object.
     * 
     * @param userObject
     *            an Object provided by the user that constitutes the node's
     *            data
     */
    public AutomatedTreeNode(Object userObject) {
        super(userObject);
    }

    /**
     * Creates an AutomatedTreeNode with no parent, no children, initialized
     * with the specified user object, and that allows children only if
     * specified.
     * 
     * @param userObject
     *            an Object provided by the user that constitutes the node's
     *            data
     * @param allowsChildren
     *            if true, the node is allowed to have child nodes -- otherwise,
     *            it is always a leaf node
     */
    public AutomatedTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    /**
     * Removes <code>newChild</code> from its present parent (if it has a
     * parent), sets the child's parent to this node, adds the child to this
     * node's child array at index <code>childIndex</code>, fires a
     * <code>nodesWereInserted</code> event, and then adds itself as a
     * <code>TreeModelListener</code> to <code>newChild</code>.
     * <code>newChild</code> must not be null and must not be an ancestor of
     * this node.
     * 
     * @param newChild
     *            the MutableTreeNode to insert under this node
     * @param childIndex
     *            the index in this node's child array where this node is to be
     *            inserted
     * @exception ArrayIndexOutOfBoundsException
     *                if <code>childIndex</code> is out of bounds
     * @exception IllegalArgumentException
     *                if <code>newChild</code> is null or is an ancestor of
     *                this node
     * @exception IllegalStateException
     *                if this node does not allow children
     * @see #isNodeDescendant
     */

    /**
     * Removes <code>newChild</code> from its present parent (if it has a
     * parent), sets the child's parent to this node, adds the child to this
     * node's child array at index <code>childIndex</code>, fires a
     * <code>nodesWereInserted</code> event, and then adds itself as a
     * <code>TreeModelListener</code> to <code>newChild</code>.
     * <code>newChild</code> must not be null and must not be an ancestor of
     * this node.
     * 
     * @param newChild
     *            the MutableTreeNode to insert under this node
     * @param childIndex
     *            the index in this node's child array where this node is to be
     *            inserted
     * @exception ArrayIndexOutOfBoundsException
     *                if <code>childIndex</code> is out of bounds
     * @exception IllegalArgumentException
     *                if <code>newChild</code> is null or is an ancestor of
     *                this node
     * @exception IllegalStateException
     *                if this node does not allow children
     * @see #isNodeDescendant
     */
    public void insert(final MutableTreeNode newChild, final int childIndex) {
        super.insert(newChild, childIndex);
        int[] newIndexs = new int[1];
        newIndexs[0] = childIndex;
        nodesWereInserted(newIndexs);
        ((AutomatedTreeNode) newChild).addTreeModelListener(this);
    }

    /**
     * Removes the child at the specified index from this node's children and
     * sets that node's parent to null. The child node to remove must be a
     * <code>MutableTreeNode</code>.
     * 
     * @param childIndex
     *            the index in this node's child array of the child to remove
     * @exception ArrayIndexOutOfBoundsException
     *                if <code>childIndex</code> is out of bounds
     */
    public void remove(final int childIndex) {
        Object[] removedArray = new Object[1];
        AutomatedTreeNode node = (AutomatedTreeNode) getChildAt(childIndex);
        node.removeTreeModelListener(this);
        removedArray[0] = node;
        super.remove(childIndex);
        nodesWereRemoved(new int[] { childIndex }, removedArray);
    }

    /**
     * Sets the user object for this node to <code>userObject</code>.
     * 
     * @param userObject
     *            the Object that constitutes this node's user-specified data
     * @see #toString
     */
    public void setUserObject(Object userObject) {
        super.setUserObject(userObject);
        nodeChanged();
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

    /**
     * Invoke this method after the node changed how it is to be represented in
     * the tree.
     */
    protected void nodeChanged() {
        if (listenerList != null) {
            AutomatedTreeNode parent = (AutomatedTreeNode) getParent();
            if (parent != null) {
                int anIndex = parent.getIndex(this);
                if (anIndex != -1) {
                    int[] cIndexs = new int[1];
                    cIndexs[0] = anIndex;
                    //parent.nodesChanged(cIndexs);
                    Object[] cChildren = new Object[1];
                    cChildren[0] = this;
                    fireTreeNodesChanged(parent.getPath(), cIndexs, cChildren);
                }
            } else if (this == getRoot()) {
                fireTreeNodesChanged(getPath(), null, null);
            }
        }
    }

    /**
     * This method invoked after you've inserted some AutomatedTreeNodes into
     * node. childIndices should be the index of the new elements and must be
     * sorted in ascending order.
     */
    protected void nodesWereInserted(int[] childIndices) {
        if (listenerList != null && childIndices != null
                && childIndices.length > 0) {
            int cCount = childIndices.length;
            Object[] newChildren = new Object[cCount];
            for (int counter = 0; counter < cCount; counter++)
                newChildren[counter] = getChildAt(childIndices[counter]);
            fireTreeNodesInserted(childIndices, newChildren);
        }
    }

    /**
     * This method invoked after you've removed some AutomatedTreeNodes from
     * node. childIndices should be the index of the removed elements and must
     * be sorted in ascending order. And removedChildren should be the array of
     * the children objects that were removed.
     */
    protected void nodesWereRemoved(int[] childIndices, Object[] removedChildren) {
        if (childIndices != null) {
            fireTreeNodesRemoved(childIndices, removedChildren);
        }
    }

    /**
     * Invoke this method if you've totally changed the children of node and its
     * childrens children... This will post a treeStructureChanged event.
     */
    protected void nodeStructureChanged() {
        fireTreeStructureChanged(null, null);
    }

    /**
     * Adds a listener for the TreeModelEvent posted after the node changes.
     * 
     * @see #removeTreeModelListener
     * @param l
     *            the listener to add
     */
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    /**
     * Removes a listener previously added with <B>addTreeModelListener() </B>.
     * 
     * @see #addTreeModelListener
     * @param l
     *            the listener to remove
     */
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type by firing a treeNodesChanged() method.
     * 
     * @param childIndices
     *            the indices of the changed elements
     * @param children
     *            the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesChanged(int[] childIndices, Object[] children) {
        fireTreeNodesChanged(getPath(), childIndices, children);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type by firing a treeNodesChanged() method.
     * 
     * @param path
     *            the path to the root node
     * @param childIndices
     *            the indices of the changed elements
     * @param children
     *            the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesChanged(Object[] path, int[] childIndices,
            Object[] children) {
        fireTreeNodesChanged(this, path, childIndices, children);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type by firing a treeNodesChanged() method.
     * 
     * @param source
     *            the node being changed
     * @param path
     *            the path to the root node
     * @param childIndices
     *            the indices of the changed elements
     * @param children
     *            the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesChanged(Object source, Object[] path,
            int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type by firing a treeNodesInserted() method.
     * 
     * @param childIndices
     *            the indices of the changed elements
     * @param children
     *            the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesInserted(int[] childIndices, Object[] children) {
        fireTreeNodesInserted(this, getPath(), childIndices, children);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type by firing a treeNodesInserted() method.
     * 
     * @param source
     *            the node being changed
     * @param path
     *            the path to the root node
     * @param childIndices
     *            the indices of the changed elements
     * @param children
     *            the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesInserted(Object source, Object[] path,
            int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type by firing a treeNodesRemoved() method.
     * 
     * @param childIndices
     *            the indices of the changed elements
     * @param children
     *            the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesRemoved(int[] childIndices, Object[] children) {
        fireTreeNodesRemoved(this, getPath(), childIndices, children);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type by firing a treeNodesRemoved() method.
     * 
     * @param source
     *            the node being changed
     * @param path
     *            the path to the root node
     * @param childIndices
     *            the indices of the changed elements
     * @param children
     *            the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesRemoved(Object source, Object[] path,
            int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type by firing a treeStructureChanged() method.
     * 
     * @param childIndices
     *            the indices of the changed elements
     * @param children
     *            the changed elements
     * @see EventListenerList
     */
    protected void fireTreeStructureChanged(int[] childIndices,
            Object[] children) {
        fireTreeStructureChanged(this, getPath(), childIndices, children);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type by firing a treeStructureChanged() method.
     * 
     * @param source
     *            the node being changed
     * @param path
     *            the path to the root node
     * @param childIndices
     *            the indices of the changed elements
     * @param children
     *            the changed elements
     * @see EventListenerList
     */
    protected void fireTreeStructureChanged(Object source, Object[] path,
            int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, childIndices, children);
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }
}