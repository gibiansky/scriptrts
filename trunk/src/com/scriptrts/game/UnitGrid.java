package com.scriptrts.game;


public class UnitGrid {

	private int n;
	public UnitTile[][] unitGrid;
	
	public UnitGrid(int n) {
		this.n = n;
		unitGrid = new UnitTile[n][n];
	}

    public void moveUnitOneTile(SimpleUnit unit){
        UnitLocation loc = unit.getUnitLocation();
        Direction d = unit.getDirection();

        /* Figure out new unit location */
        UnitLocation newLoc = loc.move(d);
        
        /* Figure out if it's in a new tile */
        int tileshiftX = 0, tileshiftY = 0;
        if(loc == UnitLocation.Northwest && d == Direction.Northwest){
            tileshiftY = -2;
        }
        if(loc == UnitLocation.North && d == Direction.North){
            tileshiftY = -1;
            tileshiftX = (i % 2 == 0) ? 0 : 1;
        }
        if(loc == UnitLocation.Northeast && d == Direction.Northeast){
            tileshiftX = 1;
        }
        if(loc == UnitLocation.East && d == Direction.East){
            tileshiftY = 1;
            tileshiftX = (i % 2 == 0) ? 0 : -1;
        }
        if(loc == UnitLocation.Southeast && d == Direction.Southeast){
            tileshiftY = 2;
        }
        if(loc == UnitLocation.South && d == Direction.South){
            tileshiftY = 1;
            tileshiftX = (i % 2 == 0) ? -1 : 0;
        }
        if(loc == UnitLocation.Southwest && d == Direction.Southwest){
            tileshiftX = -1;
        }
        if(loc == UnitLocation.West && d == Direction.West){
            tileshiftY = -1;
            tileshiftX = (i % 2 == 0) ? -1 : 0;
        }

        int originalX = unit.getX();
        int originalY = unit.getY();

        /* If we're moving between tiles */
        if(tileshiftX != 0 || tileshiftY != 0){
            int finalX = originalX + tileshiftX;
            int finalY = originalY + tileshiftY;
            
            /* Remove unit from current tile placement */
            unitGrid[originalX][originalY].removeUnit(unit);
            if(unitGrid[originalX][originalY].isEmpty())
                unitGrid[originalX][originalY] = null;

            /* Add unit to new tile placement */
            if(unitGrid[finalX][finalY] == null)
                unitGrid[finalX][finalY] = new UnitTile();

            unitGrid[finalX][finalY].addUnit(unit);
        }
    }
}
