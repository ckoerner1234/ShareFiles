

//SWE622 Programming assignment 1   Carolyn Koerner
//java FileServer 6000
//

import java.io.*;
import java.net.*;
import java.util.Scanner;

class FileServer {
	Socket connectionSocket;

	public static void main (String args[]) throws IOException {
		//Needs to be called with the port number as an argument
		//for example:  java FileServer 6001

		int portnumber = Integer.parseInt(args[0]);
		
		//Creates a temp directory under the directory where the Server is running
		Runtime.getRuntime().exec("mkdir temp");
		FileServer server = new FileServer();
		server.go(portnumber);
	}

	public void go(int portnumber){

		try{	
			// Create Welcoming Socket 
			ServerSocket welcomeSocket = new ServerSocket(portnumber);

			// Wait for contact-request by clients
			while(true) {

				// Once request arrives allocate new socket
				connectionSocket = welcomeSocket.accept();
				System.out.println("Accepted connection : " + connectionSocket);

				Thread t = new Thread(new ClientHandler());
				t.start();

			} // End of while loop, wait for another client to connect
		} catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public class ClientHandler implements Runnable {

		public ClientHandler(){

		}

		public void run(){

			try{

				ObjectOutputStream outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());  
				ObjectInputStream inFromClient = new ObjectInputStream(connectionSocket.getInputStream()); 
				
				String up_directory = (String) inFromClient.readObject( );
				
				while(true){

					try{

						String clientCommand = (String) inFromClient.readObject( );
						clientCommand = clientCommand.toUpperCase();

						if(clientCommand.equals("X")){
							System.out.println ("exiting");
							try{
								outToClient.close();
								break;
							} catch (Exception ex){
								ex.printStackTrace();
							}
						}

						//Client wants to upload a file to the Server
						if(clientCommand.equals("U")){

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							byte[] aByte = new byte[1];
							int bytesRead;
							long lengthOfFile;
							long offset = 0;

							
							//Server receives the name of the file
							String filename = (String) inFromClient.readObject( );
							
							//Server will temporarily store the downloaded file in the temp directory.
							//When it is completely downloaded, it will be moved to the main directory.
							
							//If there is a partially uploaded file on the Server, it will be
							//in the ~/temp directory.  If it is there, find out how much of the 
							//file is already uploaded.  The size of the partial file will be
							//the offset - i.e. where the Client will start from when uploading 
							//the file.
							String tempUploadedFile = "temp/" + filename;
							File tempf = new File(tempUploadedFile);
							if(tempf.exists()) { 
								long lengthOfTempFile = tempf.length();
								offset = lengthOfTempFile;
							}
							
							//Server sends the offset to the Client 
							outToClient.writeLong((long) offset);
							outToClient.flush();

							//Receive the file from the Client
							if (inFromClient != null) {

								FileOutputStream fos = null;
								BufferedOutputStream bos = null;
								try {
									
									//Get the length of file to upload to the Server
									lengthOfFile = inFromClient.readLong();

									//Client is sending the whole file
									if(offset == 0){
										fos = new FileOutputStream(tempUploadedFile);
										bos = new BufferedOutputStream(fos);
										for (int i=0; i<lengthOfFile; i++){						
											bytesRead = inFromClient.read(aByte);
											baos.write(aByte);
										}
										bos.write(baos.toByteArray());
									}
									
									//Client is only sending bytes that were not sent before, from offset to the
									//end of the file.  Appends data to the end of the file.
									if(offset > 0){
										fos = new FileOutputStream(tempUploadedFile, true);
										bos = new BufferedOutputStream(fos);
										for (int i=0; i< lengthOfFile-offset; i++){						
											bytesRead = inFromClient.read(aByte);
											baos.write(aByte);
										}	
										bos.write(baos.toByteArray());																
									}
									
									bos.flush();
									bos.close();
									
									//Move file from the temp directory to the main directory
									Runtime.getRuntime().exec("mv " + tempUploadedFile + " "  + 
											up_directory + filename);
									
								} catch (IOException ex) {
									// Do exception handling
									ex.printStackTrace();
								}	
							}
						}

						//Client wants to download a file from the Server
						if(clientCommand.equals("D")){
							
							if (outToClient != null) {

								//Get the name of the file the Client wants to download
								String filename = (String) inFromClient.readObject( );
								File myFile = new File(filename);
								
								//Get offset from Client. If the Client already has part of the file,
								//offset will be > 0.  If the Client doesn't already have part of the file,
								//i.e. it has not been partially downloaded, offset will be 0.
								long offset = inFromClient.readLong();

								//Server sends length of the whole file to Client
								outToClient.writeLong((long) myFile.length());
								outToClient.flush();

								byte[] mybytearray = new byte[(int) myFile.length()];

								FileInputStream fis = null;

								try {
									fis = new FileInputStream(myFile);
								} catch (FileNotFoundException ex) {
									// Do exception handling
									System.out.println("  ");
									System.out.println("Can't find that file");
									System.out.println("Please try again");
									System.out.println("      ");									
									ex.printStackTrace();
									System.out.println("  ");
								}
								
								BufferedInputStream bis = new BufferedInputStream(fis);
								
								//Send file to the Client
								try {
									//read in the whole file (on the Server side)
									bis.read(mybytearray, 0, mybytearray.length);
									
									//only send the part of the file that the Client doesn't already have
									outToClient.write(mybytearray, (int)offset, (int)(mybytearray.length - offset));
									
									outToClient.flush();
									bis.close();
								} catch (IOException ex) {
									// Do exception handling
									ex.printStackTrace();
								}
							}
						}

					} catch(Exception ex){
						ex.printStackTrace();
					}

				}
			}catch(Exception ex){				
				// Do exception handling
				ex.printStackTrace();
			}
		}
	}//End of class ClientHandler

}//End of class FileServer

