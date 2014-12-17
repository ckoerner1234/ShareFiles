SWE622 Programming Assignment 1   Carolyn Koerner

There is a Client .jar file (FileClient.jar) in /usr/local/shared/SWE622/ckoerner/CLIENT1 and /usr/local/shared/SWE622/ckoerner/CLIENT2/.

There is a Server .jar file (FileServer.jar) in /usr/local/shared/SWE622/ckoerner/SERVER1 and /usr/local/shared/SWE622/ckoerner/SERVER2/.

There are also some test files to use - A text file, a movie file and an image file - testfile.txt, movie.mpg and picture.jpg in the /ckoerner directory.

Run the Server in one of the server directories (SERVER1 or SERVER2) with a port number as the only argument:

Example:  java -jar FileServer.jar 6001

Run the Client in another window in one of the CLIENT directories (CLIENT1 OR CLIENT2) with the IP address and port number as the two arguments:

Example:  java -jar FileClient.jar localhost 6001

A Client connects to a Server by specifying the IPAddress and port number of the Server.  So if you want to connect to a certain Server, make sure you have the same port number as that Server.

The Client program will ask whether you want to upload, download, or exit.  Enter U, D or X.  It will continue to transfer files and ask you this until you enter X, for exit.  

You can have as many Clients as you want - just run FileClient again in each new window to create another Client:  java -jar FileClient.jar localhost 6001

It is best if you copy the FileClient.jar file into a new CLIENT directory and run it there to start another new Client.  There are already two Client directories in /ckoerner called CLIENT1 and CLIENT2 with FileClient.jar in each of them.  So, if you wanted to start a third client, create a CLIENT3 directory and copy FileClient.jar into it and run it there.  All downloaded files (from the Server) will go into the CLIENT directory that the Client is running in.

You can have as many Servers as you want - just run FileServer again in each new window to create another Server, but pick another port number for each Server:
java -jar FileServer.jar 6002

Every Client will have to have the same port number as one of the Servers.  Many Clients can connect to the same Server. A Client can only connect to one Server at a time.  It is best if you copy the FileServer.jar file into a new SERVER directory and run it there to start a new Server.  There are already two SERVER directories in /ckoerner called SERVER1 and SERVER2 with FileServer.jar in each of them.  So, if you wanted to start a third Server, create a SERVER3 directory and copy FileServer.jar into it and run it there.  All uploaded files (from the Client) will go into the SERVER directory that the Server is running in.


When you enter the filename of the file you want to upload/download, it will be relative to the directory the .jar file is in.  So if you want to up/download a file in the same directory as the .jar file, just enter the name.  If you want to up/download a file that is located in a different directory, give the path name, relative to the current directory.   

The first time a Client or Server is started, a /temp directory is created in the same directory as the corresponding jar file.  You can delete this file after you are done running everything (all the Clients and Servers), but do not delete it before that.  You do not have to start out with a temp directory.  If there is not one there when you start a Client or Server, it will be created when you start the Client or Server.

If there is an interruption in the transfer of one of the files, and it is only partially down/up loaded, it will be in the /temp directory (located in the directory that the Client or Server is running in).  If the file is already where you wanted to put it, it will be a complete file.  So, if you interrupt a file transfer and want to resume the up/download, just up/download it again.  It will only up/download the remaining part of the file, i.e. that part that was not already transferred.  When it is complete, it will move the file from the /temp directory to where it is supposed to go (it will no longer be in the /temp directory).  There is a partial image file in /usr/local/shared/SWE622/ckoerner called partial_picture.jpg.  You can test this by putting the partial file in one of the temp directories and renaming it picture.jpg.  For example, put partial_picture.jpg in the CLIENT1/temp directory and rename it picture.jpg.  Then run the Server and Client and ask to download the picture file from the Server.  Make sure there is a whole picture file in the SERVER directory.  The Client program will only download the part of the picture file not already in the partial file.  It will move the (now) whole picture file to the CLIENT1 directory and remove the partial file from CLIENT1/temp.  

If the Server stops running while a Client is connected, you will soon get a message in the Client telling you that you have lost your connection.  You don't need to restart the Client again.  It will ask for the IPAddress and port number of a Server you want to connect to.  It will connect you to the new Server (which should be up and running), and you can continue up/downloading files.



