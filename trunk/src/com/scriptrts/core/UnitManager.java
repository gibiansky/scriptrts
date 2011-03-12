package com.scriptrts.core;

import java.util.*;

import com.scriptrts.game.SimpleUnit;
import com.scriptrts.game.UnitGrid;

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
    private HashMap<Integer, SimpleUnit> allUnits;

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
            }
        }

        unit.progressSpriteAnimation();
    }

}