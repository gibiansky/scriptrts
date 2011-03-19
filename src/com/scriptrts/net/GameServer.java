package com.scriptrts.net;

import java.util.*;
import java.awt.Color;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.awt.image.BufferedImage;

import com.scriptrts.game.*;
import com.scriptrts.util.ResourceManager;
import com.scriptrts.game.SimpleUnit;
import com.scriptrts.core.HeadlessGame;
import com.scriptrts.core.Main;

/**
 * Game server used to communicate between game clients in a networked game
 */
public class GameServer {

    /**
     * Maximum number of players (connections) allowed.
     */
    public static final int MAX_CONNECTIONS = 2;

    /**
     * Port used by this server
     */
    public static final int PORT = 4242;

    /**
     * Store sockets for each player connection
     */
    private Vector<Socket> connections = new Vector<Socket>();

    /**
     * Store object input and output streams for each player 
     */
    private Vector<DataInputStream> objectInputs = new Vector<DataInputStream>();
    private Vector<DataOutputStream> objectOutputs = new Vector<DataOutputStream>();

    /**
     * Running game
     */
    private HeadlessGame game;

    /**
     * Main entry point for testing this class.
     * @param args command line arguments
     */
    public static void main(String... args) {
        new GameServer().start(new HeadlessGame(129));
    }

    /**
     * Start the server
     * @param headless a headless game instance
     */
    public void start(HeadlessGame headless) {
        this.game = headless;

        new Thread(){
            public void run(){
                try{
                    /* Creates a server socket that lurks about our port waiting for connections */
                    /*
                       ServerSocketChannel serverChannel = ServerSocketChannel.open();
                       serverChannel.configureBlocking(false);
                       serverChannel.socket().bind(new InetSocketAddress(GameServer.PORT));
                       */
                    ServerSocket serverSocket = new ServerSocket(GameServer.PORT);

                    /* Start a new thread to deal with connections */
                    new Thread(new Runnable(){
                        public void run(){
                            try {
                                handleRequests();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    /* Start a new thread to update collections */
                    new Thread(new Runnable(){
                        public void run(){
                            try {
                                sendUpdates();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    /* Keep accepting connections until we're full */
                    while(connections.size() < GameServer.MAX_CONNECTIONS && game != null){
                        Socket connection = serverSocket.accept();

                        /* Store object input and output streams */
                        synchronized(connections){
                            connections.add(connection);
                            objectOutputs.add(new DataOutputStream(connection.getOutputStream()));
                            objectInputs.add(new DataInputStream(connection.getInputStream()));

                            /* Add the player */
                            addPlayerConnection(connection);
                            System.out.println("Done initializing player.");
                        }


                        try {
                            Thread.sleep(300);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    /**
     * Handle requests made by clients
     */
    private void handleRequests() throws IOException, ClassNotFoundException {
        while(game != null){
            try {
                Thread.sleep(10);
            } catch (Exception e){
                e.printStackTrace();
            }

            /* Clone the array list so it isn't modified by a new player joining while we're processing requests */
            try {
                Socket toRemove = null;
                for(Socket socket : connections){
                    try {
                        DataInputStream objIn = objectInputs.get(connections.indexOf(socket));
                        ServerRequest request = GameProtocol.readRequest(objIn);
                        switch(request){
                            case PlayerNameChange:
                                changeNameRequest(socket, objIn);
                                break;
                            case PlayerColorChange:
                                changeColorRequest(socket, objIn);
                                break;
                            case PathAppended:
                                pathAppendedRequest(socket, objIn);
                                break;
                            case PathCleared:
                                pathClearedRequest(socket, objIn);
                                break;
                            case NewUnit:
                                newUnitRequest(socket, objIn);
                                break;
                            default:
                                break;
                        }
                    } catch (IOException e){
                        if(e instanceof EOFException || e.getMessage() != null && e.getMessage().trim().equals("Broken pipe"))
                            toRemove = socket;
                        else
                            throw e;
                    }
                }

                if(toRemove != null)
                    removePlayerConnection(toRemove);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Send updates to clients
     */
    private void sendUpdates() throws IOException, ClassNotFoundException {
        while(game != null){
            try {
                Thread.sleep(10);
            } catch (Exception e){
                e.printStackTrace();
            }

            if(game.getUnitManager() != null){
                synchronized(connections){
                    try {
                        for(Socket socket : connections){
                            try {
                                if(!socket.isClosed() && socket.getOutputStream() != null){
                                    DataOutputStream out = objectOutputs.get(connections.indexOf(socket));
                                    updateClient(out, game.getPlayers().get(connections.indexOf(socket)));
                                }
                            } catch (IOException e){
                                System.out.println("Error in socket!");
                                e.printStackTrace();
                            }
                        }

                        /* If we have removed connections, remove them */
                        for(int i = 0; i < connections.size(); i++){
                            Socket socket = connections.get(i);
                            if(socket.isClosed()){
                                removePlayerConnection(socket);
                                System.out.println("Connection removed.");
                            }
                        }

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                game.getUnitManager().clearUpdates();
            }
        }
    }

    /**
     * Send an update to a given client.
     * @param output output stream connecting server to client
     * @param player player receiving the update
     */
    private void updateClient(DataOutputStream output, Player player) throws IOException, ClassNotFoundException {
        List<SimpleUnit> updated = game.getUnitManager().updatedUnits();
        List<SimpleUnit> created = game.getUnitManager().newUnits();
        if(updated.size() > 0 || created.size() > 0) {
            GameProtocol.sendResponse(output, ServerResponse.UnitUpdate);

            output.writeInt(created.size());
            for(SimpleUnit unit : created)
                GameProtocol.sendUnit(output, unit);

            output.writeInt(updated.size());
            for(SimpleUnit unit : updated)
                GameProtocol.sendUnit(output, unit);
        }
    }

    /**
     * Add a player who has connected to the game
     * @param socket network connection to the player
     */
    private void addPlayerConnection(Socket socket) throws IOException, ClassNotFoundException {
        /* Get player's desired name and color */
        DataInputStream objIn = objectInputs.get(connections.indexOf(socket));
        int desiredID = objIn.readInt();
        String desiredName = GameProtocol.readString(objIn);
        Color desiredColor = GameProtocol.readColor(objIn);

        String name = desiredName;
        Color color = desiredColor;
        int id = desiredID;

        /* Check if desired name is taken */
        if(nameTaken(name)){
            /* Add a number to the name to make it unique */
            int counter = 1;
            while(nameTaken(name + " " + counter))
                counter++;

            name += " " + counter;
        }

        /* Check if the desired color is taken */
        if(colorTaken(color)){
            for(Color c : Player.COLORS)
                if(!colorTaken(c)){
                    color = c;
                    break;
                }
        }

        /* Check if desired ID is taken */
        if(idTaken(id)){
            /* Add a number to the name to make it unique, keep adding until we have a unique ID */
            while(idTaken(id ++));
        }

        Player player = new Player(name, color, id);
        game.addPlayer(player);

        /* Send back the player color and name */
        DataOutputStream objOut = objectOutputs.get(connections.indexOf(socket));
        System.out.println("Allowing " + player + " to join.");
        objOut.writeInt(id);
        GameProtocol.sendString(objOut, name);
        GameProtocol.sendColor(objOut, color);

        /* Send back map */
        GameProtocol.sendMap(objOut, game.getCurrentMap());

        /* Send back players */
        for(Player p : Main.getGame().getPlayers()){
            System.out.println("Writing players " + p);
            GameProtocol.sendResponse(objOut, ServerResponse.NewPlayer);
            objOut.writeInt(p.getID());
            GameProtocol.sendString(objOut, p.getName());
            GameProtocol.sendColor(objOut, p.getColor());
        }

        /* Send back all units as a unit update */
        GameProtocol.sendResponse(objOut, ServerResponse.UnitUpdate);

        Collection<SimpleUnit> all = game.getUnitManager().allUnits();
        objOut.writeInt(all.size());
        for(SimpleUnit unit : all)
            GameProtocol.sendUnit(objOut, unit);

        objOut.writeInt(0);
        objOut.flush();
    }

    /**
     * Remove a connection and the associated player
     * @param socket socket associated with connection to remove
     */
    private void removePlayerConnection(Socket socket) throws IOException {
        int index = connections.indexOf(socket);
        if(index < 0)
            return;

        connections.remove(index);

        objectInputs.get(index).close();
        objectInputs.remove(index);

        objectOutputs.get(index).close();
        objectOutputs.remove(index);

        game.getPlayers().remove(index);
    }

    /**
     * Respond to a request to change a player's name
     */
    public void changeNameRequest(Socket socket, DataInputStream in) throws IOException, ClassNotFoundException {
        String newName = GameProtocol.readString(in);
        Player player = game.getPlayers().get(connections.indexOf(socket));

        /* Check if desired name is taken */
        boolean success = false;
        if(!nameTaken(newName)){
            player.setName(newName);
            success = true;
        }

        if(success)
            GameProtocol.sendResponse(objectOutputs.get(connections.indexOf(socket)), ServerResponse.OperationSuccess);
        else
            GameProtocol.sendResponse(objectOutputs.get(connections.indexOf(socket)), ServerResponse.NameTaken);
    }

    /**
     * Respond to a request to change a player's color
     */
    public void changeColorRequest(Socket socket, DataInputStream in) throws IOException, ClassNotFoundException {
        Color newColor = GameProtocol.readColor(in);
        Player player = game.getPlayers().get(connections.indexOf(socket));

        /* Check if desired name is taken */
        boolean success = false;
        if(!colorTaken(newColor)){
            player.setColor(newColor);
            success = true;
        }

        if(success)
            GameProtocol.sendResponse(objectOutputs.get(connections.indexOf(socket)), ServerResponse.OperationSuccess);
        else
            GameProtocol.sendResponse(objectOutputs.get(connections.indexOf(socket)), ServerResponse.ColorTaken);
    }

    /**
     * Respond to a appended path request
     * @param socket Socket to get data from
     * @param in input stream to read from
     */
    public void pathAppendedRequest(Socket socket, DataInputStream in) throws IOException, ClassNotFoundException {
        int unitID = in.readInt();
        int dirs = in.readInt();
        SimpleUnit unit = Main.getGame().getUnitManager().unitWithID(unitID);

        System.out.println("Here: " + unitID + " " + dirs + " " + unit);
        for(int i = 0; i < dirs; i++){
            System.out.println("read read read " + i);
            Direction d = GameProtocol.readDirection(in);
            System.out.println("path appended server " + unit + " " + d);

            if(unit != null)
                unit.addToPath(d);
        }
    }

    /**
     * Respond to a changed path request
     * @param socket Socket to get data from
     * @param in input stream to read from
     */
    public void pathClearedRequest(Socket socket, DataInputStream in) throws IOException, ClassNotFoundException {
        int unitID = in.readInt();
        SimpleUnit unit = Main.getGame().getUnitManager().unitWithID(unitID);
        System.out.println("path cleared server " + unitID);
        if(unit != null)
            unit.clearPath();
    }


    /**
     * Respond to a new unit request
     * @param socket Socket to get data from
     * @param in input stream to read from
     */
    private void newUnitRequest(Socket socket, DataInputStream in) throws IOException, ClassNotFoundException {
        SimpleUnit newUnit = GameProtocol.readUnit(in);
        System.out.println(Main.getGame().getPlayers());

        try {
            /* Retrieve spaceship sprites */
            BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
            Sprite[] sprites = new Sprite[16];
            for(Direction d : Direction.values()){
                String unitDir = "resource/unit/spaceship/";
                String unitFilename = "Ship" + d.name() + ".png";
                BufferedImage img = ResourceManager.loadBandedImage(
                        unitDir + unitFilename, unitDir + "allegiance/" + unitFilename, newUnit.getAllegiance().getColor());
                sprites[d.ordinal()]  = new Sprite(img, 0.3, 87, 25);
            }

            for(Direction d : Direction.values()){
                String unitDir = "resource/unit/spaceship/";
                String unitFilename = "Ship" + d.name() + ".png";
                BufferedImage normalImg = ResourceManager.loadBandedImage(
                        unitDir + unitFilename, unitDir + "allegiance/" + unitFilename, newUnit.getAllegiance().getColor());
                BufferedImage attackImg = ResourceManager.loadBandedImage(
                        unitDir + "attack/" + unitFilename, unitDir + "allegiance/" + unitFilename, newUnit.getAllegiance().getColor());
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

            SimpleUnit spaceship = new SimpleUnit(null, sprites, art, 0, 0, 0, null, true, Main.getGame().getPathHandler());
            spaceship.setParameters(newUnit);
            SimpleUnit unit = spaceship;
            Main.getGame().getUnitGrid().placeUnit(unit, unit.getX(), unit.getY());
            Main.getGame().getUnitManager().addUnit(unit);
            System.out.println("New Unit ID: " + unit.getID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a given name is taken.
     * @param name name to check
     * @return true if the name is taken
     */
    private boolean nameTaken(String name){
        for(Player p : game.getPlayers())
            if(p.getName().equals(name))
                return true;
        return false;
    }

    /**
     * Check if a given id is taken.
     * @param id id to check
     * @return true if the id is taken
     */
    private boolean idTaken(int id){
        for(Player p : game.getPlayers())
            if(p.getID() == id)
                return true;
        return false;
    }


    /**
     * Check if a given color is taken.
     * @param color color to check
     * @return true if the color is taken
     */
    private boolean colorTaken(Color color){
        for(Player p : game.getPlayers())
            if(p.getColor().equals(color))
                return true;
        return false;
    }

    /**
     * Destroy this server and stop all background threads
     */
    public void destroy() throws IOException {
        game = null;

        synchronized(connections){
            for(DataInputStream in : objectInputs)
                in.close();
            for(DataOutputStream out : objectOutputs)
                out.close();
            for(Socket sock : connections)
                sock.close();

            objectOutputs.clear();
            objectInputs.clear();
            connections.clear();
        }
    }
}
