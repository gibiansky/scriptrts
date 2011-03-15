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
     * Get all new units
     * @return a list of new units
     */
    public List<SimpleUnit> newUnits(){
        return newUnits;
    }

    /**
     * Update a single unit with info from the given unit object
     */
    public void updateUnit(SimpleUnit unit){
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

        /* Unit speed is in subtiles per second */
        int uSpeed = unit.getSpeed();
        double subtilesMovedPerFrame = (double)(uSpeed) /* subtiles per second */ / fps /* times seconds */;

        int tilesMoved = unit.incrementAnimationCounter(subtilesMovedPerFrame);

        /* Move it however many tiles it wants to be moved if this is the server;
         * the client will change positions when the server sends updated data. */
        if(Main.getGameServer() != null){
            while(tilesMoved > 0){
                boolean moveSucceeded = grid.moveUnitOneTile(unit);
                tilesMoved--;

                setUnitUpdated(unit);
            }
        }

        unit.progressSpriteAnimation();
        
        unit.getOrderHandler().update();
    }

}
