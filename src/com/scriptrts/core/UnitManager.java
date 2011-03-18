package com.scriptrts.core;

import java.util.*;
import java.awt.image.BufferedImage;

import com.scriptrts.game.SimpleUnit;
import com.scriptrts.game.UnitGrid;
import com.scriptrts.control.MoveOrder;
import com.scriptrts.control.Selection;
import com.scriptrts.control.SelectionStorage;
import com.scriptrts.game.Direction;
import com.scriptrts.game.SimpleUnit;
import com.scriptrts.game.Player;
import com.scriptrts.game.Sprite;
import com.scriptrts.game.AnimatedSprite;
import com.scriptrts.game.UnitGrid;
import com.scriptrts.util.ResourceManager;


/**
 * Manages and update units and their positions and statistics
 */
public class UnitManager {
    /**
     * Current running game instance
     */
    private HeadlessGame game;

    /**
     * Hashed list of all units, hashed by ID
     */
    private HashMap<Integer, SimpleUnit> allUnits = new HashMap<Integer, SimpleUnit>();

    /**
     * All updated units
     */
    private List<SimpleUnit> updatedUnits = new ArrayList<SimpleUnit>(100);

    /**
     * All new units
     */
    private List<SimpleUnit> newUnits = new ArrayList<SimpleUnit>(100);

    /**
     * Create a new unit manager
     * @param g game instance for which units are being managed
     */
    public UnitManager(HeadlessGame g){
        super();
        this.game = g;
    }

    /**
     * Add a unit to the manager
     * @param u unit to add
     */
    public void addUnit(SimpleUnit u){
        allUnits.put(u.getID(), u);
        newUnits.add(u);
    }

    /**
     * Mark a unit as updated
     */
    private void setUnitUpdated(SimpleUnit unit){
        updatedUnits.add(unit);
    }

    /**
     * Get all updated units
     * @return a list of updated units
     */
    public List<SimpleUnit> updatedUnits(){
        return updatedUnits;
    }

    /**
     * Get all units
     * @return a collection of all units
     */
    public Collection<SimpleUnit> allUnits(){
        return allUnits.values();
    }

    /**
     * Get the unit with a given ID
     * @param id unit id
     * @return unit with the specified ID
     */
    public SimpleUnit unitWithID(int id){
        return allUnits.get(id);
    }

    /**
     * Get all new units
     * @return a list of new units
     */
    public List<SimpleUnit> newUnits(){
        return newUnits;
    }

    /**
     * Update a single unit with info from the given unit object
     */
    public void synchronizeUnit(SimpleUnit unit){
        UnitGrid grid = game.getUnitGrid();

        /* Get the unit that was on the map, remove it so we can make updates */
        SimpleUnit prevUnit = allUnits.get(unit.getID());
        grid.removeUnit(prevUnit);

        /* Synchronize the unit with the server's version (including position) */
        prevUnit.setParameters(unit);

        /* Re-add the now-synchronized unit (at the possibly updated position) */
        grid.placeUnit(prevUnit, prevUnit.getX(), prevUnit.getY());
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
        UnitGrid grid = game.getUnitGrid();
        int n = game.getCurrentMap().getN() * UnitGrid.SPACES_PER_TILE;

        /* Check if any movement is occurring this update */
        boolean needsReset = false;
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                if(grid.getUnit(i, j) != null && grid.getUnit(i, j).getX() == i && grid.getUnit(i, j).getY() == j)  
                    if(grid.getUnit(i, j).getSpeed() != 0 && grid.getUnit(i, j).getDirection() != null)  
                        needsReset = true;
            }
        }

        /* If movement is occurring, reset the visibility grid */
        if(needsReset)
            resetVisibilityGrid();

        /* Loop over all unit positions, update where there are units */
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                if(grid.getUnit(i, j) != null && grid.getUnit(i, j).getX() == i && grid.getUnit(i, j).getY() == j)
                    updateUnit(i, j, grid);
            }
        }
    }

    /**
     * Update the unit at the given location 
     * @param i x coordinate in unit grid
     * @param j y coordinate in unit grid
     */
    private void updateUnit(int i, int j, UnitGrid grid){
        /* Get fps */
        int fps = Main.getFPS();

        SimpleUnit unit = grid.getUnit(i, j);

        boolean needsVisibilityUpdate = false;

        /* Unit speed is in subtiles per second */
        int uSpeed = unit.getSpeed();
        if(unit.getSpeed() != 0 && unit.getDirection() != null) {
            double subtilesMovedPerFrame = (double)(uSpeed) /* subtiles per second */ / fps /* times seconds */;

            int tilesMoved = unit.incrementAnimationCounter(subtilesMovedPerFrame);

            needsVisibilityUpdate = tilesMoved > 0;

            /* Move it however many tiles it wants to be moved if this is the server;
             * the client will change positions when the server sends updated data. */
            if(Main.getGameServer() != null){
                while(tilesMoved > 0){
                    boolean moveSucceeded = grid.moveUnitOneTile(unit);
                    tilesMoved--;

                    setUnitUpdated(unit);
                }
            }
        }

        /* Retrieve the visibility grid */
        byte[][] vGrid = Main.getGame().getPlayer().getVisibilityGrid();

        /* If the unit moved, set its new position to visible */
        if(needsVisibilityUpdate)  
            vGrid[unit.getX()][unit.getY()] = 2; 

        unit.progressSpriteAnimation();
        unit.getOrderHandler().update();
    }

    /**
     * Resets the visibility grid for the player before each update
     */
    private void resetVisibilityGrid() {
        byte[][] grid = Main.getGame().getPlayer().getVisibilityGrid();
        for(int i = 0; i < grid.length; i++)
            for(int j = 0; j < grid[i].length; j++)
                if(grid[i][j] == 2)
                    grid[i][j] = 1;
    }
}
