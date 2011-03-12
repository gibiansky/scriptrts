package com.scriptrts.net;

import java.io.*;
import java.awt.Color;
import java.net.Socket;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

import com.scriptrts.core.Main;
import com.scriptrts.game.Player;
import com.scriptrts.game.Direction;
import com.scriptrts.game.SimpleUnit;

/**
 * Game client for networked games
 */
public class GameClient {
    /**
     * Connection to the server
     */
    private Socket connection;

    /**
     * Input from server
     */
    private ObjectInputStream input;

    /**
     * Output to server
     */
    private ObjectOutputStream output;

    /**
     * Objects to send
     */
    private Queue<Object> toSend = new LinkedList<Object>();

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

    /**
     * Tell the client to send a server a request
     * @param req type of request to send
     * @param objects list of objects to send as data
     */
    private void sendRequest(ServerRequest req, List<Object> objects){
        synchronized(toSend){
            toSend.offer(req);
            for(Object o : objects)
                toSend.offer(o);
        }
    }

    /**
     * Tell the client to send a server a request
     * @param req type of request to send
     * @param objects list of objects to send as data
     */
    private void sendRequest(ServerRequest req, Object... objects){
        synchronized(toSend){
            toSend.offer(req);
            for(Object o : objects)
                toSend.offer(o);
        }
    }

    /**
     * Tell the server that the given unit had a direction appended to its path
     */
    public void sendPathAppendedNotification(SimpleUnit unit, Direction d){
        /* If the server is on this computer, just call the server method */
        if(Main.getGameServer() != null)
            Main.getGameServer().pathAppendedRequest(unit.getID(), d);
        else
            sendRequest(ServerRequest.PathAppended, new Integer(unit.getID()), d);
    }

    public void sendPathChangedNotification(SimpleUnit unit, Queue<Direction> newPath){
        /* If the server is on this computer, just call the server method */
        if(Main.getGameServer() != null)
            Main.getGameServer().pathChangedRequest(unit.getID(), newPath);
        else
            sendRequest(ServerRequest.PathChanged, new Integer(unit.getID()), newPath);

    }

    public GameClient(String ip){
        if(Main.getGameServer() == null){
            try {
                connection = new Socket(ip, GameServer.PORT); 

                output = new ObjectOutputStream(connection.getOutputStream());
                input = new ObjectInputStream(connection.getInputStream());

                /* Request name and color */
                output.writeObject("Player One");
                output.writeObject(java.awt.Color.RED);

                String name = (String) input.readObject();
                Color color = (Color) input.readObject();
            } catch (Exception e){
                e.printStackTrace();
            }

            new Thread(){
                public void run(){
                    while(true){
                        try {
                            synchronized(toSend){
                                flushRequests();
                            }
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

            new Thread(){
                public void run(){
                    try {
                        processUpdates();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public GameClient(){
        this("127.0.0.1");
    }

    private void flushRequests() throws IOException {
        for(Object o : toSend){
            output.writeObject(o);
        }
    }

    /**
     * Listen for updates from the server
     */
    private void processUpdates() throws IOException, ClassNotFoundException {
        while (true) {
            try {
                Thread.sleep(20);
            } catch (Exception e){
                e.printStackTrace();
            }

            System.out.println("READING");
            ServerResponse serverResponse = (ServerResponse) input.readObject();
            System.out.println("Response: " + serverResponse.name());
            if(serverResponse == ServerResponse.UnitUpdate){
                int size = input.readInt();
                System.out.println("Size: " + size);
                for(int i = 0; i < size; i++) {
                    SimpleUnit updatedUnit = (SimpleUnit) input.readObject();
                    Main.getGame().getUnitManager().updateUnit(updatedUnit);

                }

            }
            System.out.println("READ\n");
        }
    }

    private void start(String ip) {
        try {
            Player player = new Player("Nilay", Color.RED);

            System.out.println("Connected to server, sending data.");

            output.writeObject(player.getName());
            output.writeObject(player.getColor());

            String assignedName = (String) input.readObject();
            Color assignedColor = (Color) input.readObject();

            System.out.println("Attempt: " + player);
            player.setName(assignedName);
            player.setColor(assignedColor);
            System.out.println("Result: " + player);

            /* Attempt to change player name and color */
            output.writeObject(ServerRequest.PlayerNameChange);
            output.writeObject("Gibi");
            ServerResponse nameResp = (ServerResponse) input.readObject();

            output.writeObject(ServerRequest.PlayerColorChange);
            output.writeObject(Color.BLACK);
            ServerResponse colorResp = (ServerResponse) input.readObject();

            if(nameResp == ServerResponse.OperationSuccess)
                System.out.println("Player changed to Gibi");
            if(colorResp == ServerResponse.OperationSuccess)
                System.out.println("Color changed to " + Color.BLACK);

            connection.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
