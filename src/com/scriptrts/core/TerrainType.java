package com.scriptrts.core;

import java.awt.Color;

/**
 * Enumeration of all types of terrain that appear on the maps.
 */
public enum TerrainType {
    /* Terrain types */
	Grass,
	Dirt,
	Sand,
	Rock,
	Water,
	DeepFire;

    /**
     * Get the color of this tile
     * @return what color to display this as on the minimap
     */
    public Color getMinimapColor(){
        switch(this){
            case Grass: return Color.green;
            case Dirt: return Color.orange;
            case Sand: return Color.yellow;
            case Rock: return Color.gray;
            case Water: return Color.blue;
            case DeepFire: return Color.black;
            default: return Color.white;
        }
    }
}
