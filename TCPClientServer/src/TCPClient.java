//SWE622 Programming assignment 1   Carolyn Koerner


import java.io.*;
import java.net.*;

class TCPClient {

	BufferedReader inFromUser;
	Socket clientSocket;
	DataOutputStream outToServer;
	BufferedReader inFromServer;

	public static void main (String args[]) throws Exception {

		TCPClient client = new TCPClient();
		client.go();
	}

	public void go(){

		setUpNetworking();
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}

	private void setUpNetworking(){

		try{
			// Create client socket to connect to server 
			clientSocket = new Socket("127.0.0.1",5001);
			
			// Create input stream from user
			inFromUser = new BufferedReader(new InputStreamReader(System.in));

			// Create output stream to server
			outToServer = new DataOutputStream(clientSocket.getOutputStream());

			// Create input stream attached to socket
			inFromServer = new BufferedReader (new
					InputStreamReader(clientSocket.getInputStream()));
		} catch(IOException ex){
			ex.printStackTrace();
		}

	}


	public class IncomingReader implements Runnable {
		public void run() {

			try{
				//get input from user (keyboard)
				String sentence  = inFromUser.readLine();

				// Send line to server
				outToServer.writeBytes(sentence + "\n");
				outToServer.flush();

				String modifiedSentence = inFromServer.readLine();
				// Read line from server
				System.out.println("FROM SERVER: " + modifiedSentence);

			} catch (Exception ex){
				ex.printStackTrace();
			}

		}

	}


}
