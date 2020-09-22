package client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;


public class Main {

	public static void main(String[] args) {
		String host = "127.0.0.1"; // IP of a server
		Client c = new Client(host);
		c.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		c.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				c.confirmClosing();
			}
		});
		c.startRunning();
	}

}
