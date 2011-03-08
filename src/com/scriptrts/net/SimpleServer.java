package com.scriptrts.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class is used as the server for testing bandwidth
 * and data transfer.
 *
 */
public class SimpleServer {

	/**
	 * Starts the server.
	 * @param args
	 */
	public static void main(String... args) {
        while(true)
            new SimpleServer().start();
	}

	private void start() {
		try{
			/* Creates a server socket that lurks about port 4242
			 * and has a maximum connections number of 1.
			 */
			ServerSocket sSock = new ServerSocket(4242, 1);
			/* Block until a connection is found */
			Socket cSock = sSock.accept();
			System.out.println("Received a connection from " + cSock.getInetAddress());
			byte[] arr = new byte[128 * 128];
			for(int i = 0; i < arr.length; i++)
				arr[i] = 1;
			System.out.println("Writing...");
			/* Write a byte array of 1s. */
			cSock.getOutputStream().write(arr);
			cSock.getOutputStream().flush();
			cSock.close();
			sSock.close();
			System.out.println("Closed connections... exiting.");
		} catch(IOException e) {
			System.out.println("Could not listen at port 4242");
			System.exit(1);
		}
	}
}
