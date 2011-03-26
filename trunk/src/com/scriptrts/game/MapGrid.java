package com.scriptrts.game;

import java.awt.Point;
import java.util.Queue;

import com.scriptrts.core.Main;
import com.scriptrts.core.ui.Minimap;

/**
 * Stores the locations of all the units on the map.
 */
public class MapGrid {
    /** 
     * How many of the smallest unit can fit along one side of each map tile
     */
    public final static int SPACES_PER_TILE = 3;

    /**
     * Size of the map.
     */
    private int n;

    /**
     * Array of units representing the map.
     */
    private GameObject[][] grid;

    /**
     * Array holding reservations: if null, this tile is unreserved; if non-null, the tile
     * is reserved for the unit which is in the tile location.
     */
    private GameObject[][] reserved;

    /**
     * Create a new unit grid.
     * @param n size of the map.
     */
    public MapGrid(int n) {
        this.n = n * SPACES_PER_TILE;
        grid = new GameObject[this.n][this.n];
        reserved = new GameObject[this.n][this.n];
    }

    /**
     * Attempt to move the unit onto its destination tile. 
     * @return true if movement successful, false if movement was stalled
     */
    public void moveUnitOneTile(GameObject unit){
        /* Get the direction the unit is currently moving in, the direction in which 
         * it should move after this method is finished, and the direction it will turn
         * in after the next move is finished
         */
        Direction current = unit.getDirection();
        Direction next = unit.getNextDirection();

        /* Get the offsets for each of the directions retrieved above */
        Point currentOffset = getDirectionOffset(current);
        Point nextOffset = getDirectionOffset(next);

        /* If the unit is stationary and isn't going to start moving */
        if(current == null && next == null){
            /* Just update the direction so that on the next call, this unit might start moving */
            unit.updateDirection();
            return;
        }

        /* If the unit is moving, move it */
        if(current != null){
            removeUnit(unit);
            unit.getUnit().setX(unit.getUnit().getX() + currentOffset.x);
            unit.getUnit().setY(unit.getUnit().getY() + currentOffset.y);
            placeUnit(unit);
        }

        /* If the unit wants to move */
        if(next != null) {
            /* Check if the unit can:
             *     turn in the direction it wants to move in AND
             *     move in the direction it wants to move in (after turning)
             */
            if(canPlaceUnit(unit, unit.getUnit().getX(), unit.getUnit().getY(), next) && 
            		canPlaceUnit(unit, unit.getUnit().getX() + nextOffset.x, unit.getUnit().getY() + nextOffset.y, next)){
                placeReservation(unit, unit.getUnit().getX(), unit.getUnit().getY(), next);

                /* Re-orient the unit in its new facing direction */
                removeUnit(unit);
                unit.updateDirection();
                placeUnit(unit);
            }
            else
                stopUnit(unit);
        } else {
            unit.updateDirection();
        }
    }

    /**
     * Find the offset in the given direction on the unit grid
     * @param d direction in which to move
     * @return point with x, y offsets
     */
    private Point getDirectionOffset(Direction d){
        int delX = 0, delY = 0;
        if(d != null)
            switch(d){
                case North:
                    delY = 1; 
                    break;
                case Northeast:
                    delX = delY = 1; 
                    break;
                case East:
                    delX = 1; 
                    break;
                case Southeast:
                    delX = 1;
                    delY = -1; 
                    break;
                case South:
                    delY = -1;
                    break;
                case Southwest:
                    delY = -1;
                    delX = -1;
                    break;
                case West:
                    delX = -1; 
                    break;
                case Northwest:
                default:
                    delX = -1;
                    delY = 1;
                    break;
            }
        return new Point(delX, delY);
    }

    /**
     * Return the unit at the given coordinates.
     * @param i x coordinate
     * @param j y coordinate
     * @return unit at specified coordinates
     */
    public GameObject getUnit(int i, int j){
        return grid[i][j];
    }

    /**
     * Check whether the given unit can move to the specified spot.
     * @param i x coordinate of spot
     * @param j y coordinate of spot
     * @param unit the unit for which to check. If this is null, then it checks whether this spot is taken for any unit.
     */
    public boolean spaceTakenFor(int i, int j, GameObject unit){
        /* If the unit for which we're checking is null, then just check if this space is taken at all */
        if(unit == null)
            return reserved[i][j] != null;

        boolean empty = (reserved[i][j] == null);
        if(empty)
            return false;
        else if(reserved[i][j] == unit)
            return false;
        else
            return true;
    }

    /**
     * Place a unit in a given position. This moves the entire unit, not just the center.
     * @param unit the unit 
     * @param i x coordinate of spot
     * @param j y coordinate of spot
     */
    public void placeUnit(GameObject unit, int i, int j){
        unit.getUnit().setX(i);
        unit.getUnit().setY(j);
        placeUnit(unit);
    }


    /**
     * Place a unit in its position
     * @param unit the unit 
     */
    public void placeUnit(GameObject unit){
        Point[] points = unit.getCurrentShape();

        for(Point p : points)
            setUnit(unit, unit.getUnit().getX() + p.x, unit.getUnit().getY() + p.y);
    }

    /**
     * Remove a unit from a location. This removes the entire unit, not just the center.
     * @param unit the unit 
     */
    public void removeUnit(GameObject unit){
        Point[] points = unit.getCurrentShape();
        for(Point p : points)
            setUnit(null, unit.getUnit().getX() + p.x, unit.getUnit().getY() + p.y);
    }

    /**
     * Get the neighbors of a point
     * @return the neighbors of a given point
     */
    public Point[] getNeighbors(int x, int y){
        Point[] neighbors;
        int count = 0;
        if(!this.contains(x,y))
            return null;
        if(x == 0 || x == n - 1)
            if(y == 0 || y == n - 1)
                neighbors = new Point[3];
            else
                neighbors = new Point[5];
        else if(y == 0 || y == n - 1)
            neighbors = new Point[5];
        else
            neighbors = new Point[8];
        for(int i = x - 1; i <= x + 1; i++)
            for(int j = y - 1; j <= y + 1; j++){
                if(this.contains(i,j) && !(x == i && y == j)){
                    neighbors[count] = new Point(i,j);
                    count++;
                }
            }
        return neighbors;
    }

    /**
     * Get the map tile corresponding to the unit tile
     */
    public int[] getMapTile(int x, int y){
        int[] mapTile = {x / SPACES_PER_TILE, y / SPACES_PER_TILE};
        return mapTile;
    }

    /**
     * Tell the unit grid that the path of this unit has changed.
     * @param unit unit for which path has changed
     * @param previousPath previous path this unit had
     */
    public void unitPathChanged(GameObject unit, Queue<Direction> newPath){
        /* Clear all reservations */
        for(int i = 0; i < reserved.length; i++)
            for(int j = 0; j < reserved[i].length; j++)
                if(reserved[i][j] == unit){
                    reserved[i][j] = null;
                    grid[i][j] = null;
                }

        /* Reserve where the unit is */
        placeUnit(unit);
    }


    /**
     * Set a location in the unit grid to a given unit.
     * @param unit the unit 
     * @param i x coordinate of spot
     * @param j y coordinate of spot
     */
    private void setUnit(GameObject unit, int i, int j){
        grid[i][j] = unit;
        Minimap.updateMinimap();

        /* Reserve or unreserve this location (depending on whether unit == null) */
        reserved[i][j] = unit;
    }

    /**
     * Whether a point is contained in the unit grid
     * @param x x coordinate of point
     * @param y y coordinate of point
     * @return whether the point is on the unit grid
     */
    private boolean contains(int x, int y){
        return (x >= 0 && x < n && y >= 0 && y < n);
    }

    /**
     * Check whether we can place a unit in a location.
     * @param unit unit to place
     * @param x x coordinate of location
     * @param y y coordinate of location
     * @param orientation orientation of the unit
     * @return whether we can place the unit there without interfering with other units
     */
    public boolean canPlaceUnit(GameObject unit, int x, int y, Direction orientation){
        Point[] points = unit.getShape(orientation);

        for(Point p : points){
        	try{
        		if(spaceTakenFor(x + p.x, y + p.y, unit))
        			return false;
        	}
        	
        	//If it goes off the map, return false
        	catch(ArrayIndexOutOfBoundsException e){
        		return false;
        	}
        }

        return true;
    }

    /**
     * Reserve the future location of this unit.
     * @param unit unit for which to make reservation
     * @param x x coordinate of current unit location
     * @param y y coordinate of current unit location
     * @param direction direction in which unit will move (and orientation of unit)
     */
    private void placeReservation(GameObject unit, int x, int y, Direction direction){
        Point[] points = unit.getShape(direction);
        Point offset = getDirectionOffset(direction);

        for(Point p : points){
            int resX = x + p.x + offset.x;
            int resY = y + p.y + offset.y;
            if(reserved[resX][resY] == null)
                reserved[resX][resY] = unit;
        }
    }

    /**
     * Stop the unit and reroute it if necessary.
     * @param unit unit to stop
     */
    private void stopUnit(GameObject unit){
        unit.setDirection(null);
        unit.clearPath();
        unit.setState(SpriteState.Attack);
    }
}
