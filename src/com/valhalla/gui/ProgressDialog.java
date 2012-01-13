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

package com.valhalla.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

/**
 * Displays a JDialog with a progress bar in it
 *
 * @author Adam Olsen
 * @version 1.0
*/
public class ProgressDialog extends JProgressBar
{
  protected JButton cancelButton = new JButton( "Cancel" );
  protected JDialog container = null;
  protected JPanel mainPanel;
  protected JPanel progressPanel = new JPanel();
  protected JLabel messageLabel = new JLabel( "", SwingConstants.CENTER );
  protected JPanel buttonPanel = new JPanel();

/**
 * @param message the message to display
 * @param min the minimum value of the progress bar
 * @param max the maximum value of the progress bar
*/
  public ProgressDialog( Component parent, String message, int min, int max )
  {
    super( min, max );

    if( parent instanceof Frame )
    {
      container = new JDialog( (Frame)parent );
    }
    else if( parent instanceof Dialog )
    {
      container = new JDialog( (Dialog)parent );
    }
    else container = new JDialog();

    if( parent != null ) container.setLocationRelativeTo( parent );

    messageLabel.setText( message );
    container.setTitle( message );

    mainPanel = (JPanel)container.getContentPane();
    mainPanel.setLayout( new BorderLayout() );
    mainPanel.add( messageLabel, BorderLayout.NORTH );

//  JPanel progressPanel = new JPanel();
    progressPanel.setLayout( new BoxLayout( progressPanel, BoxLayout.Y_AXIS ) );
    progressPanel.add( this );
    progressPanel.add( Box.createVerticalGlue() );
    progressPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

    mainPanel.add( progressPanel, BorderLayout.CENTER );
    mainPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

    buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.X_AXIS ) );
    buttonPanel.add( Box.createHorizontalGlue() );
    buttonPanel.add( cancelButton );
    buttonPanel.add( Box.createHorizontalGlue() );
    setStringPainted( true );

    mainPanel.add( buttonPanel, BorderLayout.SOUTH );
    container.pack();

    container.setSize( new Dimension( 300, 120 ) );
    container.setLocationRelativeTo( null );
    container.setVisible(true);
    container.addWindowListener( new WindowAdapter() {
	public void windowClosing( WindowEvent e ) { }
    } );
  }

  public ProgressDialog()
  {}

  /**
   * Returns the cancel button in the dialog
   * @return the dialog's cancel button
   */
  public JButton getButton() { return cancelButton; }

  /**
   * Deletes the dialog
   */
  public void delete() { container.dispose(); }
  public JDialog getDialog() { return container; }
}
