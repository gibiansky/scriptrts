package com.scriptrts.net;

import java.io.*;
import java.awt.Color;
import java.net.Socket;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.awt.image.BufferedImage;

import com.scriptrts.core.Main;
import com.scriptrts.util.ResourceManager;
import com.scriptrts.core.Map;
import com.scriptrts.game.Player;
import com.scriptrts.game.*;
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
     * Create a new game client
     * @param ip ip to connect to
     */
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
                Map map = (Map) input.readObject();
                Main.getGame().setCurrentMap(map);
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

    /**
     * Create a new game client connected to localhost
     */
    public GameClient(){
        this("127.0.0.1");
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
     * @param unit unit that has had a path change
     * @param d direction added to path
     */
    public void sendPathAppendedNotification(SimpleUnit unit, Direction d){
        /* If the server is on this computer, just call the server method */
        if(Main.getGameServer() != null)
            Main.getGameServer().pathAppendedRequest(unit.getID(), d);
        else
            sendRequest(ServerRequest.PathAppended, new Integer(unit.getID()), d);
    }

    /**
     * Tell the server that the given unit had a direction appended to its path
     * @param unit unit that has had a path change
     * @param newPath the new path of the unit
     */
    public void sendPathChangedNotification(SimpleUnit unit, Queue<Direction> newPath){
        /* If the server is on this computer, just call the server method */
        if(Main.getGameServer() != null)
            Main.getGameServer().pathChangedRequest(unit.getID(), newPath);
        else
            sendRequest(ServerRequest.PathChanged, new Integer(unit.getID()), newPath);
    }

    /**
     * Tell the server that a new unit has been added
     * @param unit added unit
     */
    public void sendNewUnitNotification(SimpleUnit unit){
        if(Main.getGameServer() == null){
            System.out.println("Found new unit with ID " + unit.getID());
            sendRequest(ServerRequest.NewUnit, unit);
        }
    }

    /**
     * Flush all requests and send them to the server
     */
    private void flushRequests() throws IOException {
        while(toSend.peek() != null){
            Object o = toSend.poll();
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

            ServerResponse serverResponse = (ServerResponse) input.readObject();
            if(serverResponse == ServerResponse.UnitUpdate){
                int sizeNew = input.readInt();
                for(int i = 0; i < sizeNew; i++) {
                    SimpleUnit newUnit = (SimpleUnit) input.readObject();
                    System.out.println("Reading new " + sizeNew);

                    try {
                        /* Retrieve spaceship sprites */
                        BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
                        Sprite[] sprites = new Sprite[16];
                        for(Direction d : Direction.values()){
                            String unitDir = "resource/unit/spaceship/";
                            String unitFilename = "Ship" + d.name() + ".png";
                            BufferedImage img = ResourceManager.loadBandedImage(
                                    unitDir + unitFilename, unitDir + "allegiance/" + unitFilename, Main.getGame().getPlayer().getColor());
                            sprites[d.ordinal()]  = new Sprite(img, 0.3, 87, 25);
                        }

                    System.out.println("Reading XXX " + sizeNew);
                        for(Direction d : Direction.values()){
                            String unitDir = "resource/unit/spaceship/";
                            String unitFilename = "Ship" + d.name() + ".png";
                            BufferedImage normalImg = ResourceManager.loadBandedImage(
                                    unitDir + unitFilename, unitDir + "allegiance/" + unitFilename, Main.getGame().getPlayer().getColor());
                            BufferedImage attackImg = ResourceManager.loadBandedImage(
                                    unitDir + "attack/" + unitFilename, unitDir + "allegiance/" + unitFilename, Main.getGame().getPlayer().getColor());
                            int bX = 87, bY = 25;
                            if(d == Direction.Northwest)
                                bY += 43;
                            if(d == Direction.Southwest)
                                bX += 30;
                            sprites[8 + d.ordinal()]  = new AnimatedSprite(
                                    new BufferedImage[]{
                                        normalImg, attackImg
                                    }, new int[]{
                                        10, 10
                                    }, 0.3, new int[]{
                                        87, bX
                                    }, new int[]{
                                        25, bY
                                    });
                        }
                    System.out.println("Reading YYY " + sizeNew);

                    System.out.println("Reading ZZZ1 " + sizeNew);
                        SimpleUnit spaceship = new SimpleUnit(null, sprites, art, 0, 0, 0, null, true, null);
                    System.out.println("Reading ZZZ2 " + sizeNew);
                        spaceship.setParameters(newUnit);
                    System.out.println("Reading ZZZ2 " + sizeNew);
                        SimpleUnit unit = spaceship;
                    System.out.println("Reading ZZZ3 " + sizeNew);
                        Main.getGame().getUnitGrid().placeUnit(unit, unit.getX(), unit.getY());
                    System.out.println("Reading DDD " + sizeNew);
                        Main.getGame().getUnitManager().addUnit(unit);
                    System.out.println("Reading MMM " + sizeNew);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                int sizeUpdated = input.readInt();
                for(int i = 0; i < sizeUpdated; i++) {
                    System.out.println("Reading updated " + sizeUpdated);
                    SimpleUnit updatedUnit = (SimpleUnit) input.readObject();
                    Main.getGame().getUnitManager().updateUnit(updatedUnit);
                }

            }
        }
    }
}
