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
package com.valhalla.jbother.jabber.smack;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.packet.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * class implementing packets used to search Jabber User Directory
 *
 * searching process, according to JEP-0055 looks as following:
 *   1) query the information repository regarding the possible search fields
 *   2) send search query
 *   3) receive search results
 *
 * Because search fields are not standard, they are kept in hash map: keys are names of the
 * fields and values are, well, values of those fields :-)
 *
 * TODO: search using data forms
 *
 * @author Lukasz Wiechec
 * @version 1.0
 * @created Apr 1, 2005 12:53:47 PM
 */

public class Search extends IQ {
    private static String fId = "$Id$";
    
    public final String NAMESPACE = "jabber:iq:search";
    
    String instructions = null;
    
    HashMap searchFields = null;
    ArrayList items = null;
    
    DataForm searchDataForm = null;
    
    public Search(String aInstructions, HashMap aSearchFields) {
        instructions = aInstructions;
        searchFields = aSearchFields;
    }
    
    public Search() {
    }
    
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public HashMap getSearchFields() {
        return searchFields;
    }
    
    public void setSearchFields(HashMap searchFields) {
        this.searchFields = searchFields;
    }
    
    public ArrayList getItems() {
        return items;
    }
    
    public void setItems(ArrayList items) {
        this.items = items;
    }
    
    public DataForm getSearchDataForm() {
        return searchDataForm;
    }
    
    public void setSearchDataForm(DataForm searchDataForm) {
        this.searchDataForm = searchDataForm;
    }
    
    public void addItem(Item aItem) {
        if(items == null) {
            items = new ArrayList();
        }
        items.add(aItem);
    }
    
    public String getChildElementXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<query xmlns=\"" + NAMESPACE + "\">");
        if(instructions != null) {
            buf.append("<instructions>" + instructions + "</instructions>");
        }
        if(searchFields != null) {
            Iterator iKeys = searchFields.keySet().iterator();
            while(iKeys.hasNext()) {
                String field = (String)iKeys.next();
                buf.append( "<" + field + ">" );
                buf.append( searchFields.get(field) );
                buf.append( "</" + field + ">" );
            }
        }
        if(items != null) {
            Iterator iItems = items.iterator();
            while(iItems.hasNext()) {
                buf.append(((Search.Item)iItems.next()).toXML());
            }
        }
        if(searchDataForm != null) {
            buf.append(searchDataForm.toXML());
        }
        buf.append("</query>");
        return buf.toString();
    }
    
    /**
     * inner class for representing "items", ie. the elements that are returned
     * from querying the JUD
     */
    public static class Item {
        private String jid;
        HashMap attributes;
        
        public Item(String jid) {
            this.jid = jid;
        }
        
        public String getJid() {
            return jid;
        }
        
        public void setJid(String jid) {
            this.jid = jid;
        }
        
        public HashMap getAttributes() {
            return attributes;
        }
        
        public void setAttributes(HashMap attributes) {
            this.attributes = attributes;
        }
        
        public String toXML() {
            StringBuffer out = new StringBuffer();
            out.append( "<item jid=\"" + jid + "\">" );
            Iterator iAttrs = attributes.keySet().iterator();
            while(iAttrs.hasNext()) {
                String attr = (String)iAttrs.next();
                out.append( "<" + attr + ">" + attributes.get(attr) + "</" + attr + ">" );
            }
            out.append( "</item>" );
            return out.toString();
        }
        
    }
}
