package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.w3c.dom.NameList;

public class Server extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea chatWindow;
	private ServerSocket server;
	private Socket connection;
	private List<Connection> connections;
	private List<String> memberList;
	private ExecutorService pool;

	public Server() {
		super("Messenger");

		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300, 150);
		setVisible(true);
		connections = new ArrayList<>();
		memberList = new ArrayList<>();
		pool = Executors.newFixedThreadPool(100);
	}

	// set up and run the server
	public void startRunning() {
		try {
			server = new ServerSocket(22, 100);
			while (true) {
				try {
					connection = null; // nollaa aina kun edellinen yhteys heitetty threadiin ja lisätty listaan
					showMessage(" Waiting for someone to connect...\n");
					connection = server.accept();
					showMessage(" Now connected to " + connection.getInetAddress().getHostName() + "\n"); // in server window
					Connection connectionThread = new Connection(connection, chatWindow, connections, memberList);
					connections.add(connectionThread); // add Connection to the list
					pool.execute(connectionThread);
				} catch (EOFException eofException) {
					showMessage("\n Server ended the connection! ");
				}
			}
		} catch (IOException iOException) {
			iOException.printStackTrace();
		}
	}

	private void showMessage(final String message) {
		SwingUtilities.invokeLater( // creates a thread
				new Runnable() {
					public void run() {
						chatWindow.append(message); // adds a message to the end of the document
					}
				});
	}

	/*private void updateMemberList() {
		String[] newList = new String[connections.size()];
		int i = 0;
		for(Connection c : connections) {
			newList[i] = c.getName();
			i++;
		}
	}	*/
	

}
