package com.scriptrts.game;

import java.util.List;

/**
 * Unit object for unit types
 */
public class Unit {
    /**
     * Counter used to initialize IDs of new units 
     */
    private static int idCounter = 0;

    /**
     * ID of this unit (unique)
     */
    private int id;

    /**
     * The unit type name
     */
    private String name;

    /**
     * Number of hitpoints unit type starts with
     */
    private int hitpoints;

    /**
     * The armor bonus of this unit
     */
    private int armor;

    /**
     * The base attack of this unit
     */
    private int attack;

    /**
     * Check if the units are equal
     * @return true if the unit is the same instance
     */
    public boolean equals(Unit u){
        return u.id == id;
    }

    /**
     * Get the name of the unit type
     * @return unit type name
     */
    public String name(){
        return name;
    }

    /**
     * Get the current number of hitpoints
     * @return current hitpoints
     */
    public int hitpoints(){
        return hitpoints;
    }

    /**
     * Check whether this unit is alive
     * @return true if hitpoints are above 0
     */
    public boolean alive(){
        return (hitpoints > 0);
    }

    /**
     * Get the armor bonus
     * @return armor
     */
    public int armor(){
        return armor;
    }

    /**
     * Get the base attack
     * @return base attack
     */
    public int attack(){
        return attack;
    }


}