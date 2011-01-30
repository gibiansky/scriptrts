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

    public void moveUnitOneTile(SimpleUnit unit){
        Direction d = unit.getDirection();
        if(d == null) 
            return;

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

        setUnit(null, unit.getX(), unit.getY());
        unit.setX(unit.getX() + delX);
        unit.setY(unit.getY() + delY);
        setUnit(unit, unit.getX(), unit.getY());

        unit.updateDirection();
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
