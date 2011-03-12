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
        List<SimpleUnit> updated = updatedUnits;
        return updated;
    }

    /**
     * Update a single unit with info from the given unit object
     */
    public void updateUnit(SimpleUnit unit){
        UnitGrid grid = game.getUnitGrid();

        /* If this is a new unit, remove it first */
        if(allUnits.containsKey(unit.getID())){
            SimpleUnit prevUnit = allUnits.get(unit.getID());
            grid.removeUnit(prevUnit);
            prevUnit.setParameters(unit);
        }
        else  {
            try {
                /* Retrieve spaceship sprites */
                BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
                Sprite[] sprites = new Sprite[16];
                for(Direction d : Direction.values()){
                    String unitDir = "resource/unit/spaceship/";
                    String unitFilename = "Ship" + d.name() + ".png";
                    BufferedImage img = ResourceManager.loadBandedImage(
                            unitDir + unitFilename, unitDir + "allegiance/" + unitFilename, ((Game) game).getPlayer().getColor());
                    sprites[d.ordinal()]  = new Sprite(img, 0.3, 87, 25);
                }

                for(Direction d : Direction.values()){
                    String unitDir = "resource/unit/spaceship/";
                    String unitFilename = "Ship" + d.name() + ".png";
                    BufferedImage normalImg = ResourceManager.loadBandedImage(
                            unitDir + unitFilename, unitDir + "allegiance/" + unitFilename, ((Game) game).getPlayer().getColor());
                    BufferedImage attackImg = ResourceManager.loadBandedImage(
                            unitDir + "attack/" + unitFilename, unitDir + "allegiance/" + unitFilename, ((Game) game).getPlayer().getColor());
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

                SimpleUnit spaceship = new SimpleUnit(null, sprites, art, 5, 0, 0, Direction.East, true, null);
                spaceship.setParameters(unit);
                allUnits.put(unit.getID(), spaceship);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /* Add it to the map */
        unit = allUnits.get(unit.getID());
        grid.placeUnit(unit, unit.getX(), unit.getY());

    }

    /**
     * Clear updated units
     */
    public void clearUpdates(){
        updatedUnits.clear();
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
