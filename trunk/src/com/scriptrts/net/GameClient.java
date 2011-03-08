package com.scriptrts.net;

import java.io.*;
import java.awt.Color;
import java.net.Socket;

import com.scriptrts.game.Player;

/**
 * Game client for networked games
 */
public class GameClient {

	/**
	 * Starts the client.
	 * @param args The IP address to connect to.
	 */
	public static void main(String... args) {
		GameClient client = new GameClient();
        if(args.length >= 1)
            client.start(args[0]);
        else
            client.start("127.0.0.1");
	}
	
	private void start(String ip) {
		try {
            Player player = new Player("Nilay", Color.RED);

			Socket socket = new Socket(ip, GameServer.PORT); 
			System.out.println("Connected to server, sending data.");
            
            ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
            objOut.writeObject(player.getName());
            objOut.writeObject(player.getColor());

            ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
            String assignedName = (String) objIn.readObject();
            Color assignedColor = (Color) objIn.readObject();

            System.out.println("Attempt: " + player);
            player.setName(assignedName);
            player.setColor(assignedColor);
            System.out.println("Result: " + player);

            /* Attempt to change player name and color */
            objOut.writeObject(ServerRequest.PlayerNameChange);
            objOut.writeObject("Gibi");
            ServerResponse nameResp = (ServerResponse) objIn.readObject();


            objOut.writeObject(ServerRequest.PlayerColorChange);
            objOut.writeObject(Color.BLACK);
            ServerResponse colorResp = (ServerResponse) objIn.readObject();

            if(nameResp == ServerResponse.OperationSuccess)
                System.out.println("Player changed to Gibi");
            if(colorResp == ServerResponse.OperationSuccess)
                System.out.println("Color changed to " + Color.BLACK);

			socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
		
}
