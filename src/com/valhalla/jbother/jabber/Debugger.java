/*Copyright (C) 2003 Adam Olsen
 *This program is free software; you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation; either version 1, or (at your option)
 *any later version.
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * Debugger.java
 *
 * Created on September 13, 2005, 7:02 PM
 */

package com.valhalla.jbother.jabber;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.debugger.*;
import org.jivesoftware.smack.util.*;
import org.jivesoftware.smack.packet.*;
import java.io.*;
import com.valhalla.jbother.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.util.regex.*;

/**
 *
 * @author synic
 */
public class Debugger implements SmackDebugger {
    Writer writer;
    Reader reader;
    XMPPConnection connection;
    ReaderListener readerListener;
    WriterListener writerListener;
    StringBuffer read = new StringBuffer();
    StringBuffer write = new StringBuffer();
    Pattern p = Pattern.compile("^\\s*<([a-zA-Z0-9]+)\\s?[^>]*>");
    
    private PacketListener packetReaderListener = new PacketListener()
    {
        public void processPacket(Packet packet) { }
    };
    private PacketListener packetWriterListener = new PacketListener()
    {
        public void processPacket(Packet packet) { }
    };
    
    /** Creates a new instance of Debugger */
    public Debugger(XMPPConnection connection, Writer writer, Reader reader) {
        this.connection = connection;
        this.reader = reader;
        this.writer = writer;
        
        
        // Create a special Reader that wraps the main Reader and logs data to the GUI.
        ObservableReader debugReader = new ObservableReader(reader);
        readerListener = new ReaderListener() {
            public void read(String str) {
                addReadPacket(str);
            }
        };
                
                
        debugReader.addReaderListener(readerListener);

        // Create a special Writer that wraps the main Writer and logs data to the GUI.
        ObservableWriter debugWriter = new ObservableWriter(writer);
        writerListener = new WriterListener() {
            public void write(String str) {
                addWritePacket(str);
            }
        };

        debugWriter.addWriterListener(writerListener);

        // Assign the reader/writer objects to use the debug versions. The packet reader
        // and writer will use the debug versions when they are created.
        this.reader = debugReader;
        this.writer = debugWriter;                
    }
    
    private boolean checkForm(String str)
    {
        Matcher m = p.matcher(str);
        if(m.find())
        {
            String tag = m.group(1);
            if(str.trim().endsWith("</"+tag+">")) return true;
        }
        
        return false;
    }
    
    /**
     * Description of the Method
     *
     * @param str
     *            Description of the Parameter
     * @return Description of the Return Value
     */
    private String formatXML(String str) {
        try {
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            // Surround this setting in a try/catch for compatibility with Java
            // 1.4. This setting is required for Java 1.5
            try {
                tFactory.setAttribute("indent-number", new Integer(8));
            } catch (IllegalArgumentException e) {
            }
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xalan}indent-amount", "8");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "8");

            // Transform the requested string into a nice formatted XML string
            StreamSource source = new StreamSource(new StringReader(str));
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);
            return sw.toString().replaceAll("\\s*$", "");
        } catch (TransformerConfigurationException tce) {

            // Use the contained exception, if any
            Throwable x = tce;
            if (tce.getException() != null) {
                x = tce.getException();
            }

        } catch (TransformerException te) {
            // Use the contained exception, if any
            Throwable x = te;
            if (te.getException() != null) {
                x = te.getException();
            }

        }
        return str.replaceAll("\\s*$", "");
    }    
    
    private void addReadPacket( String string )
    {
        if( ConsolePanel.getInstance() != null )
        {
            if(string.trim().equals("")) return;
            if(checkForm(string)) string = formatXML(string);
            ConsolePanel.getInstance().append(string, false);
        }
    }
    
    private void addWritePacket( String string )
    {
        if( ConsolePanel.getInstance() != null )
        {
            if(string.trim().equals("")) return;
            if(checkForm(string)) string = formatXML(string);
            ConsolePanel.getInstance().append(string, true);
        }
    }    
    
    public PacketListener getReaderListener() { return packetReaderListener; }
    public PacketListener getWriterListener() { return packetWriterListener; }    
    
    public Writer getWriter() { return writer; }
    public Reader getReader() { return reader; }
    
    public void userHasLogged(String user) { }
    
    public Reader newConnectionReader(Reader newReader) {
        ((ObservableReader)reader).removeReaderListener(readerListener);
        ObservableReader debugReader = new ObservableReader(newReader);
        debugReader.addReaderListener(readerListener);
        reader = debugReader;
        return reader;
    }

    public Writer newConnectionWriter(Writer newWriter) {
        ((ObservableWriter)writer).removeWriterListener(writerListener);
        ObservableWriter debugWriter = new ObservableWriter(newWriter);
        debugWriter.addWriterListener(writerListener);
        writer = debugWriter;
        return writer;
    }
    
}