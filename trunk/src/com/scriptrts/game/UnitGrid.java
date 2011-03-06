package com.scriptrts.game;

import java.awt.Point;
import com.scriptrts.core.Minimap;

/**
 * Stores the locations of all the units on the map.
 */
public class UnitGrid {
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
	private SimpleUnit[][] unitGrid;
	
    /**
     * Create a new unit grid.
     * @param n size of the map.
     */
	public UnitGrid(int n) {
		this.n = n * SPACES_PER_TILE;
		unitGrid = new SimpleUnit[this.n][this.n];
	}

    /**
     * Attempt to move the unit onto its destination tile. 
     * @return true if movement successful, false if movement was stalled
     */
    public boolean moveUnitOneTile(SimpleUnit unit){
        Direction d = unit.getDirection();
        if(d == null) {
            Direction nextDirection = unit.peekNextDirection();
            if(canMove(unit, nextDirection))
                unit.updateDirection();
            else
                unit.clearPath();
            return false;
        }

        int delX = 0, delY = 0;
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

        /* Remove the unit from the map and place it's new center */
        removeUnit(unit);
        unit.setX(unit.getX() + delX);
        unit.setY(unit.getY() + delY);

        /* Check if the place where it's going is taken */
        Direction nextDirection = unit.peekNextDirection();
        if(canMove(unit, nextDirection)){
            /* Change the orientation of the unit */
            unit.updateDirection();

            /* Put a reservation on the tiles which the unit will occupy later */
            reserveNextUnitLocation(unit, unit.getDirection(), unit.peekNextDirection());
        }
        else {
            unit.setDirection(null);
            unit.clearPath();
        }

        /* Place the unit, in its new orientation, on the map */
        placeUnit(unit, unit.getX(), unit.getY());

        return true;
    }

    /**
     * Figure out whether the unit can move in the specified direction. This direction has to be along the unit's path.
     * @return true if the unit can move there, false otherwise;
     */
    private boolean canMove(SimpleUnit unit, Direction nextDirection){
        if(nextDirection == null)
            return true;

        /* Calculate location of next tile */
        int delX = 0, delY = 0;
        switch(nextDirection){
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

        Point[] newShape = unit.getShape(nextDirection);
        for(Point p : newShape)
            if(spaceTakenFor(unit.getX() + delX + p.x, unit.getY() + delY + p.y, unit))
                    return false;

        return true;
    }

    /**
     * Reserve a unit location for a given unit. This prevents units from attempting to move into that location.
     * @param unit the unit for which to reserve area
     * @param moving which direction the unit is moving in right now
     * @param turnAfterMove which direction the unit will move in after it finishes the current move
     */
    private void reserveNextUnitLocation(SimpleUnit unit, Direction moving, Direction turnAfterMove){
        /* Calculate location of next tile */
        int delX = 0, delY = 0;
        switch(moving){
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

        /* Reserve the location to which the unit will move */
        if(moving != null){
            /* Reserve locations */
            Point[] points = unit.getShape(moving);
            for(Point p : points)
                reserveLoc(unit, unit.getX() + p.x + delX, unit.getY() + p.y + delY);
        }

        /* Reserve the location to which the unit will turn after moving */
        if(turnAfterMove != null){
            /* Reserve locations */
            Point[] points = unit.getShape(turnAfterMove);
            for(Point p : points)
                reserveLoc(unit, unit.getX() + p.x + delX, unit.getY() + p.y + delY);
        }
    }

    /**
     * Reserve a specific spot in the unit grid.
     * @param unit the unit for which to reserve the spot
     * @param i x coordinate of spot
     * @param j y coordinate of spot
     */
    private void reserveLoc(SimpleUnit unit, int i, int j){
        unitGrid[i][j] = new ReserveUnit(unit);
    }

    /**
     * Return the unit at the given coordinates.
     * @param i x coordinate
     * @param j y coordinate
     * @return unit at specified coordinates
     */
    public SimpleUnit getUnit(int i, int j){
        if(unitGrid[i][j] instanceof ReserveUnit)
            return null;

        return unitGrid[i][j];
    }

    /**
     * Check whether the given unit can move to the specified spot.
     * @param i x coordinate of spot
     * @param j y coordinate of spot
     * @param unit the unit for which to check
     */
    public boolean spaceTakenFor(int i, int j, SimpleUnit unit){
        SimpleUnit u = unitGrid[i][j];
        if(u == null)
            return false;
        if(u == unit)
            return false;
        if(u instanceof ReserveUnit && ((ReserveUnit) u).getOriginal() == unit)
            return false;

        return true;
    }

    /**
     * Set a location in the unit grid to a given unit.
     * @param unit the unit 
     * @param i x coordinate of spot
     * @param j y coordinate of spot
     */
    private void setUnit(SimpleUnit unit, int i, int j){
        unitGrid[i][j] = unit;

        Minimap.updateMinimap();
    }

    /**
     * Place a unit in a given position. This moves the entire unit, not just the center.
     * @param unit the unit 
     * @param i x coordinate of spot
     * @param j y coordinate of spot
     */
    public void placeUnit(SimpleUnit unit, int i, int j){
        Point[] points = unit.getCurrentShape();
        unit.setX(i);
        unit.setY(j);

        for(Point p : points)
            setUnit(unit, unit.getX() + p.x, unit.getY() + p.y);
    }

    /**
     * Check if this spot is reserved.
     * @param i x coordinate of spot
     * @param j y coordinate of spot
     * @return whether this spot is reserved
     */
    public boolean reserved(int i, int j){
        return unitGrid[i][j] instanceof ReserveUnit;
    }

    /**
     * Remove a unit from a location. This removes the entire unit, not just the center.
     * @param unit the unit 
     */
    public void removeUnit(SimpleUnit unit){
        Point[] points = unit.getCurrentShape();
        for(Point p : points)
            setUnit(null, unit.getX() + p.x, unit.getY() + p.y);
    }
    
    /**
     * Get the neighbors of a point
	 * @return the neighbors of a given point
	 */
	public int[][] getNeighbors(int x, int y){
		int[][] neighbors;
		int count = 0;
		if(!this.contains(x,y))
			return null;
		if(x == 0 || x == n - 1)
			if(y == 0 || y == n - 1)
				neighbors = new int[3][2];
			else
				neighbors = new int[5][2];
		else if(y == 0 || y == n - 1)
			neighbors = new int[5][2];
		else
			neighbors = new int[8][2];
		for(int i = x - 1; i <= x + 1; i++)
			for(int j = y - 1; j <= y + 1; j++){
				if(this.contains(i,j) && !(x == i && y == j)){
					neighbors[count][0] = i;
					neighbors[count][1] = j;
					count++;
				}
			}
		return neighbors;
	}
    
	/**
     * Check whether the unit grid contains a given point
	 * @return if the unit grid contains a given point
	 */
	
	public boolean contains(int x, int y){
		if(0 <= x && x <= n-1 && 0 <= y && y <= n-1)
			return true;
		return false;
	}
    
    /**
     * Get the map tile corresponding to the unit tile
     */
    public int[] getMapTile(int x, int y){
    	int[] mapTile = {x / SPACES_PER_TILE, y / SPACES_PER_TILE};
    	return mapTile;
    }
}

/**
 * "Fake" unit used by the UnitGrid to reserve spots in the grid.
 */
class ReserveUnit extends SimpleUnit {
    /**
     * Which unit this spot is reserved for.
     */
    private SimpleUnit reserve;

    /**
     * Create a new reserve unit.
     * @param  reserveFor which unit to reserve the spot for.
     */
    public ReserveUnit(SimpleUnit reserveFor){
        super(reserveFor.getAllegiance(), null, null, 0, 0, 0, null);
        reserve = reserveFor;
    }

    /**
     * Check whether the specified unit can pass over this reserve unit.
     * @param u unit which wants to pass over this unit
     * @return whether the unit can pass the reserved spot
     */
    public boolean isPassable(SimpleUnit u){
        return reserve.isPassable(u);
    }

    /**
     * Get which unit this is reserving for.
     * @return the unit which is going to this destination
     */
    public SimpleUnit getOriginal(){
        return reserve;
    }
}
