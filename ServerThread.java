package project;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLConnection;
import java.util.StringTokenizer;

public class ServerThread extends Thread {
	private static final String DEFAULT_PATH = "index.html";
	private Socket connectionSocket;
	private FileInputStream targetFile;

	public ServerThread(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
	}

	public void run() {
		System.out.println("new WebServer Thread Created");

		// get inputStream From Client
		BufferedReader getter = null;

		// send outputStream To Client
		DataOutputStream sender = null;

		try {
			getter = new BufferedReader(
					new InputStreamReader(connectionSocket.getInputStream()) );
			sender = new DataOutputStream(
					connectionSocket.getOutputStream() );

			// read First line From inputStream
			String requestMessageLine = getter.readLine();
			System.out.println(requestMessageLine);

			// request parser
			StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);
			// 
			String requestType = tokenizedLine.nextToken();
			String fileName = tokenizedLine.nextToken();

			// Prevent Client approach to root directory
			if (fileName.startsWith("/") == true) {
				if (fileName.length() > 1) {
					fileName = fileName.substring(1);
				}
				else {
					fileName = DEFAULT_PATH;
				}
			}
			System.out.println("Requested File: " + fileName);

			File file = new File(fileName);

			// When file Dosen't Exists
			if (!file.exists()) {
				System.out.println("Requested File Not Found:" + fileName);

				sender.writeBytes("HTTP/1.1 404 Not Found \r\n");
				sender.writeBytes("Connection: colse\r\n");
				sender.writeBytes("\r\n");
			}

			String mimeType = URLConnection.guessContentTypeFromName(fileName);
			System.out.println("Requested File Type: " + mimeType);
			int numOfBytes = (int) file.length();

			targetFile = new FileInputStream(fileName);
			byte[] fileBuffer = new byte[numOfBytes];
			targetFile.read(fileBuffer);

			if (requestType.equals("GET")) {
				// send HTTP Header
				sender.writeBytes("HTTP/1.1 200 Document Follow \r\n");
				sender.writeBytes("Content-Type: " + mimeType + "\r\n");
				sender.writeBytes("Content-Length: " + numOfBytes + "\r\n");
				sender.writeBytes("\r\n");

				// print request file
				sender.write(fileBuffer, 0, numOfBytes);
			}
			else if(requestType.equals("POST")){
				int contentLength = -1;
				while (true) {
					String line = getter.readLine();
					//System.out.println(line);

					String contentLengthStr = "Content-Length: ";
					if (line.startsWith(contentLengthStr)) {
						contentLength = Integer.parseInt(line.substring(contentLengthStr.length()));
					}

					if (line.length() == 0) {
						break;
					}
				}

				char[] charPostParameters = new char[contentLength];
				getter.read(charPostParameters);
				String strPostParameters = new String(charPostParameters);
				System.out.println("postParameters: " + strPostParameters);

				sender.writeBytes("HTTP/1.1 200 Document Follow \r\n");
				sender.writeBytes("Content-Type: " + mimeType + "\r\n");
				sender.writeBytes("Content-Length: " + (numOfBytes + charPostParameters.length) + "\r\n");
				sender.writeBytes("\r\n");
				sender.writeBytes(strPostParameters + "\r\n");
				sender.writeBytes("\r\n");

				sender.write(fileBuffer, 0, numOfBytes);
			}
			else {
				System.out.println("Bad Request");

				sender.writeBytes("HTTP/1.1 400 Bad Request Mesage \r\n");
				sender.writeBytes("Connection: close\r\n");
				sender.writeBytes("\r\n");
			}

			connectionSocket.close();
			System.out.println("Connection Closed");
		}
		catch(IOException e) {
			e.printStackTrace();
		}

		System.out.println();
	}

}
