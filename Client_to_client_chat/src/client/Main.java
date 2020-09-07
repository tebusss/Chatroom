package client;
import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) {
		String host = "127.0.0.1"; // IP of a server
		Client c = new Client(host);
		c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.startRunning();
	}

}
