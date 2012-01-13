/*
 * ExchangeListener.java
 *
 * Created on October 31, 2005, 7:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.valhalla.jbother.jabber.smack;

import org.jivesoftware.smackx.*;
import org.jivesoftware.smack.*;
import com.valhalla.gui.*;
import java.util.*;
import javax.swing.*;
import java.text.*;
import com.valhalla.jbother.*;

/**
 *
 * @author synic
 */
public class ExchangeListener implements RosterExchangeListener {

    protected ResourceBundle resources = ResourceBundle.getBundle("JBotherBundle", Locale.getDefault());   
    private Hashtable choosers = new Hashtable();
    
    public void entriesReceived(final String from, final Iterator entries)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                UserChooser chooser = (UserChooser)choosers.get(from);
                if(chooser != null) 
                {
                    chooser.addEntries(entries);
                    if(!chooser.isVisible()) chooser.setVisible(true);
                    return;
                }
                
                String message = MessageFormat.format(resources.getString("entriesReceived"), new String[] {from});
                NMOptionDialog d = new NMOptionDialog(BuddyList.getInstance().getContainerFrame(),
                        resources.getString("receiveEntries"), message);
                
                d.addButton("Yes", 2);                
                d.addButton("No", 1);

                d.addOptionListener(new OptionListener(d, from, entries));
                d.setVisible(true);
            }
        });
    }
    
    class OptionListener implements NMOptionListener, UserChooserListener
    {
        NMOptionDialog d;
        Iterator entries;
        String from;
        public OptionListener(NMOptionDialog d, String from, Iterator entries)
        {
            this.d = d;
            this.from = from;
            this.entries = entries;
        }
        
        public synchronized void buttonClicked(int num)
        {
            if(num == 1) return;
            d.dispose();
            
            UserChooser chooser = null;
            if(choosers.get(from) == null)
            {
                chooser = new UserChooser(BuddyList.getInstance().getContainerFrame(), 
                        resources.getString("receiveEntries"), resources.getString("selectReceiveItems"), false);
                chooser.clearEntries();
                chooser.addListener(this);
                chooser.setEditChooser(true);
                choosers.put(from, chooser);
            }
            else {
                chooser = (UserChooser)choosers.get(from);
            }

            chooser.addEntries(entries);
            chooser.setVisible(true);
        }
        
        public void usersChosen(UserChooser.Item[] items)
        {
            choosers.remove(from);
            new Thread(new AddUserThread(items)).start();
        }
    }
    
    class AddUserThread implements Runnable
    {
        UserChooser.Item[] items;
        public AddUserThread(UserChooser.Item[] items)
        {
            this.items = items;
        }
        public void run()
        {
            Roster roster = BuddyList.getInstance().getConnection().getRoster();
            for(int i = 0; i < items.length; i++)
            {
                UserChooser.Item item = items[i];
                String name = item.getName();
                String group = item.getGroup();
                String user = item.getJID();
                
                try {
                    roster.createEntry(user, name, new String[] {group});
                }
                catch(XMPPException ex) { }
            }
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    BuddyList.getInstance().getBuddyListTree().reloadBuddies();
                }
            });
        }
    }
}
