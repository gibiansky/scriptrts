package com.scriptrts.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.scriptrts.game.HeadlessGame;
import com.scriptrts.game.MapObjectGrid;
import com.scriptrts.game.Unit;


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
    private HashMap<Integer, Unit> allUnits = new HashMap<Integer, Unit>();

    /**
     * All updated units
     */
    private List<Unit> updatedUnits = new ArrayList<Unit>(100);

    /**
     * All new units
     */
    private List<Unit> newUnits = new ArrayList<Unit>(100);

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
    public void addUnit(Unit u){
        allUnits.put(u.getID(), u);
        newUnits.add(u);
    }

    /**
     * Mark a unit as updated
     */
    private void setUnitUpdated(Unit unit){
        updatedUnits.add(unit);
    }

    /**
     * Get all updated units
     * @return a list of updated units
     */
    public List<Unit> updatedUnits(){
        return updatedUnits;
    }

    /**
     * Get all units
     * @return a collection of all units
     */
    public Collection<Unit> allUnits(){
        return allUnits.values();
    }

    /**
     * Get the unit with a given ID
     * @param id unit id
     * @return unit with the specified ID
     */
    public Unit unitWithID(int id){
        return allUnits.get(id);
    }

    /**
     * Get all new units
     * @return a list of new units
     */
    public List<Unit> newUnits(){
        return newUnits;
    }

    /**
     * Update a single unit with info from the given unit object
     */
    public void synchronizeUnit(Unit unit){
        MapObjectGrid grid = game.getUnitGrid();

        /* Get the unit that was on the map, remove it so we can make updates */
        Unit prevUnit = allUnits.get(unit.getID());
        grid.removeMapObject(prevUnit.getMapObject());

        /* Synchronize the unit with the server's version (including position) */
        prevUnit.setParameters(unit);

        /* Re-add the now-synchronized unit (at the possibly updated position) */
        grid.placeMapObject(prevUnit.getMapObject(), prevUnit.getX(), prevUnit.getY());
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
        MapObjectGrid grid = game.getUnitGrid();
        int n = game.getCurrentMap().getN() * MapObjectGrid.SPACES_PER_TILE;

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
    private void updateUnit(int i, int j, MapObjectGrid grid){
        /* Get fps */
        int fps = Main.getFPS();

        Unit unit = grid.getUnit(i, j);

        boolean needsVisibilityUpdate = false;

        /* Unit speed is in subtiles per second */
        int uSpeed = unit.getSpeed();

        /* For a moving unit, move it */
        if(unit.getSpeed() != 0 && unit.getDirection() != null) {
            double subtilesMovedPerFrame = (double)(uSpeed) /* subtiles per second */ / fps /* times seconds */;

            int tilesMoved = unit.getMapObject().incrementAnimationCounter(subtilesMovedPerFrame);

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
        else if(unit.getSpeed() > 0 && unit.getDirection() == null){
            grid.moveUnitOneTile(unit);
        }

        /* Retrieve the visibility grid */
        byte[][] vGrid = Main.getGame().getPlayer().getVisibilityGrid();

        /* If the unit moved, set its new position to visible */
        if(needsVisibilityUpdate)  
            vGrid[unit.getX()][unit.getY()] = 2; 

        unit.getMapObject().progressSpriteAnimation();
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
