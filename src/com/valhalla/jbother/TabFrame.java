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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.border.*;
import javax.swing.*;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.*;

import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.Standard;
import com.valhalla.jbother.groupchat.ChatRoomPanel;
import com.valhalla.jbother.groupchat.GroupChatBookmarks;
import com.valhalla.jbother.plugins.events.ExitingEvent;
import com.valhalla.jbother.jabber.BuddyStatus;
import com.valhalla.pluginmanager.PluginChain;
import com.valhalla.settings.Settings;
import net.infonode.tabbedpanel.*;
import net.infonode.tabbedpanel.titledtab.*;
import net.infonode.util.*;
import net.infonode.tabbedpanel.theme.*;
import net.infonode.gui.colorprovider.*;
import net.infonode.gui.hover.*;

/**
 * Contains all of the groupchat windows in tabs
 *
 * @author Adam Olsen
 * @author Andrey Zakirov
 * @version 1.1
 */
public class TabFrame extends JFrame {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    private JPanel container = new JPanel(new BorderLayout());

    private TabbedPanel tabPane = new TabbedPanel();

    private TabListener tabListener = null;

    private JMenuBar menuBar = new JMenuBar();

    private JMenu optionMenu = new JMenu(resources.getString("options"));

    private JMenuItem newItem = new JMenuItem(resources.getString("joinRoom")),
            leaveItem = new JMenuItem(resources.getString("leaveAll")),
            closeItem = new JMenuItem(resources.getString("closeButton"));

    private Hashtable queueCounts = new Hashtable();

    private GCTabHandler switchListener = new GCTabHandler();

    private WindowAdapter windowListener = null;

    private TabFocusListener tabFocusListener = null;

    private CloseMenu close = new CloseMenu();

    private JSplitPane pane;

    private static boolean tabListenerAdded = false;

    private MyFocusListener focusListener = new MyFocusListener();
    private javax.swing.Timer focusTimer = new javax.swing.Timer( 50, focusListener );
    //private ShapedGradientTheme theme = new ShapedGradientTheme();
    //private SmallFlatTheme theme = new SmallFlatTheme();
    private ShapedGradientTheme theme = new ShapedGradientTheme(0f, 0f, new FixedColorProvider(new Color(150, 150, 150)),null);

    /**
     * Constructor sets the frame up and adds a listener to the JTabPane so that
     * if a tab is changed the title of this frame reflects the topic and the
     * name of the room in the tab
     */
    public TabFrame() {
        super("JBother");

        setIconImage(Standard.getImage("frameicon.png"));

        setContentPane(container);
        container.add(tabPane, BorderLayout.CENTER);

        // add the tab switch listener so the title of the frame reflects the
        // current tab
        tabListener = new TabAdapter() {
            public void tabSelected(TabStateChangedEvent e) {
                if( tabPane.getSelectedTab() == null) return;
                if (tabPane.getSelectedTab().getContentComponent() != null) {
                    final TabFramePanel panel = (TabFramePanel) tabPane.getSelectedTab().getContentComponent();
                    setTitle(panel.getWindowTitle());
                    focusComponent( panel.getInputComponent() );
                    clearTab(panel);
                }
            }
        };

        tabFocusListener = new TabFocusListener();

        optionMenu.add(newItem);
        optionMenu.add(leaveItem);

        addListeners();

        menuBar.add(optionMenu);
        windowListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveStates();
                closeHandler();
            }
        };

        // if they press the close button, we wanna handle leaving of the rooms
        addWindowListener(windowListener);

        setPreferredLocation();
        pack();

        String stringWidth = Settings.getInstance().getProperty(
                "chatFrameWidth");
        String stringHeight = Settings.getInstance().getProperty(
                "chatFrameHeight");

        if (stringWidth == null)
            stringWidth = "635";
        if (stringHeight == null)
            stringHeight = "450";

        setSize(new Dimension(Integer.parseInt(stringWidth), Integer
                .parseInt(stringHeight)));
        DialogTracker.addDialog(this, true, false);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                saveStates();
            }
        });

        com.valhalla.Logger.debug("TabFrame is being created");

        addComponentListener(new MoveListener());
        addTabListeners();
    }

    /**
     * When the focus is gained on a tab, this class focuses the input
     * component for this tab
     */
    class TabFocusListener implements FocusListener {
        public void focusLost(FocusEvent e) {
        }

        public void focusGained(FocusEvent e) {
            final TabFramePanel panel = (TabFramePanel) tabPane.getSelectedTab().getContentComponent();
            if (panel != null)
            {
                focusComponent( panel.getInputComponent() );
            }
            if (panel instanceof ConversationPanel)
            {
                (((ConversationPanel) panel).getBuddy()).sendNotDisplayedID();
            }
        }
    }

    /**
     * Focuses the input component on a tab
     */
    private void focusComponent( Component comp )
    {
        focusListener.setComponent( comp );
        if( !focusTimer.isRunning() ) focusTimer.start();
        else focusTimer.restart();
    }

    /**
     * Waits a few seconds to focus the new component when a tab is selected
     */
    class MyFocusListener implements ActionListener
    {
        private Component comp = null;
        public void setComponent( Component comp ) { this.comp = comp; }
        public void actionPerformed( ActionEvent e )
        {
            SwingUtilities.invokeLater( new Runnable()
            {
                public void run() { if( comp != null ) comp.requestFocus(); comp = null; }
            } );

            //comp = null;

            focusTimer.stop();
        }
    }

    /**
     * Adds event listeners to this tab frame
     */
    private void addTabListeners() {
        tabPane.addTabListener(tabListener);
        tabPane.addFocusListener(tabFocusListener);
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
               .addKeyEventPostProcessor(switchListener);


        Direction d = Direction.DOWN;
        String dSetting = Settings.getInstance().getProperty( "tabOrientation", "Down" );
        if( dSetting.equals( "Up" ) ) d = Direction.UP;
        else if( dSetting.equals( "Right" ) ) d = Direction.RIGHT;
        else if( dSetting.equals( "Left" ) ) d = Direction.LEFT;

        tabPane.getProperties().setTabAreaOrientation(d);
        tabPane.getProperties().setAutoSelectTab(true);
        tabPane.getProperties().setTabReorderEnabled(true);
        tabPane.getProperties().setTabLayoutPolicy(TabLayoutPolicy.COMPRESSION);
        tabPane.getProperties().setTabDropDownListVisiblePolicy(TabDropDownListVisiblePolicy.MORE_THAN_ONE_TAB);
        tabPane.getProperties().addSuperObject(theme.getTabbedPanelProperties());
    }

    /**
     * Removes all event listeners from the TabbedPanel
     */
    public void removeTabListeners() {
        tabPane.removeTabListener(tabListener);
        tabPane.removeFocusListener(tabFocusListener);

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removeKeyEventPostProcessor(switchListener);
    }

    /**
     * Docks the BuddyList to this frame
     * @param list The buddy list to dock
     */
    public void dockBuddyList(final BuddyList list) {
        container.remove(tabPane);
        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setResizeWeight(.7);

        String dockWhere = Settings.getInstance().getProperty("dockOption", "Left");
        if( dockWhere.equals( "Left" ) )
        {
            pane.add(list);
            pane.add(tabPane);
        }
        else {
            pane.add(tabPane);
            pane.add(list);
        }

        container.add(pane, BorderLayout.CENTER);
        removeWindowListener(windowListener);
        windowListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveStates();

                ExitingEvent event = new ExitingEvent(list);
                PluginChain.fireEvent(event);
                if (event.getExit()) {
                    closeHandler();
                    list.quitHandler();
                }
            }
        };

        addWindowListener(windowListener);

        String divLocString = Settings.getInstance().getProperty(
                "dockedBuddyListDivLocation");
        int divLoc = 150;

        try {
            if (divLocString != null) {
                divLoc = Integer.parseInt(divLocString);
            }

        } catch (NumberFormatException ex) {
        }

        addComponentListener(new MoveListener());

        pane.setDividerLocation(divLoc);
        pane.addPropertyChangeListener("lastDividerLocation",
                new DividerListener());

        validate();
    }

    /**
     * Listens for movement in the tab frame, and saves the new position
     */
    class MoveListener extends ComponentAdapter {
        public void componentMoved(ComponentEvent e) {
            saveStates();
        }
    }

    /**
     * Undocks the BuddyList from this tab frame
     */
    public void undock() {
        container.remove(tabPane);
        removeTabListeners();
        TabFrame frame = new TabFrame();
        frame.setTabPane(tabPane);
        BuddyList.getInstance().setTabFrame(frame);
        frame.setVisible(true);
        dispose();
        if (tabPane.getTabCount() > 0 && tabPane.getTabAt(0) != null)
            tabPane.setSelectedTab(tabPane.getTabAt(0));
    }

    /**
     * Sets the tab pane to be used for this frame
     * This is done because unless a new tab pane is created when docking
     * windows, weird things happen
     * @param pane the new TabbedPanel
     */
    private void setTabPane(TabbedPanel pane) {
        try {
            remove(this.tabPane);
        } catch (Exception e) {
        }
        this.tabPane = pane;
        com.valhalla.Logger.debug("Setting tab pane");

        addTabListeners();
        getContentPane().add(pane, BorderLayout.CENTER);
        validate();
    }

    /**
     * Listens for the user to move the divider, and saves it's location
     *
     * @author Adam Olsen
     * @version 1.0
     */
    private class DividerListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            Settings.getInstance().setProperty("dockedBuddyListDivLocation",
                    e.getOldValue().toString());
        }
    }

    /**
     * Marks a tab for a TabFramePanel if it's not already selected
     *
     * @param panel
     *            the panel to mark
     */
    public void markTab(TabFramePanel panel, boolean messageToMe) {
        if( tabPane.getSelectedTab() == null ) return;
        if (tabPane.getSelectedTab().getContentComponent() != panel) {
            Integer i = (Integer) queueCounts.get(panel);
            if (i == null)
                i = new Integer(1);

            int index = tabPane.getTabIndex(panel.getTab());
            if (index == -1) return;

            Icon icon = Standard.getIcon("images/newmessage.png");
            if( messageToMe ) icon = Standard.getIcon("images/newhighlight.png");

            TitledTab tab = panel.getTab();
            String tip = panel.getPanelToolTip();
            tip = htmlPad("<b>" + tip + "</b><br>" +  i + " new messages");
            tab.setToolTipText(tip);
            tab.setIcon(icon);

            queueCounts.put(panel, new Integer(i.intValue() + 1));
        }
    }

    /**
     * Returns an html enclosed and 3 pixel padding string for tab tooltips
     * @return html for tab tooltips
     */
    private String htmlPad(String html)
    {
        return "<html><div style='padding: 3px;'>" + html + "</div></html>";
    }

    /**
     * Clears the tab message queue count
     */
    public void clearTab(TabFramePanel panel) {

        String name = panel.getPanelName();
        TitledTab tab = panel.getTab();
        if( tab == null ) return;

        tab.setToolTipText(htmlPad(panel.getPanelToolTip()));

        tab.setIcon(Standard.getIcon("images/nomessage.png"));
        queueCounts.remove(panel);
        if (panel instanceof ChatRoomPanel) {
            ((ChatRoomPanel) panel).resetMessageToMe();
        }
    }

    /**
     * Saves the size of the chat frame
     */
    public void saveStates() {
        if (isVisible()) {
            Point location = new Point(getLocationOnScreen());
            Settings.getInstance().setProperty("tabFrameX",
                    new Double(location.getX()).toString());
            Settings.getInstance().setProperty("tabFrameY",
                    new Double(location.getY()).toString());
        } else {
            com.valhalla.Logger.debug("TabFrame is not visible");
        }

        Dimension size = getSize();
        Integer width = new Integer((int) size.getWidth());
        Integer height = new Integer((int) size.getHeight());
        Settings.getInstance().setProperty("chatFrameWidth", width.toString());
        Settings.getInstance()
                .setProperty("chatFrameHeight", height.toString());
    }

    /**
     * Switches the current tab in the tab frame
     */
    public void switchTab(TabbedPanel tabPane) {
        com.valhalla.Logger.debug("Switching the tab");
        int current = tabPane.getTabIndex(tabPane.getSelectedTab());
        current++;
        if (current >= tabPane.getTabCount())
            current = 0;
        tabPane.setSelectedTab(tabPane.getTabAt(current));
        TabFramePanel panel = (TabFramePanel) tabPane.getTabAt(current).getContentComponent();
        if (panel != null)
            focusComponent( panel.getInputComponent() );
    }

    /**
     * Loads the saved settings from any previous settings
     */
    private void setPreferredLocation() {
        //load the settings from the settings file
        String xString = Settings.getInstance().getProperty("tabFrameX");
        String yString = Settings.getInstance().getProperty("tabFrameY");

        if (yString == null)
            yString = "100";
        if (xString == null)
            xString = "100";

        double x = 100;
        double y = 100;

        try {
            x = Double.parseDouble(xString);
            y = Double.parseDouble(yString);
        } catch (NumberFormatException e) {
            com.valhalla.Logger.logException(e);
        }

        if (x < -50.0)
            x = 100.0;
        if (y < -50.0)
            y = 100.0;

        setLocation((int) x, (int) y);
    }

    /**
     * Adds the various event listeners
     *
     * @author Adam Olsen
     * @version 1.0
     */
    private void addListeners() {
        MenuItemListener listener = new MenuItemListener();
        newItem.addActionListener(listener);
        leaveItem.addActionListener(listener);
    }

    public void addFrameListener (BuddyStatus buddy)
    {
        final BuddyStatus buddy2=buddy;
        addWindowFocusListener ( new WindowFocusListener() {
            public void windowGainedFocus (WindowEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        buddy2.sendNotDisplayedID();
                    }
                });
            }
            public void windowLostFocus(WindowEvent e) {
            }
        });

    }

    /**
     * Listens for a menu item to be clicked
     *
     * @author Adam Olsen
     * @version 1.0
     */
    private class MenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == newItem)
                new GroupChatBookmarks(TabFrame.this).setVisible(true);
            if (e.getSource() == leaveItem)
                closeHandler();
        }
    }

    /**
     * Updates the font in all the chat conversationareas
     *
     * @param font
     *            the font to update to
     */
    public void updateStyles(Font font) {
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            TabFramePanel panel = (TabFramePanel) tabPane.getTabAt(i).getContentComponent();
            panel.updateStyle(font);
        }
    }

    /**
     * Set the status in all the rooms
     *
     * @param mode
     *            the presence mode
     * @param status
     *            the status string
     */
    public void setStatus(Presence.Mode mode, String status) {
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            TabFramePanel panel = (TabFramePanel) tabPane.getTabAt(i).getContentComponent();
            if (panel instanceof ChatRoomPanel) {
                ChatRoomPanel window = (ChatRoomPanel) panel;
                MultiUserChat chat = window.getChat();
                if( chat == null || !chat.isJoined() ) continue;

                //set up a packet to be sent to my user in every groupchat
                Presence presence = new Presence(Presence.Type.AVAILABLE,
                        status, 0, mode);
                presence.setTo(window.getRoomName() + '/'
                        + window.getNickname());

                if (!BuddyList.getInstance().checkConnection()) {
                    BuddyList.getInstance().connectionError();
                    return;
                }

                BuddyList.getInstance().getConnection().sendPacket(presence);
            }
        }
    }

    /**
     * This not only closes the window, but it leaves all the rooms like it
     * should
     */
    public void closeHandler() {
        removeTabListeners();
        leaveAll();
    }

    /**
     * Since there is no way to check to see if a message is from someone in a
     * chat room, we check to see if the message is coming from the same server
     * as a chatroom we are in.
     *
     * @param server
     *            the server to check
     */
    public boolean isRoomOpen(String server) {
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            TabFramePanel panel = (TabFramePanel) tabPane.getTabAt(i).getContentComponent();
            if (panel instanceof ChatRoomPanel) {
                ChatRoomPanel window = (ChatRoomPanel) panel;
                if (server.toLowerCase().equals(
                        window.getRoomName().toLowerCase()))
                    return true;
            }
        }

        return false;
    }

    /**
     * If there is a chatroom open in this frame with a server name, this
     * returns the ChatRoomPanel that contains it
     *
     * @param server
     *            the name of the room to get the ChatRoomPanel for
     * @return the ChatRoomPanel requested, or <tt>null</tt> if it could not
     *         be found
     */
    public ChatRoomPanel getChatPanel(String server) {
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            TabFramePanel panel = (TabFramePanel) tabPane.getTabAt(i).getContentComponent();
            if (panel instanceof ChatRoomPanel) {
                ChatRoomPanel window = (ChatRoomPanel) panel;
                if (server.toLowerCase().equals(
                        window.getRoomName().toLowerCase()))
                    return window;
            }
        }

        return null;
    }

    /**
     * This leaves a chatroom and removes the associated ChatRoomPanel from the
     * TabPane
     *
     * @param window
     *            the room to leave
     */
    public void removePanel(TabFramePanel panel) {
        queueCounts.remove(panel);
        TitledTab tab = panel.getTab();
        tab.setHighlightedStateTitleComponent(null);
        tab.setNormalStateTitleComponent(null);
        tab.setDisabledStateTitleComponent(null);
        tab.getProperties().setHoverListener(null);

        tabPane.removeTab(tab);
        tabPane.validate();
        if(panel instanceof ConversationPanel) MessageDelegator.getInstance().removePanel((ConversationPanel)panel);

        if (panel instanceof ChatRoomPanel) {
            ((ChatRoomPanel) panel).leave();
	    ((ChatRoomPanel) panel).removed();
            panel = null;
        }

        try {
            panel = (TabFramePanel) tabPane.getSelectedTab().getContentComponent();
        }
        catch( NullPointerException npe ) { panel = null; }

        if (panel != null) {
            setTitle(panel.getWindowTitle());
            TitledTab t = panel.getTab();
            t.setText(panel.getPanelName());
         } else {
            setTitle("JBother");
        }

        BuddyList.getInstance().stopTabFrame();
    }

    /**
     * Sets the subject of a ChatRoomPanel based on a message that was received
     * from the GroupChat server with &lt;subject&gt; in it
     *
     * @param window
     *            the window to set the subject for
     */
    public void setSubject(ChatRoomPanel window) {
        if( tabPane.getSelectedTab() == null) return;
        if (!(tabPane.getSelectedTab().getContentComponent() instanceof ChatRoomPanel))
            return;
        if ((ChatRoomPanel) tabPane.getSelectedTab().getContentComponent() == window) {
            setTitle(resources.getString("groupChat") + ": "
                    + window.getRoomName());
            validate();
        }
    }

    /**
     * @param panel
     *            the panel to check
     * @return true if the tab panel is currently displayed in the tab frame
     */
    public boolean contains(TabFramePanel panel) {
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            TabFramePanel p = (TabFramePanel) tabPane.getTabAt(i).getContentComponent();
            if (p == panel)
                return true;
        }

        return false;
    }

    /**
     * Returns the number of rooms currently open in the frame
     *
     * @return the number of rooms still open
     */
    public int tabsLeft() {
        return tabPane.getTabCount();
    }

    /**
     * Adds a chat room to the frame
     *
     * @param window
     *            the room to add
     */
    public void addPanel(final TabFramePanel panel) {
        panel.setListenersAdded(true);

        String name = panel.getPanelName();
        String orient = Settings.getInstance().getProperty("tabOrientation", "Down");

        final TitledTab tab = new TitledTab( name, Standard.getIcon("images/nomessage.png"), 
            (JComponent) panel, null );
        tab.setToolTipText(htmlPad(panel.getPanelToolTip()));
        
        tab.getProperties().addSuperObject(theme.getTitledTabProperties());

        CloseButton b = new CloseButton(tab);
        final CloseButton temp = b;
        tab.setHighlightedStateTitleComponent(b);

        if( !Settings.getInstance().getBoolean("closeButtonOnAll")) b = null;
        tab.setNormalStateTitleComponent(b);
        tab.setDisabledStateTitleComponent(b);
        tab.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
               if(e.isPopupTrigger())
                {
                    close.setTab(tab);
                    close.show(tab, e.getX(), e.getY());
                }

            }

        } );

        tab.getProperties().setHoverListener( new HoverListener()
            {
                public void mouseEntered(HoverEvent e)
                {
                    if(Settings.getInstance().getBoolean("closeButtonOnAll")) return;
                    tab.setNormalStateTitleComponent(temp);
                    tab.setDisabledStateTitleComponent(temp);
                    tab.validate();
                }

                public void mouseExited(HoverEvent e)
                {
                    if(Settings.getInstance().getBoolean("closeButtonOnAll")) return;
                    tab.setNormalStateTitleComponent(null);
                    tab.setDisabledStateTitleComponent(null);
                    tab.validate();
                }
            } );



        tabPane.addTab( tab );
        panel.setTab( tab );
        if (panel instanceof ChatRoomPanel)
            tabPane.setSelectedTab(tab);
    }

    /**
     * Revalidates the close button on a tab
     * @param selected whether or not this tab is selected
     */
    public void resetCloseButtons(boolean selected)
    {
        for( int i = 0; i < tabPane.getTabCount(); i++ )
        {
            TitledTab tab = (TitledTab)tabPane.getTabAt(i);
            CloseButton button = new CloseButton(tab);
            tab.setHighlightedStateTitleComponent(button);
            if( !selected) button = null;

            tab.setNormalStateTitleComponent(button);
            tab.setDisabledStateTitleComponent(button);
            tab.validate();
        }
    }


    /**
     * Switches the tab based on CTRL+n keys
     */
    class GCTabHandler implements KeyEventPostProcessor {
        boolean first = false;

        public boolean postProcessKeyEvent(KeyEvent e) {
            Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .getFocusedWindow();

            if (!(w instanceof TabFrame))
                return false;
            TabbedPanel tabPane = ((TabFrame) w).getTabPane();

            // get the ASCII character code for the character typed
            int numPressed = (int) e.getKeyChar();

            int mask = KeyEvent.CTRL_MASK;
            if (System.getProperty("mrj.version") != null) {
                mask = KeyEvent.META_DOWN_MASK;
            }

            // the integer characters start at ASCII table number 49, so we
            // subtract 49
            numPressed -= 49;

            // if the new ASCII value is between 0 and 8, then the
            // key pressed was 1 through 9 - which is what we want
            // also check that the CTRL key was being held down
            if ((numPressed >= 0 && numPressed <= 8)
                    && (e.getModifiers() & mask) == Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()) {
                e.consume();

                if (tabPane.getTabCount() >= numPressed)
                    tabPane.setSelectedTab(tabPane.getTabAt(numPressed));
            } else if (e.getKeyCode() == KeyEvent.VK_TAB
                    && (e.getModifiers() & mask) == Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()) {
                if (first == false) {
                    first = true;
                } else {
                    switchTab(tabPane);
                    first = false;

                    final TabFramePanel panel = (TabFramePanel) tabPane.getSelectedTab().getContentComponent();
                    focusComponent( panel.getInputComponent() );

                }
            }

            return true;
        }
    }

    /**
     * Leaves all chatrooms (for if they close the window)
     */
    public void leaveAll() {
        com.valhalla.Logger.debug("There are " + tabPane.getTabCount()
                + " rooms");

        int tabCount = tabPane.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            TabFramePanel panel = (TabFramePanel) tabPane.getTabAt(0).getContentComponent();

            if (panel instanceof ChatRoomPanel) {
                ChatRoomPanel window = (ChatRoomPanel) panel;
                //if this frame is closed as a result of connection loss and we
                // try to leave
                //the channel, it will not work, so we need to catch it.
		window.removed();
                try {
                    window.leave();
		    
                } catch (IllegalStateException e) {
                    com.valhalla.Logger
                            .debug("Caught Illegal State Exception when leaving window: "
                                    + window.toString());
                }

                BuddyList.getInstance().removeTabPanel(panel);
            } else {
                ((ConversationPanel) panel).checkCloseHandler();
            }
        }

        BuddyList.getInstance().stopTabFrame();
    }

    /**
     * @return Returns the tabPane.
     */
    public TabbedPanel getTabPane() {
        return tabPane;
    }

    class CloseMenu extends JPopupMenu
    {
        JMenuItem closeItem = new JMenuItem(resources.getString("closeButton"));
        JMenuItem closeAll = new JMenuItem(resources.getString("closeAllButton"));
        private TitledTab tab;
        public CloseMenu()
        {
            add(closeItem);
            add(closeAll);

            closeItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if( tab == null ) return;
                    TabFramePanel panel = (TabFramePanel)tab.getContentComponent();

                    if (panel instanceof ConversationPanel) {
                        ((ConversationPanel) panel).checkCloseHandler();
                    } else {
                        removePanel(panel);
                    }
                }

            } );

            closeAll.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    int count = tabPane.getTabCount();
                    for( int i = 0; i < count; i++ )
                    {
                        TitledTab tab = (TitledTab)tabPane.getTabAt(0);
                        TabFramePanel panel = (TabFramePanel)tab.getContentComponent();

                        if (panel instanceof ConversationPanel) {
                            ((ConversationPanel) panel).checkCloseHandler();
                        } else {
                            removePanel(panel);
                        }
                    }
                }

            } );
        }

        public void setTab(TitledTab tab)
        {
            this.tab = tab;
        }
    }


    class CloseButton extends JLabel
    {
        TitledTab tab;
        public CloseButton( final TitledTab tab )
        {
            super(Standard.getIcon("images/buttons/close.png"));
            setPreferredSize( new Dimension( 15, 8 ) );
            this.tab = tab;
            setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
            setToolTipText( resources.getString( "closeButton" ) );

            addMouseListener( new MouseAdapter()
            {
                public void mouseEntered( MouseEvent e )
                {
                    setBorder(BorderFactory.createEtchedBorder());
                    validate();
                }

                public void mouseExited( MouseEvent e )
                {
                    setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                    validate();
                }

                public void mouseClicked( MouseEvent e )
                {
                    TabFramePanel panel = (TabFramePanel)tab.getContentComponent();

                    if (panel instanceof ConversationPanel) {
                        ((ConversationPanel) panel).checkCloseHandler();
                    } else {
                        removePanel(panel);
                    }

                }

            } );
        }

    }
}
