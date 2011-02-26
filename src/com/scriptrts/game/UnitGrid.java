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

        /* Prevent units from colliding and annihilating each other */
        if(getUnit(unit.getX() + delX, unit.getY() + delY) != null && getUnit(unit.getX() + delX, unit.getY() + delY) != unit){
            return false;
        }

        setUnit(null, unit.getX(), unit.getY());
        unit.setX(unit.getX() + delX);
        unit.setY(unit.getY() + delY);
        setUnit(unit, unit.getX(), unit.getY());

        /* Check if the place where it's going is taken */
        Direction nextDirection = unit.peekNextDirection();
        if(canMove(unit, nextDirection))
                unit.updateDirection();
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
        if(isEmpty(unit.getX() + delX, unit.getY() + delY))
                return true;

        return false;
    }

    /**
     * Reserve a unit location for a given unit. This prevents units from attempting to move into that location.
     */
    public void reserve(int i, int j, SimpleUnit u){
        setUnit(u, i, j);
    }

    /**
     * Remove any reservations on this unit spot.
     */
    public void unreserve(int i, int j){
        if(unitGrid[i][j] instanceof ReserveUnit)
            unitGrid[i][j] = null;
    }

    public boolean isEmpty(int i, int j){
        return getUnit(i, j) == null && !(unitGrid[i][j] instanceof ReserveUnit);
    }

    public SimpleUnit getUnit(int i, int j){
        if(unitGrid[i][j] instanceof ReserveUnit)
            return null;

        return unitGrid[i][j];
    }

    public boolean spaceTaken(int i, int j){
        return getUnit(i, j) != null;
    }

    public void setUnit(SimpleUnit unit, int i, int j){
        unitGrid[i][j] = unit;
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
