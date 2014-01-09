package project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
	
	private static ServerSocket serverSocket;

	public static void main(String[] args) throws IOException {
		int port = 8080;
		serverSocket = new ServerSocket(port);
		System.out.println("Port Number: " + port);
		
		Socket connectionSocket;
		ServerThread serverThread;

		while (true) {
			connectionSocket = serverSocket.accept();
			if (connectionSocket == null) {
				break;
			}
			
			serverThread = new ServerThread(connectionSocket);
			serverThread.start();
		}
	}
	
}
