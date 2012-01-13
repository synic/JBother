/*
 * ConversationArea.java
 *
 * Created on September 9, 2005, 6:26 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.valhalla.jbother;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.valhalla.settings.*;
import com.valhalla.gui.*;

/**
 *
 * @author synic
 */
public class ConversationArea extends JScrollPane {
    private ResourceBundle resources = ResourceBundle.getBundle(
            "JBotherBundle", Locale.getDefault());

    JTextPane pane = new JTextPane();

    public static Color SENDER = new Color(128, 0, 0);
    public static Color RECEIVER = new Color(22, 86, 158);
    public static Color SERVER = new Color(0, 128, 0);
    public static Color BLACK = Color.BLACK;
    public static Color HL = new Color(248,192,192);
    public static Color GRAY = Color.GRAY;
    Document doc = pane.getDocument();
    File file;
    PrintWriter out = null;
    OutputStreamWriter fw = null;
    LinkedList textBuffer = new LinkedList();
    LinkedList sasBuffer = new LinkedList();
    javax.swing.Timer timer = new javax.swing.Timer(50, new ShowHandler());
    boolean emoticons = true;

    /** Creates a new instance of ConversationArea */
    public ConversationArea() {
	pane.setBackground(Color.WHITE);
        setViewportView(pane);
        pane.setEditable(false);

        Color c = UIManager.getColor("TextPane.foreground");
        pane.setForeground(c);

        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        addAdapter(pane);
    }

    public void setEmoticons(boolean e) { this.emoticons = e; }

    /**
     * Sets the log file
     *
     * @param logFile
     *            the file to use for logging
     * @param encoding
     *            the encoding of the log file
     *            (if null, empty, or invalid, the platform default encoding will
     *            be used)
     */
    public void setLogFile(File logFile, String encoding) {
        try {
            fw = null;
            try {
                fw = new OutputStreamWriter(
                    new FileOutputStream(logFile, true), encoding);
            } catch (UnsupportedEncodingException ex) {
            } catch (NullPointerException ex) {
            }
            if (fw == null) {
                fw = new OutputStreamWriter(
                    new FileOutputStream(logFile, true));
            }
            out = new PrintWriter(fw, true);
        } catch (Exception ex) {
            fw = null;
        }
    }

    public void closeLog()
    {
        try {
            if( fw != null ) fw.close();
        }
        catch( Exception ex){}
    }

    public void setText(String text) { pane.setText(text);}

    public JTextPane getTextPane() { return pane; }

    public void append( String text, Color color )
    {
        this.append(text, color, false, Color.WHITE);
    }

    public void append( String text, Color color, boolean bold )
    {
        this.append(text, color, bold, Color.WHITE);
    }

    public String getSelectedText() { return pane.getSelectedText(); }

    public void append( String text )
    {
        this.append(text, Color.BLACK);
    }

    public void appendIcon(ImageIcon icon)
    {
        pane.insertIcon(icon);
    }

    public void append( String text, final Color color,
            final boolean bold, final Color background )
    {
        text = text.replaceAll("\n", " \n");
        if( out != null )
        {
            out.print(text);
            out.flush();
        }

        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setForeground(sas, color);
        StyleConstants.setBackground(sas, background);
        StyleConstants.setBold(sas, bold);

        synchronized(textBuffer)
        {
            textBuffer.add(text);
            sasBuffer.add(sas);
            if(!timer.isRunning()) timer.start();
            else timer.restart();
        }
    }

    private synchronized void showItem(final String text, final SimpleAttributeSet sas)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                final JScrollBar bar = getVerticalScrollBar();
                final int barPos = bar.getValue();

                boolean end = bar.getValue() - ( bar.getMaximum() - bar.getModel().getExtent() ) >= -16;
                Point p = getViewport().getViewPosition();

                boolean scrFlag = bar.isVisible();
                p.y += 50;  // just so it's not the first line (might scroll a bit)
                int pos = pane.viewToModel( p );

                try {

                    ConversationFormatter.getInstance().replaceIcons(text, (StyledDocument)doc, sas, pane, emoticons);
                    //doc.insertString(doc.getLength(), text , sas);

                    if( !end && scrFlag ) pane.setCaretPosition( pos );
                    else pane.setCaretPosition(doc.getLength());

                }
                catch( Exception blx )
                {
                    //com.valhalla.Logger.logException( blx );
                }
            }
        });

        notify();

    }

    class ShowHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            synchronized(textBuffer)
            {
                while(textBuffer.size() > 0)
                {
                    String text = (String)textBuffer.removeFirst();
                    SimpleAttributeSet sas = (SimpleAttributeSet)sasBuffer.removeFirst();
                    showItem(text, sas);
                }

                timer.stop();
            }
        }
    }

    private void addAdapter(final JTextPane pane)
    {
        pane.addMouseListener(new MouseAdapter()
        {
            public void mouseReleased(MouseEvent ev)
            {
                if(ev.getButton() != MouseEvent.BUTTON1) return;
                ev.consume();

                if( pane.getSelectedText() != null ) return;
                int c = pane.viewToModel(ev.getPoint());
                String text = pane.getText().replaceAll("\n", " ");

                if(c == -1) return;
                int b = text.lastIndexOf(" ", c);
                if( b == -1 ) b = 0;
                int e = text.indexOf(" ", c);
                if( e == -1 ) e = text.length();

                String word = text.substring(b, e).trim();
                if(text.indexOf(" ") == -1) word = text;

                String app = null;
                boolean match = false;

                if( Pattern.matches("^.*(\\s|^)((?!(ftp|https?)://)[^\\s\"'\\/]+?@[^\\s\"'\\/]+?)(\\s|$).*$", word))
                {
                    app = Settings.getInstance().getProperty("emailApplication");
                    match = true;
                }
                else if( Pattern.matches("^.*(^|\\s)((ftp|http|https)://[^\\s\"']+?)(\\s|$).*$", word))
                {
                    app = Settings.getInstance().getProperty("browserApplication");
                    match = true;
                }

                if( match )
                {
                    try {
                        if (app != null && !app.equals("")) {
                            /* tail */
                            String command = app
                                    .replaceAll("%s", word);
                            command = command.replaceAll("\\%l", word);

                            Runtime.getRuntime().exec(command);
                        /* tail */
                        } else
                            Standard.warningMessage(BuddyList.getInstance().getContainerFrame(), resources
                                    .getString("hyperlink"), resources
                                    .getString("noApplication"));
                    } catch (java.io.IOException ex) {
                        Standard.warningMessage(BuddyList.getInstance().getContainerFrame(), resources.getString("hyperlink"),
                                resources.getString("errorExecutingApplication"));
                    }
                }
            }
        });
    }
}
