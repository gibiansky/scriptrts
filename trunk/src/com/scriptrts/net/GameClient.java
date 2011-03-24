package com.scriptrts.net;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import com.scriptrts.core.Main;
import com.scriptrts.game.AnimatedSprite;
import com.scriptrts.game.Direction;
import com.scriptrts.game.GameObject;
import com.scriptrts.game.Map;
import com.scriptrts.game.Player;
import com.scriptrts.game.Sprite;
import com.scriptrts.game.UnitClass;
import com.scriptrts.util.ResourceManager;

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
    private DataInputStream input;

    /**
     * Output to server
     */
    private DataOutputStream output;

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
                synchronized(this){
                    connection = new Socket(ip, GameServer.PORT); 

                    output = new DataOutputStream(connection.getOutputStream());
                    input = new DataInputStream(connection.getInputStream());

                    /* Request name and color */
                    output.writeInt(Main.getGame().getPlayer().getID());
                    GameProtocol.sendString(output, Main.getGame().getPlayer().getName());
                    GameProtocol.sendColor(output, Main.getGame().getPlayer().getColor());

                    int id = input.readInt();
                    String name = GameProtocol.readString(input);
                    Color color = GameProtocol.readColor(input);
                    Main.getGame().getPlayer().setName(name);
                    Main.getGame().getPlayer().setColor(color);
                    Main.getGame().getPlayer().setID(id);

                    Map map = GameProtocol.readMap(input);
                    Main.getGame().setCurrentMap(map);
                    System.out.println("Done initializing game client.");
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            new Thread(){
                public void run(){
                    while(true){
                        try {
                            flushRequests();
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
     * Tell the client to send a server an object
     * @param objects what to send
     */
    private void sendRequest(Object... objects){
        synchronized(toSend){
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
    public void sendPathAppendedNotification(GameObject unit, Direction d){
        sendRequest(ServerRequest.PathAppended, new Integer(unit.getID()), new Integer(1), d);
    }

    /**
     * Tell the server that the given unit had a direction appended to its path
     * @param unit unit that has had a path change
     * @param newPath the new path of the unit
     */
    public void sendPathChangedNotification(GameObject unit, Queue<Direction> newPath){
        sendRequest(ServerRequest.PathCleared, new Integer(unit.getID()));


        sendRequest(ServerRequest.PathAppended, new Integer(unit.getID()), new Integer(newPath.size()));
        for(Direction d : newPath){
            sendRequest(d);
        }
    }

    /**
     * Tell the server that a new unit has been added
     * @param unit added unit
     */
    public void sendNewUnitNotification(GameObject unit){
        System.out.println("Found new unit with ID " + unit.getID());
        sendRequest(ServerRequest.NewUnit, unit);
    }

    /**
     * Flush all requests and send them to the server
     */
    private void flushRequests() throws IOException {
        while(toSend.peek() != null){
            Object o = toSend.poll();
            if(o instanceof ServerRequest)
                GameProtocol.sendRequest(output, (ServerRequest) o);
            if(o instanceof Map)
                GameProtocol.sendMap(output, (Map) o);
            if(o instanceof String)
                GameProtocol.sendString(output, (String) o);
            if(o instanceof Color)
                GameProtocol.sendColor(output, (Color) o);
            if(o instanceof GameObject)
                GameProtocol.sendUnit(output, (GameObject) o);
            if(o instanceof Integer)
                output.writeInt(((Integer) o).intValue());
            if(o instanceof Direction)
                GameProtocol.sendDirection(output, (Direction) o);
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

            synchronized(this){
                ServerResponse serverResponse = GameProtocol.readResponse(input);
                if(serverResponse == ServerResponse.UnitUpdate){
                    int sizeNew = input.readInt();
                    for(int i = 0; i < sizeNew; i++) {
                        GameObject newUnit = GameProtocol.readUnit(input);

                        try {
                            /* Retrieve spaceship sprites */
                            BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
                            Sprite[] sprites = new Sprite[16];
                            for(Direction d : Direction.values()){
                                String unitDir = "resource/unit/spaceship/";
                                String unitFilename = "Ship" + d.name() + ".png";
                                BufferedImage img = ResourceManager.loadBandedImage(
                                        unitDir + unitFilename, unitDir + "allegiance/" + unitFilename, newUnit.getUnit().getAllegiance().getColor());
                                sprites[d.ordinal()]  = new Sprite(img, 0.3, 87, 25);
                            }

                            for(Direction d : Direction.values()){
                                String unitDir = "resource/unit/spaceship/";
                                String unitFilename = "Ship" + d.name() + ".png";
                                BufferedImage normalImg = ResourceManager.loadBandedImage(
                                        unitDir + unitFilename, unitDir + "allegiance/" + unitFilename, newUnit.getUnit().getAllegiance().getColor());
                                BufferedImage attackImg = ResourceManager.loadBandedImage(
                                        unitDir + "attack/" + unitFilename, unitDir + "allegiance/" + unitFilename, newUnit.getUnit().getAllegiance().getColor());
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

                            GameObject spaceship = new GameObject(null, sprites, art, 0, 0, 0, null, true, UnitClass.Standard);
                            spaceship.setParameters(newUnit);
                            GameObject unit = spaceship;
                            Main.getGame().getGameGrid().placeUnit(unit, unit.getUnit().getX(), unit.getUnit().getY());
                            Main.getGame().getGameManager().addUnit(unit);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    int sizeUpdated = input.readInt();
                    for(int i = 0; i < sizeUpdated; i++) {
                        GameObject updatedUnit = GameProtocol.readUnit(input);
                        Main.getGame().getGameManager().synchronizeUnit(updatedUnit);
                    }

                }
                if(serverResponse == ServerResponse.NewPlayer){
                    int id = input.readInt();
                    String name = GameProtocol.readString(input);
                    Color color = GameProtocol.readColor(input);
                    Player p = new Player(name, color, id);
                    System.out.println("Received Player " + p);
                    if(!Main.getGame().getPlayers().contains(p))
                        Main.getGame().getPlayers().add(p);
                }
            }
        }
    }
}
