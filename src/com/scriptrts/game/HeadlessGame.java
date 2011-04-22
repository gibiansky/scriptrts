package com.scriptrts.game;

import java.util.ArrayList;
import java.util.List;

import com.scriptrts.game.path.PathHandler;

/**
 * Headless game run by the servers and extended by the graphical game
 */
public class HeadlessGame {
    /**
     * The map on which this game is being played
     */
    protected GameMap map;

    /**
     * PathHandler used for all units
     */
    protected PathHandler pathHandler;

    /**
     * Size of the map (length along one edge). This is measured in map tiles, not unit tiles.
     * Each map tile has some number of unit tiles in it (3 as of last check).
     */
    protected int n; 

    /**
     * Grid for units to be on
     */
    protected MapGrid grid;

    /**
     * Unit manager to move and update units
     */
    protected GameManager gameManager;

    /**
     * All players currently connected
     */
    private ArrayList<Player> players = new ArrayList<Player>();

    /**
     * Create a new game
     * @param n size of the map
     */
    public HeadlessGame(int n){
        super();

        this.n = n;
    }

    /**
     * Initialize the game
     */
    public void init(){
    	/* Create the grid for units */
        grid = new MapGrid(n);
        
        /* Create the game manager */
        gameManager = new GameManager(this);
    	
        /* Create and populate map with tiles */
        map = new GameMap(n);
        map.generateMap(.64, 2);

        /* Create path handler */
        pathHandler = new PathHandler();
        /* Set number of pathfinders used */
        pathHandler.setNumPathfinders(10);
    }

    /**
     * Update the game state
     */
    public void update(){
        /* Update all units */
        gameManager.update();
    }

    /**
     * Add a player to the game
     */
    public void addPlayer(Player player){
        players.add(player);
    }

    /**
     * Get all players connected to this game
     */
    public List<Player> getPlayers(){
        return players;
    }

    /**
     * Get the map.
     * @return the current map
     */
    public GameMap getCurrentMap(){
        return map;
    }

    /**
     * Return the global path handler
     * @return the path handler
     */
    public PathHandler getPathHandler(){
        return pathHandler;
    }

    /**
     * Get the unit grid.
     * @return current unit grid
     */
    public MapGrid getGameGrid(){
        return grid;
    }

    /**
     * Get the unit manager
     * @return current unit manager
     */
    public GameManager getGameManager(){
        return gameManager;
    }
}
