/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author tebus
 */

public class Client extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	private String clientsName;
	private JList<String> memberList;

	public Client(String host) {
		super("Client"); // is gonna be changed later on
		serverIP = host;
		// set the text field
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				sendMessage(event.getActionCommand()); // this is what is in the text field
				userText.setText("");
			}
		});
		// set the chat area
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);

		// The main layout
		Container mainLayout = this.getContentPane();
		mainLayout.setLayout(new BorderLayout());
		mainLayout.setBackground(Color.LIGHT_GRAY);
		mainLayout.add(userText, BorderLayout.PAGE_START);
		mainLayout.add(new JScrollPane(chatWindow), BorderLayout.CENTER);

		
		memberList = new JList<>();
		mainLayout.add(memberList, BorderLayout.EAST);
		//
		setSize(600, 600);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	// connect to server
	public void startRunning() {
		try {
			connectToServer();
			setupStreams();
			askName();
			whileChatting();
		} catch (EOFException eOFException) { // This exception is mainly used by data input streams to signal end of stream.
			showMessage("\nConnection terminated");
		} catch (IOException iOException) {
			iOException.printStackTrace();
		} finally {
			closeCrap();
		}
	}

	private void askName() {

		JFrame askNameWindow = new JFrame();
		askNameWindow.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		askNameWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				askNameWindow.setAlwaysOnTop(false);
				confirmClosing();
				askNameWindow.setAlwaysOnTop(true);
			}
		});
		askNameWindow.setAlwaysOnTop(true);
		JLabel instructionText = new JLabel("Hello! Give me your nickname!");
		JTextField nameField = new JTextField();
		askNameWindow.setSize(400, 80);
		askNameWindow.setLocationRelativeTo(null);
		nameField.addActionListener(event -> {
			clientsName = event.getActionCommand();
			if (!clientsName.equals("")) {
				askNameWindow.setVisible(false);
				this.setTitle(clientsName + "n oma chatti-ikkuna");
				sendWithoutShowing(clientsName);
				ableToType(true);
			} else {
				instructionText.setText("Your nickname should contain one or more letter.\nTry again");
			}
		});
		askNameWindow.setVisible(true);
		askNameWindow.add(instructionText, BorderLayout.NORTH);
		askNameWindow.add(nameField, BorderLayout.AFTER_LAST_LINE);
	}

	// change or update chatWindow
	private void showMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				chatWindow.append(message);
			}
		});
	}

	public void confirmClosing() {
		if (JOptionPane.showConfirmDialog(this, "Do you really want to quit?", "Confirm", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
			System.exit(1); // breaks the application
		} else
			System.out.print("The answer is NOOOOO (so we continue)");

	}

	// close the streams and sockets
	private void closeCrap() {
		showMessage("\nClosing crap down...");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		} catch (IOException iOException) {
			iOException.printStackTrace();
		}
	}

	private void connectToServer() throws IOException {
		showMessage("Attempting connection..");
		connection = new Socket(InetAddress.getByName(serverIP),22); // InetAddress class represents an Internet																	// Protocol (IP) address.
		showMessage("\nConnected to: " + connection.getInetAddress().getHostName() + ": " + serverIP);
	}

	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\nDude your streams are now good to go!!");
	}

	private void whileChatting() throws IOException {
		do {
			try {
			Object msg = input.readObject(); // we're trying to read the input continuously from the server
				if (msg instanceof String) { // jos String
					message = (String) msg;
					showMessage("\n" + message);
				} else if(msg instanceof String[]){
					// ATM there's nothing else to come so....:
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							memberList.setModel(new AbstractListModel<String>() {
								private static final long serialVersionUID = 1L; // ????
								String[] names = (String[]) msg;
								@Override
								public int getSize() {
									return names.length;
								}
								@Override
								public String getElementAt(int index) {
									return names[index];						
								}
								
							});
							
						}	
					});
				}
			} catch (ClassNotFoundException classNotFoundException) {
				showMessage("\nI don't know that object to type");
			}

		} while (true);
	}

	// gives user permission to type into the text box
	private void ableToType(final boolean b) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				userText.setEditable(b);
			}
		});
	}

	// send messages to server
	private void sendMessage(String message) {
		try {
			output.writeObject(clientsName + ": " + message); // we're sending this to server to show.
			output.flush(); // flush the pipe!!
			showMessage("\nMe: " + message); // this one we're displaying on client window
		} catch (IOException iOException) {
			chatWindow.append("\nSomething messed up sending message!");
		}
	}

	private void sendWithoutShowing(String message) {
		try {
			output.writeObject(message);
			output.flush();
		} catch (IOException iOException) {
			iOException.printStackTrace();
		}
	}

	// Keeps track of running clients
	private void updateMemberList() {
		
		showMessage("Members updated");
	}
}
