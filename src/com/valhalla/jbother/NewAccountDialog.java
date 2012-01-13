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

import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.SSLXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import com.valhalla.gui.DialogTracker;
import com.valhalla.gui.Standard;
import com.valhalla.gui.WaitDialog;
import com.valhalla.gui.WaitDialogListener;
import com.valhalla.misc.*;

/**
 * Special <code>RegistrationForm</code> that allows you to register for a
 * jabber account
 *
 * @author Adam Olsen
 * @version 1.0
 */
public class NewAccountDialog extends RegistrationForm {
	private XMPPConnection connection;

	private AccountManager manager;

	private String username, password, server;

	private boolean ssl = false;

	private int port = 5222;

	private ProfileEditorDialog profDialog = null;

	private WaitDialog wait;

	/**
	 * Default constructor
	 *
	 * @param dialog
	 *            the <code>LoginDialog</code> that called this form
	 * @param server
	 *            the server to register for
	 */
	public NewAccountDialog(ProfileEditorDialog dialog, String server,
			String username, String password, int port, boolean useSSL) {
		super(dialog.getDialogParent(),server);

		this.port = port;
		this.ssl = useSSL;
		this.username = username;
        this.server = server;
		this.password = password;
		this.profDialog = dialog;
		setTitle(resources.getString("createNewAccount"));
	}

	/**
	 * Collects the required registration fields from the server
	 */
	public void getRegistrationInfo() {
		GetRegistrationThread thread = new GetRegistrationThread();
		wait = new WaitDialog(this, thread, "Gathering registration form...");
		wait.setVisible(true);

		thread.start();
	}

	/**
	 * Sends the registration information to the server
	 */
	public void register() {
		RegistrationThread thread = new RegistrationThread();

		wait = new WaitDialog(this, thread, "Submitting registration...");
		wait.setVisible(true);

		thread.start();
	}

	/**
	 * Closes the NewAccountDialog
	 */
	public void closeHandler() {
		DialogTracker.removeDialog(this);
	}

	/**
	 * Sends the registration information to the server
	 *
	 * @author Adam Olsen
	 * @version 1.0
	 */
	class RegistrationThread extends Thread implements WaitDialogListener {
		private boolean stopped = false;

		public void cancel() {
			stopped = true;
			interrupt();
		}

		public void run() {
			NewAccountDialog.this.setVisible(false);
			String errorMessage = null;

			String username = null;
			String password = null;

			Hashtable map = new Hashtable();
			for (int i = 0; i < fieldListNames.size(); i++) {
				String name = (String) fieldListNames.get(i);
				JTextField field = (JTextField) fieldListFields.get(i);

				if (name.equals("password"))
					password = field.getText();
				else if (name.equals("username"))
					username = field.getText();
				else
					map.put(name, field.getText());
			}

			try {
				if (!connection.isConnected()) {
					if (!ssl) {
						connection = new XMPPConnection(server, port);
					} else {
						connection = new SSLXMPPConnection(server, port);
					}
				}

				manager.createAccount(username, password, map);
			} catch (XMPPException e) {
				if (stopped)
					return;
				if (e.getXMPPError() == null)
					errorMessage = e.getMessage();
				else
					errorMessage = resources.getString("xmppError"
							+ e.getXMPPError().getCode());
			} catch (IllegalStateException e) {
				if (stopped)
					return;
				errorMessage = e.getMessage();
			}

			if (stopped)
				return;

			connection.close();

			final String tempMessage = errorMessage;
			final String tempUsername = username;
			final String tempPassword = password;

			/**
			 * displays an error if there is one or close the registration
			 * dialog if the registration was successful
			 *
			 * @author Adam Olsen
			 * @version 1.0
			 */
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					wait.dispose();
					if (tempMessage == null) {
						Standard.noticeMessage(null, resources
								.getString("createNewAccount"), resources
								.getString("accountHasBeenCreated"));
						profDialog.setUsername(tempUsername);
						profDialog.setPassword(tempPassword);
                        profDialog.setServer(server);

						DialogTracker.removeDialog(NewAccountDialog.this);
					} else {
						Standard.warningMessage(BuddyList.getInstance(),
								resources.getString("error"), tempMessage);
						setVisible(true);
					}
				}
			});
		}
	}

	/**
	 * Thread to get the required fields from the server. Also builds the
	 * dynamic registration form
	 *
	 * @author Adam Olsen
	 * @version 1.0
	 */
	class GetRegistrationThread extends Thread implements WaitDialogListener {
		private String errorMessage;

		private boolean stopped = false;

		public void cancel() {
			stopped = true;
			interrupt();
		}

		/**
		 * Called by the enclosing thread
		 */
		public void run() {

			try {
				if (!ssl) {
					connection = new XMPPConnection(server, port);
				} else {
					connection = new SSLXMPPConnection(server, port);
				}
			} catch (XMPPException e) {
				if (stopped)
					return;
				if (e.getXMPPError() == null)
					errorMessage = e.getMessage();
				else
					errorMessage = resources.getString("xmppError"
							+ e.getXMPPError().getCode());
			}

			if (errorMessage == null) {
				manager = connection.getAccountManager();

				instructions
						.setText("<html><table width='300' border='0'><tr><td align='center'> "
								+ manager.getAccountInstructions()
								+ "</td></tr></table></html>");
				Iterator iterator = manager.getAccountAttributes();

				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					if (key.equals("username"))
						createInputBox(key, "");
				}

				iterator = manager.getAccountAttributes();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					if (!key.equals("username"))
						createInputBox(key, "");
				}
			}

			if (stopped)
				return;

			/**
			 * Displays the error if there is one, or displays the new
			 * RegistrationForm
			 *
			 * @author Adam Olsen
			 * @version 1.0
			 */
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					wait.dispose();
					if (errorMessage != null) {
						Standard.warningMessage(null, resources
								.getString("registration"), errorMessage);
						DialogTracker.removeDialog(NewAccountDialog.this);
					} else {
						pack();

						setLocationRelativeTo(null);
						setVisible(true);
					}
				}
			});
		}
	}
}