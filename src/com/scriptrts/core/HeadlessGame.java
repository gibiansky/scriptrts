package com.scriptrts.core;

import java.util.ArrayList;
import java.util.List;
import com.scriptrts.game.Player;
import com.scriptrts.game.UnitGrid;
import com.scriptrts.game.SimpleUnit;
import com.scriptrts.core.Map;

/**
 * Headless game run by the servers and extended by the graphical game
 */
public class HeadlessGame {
    /**
     * The map on which this game is being played
     */
    protected Map map;

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
    protected UnitGrid grid;

    /**
     * Unit manager to move and update units
     */
    protected UnitManager unitManager;

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
        /* Create and populate map with tiles */
        map = new Map(n);
        map.generateMap(.64);

        /* Create the grid for units */
        grid = new UnitGrid(n);
        
        /* Create the unit manager */
        unitManager = new UnitManager(this);

        /* Create path handler */
        pathHandler = new PathHandler(map, grid);
        /* Set number of pathfinders used */
        pathHandler.setNumPathfinders(10);
    }

    /**
     * Update the game state
     */
    public void update(){
        /* Update all units */
        unitManager.update();
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
    public Map getCurrentMap(){
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
    public UnitGrid getUnitGrid(){
        return grid;
    }

    /**
     * Get the unit manager
     * @return current unit manager
     */
    public UnitManager getUnitManager(){
        return unitManager;
    }
}
