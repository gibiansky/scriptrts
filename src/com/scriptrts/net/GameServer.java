package com.scriptrts.net;

import java.util.Vector;
import java.util.Queue;
import java.awt.Color;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

import com.scriptrts.game.Direction;
import com.scriptrts.game.Player;
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
    private Vector<ObjectInputStream> objectInputs = new Vector<ObjectInputStream>();
    private Vector<ObjectOutputStream> objectOutputs = new Vector<ObjectOutputStream>();

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
                    ServerSocketChannel serverChannel = ServerSocketChannel.open();
                    serverChannel.configureBlocking(false);
                    serverChannel.socket().bind(new InetSocketAddress(GameServer.PORT));

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
                        SocketChannel connectionChannel = serverChannel.accept();

                        if(connectionChannel != null){
                            Socket connection = connectionChannel.socket();

                            /* Store object input and output streams */
                            synchronized(connections){
                                connections.add(connection);
                                objectInputs.add(new ObjectInputStream(connection.getInputStream()));
                                objectOutputs.add(new ObjectOutputStream(connection.getOutputStream()));

                                /* Add the player */
                                addPlayerConnection(connection);
                            }
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
            synchronized (connections){
                for(Socket socket : connections){
                    if(socket.isClosed())
                        connections.remove(socket);
                    else {
                        if(socket.getInputStream() != null && socket.getInputStream().available() > 0){
                            ObjectInputStream objIn = objectInputs.get(connections.indexOf(socket));
                            ServerRequest request = (ServerRequest) objIn.readObject();
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
                                default:
                                    break;
                            }
                        }
                    }
                }
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

            /* Clone the array list so it isn't modified by a new player joining while we're processing requests */
            synchronized (connections){
                for(Socket socket : connections){
                    if(socket.isClosed())
                        connections.remove(socket);
                    else if(socket.getOutputStream() != null){
                        ObjectOutputStream out = objectOutputs.get(connections.indexOf(socket));
                        updateClient(out, game.getPlayers().get(connections.indexOf(socket)));
                    }
                }

                if(Main.getGameClient() != null){
                    /* We don't need to do anything because the server's game is already updated */
                }
            }
        }
    }

    private void updateClient(ObjectOutputStream output, Player player) throws IOException, ClassNotFoundException {

    }

    /**
     * Add a player who has connected to the game
     * @param socket network connection to the player
     */
    private void addPlayerConnection(Socket socket) throws IOException, ClassNotFoundException {
        /* Get player's desired name and color */
        ObjectInputStream objIn = objectInputs.get(connections.indexOf(socket));
        String desiredName = (String) objIn.readObject();
        Color desiredColor = (Color) objIn.readObject();

        String name = desiredName;
        Color color = desiredColor;

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

        Player player = new Player(name, color);
        game.addPlayer(player);
        System.out.println(game.getPlayers());

        /* Send back the player color and name */
        ObjectOutputStream objOut = objectOutputs.get(connections.indexOf(socket));
        System.out.println("Allowing " + player + " to join.");
        objOut.writeObject(name);
        objOut.writeObject(color);
        objOut.flush();
    }

    /**
     * Respond to a request to change a player's name
     */
    public void changeNameRequest(Socket socket, ObjectInputStream in) throws IOException, ClassNotFoundException {
        String newName = (String) in.readObject();
        Player player = game.getPlayers().get(connections.indexOf(socket));

        /* Check if desired name is taken */
        boolean success = false;
        if(!nameTaken(newName)){
            player.setName(newName);
            success = true;
        }

        if(success)
            objectOutputs.get(connections.indexOf(socket)).writeObject(ServerResponse.OperationSuccess);
        else
            objectOutputs.get(connections.indexOf(socket)).writeObject(ServerResponse.NameTaken);
    }

    /**
     * Respond to a request to change a player's color
     */
    public void changeColorRequest(Socket socket, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Color newColor = (Color) in.readObject();
        Player player = game.getPlayers().get(connections.indexOf(socket));

        /* Check if desired name is taken */
        boolean success = false;
        if(!colorTaken(newColor)){
            player.setColor(newColor);
            success = true;
        }

        if(success)
            objectOutputs.get(connections.indexOf(socket)).writeObject(ServerResponse.OperationSuccess);
        else
            objectOutputs.get(connections.indexOf(socket)).writeObject(ServerResponse.ColorTaken);
    }

    /**
     * Respond to a appended path request
     * @param socket Socket to get data from
     * @param in input stream to read from
     */
    public void pathAppendedRequest(Socket socket, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Integer id = (Integer) in.readObject();
        Direction dir = (Direction) in.readObject();
        pathAppendedRequest(id, dir);
    }

    /**
     * Respond to a appended path request
     * @param id unique unit id
     * @param d direction appended to path
     */
    public void pathAppendedRequest(Integer id, Direction d){
    }

    /**
     * Respond to a changed path request
     * @param socket Socket to get data from
     * @param in input stream to read from
     */
    public void pathChangedRequest(Socket socket, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Integer id = (Integer) in.readObject();
        Queue<Direction> dir = (Queue<Direction>) in.readObject();
        pathChangedRequest(id, dir);
    }
    /**
     * Respond to a path changed request
     * @param id unique unit id
     * @param queue new path
     */
    public void pathChangedRequest(Integer id, Queue<Direction> ds){

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
            for(ObjectInputStream in : objectInputs)
                in.close();
            for(ObjectOutputStream out : objectOutputs)
                out.close();
            for(Socket sock : connections)
                sock.close();

            objectOutputs.clear();
            objectInputs.clear();
            connections.clear();
        }
    }
}
