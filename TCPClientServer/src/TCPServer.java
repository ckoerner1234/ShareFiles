//SWE622 Programming assignment 1   Carolyn Koerner


import java.io.*;
import java.net.*;

class TCPServer {


	public static void main (String args[]) throws Exception {

		TCPServer server = new TCPServer();
		server.go();
	}

	public void go(){

		try{	
			// Create Welcoming Socket at port 5001
			ServerSocket welcomeSocket = new ServerSocket(5001);

			// Wait for contact-request by clients
			while(true) {

				// Once request arrives allocate new socket
				Socket connectionSocket = welcomeSocket.accept();

				
				Thread t = new Thread(new ClientHandler(connectionSocket));
				t.start();

			} // End of while loop, wait for another client to connect
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public class ClientHandler implements Runnable {

		BufferedReader inFromClient;
		DataOutputStream outToClient;
//		PrintWriter outToClient;

		public ClientHandler(Socket connectionSocket){
			try{
				// Create input stream attached to socket
				inFromClient = new BufferedReader (new
						InputStreamReader(connectionSocket.getInputStream())); 
				// Create & attach output stream to new socket
				outToClient = new DataOutputStream(connectionSocket.getOutputStream());
//				outToClient = new PrintWriter(new
//						OutputStreamWriter(connectionSocket.getOutputStream()));
			}catch(Exception ex){
				ex.printStackTrace();
			}

		}

		public void run(){
			// Read from socket
			try{
				//Sentence comes in from client
				String clientSentence = inFromClient.readLine( );
				//Capitalize it
				String capitalizedSentence = clientSentence.toUpperCase() + "\n";

				// Write to socket
				outToClient.writeBytes(capitalizedSentence);
//				outToClient.println(capitalizedSentence);
				outToClient.flush();
			} catch(Exception ex){
				ex.printStackTrace();
			}

		}
	}

}

