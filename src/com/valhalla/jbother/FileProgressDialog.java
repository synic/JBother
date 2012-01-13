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

import com.valhalla.gui.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.util.*;
import javax.swing.*;
import java.beans.*;
import org.jivesoftware.smackx.filetransfer.*;

/**
 *
 * @author synic
 */
public class FileProgressDialog extends JFrame {

    private static FileProgressDialog instance = null;
    private JPanel main;
    private JButton clear = new JButton("Clean Up");
    private JButton close = new JButton("Close");
    private JButton cancel = new JButton("Cancel");
    private Component glue = Box.createVerticalGlue();
    private TableModel model = new TableModel();
    private JTable table = new JTable(model);
    java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
    java.text.NumberFormat nf2 = java.text.NumberFormat.getInstance();


    /** Creates a new instance of FileProgressDialog */
    private FileProgressDialog() {
        super("File Transfer Manager");

        setIconImage(Standard.getImage("frameicon.png"));

        main = (JPanel)getContentPane();
        main.setLayout(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        nf.setMaximumFractionDigits(1);
        nf.setMinimumFractionDigits(1);
        JScrollPane scroll = new JScrollPane(table);
        table.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);

        main.add(scroll, BorderLayout.CENTER);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setTableHeader(null);

        // get the height for each row
        Window w = new Window(this);
        w.setLayout(new BorderLayout());
        TransferPanel tp = new TransferPanel(null,"", "",66, 0);
        tp.bar.setValue(100);
        w.add(tp, BorderLayout.CENTER);
        w.pack();

        table.setRowHeight(w.getHeight() + 18);
        w.dispose();

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setResizable(false);
        col.setPreferredWidth(28);
        col.setMaxWidth(28);
        col = table.getColumnModel().getColumn(1);
        col.setPreferredWidth(376);
        table.setDefaultRenderer(TransferPanel.class, new CellRend());
        table.setDefaultRenderer(IconLabel.class, new IconRend());

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(cancel);
        buttons.add(clear);
        buttons.add(close);
        buttons.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
        close.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        });

        clear.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cleanup();
            }
        } );

        cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int row = table.getSelectedRow();
                if(row != -1)
                {
                    TransferPanel panel = (TransferPanel)model.getValueAt(row, 1);
                    if(!panel.getDone()) panel.cancel();
                }
            }
        } );

        main.add(buttons, BorderLayout.SOUTH);
        pack();
        setSize(new Dimension(450, 250));
        Standard.cascadePlacement(this);
    }

    private void cleanup()
    {
        ArrayList items = model.getItems();
        for( int i = 0; i < items.size(); i++ )
        {
            TransferPanel panel = (TransferPanel)items.get(i);
            if(panel.getDone())
            {
                model.remove(panel);
                i--;
            }
        }
    }

    public static FileProgressDialog getInstance()
    {
        if(instance == null) instance = new FileProgressDialog();
        return instance;
    }

    public void addFile(FileTransfer transfer, String jid, String name, int type, long size)
    {
        setVisible(true);
        new TransferPanel(transfer, jid, name, type, size);
    }

    class TransferPanel extends JPanel
    {
        JProgressBar bar = new JProgressBar(0, 100);
        private String fileName = null;
        private JLabel percent = new JLabel("0%");
        private int status = 3;
        private int type = 0;
        private IconLabel l = new IconLabel(this);
        private FileTransfer transfer;
        private String name;
        private long size;
        private long current = 0;
        private long lastSize = 0;
        private long last = 0;
        private javax.swing.Timer rateTimer = new javax.swing.Timer(1000, new RateHandler());
        private double rate = 0;
        private Vector avg = new Vector();
        private int counter = 0;
        private boolean popShown = false;
        private String tooltip = null;
        private String jid, from;

        public TransferPanel(FileTransfer transfer, String jid, String name, int type, long size)
        {
            this.type = type;
            this.transfer = transfer;
            this.jid = jid;
            this.from = ConnectorThread.getInstance().getConnection().getUser();
            this.size = size;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            this.name = name;

            JLabel n = new JLabel(name);
            n.setToolTipText(getTooltip());
            l.setToolTipText(getTooltip());
            setToolTipText(getTooltip());
            add(n, BorderLayout.NORTH);

            bar.setToolTipText(getTooltip());
            percent.setToolTipText(getTooltip());

            add(bar, BorderLayout.CENTER);
            add(percent, BorderLayout.SOUTH);

            this.fileName = name;
            if(type != 66) model.add(this);
            startMonitor();
        }

        public String getTooltip()
        {
            if(transfer == null) return "";
            if(tooltip == null)
            {
                StringBuffer buff = new StringBuffer();
                buff.append("<html><table><tr><td><b>File: </b></td><td>");

                String loc = name;
                if(type == 1) loc = transfer.getFileName();
                buff.append(loc).append("</td></tr>");
                if(type == 0)
                {
                    buff.append("<tr><td><b>To: </b></td><td>").append(jid).append("</td></tr>");
                }
                else {
                    buff.append("<tr><td><b>From: </b></td><td>").append(from).append("</td></tr>");
                }

                String desc = ""; //transfer.getDescription();
                if(desc.equals("")) desc = "None given";
                buff.append("<tr><td><b>Description:</b></td><td>").append(desc).append("</td></tr>");
                buff.append("</table></html>\n");
                tooltip = buff.toString();
            }
            return tooltip;
        }

        class RateHandler implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                double diff = current - last;
                last = current;

                avg.add(0, new Double(diff));
                if(avg.size() > 5) avg.setSize(5);

                if(avg.size() < 5) rate = diff / 1024.0;
                else {
                    double value = 0;
                    for(int i = 0; i < avg.size(); i++)
                    {
                        Double d = (Double)avg.get(i);
                        value += d.doubleValue();
                    }

                    rate = (value / avg.size()) / 1024.0;
                }
            }
        }

        public IconLabel getLabel()
        {
            return l;
        }

        public int getStatus()
        {
            return status;
        }

        public boolean getDone()
        {
            return transfer.isDone();
        }

        public void cancel() 
        {   
            transfer.cancel();
            rateTimer.stop();
            status = 4; 
        }

        public void setSelected(boolean selected)
        {
            if(selected) setBackground(table.getSelectionBackground());
            else setBackground(Color.WHITE);
        }

        class MonitorThread extends Thread
        {
            FileTransfer transfer;
            public MonitorThread(FileTransfer transfer)
            {
                this.transfer = transfer;
            }
            
            public void run()
            {
                while( !transfer.isDone() )
                {
                    current = transfer.getAmountWritten();
                    try {
                        Thread.sleep(500);
                    }
                    catch(Exception ex) { }
                    if( transfer.getStatus().equals(FileTransfer.Status.ERROR) )
                    {
                        status = 4;
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            int val = (int)(transfer.getProgress() * 100);
                            bar.setValue(val);
                            table.repaint();
                            table.validate();
                            percent.setText(val + "%");
                        }
                    } );
                }

            }
        }
        
        public void startMonitor()
        {
            if( transfer == null ) return;
            if( !rateTimer.isRunning()) rateTimer.start();
            status = type;
            new MonitorThread(transfer).start();
        }

        public void update(final String event, final int percent, final long bytes)
        {
            if(!rateTimer.isRunning()) rateTimer.start();

            if(event.equals("Transferring")) status = type;
            else if(event.equals("Done")) status = 5;
            else if(event.equals("Error") || event.equals("Cancelled")) status = 4;

            if( status > 1 ) rateTimer.stop();

            if(counter++ < 5 && status == type) return;
            counter = 0;

            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                   long b = bytes;
                   if(bytes <= 0)
                   {
                       b = lastSize;
                   }
                   else {
                       bar.setValue(percent);
                   }
                   lastSize = b;
                   current = b;
                   String transferred = nf2.format(b / 1024) + "k";
                   String total = nf2.format(size / 1024) + "k";

                   TransferPanel.this.percent.setText(percent + "% - " + event + " - " +
                            transferred + "/" + total + " - " +   nf.format(rate) + "KB/s");
                   table.repaint();

                   if(status == 5 && !popShown)
                   {
                       popShown = true;
                       NotificationPopup.showSingleton(FileProgressDialog.this, "Transfer Complete", transfer.getFileName(), TransferPanel.this);
                   }
               }
            });
        }
    }

    class CellRend extends JPanel implements TableCellRenderer
    {

        public Component getTableCellRendererComponent(
                            JTable table, Object color,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {

            TransferPanel panel = (TransferPanel)color;
            setToolTipText(panel.getTooltip());
            panel.setSelected(isSelected);
            panel.validate();
            return (JPanel)color;
        }
    }

    class IconLabel extends JLabel
    {
        private ImageIcon up = Standard.getIcon("images/buttons/Up24.gif");
        private ImageIcon down = Standard.getIcon("images/buttons/Down24.gif");
        private ImageIcon error = Standard.getIcon("images/buttons/Error24.gif");
        private ImageIcon wait = Standard.getIcon("images/buttons/Wait24.gif");
        private ImageIcon done = Standard.getIcon("images/buttons/Done24.gif");

        private TransferPanel panel;

        public IconLabel(TransferPanel panel)
        {
            setPreferredSize(new Dimension(28, 28));
            setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            this.panel = panel;
            setOpaque(true);
        }

        public void updateIcon()
        {
            int status = panel.getStatus();

            switch(status)
            {
                case 0:
                    setIcon(up);
                    break;
                case 1:
                    setIcon(down);
                    break;
                case 4:
                    setIcon(error);
                    break;
                case 5:
                    setIcon(done);
                    break;
                default:
                    setIcon(wait);
                    break;
            }
        }

        public void setSelected(boolean selected)
        {
            if(selected) setBackground(table.getSelectionBackground());
            else setBackground(Color.WHITE);
        }

    }

    class IconRend extends JPanel implements TableCellRenderer
    {
        public Component getTableCellRendererComponent(
                            JTable table, Object color,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {

            IconLabel panel = (IconLabel)color;
            panel.setSelected(isSelected);

            panel.setSelected(isSelected);
            panel.updateIcon();
            panel.validate();
            return (JLabel)panel;
        }
    }

    class TableModel extends AbstractTableModel
    {
        private ArrayList items = new ArrayList();

        public synchronized void add(TransferPanel panel)
        {
            items.add(0, panel);
            fireTableRowsInserted(items.size(), items.size());
        }

        public ArrayList getItems() { return items; }

        public int getColumnCount() { return 2; }

        public int getRowCount() { return items.size(); }

        public Object getValueAt(int row, int col)
        {
            TransferPanel panel = (TransferPanel)items.get(row);
            if(col == 0) return panel.getLabel();
            else return panel;
        }

        public Class getColumnClass(int c)
        {
            if(c == 0) return IconLabel.class;
            else return TransferPanel.class;
        }

        public synchronized void remove(TransferPanel panel)
        {
            int index = items.indexOf(panel);
            if(index != -1) items.remove(index);
            fireTableRowsDeleted(items.size(), items.size());
        }


    }
}
