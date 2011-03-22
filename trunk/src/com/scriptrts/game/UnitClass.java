package com.scriptrts.game;



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
}
