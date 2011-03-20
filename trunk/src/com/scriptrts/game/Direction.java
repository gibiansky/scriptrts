package com.scriptrts.game;

import java.awt.Point;

import com.scriptrts.core.ui.MapPainter;

/**
 * Possible directions of movement on the unit grid
 */
public enum Direction {
    /* All possible directions (each cardinal and subcardinal direction) */
	North,
	Northeast,
	East,
	Southeast,
	South,
	Southwest, 
	West,
	Northwest;

    /**
     * Find how many pixels a unit shifts when it moves one space in the given direction
     * @param mapPainter painter object used to paint the map
     * @param direction which direction the unit is moving in
     * @return point containing the shift in x, y pixel coordinates
     */
    public static Point getShift(MapPainter mapPainter, Direction direction){
        if(direction == null)
            return new Point(0, 0);

		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();

        int shiftX = -1000, shiftY = -1000;
        switch(direction){
            case North:
                shiftX = tileX / 6;
                shiftY = -tileY / 6;
                break;
            case Northeast:
                shiftX = tileX / 3;
                shiftY = 0;
                break;
            case East:
                shiftX = tileX / 6;
                shiftY = tileY / 6;
                break;
            case Southeast:
                shiftX = 0;
                shiftY = tileY / 3;
                break;
            case South:
                shiftX = -tileX / 6;
                shiftY = tileY / 6;
                break;
            case Southwest:
                shiftX = -tileX / 3;
                shiftY = 0;
                break;
            case West:
                shiftX = -tileX / 6;
                shiftY = -tileY / 6;
                break;
            case Northwest:
                shiftX = 0;
                shiftY = -tileY / 3;

            default:
                break;
        }

        return new Point(shiftX, shiftY);
    }
}
