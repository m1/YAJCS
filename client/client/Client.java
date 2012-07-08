package client;

import java.io.*;
import java.net.*;

class Client {
	private BufferedReader cin;
	private BufferedReader sin;

	private PrintWriter out;
	private InputStreamReader reader;
	private Socket socket;
	private boolean running = true;
	private boolean ok = false;

	public Client(String serverName, int port) {
		// Connect to servername:port
		System.out.println("[CLIENT] Connecting to " + serverName + ":" + port);
		// see if everything is okay
		try {
			socket = new Socket(serverName, port);
			reader = new InputStreamReader(socket.getInputStream());
			sin = new BufferedReader(reader);
			System.out.println(sin.readLine());
			out = new PrintWriter(socket.getOutputStream());
			ok = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (ok) {
			System.out.println("[CLIENT] Connection to server [OK]");
			//start new read thread
			new Read();
		}
	}

	public static void main(String[] argv) throws Exception {
		// make new client connecting to localhost:5000
		new Client("localhost", 5000);
	}

	class Read extends Thread {
		public Read() {
			start();
		}

		public void run() {
			String str;
			try {
				// send login details to server
				System.out.println(sin.readLine());
				out = new PrintWriter(socket.getOutputStream());
				cin = new BufferedReader(new InputStreamReader(System.in));
				sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String user = cin.readLine();
				out.println(user);
				out.flush();
				//start write thread
				new Write();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
			while (running) {
				try {
					str = sin.readLine();
					// if server sends a heartbeat then respond, but don't send
					// it to console.
					if (str.startsWith("[HEARTBEAT]")) {
						out.println("[RESPONSE]");
						out.flush();
					} else {
						System.out.println(str);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	class Write extends Thread {
		public Write() {
			start();
		}

		public void run() {
			String str;
			try {
				cin = new BufferedReader(new InputStreamReader(System.in));
				while ((str = cin.readLine()) != null && running) {
					out.println(str);
					out.flush();
					// if !quit then exit program and send quit message to
					// server
					if (str.equalsIgnoreCase("!quit")) {
						System.exit(0);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}