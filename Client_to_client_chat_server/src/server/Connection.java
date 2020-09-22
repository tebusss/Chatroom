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
	private List<String> memberList;

	public Connection(Socket clientSocket, JTextArea cw, List<Connection> c, List<String> memberList) {
		connection = clientSocket;
		clientsRunning = c;
		chatWindow = cw;
		this.memberList = memberList;
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
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chatWindow.append(message); // adds a message to the end of the document
			}
		});

	}

	// send message to other clients
	private void sendMessage(Object message, boolean metoo) {
		for (Connection client : clientsRunning) {
			try {
				if (metoo) {
					client.output.writeObject(message);
					client.output.flush();
				} else if (client != this) {
					client.output.writeObject(message); // sends a message through the output
					client.output.flush();
				}
			} catch (IOException iOException) {
				chatWindow.append("\nERROR: I can't send that message..");
			}
		}
	}

	// during the chat conversation
	private void whileChatting() throws IOException {
		String message = "";
		setClientsName();
		do {
			try {
				message = (String) input.readObject(); // server waits input message
				sendMessage(message, false); // sends the message to the others
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		} while (!message.split(" ")[1].equals("end"));
	}

	private void setClientsName() throws IOException {
		try {
			clientsName = (String) input.readObject(); // this is the first message received so it's the clients name
			memberList.add(clientsName);
			updateMemberList();
			sendMessage(clientsName + " connected", false); // info to the others
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// close streams and sockets after you're done chatting
	private void closeCrap() {
		clientsRunning.remove(this);
		memberList.remove(clientsName);
		updateMemberList();
		showMessage("\nClosing connections...");
		try {
			showMessage(connection.getInetAddress() + " disconnected\n");
			sendMessage(this.clientsName + " disconnected\n", false);
			input.close();
			output.close();
			connection.close();
		} catch (IOException iOException) {
			iOException.printStackTrace();
		}
	}
	private void updateMemberList() {
		String[] newList = new String[memberList.size()];
		int i = 0;
		for(String member : memberList) {
			newList[i] = member;
			i++;
		}
		sendMessage(newList, true);
	}

	/*public String getName() {
		return this.clientsName;
	}*/
}
