/*
 * UserChooser.java
 *
 * Created on October 28, 2005, 7:27 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.valhalla.jbother;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.*;
import com.valhalla.jbother.jabber.*;

/**
 *
 * @author synic
 */
public class UserChooser extends JDialog {
    protected ResourceBundle resources = ResourceBundle.getBundle("JBotherBundle", Locale.getDefault());
    
    JPanel main;
    JButton ok = new JButton(resources.getString("okButton"));
    JButton cancel = new JButton(resources.getString("cancelButton"));
    JButton check = new JButton(resources.getString("checkAll"));
    ArrayList items = new ArrayList();    
    UserModel model = new UserModel();
    JTable table = new JTable(model);
    ArrayList listeners = new ArrayList();
    JScrollPane scroll = new JScrollPane(table);
    
    /** Creates a new instance of UserChooser */
    public UserChooser(JFrame parent, String title, String instructions, boolean all) {
        super(parent, title);
        
        model.setTable();
        
        JLabel label = new JLabel(instructions);
        label.setAlignmentX(Container.CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        main = (JPanel)getContentPane();
        main.setLayout(new BorderLayout());
        main.add(label, BorderLayout.NORTH);
        main.setBorder(BorderFactory.createTitledBorder(title));
        
        updateUsers(all);
        
        scroll.getViewport().setBackground(Color.WHITE);
        main.add(scroll, BorderLayout.CENTER);
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(check);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(cancel);
        buttons.add(ok);
        
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 0,0,0));
        main.add(buttons, BorderLayout.SOUTH);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        } );
        
        ok.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                okHandler();
            }
        } );
        
        check.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                for(int i = 0; i < items.size(); i++)
                {
                    Item item = (Item)items.get(i);
                    item.setState(!item.getState());
                }
                
                table.repaint();
            }
        } );
        
        pack();
        setSize(500, 450);
        setLocationRelativeTo(parent);       

    }
    
    public UserChooser(JFrame parent, String title)
    {
        this(parent, title, "", false);
    }

    public void setEditChooser(boolean edit) 
    {
        model.setEditChooser(edit);
        table = new JTable(model);
        scroll.setViewportView(table);
        model.setTable();
    }    
    
    public void addListener(UserChooserListener l)
    {
        listeners.add(l);
    }
    
    public void clearEntries()
    {
        items.clear();
    }
    public void addEntries(Iterator entries)
    {
        while(entries.hasNext())
        {
            RemoteRosterEntry entry = (RemoteRosterEntry)entries.next();
            Item item = new Item(entry.getName(), entry.getUser(), false);
            
            String group = null;
            Iterator i = entry.getGroupNames();
            while(i.hasNext())
            {
                group = (String)i.next();
                break;
            }
            
            item.setGroup(group);
            items.add(item);            
        }
        
        model.update();
    }
    
    private void okHandler()
    {
        setVisible(false);
        ArrayList list = new ArrayList();
        int count = 0;
        for( int i = 0; i < items.size(); i++)
        {
            Item item = (Item)items.get(i);
            if(item.getState()) 
            {
                list.add(item);
                count++;
            }
        }
        
        Item[] array = (Item[])list.toArray(new Item[count]);
        
        for( int i = 0; i < listeners.size(); i++)
        {
            UserChooserListener listener = (UserChooserListener)listeners.get(i);
            listener.usersChosen(array);
        }
        
        dispose();
    }
    
    private void updateUsers(boolean all)
    {
        Roster roster = BuddyList.getInstance().getConnection().getRoster();
        Iterator i = roster.getEntries();
        
        while(i.hasNext())
        {
            RosterEntry entry = (RosterEntry)i.next();
            BuddyStatus buddy = BuddyList.getInstance().getBuddyStatus(entry.getUser());
            
            if(buddy.size() > 0 || all)
            {
                items.add(new Item(buddy.getName(), buddy.getUser(), false));
            }
        }
        
        Item[] array = (Item[])items.toArray(new Item[items.size()]);
        Arrays.sort(array, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                Item item1 = (Item)o1;
                Item item2 = (Item)o2;
                
                return item1.getName().compareTo(item2.getName());
            }
        });
        
        items = new ArrayList();
        for( int ii = 0; ii < array.length; ii++)
        {
            items.add(array[ii]);
        }
    }
    
    class UserModel extends AbstractTableModel
    {    
        private boolean editChooser = false;

        public void setEditChooser(boolean edit) 
        { 
            editChooser = edit; 
        }
        
        public void update()
        {
            fireTableChanged(new TableModelEvent(this));
        }
        
        public void setTable()
        {
            int width = 400;

            JComboBox comboBox = null;
            if( editChooser ) 
            {
                width = 300;
                
                ArrayList groups = new ArrayList();
                groups.add("None");
                Roster roster = BuddyList.getInstance().getConnection().getRoster();
                Iterator i = roster.getGroups();
                
                while(i.hasNext())
                {
                    RosterGroup group = (RosterGroup)i.next();
                    groups.add(group.getName());
                }
                comboBox = new JComboBox(groups.toArray(new String[groups.size()]));
                comboBox.setEditable(true);
            }           
            
            // set the default column widths
            for (int i = 0; i < getColumnCount(); i++) {
                TableColumn column = table.getColumnModel().getColumn(i);
                if (i == 0) {
                    column.setPreferredWidth(20);
                } else if (i == 1)
                {
                    column.setPreferredWidth(80);
                }
                else if( i == 2 )
                {
                    column.setPreferredWidth(width);
                }
                else if( i == 3 )
                {
                    column.setPreferredWidth(100);
                    column.setCellEditor(new DefaultCellEditor(comboBox));
                }    
                
            }
        }
        
        public Class getColumnClass( int index )
        {
            if(index == 0) return Boolean.class;
            else return String.class;
        }
        
        public String getColumnName( int index )
        {
            switch(index)
            {
                case 0:
                    return " ";

                case 1: 
                    return "Nick";

                case 2:
                    return "JID";

                case 3:
                    return "Group";

                default:
                    return " ";                            
            }
        }
        
        public int getColumnCount() 
        {  
            if(editChooser) return 4;
            return 3; 
        }
        public int getRowCount() { return items.size(); }
        public Object getValueAt(int row, int column)
        {
            Item item = (Item)items.get(row);
            
            if(column == 0) return new Boolean(item.getState());
            else if(column == 1) return item.getName();
            else if(column == 2) return item.getJID();
            else return item.getGroup();
        }
        
        public boolean isCellEditable(int row, int column) {
            
            if(editChooser && ( column == 1 || column == 3 )) return true;
            
            if (column == 0)
                return true;
            else
                return false;
        }
        
        public void setValueAt(Object value, int row, int col) {

            
            Item item = (Item)items.get(row);
            
            if(col == 0)
            {
                boolean b = ((Boolean) value).booleanValue();
                item.setState(b);
            }
            else if(col == 1)
            {
                item.setName((String)value);
            }
            
            else if( col == 3 )
            {
                String group = (String)value;
                item.setGroup(group);
            }

            fireTableCellUpdated(row, col);
        }

    }
    
    public static class Item
    {
        String name;
        String jid;
        boolean state;
        String group = "None";
        public Item(String name, String jid, boolean state)
        {
            this.name = name;
            this.jid = jid;
            this.state = state;
        }
        
        public boolean getState() { return state; }
        public String getName() { return name; }
        public String getJID() { return jid; }
        public String getGroup() { return group; }
        
        public void setState(boolean s) { state = s; }
        public void setName(String name) { this.name = name; }
        public void setGroup(String group) 
        { 
            if(group == null) group = "None";
            this.group = group; 
        }
    }
}
