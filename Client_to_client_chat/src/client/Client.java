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

	public Client(String host) {
		super("Client");
		serverIP = host;
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				sendMessage(event.getActionCommand()); // this is what is in the text field
				userText.setText("");
			}
		});
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setSize(600, 200);
	}

	// connect to server
	public void startRunning() {
		try {
			connectToServer();
			setupStreams();
			askName();
			whileChatting();
		} catch (EOFException eOFException) { // This exception is mainly used by data input streams to signal end of
												// stream.
			showMessage("\nConnection terminated");
		} catch (IOException iOException) {
			iOException.printStackTrace();
		} finally {
			closeCrap();
		}
	}

	private void askName() {
		JFrame askNameWindow = new JFrame();
		JLabel instructionText = new JLabel("Hello " + connection.getInetAddress() + ". Give me your nickname!");
		JTextField nameField = new JTextField();
		askNameWindow.setSize(350, 60);
		nameField.addActionListener(event -> {
			clientsName = event.getActionCommand();
			askNameWindow.setVisible(false);
			this.setVisible(true);
			this.setTitle(clientsName + "n oma chatti-ikkuna");
			sendWithoutShowing(clientsName);
		});
		askNameWindow.setVisible(true);
		askNameWindow.add(instructionText, BorderLayout.NORTH);
		askNameWindow.add(nameField, BorderLayout.CENTER);
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
		connection = new Socket(InetAddress.getByName(serverIP), 6789); // InetAddress class represents an Internet
																		// Protocol (IP) address.
		showMessage("\nConnected to: " + connection.getInetAddress().getHostName() + ": " + serverIP);
	}

	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\nDude your streams are now good to go!!");
	}

	private void whileChatting() throws IOException {
		ableToType(true); // make sure this is possible
		do {
			try {
				message = (String) input.readObject(); // we're trying to read the input continuously from the server
				showMessage("\n" + message);
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
}
