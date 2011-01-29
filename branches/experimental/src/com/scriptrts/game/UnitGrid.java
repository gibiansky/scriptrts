package com.scriptrts.game;

import java.awt.Point;
import com.scriptrts.core.UnitLocation;

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
        int i = (int) unit.getY();
        
        /* Figure out if it's in a new tile */
        final Point north       = new Point(-1, i % 2 == 0 ? 0 : 1);
        final Point northeast   = new Point(0, 1);
        final Point east        = new Point(1, i % 2 == 0 ? 0 : 1);
        final Point southeast   = new Point(2, 0);
        final Point south       = new Point(1, i % 2 == 0 ? -1 : 0);
        final Point southwest   = new Point(0, -1);
        final Point west        = new Point(-1, i % 2 == 0 ? -1 : 0);
        final Point northwest   = new Point(-2, 0);
        final Point none        = new Point(0, 0);
        final Point[][] tileshifts = {
            /* Northwest */
            {north, north, none, none, none, west, west, northwest},
            /* West */
            {none, none, none, none, none, west, west, west},
            /* North */
            {north, north, none, none, none, none, none, north},
            /* Southwest */
            {none, none, none, south, south, southwest, west, west},
            /* Center */
            {none, none, none, none, none, none, none, none},
            /* Northeast */
            {north, northeast, east, east, none, none, none, north},
            /* South */
            {none, none, none, south, south, south, none, none},
            /* East */
            {none, east, east, east, none, none, none, none},
            /* Southeast */
            {none, east, east, southeast, south, south, none, none}
        };

        Point shift = tileshifts[loc.ordinal()][d.ordinal()];
        int tileshiftI = (int) (shift.getX());
        int tileshiftJ = (int) (shift.getY());

        int originalX = unit.getX();
        int originalY = unit.getY();

        /* Set new unit location (before moving tiles, so that the unit placer knows where to put the unit) */
        unit.setUnitLocation(newLoc);

        /* If we're moving between tiles */
        if(tileshiftI != 0 || tileshiftJ != 0){
            int finalX = originalX + tileshiftJ;
            int finalY = originalY + tileshiftI;
            
            /* Remove unit from current tile placement */
            unitGrid[originalY][originalX].removeUnit(unit);
            if(unitGrid[originalY][originalX].isEmpty())
                unitGrid[originalY][originalX] = null;

            /* Add unit to new tile placement */
            if(unitGrid[finalY][finalX] == null)
                unitGrid[finalY][finalX] = new UnitTile();

            unitGrid[finalY][finalX].addUnit(unit);

            unit.setX(finalX);
            unit.setY(finalY);
        }

        /* If we're not moving between tiles, just rearrange unit slots in the current tile to reflect the movement */
        else {
            UnitTile tile = unitGrid[originalY][originalX];
            for(int k = 0; k < tile.units.length; k++){
                if(k == newLoc.ordinal())
                    tile.units[k] = unit;
                else if(tile.units[k] == unit)
                    tile.units[k] = null;
            }
        }

    }
}
