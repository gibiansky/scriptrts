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
        if(getUnit(unit.getX() + delX, unit.getY() + delY) != null)
            return false;

        setUnit(null, unit.getX(), unit.getY());
        unit.setX(unit.getX() + delX);
        unit.setY(unit.getY() + delY);
        setUnit(unit, unit.getX(), unit.getY());

        /* Check if the place where it's going is taken */
        Direction nextDirection = unit.peekNextDirection();
        if(nextDirection != null){
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
            if(getUnit(unit.getX() + delX, unit.getY() + delY) == null)
                unit.updateDirection();
            else
                unit.setDirection(null);
        }
        else
            unit.updateDirection();

        return true;
    }

    public boolean isEmpty(int i, int j){
        return getUnit(i, j) == null;
    }

    public SimpleUnit getUnit(int i, int j){
        return unitGrid[i][j];
    }

    public boolean spaceTaken(int i, int j){
        return getUnit(i, j) != null;
    }

    public void setUnit(SimpleUnit unit, int i, int j){
        unitGrid[i][j] = unit;
    }
}
