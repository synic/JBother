package com.valhalla.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class MemoryDialog extends JDialog
{
    GridBagLayout grid = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    JButton closeButton = new JButton( "Close" );
    JButton gcButton = new JButton( "Garbage Collection" );
    JLabel total = new JLabel("0k");
    JLabel free = new JLabel("0k");
    JLabel used = new JLabel("0k");
    JPanel middle = new JPanel(grid);
    static MemoryDialog instance = null;
    javax.swing.Timer timer = new javax.swing.Timer(3000, new TimerListener());
    Runtime rt = Runtime.getRuntime();
    long totalk = 0;
    long freek = 0;
    long usedk = 0;
    java.text.NumberFormat nf = java.text.NumberFormat.getInstance();


    public static void main(String args[])
    {
       MemoryDialog.getInstance(null);
    }

    class TimerListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) {setMemory();}
    }

    public static MemoryDialog getInstance(JFrame parent)
    {
        if( instance == null ) instance = new MemoryDialog(parent);

        return instance;
    }

    private MemoryDialog(JFrame parent)
    {
        super(parent, "Heap Statistics");

        JPanel main = (JPanel)getContentPane();
        main.setLayout(new BorderLayout());
        main.setBorder(BorderFactory.createTitledBorder("Heap Statistics"));

        middle.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        c.gridx = 0;
        c.gridy = -1;
        c.weightx = .5;

        addElement("Total Memory:", total);
        addElement("Used Memory:", used);
        addElement("Free Memory:", free);

        c.weighty = 1;
        c.gridwidth = 2;
        c.gridy++;
        JLabel blank = new JLabel();
        grid.setConstraints(blank, c);
        middle.add(blank);

        main.add(middle, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(gcButton);
        buttons.add(closeButton);
        buttons.add(Box.createHorizontalGlue());
        buttons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        addWindowListener(new WindowAdapter()
        {
            public void windowClosed(WindowEvent e)
            {
                closeHandler();
            }
        });

        closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                closeHandler();
            }
        });

        gcButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                System.gc();
                System.gc();
                setMemory();
            }
        });

        main.add(buttons, BorderLayout.SOUTH);
        setMemory();
        timer.start();
        pack();
	setSize(new Dimension(310,140));
	setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void closeHandler()
    {
	timer.stop();
        setVisible(false);
	instance = null;
    }

    private void setMemory()
    {
        totalk = rt.totalMemory() / 1024;
        freek = rt.freeMemory() / 1024;
        usedk = totalk - freek;

        this.total.setText(nf.format(totalk));
        this.free.setText(nf.format(freek));
        this.used.setText(nf.format(usedk));
    }

    private void addElement(String title, JLabel label)
    {
        c.gridx = 0;
        c.gridy++;
	c.weightx = .5;
        c.anchor = GridBagConstraints.EAST;
        JLabel t = new JLabel(title);
        t.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        grid.setConstraints(t, c);
        middle.add(t);
        c.gridx++;
        grid.setConstraints(label,c);
        middle.add(label);
	c.weightx = 0;
	c.gridx++;
	JLabel k = new JLabel("k");
	k.setBorder(BorderFactory.createEmptyBorder(0,0,0,20));
	grid.setConstraints(k,c);
	middle.add(k);
    }
}
