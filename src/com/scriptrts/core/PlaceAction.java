package com.scriptrts.core;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import com.scriptrts.core.ui.Viewport;
import com.scriptrts.game.GameObject;

/**
 * A click action to place a unit on the map
 */
public class PlaceAction extends ClickAction {
    /**
     * Unit to place on the map
     */
    private GameObject unit = null;

    /**
     * Create a new place action to put a given unit on the map
     * @param placing unit to place on the map
     */
    public PlaceAction(GameObject placing){
        super();
        unit = placing;
    }

    /**
     * Perform the click action
     * @param x mouse coordinate x
     * @param y mouse coordinate y
     */
    public boolean click(int x, int y){
        Viewport viewport = Main.getGame().getViewport();
        Point point = new Point(x, y);
        Point unitTile = Main.getGame().getUnitPainter().unitTileAtPoint(point, viewport);
        /* If space is taken, don't do anything */
        if(!Main.getGame().getGameGrid().canPlaceUnit(unit, unitTile.x, unitTile.y, unit.getFacingDirection()))
        	return false;
        unit.getUnit().setX(unitTile.x);
        unit.getUnit().setY(unitTile.y);
        Main.getGame().getGameGrid().placeUnit(unit, unitTile.x, unitTile.y);
        Main.getGame().getGameManager().addUnit(unit);
        Main.getGame().getGameManager().setVisibleTiles(unit, unit.getUnit().getX(), unit.getUnit().getY());

        if(Main.getGameClient() != null)
            Main.getGameClient().sendNewUnitNotification(unit);
        return true;
    }

    /**
     * Returns true because the place action draws the unit it's placing
     */
    public boolean hasCursor() {
        return true;
    }

    /**
     * Draw the cursor (the unit being placed) on the screen
     */
    public void paintCursor(Graphics graphics, Viewport viewport, int x, int y){
        Main.getGame().getUnitPainter().paintTemporaryUnit((Graphics2D) graphics, viewport, unit, x, y);
    }
    
    public GameObject getUnit(){
    	return unit;
    }

}
