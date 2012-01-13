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

import com.valhalla.jbother.*;
import com.valhalla.jbother.BuddyList;
import com.valhalla.gui.*;
import com.valhalla.jbother.jabber.*;
import com.valhalla.jbother.groupchat.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import net.infonode.tabbedpanel.*;
import net.infonode.tabbedpanel.titledtab.*;
import net.infonode.util.*;

import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.*;

/**
 * A blank message window - to send "NORMAL" type messages
 *
 * @author     Adam Olsen
 * @created    March 2, 2005
 * @version    1.0
 */
public class MessagePanel extends ConversationPanel implements TabFramePanel
{
	private ResourceBundle resources = ResourceBundle.getBundle( "JBotherBundle", Locale.getDefault() );
	private JLabel toLabel = new JLabel( resources.getString( "to" ) + ": " );
	private JLabel subjectLabel = new JLabel( resources.getString( "subject" ) + ": " );
	private JButton sendButton = new JButton( resources.getString( "send" ) );
	private JButton replyButton = new JButton( resources.getString( "reply" ) );
	private JButton replyQuoteButton = new JButton( resources.getString( "replyQuote" ) );
	private MJTextField toField = new MJTextField();
	private MJTextField subjectField = new MJTextField();
	private MJTextArea textEntryArea = new MJTextArea();
	private JPanel buttonPanel = new JPanel();
    JScrollPane scroll = new JScrollPane(textEntryArea);

	private Message receivedMessage = null;
	private String from = "";
        private TitledTab tab;

	/**
	 *  Description of the Field
	 */
	public final static String QUOTE_STRING = ">";
	/**
	 *  Description of the Field
	 */
	public final static String RECIPIENTS_DELIMITER = ";";
	// character that separates recipients of the message


	/**
	 * Default constructor
	 */
	public MessagePanel()
	{
		super( null );

		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		setLayout( new BorderLayout( 5, 5 ) );

		JPanel topPanel = new JPanel();
		topPanel.setLayout( new BoxLayout( topPanel, BoxLayout.Y_AXIS ) );

		JPanel toPanel = new JPanel( new BorderLayout( 5, 5 ) );
		toPanel.add( toLabel, BorderLayout.WEST );
		toPanel.add( toField, BorderLayout.CENTER );
		toPanel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 5, 0 ) );
		topPanel.add( toPanel );

		JPanel subjectPanel = new JPanel( new BorderLayout( 5, 5 ) );
		subjectPanel.add( subjectLabel, BorderLayout.WEST );
		subjectPanel.add( subjectField, BorderLayout.CENTER );
		topPanel.add( subjectPanel );
		textEntryArea.setWrapStyleWord( true );
		textEntryArea.setLineWrap( true );

		add( topPanel, BorderLayout.NORTH );


		add( scroll, BorderLayout.CENTER );

		buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.X_AXIS ) );
		buttonPanel.add( Box.createHorizontalGlue() );
		buttonPanel.add( sendButton );
		from = BuddyList.getInstance().getConnection().getUser();

		addListeners();

		add( buttonPanel, BorderLayout.SOUTH );

		toField.grabFocus();
	}

    public void setTab( TitledTab tab ) { this.tab = tab; }
    public TitledTab getTab() { return tab; }

	public void setBuddy( BuddyStatus buddy ) { this.buddy = buddy; }

	/**
	 */
	public void createFrame()
	{
		frame = new JFrame();
		frame.setContentPane( this );

		frame.pack();
		frame.setSize( new Dimension( 450, 370 ) );

		frame.setIconImage( Standard.getImage( "frameicon.png" ) );

		Standard.cascadePlacement( frame );

		frame.addWindowListener(
			new WindowAdapter()
			{
				public void windowClosing( WindowEvent e )
				{
					closeHandler();
				}
			} );
	}

	/**
	 *  Gets the buddy attribute of the MessagePanel object
	 *
	 * @return    The buddy value
	 */
	public BuddyStatus getBuddy()
	{
		String from = "unknown";
		from = toField.getText();
		if( from.equals( "" ) )
		{
			from = "unknown";
		}
		BuddyStatus buddy = BuddyList.getInstance().getBuddyStatus( from );
		return buddy;
	}

	/**
	 * Receives a message and displays it in the Dialog
	 *
	 * @param  message  the message packet to receive
	 */
	public void receiveMessage( Message message )
	{
		toLabel.setText( resources.getString( "from" ) + ": " );
		toField.setText( message.getFrom() );
		toField.setEditable( false );
		// to and subject fields are disabled in the
		// receiving dialog
		subjectField.setText( message.getSubject() );
		subjectField.setEditable( false );

		String body = message.getBody();
        remove(scroll);
        add(conversationArea, BorderLayout.CENTER);
		conversationArea.append( body );
		buttonPanel.remove( sendButton );
		buttonPanel.add( replyQuoteButton );
		buttonPanel.add( replyButton );
		buttonPanel.repaint();

		receivedMessage = message;

		repaint();
		validate();
		MessageDelegator.getInstance().showPanel( this );
		MessageDelegator.getInstance().frontFrame( this );
		super.receiveMessage();
	}

	public void setFrom( String from ) { this.from = from; }

	/**
	 * @return    the name of the tab
	 */
	public String getPanelName()
	{
		String to = toField.getText();

		//check to see if it's a private message
		if( BuddyList.getInstance().getTabFrame() != null &&
				BuddyList.getInstance().getTabFrame().isRoomOpen( to.replaceAll( "\\/.*", "" ) ) )
		{
			ChatRoomPanel window = BuddyList.getInstance().getTabFrame().getChatPanel( to.replaceAll( "\\/.*", "" ) );
			if( window != null ) buddy = window.getBuddyStatus( to.replaceAll( "[^/]*\\/", "" ) );
		}
		else {
			Roster r = ConnectorThread.getInstance().getRoster();
			RosterEntry e = r.getEntry( toField.getText().replaceAll( "\\/.*$", "" ) );
			if( e != null )
			{
				to = e.getName();
				if( to == null || to.equals( "" ) ) to = e.getUser();
			}
		}

		if( buddy != null && buddy instanceof MUCBuddyStatus )
		{
			to = buddy.getName();
		}
        
        if (to.length() >= 10 )
        {
            to = to.substring(0, 7) + "...";
        }

		if( !to.equals( "" ) )
		{
			return to;
		}
		else
		{
			return "message";
		}
	}

	/**
	 * @return    the name of the tab
	 */
	public String getWindowTitle()
	{
		if( !toField.getText().equals( "" ) )
		{
			return toField.getText();
		}
		else
		{
			return "message";
		}
	}

	/**
	 * Sets the text in the "To: " field
	 *
	 * @param  to  the string to set the text to
	 */
	public void setTo( String to )
	{
		toField.setText( to );
	}

	/**
	 *  Gets the subjectField attribute of the MessagePanel object
	 *
	 * @return    The subjectField value
	 */
	public MJTextField getSubjectField()
	{
		return subjectField;
	}

	/**
	 * Sets the text in the "Subject:" field
	 *
	 * @param  subject  the string to set the subject text to
	 */
	public void setSubject( String subject )
	{
		subjectField.setText( subject );
	}

	/**
	 * Returns the MJTextArea widget
	 *
	 * @return    The main text area
	 */
	public MJTextArea getTextEntryArea()
	{
		return textEntryArea;
	}

	/**
	 * Called by the reply button to create a reply window
	 *
	 * @param  quote  if true, the message we're replying to will be included
   *                with quoted characters
	 */
	private void replyHandler( boolean quote )
	{
		MessagePanel window = new MessagePanel();
		window.setFrom( from );
		window.setTo( toField.getText() );
		window.setSubject( "Re: " + subjectField.getText() );
		window.validate();
		if( quote == true )
		{
			window.getTextEntryArea().setText( quoteMessage( receivedMessage.getBody(), QUOTE_STRING ) );
		}
		MessageDelegator.getInstance().showPanel( window );
		MessageDelegator.getInstance().frontFrame( window );
		window.getTextEntryArea().grabFocus();

		closeHandler();
	}

	/**
	 * Adds the listeners to the various event emitting widgets
	 */
	private void addListeners()
	{
		MessageActionListener listener = new MessageActionListener();
		sendButton.addActionListener( listener );
		replyButton.addActionListener( listener );
		replyQuoteButton.addActionListener( listener );

		Action closeAction =
			new AbstractAction()
			{
				public void actionPerformed( ActionEvent e )
				{
					closeHandler();
				}
			};

		toField.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), closeAction );
		subjectField.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), closeAction );
		textEntryArea.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), closeAction );
		conversationArea.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), closeAction );
	}

	public JComponent getInputComponent()
	{
		JComponent comp = toField;

		if( !toField.getText().equals( "" ) ) comp = subjectField;
		if( !subjectField.getText().equals( "" ) ) comp = textEntryArea;
		return comp;
	}

  /**
   * Returns "quoted" string; each new line is preceded by aQuoteStr
   *
   * @param  aMsg       - String containing message to quote
   * @param  aQuoteStr  - quote string
   * @return            quoted message
   */
  public String quoteMessage( String aMsg, String aQuoteStr )
  {
    String out = new String();
    StringTokenizer strTok = new StringTokenizer( aMsg, "\n" );
    while( strTok.hasMoreTokens() )
    {
      out += aQuoteStr + " " + strTok.nextToken() + "\n";
    }
    out += "\n";
    return out;
  }

	/**
	 * Sends the message(s)
	 */
	private void sendHandler()
	{
		if( toField.getText().equals( "" ) || textEntryArea.getText().equals( "" ) )
		{
			Standard.warningMessage( this, "messageWindow",
				resources.getString( "mustSpecifyToAndBody" ) );
			return;
		}

		// parse the list of recipients and split it using RECIPIENTS_DELIMITER character
		// then send a message to each of them
		StringTokenizer strTok = new StringTokenizer( toField.getText(), RECIPIENTS_DELIMITER );
		while( strTok.hasMoreTokens() )
		{
			Message message = new Message();

			// sets up the message
			message.setBody( textEntryArea.getText() );
			message.setType( Message.Type.NORMAL );
			message.setSubject( subjectField.getText() );
			message.setFrom( from );
			message.setTo( strTok.nextToken().trim() );
			// make sure to remove unnecessary spaces around the JID

			BuddyList.getInstance().getConnection().sendPacket( message );
		}
		closeHandler();
	}


	/**
	 * Handles the events in the MessageDialog
	 *
	 * @author     Adam Olsen
	 * @created    March 2, 2005
	 * @version    1.0
	 */
	class MessageActionListener implements ActionListener
	{
		/**
		 * called by the button widgets
		 *
		 * @param  e  Description of the Parameter
		 */
		public void actionPerformed( ActionEvent e )
		{
			if( e.getSource() == sendButton )
			{
				sendHandler();
			}
			if( e.getSource() == replyButton )
			{
				replyHandler( false );
			}
			if( e.getSource() == replyQuoteButton )
			{
				replyHandler( true );
			}
		}
	}
}

