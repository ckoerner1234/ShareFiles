

//SWE622 Programming assignment 1   Carolyn Koerner
//java FileClient localhost 6000


import java.io.*;
import java.net.*;
import java.util.Scanner;

class FileClient {

	Socket clientSocket;

	public static void main (String args[]) throws IOException {
		//Needs to be called with the IPAddress and port number as arguments
		//for example:  java FileClient localhost 6001

		String IPAddress = args[0];
		int portnumber = Integer.parseInt(args[1]);
		
		//Creates a temp directory under the directory where the Client is running
		Runtime.getRuntime().exec("mkdir temp");
		
		FileClient client = new FileClient();
		client.go(IPAddress, portnumber);
	}

	public void go(String IPAddress, int portnumber){

		setUpNetworking(IPAddress, portnumber);
		Thread clientThread = new Thread(new HandleClientRequests());
		clientThread.start();
	}

	private void setUpNetworking(String IPAddress, int portnumber){

		try{
			// Create client socket to connect to server 
			clientSocket = new Socket(IPAddress,portnumber);
			System.out.println("Client Socket = " + clientSocket);

		} catch(IOException ex){
			ex.printStackTrace();
		}

	} 

	//Inner class
	public class HandleClientRequests implements Runnable {
		
		ObjectInputStream inFromServer;
		ObjectOutputStream outToServer;
		Scanner keyboard;

		public void run() {

			try{
				// Create input stream attached to socket
				inFromServer = new ObjectInputStream(clientSocket.getInputStream());

				// Create output stream to server
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());  				

				while(true){

					//Find out what the Client wants to do - upload, download or quit
					System.out.println(" ");
					System.out.println("Do you want to upload a file, download a file, or exit?");
					System.out.println("Enter U for upload or D for download X for exit:  ");
					keyboard = new Scanner(System.in);
					String answer = keyboard.nextLine();
					answer = answer.toUpperCase();
					
					//The Client wants to exit
					if(answer.equals("X")){
						System.out.println ("exiting");
						try{
							outToServer.writeObject(answer);
							outToServer.close();
							break;
						} catch(SocketException ex){
							System.out.println("Connection has closed.");
							System.out.println("Need to reconnect.");
							reconnect();
						} catch (Exception ex){
							ex.printStackTrace();
						}
					}

					//The Client wants to download a file from the Server
					if(answer.equals("D")){

						try{
							//send command to server
							outToServer.writeObject(answer);
							outToServer.flush();

							//get input from user (keyboard)
							System.out.println("Input the name of the file you want to download from the server:  \n");
							keyboard = new Scanner(System.in);
							String filename = keyboard.nextLine();
							String downloadedFile = filename;
							String pathOfFile = "";
							
							int indexofslash = filename.lastIndexOf("/");
							if(indexofslash != -1)
							{
							pathOfFile = filename.substring(0, indexofslash+1);
							downloadedFile = filename.substring(indexofslash+1,filename.length());
							}
							
							//send the name of file to be downloaded from the Server
							//try{
								outToServer.writeObject(pathOfFile + downloadedFile);
								outToServer.flush();
							//} catch(IOException ex){
							//	ex.printStackTrace();
							//}

							//get downloaded file from server
							receiveFile(inFromServer, outToServer, downloadedFile);

						} catch(SocketException ex){
							System.out.println("Connection has closed.");
							System.out.println("Need to reconnect.");
							reconnect();
						} catch (Exception ex){
							ex.printStackTrace();
						}

					}

					//The Client wants to upload a file to the Server
					if(answer.equals("U")){

						try{
							//send command to server
							outToServer.writeObject(answer);
							outToServer.flush();

							//Get input from user (keyboard)
							System.out.println("Input the name of the file you want to upload to the server:  \n");
							keyboard = new Scanner(System.in);
							String filename = keyboard.nextLine();
							String uploadedFile = filename;
							String pathOfFile = "";
							
							int indexofslash = filename.lastIndexOf("/");
							if(indexofslash != -1)
							{
							pathOfFile = filename.substring(0, indexofslash+1);
							uploadedFile = filename.substring(indexofslash+1,filename.length());
							}


							//send the name of the file to be uploaded to the Server
							//try{
								outToServer.writeObject(uploadedFile);
								outToServer.flush();
							//} catch(IOException ex){
							//	ex.printStackTrace();
							//}

							sendFile(inFromServer, outToServer, pathOfFile+uploadedFile);

						} catch(SocketException ex){
							System.out.println("Connection has closed.");
							System.out.println("Need to reconnect.");
							reconnect();
						} catch (Exception ex){
							ex.printStackTrace();
						}
					}
				}
			} catch (Exception ex){
				ex.printStackTrace();
			}

		}


		//Receives a file from the Server - downloads it
		public void receiveFile(ObjectInputStream is, ObjectOutputStream os, 
				String downloadedFile) throws Exception{

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] aByte = new byte[1];
			int bytesRead;
			long lengthOfFile;
			long offset = 0;

			//Client will temporarily store the downloaded file in the temp directory.
			//When it is completely downloaded, it will be moved to the final directory.
			
			//If there is a partially uploaded file on the Client, it will be
			//in the temp directory.  If it is there, find out how much of the 
			//file is already uploaded.  The size of the partial file will be
			//the offset - i.e. where the Server will start from when uploading 
			//the file.
			
			String tempDownloadedFile = "temp/" + downloadedFile;
			File tempf = new File(tempDownloadedFile);
			if(tempf.exists()) { 
				long lengthOfTempFile = tempf.length();
				offset = lengthOfTempFile;
			}
			os.writeLong(offset);
			os.flush();


			if (is != null) {

				FileOutputStream fos = null;
				BufferedOutputStream bos = null;
				try {

					//Client gets the length of the whole file from the Server
					lengthOfFile = is.readLong();
					System.out.println("Number of bytes in the file: " + lengthOfFile);
					int numberOfBytesSent = 0;

					//Server is sending the whole file
					if(offset == 0){
						fos = new FileOutputStream(tempDownloadedFile);
						bos = new BufferedOutputStream(fos);
						for (int i=0; i<lengthOfFile; i++){						
							bytesRead = is.read(aByte);
							numberOfBytesSent++;
							baos.write(aByte);
						}	
						bos.write(baos.toByteArray());
					}

					//Server is only sending bytes that were not sent before, from offset to the
					//end of the file.  Appends new data to the end of the file.
					if(offset > 0){
						fos = new FileOutputStream(tempDownloadedFile, true);
						bos = new BufferedOutputStream(fos);
						for (int i=0; i< lengthOfFile-offset; i++){						
							bytesRead = is.read(aByte);
							baos.write(aByte);
							numberOfBytesSent++;
						}	
						bos.write(baos.toByteArray());						
					}
					System.out.println("       Number of bytes sent: " + numberOfBytesSent);

					bos.flush();
					bos.close();

					Runtime.getRuntime().exec("mv " + tempDownloadedFile + " " + 
							downloadedFile);

				} catch (IOException ex) {
					// Do exception handling
					ex.printStackTrace();
				}	
			}

		}

		//Sends a file to the Server - uploads it
		public void sendFile(ObjectInputStream is, ObjectOutputStream os, String uploadedFile) throws Exception{


			if (os != null) {
				File myFile = new File(uploadedFile);
				byte[] mybytearray = new byte[(int) myFile.length()];

				//Get offset from the Server (if the Server already has part of the file)
				long offset = is.readLong();

				//Send length of the whole file to the server
				os.writeLong((long) myFile.length());
				os.flush();

				FileInputStream fis = null;

				try {
					fis = new FileInputStream(myFile);
				} catch (FileNotFoundException ex) {
					// Do exception handling
					System.out.println("  ");
					System.out.println("Can't find that file");
					System.out.println("Please try again");
					System.out.println("      ");
					System.out.println("  ");
				}
				BufferedInputStream bis = new BufferedInputStream(fis);

				try {
					//read in the whole file (on the Client side)
					bis.read(mybytearray, 0, mybytearray.length);
					System.out.println("Number of bytes in the file: " + mybytearray.length);
					
					//only send the part of the file the Server doesn't have already
					os.write(mybytearray, (int)offset, (int)(mybytearray.length - offset));
					int numberOfBytesSent = (int)(mybytearray.length - offset);
					System.out.println("       Number of bytes sent: " + numberOfBytesSent);
					
					os.flush();
					bis.close();

					return;
				} catch (IOException ex) {
					// Do exception handling
					ex.printStackTrace();
				}
			}

		}//end of sendFile
		
		public void reconnect(){
			Scanner keyboard;
			System.out.println("Enter the IPAddress of the Server you want to connect to:  ");		
			keyboard = new Scanner(System.in);
			String IPAddress = keyboard.nextLine();
			System.out.println("Enter the portnumber of the Server you want to connect to:  ");
			keyboard = new Scanner(System.in);
			int portnumber = Integer.parseInt(keyboard.nextLine());

			try{
				// Create client socket to connect to server 
				clientSocket = new Socket(IPAddress,portnumber);
				System.out.println("Client Socket = " + clientSocket);

				// Create input stream attached to socket
				inFromServer = new ObjectInputStream(clientSocket.getInputStream());

				// Create output stream to server
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());  

			} catch(IOException ex){
				ex.printStackTrace();
			}

		}//end of reconnect


	}  //end of class HandleClientRequests

} //end of FileClient class
