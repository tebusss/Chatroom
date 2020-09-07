package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Connection implements Runnable {
	private final Socket connection;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private JTextArea chatWindow;
	private List<Connection> clientsRunning;
	private String clientsName;

	public Connection(Socket clientSocket, JTextArea cw, List<Connection> c) {
		connection = clientSocket;
		clientsRunning = c;
		chatWindow = cw;
	}

	@Override
	public void run() {
		try {
			try {
				setupConnection();
				whileChatting();
			} catch (EOFException e) {
				showMessage("\nConnection terminated..");
			} finally {
				closeCrap();
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public void setupConnection() throws EOFException {
		try {
			output = new ObjectOutputStream(connection.getOutputStream()); // from server
			output.flush();
			input = new ObjectInputStream(connection.getInputStream()); // to server
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getIP() {
		return connection.getInetAddress().getHostName();
	}

	private void showMessage(final String message) {
		SwingUtilities.invokeLater( // creates a thread
				new Runnable() {
					public void run() {
						chatWindow.append(message); // adds a message to the end of the document
					}
				});
	}

	// send message to other clients
	private void sendMessage(String message) {
		for (Connection client : clientsRunning) {
			try {
				if (client != this)
					client.output.writeObject(message); // sends a message through the output
				client.output.flush();
			} catch (IOException iOException) {
				chatWindow.append("\nERROR: I can't send that message..");
			}
		}
	}

	// during the chat conversation
	private void whileChatting() throws IOException {
		String message = "";
		getClientsName();
		do {
			try {
				message = (String) input.readObject(); // server waits input message
				sendMessage(message); // sends the message to the others
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		} while (!message.split(" ")[1].equals("end"));
	}

	private void getClientsName() throws IOException {
		try {
			clientsName = (String) input.readObject(); // this is the first message received so it's the clients name
			sendMessage(clientsName + " connected"); // info to the others
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// close streams and sockets after you're done chatting
	private void closeCrap() {
		clientsRunning.remove(this);
		showMessage("\nClosing connections...");
		try {
			showMessage("\n" + connection.getInetAddress() + " disconnected");
			sendMessage("\n" + this.clientsName + " disconnected");
			input.close();
			output.close();
			connection.close();
		} catch (IOException iOException) {
			iOException.printStackTrace();
		}
	}
}
