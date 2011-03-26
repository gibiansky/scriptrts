package com.scriptrts.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.scriptrts.core.Main;


/**
 * Manages and update units and their positions and statistics
 */
public class GameManager {
    /**
     * Current running game instance
     */
    private HeadlessGame game;

    /**
     * Hashed list of all units, hashed by ID
     */
    private HashMap<Integer, GameObject> allUnits = new HashMap<Integer, GameObject>();

    /**
     * All updated units
     */
    private List<GameObject> updatedUnits = new ArrayList<GameObject>(100);

    /**
     * All new units
     */
    private List<GameObject> newUnits = new ArrayList<GameObject>(100);

    /**
     * Create a new unit manager
     * @param g game instance for which units are being managed
     */
    public GameManager(HeadlessGame g){
        super();
        this.game = g;
    }

    /**
     * Add a unit to the manager
     * @param u unit to add
     */
    public void addUnit(GameObject u){
        allUnits.put(u.getID(), u);
        newUnits.add(u);
    }

    /**
     * Mark a unit as updated
     */
    private void setUnitUpdated(GameObject unit){
        updatedUnits.add(unit);
    }

    /**
     * Get all updated units
     * @return a list of updated units
     */
    public List<GameObject> updatedUnits(){
        return updatedUnits;
    }

    /**
     * Get all units
     * @return a collection of all units
     */
    public Collection<GameObject> allUnits(){
        return allUnits.values();
    }

    /**
     * Get the unit with a given ID
     * @param id unit id
     * @return unit with the specified ID
     */
    public GameObject unitWithId(int id){
        return allUnits.get(id);
    }

    /**
     * Get all new units
     * @return a list of new units
     */
    public List<GameObject> newUnits(){
        return newUnits;
    }

    /**
     * Update a single unit with info from the given unit object
     */
    public void synchronizeUnit(GameObject unit){
        MapGrid grid = game.getGameGrid();

        /* Get the unit that was on the map, remove it so we can make updates */
        GameObject prevUnit = allUnits.get(unit.getID());
        grid.removeUnit(prevUnit);

        /* Synchronize the unit with the server's version (including position) */
        prevUnit.setParameters(unit);

        /* Re-add the now-synchronized unit (at the possibly updated position) */
        grid.placeUnit(prevUnit, prevUnit.getUnit().getX(), prevUnit.getUnit().getY());

        System.out.println("Updated Direction: " + unit.getDirection());
    }

    /**
     * Clear updated units
     */
    public void clearUpdates(){
        updatedUnits.clear();
        newUnits.clear();
    }


    /**
     * Updates unit positions and animations
     */
    public void update(){
        MapGrid grid = game.getGameGrid();
        int n = game.getCurrentMap().getN() * MapGrid.SPACES_PER_TILE;

        

        /* Loop over all unit positions, update where there are units */
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                if(grid.getUnit(i, j) != null && grid.getUnit(i, j).getUnit().getX() == i && grid.getUnit(i, j).getUnit().getY() == j)
                    updateUnit(i, j, grid);
            }
        }
    }

    /**
     * Update the unit at the given location 
     * @param i x coordinate in unit grid
     * @param j y coordinate in unit grid
     */
    private void updateUnit(int i, int j, MapGrid grid){
        /* Get fps */
        int fps = Main.getFPS();

        GameObject unit = grid.getUnit(i, j);

        boolean needsVisibilityUpdate = false;

        /* Unit speed is in subtiles per second */
        int uSpeed = unit.getUnit().getSpeed();

        int prevX = unit.getUnit().getX(), prevY = unit.getUnit().getY();
        
        /* For a moving unit, move it */
        if(unit.getUnit().getSpeed() != 0 && unit.getDirection() != null) {
            double subtilesMovedPerFrame = (double)(uSpeed) /* subtiles per second */ / fps /* times seconds */;
            
            int tilesMoved = unit.incrementAnimationCounter(subtilesMovedPerFrame);

            needsVisibilityUpdate = tilesMoved > 0;

            /* Move it however many tiles it wants to be moved if this is the server;
             * the client will change positions when the server sends updated data. */
            if(Main.getGameServer() != null){
                while(tilesMoved > 0){
                    grid.moveUnitOneTile(unit);
                    tilesMoved--;
                    setUnitUpdated(unit);
                }
            }
        }

        /* For a unit which could move, but is standing still, update it every time in case it wants an update */
        else if(unit.getUnit().getSpeed() > 0 && unit.getDirection() == null){
            grid.moveUnitOneTile(unit);
        }

        /* If the unit moved, set its new position to visible */
        if(needsVisibilityUpdate)
        	setVisibleTiles(unit, prevX, prevY);

        unit.progressSpriteAnimation();
        unit.getUnit().getOrderHandler().update();
    }
    
    public void setVisibleTiles(GameObject unit, int prevX, int prevY){
    	/* Retrieve the visibility grid */
        byte[][] vGrid = Main.getGame().getPlayer().getVisibilityGrid();
        for(Point p : unit.getShape(unit.getFacingDirection())){
        	for(Point tile : unit.getUnit().getVisibleTiles(prevX + p.x, prevY + p.y)){
        		if(tile != null)
        			vGrid[tile.x][tile.y] = 1;
        	}
        	for(Point tile : unit.getUnit().getVisibleTiles(unit.getUnit().getX() + p.x, unit.getUnit().getY() + p.y)){
        		if(tile != null)
        			vGrid[tile.x][tile.y] = 2;
        	}
        }
    }
}
