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
        else 
            unit.setDirection(null);

        /* Place the unit, in its new orientation, on the map */
        placeUnit(unit, unit.getX(), unit.getY());

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
            if(spaceTakenFor(unit.getX() + delX + p.x, unit.getY() + delY + p.y, unit))
                    return false;

        return true;
    }

    /**
     * Reserve a unit location for a given unit. This prevents units from attempting to move into that location.
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

    private void reserveLoc(SimpleUnit unit, int i, int j){
        unitGrid[i][j] = new ReserveUnit(unit);
    }

    public SimpleUnit getUnit(int i, int j){
        if(unitGrid[i][j] instanceof ReserveUnit)
            return null;

        return unitGrid[i][j];
    }

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

    public boolean reserved(int i, int j){
        return unitGrid[i][j] instanceof ReserveUnit;
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

    public SimpleUnit getOriginal(){
        return reserve;
    }
}
