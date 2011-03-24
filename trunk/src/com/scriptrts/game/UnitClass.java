package com.scriptrts.game;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.scriptrts.core.Main;

/**
 * Represents fundamentally different types of units.
 */
public enum UnitClass {
    /* Standard unit: moves around, controlled by players, etc. */
    Standard,

    /* Unmoving building */
    Building,

    /* Not controlled by players, possibly not destructible */
    Terrain;
    
    public static GameObject createTerrain(Sprite[] sprites, int x, int y, boolean shaped) {
    	Player terrain = new Player("", new Color(255,255,255,1), -1);
        GameObject terrainObject = new GameObject(terrain, sprites, null, 0, x, y, Direction.North, shaped, UnitClass.Terrain);
        Main.getGame().grid.placeUnit(terrainObject);
        Main.getGame().unitManager.addUnit(terrainObject);
        return terrainObject;
    }
}
