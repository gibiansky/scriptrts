package com.scriptrts.game;

import java.awt.Point;
import com.scriptrts.core.*;
public enum Direction {
	North,
	Northeast,
	East,
	Southeast,
	South,
	Southwest, 
	West,
	Northwest;

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
