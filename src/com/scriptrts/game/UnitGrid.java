package com.scriptrts.game;

import java.awt.Point;

public class UnitGrid {
	/** 
	 * How many of the smallest unit can fit along one side of each map tile
	 */
    public final static int SPACES_PER_TILE = 3;

	private int n;
	private SimpleUnit[][] unitGrid;
	
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
            unit.updateDirection();
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

        removeUnit(unit);
        unit.setX(unit.getX() + delX);
        unit.setY(unit.getY() + delY);
        placeUnit(unit, unit.getX(), unit.getY());

        /* Check if the place where it's going is taken */
        Direction nextDirection = unit.peekNextDirection();
        if(canMove(unit, nextDirection)){
            /* Change the orientation of the unit */
            removeUnit(unit);
            unit.updateDirection();
            placeUnit(unit, unit.getX(), unit.getY());
        }
        else
            unit.setDirection(null);

        return true;
    }

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
            if(spaceTaken(unit.getX() + delX + p.x, unit.getY() + delY + p.y) 
                    && getUnit(unit.getX() + delX + p.x, unit.getY() + delY + p.y) != unit)
                    return false;

        return true;
    }

    /**
     * Reserve a unit location for a given unit. This prevents units from attempting to move into that location.
     */
    private void reserveUnit(int i, int j, SimpleUnit u){
        /*
        Point[] points = unit.getCurrentShape();
        unit.setX(i);
        unit.setY(j);

        for(Point p : points)
            setUnit(unit, unit.getX() + p.x, unit.getY() + p.y);
        setUnit(u, i, j);
        */
    }

    /**
     * Remove any reservations on this unit spot.
     */
    private void unreserveUnit(int i, int j){
        if(unitGrid[i][j] instanceof ReserveUnit)
            unitGrid[i][j] = null;
    }

    public SimpleUnit getUnit(int i, int j){
        if(unitGrid[i][j] instanceof ReserveUnit)
            return null;

        return unitGrid[i][j];
    }

    public boolean spaceTaken(int i, int j){
        return unitGrid[i][j] != null;
    }

    private void setUnit(SimpleUnit unit, int i, int j){
        unitGrid[i][j] = unit;
    }

    public void placeUnit(SimpleUnit unit, int i, int j){
        Point[] points = unit.getCurrentShape();
        unit.setX(i);
        unit.setY(j);

        for(Point p : points)
            setUnit(unit, unit.getX() + p.x, unit.getY() + p.y);
    }

    public void removeUnit(SimpleUnit unit){
        Point[] points = unit.getCurrentShape();
        for(Point p : points)
            setUnit(null, unit.getX() + p.x, unit.getY() + p.y);
    }
}

class ReserveUnit extends SimpleUnit {
    private SimpleUnit reserve;

    public ReserveUnit(SimpleUnit reserveFor){
        super(null, 0, 0, 0, null);
        reserve = reserveFor;
    }

    public boolean isPassable(SimpleUnit u){
        return reserve.isPassable(u);
    }
}
