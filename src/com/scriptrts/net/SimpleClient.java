package com.scriptrts.net;

import java.io.InputStream;
import java.net.Socket;

/**
 * This class is used as the client for testing bandwidth
 * and data transfer.
 *
 */
public class SimpleClient {

	private static final int PORT = 4242;
	private static final int SIZE = 128 * 128;
	
	/**
	 * Starts the client.
	 * @param args The IP address to connect to.
	 */
	public static void main(String... args) {
		SimpleClient client = new SimpleClient();
		client.start(args[0]);
	}
	
	private void start(String ip) {
		try {
			Socket sock = new Socket(ip, PORT); 
			System.out.println("Connected to server.");
			InputStream input = sock.getInputStream();
			int counter = 0;
			byte[] buf = new byte[SIZE];
			/* Wait until there is something to read */
			while(input.available() == 0)
				;
			/* Read whatever is available to read */
			while(input.available() > 0 && counter < SIZE)
				counter += input.read(buf, counter, SIZE - counter);
			double sum = 0;
			for(byte b : buf)
				sum += b;
			System.out.println("Done reading. Average value of the bytes is: " + sum / buf.length);
			System.out.println("Exiting...");
			sock.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
		
}
