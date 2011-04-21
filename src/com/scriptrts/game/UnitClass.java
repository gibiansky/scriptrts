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
}
